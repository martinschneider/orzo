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
import static io.github.martinschneider.orzo.codegen.OpCodes.I2C;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2D;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2F;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2L;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2S;
import static io.github.martinschneider.orzo.codegen.OpCodes.IADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.IAND;
import static io.github.martinschneider.orzo.codegen.OpCodes.IDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFGE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFGT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFLE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFLT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPGE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPGT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPLE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPLT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPNE;
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
import static io.github.martinschneider.orzo.codegen.OpCodes.LCMP;
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
import static io.github.martinschneider.orzo.lexer.tokens.Operators.EQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESSEQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NOTEQUAL;
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
  public static final Map<Operators, Map<String, byte[]>> ARITHMETIC_OPS =
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
                  new byte[] {LCMP, IFLE})));

  public static final Map<Operators, Byte> COMPARE_TO_ZERO_OPS =
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
