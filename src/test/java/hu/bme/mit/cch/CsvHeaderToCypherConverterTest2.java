package hu.bme.mit.cch;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CsvHeaderToCypherConverterTest2 {

  final CsvHeaderToCypherConverter h2c = new CsvHeaderToCypherConverter();
  
  final CsvLoaderConfig config = CsvLoaderConfig.builder().build(); 
  
  @Test
  public void test2() {
    System.out.println(h2c.convertNodes("/routes.csv", "\":ID\",name", Arrays.asList("Route"), config));
  }

  @Test
  public void test3() {
    System.out.println(h2c.convertNodes("/routes.csv", "\":ID\",\"active:BOOLEAN\"", Arrays.asList("Route"), config));
  }

  @Test
  public void test4() {
    System.out.println(h2c.convertNodes("/switch.csv", "\":ID\",\"currentPosition\"", Arrays.asList("Route"), config));
  }
  
  @Test
  public void testRelationship1() {
    System.out.println(h2c.convertRelationships("/connectsTo.csv", "\":START_ID(RailwayElement)\",\":END_ID\"", "connectsTo", config));
  }
  
}
