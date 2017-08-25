package hu.bme.mit.cch;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class FileUtilTest {

    @Test
    public void test() throws IOException {
        Assert.assertEquals(2, FileUtil.countLines("src/test/resources/id.csv"));
    }

}
