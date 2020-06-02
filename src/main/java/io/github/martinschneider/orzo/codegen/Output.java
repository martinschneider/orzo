package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.ByteUtils.bytesToHex;
import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;
import static io.github.martinschneider.orzo.codegen.ByteUtils.longToByteArray;
import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

import java.io.IOException;
import java.io.PrintStream;

// output of the compiler
public class Output implements HasOutput {
  private DynamicByteArray array = new DynamicByteArray();
  private PrintStream[] outputs;

  public Output(PrintStream... outputs) {
    this.outputs = outputs;
  }

  public void close() {
    for (PrintStream out : outputs) {
      out.close();
    }
  }

  public void flush() {
    for (PrintStream out : outputs) {
      out.flush();
    }
  }

  @Override
  public void write(byte output) {
    for (PrintStream out : outputs) {
      out.write(output);
    }
    array.write(output);
  }

  public void write(byte[] output) {
    try {
      for (PrintStream out : outputs) {
        out.write(output);
      }
      array.write(output);
    } catch (IOException e) {
      throw new RuntimeException("Error writing byte-code", e);
    }
  }

  public void write(int output) {
    write(intToByteArray(output));
  }

  public void write(long output) {
    write(longToByteArray(output));
  }

  public void write(short output) {
    write(shortToByteArray(output));
  }

  @Override
  public String toString() {
    return bytesToHex(array.getBytes());
  }

  @Override
  public byte[] getBytes() {
    return array.getBytes();
  }
}
