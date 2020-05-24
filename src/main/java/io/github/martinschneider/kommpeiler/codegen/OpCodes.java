package io.github.martinschneider.kommpeiler.codegen;

public class OpCodes {
  public static final byte ICONST_M1 = (byte) 2;
  public static final byte ICONST_0 = (byte) 3;
  public static final byte ICONST_1 = (byte) 4;
  public static final byte ICONST_2 = (byte) 5;
  public static final byte ICONST_3 = (byte) 6;
  public static final byte ICONST_4 = (byte) 7;
  public static final byte ICONST_5 = (byte) 8;
  public static final byte LCONST_0 = (byte) 9;
  public static final byte LCONST_1 = (byte) 10;
  public static final byte FCONST_0 = (byte) 11;
  public static final byte FCONST_1 = (byte) 12;
  public static final byte FCONST_2 = (byte) 13;
  public static final byte DCONST_0 = (byte) 14;
  public static final byte DCONST_1 = (byte) 15;
  public static final byte BIPUSH = (byte) 16;
  public static final byte SIPUSH = (byte) 17;
  public static final byte LDC = (byte) 18;
  public static final byte LDC2_W = (byte) 20;
  public static final byte ILOAD = (byte) 21;
  public static final byte LLOAD = (byte) 22;
  public static final byte FLOAD = (byte) 22;
  public static final byte DLOAD = (byte) 24;
  public static final byte ILOAD_0 = (byte) 26;
  public static final byte ILOAD_1 = (byte) 27;
  public static final byte ILOAD_2 = (byte) 28;
  public static final byte ILOAD_3 = (byte) 29;
  public static final byte LLOAD_0 = (byte) 30;
  public static final byte LLOAD_1 = (byte) 31;
  public static final byte LLOAD_2 = (byte) 32;
  public static final byte LLOAD_3 = (byte) 33;
  public static final byte FLOAD_0 = (byte) 34;
  public static final byte FLOAD_1 = (byte) 35;
  public static final byte FLOAD_2 = (byte) 36;
  public static final byte FLOAD_3 = (byte) 37;
  public static final byte DLOAD_0 = (byte) 38;
  public static final byte DLOAD_1 = (byte) 39;
  public static final byte DLOAD_2 = (byte) 40;
  public static final byte DLOAD_3 = (byte) 41;
  public static final byte ISTORE = (byte) 54;
  public static final byte LSTORE = (byte) 55;
  public static final byte FSTORE = (byte) 56;
  public static final byte DSTORE = (byte) 57;
  public static final byte ISTORE_0 = (byte) 59;
  public static final byte ISTORE_1 = (byte) 60;
  public static final byte ISTORE_2 = (byte) 61;
  public static final byte ISTORE_3 = (byte) 62;
  public static final byte LSTORE_0 = (byte) 63;
  public static final byte LSTORE_1 = (byte) 64;
  public static final byte LSTORE_2 = (byte) 65;
  public static final byte LSTORE_3 = (byte) 66;
  public static final byte FSTORE_0 = (byte) 67;
  public static final byte FSTORE_1 = (byte) 68;
  public static final byte FSTORE_2 = (byte) 69;
  public static final byte FSTORE_3 = (byte) 70;
  public static final byte DSTORE_0 = (byte) 71;
  public static final byte DSTORE_1 = (byte) 72;
  public static final byte DSTORE_2 = (byte) 73;
  public static final byte DSTORE_3 = (byte) 74;
  public static final byte IADD = (byte) 96;
  public static final byte LADD = (byte) 97;
  public static final byte FADD = (byte) 98;
  public static final byte DADD = (byte) 99;
  public static final byte ISUB = (byte) 100;
  public static final byte LSUB = (byte) 101;
  public static final byte FSUB = (byte) 102;
  public static final byte DSUB = (byte) 103;
  public static final byte IMUL = (byte) 104;
  public static final byte LMUL = (byte) 105;
  public static final byte FMUL = (byte) 106;
  public static final byte DMUL = (byte) 107;
  public static final byte IDIV = (byte) 108;
  public static final byte LDIV = (byte) 109;
  public static final byte FDIV = (byte) 110;
  public static final byte DDIV = (byte) 111;
  public static final byte IREM = (byte) 112;
  public static final byte LREM = (byte) 113;
  public static final byte FREM = (byte) 114;
  public static final byte DREM = (byte) 115;
  public static final byte ISHL = (byte) 120;
  public static final byte LSHL = (byte) 121;
  public static final byte ISHR = (byte) 122;
  public static final byte LSHR = (byte) 123;
  public static final byte IUSHR = (byte) 124;
  public static final byte LUSHR = (byte) 125;
  public static final byte IAND = (byte) 126;
  public static final byte LAND = (byte) 127;
  public static final byte IOR = (byte) 128;
  public static final byte LOR = (byte) 129;
  public static final byte IXOR = (byte) 130;
  public static final byte LXOR = (byte) 131;
  public static final byte IINC = (byte) 132;
  public static final byte I2L = (byte) 133;
  public static final byte I2D = (byte) 135;
  public static final byte I2B = (byte) 145;
  public static final byte I2S = (byte) 147;
  public static final byte LCMP = (byte) 148;
  public static final byte IFEQ = (byte) 153;
  public static final byte IFNE = (byte) 154;
  public static final byte IFLT = (byte) 155;
  public static final byte IFGE = (byte) 156;
  public static final byte IFGT = (byte) 157;
  public static final byte IFLE = (byte) 158;
  public static final byte IF_ICMPEQ = (byte) 159;
  public static final byte IF_ICMPNE = (byte) 160;
  public static final byte IF_ICMPLT = (byte) 161;
  public static final byte IF_ICMPGE = (byte) 162;
  public static final byte IF_ICMPGT = (byte) 163;
  public static final byte IF_ICMPLE = (byte) 164;
  public static final byte GOTO = (byte) 167;
  public static final byte IRETURN = (byte) 172;
  public static final byte LRETURN = (byte) 173;
  public static final byte DRETURN = (byte) 175;
  public static final byte FRETURN = (byte) 174;
  public static final byte RETURN = (byte) 177;
  public static final byte GETSTATIC = (byte) 178;
  public static final byte INVOKEVIRTUAL = (byte) 182;
  public static final byte INVOKESTATIC = (byte) 184;
}
