package com.jacobsonmt.mags.server.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.jacobsonmt.mags.server.exceptions.FASTAValidationException;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class FASTASequenceTest {

    private final String OK_SEQUENCE = "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWCPQYPGMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILWSEILWMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW";
    private final String OK_SEQUENCE2 = "VILTMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITVILTMQSGTHWRVLGLCLLSVGVWVILTMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTGQDGNEEMGGITQTQTMQSGTHWRVLGLCLLSVGVWGVILTMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTQDGNEEMGGITQTPYKVSISGTTVILTMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGT";

    @Test
    public void parseSingleSequenceSingleLine() {
        String fastaContent = ">Example Header\n" + OK_SEQUENCE + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();
        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( OK_SEQUENCE );
        assertThat( seq.getFASTAContent() ).isEqualTo( fastaContent );
        assertThat( seq.getValidationStatus() ).isEqualTo( "" );
    }

    @Test
    public void parseNoEndingNewline() {
        String fastaContent = ">Example Header\n" + OK_SEQUENCE;
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();
        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( OK_SEQUENCE );
        assertThat( seq.getFASTAContent() ).isEqualTo( ">Example Header\n" + OK_SEQUENCE + "\n" );
        assertThat( seq.getValidationStatus() ).isEqualTo( "" );
    }

    @Test
    public void parseStartsWithNewlines() {
        String fastaContent = "\n\n\n>Example Header\n" + OK_SEQUENCE;
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();
        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( OK_SEQUENCE );
        assertThat( seq.getFASTAContent() ).isEqualTo( ">Example Header\n" + OK_SEQUENCE + "\n" );
        assertThat( seq.getValidationStatus() ).isEqualTo( "" );
    }

    @Test
    public void parseSingleSequenceMultipleLines() {
        String fastaContent = ">Example Header\n" + String.join("\n", OK_SEQUENCE.split("(?<=\\G.{4})")) + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();
        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( OK_SEQUENCE );
        assertThat( seq.getFASTAContent() ).isEqualTo( ">Example Header\n" + OK_SEQUENCE + "\n" );
        assertThat( seq.getValidationStatus() ).isEqualTo( "" );
    }

    @Test
    public void parseMultipleSequencesSingleLine() {
        String fastaContent = ">Example Header\n" +
                "" + OK_SEQUENCE + "\n" +
                ">Example Header2\n" +
                OK_SEQUENCE2 + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        List<FASTASequence> expected = new ArrayList<>();
        expected.add( new FASTASequence( "Example Header",
                OK_SEQUENCE, "" ) );
        expected.add( new FASTASequence( "Example Header2",
                OK_SEQUENCE2, "" ) );

        Assertions.assertThat( result ).containsExactlyInAnyOrderElementsOf( expected );
    }

    @Test
    public void parseMultipleSequencesEmptyLines() {
        String fastaContent = ">Example Header\n" +
                "" + OK_SEQUENCE + "\n" +
                ">Example Header2\n" +
                "\n" +
                OK_SEQUENCE2 + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        List<FASTASequence> expected = new ArrayList<>();
        expected.add( new FASTASequence( "Example Header",
                OK_SEQUENCE, "" ) );
        expected.add( new FASTASequence( "Example Header2",
                OK_SEQUENCE2, "" ) );

        Assertions.assertThat( result ).containsExactlyInAnyOrderElementsOf( expected );
    }

    @Test
    public void parseMultipleSequencesMultipleLines() {
        String fastaContent = ">Example Header\n" + String.join("\n", OK_SEQUENCE.split("(?<=\\G.{4})")) +
                "\n>Example Header2\n" + String.join("\n", OK_SEQUENCE2.split("(?<=\\G.{4})"));
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        List<FASTASequence> expected = new ArrayList<>();
        expected.add( new FASTASequence( "Example Header",
                OK_SEQUENCE, "" ) );
        expected.add( new FASTASequence( "Example Header2",
                OK_SEQUENCE2, "" ) );

        Assertions.assertThat( result ).containsExactlyInAnyOrderElementsOf( expected );
    }

    @Test
    public void parseEmpty() {
        String fastaContent = "";
        try {
            List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
            fail( "Expected FASTAValidationException" );
        } catch ( FASTAValidationException expected ) {
            // Expected
        }
    }

    @Test
    public void parseBlank() {
        String fastaContent = "   \n    \n\n\n   ";
        try {
            List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
            fail( "Expected FASTAValidationException" );
        } catch ( FASTAValidationException expected ) {
            // Expected
        }
    }

    @Test
    public void parseMalformedHeaderNoGoodSequences() {
        String fastaContent = "Example Header\n" + OK_SEQUENCE + "\n";
        try {
            List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
            fail( "Expected FASTAValidationException" );
        } catch ( FASTAValidationException expected ) {
            // Expected
        }
    }

    @Test
    public void parseMalformedHeaderOtherGoodSequences() {
        String fastaContent = "Example Header\n" +
                "" + OK_SEQUENCE + "\n" +
                ">Example Header2\n" +
                OK_SEQUENCE2 + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).containsExactly( new FASTASequence( "Example Header2",
                OK_SEQUENCE2, "" ) );
    }

    @Test
    public void parseMalformedHeaderExtraSymbols() {
        String fastaContent = ">>>>Example Header\n" +
                "" + OK_SEQUENCE + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).containsExactly( new FASTASequence( ">>>Example Header",
                OK_SEQUENCE, "" ) );
    }

    @Test
    public void parseMissingSequence() {
        String fastaContent = ">Example Header\n" +
                ">Example Header2\n" +
                OK_SEQUENCE2 + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header2",
                OK_SEQUENCE2, "" );
        Assertions.assertThat( result ).contains( expected );
        result.remove( expected );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();

        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getValidationStatus() ).isNotBlank();
    }

    @Test
    public void parseDuplicateHeaders() {
        String fastaContent = ">Example Header\n" +
                "" + OK_SEQUENCE + "\n" +
                ">Example Header\n" +
                OK_SEQUENCE2 + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header",
                OK_SEQUENCE, "" );
        Assertions.assertThat( result ).contains( expected );
        result.remove( expected );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();

        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( OK_SEQUENCE2 );
        assertThat( seq.getValidationStatus() ).isNotBlank();
    }

    @Test
    public void parseSequenceTooShort() {
        String shortSequence = new String(new char[FASTASequence.MINIMUM_SEQUENCE_SIZE - 1]).replace('\0', 'M');
        String fastaContent = ">Example Header\n" +
                "" + OK_SEQUENCE + "\n" +
                ">Example Header2\n" +
                shortSequence + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header",
                OK_SEQUENCE, "" );
        Assertions.assertThat( result ).contains( expected );
        result.remove( expected );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();

        assertThat( seq.getHeader() ).isEqualTo( "Example Header2" );
        assertThat( seq.getSequence() ).isEqualTo( shortSequence );
        assertThat( seq.getValidationStatus() ).isNotBlank();
    }

    @Test
    public void parseSequenceTooLong() {
        String longSequence = new String(new char[FASTASequence.MAXIMUM_SEQUENCE_SIZE + 1]).replace('\0', 'M');
        String fastaContent = ">Example Header\n" +
                "" + OK_SEQUENCE + "\n" +
                ">Example Header2\n" +
                longSequence + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header",
                OK_SEQUENCE, "" );
        Assertions.assertThat( result ).contains( expected );
        result.remove( expected );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();

        assertThat( seq.getHeader() ).isEqualTo( "Example Header2" );
        assertThat( seq.getSequence() ).isEqualTo( longSequence );
        assertThat( seq.getValidationStatus() ).isNotBlank();
    }

    @Test
    public void parseUnrecognizedCharacter() {
        String fastaContent = ">Example Header\n" +
                "MQSGTHWRVLGLCLLSVGVWGQDGNE111111111EMGGITQTPYKVSISGTT\n" +
                ">Example Header2\n" +
                OK_SEQUENCE2 + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header2",
                OK_SEQUENCE2, "" );
        Assertions.assertThat( result ).contains( expected );
        result.remove( expected );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();

        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( "MQSGTHWRVLGLCLLSVGVWGQDGNE111111111EMGGITQTPYKVSISGTT" );
        assertThat( seq.getValidationStatus() ).isNotBlank();
    }

    @Test
    public void parseDuplicateSequence() {
        String fastaContent = ">Example Header\n" +
            "" + OK_SEQUENCE + "\n" +
            ">Example Header2\n" +
            OK_SEQUENCE + "\n";
        List<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header",
            OK_SEQUENCE, "" );
        Assertions.assertThat( result ).contains( expected );
        result.remove( expected );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();

        assertThat( seq.getHeader() ).isEqualTo( "Example Header2" );
        assertThat( seq.getSequence() ).isEqualTo( OK_SEQUENCE );
        assertThat( seq.getValidationStatus() ).isNotBlank();
    }


}
