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
package killmap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import junit.framework.TestCase;

@SuppressWarnings("serial")
public class TestFinderTest extends TestCase {

  public static class DummyTestSuiteWithAnnotations extends TestCase {
    @Test
    public void testX() {}

    @Test
    public static void testY() {}

    public static void testHelper(String s) {}

    public static final Collection<TestMethod> testSet = new HashSet<TestMethod>() {
      {
        add(new TestMethod(DummyTestSuiteWithAnnotations.class, "testX"));
        add(new TestMethod(DummyTestSuiteWithAnnotations.class, "testY"));
      }
    };
  }

  public static class DummyTestSuiteWithoutAnnotations extends TestCase {
    public void testX() {}

    public static void testY() {}

    public static void testHelper(String s) {}

    public static final Collection<TestMethod> testSet = new HashSet<TestMethod>() {
      {
        add(new TestMethod(DummyTestSuiteWithoutAnnotations.class, "testX"));
        add(new TestMethod(DummyTestSuiteWithoutAnnotations.class, "testY"));
      }
    };
  }

  @Test
  public void testFindsTestsGivenClasses() {
    Collection<TestMethod> found = TestFinder.getTestMethods(new HashSet<Class<?>>() {
      {
        add(DummyTestSuiteWithAnnotations.class);
        add(DummyTestSuiteWithoutAnnotations.class);
      }
    });
    assertEquals(new HashSet<TestMethod>(found), new HashSet<TestMethod>() {
      {
        addAll(DummyTestSuiteWithAnnotations.testSet);
        addAll(DummyTestSuiteWithoutAnnotations.testSet);
      }
    });
  }

  @Test
  public void testParseTestFullName()
      throws IllegalArgumentException, ClassNotFoundException, NoSuchMethodException {
    assertEquals(new TestMethod(DummyTestSuiteWithAnnotations.class, "testX"),
        TestFinder.parseTestFullName(
            "killmap.TestFinderTest$DummyTestSuiteWithAnnotations::testX", "::"));
    assertEquals(new TestMethod(DummyTestSuiteWithAnnotations.class, "testX"),
        TestFinder.parseTestFullName(
            "killmap.TestFinderTest$DummyTestSuiteWithAnnotations#testX", "#"));
  }

  @Test
  public void testGetTestsFromTestClassNameFile() throws Exception {
    // create a temporary file
    File tmpFile = File.createTempFile("testGetTestsFromTestClassNameFile", ".txt");
    // add test class name to the temporary file
    BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile.getAbsolutePath()));
    bw.write("killmap.TestFinderTest$DummyTestSuiteWithAnnotations");
    bw.close();

    List<TestMethod> found = new ArrayList<TestMethod>(
        TestFinder.getTestsFromTestClassNameFile(tmpFile.getAbsolutePath()));
    assertEquals("killmap.TestFinderTest$DummyTestSuiteWithAnnotations#testX",
        found.get(0).toString());
    assertEquals("killmap.TestFinderTest$DummyTestSuiteWithAnnotations#testY",
        found.get(1).toString());

    // remove temporary file
    tmpFile.deleteOnExit();
  }
}
