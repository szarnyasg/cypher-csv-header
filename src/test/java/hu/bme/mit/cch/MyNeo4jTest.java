package hu.bme.mit.cch;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryStatistics;
import org.neo4j.graphdb.Result;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;

public class MyNeo4jTest {

    @Test
    public void hello() {
        final String header = ":ID|name:STRING";

        final CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').stringIds(false).build();
        final CsvHeaderToCypherConverter c = new CsvHeaderToCypherConverter();
        final String cypher = c.convertNodes("/home/szarnyasg/git/cypher-csv-header/src/test/resources/basic.csv", header, Arrays.asList("Person"), config);

        System.out.println(cypher);

        final GraphDatabaseService gds = new TestGraphDatabaseFactory().newImpermanentDatabase();
        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());
    }

}
