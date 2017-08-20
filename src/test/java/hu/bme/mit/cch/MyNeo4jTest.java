package hu.bme.mit.cch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryStatistics;
import org.neo4j.graphdb.Result;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.net.URL;
import java.util.Arrays;

public class MyNeo4jTest {

    CsvLoaderConfig config;
    CsvHeaderToCypherConverter converter;
    GraphDatabaseService gds;

    static String getResourceAbsolutePath(String testResource) {
        final URL resource = MyNeo4jTest.class.getClassLoader().getResource(testResource);
        return resource.getPath();
    }

    @Before
    public void init() {
        config = CsvLoaderConfig.builder().fieldTerminator('|').stringIds(false).skipHeaders(false).build();
        converter = new CsvHeaderToCypherConverter();
        gds = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    @After
    public void shutdown() {
        gds.shutdown();
    }

    @Test
    public void basicTest() {
        final String header = "name:STRING";

        final String cypher = converter.convertNodes(getResourceAbsolutePath("basic.csv"), header, Arrays.asList("Person"), config);
        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());
    }

    @Test
    public void idTest() {
        final String header = ":ID|name:STRING";
        final String cypher = converter.convertNodes(getResourceAbsolutePath("id.csv"), header, Arrays.asList("Person"), config);

        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute(
                String.format("MATCH (n) WHERE n.%s = 1 RETURN COUNT(*) AS converter", Constants.ID_PROPERTY)
        );
        Assert.assertEquals(1L, checkExecute.next().get("converter"));
    }

    @Test
    public void idSpaceTest() {
        final String idSpace = "PERSONID";
        final String header = String.format(":ID(%s)|name:STRING", idSpace);
        final String cypher = converter.convertNodes(getResourceAbsolutePath("id_space.csv"), header, Arrays.asList("Person"), config);

        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute(
                String.format("MATCH (n) WHERE n.%s_%s = 1 RETURN count(*) AS converter", Constants.ID_PROPERTY, idSpace)
        );
        Assert.assertEquals(1L, checkExecute.next().get("converter"));
    }

    @Test
    @Ignore
    public void labelTest() {
        final String header = ":ID|:LABEL|name:STRING";
        final String cypher = converter.convertNodes(getResourceAbsolutePath("label.csv"), header, Arrays.asList("Person"), config);

        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute("MATCH (n:Person) RETURN COUNT(*) AS c");
        Assert.assertEquals(1, checkExecute.next().get("converter"));
    }

    @Test
    @Ignore
    public void arrayTest() {
        final String header = ":ID|name:STRING[]";
        final String cypher = converter.convertNodes(getResourceAbsolutePath("array.csv"), header, Arrays.asList("Person"), config);

        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        Assert.assertEquals(1, qs.getNodesCreated());
    }

}
