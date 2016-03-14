package com.tomac120.ShopifyClient.tests;

import java.util.ArrayList;
import java.util.List;

import com.tomac120.Tests.TestResult;

public class TestsMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<TestResult> testsArray = new ArrayList<TestResult>();
		new ShopifyClientTests().doTests(testsArray);
		

		TestResult.printResults(testsArray);
	}

}
