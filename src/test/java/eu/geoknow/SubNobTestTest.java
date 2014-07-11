package eu.geoknow;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import eu.geoknow.subnottesting.services.RsineServiceTest;

/**
 * Unit test for simple App.
 */
public class SubNobTestTest extends TestCase {
  /**
   * Create the test case
   * 
   * @param testName
   *          name of the test case
   */
  public SubNobTestTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new RsineServiceTest("Adding subscriptions to rsine") {
      protected void runTest() {
        testSubscriptions();
      }
    });
    return new TestSuite(SubNobTestTest.class);
  }

  /**
   * Rigourous Test :-)
   */
  public void testApp() {
    assertTrue(true);
  }
}
