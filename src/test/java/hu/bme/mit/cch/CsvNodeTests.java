package hu.bme.mit.cch;

import java.util.Arrays;

import org.junit.Test;

public class CsvNodeTests {

    final CsvHeaderToCypherConverter h2c = new CsvHeaderToCypherConverter();

    final CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').build();

    @Test
    public void testNode1() {
        System.out.println(h2c.convertNodes("/test.csv", ":ID(Forum)|id:LONG|title:STRING|creationDate:STRING", Arrays.asList("Forum"), config));
    }

    @Test
    public void testNode2() {
        System.out.println(h2c.convertNodes("/test.csv", ":ID|languages:STRING[]|my-numbers:INT[]|bools:BOOLEAN[]", Arrays.asList("Sensor"), config));
    }

}
