package hu.bme.mit.cch;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;

public class RegexTests {

	@Test
	public void testNamedGroups() {
		Matcher matcher = Constants.FIELD_PATTERN.matcher(TestConstants.TEST_FIELD_1);

		Assert.assertTrue(matcher.find());
		Assert.assertEquals(5,      matcher.groupCount());
		Assert.assertEquals(TestConstants.TEST_NAME, matcher.group("name"));
		Assert.assertEquals(TestConstants.TEST_TYPE, matcher.group("fieldtype"));
		Assert.assertEquals(TestConstants.TEST_IDPSACE, matcher.group("idspace"));
		Assert.assertEquals(TestConstants.TEST_ARRAY,   matcher.group("array"));
	}
}
