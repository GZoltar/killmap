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

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import org.junit.runner.Description;
import org.junit.runner.Request;

/**
 * Class for finding JUnit tests.
 * 
 * Can read a list of test-class names from a file, and return those classes; and, given a list of
 * test-classes, can return all test-methods therein.
 */
public class TestFinder {

  public static String getTestFullName(TestMethod test, String separator) {
    // Returns a string that uniquely identifies the given test,
    // and can be used to load that test as a Method using parseTestFullName.
    return test.getTestClass().getName() + separator + test.getName();
  }

  public static TestMethod parseTestFullName(String testFullName, String separator)
      throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException {
    // Inverse of getTestFullName
    String[] class_method = testFullName.split(separator, 2);
    if (class_method.length != 2) {
      throw new IllegalArgumentException(testFullName);
    }
    return new TestMethod(Class.forName(class_method[0]), class_method[1]);
  }

  public static Collection<TestMethod> parseTriggeringTestsFile(String filename)
      throws IOException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException {
    // Parses the output of `defects4j export -p tests.trigger` into a collection of Methods.
    Collection<String> testNames =
        Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
    Collection<TestMethod> result = new Vector<TestMethod>();
    for (String name : testNames) {
      if (name.startsWith("--- ")) {
        String fullName = name.replace("--- ", "");
        result.add(parseTestFullName(fullName, "::"));
      }
    }

    return result;
  }

  public static Collection<Class<?>> getTestClasses(String testClassNameFilename)
      throws IOException, ClassNotFoundException, NoSuchMethodException {
    // Loads a bunch of classes named in a file.
    // Specifically, parses the output of `defects4j export -p tests.relevant`.
    Collection<String> testClassNames =
        Files.readAllLines(Paths.get(testClassNameFilename), Charset.defaultCharset());
    Collection<Class<?>> classes = new Vector<Class<?>>();
    for (String name : testClassNames) {
      classes.add(Class.forName(name));
    }

    return classes;
  }

  private static boolean looksLikeTest(Method m) {
    // Determines whether a method looks enough like a test that we should run it.
    // Tries to compatible with both JUnit 3 and JUnit 4.
    return (m.isAnnotationPresent(org.junit.Test.class)
        || (m.getParameterTypes().length == 0 && m.getReturnType().equals(Void.TYPE)
            && Modifier.isPublic(m.getModifiers()) && m.getName().startsWith("test")));
  }

  public static Collection<TestMethod> getTestMethods(Collection<Class<?>> classes) {
    // Given a bunch of classes, find all of the JUnit test-methods defined by them.
    Vector<TestMethod> tests = new Vector<TestMethod>();
    for (Class<?> cls : classes) {
      for (Description test : Request.aClass(cls).getRunner().getDescription().getChildren()) {
        // a parameterized atomic test case does not have a method name
        if (test.getMethodName() == null) {
          for (Method m : cls.getMethods()) {
            // JUnit 3: an atomic test case is "public", does not return anything ("void"), has 0
            // parameters and starts with the word "test"
            // JUnit 4: an atomic test case is annotated with @Test
            if (looksLikeTest(m)) {
              tests.add(new TestMethod(cls, m.getName() + test.getDisplayName()));
            }
          }
        } else {
          // non-parameterized atomic test case
          tests.add(new TestMethod(test.getTestClass(), test.getMethodName()));
        }
      }
    }
    // It'd be nice to ensure the result has a deterministic order, so that if we
    // run the program multiple times, the outputs look similar.
    Collections.sort(tests);
    return tests;
  }

  public static Collection<TestMethod> getTestsFromTestClassNameFile(String path)
      throws IOException, ClassNotFoundException, NoSuchMethodException {
    // Loads all the test-methods defined by any class named in the given file.
    // Specifically, the file should have the result of `defects4j export -p tests.relevant`.
    return getTestMethods(getTestClasses(path));
  }

  public static void main(String... args) {
    if (args.length != 1) {
      System.err
          .println("usage: java -cp killmap.jar killmap.TestFinder CLASSNAME_FILE");
      System.exit(1);
    }
    Collection<TestMethod> testMethods = null;
    try {
      testMethods = getTestsFromTestClassNameFile(args[0]);
    } catch (Exception e) {
      System.err.println("error: " + e);
      System.exit(1);
    }
    for (TestMethod testMethod : testMethods) {
      System.out.println(testMethod);
    }
  }
}
