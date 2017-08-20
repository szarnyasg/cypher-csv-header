package hu.bme.mit.cch;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvField {

  private final int index;
  private final String name;
  private final String type;
  private final boolean array;
  private final Optional<String> idSpace;
  
  private CsvField(int index, String name, String type, boolean array, Optional<String> idSpace) {
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
  
  public Optional<String> getIdSpace() {
    return idSpace;
  }

  @Override
  public String toString() {
    return String.format("AttributeEntry [index=%d, name=%s, type=%s, array=%s, idSpace=%s]", index, name, type, array, idSpace.orElse(""));
  }
  
  private static final String IDENTIFIER_PATTERN = "[-a-zA-Z]";

  public static Optional<CsvField> from(final int index, final String attribute, final char quotationCharacter) {
    final String attributeCleaned = attribute.replaceAll(String.valueOf(quotationCharacter), "");

    if (attribute.toUpperCase().endsWith(":IGNORE")) {
      return Optional.empty();
    }

    // match for id fields, e.g. :ID(IdSpace), :START_ID(IdSpace1), :END_ID(IdSpace2)
    final Matcher idMatcher = Pattern.compile(
          String.format("^%s*:(ID|START_ID|END_ID)(\\((%s+)\\))?$", IDENTIFIER_PATTERN, IDENTIFIER_PATTERN)
        ).matcher(attributeCleaned);
    if (idMatcher.find()) {
      final String type = idMatcher.group(1);
      final Optional<String> idSpace = Optional.ofNullable(idMatcher.group(3));

      final String name = Constants.getPostfix(idSpace);

      return Optional.of(new CsvField(index, name, type, false, idSpace));
    }

    // match for normal fields, e.g. name:STRING, age:INT, name, languages:STRING[]
    final Matcher fieldMatcher = Pattern.compile(String.format("^(%s+)(:(%s+)(\\[\\])?)?$", IDENTIFIER_PATTERN, IDENTIFIER_PATTERN)).matcher(attributeCleaned);
    if (fieldMatcher.find()) {
      final String name = fieldMatcher.group(1);
      final String type = Optional.ofNullable(fieldMatcher.group(3)).orElse("STRING");
      final boolean array = Optional.ofNullable(fieldMatcher.group(4)).isPresent();

      return Optional.of(new CsvField(index, name, type, array, Optional.empty()));
    }

    throw new IllegalStateException(String.format("Header is not well-formed, field '%s' cannot be parsed", attribute));
  }

}
