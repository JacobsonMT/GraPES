package com.jacobsonmt.mags.server.repositories;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jacobsonmt.mags.server.exceptions.ResultFileException;
import com.jacobsonmt.mags.server.model.JobDO;
import com.jacobsonmt.mags.server.model.JobResult;
import com.jacobsonmt.mags.server.settings.ApplicationSettings;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * All in progress/failed jobs are cached in memory.
 * Successfully completed jobs are cached in memory based on LRU eviction and persisted to disk.
 */
@Log4j2
@Component
public class CaffeineJobRepository implements JobRepository {

    ApplicationSettings applicationSettings;

    // Contains map of token to saved job for future viewing
    private final Cache<String, JobDO> cachedJobs;

    // Contains all userIds to set of jobIds, this needs to be populated on startup
    private final Map<String, Map<String, Set<String>>> clientIdToUserIdToJobIds = new ConcurrentHashMap<>();

    // Contains all existing jobIds, this needs to be populated on startup
    // Used so we don't have to run Files.exist for no reason. Not necessary but nice.
    private final Set<String> existingJobIds = ConcurrentHashMap.newKeySet();

    // Needed to correctly give weight to jobs in cache
    private final Set<String> persistedJobIds = ConcurrentHashMap.newKeySet();

    @Autowired
    public CaffeineJobRepository( ApplicationSettings applicationSettings ) {
        this.applicationSettings = applicationSettings;

        // Need to make sure unpersisted (incomplete) jobs can't be invalidated, so we assign them a weight of 0.
        // When they are eventually persisted we recache them so that their weight updates.
        cachedJobs = Caffeine.newBuilder()
                .maximumWeight( applicationSettings.getMaxCachedJobs() )
                .weigher( (String k, JobDO v) -> persistedJobIds.contains( k ) ? 1 : 0 )
                .build();

        if ( applicationSettings.isLoadJobsFromDisk() ) {
            loadJobsFromDisk();
        }
    }

