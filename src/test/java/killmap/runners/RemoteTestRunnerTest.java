package killmap.runners;

import org.junit.Test;
import killmap.TestMethod;
import killmap.runners.communication.Outcome;
import killmap.runners.communication.WorkOrder;
import junit.framework.TestCase;

public class RemoteTestRunnerTest extends TestCase {

  public static class DummyTest extends TestCase {
    public static boolean isFirstRun = true;

    @Test
    public void testThatPassesOnFirstRunOnly() {
      assertTrue(isFirstRun);
      isFirstRun = false;
    }
  }

  private static WorkOrder getWorkOrderForName(String testName) throws NoSuchMethodException {
    TestMethod test = new TestMethod(DummyTest.class, testName);
    return new WorkOrder(test, 0, (long) 100);
  }

  @Test
  public void testIsolation() throws Exception {
    RemoteTestRunner runner = new RemoteTestRunner();
    Outcome outcome1 = runner.runTest(getWorkOrderForName("testThatPassesOnFirstRunOnly"));
    Outcome outcome2 = runner.runTest(getWorkOrderForName("testThatPassesOnFirstRunOnly"));
    runner.close();
    assertEquals(Outcome.Type.PASS, outcome1.type);
    assertEquals(Outcome.Type.PASS, outcome2.type);
  }
}
