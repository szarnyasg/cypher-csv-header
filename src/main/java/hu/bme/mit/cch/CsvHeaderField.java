package hu.bme.mit.cch;

import java.util.Optional;
import java.util.regex.Matcher;

public class CsvHeaderField {

    private final int index;
    private final String name;
    private final String type;
    private final boolean array;
    private final String idSpace;

    private CsvHeaderField(int index, String name, String type, boolean array, String idSpace) {
        super();
        this.index = index;
        this.name = name;
        this.type = type;
        this.array = array;
        this.idSpace = idSpace;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isArray() {
        return array;
    }

    public String getIdSpace() {
        return idSpace;
    }

    public boolean isMeta() {
        // TODO check TYPE/LABEL
        return Constants.LABEL_FIELD.equals(type) || Constants.TYPE_FIELD.equals(type);
    }

    @Override
    public String toString() {
        return String.format( //
                "AttributeEntry [index=%d, name=%s, type=%s, array=%s, idSpace=%s]", //
                index, name, type, array, Optional.ofNullable(idSpace).orElse("") //
        );
    }

    public static CsvHeaderField parse(final int index, final String attribute, final char quotationCharacter) {
        final String attributeCleaned = attribute.replaceAll(String.valueOf(quotationCharacter), "");

        final Matcher matcher = Constants.FIELD_PATTERN.matcher(attributeCleaned);
        final boolean success = matcher.find();

        final String name = extractGroup(matcher, "name");
        final String type = extractGroup(matcher, "fieldtype");
        final String idSpace = extractGroup(matcher, "idspace");

        final boolean isLabels = "LABEL".equals(type);

        // as a notable exception, the ':LABEL' header always denotes an array
        boolean array = Constants.ARRAY_PATTERN.equals(extractGroup(matcher, "array")) || isLabels;

        return new CsvHeaderField(index, name, type, array, idSpace);
    }

    private static String extractGroup(Matcher matcher, String groupName) {
        try {
            return matcher.group(groupName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
