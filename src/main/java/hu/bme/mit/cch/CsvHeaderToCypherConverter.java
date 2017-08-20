package hu.bme.mit.cch;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * See the <a href=
 * "https://github.com/opencypher/openCypher/blob/dfd89877107250d69856e9ef890873f6d6e6a3a8/docs/style-guide.adoc">openCypher
 * style guide</a>.
 * 
 */
public class CsvHeaderToCypherConverter {

  /**
   * String formats to generate Cypher expressions that convert entries to the
   * appropriate type. Usage: {@code String.format(converter, dataEntry)}.
   * 
   * Property types are listed in the <a href=
   * "https://neo4j.com/docs/operations-manual/current/tools/import/file-header-format/">operations
   * manual for the import tool</a>
   * 
   * Note that Cypher currently supports <a href=
   * "https://github.com/opencypher/openCypher/blob/dfd89877107250d69856e9ef890873f6d6e6a3a8/cip/2.testable/CIP2016-07-07-Type-conversion-functions.adoc">
   * 3 conversions functions</a>: toString, toInt and toFloat
   */
  private static final Map<String, String> converters = Collections
      .unmodifiableMap(Stream.of(new SimpleEntry<>("INT", "toInt(%s)"), //
          new SimpleEntry<>("LONG", "toInt(%s)"), //
          new SimpleEntry<>("FLOAT", "toFloat(%s)"), //
          new SimpleEntry<>("DOUBLE", "toFloat(%s)"), // ??
          new SimpleEntry<>("BOOLEAN", "CASE toUpper(%s) WHEN 'TRUE' THEN true WHEN 'FALSE' THEN false END"),
          new SimpleEntry<>("BYTE", "toInt(%s)"), //
          new SimpleEntry<>("SHORT", "toInt(%s)"), //
          new SimpleEntry<>("CHAR", "%s"), //
          new SimpleEntry<>("STRING", "%s"), //
          new SimpleEntry<>("ID", "toInt(%s)") //
  ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

  /**
   * 
   * @param filename
   * @param header
   * @param labels
   * @param config
   * @return
   */
  public String convertNodes(final String filename, final String header, final Collection<String> labels,
      final CsvLoaderConfig config) {
    final List<CsvField> fields = CsvHeader.processHeader(header, config.getFieldTerminator(), config.getQuotationCharacter());

    final String cypherLabels = labels.stream().map(l -> String.format(":`%s`", l)).collect(Collectors.joining());

//    final Optional<String> idSpace = fields.stream().filter(f -> f.getType().equals(Constants.ID_FIELD))
//            .findFirst().get().getIdSpace();
//    final String idProperty = Constants.ID_PROPERTY + (idSpace.isPresent() ? ("_" + idSpace.get()) : "");

    final String cypherProperties = cypherProperties(fields);
    final String cypherOptionalSpace = !cypherLabels.isEmpty() && !cypherProperties.isEmpty() ? " " : "";

    final String createNodeClause = String.format("CREATE (%s%s%s)\n",
          cypherLabels,
          cypherOptionalSpace,
          cypherProperties);


    return createLoadCsvQuery(filename, config.getFieldTerminator(), fields, createNodeClause, config.isSkipHeaders()).toString();
  }

  /**
   * 
   * @param filename
   * @param header
   * @param label
   * @param config
   * @return
   */
  public String convertRelationships(final String filename, final String header, final String label,
      final CsvLoaderConfig config) {
    final List<CsvField> fields = CsvHeader.processHeader(header, config.getFieldTerminator(), config.getQuotationCharacter());

    final Optional<String> startIdSpace = fields.stream().filter(f -> f.getType().equals(Constants.START_ID_FIELD))
        .findFirst().get().getIdSpace();
    final Optional<String> endIdSpace = fields.stream().filter(f -> f.getType().equals(Constants.END_ID_FIELD))
        .findFirst().get().getIdSpace();

    final String createRelationshipsClause = String.format( //
        "MATCH\n" + //
        "  (src {`%s%s`: `%s`}),\n" + //
        "  (trg {`%s%s`: `%s`})\n" + //
        "CREATE\n" + //
        "  (src)-[:`%s` %s]->(trg)\n", //
        Constants.ID_PROPERTY, Constants.getPostfix(startIdSpace), Constants.START_ID_PROPERTY, //
        Constants.ID_PROPERTY, Constants.getPostfix(endIdSpace  ), Constants.END_ID_PROPERTY, //
        label, cypherProperties(fields));

    return createLoadCsvQuery(filename, config.getFieldTerminator(), fields, createRelationshipsClause, config.isSkipHeaders());
  }

  /**
   * Generate "LOAD CSV ... WITH ... CREATE ..." queries.
   * 
   * @param filename
   * @param fieldTerminator
   * @param fields
   * @param createGraphElementClause
   * @return
   */
  private String createLoadCsvQuery(final String filename, char fieldTerminator, List<CsvField> fields,
      final String createGraphElementClause, final boolean skipHeaders) {
    final StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append(loadCsvClause(filename, fieldTerminator));
    if (skipHeaders) {
      queryBuilder.append("WITH line\nSKIP 1\n");
    }
    queryBuilder.append(withPropertiesClause(fields));
    queryBuilder.append(createGraphElementClause);
    return queryBuilder.toString();
  }

  /**
   * Generates the "LOAD CSV ..." clause.
   * 
   * @param filename
   * @param fieldTerminator
   * @return
   */
  private String loadCsvClause(final String filename, final char fieldTerminator) {
    return String.format("LOAD CSV FROM 'file://%s' AS %s FIELDTERMINATOR '%s'\n", filename, Constants.LINE_VAR,
        fieldTerminator);
  }

  /**
   * Generates the "WITH ..." clause with properties, e.g. "line[2] AS name".
   * 
   * @param fields
   * @return
   */
  private String withPropertiesClause(final List<CsvField> fields) {
    final String withEntries = fields.stream().map(f -> {
      final String converter = converters.getOrDefault(f.getType(), "%s");

      final String lineEntry = String.format("%s[%d]", Constants.LINE_VAR, f.getIndex());

      final String variable;
      if (f.isArray()) {
        final String varConv = String.format(converter, "property");
        variable = String.format("[property IN %s | %s]", lineEntry, varConv);
      } else {
        variable = String.format(converter, lineEntry);
      }
      final String withEntry = String.format("  %s AS `%s`", variable, f.getName());

      return withEntry;
    }).collect(Collectors.joining(",\n"));

    return "WITH\n" + withEntries + "\n";
  }

  /**
   * Generates string that specifies properties of graph nodes/relationships in
   * Cypher, e.g.
   * 
   * <pre>
   * {name: name, age: age}
   * </pre>
   * 
   * @param attributes
   * @return
   */
  private String cypherProperties(List<CsvField> attributes) {
    final String cypherProperties = attributes.stream() //
        .map(a -> String.format("`%s`: `%s`", a.getName(), a.getName())) //
        .collect(Collectors.joining(", "));
    if ("".equals(cypherProperties)) {
      return "";
    } else {
      return "{" + cypherProperties + "}";
    }
  }

}
