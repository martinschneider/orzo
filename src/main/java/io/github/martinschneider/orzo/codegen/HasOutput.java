package io.github.martinschneider.orzo.codegen;

// marker interface for classes bytes can be written to
// implemented by both io.github.martinschneider.kommpeiler.codegen.Output and
// io.github.martinschneider.kommpeiler.codegen.DynamicByteArray so they can be
// used interchangeably
public interface HasOutput {
  void write(byte b);

  void write(short b);

  void write(int b);

  void write(byte[] b);

  byte[] getBytes();
}
