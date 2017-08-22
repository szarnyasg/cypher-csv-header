package hu.bme.mit.cch;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;

public class RegexTest {

	@Test
	public void testNamedGroups() {
		Matcher matcher = Constants.FIELD_PATTERN.matcher(TestConstants.TEST_ATTR);

		Assert.assertTrue(matcher.find());
		Assert.assertEquals(5,      matcher.groupCount());
		Assert.assertEquals("test", matcher.group("name"));
		Assert.assertEquals("TEST", matcher.group("fieldtype"));
		Assert.assertEquals("Test", matcher.group("idspace"));
		Assert.assertEquals("[]",   matcher.group("array"));
	}
}
