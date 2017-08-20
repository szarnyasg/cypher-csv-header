package hu.bme.mit.cch;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CsvHeader {

    /**
     * Processes CSV header. Works for both nodes and relationships.
     *
     * @param header
     * @param fieldTerminator
     * @param quotationCharacter
     * @return
     */
    public static List<CsvField> processHeader(final String header, final char fieldTerminator, final char quotationCharacter) {
        final String separatorRegex = Pattern.quote(String.valueOf(fieldTerminator));
        final List<String> attributes = Arrays.asList(header.split(separatorRegex));

        final List<CsvField> fieldEntries = //
                IntStream.range(0, attributes.size()) //
                        .mapToObj(i -> CsvField.from(i, attributes.get(i), quotationCharacter)) //
                        .flatMap(entry -> entry.isPresent() ? Stream.of(entry.get()) : Stream.empty()).collect(Collectors.toList());

        return fieldEntries;
    }

}
