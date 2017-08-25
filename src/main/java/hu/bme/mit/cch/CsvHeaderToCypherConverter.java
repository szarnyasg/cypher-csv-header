package hu.bme.mit.cch;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
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
     *
     * Neo4j 3.2 considers toInt is deprecated and notes that it will be replaced with toInteger.
     */
    private static final Map<String, String> CONVERTERS = Collections
            .unmodifiableMap(Stream.of( //
                    new SimpleEntry<>("INT", "toInteger(%s)"), //
                    new SimpleEntry<>("LONG", "toInteger(%s)"), //
                    new SimpleEntry<>("FLOAT", "toFloat(%s)"), //
                    new SimpleEntry<>("DOUBLE", "toFloat(%s)"), // ??
                    new SimpleEntry<>("BOOLEAN", "CASE toLower(%s) WHEN 'true' THEN true WHEN 'false' THEN false END"),
                    new SimpleEntry<>("BYTE", "toInt(%s)"), //
                    new SimpleEntry<>("SHORT", "toInt(%s)"), //
                    new SimpleEntry<>("CHAR", "%s"), //
                    new SimpleEntry<>("STRING", "%s"), //
                    new SimpleEntry<>("ID", "toInteger(%s)"), //
                    new SimpleEntry<>("START_ID", "toInteger(%s)"), //
                    new SimpleEntry<>("END_ID", "toInteger(%s)") //
            ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));

    /**
     * @param filename
     * @param header
     * @param labels
     * @param config
     * @return
     */
    public List<String> convertNodes(final String filename, final String header, final Collection<String> labels,
                               final CsvLoaderConfig config) {
        final List<CsvHeaderField> fields = CsvHeaderFields.processHeader(header, config.getFieldTerminator(), config.getQuotationCharacter());

        final String createIndexes = String.format(
                "CREATE INDEX ON :%s(%s)",
                labels.iterator().next(),
                Constants.ID_ATTR
                );

        // The labels can be anything implementing the Collection interface which might not be mutable
        // Because this needs to be mutable, the labels are being copied to an ArrayList
        final List<String> mutableLabels = new ArrayList<>(labels);

        final Optional<String> idSpace = fields.stream()
                .filter(f -> Constants.ID_FIELD.equals(f.getType()))
                .filter(f -> f.getIdSpace() != null)
                .map(f -> Constants.IDSPACE_LABEL_PREFIX + f.getIdSpace())
                .findFirst();
        idSpace.ifPresent(mutableLabels::add);

        final String cypherProperties = cypherProperties(fields);

        final boolean staticallyCreated = fields.stream().filter(f -> Constants.LABEL_FIELD.equals(f.getType())).count() == 0;

        String createNodeClause;
        if (staticallyCreated) { // static node labels
            final String cypherLabels = mutableLabels.stream()
                    .map(label -> String.format(":`%s`", label))
                    .collect(Collectors.joining());

            createNodeClause = String.format("CREATE (%s {%s})\n",
                    cypherLabels,
                    cypherProperties);
        } else { // dynamic node labels
            final String cypherLabels = mutableLabels.stream()
                    .map(label -> String.format("'%s'", label))
                    .collect(Collectors.joining(", "));

            createNodeClause = String.format("CALL apoc.create.node([%s] + %s, {%s}) YIELD node RETURN count(node)",
                    cypherLabels,
                    Constants.LABEL_ATTR,
                    cypherProperties);
        }

        return Arrays.asList(
            createIndexes,
            createLoadCsvQuery(filename, fields, createNodeClause, config)
        );
    }

    /**
     * @param filename
     * @param header
     * @param type
     * @param config
     * @return
     */
    public String convertRelationships(final String filename, final String header, final String type,
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

        final String cypherProperties = cypherProperties(edgeProperties);

        String createRelationshipsClause =
                String.format(
                "MATCH\n" + //
                        "  (src%s {`%s`: `%s`}),\n" + //
                        "  (trg%s {`%s`: `%s`})\n",
                        startIdSpaceLabel, Constants.ID_ATTR, Constants.START_ID_ATTR, //
                        endIdSpaceLabel, Constants.ID_ATTR, Constants.END_ID_ATTR //
                );
        
        final boolean staticallyCreated = fields.stream().filter(f -> Constants.TYPE_FIELD.equals(f.getType())).count() == 0;
        if (staticallyCreated) { // static relationship type
            createRelationshipsClause += String.format( //
                            "CREATE\n" + //
                            "  (src)-[:`%s` {%s}]->(trg)\n", //
                    type, cypherProperties);
        } else { // dynamic relationship type
            createRelationshipsClause += String.format(
                        "CALL apoc.create.relationship(src, %s, {%s}, trg) YIELD rel RETURN count(rel)",
                        Constants.TYPE_ATTR,
                        cypherProperties
                    );
        }


        return createLoadCsvQuery(filename, fields, createRelationshipsClause, config);
    }

    /**
     * Generate "LOAD CSV ... WITH ... CREATE ..." queries.
     *
     * @return
     */
    private String createLoadCsvQuery(final String filename, List<CsvHeaderField> fields,
                                      final String createGraphElementClause, final CsvLoaderConfig config) {
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format(
                "LOAD CSV FROM 'file://%s' AS %s FIELDTERMINATOR '%s'\n",
                filename,
                Constants.LINE_VAR,
                config.getFieldTerminator()));
        if (config.isSkipHeaders()) {
            queryBuilder.append("WITH line\nSKIP 1\n");
        }
        queryBuilder.append(withPropertiesClause(fields, config.getArrayDelimiter()));
        queryBuilder.append(createGraphElementClause);
        return queryBuilder.toString();
    }

    /**
     * Generates the "WITH ..." clause with properties, e.g. "line[2] AS name".
     *
     * @param fields
     * @return
     */
    private String withPropertiesClause(final List<CsvHeaderField> fields, final char arrayDelimiter) {
        final String withEntries = fields.stream()
            .map(f -> {
                final String converter = CONVERTERS.getOrDefault(f.getType(), "%s");
                final String lineEntry = String.format("%s[%d]", Constants.LINE_VAR, f.getIndex());

                final String variable;
                if (f.isArray()) {
                    final String splitter = String.format("split(%s, '%s')", lineEntry, arrayDelimiter);
                    final String varConv = String.format(converter, "property");
                    variable = String.format("[property IN %s | %s]", splitter, varConv);
                } else {
                    variable = String.format(converter, lineEntry);
                }

                String identifier = nameAndTypeToAttribute(f.getName(), f.getType());

                return String.format(
                        "  %s AS `%s`",
                        variable,
                        identifier);
            }).collect(Collectors.joining(",\n"));

        return "WITH\n" + withEntries + "\n";
    }

    private String nameAndTypeToAttribute(String name, String type) {
        switch (type) {
            case Constants.ID_FIELD:       return Constants.ID_ATTR; //Constants.IDSPACE_ATTR_PREFIX;
            case Constants.START_ID_FIELD: return Constants.START_ID_ATTR;
            case Constants.END_ID_FIELD:   return Constants.END_ID_ATTR;
            case Constants.LABEL_FIELD:    return Constants.LABEL_ATTR;
            case Constants.TYPE_FIELD:     return Constants.TYPE_ATTR;
        }
        return name;
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
        return fields.stream()
                .filter(f -> !f.isMeta())
                .map(f -> nameAndTypeToAttribute(f.getName(), f.getType()))
                .map(name -> String.format("`%s`: `%s`", name, name))
                .collect(Collectors.joining(", "));
    }

}
