package io.github.martinschneider.kommpeiler.codegen;

public interface HasOutput {
  void write(byte b);

  byte[] getBytes();
}
