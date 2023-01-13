package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.*;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.*;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static java.util.Map.entry;

import io.github.martinschneider.orzo.lexer.tokens.Operator;
import java.util.Map;

public class OperatorMaps {
  // mapping operator and types to the corresponding JVM opcodes, e.g. LSHIFT and LONG should return
  // LSHL
  // I prefer the static map over using switch expressions
  public static final Map<Operator, Map<String, byte[]>> ARITHMETIC_OPS =
      Map.ofEntries(
          entry(
              PLUS,
              Map.of(
                  INT,
                  new byte[] {IADD},
                  DOUBLE,
                  new byte[] {DADD},
                  FLOAT,
                  new byte[] {FADD},
                  BYTE,
                  new byte[] {IADD},
                  CHAR,
                  new byte[] {IADD},
                  SHORT,
                  new byte[] {IADD},
                  LONG,
                  new byte[] {LADD})),
          entry(
              MINUS,
              Map.of(
                  INT,
                  new byte[] {ISUB},
                  DOUBLE,
                  new byte[] {DSUB},
                  FLOAT,
                  new byte[] {FSUB},
                  BYTE,
                  new byte[] {ISUB},
                  CHAR,
                  new byte[] {ISUB},
                  SHORT,
                  new byte[] {ISUB},
                  LONG,
                  new byte[] {LSUB})),
          entry(
              TIMES,
              Map.of(
                  INT,
                  new byte[] {IMUL},
                  DOUBLE,
                  new byte[] {DMUL},
                  FLOAT,
                  new byte[] {FMUL},
                  BYTE,
                  new byte[] {IMUL},
                  CHAR,
                  new byte[] {IMUL},
                  SHORT,
                  new byte[] {IMUL},
                  LONG,
                  new byte[] {LMUL})),
          entry(
              DIV,
              Map.of(
                  INT,
                  new byte[] {IDIV},
                  DOUBLE,
                  new byte[] {DDIV},
                  FLOAT,
                  new byte[] {FDIV},
                  BYTE,
                  new byte[] {IDIV},
                  CHAR,
                  new byte[] {IDIV},
                  SHORT,
                  new byte[] {IDIV},
                  LONG,
                  new byte[] {LDIV})),
          entry(
              MOD,
              Map.of(
                  INT,
                  new byte[] {IREM},
                  DOUBLE,
                  new byte[] {DREM},
                  FLOAT,
                  new byte[] {FREM},
                  BYTE,
                  new byte[] {IREM},
                  CHAR,
                  new byte[] {IREM},
                  SHORT,
                  new byte[] {IREM},
                  LONG,
                  new byte[] {LREM})),
          entry(
              LSHIFT,
              Map.of(
                  INT,
                  new byte[] {ISHL},
                  LONG,
                  new byte[] {LSHL},
                  BYTE,
                  new byte[] {ISHL},
                  SHORT,
                  new byte[] {ISHL})),
          entry(
              RSHIFT,
              Map.of(
                  INT,
                  new byte[] {ISHR},
                  LONG,
                  new byte[] {LSHR},
                  BYTE,
                  new byte[] {ISHR},
                  SHORT,
                  new byte[] {ISHR})),
          entry(
              RSHIFTU,
              Map.of(
                  INT,
                  new byte[] {IUSHR},
                  LONG,
                  new byte[] {LUSHR},
                  BYTE,
                  new byte[] {IUSHR},
                  SHORT,
                  new byte[] {IUSHR})),
          entry(
              BITWISE_AND,
              Map.of(
                  INT,
                  new byte[] {IAND},
                  LONG,
                  new byte[] {LAND},
                  BYTE,
                  new byte[] {IAND},
                  SHORT,
                  new byte[] {IAND})),
          entry(
              BITWISE_OR,
              Map.of(
                  INT,
                  new byte[] {IOR},
                  LONG,
                  new byte[] {LOR},
                  BYTE,
                  new byte[] {IOR},
                  SHORT,
                  new byte[] {IOR})),
          entry(
              BITWISE_XOR,
              Map.of(
                  INT,
                  new byte[] {IXOR},
                  LONG,
                  new byte[] {LXOR},
                  BYTE,
                  new byte[] {IXOR},
                  SHORT,
                  new byte[] {IXOR})),
          entry(
              NOTEQUAL,
              Map.of(
                  INT,
                  new byte[] {IF_ICMPNE},
                  BYTE,
                  new byte[] {IF_ICMPNE},
                  SHORT,
                  new byte[] {IF_ICMPNE},
                  CHAR,
                  new byte[] {IF_ICMPEQ},
                  BOOLEAN,
                  new byte[] {IF_ICMPNE},
                  LONG,
                  new byte[] {LCMP, IFNE})),
          entry(
              EQUAL,
              Map.of(
                  INT,
                  new byte[] {IF_ICMPEQ},
                  BYTE,
                  new byte[] {IF_ICMPEQ},
                  SHORT,
                  new byte[] {IF_ICMPEQ},
                  CHAR,
                  new byte[] {IF_ICMPEQ},
                  BOOLEAN,
                  new byte[] {IF_ICMPEQ},
                  LONG,
                  new byte[] {LCMP, IFEQ})),
          entry(
              GREATEREQ,
              Map.of(
                  INT,
                  new byte[] {IF_ICMPGE},
                  BYTE,
                  new byte[] {IF_ICMPGE},
                  SHORT,
                  new byte[] {IF_ICMPGE},
                  CHAR,
                  new byte[] {IF_ICMPGE},
                  LONG,
                  new byte[] {LCMP, IFGE})),
          entry(
              GREATER,
              Map.of(
                  INT,
                  new byte[] {IF_ICMPGT},
                  BYTE,
                  new byte[] {IF_ICMPGT},
                  SHORT,
                  new byte[] {IF_ICMPGT},
                  CHAR,
                  new byte[] {IF_ICMPGT},
                  LONG,
                  new byte[] {LCMP, IFGT})),
          entry(
              LESS,
              Map.of(
                  INT,
                  new byte[] {IF_ICMPLT},
                  BYTE,
                  new byte[] {IF_ICMPLT},
                  SHORT,
                  new byte[] {IF_ICMPLT},
                  CHAR,
                  new byte[] {IF_ICMPLT},
                  LONG,
                  new byte[] {LCMP, IFLT})),
          entry(
              LESSEQ,
              Map.of(
                  INT,
                  new byte[] {IF_ICMPLE},
                  BYTE,
                  new byte[] {IF_ICMPLE},
                  SHORT,
                  new byte[] {IF_ICMPLE},
                  CHAR,
                  new byte[] {IF_ICMPEQ},
                  LONG,
                  new byte[] {LCMP, IFLE})),
          entry(NEGATE, Map.of(BOOLEAN, new byte[] {ICONST_1, IXOR})),
          entry(LOGICAL_AND, Map.of(BOOLEAN, new byte[] {IAND})),
          entry(LOGICAL_OR, Map.of(BOOLEAN, new byte[] {IOR})));

