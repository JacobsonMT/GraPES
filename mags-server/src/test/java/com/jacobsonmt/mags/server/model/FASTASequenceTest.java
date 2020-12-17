package com.jacobsonmt.mags.server.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.jacobsonmt.mags.server.exceptions.FASTAValidationException;
import java.util.HashSet;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class FASTASequenceTest {

    @Test
    public void parseSingleSequenceSingleLine() {
        String fastaContent = ">Example Header\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();
        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW" );
        assertThat( seq.getFASTAContent() ).isEqualTo( fastaContent );
        assertThat( seq.getValidationStatus() ).isEqualTo( "" );
    }

    @Test
    public void parseNoEndingNewline() {
        String fastaContent = ">Example Header\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();
        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW" );
        assertThat( seq.getFASTAContent() ).isEqualTo( ">Example Header\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" );
        assertThat( seq.getValidationStatus() ).isEqualTo( "" );
    }

    @Test
    public void parseStartsWithNewlines() {
        String fastaContent = "\n\n\n>Example Header\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();
        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW" );
        assertThat( seq.getFASTAContent() ).isEqualTo( ">Example Header\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" );
        assertThat( seq.getValidationStatus() ).isEqualTo( "" );
    }

    @Test
    public void parseSingleSequenceMultipleLines() {
        String fastaContent = ">Example Header\nMQSGTHWRVLGLCLLSV\nGVWGQDGNEEMGGITQTPYKV\nSISGTTVILTCPQYPGSEILW\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();
        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW" );
        assertThat( seq.getFASTAContent() ).isEqualTo( ">Example Header\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" );
        assertThat( seq.getValidationStatus() ).isEqualTo( "" );
    }

    @Test
    public void parseMultipleSequencesSingleLine() {
        String fastaContent = ">Example Header\n" +
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" +
                ">Example Header2\n" +
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        Set<FASTASequence> expected = new HashSet<>();
        expected.add( new FASTASequence( "Example Header",
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW", "" ) );
        expected.add( new FASTASequence( "Example Header2",
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM", "" ) );

        Assertions.assertThat( result ).containsExactlyInAnyOrderElementsOf( expected );
    }

    @Test
    public void parseMultipleSequencesEmptyLines() {
        String fastaContent = ">Example Header\n" +
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" +
                ">Example Header2\n" +
                "\n" +
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        Set<FASTASequence> expected = new HashSet<>();
        expected.add( new FASTASequence( "Example Header",
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW", "" ) );
        expected.add( new FASTASequence( "Example Header2",
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM", "" ) );

        Assertions.assertThat( result ).containsExactlyInAnyOrderElementsOf( expected );
    }

    @Test
    public void parseMultipleSequencesMultipleLines() {
        String fastaContent = ">Example Header\n" +
                "MQSGTHWRVLGLCLLSVGVWG\nQDGNEEMGGITQTPYKVSISGTT\nVILTCPQYPGSEILW\n" +
                ">Example Header2\n" +
                "KNIGGDEDDKNIG\nSDEDHLSLKEFSELEQSGYYVC\nYPRGSKPEDANFYLYLRARVCENCMEM\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        Set<FASTASequence> expected = new HashSet<>();
        expected.add( new FASTASequence( "Example Header",
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW", "" ) );
        expected.add( new FASTASequence( "Example Header2",
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM", "" ) );

        Assertions.assertThat( result ).containsExactlyInAnyOrderElementsOf( expected );
    }

    @Test
    public void parseEmpty() {
        String fastaContent = "";
        try {
            Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
            fail( "Expected FASTAValidationException" );
        } catch ( FASTAValidationException expected ) {
            // Expected
        }
    }

    @Test
    public void parseBlank() {
        String fastaContent = "   \n    \n\n\n   ";
        try {
            Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
            fail( "Expected FASTAValidationException" );
        } catch ( FASTAValidationException expected ) {
            // Expected
        }
    }

    @Test
    public void parseMalformedHeaderNoGoodSequences() {
        String fastaContent = "Example Header\nMQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n";
        try {
            Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
            fail( "Expected FASTAValidationException" );
        } catch ( FASTAValidationException expected ) {
            // Expected
        }
    }

    @Test
    public void parseMalformedHeaderOtherGoodSequences() {
        String fastaContent = "Example Header\n" +
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" +
                ">Example Header2\n" +
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).containsExactly( new FASTASequence( "Example Header2",
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM", "" ) );
    }

    @Test
    public void parseMalformedHeaderExtraSymbols() {
        String fastaContent = ">>>>Example Header\n" +
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );

        Assertions.assertThat( result ).containsExactly( new FASTASequence( ">>>Example Header",
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW", "" ) );
    }

    @Test
    public void parseMissingSequence() {
        String fastaContent = ">Example Header\n" +
                ">Example Header2\n" +
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header2",
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM", "" );
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
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" +
                ">Example Header\n" +
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header",
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW", "" );
        Assertions.assertThat( result ).contains( expected );
        result.remove( expected );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();

        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM" );
        assertThat( seq.getValidationStatus() ).isNotBlank();
    }

    @Test
    public void parseSequenceTooShort() {
        String shortSequence = new String(new char[FASTASequence.MINIMUM_SEQUENCE_SIZE - 1]).replace('\0', 'M');
        String fastaContent = ">Example Header\n" +
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" +
                ">Example Header2\n" +
                shortSequence + "\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header",
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW", "" );
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
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW\n" +
                ">Example Header2\n" +
                longSequence + "\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header",
                "MQSGTHWRVLGLCLLSVGVWGQDGNEEMGGITQTPYKVSISGTTVILTCPQYPGSEILW", "" );
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
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM\n";
        Set<FASTASequence> result = FASTASequence.parseFASTAContent( fastaContent );
        FASTASequence expected = new FASTASequence( "Example Header2",
                "KNIGGDEDDKNIGSDEDHLSLKEFSELEQSGYYVCYPRGSKPEDANFYLYLRARVCENCMEM", "" );
        Assertions.assertThat( result ).contains( expected );
        result.remove( expected );

        Assertions.assertThat( result ).hasSize( 1 );
        FASTASequence seq = result.iterator().next();

        assertThat( seq.getHeader() ).isEqualTo( "Example Header" );
        assertThat( seq.getSequence() ).isEqualTo( "MQSGTHWRVLGLCLLSVGVWGQDGNE111111111EMGGITQTPYKVSISGTT" );
        assertThat( seq.getValidationStatus() ).isNotBlank();
    }


}
