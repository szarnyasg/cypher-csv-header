package hu.bme.mit.cch;

import java.util.Arrays;

import org.junit.Test;

public class CsvHeaderToCypherConverterTest {

  final CsvHeaderToCypherConverter h2c = new CsvHeaderToCypherConverter();

  final CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').build(); 

  @Test
  public void test1() {
    System.out.println(h2c.convertNodes("/my.csv", ":ID(Forum)|id:LONG|title:STRING|creationDate:STRING", Arrays.asList("Forum"), config));
  }

  @Test
  public void test2() {
    System.out.println(h2c.convertNodes("/Sensor.csv", ":ID|languages:STRING[]|my-numbers:INT[]|bools:BOOLEAN[]", Arrays.asList("Sensor"), config));
  }

  @Test
  public void test3() {
    System.out.println(h2c.convertRelationships("/connectsTo.csv", ":START_ID(Forum)|:END_ID(Person)|languages:STRING[]|my-numbers:INT[]|bools:BOOLEAN[]", "connectsTo", config));
  }

  
}
