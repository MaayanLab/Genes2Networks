package edu.mssm.pharm.maayanlab.Genes2Networks;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RegressionTest extends TestCase {
	
	private Genes2Networks app;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		app = new Genes2Networks();
	}
	
	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( RegressionTest.class );
	}
	
	private void assertEquivalentOutput() {
		
	}
	
	public void test() {
		
	}
	

}
