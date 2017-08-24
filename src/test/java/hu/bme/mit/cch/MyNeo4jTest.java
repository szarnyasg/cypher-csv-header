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
import java.util.List;

@Ignore
public class MyNeo4jTest {

    CsvLoaderConfig config;
    CsvHeaderToCypherConverter converter;
    GraphDatabaseService gds;

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

    static String getResourceAbsolutePath(String testResource) {
        final URL resource = MyNeo4jTest.class.getClassLoader().getResource(testResource);
        return resource.getPath();
    }

    protected QueryStatistics test(String testResource, String header, String label) {
        return test(testResource, header, Arrays.asList(label));
    }

    protected QueryStatistics test(String testResource, String header, List<String> labels) {
        final String cypher = converter.convertNodes(getResourceAbsolutePath(testResource), header, labels, config);
        final Result execute = gds.execute(cypher);
        final QueryStatistics qs = execute.getQueryStatistics();
        return qs;
    }

    @Test
    public void basicTest() {
        final QueryStatistics qs = test("basic.csv", "name:STRING", "Person");
        Assert.assertEquals(1, qs.getNodesCreated());
    }

    @Test
    public void idTest() {
        final QueryStatistics qs = test("id.csv", ":ID|name:STRING", "Person");
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute(
                String.format("MATCH (n) WHERE n.%s = 1 RETURN COUNT(*) AS converter", Constants.ID_ATTR)
        );
        Assert.assertEquals(1L, checkExecute.next().get("converter"));
    }

    @Test
    public void idSpaceTest() {
        final String idSpace = "PERSONID";
        final String header = String.format(":ID(%s)|name:STRING", idSpace);
        final QueryStatistics qs = test("id_space.csv", header, "Person");
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute(
                String.format("MATCH (n) WHERE n.%s_%s = 1 RETURN count(*) AS converter", Constants.ID_ATTR, idSpace)
        );
        Assert.assertEquals(1L, checkExecute.next().get("converter"));
    }

    @Test
    @Ignore
    public void labelTest() {
        final QueryStatistics qs = test("label.csv", ":ID|:LABEL|name:STRING", "Person");
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute("MATCH (n:Person) RETURN COUNT(*) AS c");
        Assert.assertEquals(1, checkExecute.next().get("converter"));
    }

    @Test
    @Ignore
    public void arrayTest() {
        final QueryStatistics qs = test("array.csv", ":ID|name:STRING[]", "Person");
        Assert.assertEquals(1, qs.getNodesCreated());
    }

}
