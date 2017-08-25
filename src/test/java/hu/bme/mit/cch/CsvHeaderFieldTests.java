package hu.bme.mit.cch;

import org.junit.Assert;
import org.junit.Test;

public class CsvHeaderFieldTests {

    @Test
    public void testCsvField1() {
        System.out.println(TestConstants.TEST_FIELD_1);
        CsvHeaderField field = CsvHeaderField.parse(0, TestConstants.TEST_FIELD_1, '"');
        System.out.println(field);

        Assert.assertEquals(TestConstants.TEST_NAME,    field.getName());
        Assert.assertEquals(TestConstants.TEST_TYPE,    field.getType());
        Assert.assertEquals(TestConstants.TEST_IDSPACE, field.getIdSpace());
        Assert.assertTrue(field.isArray());
    }

    @Test
    public void testCsvField2() {
        System.out.println(TestConstants.TEST_FIELD_2);
        CsvHeaderField field = CsvHeaderField.parse(0, TestConstants.TEST_FIELD_2, '"');
        System.out.println(field);

        Assert.assertEquals(TestConstants.TEST_NAME,    field.getName());
        Assert.assertEquals(TestConstants.TEST_TYPE,    field.getType());
        Assert.assertEquals(TestConstants.TEST_IDSPACE, field.getIdSpace());
        Assert.assertFalse(field.isArray());
    }

}
