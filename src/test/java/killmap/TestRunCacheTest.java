package killmap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import killmap.runners.communication.WorkOrder;
import junit.framework.TestCase;

public class TestRunCacheTest extends TestCase {

  public static class DummyTestSuite extends TestCase {
    @Test
    public void testThatPasses() {}
  }

  public static WorkOrder dummyWorkOrder(Integer mutantId, Long timeout) {
    TestMethod method = new TestMethod(DummyTestSuite.class, "testThatPasses");
    return new WorkOrder(method, mutantId, timeout);
  }

  public static Result passingJUnitResult() {
    return (new JUnitCore())
        .run(Request.method(TestRunCacheTest.DummyTestSuite.class, "testThatPasses"));
  }

  @Test
  public void testRetrievesCachedValues() throws IOException {
    BufferedWriter w = new BufferedWriter(new FileWriter("/tmp/killmap_cache_test"));
    w.write(
        "killmap.TestRunCacheTest$DummyTestSuite#testThatPasses,0,123,PASS,12,1234abcdef,7 8 9,\nkillmap.TestRunCacheTest$DummyTestSuite#testThatPasses,0,321,PASS,21,fedcba4321,,\n\n");
    w.close();
    TestRunCache cache = new TestRunCache("/tmp/killmap_cache_test");
    assertEquals("1234abcdef", cache.tryGet(dummyWorkOrder(0, 123L)).digest);
    assertEquals("fedcba4321", cache.tryGet(dummyWorkOrder(0, 321L)).digest);
    assertNull(cache.tryGet(dummyWorkOrder(0, 123L)));
  }

  @Test
  public void testRejectsLastLineIfNoNewline() throws IOException {
    BufferedWriter w = new BufferedWriter(new FileWriter("/tmp/killmap_cache_test"));
    w.write(
        "killmap.TestRunCacheTest$DummyTestSuite#testThatPasses,0,123,PASS,12,1234abcdef,7 8 9,\nkillmap.TestRunCacheTest$DummyTestSuite#testThatPasses,0,321,PASS,21,fedcba4321,,");
    w.close();
    TestRunCache cache = new TestRunCache("/tmp/killmap_cache_test");
    assertEquals("1234abcdef", cache.tryGet(dummyWorkOrder(0, 123L)).digest);
    assertNull(cache.tryGet(dummyWorkOrder(0, 321L)));
  }
}
