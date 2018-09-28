package tests.junit;

import junit.framework.TestCase;

public class FixtureTest extends TestCase {
	/**
	 * Fixtures is a fixed state of a set of objects used as a baseline for running tests. 
	 * The purpose of a test fixture is to ensure that there is a well-known and fixed environment 
	 * in which tests are run so that results are repeatable.
	 * */
	
	   protected int value1, value2;
	   
	   // assigning the values
	   protected void setUp(){
	      value1 = 2;
	      value2 = 3;
	   }

	   // test method to add two values
	   public void testAdd(){
	      double result = value1 + value2;
	      assertTrue(result == 6);
	   }
	   
	   // protected void tearDown() {}
}