    private void loadJobsFromDisk() {
        // Populate completed jobs from jobs folder
        Path jobsDirectory = Paths.get( applicationSettings.getJobsDirectory() );

        PathMatcher matcher =
                FileSystems.getDefault().getPathMatcher( "glob:**/" + applicationSettings.getJobSerializationFilename() );

        try {
            Files.walkFileTree( jobsDirectory, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile( Path path,
                                                  BasicFileAttributes attrs ) throws IOException {
                    if ( matcher.matches( path ) ) {
                        try ( ObjectInputStream ois = new ObjectInputStream( Files.newInputStream( path ) ) ) {
                            JobDO job = (JobDO) ois.readObject();

                            existingJobIds.add( job.getJobId() );
                            persistedJobIds.add( job.getJobId() );
                            clientIdToUserIdToJobIds
                                    .computeIfAbsent( job.getClientId(), k -> new ConcurrentHashMap<>() )
                                    .computeIfAbsent( job.getUserId(), k -> new HashSet<>() )
                                    .add( job.getJobId() );

                        } catch ( ClassNotFoundException e ) {
                            log.error( e );
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed( Path file, IOException exc )
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            } );
        } catch ( IOException e ) {
            log.error( e );
        }
    }

    @Override
    public JobDO getById( String id ) {

        JobDO cached = cachedJobs.getIfPresent( id );
        if ( cached != null ) {
            return cached;
        }

        if ( !existingJobIds.contains( id ) ) {
            return null;
        }

        Path path = Paths.get( applicationSettings.getJobsDirectory(), id, applicationSettings.getJobSerializationFilename() );

        if ( !Files.exists( path ) ) {
            return null;
        }

        log.debug( "Retrieving job from disk: " + id );

        try ( ObjectInputStream ois = new ObjectInputStream( Files.newInputStream( path ) ) ) {
            JobDO job = (JobDO) ois.readObject();

            // Add back important transient fields
            job.setJobsDirectory( path.getParent() );
            job.setInputFASTAContent( inputStreamToString( Files.newInputStream( job.getJobsDirectory().resolve( job.getInputFASTAFilename() ) ) ) );
            job.setPosition( null );
            job.setEmail( "" );
            job.setSaveExpiredDate( System.currentTimeMillis() + applicationSettings.getPurgeAfterHours() * 60 * 60 * 1000 );

            // Unset JobResult transient?
            try {
                job.setResult( JobResult.parseResultCSVStream(
                        Files.newInputStream( job.getJobsDirectory().resolve( job.getOutputCSVFilename() ) ) ) );
            } catch ( ResultFileException e ) {
                job.setResult( JobResult.createNullResult() );
            }

            cacheJob( job );

            return job;

        } catch ( ClassNotFoundException e ) {
            log.error( e );
        } catch ( NoSuchFileException ex) {
            log.debug( "No file found for: " + id );
        } catch ( FileNotFoundException ex) {
            log.warn( "File not accessible: " + id );
        } catch ( IOException ex) {
            log.error( "IO Error for: " + id, ex );
        }

        return null;
    }

    @Override
    public String getRawResultFileById( String id ) {
        Path path = Paths.get( applicationSettings.getJobsDirectory(), id, applicationSettings.getOutputCSVFilename() );

        if ( !Files.exists( path ) ) {
            return null;
        }

        try {
            return inputStreamToString( Files.newInputStream( path ) );
        } catch ( IOException e ) {
            log.error( e );
        }

        return null;
    }

    /**
     * @return All cached and persisted jobs for client + user
     */
    @Override
    public Stream<JobDO> allJobsForClientAndUser( String clientId, String userId) {
        Map<String, Set<String>> userjobs = clientIdToUserIdToJobIds.get( clientId );
        if ( userjobs != null) {
            Set<String> jobs = userjobs.get( userId );
            if ( jobs != null) {
                return jobs.stream().map( this::getById );
            }
        }

        return Stream.empty();
    }

    @Override
    public void delete( JobDO job ) {

        cachedJobs.invalidate( job.getJobId() );
        existingJobIds.remove( job.getJobId() );
        Map<String, Set<String>> userjobs = clientIdToUserIdToJobIds.get( job.getClientId() );
        if ( userjobs != null) {
            Set<String> jobs = userjobs.get( job.getUserId() );
            if ( jobs != null) {
                jobs.remove( job.getJobId() );
            } else {
                log.warn( "Empty jobs list for user (" + job.getUserId() + ") this should not be possible!" );
            }
        } else {
            log.warn( "Empty users list for client (" + job.getClientId() + ") this should not be possible!" );
        }

        // Delete serialization on disk
        try {
            Path serializedJob = job.getJobsDirectory().resolve( job.getJobSerializationFilename() );
            Files.deleteIfExists( serializedJob );
            persistedJobIds.remove( job.getJobId() );
        } catch ( IOException e ) {
            log.error(e);
        }
    }

    @Override
    public void persistJob( JobDO job ) {
        // Write metadata to job folder
        Path serializedJob = job.getJobsDirectory().resolve( job.getJobSerializationFilename() );
        try ( ObjectOutputStream oos = new ObjectOutputStream( Files.newOutputStream( serializedJob ) ) ) {
            oos.writeObject( job );
            persistedJobIds.add( job.getJobId() );
            cachedJobs.put( job.getJobId(), job ); // Needed to recalculate job cache weight
        } catch ( IOException e ) {
            log.error( "Failed to serialize job: " + job.getJobId(), e );
        }
    }

    @Override
    public void cacheJob( JobDO job ) {
        cachedJobs.put( job.getJobId(), job );
        existingJobIds.add( job.getJobId() );
        clientIdToUserIdToJobIds
                .computeIfAbsent( job.getClientId(), k -> new ConcurrentHashMap<>() )
                .computeIfAbsent( job.getUserId(), k -> new HashSet<>() )
                .add( job.getJobId() );
    }

    private static String inputStreamToString( InputStream inputStream) throws IOException {
        StringBuilder textBuilder = new StringBuilder();
        try ( Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName( StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }
}
