package hu.bme.mit.cch;

import org.junit.Test;

public class CsvRelationshipTests {

    final CsvHeaderToCypherConverter h2c = new CsvHeaderToCypherConverter();
    final CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').build();

    @Test
    public void testRelationship1() {
        System.out.println(h2c.convertRelationships("/test.csv", ":START_ID|:END_ID|attr:STRING", "connectsTo", config));
    }

    @Test
    public void testRelationship2() {
        System.out.println(h2c.convertRelationships("/test.csv", ":START_ID(Space1)|:END_ID(Space2)|attr:STRING", "connectsTo", config));
    }

    @Test
    public void testRelationship3() {
        System.out.println(h2c.convertRelationships("/test.csv", ":START_ID(Space1)|:END_ID(Space2)|attr:STRING[]", "connectsTo", config));
    }

}
