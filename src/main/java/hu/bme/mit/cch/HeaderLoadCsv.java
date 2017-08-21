package hu.bme.mit.cch;

import apoc.load.LoadCsv;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HeaderLoadCsv {

    public void convertNodes(final String url, final String header, final Collection<String> labels,
                               final CsvLoaderConfig config) {
        final LoadCsv loader = new LoadCsv();
        final Map<String, Object> conf = new HashMap<>();
        loader.csv(url, conf);
    }

      /*
        apoc.load.csv('url',{config})
        YIELD lineNo, list, map

        load CSV fom URL as stream of values,
        config contains any of:
          {
            skip:   1,
            limit:  5,
            header: false,
            sep:    'TAB',
            ignore: ['tmp'],
            arraySep: ';',
            mapping: {
              years: {
                type:'int',
                arraySep:'-',
                array:false,
                name:'age',
                ignore:false
              }
            }
          }
      */

}
