package tests.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JUnitTest {

	@Test
	public void testAdd() {
		String str = "Junit is working ";
      	assertEquals("Junit is working fine",str);
	}
}