  public static final Map<Operator, Byte> COMPARE_TO_ZERO_OPS =
      Map.of(EQUAL, IFEQ, NOTEQUAL, IFNE, GREATER, IFGT, GREATEREQ, IFGE, LESS, IFLT, LESSEQ, IFLE);

  public static final Map<String, Byte> DUP_OPS =
      Map.of(
          INT, DUP, SHORT, DUP, BYTE, DUP, FLOAT, DUP, CHAR, DUP, LONG, DUP2, DOUBLE, DUP2, BOOLEAN,
          DUP);
  public static final Map<String, Map<String, byte[]>> CAST_OPS =
      Map.ofEntries(
          entry(
              BYTE,
              Map.of(
                  LONG,
                  new byte[] {I2L},
                  SHORT,
                  new byte[] {I2S},
                  CHAR,
                  new byte[] {I2C},
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
                  CHAR,
                  new byte[] {I2C},
                  DOUBLE,
                  new byte[] {I2D},
                  FLOAT,
                  new byte[] {I2F})),
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
                  CHAR,
                  new byte[] {I2C})),
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
                  CHAR,
                  new byte[] {L2I, I2C})),
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
                  CHAR,
                  new byte[] {F2I, I2C})),
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
                  CHAR,
                  new byte[] {D2I, I2C})),
          entry(
              CHAR,
              Map.of(
                  LONG,
                  new byte[] {I2L},
                  DOUBLE,
                  new byte[] {I2D},
                  FLOAT,
                  new byte[] {I2F},
                  BYTE,
                  new byte[] {I2B},
                  SHORT,
                  new byte[] {I2S})));

  // never cast "below" integer
  public static final Map<String, Map<String, byte[]>> CAST_OPS_1 =
      Map.ofEntries(
          entry(
              BYTE,
              Map.of(LONG, new byte[] {I2L}, DOUBLE, new byte[] {I2D}, FLOAT, new byte[] {I2F})),
          entry(
              SHORT,
              Map.of(LONG, new byte[] {I2L}, DOUBLE, new byte[] {I2D}, FLOAT, new byte[] {I2F})),
          entry(
              INT,
              Map.of(LONG, new byte[] {I2L}, DOUBLE, new byte[] {I2D}, FLOAT, new byte[] {I2F})),
          entry(
              LONG,
              Map.of(
                  INT,
                  new byte[] {L2I},
                  BYTE,
                  new byte[] {L2I},
                  SHORT,
                  new byte[] {L2I},
                  DOUBLE,
                  new byte[] {L2D},
                  FLOAT,
                  new byte[] {L2F},
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
                  new byte[] {F2I},
                  SHORT,
                  new byte[] {F2I},
                  DOUBLE,
                  new byte[] {F2D},
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
                  new byte[] {D2I},
                  SHORT,
                  new byte[] {D2I},
                  FLOAT,
                  new byte[] {D2F},
                  CHAR,
                  new byte[] {D2I})),
          entry(
              CHAR,
              Map.of(LONG, new byte[] {I2L}, DOUBLE, new byte[] {I2D}, FLOAT, new byte[] {I2F})));
}
