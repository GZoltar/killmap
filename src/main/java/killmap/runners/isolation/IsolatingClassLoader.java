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

import java.net.URLClassLoader;

/**
 * A classloader that should have the same classpath as the normal classloader, but shares
 * absolutely nothing with it. Ensures that no test will change static state that will affect later
 * tests.
 */
public class IsolatingClassLoader extends URLClassLoader {
  public IsolatingClassLoader() {
    super(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs(), null);
  }
}
