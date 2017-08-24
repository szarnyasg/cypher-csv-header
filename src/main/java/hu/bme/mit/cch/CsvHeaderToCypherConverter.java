package hu.bme.mit.cch;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
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
                    new SimpleEntry<>("BOOLEAN", "CASE toLower(%s) WHEN 'true' THEN true WHEN 'false' THEN false END"),
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

        // The labels can be anything implementing the Collection interface which might not be mutable
        // Because this needs to be mutable, the labels are being copied to an ArrayList
        final List<String> mutableLabels = new ArrayList<>(labels);

        final Optional<String> idSpace = fields.stream()
                .filter(field -> Constants.ID_FIELD.equals(field.getType()))
                .filter(field -> field.getIdSpace() != null)
                .map(field -> Constants.IDSPACE_LABEL_PREFIX + field.getIdSpace())
                .findFirst();
        idSpace.ifPresent(mutableLabels::add);

        final String cypherLabels = mutableLabels.stream()
                .map(label -> String.format(":`%s`", label))
                .collect(Collectors.joining());

        final String cypherProperties = cypherProperties(fields);

        final String createNodeClause = String.format("CREATE (%s%s)\n",
                cypherLabels,
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

        final String startIdSpaceLabel = fields.stream()
                .filter(f -> Constants.START_ID_FIELD.equals(f.getType()))
                .findFirst()
                .map(CsvHeaderField::getIdSpace)
                .map(idspace -> ":" + Constants.IDSPACE_LABEL_PREFIX + idspace)
                .orElse("");

        final String endIdSpaceLabel = fields.stream()
                .filter(f -> Constants.END_ID_FIELD.equals(f.getType()))
                .findFirst()
                .map(CsvHeaderField::getIdSpace)
                .map(idspace -> ":" + Constants.IDSPACE_LABEL_PREFIX + idspace)
                .orElse("");

        final Collection<CsvHeaderField> edgeProperties = fields.stream()
                .filter(field -> !Constants.START_ID_FIELD.equals(field.getType()))
                .filter(field -> !Constants.END_ID_FIELD.equals(field.getType()))
                .collect(Collectors.toList());

        final String createRelationshipsClause = String.format( //
                "MATCH\n" + //
                        "  (src%s {`%s`: `%s`}),\n" + //
                        "  (trg%s {`%s`: `%s`})\n" + //
                        "CREATE\n" + //
                        "  (src)-[:`%s` %s]->(trg)\n", //
                startIdSpaceLabel, Constants.ID_ATTR, Constants.START_ID_ATTR, //
                endIdSpaceLabel, Constants.ID_ATTR, Constants.END_ID_ATTR,
                label, cypherProperties(edgeProperties));

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
        final String withEntries = fields.stream().map(field -> {
            final String converter = CONVERTERS.getOrDefault(field.getType(), "%s");
            final String lineEntry = String.format("%s[%d]", Constants.LINE_VAR, field.getIndex());

            final String variable;
            if (field.isArray()) {
                final String varConv = String.format(converter, "property");
                variable = String.format("[property IN %s | %s]", lineEntry, varConv);
            } else {
                variable = String.format(converter, lineEntry);
            }

            String identifier = field.getName();
            switch (field.getType()) {
                case Constants.ID_FIELD:
                    identifier = Constants.IDSPACE_ATTR_PREFIX;
                    break;
                case Constants.START_ID_FIELD:
                    identifier = Constants.START_ID_ATTR;
                    break;
                case Constants.END_ID_FIELD:
                    identifier = Constants.END_ID_ATTR;
                    break;
            }

            return String.format(
                    "  %s AS `%s`",
                    variable,
                    identifier);
        }).collect(Collectors.joining(",\n"));

        return "WITH\n" + withEntries + "\n";
    }

    /**
     * Generates string that specifies properties of graph nodes/relationships in
     * Cypher, e.g.
     * <p>
     * <pre>
     * {name: name, age: age}
     * </pre>
     *
     * @param fields
     * @return
     */
    private String cypherProperties(Collection<CsvHeaderField> fields) {
        final String cypherProperties = fields.stream()
                .map(field -> {
                    switch (field.getType()) {
                        case Constants.ID_FIELD:
                            return Constants.ID_FIELD;
                        case Constants.START_ID_FIELD:
                            return Constants.START_ID_FIELD;
                        case Constants.END_ID_FIELD:
                            return Constants.END_ID_FIELD;
                        default:
                            return field.getName();
                    }
                })
                .map(name -> String.format("`%s`: `%s`", name, name))
                .collect(Collectors.joining(", "));
        if ("".equals(cypherProperties)) {
            return "";
        } else {
            return " {" + cypherProperties + "}";
        }
    }



}
