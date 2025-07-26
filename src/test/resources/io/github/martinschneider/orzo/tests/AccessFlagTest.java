package io.github.martinschneider.orzo.tests;

public enum AccessFlagTest {
  ACC_DEFAULT((short) 0x0000),
  ACC_PUBLIC((short) 0x0001),
  ACC_PRIVATE((short) 0x0002);

  public short val;

  private AccessFlagTest(short val) {
    this.val = val;
  }
}