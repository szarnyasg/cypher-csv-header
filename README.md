# cypher-csv-header

[![Build Status](https://travis-ci.org/szarnyasg/cypher-csv-header.svg?branch=master)](https://travis-ci.org/szarnyasg/cypher-csv-header)

Little tool to generate Cypher commands from CSV headers compliant with the [Neo4j Import Tool's format](https://neo4j.com/docs/operations-manual/3.2/tools/import/file-header-format/).

## Goals

We recommend to use this tool for experimenting and functional testing. The performance of the import is currently untested, but it is probably much worse than the performance of dedicated import tools, such as the [command-line _import tool_](https://neo4j.com/docs/operations-manual/3.2/tools/import/) and the [_shell tools_](https://github.com/jexp/neo4j-shell-tools).

## Approach

* Generate a command that loads the CSV file line-by-line: `LOAD CSV FROM ... AS line`
* Use indices to select in a line. For example, if the third field of the header contains `age:int`, it will be translated to `toInt(line[2]) AS age`.
* Create nodes like: `CREATE ({name: name, age: age})`
* Create relationships: `MATCH (src {...}), (trg {...}) CREATE (src)-[...]->(trg)`
* Properties are converted to the appropriate type. For most types, this is a trivial operation (e.g. `toInt`, `toFloat`), for booleans, this requires a `CASE` clause (as `toBoolean` has just been accepted in [openCypher](https://github.com/opencypher/openCypher/blob/dfd89877107250d69856e9ef890873f6d6e6a3a8/cip/2.testable/CIP2016-07-07-Type-conversion-functions.adoc ) and not supported yet in Neo4j 3.2).
* Array are also supported: a field can be split along the `arrayDelimiter` specified by the client and converted to the appropriate type using a [list comprehension](https://neo4j.com/docs/developer-manual/3.2/cypher/syntax/lists/#cypher-list-comprehension).
* [ID spaces](https://neo4j.com/docs/operations-manual/3.2/tutorial/import-tool/#import-tool-id-handling) are handled by appending a postfix to the property names, e.g. `:ID(Forum)` results in the property `csv_internal_id_Forum`.

Note that unlike the _import tool_, this tool offers little fault-tolerance: faulty data will probably crash the `LOAD CSV` command.


## Todo list

* `:LABEL` and `:TYPE` are not yet supported, but they could be with APOC for [dynamic types](https://neo4j-contrib.github.io/neo4j-apoc-procedures/index32.html#_creating_data), such as `apoc.create.node` and `apoc.create.relationship`.
* Add indexing on ids to enhance performance.

## Notes

The `CsvHeaderToCypherConverter` class is intended for use cases when APOC is not available, but the `LOAD CSV` Cypher command works.

Note that this is a rare case, as `LOAD CSV` is considered [legacy in openCypher](https://github.com/opencypher/openCypher/blob/master/docs/standardisation-scope.adoc). However, we plan to support `LOAD CSV` in our [ingraph](http://docs.inf.mit.bme.hu/ingraph/) system.

## Related pages in the Neo4j doc

* [Developer guide](https://neo4j.com/developer/guide-import-csv/)
* [Operations manual - Tutorial](https://neo4j.com/docs/operations-manual/3.2/tutorial/import-tool/)
* [Operations manual - Tools](http://neo4j.com/docs/operations-manual/3.2/tools/)
  * [Introduction](http://neo4j.com/docs/operations-manual/3.2/tools/import/)
  * [File header format](https://neo4j.com/docs/operations-manual/3.2/tools/import/file-header-format/)
  * [Command line usage](https://neo4j.com/docs/operations-manual/3.2/tools/import/command-line-usage/)
