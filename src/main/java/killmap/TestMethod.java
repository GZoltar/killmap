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

public class TestMethod implements Comparable<TestMethod> {

  private final Class<?> testClass;

  private final String name;

  private static final char SEPARATOR = '#';

  public TestMethod(Class<?> testClass, String name) {
    this.testClass = testClass;
    this.name = name;
  }

  public Class<?> getTestClass() {
    return this.testClass;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return 37 * 19 * this.toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TestMethod) {
      TestMethod other = (TestMethod) obj;
      return this.toString().equals(other.toString());
    }
    return false;
  }

  @Override
  public int compareTo(TestMethod obj) {
    if (obj instanceof TestMethod) {
      TestMethod other = (TestMethod) obj;
      return this.toString().compareTo(other.toString());
    }
    return -1;
  }

  @Override
  public String toString() {
    return this.testClass.getName() + SEPARATOR + this.name;
  }
}
