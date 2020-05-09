package io.github.martinschneider.kommpeiler.codegen;

public class OpCodes {
  public static final byte GETSTATIC = (byte) 0xb2;
  public static final byte LDC = (byte) 0x12;
  public static final byte INVOKEVIRTUAL = (byte) 0xb6;
  public static final byte RETURNVOID = (byte) 0xb1;
  public static final byte ICONST_M1 = (byte) 0x2;
  public static final byte ICONST_0 = (byte) 0x3;
  public static final byte ICONST_1 = (byte) 0x4;
  public static final byte ICONST_2 = (byte) 0x5;
  public static final byte ICONST_3 = (byte) 0x6;
  public static final byte ICONST_4 = (byte) 0x7;
  public static final byte ICONST_5 = (byte) 0x8;
  public static final byte BIPUSH = (byte) 0x10;
  public static final byte SIPUSH = (byte) 0x11;
}
