package hu.bme.mit.cch;

import apoc.create.Create;
import apoc.graph.Graphs;
import hu.bme.mit.cch.apoc.ApocHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryStatistics;
import org.neo4j.graphdb.Result;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class MyNeo4jTest {

    CsvLoaderConfig config;
    CsvHeaderToCypherConverter converter;
    GraphDatabaseService gds;

    @Before
    public void init() throws KernelException {
        config = CsvLoaderConfig.builder().fieldTerminator('|').stringIds(false).skipHeaders(false).build();
        converter = new CsvHeaderToCypherConverter();
        gds = new TestGraphDatabaseFactory().newImpermanentDatabase();

        ApocHelper.registerProcedure(gds, Create.class, Graphs.class);
    }

    @After
    public void shutdown() {
        gds.shutdown();
    }

    static String getResourceAbsolutePath(String testResource) {
        final URL resource = MyNeo4jTest.class.getClassLoader().getResource(testResource);
        return resource.getPath();
    }

    protected QueryStatistics loadNodes(String testResource, String header, String label) {
        return loadNodes(testResource, header, Arrays.asList(label));
    }

    /**
     * Note that QueryStatistics do not work for APOC.
     *
     * @param testResource
     * @param header
     * @param labels
     * @return
     */
    protected QueryStatistics loadNodes(String testResource, String header, List<String> labels) {
        final String cypher = converter.convertNodes(getResourceAbsolutePath(testResource), header, labels, config);
        final Result result = gds.execute(cypher);
        final QueryStatistics qs = result.getQueryStatistics();
        return qs;
    }

    protected QueryStatistics loadRelationships(String testResource, String header, String type) {
        final String cypher = converter.convertRelationships(getResourceAbsolutePath(testResource), header, type, config);
        final Result result = gds.execute(cypher);
        final QueryStatistics qs = result.getQueryStatistics();
        return qs;
    }

    @Test
    public void basicTest() {
        final QueryStatistics qs = loadNodes("basic.csv", "name:STRING", "Person");
        Assert.assertEquals(2, qs.getNodesCreated());
    }

    @Test
    public void idTest() {
        final QueryStatistics qs = loadNodes("id.csv", ":ID|name:STRING", "Person");
        Assert.assertEquals(2, qs.getNodesCreated());

        final Result checkExecute = gds.execute(
                String.format("MATCH (n) WHERE n.%s = 1 RETURN count(n) AS c", Constants.ID_ATTR)
        );
        Assert.assertEquals(1L, checkExecute.next().get("c"));
    }

    @Test
    public void idSpaceTest() {
        final String idSpace = "PERSONID";
        final String header = String.format(":ID(%s)|name:STRING", idSpace);
        final QueryStatistics qs = loadNodes("id_space.csv", header, "Person");
        Assert.assertEquals(1, qs.getNodesCreated());

        final Result checkExecute = gds.execute(
                String.format("MATCH (n) WHERE n.%s = 1 RETURN count(n) AS c", Constants.ID_ATTR, idSpace)
        );
        Assert.assertEquals(1L, checkExecute.next().get("c"));
    }

    @Test
    public void labelTest() {
        loadNodes("label.csv", ":ID|:LABEL|name:STRING", "Person");

        final Result checkExecute = gds.execute("MATCH (n) RETURN count(n) AS c");
        Assert.assertEquals(1L, checkExecute.next().get("c"));
    }

    @Test
    public void typeTest() {
        loadNodes("id.csv", ":ID|name:STRING", "Person");
        loadRelationships("type.csv", ":START_ID|:END_ID|:TYPE|since:INT", "DUMMY");

        final Result checkExecute = gds.execute("MATCH ()-[r:KNOWS]->() RETURN count(r) AS c");
        Assert.assertEquals(1L, checkExecute.next().get("c"));
    }

    @Test
    public void arrayTest() {
        final QueryStatistics qs = loadNodes("array.csv", ":ID|name:STRING[]", "Person");
        Assert.assertEquals(1, qs.getNodesCreated());
    }

}
