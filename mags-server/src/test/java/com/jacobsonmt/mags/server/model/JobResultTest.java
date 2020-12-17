package com.jacobsonmt.mags.server.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JobResultTest {

    private static String HEADER = String.join( "\t", JobResult.HEADER );

    private static String baseCSVResult = "sp|P07766|CD3E_\t1\tM\t41\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.0\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\t0.785\n" +
            "sp|P07766|CD3E_\t2\tQ\t7\t0.253307\t0.244276\t0.233177\t0.244276\t0.244276\t0.244276\t0.0\t0.317168\t0.244276\t0.281372\t0.244276\t0.232177\t0.30097\t0.244276\t0.244276\t0.272373\t0.244276\t0.244276\t0.244276\t0.244276\t0.244276\n";

    @Test
    public void parseSlim() {
        JobResult result = JobResult.createWithOnlyTaxa( new Taxa( "test_OX", 5, "test_name" ) );
        assertThat( result.getTaxa().getKey() ).isEqualTo( "test_OX" );
        assertThat( result.getTaxa().getId() ).isEqualTo( 5 );
        assertThat( result.getTaxa().getName() ).isEqualTo( "test_name" );
        assertThat( result.getBases() ).isNull();
    }

    @Test
    public void parseTaxaWhenExists() {
        String resultCSV = "OX\t9749\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getTaxa().getKey() ).isEqualTo( Taxa.KnownKeyTypes.OX.name() );
        assertThat( result.getTaxa().getId() ).isEqualTo( 9749 );
    }

    @Test
    public void parseTaxaWhenNotExists() {
        String resultCSV = HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getTaxa().getKey() ).isEqualTo( Taxa.KnownKeyTypes.missing_OX.name() );
        assertThat( result.getTaxa().getId() ).isEqualTo( -1 );
    }

    @Test
    public void parseTaxaWhenExistsAndCustom() {
        String resultCSV = "wsersdf_OX\t9749\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getTaxa().getKey() ).isEqualTo( "wsersdf_OX" );
        assertThat( result.getTaxa().getId() ).isEqualTo( 9749 );
    }

    @Test
    public void parseTaxaWhenExistsAndMalformed() {
        String resultCSV = "OX 9749\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getTaxa().getKey() ).isEqualTo( Taxa.KnownKeyTypes.malformed_OX.name() );
        assertThat( result.getTaxa().getId() ).isEqualTo( -1 );
    }

    @Test
    public void parseTaxaWhenJustData() {
        String resultCSV = baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getTaxa().getKey() ).isEqualTo( Taxa.KnownKeyTypes.malformed_OX.name() );
        assertThat( result.getTaxa().getId() ).isEqualTo( -1 );
    }

    @Test
    public void parseTaxaWhenExistsAndNoHeader() {
        String resultCSV = "OX\t9749\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getTaxa().getKey() ).isEqualTo( Taxa.KnownKeyTypes.OX.name() );
        assertThat( result.getTaxa().getId() ).isEqualTo( 9749 );
    }

    @Test
    public void parseTaxaWhenExistsAndNoData() {
        String resultCSV = "OX\t9749\n" + HEADER;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getTaxa().getKey() ).isEqualTo( Taxa.KnownKeyTypes.OX.name() );
        assertThat( result.getTaxa().getId() ).isEqualTo( 9749 );
    }

    @Test
    public void parseTaxa_WithSpeciesName() {
        String resultCSV = "OX\t9749\ttestSpecies\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getTaxa().getKey() ).isEqualTo( Taxa.KnownKeyTypes.OX.name() );
        assertThat( result.getTaxa().getId() ).isEqualTo( 9749 );
        assertThat( result.getTaxa().getName() ).isEqualTo( "testSpecies" );
    }

    @Test
    public void getResultCSVWhenCorrectFormat() {
        String resultCSV = "OX\t9749\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getResultCSV() ).isEqualTo( resultCSV );
    }

    @Test
    public void getResultCSVWhenCustomTaxa() {
        String resultCSV = "custom_OX\t9607\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getResultCSV() ).isEqualTo( resultCSV );
    }

    @Test
    public void getResultCSVWhenCMalformedTaxa() {
        String resultCSV = "OX 9607\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getResultCSV() ).isEqualTo( Taxa.KnownKeyTypes.malformed_OX + "\t-1\n" + HEADER + "\n" + baseCSVResult );
    }

    @Test
    public void getResultCSVWhenMissingTaxa() {
        String resultCSV = HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getResultCSV() ).isEqualTo( Taxa.KnownKeyTypes.missing_OX + "\t-1\n" + HEADER + "\n" + baseCSVResult );
    }

    @Test
    public void getResultCSVWhenMalformedHeader() {
        String resultCSV = "OX\t9749\n" + "wrong\theaders\ttest\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getResultCSV() ).isEqualTo( "OX\t9749\n" + HEADER + "\n" + baseCSVResult );
    }

    @Test
    public void getResultCSVWhenMissingHeader() {
        String resultCSV = "OX\t9749\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getResultCSV() ).isEqualTo( "OX\t9749\n" + HEADER + "\n" + baseCSVResult );
    }

    @Test
    public void getResultCSVWhenNoBases() {
        String resultCSV = "OX\t9749\n" + HEADER + "\n";
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getResultCSV() ).isEqualTo( resultCSV );
    }

    @Test
    public void getResultCSV_WithSpeciesName() {
        String resultCSV = "OX\t9749\ttestSpecies\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getResultCSV() ).isEqualTo( resultCSV );
    }

    @Test
    public void getSequenceWhenCorrectFormat() {
        String resultCSV = "OX\t9749\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        validateSequence( result.getBases() );
    }

    @Test
    public void getSequenceWhenCustomTaxa() {
        String resultCSV = "custom_OX\t9607\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        validateSequence( result.getBases() );
    }

    @Test
    public void getSequenceWhenCMalformedTaxa() {
        String resultCSV = "OX 9607\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        validateSequence( result.getBases() );
    }

    @Test
    public void getSequenceWhenMissingTaxa() {
        String resultCSV = HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        validateSequence( result.getBases() );
    }

    @Test
    public void getSequenceWhenMalformedHeader() {
        String resultCSV = "OX\t9749\n" + "wrong\theaders\ttest\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        validateSequence( result.getBases() );
    }

    @Test
    public void getSequenceWhenMissingHeader() {
        String resultCSV = "OX\t9749\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        validateSequence( result.getBases() );
    }

    @Test
    public void getSequenceWhenNoBases() {
        String resultCSV = "OX\t9749\n" + HEADER + "\n";
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        assertThat( result.getBases() ).isEmpty();
    }

    @Test
    public void getSequence_WithSpeciesName() {
        String resultCSV = "OX\t9749\ttestSpecies\n" + HEADER + "\n" + baseCSVResult;
        InputStream resultCSVInputStream = new ByteArrayInputStream(resultCSV.getBytes( StandardCharsets.UTF_8 ));
        JobResult result = JobResult.parseResultCSVStream( resultCSVInputStream );
        validateSequence( result.getBases() );
    }

    private void validateSequence( List<Base> testSequence ) {
        assertThat( testSequence ).hasSize( 2 );

        assertThat( testSequence.get( 0 ).getReference() ).isEqualTo( "M" );
        assertThat( testSequence.get( 0 ).getDepth() ).isEqualTo( 41 );
        assertThat( testSequence.get( 0 ).getConservation() ).isEqualTo( 0.785 );

        assertThat( testSequence.get( 1 ).getReference() ).isEqualTo( "Q" );
        assertThat( testSequence.get( 1 ).getDepth() ).isEqualTo( 7 );
        assertThat( testSequence.get( 1 ).getConservation() ).isEqualTo( 0.253307 );
    }

}
