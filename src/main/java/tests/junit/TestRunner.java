package tests.junit;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {

	public static void main(String[] args) {
		
		// First JUnit Test
		System.out.println("Running first JUnitTest");
		Result result1 = JUnitCore.runClasses(JUnitTest.class);

		for (Failure failure : result1.getFailures()) {
			System.out.println(failure.toString());
		}

		System.out.println(result1.wasSuccessful());
		
		
		// Fixture Test
		System.out.println("Running fixture text");
		Result result2 = JUnitCore.runClasses(FixtureTest.class);

		for (Failure failure : result2.getFailures()) {
			System.out.println(failure.toString());
		}

		System.out.println(result2.wasSuccessful());
	}

}
