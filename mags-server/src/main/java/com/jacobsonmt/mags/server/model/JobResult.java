package com.jacobsonmt.mags.server.model;

import com.jacobsonmt.mags.server.exceptions.ResultFileException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@Setter
public final class JobResult {

    protected static final String[] HEADER = {"AC","Pos","Ref","Depth","Conservation","A","R","N","D","C","Q","E","G","H","I","L","K","M","F","P","S","T","W","Y","V"};
    protected static final int HEADER_INFO_COL_CNT = 5; // All columns passed this column are part of the Base list object

    private final Taxa taxa;
    private final List<Base> bases;
    private final String accession;

    JobResult( Taxa taxa, String accession, List<Base> bases ) {
        this.taxa = taxa;
        this.accession = accession;
        this.bases = bases;
    }

    public String getResultCSV() {
        StringBuilder sb = new StringBuilder();
        if ( taxa != null ) {
            if (taxa.getKey() != null && !taxa.getKey().isEmpty()) {
                sb.append( taxa.getKey() );
            } else {
                sb.append( Taxa.KnownKeyTypes.malformed_OX );
            }

            sb.append( "\t" ).append( taxa.getId() );

            if ( taxa.getName() != null && !taxa.getName().isEmpty() ) {
                sb.append( "\t" ).append( taxa.getName() );
            }

            sb.append( "\n" );
        }

        sb.append( String.join( "\t", HEADER ) ).append( "\n" );

        if ( bases != null) {
            int i = 1;
            for ( Base base : bases ) {
                sb
                        .append( accession ).append( "\t" )
                        .append( i ).append( "\t" )
                        .append( base.getReference() ).append( "\t" )
                        .append( base.getDepth() ).append( "\t" )
                        .append( base.getConservation() ).append( "\t" )
                        .append( base.getList().stream().map( String::valueOf ).collect( Collectors.joining( "\t" ) ) )
                        .append( "\n" );
                i++;
            }
        }
        return sb.toString();
    }

    public static JobResult createNullResult() {
        return new JobResult( null, null, null );
    }

    static JobResult createWithOnlyTaxa( Taxa taxa ) {
        return new JobResult( taxa, null,null );
    }

    public static JobResult parseResultCSVStream( InputStream resultCSVStream ) throws ResultFileException {
        // Parse first line
        int tid = -1;
        String tkey = "";
        String tname = "";
        List<Base> sequence = new ArrayList<>();
        String accession = "";
        boolean foundHeader = false;

        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(resultCSVStream, StandardCharsets.UTF_8 ))) {

            // Assumptions:
            // Taxa line might not be present
            // Header line will always be present

            // Taxa Line
            String line = reader.readLine();
            if ( line != null ) {
                String[] sline = line.split( "\t" );

                if ( sline.length == 1 ) {
                    tkey = Taxa.KnownKeyTypes.malformed_OX.name();
                }

                if ( sline.length > 1 ) {
                    tkey = sline[0];
                    try {
                        tid = Integer.parseInt( sline[1] );
                    } catch ( NumberFormatException nfe ) {
                        log.warn( "Server Error: Malformed result file taxa line: " + line );
                        tid = -1;
                    }
                }

                if (sline.length > 2) {
                    tname = sline[2];
                }

                if (sline.length > 3) {
                    log.warn( "Server Error: Malformed or missing result file taxa line: " + line );
                    tid = -1;
                    tname = "";

                    // Is this is the header line?
                    if ( Arrays.equals(sline, HEADER) ) {
                        tkey = Taxa.KnownKeyTypes.missing_OX.name();
                        foundHeader = true;
                    } else {
                        tkey = Taxa.KnownKeyTypes.malformed_OX.name();
                    }
                }
            } else {
                throw new ResultFileException( "Server Error: No data" );
            }

            // Read until header is found
            while ( !foundHeader && ( line = reader.readLine() ) != null ) {
                String[] sline = line.split( "\t" );
                // is this the header?
                if ( Arrays.equals( sline, HEADER ) ) {
                    foundHeader = true;
                } else {
                    try {
                        Base base = mapBase( sline );

                        // There was no header
                        accession = sline[0];
                        sequence.add( base );
                        foundHeader = true;
                    } catch ( Exception e ) {
                        // Not a base line, keep looking for header
                    }
                }
            }

            if ( foundHeader ) {

                // Separately parse first line to grab constant value of accession in first column
                line = reader.readLine();
                if ( line != null ) {
                    String[] sline = line.split( "\t" );
                    sequence.add( mapBase( sline ) );
                    accession =  sline[0];
                }

                while ( ( line = reader.readLine() ) != null ) {
                    sequence.add( mapBase( line.split( "\t" ) ) );
                }
            }

        } catch ( Exception e ) {
            log.error( e );
            throw new ResultFileException( "Server Error: Something went wrong parsing the result file" );
        }

        return new JobResult( new Taxa( tkey, tid, tname ), accession, sequence );
    }

    private static Base mapBase(String[] splitLine ) {
        Base base = new Base( splitLine[2], Integer.parseInt( splitLine[3] ), Double.parseDouble( splitLine[4] ) );

        if ( splitLine.length > HEADER_INFO_COL_CNT ) {
            base.setList( Arrays.stream( splitLine ).skip( HEADER_INFO_COL_CNT )
                    .map( Double::parseDouble )
                    .collect( Collectors.toList() )
            );
        }

        return base;
    }
}
