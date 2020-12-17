package com.jacobsonmt.mags.server.repositories;

import com.jacobsonmt.mags.server.model.JobDO;

import java.util.stream.Stream;

public interface JobRepository {

    JobDO getById( String id );

    String getRawResultFileById( String id );

    Stream<JobDO> allJobsForClientAndUser( String clientId, String userId);

    void delete( JobDO job );

    void persistJob( JobDO job );

    void cacheJob( JobDO job );

}
