package io.github.martinschneider.orzo.tests;

import java.io.*;

public class SuperMethodCall {
  static class Base {
    String hi() {
      return "base";
    }
  }

  static class Derived extends Base {
    String hi() {
      return "derived+" + super.hi();
    }
  }

  // a JDK superclass via an on-demand import, and super calls of its methods
  static class CountingReader extends PushbackReader {
    int count;

    CountingReader(Reader reader) {
      super(reader);
    }

    @Override
    public int read() throws IOException {
      count++;
      return super.read();
    }
  }

  public static void main(String[] args) throws IOException {
    System.out.println(new Derived().hi());
    CountingReader reader = new CountingReader(new StringReader("ab"));
    reader.read();
    reader.read();
    System.out.println(reader.count + " " + reader.read());
  }
}
