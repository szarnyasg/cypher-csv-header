package hu.bme.mit.cch;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * See the <a href=
 * "https://github.com/opencypher/openCypher/blob/dfd89877107250d69856e9ef890873f6d6e6a3a8/docs/style-guide.adoc">openCypher
 * style guide</a>.
 */
public class CsvHeaderToCypherConverter {

    /**
     * String formats to generate Cypher expressions that convert entries to the
     * appropriate type. Usage: {@code String.format(converter, dataEntry)}.
     * <p>
     * Property types are listed in the <a href=
     * "https://neo4j.com/docs/operations-manual/current/tools/import/file-header-format/">operations
     * manual for the import tool</a>
     * <p>
     * Note that Cypher currently supports <a href=
     * "https://github.com/opencypher/openCypher/blob/dfd89877107250d69856e9ef890873f6d6e6a3a8/cip/2.testable/CIP2016-07-07-Type-conversion-functions.adoc">
     * 3 conversions functions</a>: toString, toInt and toFloat
     */
    private static final Map<String, String> CONVERTERS = Collections
            .unmodifiableMap(Stream.of( //
                    new SimpleEntry<>("INT", "toInt(%s)"), //
                    new SimpleEntry<>("LONG", "toInt(%s)"), //
                    new SimpleEntry<>("FLOAT", "toFloat(%s)"), //
                    new SimpleEntry<>("DOUBLE", "toFloat(%s)"), // ??
                    new SimpleEntry<>("BOOLEAN", "CASE toUpper(%s) WHEN 'TRUE' THEN true WHEN 'FALSE' THEN false END"),
                    new SimpleEntry<>("BYTE", "toInt(%s)"), //
                    new SimpleEntry<>("SHORT", "toInt(%s)"), //
                    new SimpleEntry<>("CHAR", "%s"), //
                    new SimpleEntry<>("STRING", "%s"), //
                    new SimpleEntry<>("ID", "toInt(%s)") //
            ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));

    /**
     * @param filename
     * @param header
     * @param labels
     * @param config
     * @return
     */
    public String convertNodes(final String filename, final String header, final Collection<String> labels,
                               final CsvLoaderConfig config) {
        final List<CsvHeaderField> fields = CsvHeaderFields.processHeader(header, config.getFieldTerminator(), config.getQuotationCharacter());

        final String cypherLabels = cypherLabels(labels, fields);
        final String cypherProperties = cypherProperties(fields);
        final String cypherOptionalSpace = !cypherLabels.isEmpty() && !cypherProperties.isEmpty() ? " " : "";

        final String createNodeClause = String.format("CREATE (%s%s%s)\n",
                cypherLabels,
                cypherOptionalSpace,
                cypherProperties);

        return createLoadCsvQuery(filename, config.getFieldTerminator(), fields, createNodeClause, config.isSkipHeaders());
    }

    /**
     * @param filename
     * @param header
     * @param label
     * @param config
     * @return
     */
    public String convertRelationships(final String filename, final String header, final String label,
                                       final CsvLoaderConfig config) {
        final List<CsvHeaderField> fields = CsvHeaderFields.processHeader(header, config.getFieldTerminator(), config.getQuotationCharacter());

        final String startIdSpace = fields.stream()
                .filter(f -> Constants.START_ID_FIELD.equals(f.getType()))
                .findFirst()
                .map(CsvHeaderField::getIdSpace)
                .orElse(Constants.END_ID_PROPERTY);

        final String endIdSpace = fields.stream()
                .filter(f -> Constants.END_ID_FIELD.equals(f.getType()))
                .findFirst()
                .map(CsvHeaderField::getIdSpace)
                .orElse(Constants.START_ID_PROPERTY);

        final String createRelationshipsClause = String.format( //
                "MATCH\n" + //
                        "  (src {`%s_%s`: `%s`}),\n" + //
                        "  (trg {`%s_%s`: `%s`})\n" + //
                        "CREATE\n" + //
                        "  (src)-[:`%s` %s]->(trg)\n", //
                Constants.ID_PROPERTY, startIdSpace, Constants.START_ID_PROPERTY, //
                Constants.ID_PROPERTY, endIdSpace, Constants.END_ID_PROPERTY, //
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
    private String createLoadCsvQuery(final String filename, char fieldTerminator, List<CsvHeaderField> fields,
                                      final String createGraphElementClause, final boolean skipHeaders) {
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format(
                "LOAD CSV FROM 'file://%s' AS %s FIELDTERMINATOR '%s'\n",
                filename,
                Constants.LINE_VAR,
                fieldTerminator));
        if (skipHeaders) {
            queryBuilder.append("WITH line\nSKIP 1\n");
        }
        queryBuilder.append(withPropertiesClause(fields));
        queryBuilder.append(createGraphElementClause);
        return queryBuilder.toString();
    }

    /**
     * Generates the "WITH ..." clause with properties, e.g. "line[2] AS name".
     *
     * @param fields
     * @return
     */
    private String withPropertiesClause(final List<CsvHeaderField> fields) {
        final String withEntries = fields.stream().map(f -> {
            final String converter = CONVERTERS.getOrDefault(f.getType(), "%s");
            final String lineEntry = String.format("%s[%d]", Constants.LINE_VAR, f.getIndex());

            final String variable;
            if (f.isArray()) {
                final String varConv = String.format(converter, "property");
                variable = String.format("[property IN %s | %s]", lineEntry, varConv);
            } else {
                variable = String.format(converter, lineEntry);
            }

            final String identifier = Constants.ID_FIELD.equals(f.getType()) ? Constants.IDSPACE_ATTR_PREFIX : f.getName();
            return String.format(
                    "  %s AS `%s`",
                    variable,
                    identifier);
        }).collect(Collectors.joining(",\n"));

        return "WITH\n" + withEntries + "\n";
    }

    private String cypherLabels(Collection<String> labels, List<CsvHeaderField> fields) {
        // The labels can be anything implementing the Collection interface which might not be mutable
        // Because this needs to be mutable, the labels are being copied to an ArrayList
        final List<String> mutableLabels = new ArrayList<>(labels);

        final Optional<String> idSpace = fields.stream()
                .filter(field -> Constants.ID_FIELD.equals(field.getType()))
                .filter(field -> field.getIdSpace() != null)
                .map(field -> Constants.IDSPACE_LABEL_PREFIX + field.getIdSpace())
                .findFirst();
        idSpace.ifPresent(mutableLabels::add);

        return mutableLabels.stream()
                .map(label -> String.format(":`%s`", label))
                .collect(Collectors.joining());
    }

    /**
     * Generates string that specifies properties of graph nodes/relationships in
     * Cypher, e.g.
     * <p>
     * <pre>
     * {name: name, age: age}
     * </pre>
     *
     * @param attributes
     * @return
     */
    private String cypherProperties(List<CsvHeaderField> fields) {
        final String cypherProperties = fields.stream()
                .map(field -> Constants.ID_FIELD.equals(field.getType()) ? Constants.IDSPACE_ATTR_PREFIX : field.getName())
                .map(name -> String.format("`%s`: `%s`", name, name))
                .collect(Collectors.joining(", "));
        if ("".equals(cypherProperties)) {
            return "";
        } else {
            return "{" + cypherProperties + "}";
        }
    }

}
