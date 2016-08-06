package processing.mode.p5js.server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;


/**
 * A wrapped PrintStream that uses CRLF to indicate newlines.
 */
public class CarlOrff {
  PrintStream ps;


  public CarlOrff(final OutputStream out) {
    ps = new PrintStream(out);
  }


  public void print(String s) {
    ps.print(s);
  }


  public void println(String s) {
    print(s);
    println();
  }


  public void println() {
    ps.print("\r\n");
  }


  public void flush() {
    ps.flush();
  }


  public void close() {
    ps.flush();
    ps.close();
  }


  public void write(byte[] b) {
    ps.write(b, 0, b.length);
  }


  public void write(byte[] b, int off, int length) {
    ps.write(b, off, length);
  }


  public void writeGZ(String what) throws IOException {
    byte[] b = what.getBytes(StandardCharsets.UTF_8);
    writeGZ(b);
  }


  public void writeGZ(byte[] b) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzos = new GZIPOutputStream(baos);
    gzos.write(b);
    gzos.flush();
    gzos.close();
    write(baos.toByteArray());
    println();
  }


  public void status(int code) {
    if (code == 200) {
      ps.println("HTTP/1.1 200 OK");
    } else {
      ps.println("HTTP/1.1 500 status() not implemented");
    }
  }
}