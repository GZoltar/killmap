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

import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

/**
 * An OutputStream that, instead of writing its output to a file or anything, feeds the output into
 * a hash-function. This lets us tell whether two test-runs printed the same stuff to stdout/stderr,
 * without having to store their entire output (which, with mutants, can be huge).
 */
public class DeadEndDigestOutputStream extends DigestOutputStream {

  private static class DeadEndStream extends OutputStream {
    public void write(int b) {}

    public DeadEndStream() {}
  }

  public DeadEndDigestOutputStream() {
    super(new DeadEndStream(), null);
    try {
      setMessageDigest(MessageDigest.getInstance("SHA-1"));
    } catch (java.security.NoSuchAlgorithmException e) {
      e.printStackTrace();
      System.out.println(
          "According to http://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html , java.security.MessageDigest *must* provide SHA-1. What happened?");
      System.exit(1);
    }
  }

  public String getDigestString() {
    // Gives the hash as a typical hex string.
    byte[] digestBytes = getMessageDigest().digest();
    StringBuffer sb = new StringBuffer();
    for (byte b : digestBytes) {
      sb.append(String.format("%02x", b & 0xff));
    }
    return sb.toString();
  }
}
