package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.D2F;
import static io.github.martinschneider.orzo.codegen.OpCodes.D2I;
import static io.github.martinschneider.orzo.codegen.OpCodes.D2L;
import static io.github.martinschneider.orzo.codegen.OpCodes.DADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.DMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.DREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP2;
import static io.github.martinschneider.orzo.codegen.OpCodes.F2D;
import static io.github.martinschneider.orzo.codegen.OpCodes.F2I;
import static io.github.martinschneider.orzo.codegen.OpCodes.F2L;
import static io.github.martinschneider.orzo.codegen.OpCodes.FADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.FMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.FREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2B;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2D;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2F;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2L;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2S;
import static io.github.martinschneider.orzo.codegen.OpCodes.IADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.IAND;
import static io.github.martinschneider.orzo.codegen.OpCodes.IDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.IMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.IOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.IREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISHL;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.IUSHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.IXOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.L2D;
import static io.github.martinschneider.orzo.codegen.OpCodes.L2F;
import static io.github.martinschneider.orzo.codegen.OpCodes.L2I;
import static io.github.martinschneider.orzo.codegen.OpCodes.LADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LAND;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.LMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.LOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSHL;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.LUSHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LXOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static java.util.Map.entry;

import io.github.martinschneider.orzo.lexer.tokens.Operators;
import java.util.Map;

public class OperatorMaps {
  // mapping operator and types to the corresponding JVM opcodes, e.g. LSHIFT and LONG should return
  // LSHL
  // I prefer the static map over using switch expressions
  public static final Map<Operators, Map<String, Byte>> arithmeticOps =
      Map.ofEntries(
          entry(
              PLUS,
              Map.of(
                  INT, IADD, DOUBLE, DADD, FLOAT, FADD, BYTE, IADD, CHAR, IADD, SHORT, IADD, LONG,
                  LADD, BOOLEAN, IADD)),
          entry(
              MINUS,
              Map.of(
                  INT, ISUB, DOUBLE, DSUB, FLOAT, FSUB, BYTE, ISUB, CHAR, ISUB, SHORT, ISUB, LONG,
                  LSUB, BOOLEAN, ISUB)),
          entry(
              TIMES,
              Map.of(
                  INT, IMUL, DOUBLE, DMUL, FLOAT, FMUL, BYTE, IMUL, CHAR, IMUL, SHORT, IMUL, LONG,
                  LMUL, BOOLEAN, IMUL)),
          entry(
              DIV,
              Map.of(
                  INT, IDIV, DOUBLE, DDIV, FLOAT, FDIV, BYTE, IDIV, CHAR, IDIV, SHORT, IDIV, LONG,
                  LDIV, BOOLEAN, IDIV)),
          entry(
              MOD,
              Map.of(
                  INT, IREM, DOUBLE, DREM, FLOAT, FREM, BYTE, IREM, CHAR, IREM, SHORT, IREM, LONG,
                  LREM, BOOLEAN, IREM)),
          entry(LSHIFT, Map.of(INT, ISHL, LONG, LSHL, BYTE, ISHL, SHORT, ISHL, BOOLEAN, ISHL)),
          entry(RSHIFT, Map.of(INT, ISHR, LONG, LSHR, BYTE, ISHR, SHORT, ISHR, BOOLEAN, ISHR)),
          entry(
              RSHIFTU, Map.of(INT, IUSHR, LONG, LUSHR, BYTE, IUSHR, SHORT, IUSHR, BOOLEAN, IUSHR)),
          entry(BITWISE_AND, Map.of(INT, IAND, LONG, LAND, BYTE, IAND, SHORT, IAND, BOOLEAN, IAND)),
          entry(BITWISE_OR, Map.of(INT, IOR, LONG, LOR, BYTE, IOR, SHORT, IOR, BOOLEAN, IOR)),
          entry(
              BITWISE_XOR, Map.of(INT, IXOR, LONG, LXOR, BYTE, IXOR, SHORT, IXOR, BOOLEAN, IXOR)));
  public static final Map<String, Byte> dupOps =
      Map.of(
          INT, DUP, SHORT, DUP, BYTE, DUP, FLOAT, DUP, CHAR, DUP, LONG, DUP2, DOUBLE, DUP2, BOOLEAN,
          DUP);
  public static final Map<String, Map<String, byte[]>> castOps =
      Map.ofEntries(
          entry(
              BYTE,
              Map.of(
                  LONG,
                  new byte[] {I2L},
                  SHORT,
                  new byte[] {I2S},
                  DOUBLE,
                  new byte[] {I2D},
                  FLOAT,
                  new byte[] {I2F})),
          entry(
              SHORT,
              Map.of(
                  LONG,
                  new byte[] {I2L},
                  BYTE,
                  new byte[] {I2B},
                  DOUBLE,
                  new byte[] {I2D},
                  FLOAT,
                  new byte[] {I2F},
                  BOOLEAN,
                  new byte[] {I2B})),
          entry(
              BOOLEAN,
              Map.of(LONG, new byte[] {I2L}, DOUBLE, new byte[] {I2D}, FLOAT, new byte[] {I2F})),
          entry(
              INT,
              Map.of(
                  LONG,
                  new byte[] {I2L},
                  BYTE,
                  new byte[] {I2B},
                  SHORT,
                  new byte[] {I2S},
                  DOUBLE,
                  new byte[] {I2D},
                  FLOAT,
                  new byte[] {I2F},
                  BOOLEAN,
                  new byte[] {I2B})),
          entry(
              LONG,
              Map.of(
                  INT,
                  new byte[] {L2I},
                  BYTE,
                  new byte[] {L2I, I2B},
                  SHORT,
                  new byte[] {L2I, I2S},
                  DOUBLE,
                  new byte[] {L2D},
                  FLOAT,
                  new byte[] {L2F},
                  BOOLEAN,
                  new byte[] {L2I, I2B},
                  CHAR,
                  new byte[] {L2I})),
          entry(
              FLOAT,
              Map.of(
                  LONG,
                  new byte[] {F2L},
                  INT,
                  new byte[] {F2I},
                  BYTE,
                  new byte[] {F2I, I2B},
                  SHORT,
                  new byte[] {F2I, I2S},
                  DOUBLE,
                  new byte[] {F2D},
                  BOOLEAN,
                  new byte[] {F2I, I2B},
                  CHAR,
                  new byte[] {F2I})),
          entry(
              DOUBLE,
              Map.of(
                  LONG,
                  new byte[] {D2L},
                  INT,
                  new byte[] {D2I},
                  BYTE,
                  new byte[] {D2I, I2B},
                  SHORT,
                  new byte[] {D2I, I2S},
                  FLOAT,
                  new byte[] {D2F},
                  BOOLEAN,
                  new byte[] {D2I, I2B},
                  CHAR,
                  new byte[] {D2I})),
          entry(
              CHAR,
              Map.of(LONG, new byte[] {I2L}, DOUBLE, new byte[] {I2D}, FLOAT, new byte[] {I2F})));
}
