/**
 * Copyright (C) 2018 Spencer Pearson, José Campos and killmap contributors.
 * 
 * This file is part of killmap.
 * 
 * killmap is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * killmap is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with killmap.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package killmap.runners.communication;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import killmap.TestMethod;
import junit.framework.TestCase;

public class WorkOrderTest extends TestCase {

  public static class DummyTestCase extends TestCase {
    @Test
    public void testThatPasses() {}

    @Test
    public void testThatFails() {
      assertEquals(0, 1);
    }
  }

  private static TestMethod passingTest;
  private static TestMethod failingTest;
  static {
    passingTest = new TestMethod(DummyTestCase.class, "testThatPasses");
    failingTest = new TestMethod(DummyTestCase.class, "testThatFails");
  }

  @Test
  public void testToString() {
    assertEquals("killmap.runners.communication.WorkOrderTest$DummyTestCase#testThatPasses,3,100",
        (new WorkOrder(passingTest, 3, (long) 100)).toString());
  }

  private static void assertEqualsAndHashEquals(Object x, Object y) {
    assertEquals(x, y);
    assertEquals(x.hashCode(), y.hashCode());
  }

  @Test
  public void testEquality() {
    assertEqualsAndHashEquals(new WorkOrder(passingTest, 0, (long) 1),
        new WorkOrder(passingTest, 0, (long) 1));
    assertThat(new WorkOrder(passingTest, 0, (long) 1),
        not(equalTo(new WorkOrder(failingTest, 0, (long) 1))));
    assertThat(new WorkOrder(passingTest, 0, (long) 1),
        not(equalTo(new WorkOrder(passingTest, 0, (long) 0))));
    assertThat(new WorkOrder(passingTest, 0, (long) 1),
        not(equalTo(new WorkOrder(passingTest, 1, (long) 1))));
  }

  @Test
  public void testFromStringIsInverseOfToString()
      throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException {
    WorkOrder workOrder = new WorkOrder(passingTest, 3, (long) 100);
    assertEquals(workOrder, WorkOrder.fromString(workOrder.toString()));
  }

}
