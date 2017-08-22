package hu.bme.mit.cch;

import org.junit.Test;

public class CsvFieldTest {

	@Test
	public void testCsvField() {
		CsvField field = CsvField.parse(0, TestConstants.TEST_ATTR, '"');
		System.out.println(field);
	}

}
