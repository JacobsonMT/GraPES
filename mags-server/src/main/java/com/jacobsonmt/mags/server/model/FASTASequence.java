package com.jacobsonmt.mags.server.model;

import com.jacobsonmt.mags.server.exceptions.FASTAValidationException;
import com.jacobsonmt.mags.server.exceptions.SequenceValidationException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Log4j2
public class FASTASequence {

    private final String header;
    private String sequence;
    private String validationStatus = "";

    public static final Set<Character> VALID_CHARACTERS = "QACDEFGHIKLMNPWRSTVYUOBJZ*X-.".chars()
            .mapToObj( e -> ( char ) e ).collect( Collectors.toSet() );

    public static final int MINIMUM_SEQUENCE_SIZE = 26;
    public static final int MAXIMUM_SEQUENCE_SIZE = 40000;

    public String getFASTAContent() {
        return ">" + header + '\n' + sequence + '\n';
    }

    public static Set<FASTASequence> parseFASTAContent( String fasta ) throws FASTAValidationException {

        try {

            // Remove all empty lines
            fasta = fasta.replaceAll( "(?m)^[ \t]*\r?\n", "" );

            if ( Strings.isBlank( fasta ) ) {
                throw new FASTAValidationException( "Empty FASTA" );
            }

            Set<FASTASequence> sequences = new LinkedHashSet<>();
            Set<String> headers = new HashSet<>();

            String[] sequenceStrings = fasta.split( "(^>)|(\\r?\\n>)" );
            log.debug( fasta );
            log.debug( Arrays.toString(sequenceStrings) );

            //Throw out first entry as it necessarily doesn't start with a >
            sequenceStrings[0] = "";

            if ( sequenceStrings.length == 1 ) {
                throw new FASTAValidationException( "Missing headers" );
            }

            for ( String sequenceString : sequenceStrings ) {

                if (sequenceString.isEmpty() ) {
                    continue;
                }

                String[] separatedHeader = sequenceString.split( "\\r?\\n", 2 );

                FASTASequence sequence = new FASTASequence( separatedHeader[0] );

                try {
                    if ( separatedHeader.length == 1 ) {
                        throw new SequenceValidationException( "Missing sequence: " + sequence.getHeader() );
                    }

                    sequence.setSequence( separatedHeader[1].replaceAll( "\\r|\\n", "" ) );

                    if ( headers.contains( sequence.getHeader() ) ) {
                        throw new SequenceValidationException( "Duplicate header line: " + sequence.getHeader() );
                    }

                    if ( sequence.getSequence().length() < MINIMUM_SEQUENCE_SIZE ) {
                        throw new SequenceValidationException( "Sequence too short; minimum size is " + MINIMUM_SEQUENCE_SIZE );
                    }

                    if ( sequence.getSequence().length() > MAXIMUM_SEQUENCE_SIZE ) {
                        throw new SequenceValidationException( "Sequence too long; maximum size is " + MAXIMUM_SEQUENCE_SIZE );
                    }

                    int idx = 0;
                    for ( Character c : sequence.getSequence().toCharArray() ) {
                        if ( !FASTASequence.VALID_CHARACTERS.contains( c ) ) {
                            throw new SequenceValidationException( "Unknown character (" + c + ") at position (" + idx + ")" );
                        }
                        idx++;
                    }

                } catch ( SequenceValidationException sve ) {
                    sequence.setValidationStatus( sve.getMessage() );
                }

                sequences.add( sequence );
                headers.add( sequence.getHeader() );
            }

            return sequences;
        } catch ( FASTAValidationException fve ) {
            throw fve;
        } catch ( Exception e ) {
            throw new FASTAValidationException( "Unknown validation error" );
        }

    }
}
