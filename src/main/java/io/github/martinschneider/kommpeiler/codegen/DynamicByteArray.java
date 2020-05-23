package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.bytesToHex;
import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;

// dynamically resizing byte array
public class DynamicByteArray implements HasOutput {
  private int size = 2;
  private byte[] array = new byte[size];
  private int pointer = 0;

  @Override
  public void write(byte b) {
    if (pointer >= size) {
      size *= 2;
      byte[] help = new byte[size];
      for (int i = 0; i < size / 2; i++) {
        help[i] = array[i];
      }
      array = help;
    }
    array[pointer] = b;
    pointer++;
  }

  public void write(byte[] b) {
    while (pointer + b.length > size) {
      size *= 2;
      byte[] help = new byte[size];
      for (int i = 0; i < size / 2; i++) {
        help[i] = array[i];
      }
      array = help;
    }
    for (int i = 0; i < b.length; i++) {
      array[pointer] = b[i];
      pointer++;
    }
  }

  public int size() {
    return pointer;
  }

  public byte[] flush() {
    byte[] retValue = getBytes();
    size = 2;
    pointer = 0;
    array = new byte[size];
    return retValue;
  }

  @Override
  public byte[] getBytes() {
    byte[] retValue = new byte[pointer];
    for (int i = 0; i < pointer; i++) {
      retValue[i] = array[i];
    }
    return retValue;
  }

  public void write(short value) {
    write(shortToByteArray(value));
  }

  @Override
  public String toString() {
    return bytesToHex(array);
  }
}
