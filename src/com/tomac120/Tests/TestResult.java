package com.tomac120.Tests;

import java.util.List;

public class TestResult {
	public final String name;
	public boolean passed = false;
	public boolean wasNotRun = false;
	public String failureReason = "";
	
	public TestResult(String name){
		this.name = name;
	}

	public TestResult success(){
		this.passed = true;
		return this;
	}
	

	public TestResult wasNotRun(){
		this.wasNotRun = true;
		return this;
	}
	
	public TestResult failure(String reason){
		failureReason = reason;
		return this;
	}
	
	public String getLine(int i){
		String r = "(" + Integer.toString(i) + ") " + this.name+": ";
		if (this.passed){
			r += "Passed";
		} else if (this.wasNotRun){
			r += "Skipped";
		} else if (this.failureReason != null){
			r += "Failed: "+this.failureReason;
		} else {
			r += "Failed";
		}
		return r;
	}
	
	public static void printResults(List<TestResult> testsArray){
		int i = 0;
		for (TestResult r : testsArray){
			i++;
			System.out.println(r.getLine(i));
		}
	}
}
