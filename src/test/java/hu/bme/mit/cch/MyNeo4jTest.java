package hu.bme.mit.cch;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryStatistics;
import org.neo4j.graphdb.Result;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

public class MyNeo4jTest {

    static String getResourceAbsolutePath(String testResource) {
        final URL resource = MyNeo4jTest.class.getClassLoader().getResource(testResource);
        System.out.println(resource);
        return resource.getPath();
    }

    @Test
    public void basicTest() {
        final String header = "name:STRING";

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
    public void idTest() {
        final String header = ":ID|name:STRING";

        final CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').stringIds(false).build();
        final CsvHeaderToCypherConverter c = new CsvHeaderToCypherConverter();

        final String cypher = c.convertNodes(getResourceAbsolutePath("id.csv"), header, Arrays.asList("Person"), config);
        System.out.println(cypher);

        final GraphDatabaseService gds = new TestGraphDatabaseFactory().newImpermanentDatabase();
        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute("MATCH (n) WHERE n.ID = 1 RETURN COUNT(*) AS c");
        Assert.assertEquals(1, checkExecute.next().get("c"));
    }

    @Test
    public void idSpaceTest() {
        final String header = ":ID(PERSONID)|name:STRING";

        final CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').stringIds(false).build();
        final CsvHeaderToCypherConverter c = new CsvHeaderToCypherConverter();

        final String cypher = c.convertNodes(getResourceAbsolutePath("id_space.csv"), header, Arrays.asList("Person"), config);
        System.out.println(cypher);

        final GraphDatabaseService gds = new TestGraphDatabaseFactory().newImpermanentDatabase();
        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute("MATCH (n) WHERE n._PERSONID = 1 RETURN count(*) AS c");
        Assert.assertEquals(1, checkExecute.next().get("c"));
    }

    @Test
    public void labelTest() {
        final String header = ":ID|:LABEL|name:STRING";

        final CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').stringIds(false).build();
        final CsvHeaderToCypherConverter c = new CsvHeaderToCypherConverter();

        final String cypher = c.convertNodes(getResourceAbsolutePath("label.csv"), header, Arrays.asList("Person"), config);
        System.out.println(cypher);

        final GraphDatabaseService gds = new TestGraphDatabaseFactory().newImpermanentDatabase();
        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute("MATCH (n:Person) RETURN COUNT(*) AS c");
        Assert.assertEquals(1, checkExecute.next().get("c"));
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
