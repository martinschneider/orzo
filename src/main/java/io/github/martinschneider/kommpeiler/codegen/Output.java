package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.intToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.longToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;

import java.io.IOException;
import java.io.PrintStream;

public class Output {

  private PrintStream[] outputs;

  public Output(PrintStream... outputs) {
    this.outputs = outputs;
  }

  public void write(byte output) {
    for (PrintStream out : outputs) {
      out.write(output);
    }
  }

  public void write(short output) {
    write(shortToByteArray(output));
  }

  public void write(int output) {
    write(intToByteArray(output));
  }

  public void write(long output) {
    write(longToByteArray(output));
  }

  public void write(byte[] output) {
    try {
      for (PrintStream out : outputs) {
        out.write(output);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing byte-code", e);
    }
  }

  public void flush() {
    for (PrintStream out : outputs) {
      out.flush();
    }
  }

  public void close() {
    for (PrintStream out : outputs) {
      out.close();
    }
  }
}
