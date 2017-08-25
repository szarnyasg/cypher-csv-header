package hu.bme.mit.cch;

import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LdbcCsvs {

  CsvHeaderToCypherConverter converter = new CsvHeaderToCypherConverter();
  CsvLoaderConfig config = CsvLoaderConfig.builder().fieldTerminator('|').build();

  String DIR = "src/test/resources/ldbc/";
  File nodeFileList = new File(DIR + "nodes.txt");
  File relFileList = new File(DIR + "relationships.txt");

  @Test
  @Ignore
  // TODO provide the exact filenames and the files (for determining transaction batches)
  public void load() throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(nodeFileList))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] items = line.split(" ");

        String filename = "%s" + items[0] + "%s";
        List<String> labels = Arrays.asList(items[1].split(":"));
        String header = items[2];

        System.out.println(converter.convertNodes(filename, header, labels, config));
      }
    }

    try (BufferedReader br = new BufferedReader(new FileReader(relFileList))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] items = line.split(" ");

        String filename = "%s" + items[0] + "%s";
        String type = items[1];
        String header = items[2];

        System.out.println(converter.convertRelationships(filename, header, type, config));
      }
    }

  }

}
