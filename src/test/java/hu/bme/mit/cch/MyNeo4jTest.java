package hu.bme.mit.cch;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryStatistics;
import org.neo4j.graphdb.Result;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.net.URL;
import java.util.Arrays;

public class MyNeo4jTest {

    static String getResourceAbsolutePath(String testResource) {
        final URL resource = MyNeo4jTest.class.getClassLoader().getResource(testResource);
        System.out.println(resource);
        return resource.getPath();
    }

    @Test
    public void basicTest() {
        final String header = ":ID|name:STRING";

        final CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').stringIds(false).build();
        final CsvHeaderToCypherConverter c = new CsvHeaderToCypherConverter();

        final String cypher = c.convertNodes(getResourceAbsolutePath("basic.csv"), header, Arrays.asList("Person"), config);
        System.out.println(cypher);

        final GraphDatabaseService gds = new TestGraphDatabaseFactory().newImpermanentDatabase();
        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());
    }

    @Test
    public void arrayTest() {
        final String header = ":ID|name:STRING[]";

        final CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').arrayDelimiter(':').stringIds(false).build();
        final CsvHeaderToCypherConverter c = new CsvHeaderToCypherConverter();

        final String cypher = c.convertNodes(getResourceAbsolutePath("array.csv"), header, Arrays.asList("Person"), config);
        System.out.println(cypher);

        final GraphDatabaseService gds = new TestGraphDatabaseFactory().newImpermanentDatabase();
        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());
    }

}
