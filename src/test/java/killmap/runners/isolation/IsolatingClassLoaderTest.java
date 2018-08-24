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
package killmap.runners.isolation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import junit.framework.TestCase;

public class IsolatingClassLoaderTest extends TestCase {

  public static Integer foo = 0;

  @SuppressWarnings({"rawtypes", "resource"})
  @Test
  public void testDoesNotShareClassesWithParent() throws Exception {
    ClassLoader isolatingLoader1 = new IsolatingClassLoader();
    ClassLoader isolatingLoader2 = new IsolatingClassLoader();

    String className = "killmap.runners.isolation.IsolatingClassLoaderTest";
    Class isolatedClass1 = isolatingLoader1.loadClass(className);
    Class isolatedClass2 = isolatingLoader2.loadClass(className);

    assertThat(isolatedClass1, not(equalTo(isolatedClass2)));

    // Ensure that the isolated classes' static states are independent
    isolatedClass1.getField("foo").set(isolatedClass1, (Integer) 1);
    isolatedClass2.getField("foo").set(isolatedClass2, (Integer) 2);
    assertEquals((Integer) 1, (Integer) isolatedClass1.getField("foo").get(isolatedClass1));
    assertEquals((Integer) 2, (Integer) isolatedClass2.getField("foo").get(isolatedClass2));
  }
}
