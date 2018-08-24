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

public class DeadEndDigestOutputStreamTest extends TestCase {

  @Test
  public void testDigestIsDeterministic() throws java.io.IOException {
    DeadEndDigestOutputStream stream1 = new DeadEndDigestOutputStream();
    DeadEndDigestOutputStream stream2 = new DeadEndDigestOutputStream();
    assertEquals(stream1.getDigestString(), stream2.getDigestString());
    stream1.write(123);
    stream2.write(123);
    assertEquals(stream1.getDigestString(), stream2.getDigestString());

    stream1.close();
    stream2.close();
  }

  @Test
  public void testDigestChangesOnWrite() throws java.io.IOException {
    DeadEndDigestOutputStream stream = new DeadEndDigestOutputStream();
    String originalDigest = stream.getDigestString();
    stream.write(123);
    assertThat(originalDigest, not(equalTo(stream.getDigestString())));

    stream.close();
  }
}
