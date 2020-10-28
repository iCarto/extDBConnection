package es.udc.cartolab.gvsig.users.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class QueryBuilderTest {

	private QueryBuilder builder;

	@Before
	public void setUp() throws Exception {
		builder = new QueryBuilder();
	}

	@Test
	public final void testCorrect() {
		String actual = builder.getOrderByClause(new String[] { "foo", "bar" }, true);
		String expected = " ORDER BY foo DESC, bar DESC ";
		assertEquals(expected, actual);
	}

	@Test
	public final void testFail() {
		String expected = "";
		assertEquals(expected, builder.getOrderByClause(null, true));
		assertEquals(expected, builder.getOrderByClause(new String[0], true));
	}
}
