package hu.bme.mit.cch;

/**
 * Immutable class to store the configuration for loading the CSV file. Inspired by the import tool's
 * <a href="http://neo4j.com/docs/operations-manual/current/tools/import/command-line-usage/">command line options</a>.
 *
 */
public class CsvLoaderConfig {

  private final char fieldTerminator;
  private final char arrayDelimiter;
  private final char quotationCharacter;
  private final boolean stringIds;

  private CsvLoaderConfig(Builder builder) {
    this.fieldTerminator = builder.fieldTerminator;
    this.arrayDelimiter = builder.arrayDelimiter;
    this.quotationCharacter = builder.quotationCharacter;
    this.stringIds = builder.stringIds;
  }

  public char getFieldTerminator() {
    return fieldTerminator;
  }
  
  public char getArrayDelimiter() {
    return arrayDelimiter;
  }
  
  public char getQuotationCharacter() {
    return quotationCharacter;
  }
  
  public boolean isStringIds() {
    return stringIds;
  }
  
  /**
   * Creates builder to build {@link CsvLoaderConfig}.
   * @return created builder
   */
  public static Builder builder() {
    return new Builder();
  }
  
  /**
   * Builder to build {@link CsvLoaderConfig}.
   */
  public static final class Builder {
    private char fieldTerminator = ',';
    private char arrayDelimiter = ';';
    private char quotationCharacter = '"';
    private boolean stringIds = true;

    private Builder() {
    }

    public Builder fieldTerminator(char fieldTerminator) {
      this.fieldTerminator = fieldTerminator;
      return this;
    }

    public Builder arrayDelimiter(char arrayDelimiter) {
      this.arrayDelimiter = arrayDelimiter;
      return this;
    }

    public Builder stringIds(boolean stringIds) {
      this.stringIds = stringIds;
      return this;
    }

    public CsvLoaderConfig build() {
      return new CsvLoaderConfig(this);
    }
  }
  
}
