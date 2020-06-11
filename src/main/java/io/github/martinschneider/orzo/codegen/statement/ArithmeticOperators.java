package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.OpCodes.DADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.DMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.DREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.FADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.FMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.FREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSUB;
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

public class ArithmeticOperators {
  // mapping operator and types to the corresponding JVM opcodes, e.g. LSHIFT and LONG should return
  // LSHL
  // I prefer the static map over using switch expressions
  public static final Map<Operators, Map<String, Byte>> map =
      Map.ofEntries(
          entry(
              PLUS,
              Map.of(
                  INT, IADD, DOUBLE, DADD, FLOAT, FADD, BYTE, IADD, CHAR, IADD, SHORT, IADD, LONG,
                  LADD)),
          entry(
              MINUS,
              Map.of(
                  INT, ISUB, DOUBLE, DSUB, FLOAT, FSUB, BYTE, ISUB, CHAR, ISUB, SHORT, ISUB, LONG,
                  LSUB)),
          entry(
              TIMES,
              Map.of(
                  INT, IMUL, DOUBLE, DMUL, FLOAT, FMUL, BYTE, IMUL, CHAR, IMUL, SHORT, IMUL, LONG,
                  LMUL)),
          entry(
              DIV,
              Map.of(
                  INT, IDIV, DOUBLE, DDIV, FLOAT, FDIV, BYTE, IDIV, CHAR, IDIV, SHORT, IDIV, LONG,
                  LDIV)),
          entry(
              MOD,
              Map.of(
                  INT, IREM, DOUBLE, DREM, FLOAT, FREM, BYTE, IREM, CHAR, IREM, SHORT, IREM, LONG,
                  LREM)),
          entry(LSHIFT, Map.of(INT, ISHL, LONG, LSHL)),
          entry(RSHIFT, Map.of(INT, ISHR, LONG, LSHR)),
          entry(RSHIFTU, Map.of(INT, IUSHR, LONG, LUSHR)),
          entry(BITWISE_AND, Map.of(INT, IAND, LONG, LAND)),
          entry(BITWISE_OR, Map.of(INT, IOR, LONG, LOR)),
          entry(BITWISE_XOR, Map.of(INT, IXOR, LONG, LXOR)));
}
