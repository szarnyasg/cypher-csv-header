# cypher-csv-header

## Code

The development happens in https://github.com/szarnyasg/neo4j-apoc-procedures/settings/branches.

The related issue is https://github.com/neo4j-contrib/neo4j-apoc-procedures/issues/489.

## Approach

* Take CSVs files as an input: a set of files for nodes and a set of files for relationships.
* For each file, generate a command that loads it line-by-line with the `LOAD CSV FROM ... AS line` Cypher command.
* Use indices to select a field in a line. For example, if the third field of the header contains `:ID,name:STRING,age:INT`, the `age` field will be translated to `toInt(line[2]) AS age`.
* Create nodes with the `CREATE` Cypher clause: `CREATE ({name: name, age: age})`
* Create relationships by first matching their start and end nodes with `MATCH` and using `CREATE` to add the relationship: `MATCH (src {...}), (trg {...}) CREATE (src)-[...]->(trg)`
* Properties are converted to the type prescribed by the header. For most types, this is a trivial operation (e.g. `toInteger`, `toFloat`). For booleans, this requires a `CASE` clause (as `toBoolean` has just been accepted in [openCypher](https://github.com/opencypher/openCypher/blob/dfd89877107250d69856e9ef890873f6d6e6a3a8/cip/2.testable/CIP2016-07-07-Type-conversion-functions.adoc ) and not supported yet in Neo4j 3.2).
* Array are also supported: a field can be split along the `arrayDelimiter` specified by the client and converted to the appropriate type using a [list comprehension](https://neo4j.com/docs/developer-manual/3.2/cypher/syntax/lists/#cypher-list-comprehension).
* [ID spaces](https://neo4j.com/docs/operations-manual/3.2/tutorial/import-tool/#import-tool-id-handling) are handled by appending a postfix to the property names, e.g. `:ID(Forum)` results in the property `csv_internal_id_Forum`.
* The `:LABEL` and `:TYPE` fields are also supported. The `:LABEL` field might define an multiple labels, which are separated with the `arrayDelimiter` character (default value: `;`).

Note that unlike the _import tool_, this tool offers little fault-tolerance: any faulty data will crash the `LOAD CSV` command.

## Notes

The `CsvHeaderToCypherConverter` class is intended for use cases when APOC is not available, but the `LOAD CSV` Cypher command works.

## Related pages in the Neo4j doc

* [Developer guide](https://neo4j.com/developer/guide-import-csv/)
* [Operations manual - Tutorial](https://neo4j.com/docs/operations-manual/3.2/tutorial/import-tool/)
* [Operations manual - Tools](http://neo4j.com/docs/operations-manual/3.2/tools/)
  * [Introduction](http://neo4j.com/docs/operations-manual/3.2/tools/import/)
  * [File header format](https://neo4j.com/docs/operations-manual/3.2/tools/import/file-header-format/)
  * [Command line usage](https://neo4j.com/docs/operations-manual/3.2/tools/import/command-line-usage/)
