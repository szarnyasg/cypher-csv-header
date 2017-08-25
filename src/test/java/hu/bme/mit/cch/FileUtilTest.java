package hu.bme.mit.cch;

import org.junit.Test;

import java.io.IOException;

public class FileUtilTest {

    @Test
    public void test() throws IOException {
        System.out.println(FileUtil.countLines("src/test/resources/id.csv"));
    }

}
