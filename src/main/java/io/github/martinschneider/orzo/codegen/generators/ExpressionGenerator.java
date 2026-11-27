package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ACONST_NULL;
import static io.github.martinschneider.orzo.codegen.OpCodes.ANEWARRAY;
import static io.github.martinschneider.orzo.codegen.OpCodes.DCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP2;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP2_X1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP2_X2;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP_X1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP_X2;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2B;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2C;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2S;
import static io.github.martinschneider.orzo.codegen.OpCodes.IAND;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFGE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFGT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFLE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFLT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ACMPEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ACMPNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPGE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPGT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPLE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPLT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.INSTANCEOF;
import static io.github.martinschneider.orzo.codegen.OpCodes.IOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.IXOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.NEWARRAY;
import static io.github.martinschneider.orzo.codegen.OpCodes.SWAP;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getArrayType;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getStoreOpCode;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.ARITHMETIC_OPS;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.COMPARE_TO_ZERO_OPS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.CAST;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.EQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESSEQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LOGICAL_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LOGICAL_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NEGATE;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NOTEQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POW;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT_ZERO;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.ByteUtils;
import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.EnclosingInstances;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.ExpressionTypes;
import io.github.martinschneider.orzo.codegen.FieldProcessor;
import io.github.martinschneider.orzo.codegen.GenericTypes;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.OperandStack;
import io.github.martinschneider.orzo.codegen.Statements;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.CastOperator;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.InstanceofToken;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.LambdaExpr;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.SwitchExpr;
import io.github.martinschneider.orzo.lexer.tokens.TernaryExpression;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.InnerClasses;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.CastMethodCall;
import io.github.martinschneider.orzo.parser.productions.ClassMember;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ExpressionStatement;
import io.github.martinschneider.orzo.parser.productions.InstanceInitializer;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.tree.Renaming;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExpressionGenerator {
  private static final String LOGGER_NAME = "expression code generator";

  public CGContext ctx;
  // (for the operations, see ExpressionTypes.isOperation())
  private ExpressionTreeGenerator treeGen;
  // generic type of the functional interface a lambda argument is passed as (set by
  // MethodCallGenerator.pushArgs(), null if unknown), e.g. "java.util.function.Function<a.Foo,R>"
  public String lambdaTargetGeneric;
  // The generic type the method call being evaluated as an argument is expected to have (its
  // parameter's type, set by MethodCallGenerator.pushArgs(), null if unknown), e.g.
  // "java.util.Comparator<a.Foo>": used to infer the called method's type variables
  public String callTargetGeneric;
  private static final Map<String, String> PRIMITIVE_WRAPPERS =
      Map.of(
          "void", "java/lang/Void",
          "boolean", "java/lang/Boolean",
          "byte", "java/lang/Byte",
          "char", "java/lang/Character",
          "short", "java/lang/Short",
          "int", "java/lang/Integer",
          "long", "java/lang/Long",
          "float", "java/lang/Float",
          "double", "java/lang/Double");

  private static final List<Operators> COMPARATORS =
      List.of(EQUAL, NOTEQUAL, GREATER, LESS, LESSEQ, GREATEREQ);

  // "++a.b.c", "a[i]--", ... inside an expression: a field of another object or an array element
  private static boolean isChainedIncrement(List<Token> tokens, int i) {
    if (!(tokens.get(i) instanceof Identifier) || i + 1 >= tokens.size()) {
      return false;
    }
    Identifier id = (Identifier) tokens.get(i);
    Token next = tokens.get(i + 1);
    return (id.next != null || id.arrSel != null)
        && (next.eq(op(PRE_INCREMENT))
            || next.eq(op(PRE_DECREMENT))
            || next.eq(op(POST_INCREMENT))
            || next.eq(op(POST_DECREMENT)));
  }

  /**
   * "a[i]++", "--x.arr[f()]" (the operand evaluated once: array reference and index are DUP2'ed).
   * Returns the element type, or null (generating nothing) for an unsupported operand, e.g. a
   * multi-dimensional array or one returned by a method call.
   */
  public String incrementArrayElement(
      DynamicByteArray out, Identifier id, Operators op, boolean leaveValue) {
    List<Identifier> links = new ArrayList<>();
    for (Identifier link = id; link != null; link = link.next) {
      if (link.getClass() != Identifier.class) {
        return null;
      }
      links.add(link);
    }
    Identifier last = links.get(links.size() - 1);
    if (last.arrSel == null || last.arrSel.exprs.size() != 1) {
      return null;
    }
    String elemType =
        TypeUtils.jvmDescriptorToTypeName(
            new NumExprTypeDecider(ctx).getType(ctx.classIdMap, new Expression(List.of(id))));
    String arithType = List.of(BYTE, SHORT, CHAR).contains(elemType) ? INT : elemType;
    if (!ONE.containsKey(arithType)) {
      return null;
    }
    // the array reference: the same chain without the last selector
    Identifier arrayRef = null;
    Identifier tail = null;
    for (Identifier link : links) {
      Identifier copy = new Identifier(link.val.toString(), (link == last) ? null : link.arrSel);
      if (arrayRef == null) {
        arrayRef = copy;
      } else {
        tail.next = copy;
      }
      tail = copy;
    }
    eval(out, null, new Expression(new ArrayList<>(List.of(arrayRef))));
    eval(out, INT, last.arrSel.exprs.get(0));
    boolean wide = LONG.equals(elemType) || DOUBLE.equals(elemType);
    boolean pre = op == PRE_INCREMENT || op == PRE_DECREMENT;
    Operators arith = (op == PRE_DECREMENT || op == POST_DECREMENT) ? MINUS : PLUS;
    out.write(DUP2);
    out.write(TypeUtils.getLoadOpCode(elemType));
    if (leaveValue && !pre) {
      out.write(wide ? DUP2_X2 : DUP_X2);
    }
    out.write(ONE.get(arithType));
    out.write(ARITHMETIC_OPS.get(arith).get(arithType));
    if (NARROWING.containsKey(elemType)) {
      out.write(NARROWING.get(elemType));
    }
    if (leaveValue && pre) {
      out.write(wide ? DUP2_X2 : DUP_X2);
    }
    out.write(TypeUtils.getStoreOpCode(elemType));
    ctx.opStack.pop(2); // the array reference and the index
    if (leaveValue) {
      ctx.opStack.push(elemType);
    }
    return elemType;
  }

  private static final Map<String, Byte> ONE =
      Map.of(INT, ICONST_1, LONG, LCONST_1, FLOAT, FCONST_1, DOUBLE, DCONST_1);
  private static final Map<String, Byte> NARROWING = Map.of(BYTE, I2B, SHORT, I2S, CHAR, I2C);

  // Desugars into "a.b.c = a.b.c + 1" plus a load of "a.b.c" before (post-increment) or after it
  // (pre-increment). The receiver is evaluated more than once (harmless for a chain of fields).
  private String generateChainedIncrement(DynamicByteArray out, Identifier id, Operators op) {
    String elemType = incrementArrayElement(out, id, op, true);
    if (elemType != null) {
      return elemType;
    }
    Operators arith = (op == PRE_DECREMENT || op == POST_DECREMENT) ? MINUS : PLUS;
    boolean pre = op == PRE_INCREMENT || op == PRE_DECREMENT;
    Expression value = new Expression(new ArrayList<>(List.of(id)));
    String valueType = new NumExprTypeDecider(ctx).getType(ctx.classIdMap, value);
    Expression rhs =
        new Expression(new ArrayList<>(List.of(id, new IntLiteral(1, false), op(arith))));
    Assignment assignment =
        new Assignment(new ArrayList<>(List.of(id)), new ArrayList<>(List.of(rhs)));
    if (!pre) {
      eval(out, valueType, value);
    }
    ctx.delegator.generate(out, null, assignment);
    if (pre) {
      eval(out, valueType, value);
    }
    return valueType;
  }

  public ExpressionResult eval(DynamicByteArray out, String type, Expression expr) {
    // A value: a comparison's bare branch (see the 5-argument eval()) becomes a 0 or 1.
    if (type != null
        && !TypeUtils.isPrimitive(type)
        && !STRING.equals(type)
        && expr.cast == null
        && expr.tokens.stream().anyMatch(t -> t instanceof Operator)
        && STRING.equals(new NumExprTypeDecider(ctx).getType(ctx.classIdMap, expr))) {
      // a string concatenation for a general reference type, e.g. "Object o = s + i;" or
      // "list.add(s + i)": evaluated as a String (not with its operands converted to Object)
      type = STRING;
    }
    int startPos = out.size();
    ExpressionResult result = eval(out, type, expr, true, false);
    if (out.size() > startPos) {
      byte[] written = Arrays.copyOfRange(out.getBytes(), startPos, out.size());
      int lastOp = lastOpcode(written);
      if (lastOp >= 153 && lastOp <= 166) {
        // Branch opcode without its 2-byte operand: add offset + value-producing tail.
        // Branch fires when condition is TRUE → jump to ICONST_1 (7 bytes ahead of opcode).
        out.write((short) 7); // 2-byte offset to ICONST_1
        out.write(ICONST_0); // false case (fall-through)
        out.write(GOTO);
        out.write((short) 4); // 4 bytes from GOTO opcode → past ICONST_1
        out.write(ICONST_1); // true case
      }
    }
    return result;
  }

  // Returns the opcode of the last instruction in the bytecode array by forward-scanning.
  private static int lastOpcode(byte[] code) {
    int i = 0;
    int lastPos = 0;
    while (i < code.length) {
      lastPos = i;
      i += instrLen(code, i);
    }
    return code[lastPos] & 0xFF;
  }

  // Returns the byte length of the instruction at code[pos].
  private static int instrLen(byte[] code, int pos) {
    int op = code[pos] & 0xFF;
    if (op <= 15
        || (op >= 26 && op <= 53)
        || (op >= 59 && op <= 131)
        || op == 133
        || (op >= 136 && op <= 147)
        || op == 148
        || (op >= 172 && op <= 177)
        || op == 190
        || op == 191
        || op == 194
        || op == 195) {
      return 1;
    }
    switch (op) {
      case 16:
      case 18:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 54:
      case 55:
      case 56:
      case 57:
      case 58:
      case 132:
      case 169:
      case 188:
        return 2;
      case 17:
      case 19:
      case 20:
      case 153:
      case 154:
      case 155:
      case 156:
      case 157:
      case 158:
      case 159:
      case 160:
      case 161:
      case 162:
      case 163:
      case 164:
      case 165:
      case 166:
      case 167:
      case 168:
      case 178:
      case 179:
      case 180:
      case 181:
      case 182:
      case 183:
      case 184:
      case 187:
      case 189:
      case 192:
      case 193:
      case 197:
      case 198:
      case 199:
        return 3;
      case 185:
      case 186:
      case 200:
      case 201:
        return 5;
      default:
        return 1;
    }
  }

  public ExpressionResult eval(
      DynamicByteArray out, String type, Expression expr, boolean pushIfZero, boolean reverseComp) {
    if (expr != null && expr.tree != null && ExpressionTypes.isOperation(expr.tree)) {
      if (treeGen == null) {
        treeGen = new ExpressionTreeGenerator(ctx);
      }
      return treeGen.generate(out, type, expr.tree, pushIfZero, reverseComp);
    }
    if (expr instanceof ArrayInit) {
      ArrayInit arrInit = (ArrayInit) expr;
      generateArray(out, ctx.classIdMap, arrInit);
      return new ExpressionResult(arrInit.typeDescr(), null); // the array type, e.g. "[byte"
    }
    if (type == null) {
      type = new NumExprTypeDecider(ctx).getType(ctx.classIdMap, expr);
    }
    if (expr != null
        && expr.cast != null
        && expr.tokens.size() == 1
        && expr.tokens.get(0) instanceof LambdaExpr) {
      type = expr.cast.name; // (the lambda's target type, e.g. "(Runnable) () -> {}")
    }
    Object val = null;
    if (expr == null) {
      return null;
    }
    // this stack keeps track of the operands loaded on the stack for the current expression
    // this is used for code optimization (for example, using IFLE instead of IF_CMPLE if one of the
    // operands is 0)
    OperandStack exprTypeStack = new OperandStack();
    List<Token> tokens = expr.tokens;
    int logOpIdx = findOutermostLogicalOp(tokens);
    if (logOpIdx >= 0) {
      Operators logOpType = ((Operator) tokens.get(logOpIdx)).opValue();
      int rStart = findRightOperandStart(tokens, logOpIdx);
      List<Token> leftTokens = new ArrayList<>(tokens.subList(0, rStart));
      List<Token> rightTokens = new ArrayList<>(tokens.subList(rStart, logOpIdx));
      DynamicByteArray leftOut = evalAsValue(null, new Expression(leftTokens));
      out.write(leftOut.getBytes());
      DynamicByteArray rightOut = evalAsValue(null, new Expression(rightTokens));
      byte[] rightBytes = rightOut.getBytes();
      if (logOpType == LOGICAL_AND) {
        out.write(IFEQ);
        out.write((short) (rightBytes.length + 6));
        out.write(rightBytes);
        out.write(GOTO);
        out.write((short) 4);
        out.write(ICONST_0);
      } else {
        out.write(IFNE);
        out.write((short) (rightBytes.length + 6));
        out.write(rightBytes);
        out.write(GOTO);
        out.write((short) 4);
        out.write(ICONST_1);
      }
      if (reverseComp) {
        out.write(IFEQ);
      }
      ctx.opStack.pop();
      ctx.opStack.pop();
      ctx.opStack.push(BOOLEAN);
      return new ExpressionResult(BOOLEAN, null);
    }
    // "!(a && b)", "!(a | b)" etc.: evaluate the (short-circuiting) operand as a 0/1 value and
    // invert it, rather than processing its tokens one by one below (also a negated comparison
    // whose value is used, e.g. "!(n > 3)" in "!b && !(n > 3)": its branch opcode is completed
    // to a value first)
    if (tokens.size() > 1 && tokens.get(tokens.size() - 1).eq(op(NEGATE))) {
      List<Token> operand = new ArrayList<>(tokens.subList(0, tokens.size() - 1));
      Token last = operand.get(operand.size() - 1);
      boolean comparison =
          last instanceof Operator && COMPARATORS.contains(((Operator) last).opValue());
      if (findOutermostLogicalOp(operand) >= 0
          || findOutermostBooleanBitOp(operand) != null
          || (comparison && !reverseComp)) {
        out.write(evalAsValue(null, new Expression(operand)).getBytes());
        out.write(ICONST_1);
        out.write(IXOR);
        if (reverseComp) {
          out.write(IFEQ);
        }
        return new ExpressionResult(BOOLEAN, null);
      }
    }
    // Non-short-circuit boolean &, |, ^ (e.g. "a != null & b != null"): both operands are
    // evaluated to 0/1 values first, then combined bitwise.
    Operators boolOp = findOutermostBooleanBitOp(tokens);
    if (boolOp != null) {
      int opIdx = tokens.size() - 1;
      int rStart = findRightOperandStart(tokens, opIdx);
      out.write(
          evalAsValue(null, new Expression(new ArrayList<>(tokens.subList(0, rStart)))).getBytes());
      out.write(
          evalAsValue(null, new Expression(new ArrayList<>(tokens.subList(rStart, opIdx))))
              .getBytes());
      out.write(boolOp == BITWISE_AND ? IAND : (boolOp == BITWISE_OR ? IOR : IXOR));
      if (reverseComp) {
        out.write(IFEQ);
      }
      ctx.opStack.pop();
      ctx.opStack.pop();
      ctx.opStack.push(BOOLEAN);
      return new ExpressionResult(BOOLEAN, null);
    }
    boolean branchEmitted = false;
    int lastBranchPos = -1;
    byte lastBranchOpCode = 0;
    // Pre-scan: find assignment expressions (var = expr) where lhs is a simple local variable.
    // These need special handling: skip loading lhs old value; DUP+STORE rhs at ASSIGN.
    Map<Integer, VariableInfo> skipLoad = new HashMap<>();
    Map<Integer, VariableInfo> assignTargets = findAssignmentTargets(tokens, skipLoad);
    // True right after processing an array-element access (e.g. "arr[0]"): the element type
    // (e.g. "byte") just got assigned to `type` below for codegen purposes, but it must not be
    // mistaken for the expression's originally requested type when sizing a literal that follows
    // (e.g. the 0xFF in "arr[0] & 0xFF" needs to push as int, not get narrowed to byte).
    boolean typeFromArrayElement = false;
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (isChainedIncrement(tokens, i)) {
        exprTypeStack.push(
            generateChainedIncrement(
                out, (Identifier) token, ((Operator) tokens.get(i + 1)).opValue()));
        i++; // the increment operator
      } else if (token instanceof Identifier) {
        if (skipLoad.containsKey(i)) {
          // LHS of assignment expression: skip loading old value; ASSIGN handler will DUP+STORE.
          // An instance field target additionally needs its "this" reference pushed now (before
          // the RHS is evaluated) so PUTFIELD sees [this, rhsValue] in the right order later.
          VariableInfo skipTarget = skipLoad.get(i);
          if (skipTarget.isField && !skipTarget.accFlags.contains(AccessFlag.ACC_STATIC)) {
            ctx.loadGen.loadOwner(out, skipTarget);
            ctx.opStack.push(REF);
          }
        } else {
          String operandType = handleId(out, ctx.classIdMap, tokens, i, token, type);
          if ("java.lang.String".equals(operandType)) {
            operandType = STRING; // (e.g. a lambda parameter typed by a type argument)
          }
          exprTypeStack.push(operandType);
          // In a string concatenation (e.g. "i + str"), a primitive operand's own type must not
          // replace the expression type: the next operand would be "converted" to it.
          if (!(STRING.equals(type)
              && TypeUtils.isPrimitive(operandType)
              && !BOOLEAN.equals(operandType)
              && tokens.contains(op(PLUS)))) {
            type = operandType;
          }
          // Only byte/short/char elements need this: Java always promotes them to int for
          // arithmetic/bitwise ops, unlike float/double/long elements, which a following
          // literal operand must still match (e.g. for FMUL/DMUL/LMUL to see matching types).
          typeFromArrayElement =
              ((Identifier) token).arrSel != null
                  && (BYTE.equals(type) || SHORT.equals(type) || CHAR.equals(type));
        }
      } else if (token instanceof IntLiteral) {
        BigInteger bigInt = (BigInteger) ((IntLiteral) token).val;
        Long intValue = bigInt.longValue();
        // When the outer type is String (string-concat context), use the natural numeric type
        // so that int literals generate real bytecode and are available for arithmetic.
        // (likewise in a boolean context, e.g. a comparison operand, and for the operand of a cast)
        String literalType =
            (STRING.equals(type)
                    || isNonPrimitive(type)
                    || typeFromArrayElement
                    || BOOLEAN.equals(type)
                    || (i + 1 < tokens.size() && tokens.get(i + 1) instanceof CastOperator))
                ? (intValue > Integer.MAX_VALUE ? LONG : INT)
                : type;
        typeFromArrayElement = false;
        // look ahead for <<, >> or >>> operators which require the second argument to
        // be an integer
        boolean pushed = true;
        if (i + 1 < tokens.size()
            && ((tokens.get(i + 1).eq(op(LSHIFT))
                || (tokens.get(i + 1).eq(op(RSHIFT)))
                || (tokens.get(i + 1).eq(op(RSHIFTU)))))) {
          ctx.pushGen.push(out, INT, BigDecimal.valueOf(intValue));
          literalType = INT;
        } else if (!literalType.equals(INT) || intValue != 0 || pushIfZero) {
          ctx.pushGen.push(out, literalType, BigDecimal.valueOf(intValue));
        } else {
          pushed = false;
        }
        // special handling for an (unpushed) 0 because there are different opcodes to compare to
        // 0 - a 0 that *was* pushed must be compared like any other operand
        if (!pushed
            && (literalType.equals(INT)
                || literalType.equals(SHORT)
                || literalType.equals(BYTE)
                || literalType.equals(CHAR))
            && intValue.intValue() == 0) {
          exprTypeStack.push(INT_ZERO);
        } else {
          exprTypeStack.push(literalType);
        }
        val = bigInt;
      } else if (token instanceof BoolLiteral) {
        Boolean bool = (Boolean) ((BoolLiteral) token).val;
        ctx.pushGen.pushBool(out, bool);
        val = bool;
        exprTypeStack.push(BOOLEAN);
      } else if (token instanceof FPLiteral) {
        BigDecimal bigDec = (BigDecimal) ((FPLiteral) token).val;
        // (a double, or a float, for a reference type, e.g. String or Double)
        String pushType =
            TypeUtils.isPrimitive(type) ? type : ("java.lang.Float".equals(type) ? FLOAT : DOUBLE);
        ctx.pushGen.push(out, pushType, bigDec);
        val = bigDec;
        exprTypeStack.push(FLOAT.equals(pushType) ? FLOAT : DOUBLE);
      } else if (token instanceof Str) {
        ctx.loadGen.ldc(out, CONSTANT_STRING, ((Str) token).strValue());
        // Do NOT set type = STRING here; let exprTypeStack track the actual element type.
        // Setting type=STRING would cause subsequent int/float tokens to generate no bytecode.
        exprTypeStack.push(STRING);
      } else if (token instanceof Chr) {
        char chr = (char) ((Chr) token).val;
        ctx.pushGen.push(out, CHAR, chr);
        if (!STRING.equals(type)) {
          type = CHAR; // (but keep a string-concatenation context, e.g. "'[' + str")
        }
        exprTypeStack.push(CHAR);
      } else if (token instanceof InstanceofToken) {
        InstanceofToken instof = (InstanceofToken) token;
        if (instof.binding != null) {
          ctx.errors.addError(
              "expression generator",
              "type patterns are not supported here",
              new RuntimeException().getStackTrace());
        }
        String jvmType = resolveJvmClassName(instof.targetType);
        ctx.constPool.addClass(jvmType);
        out.write(INSTANCEOF);
        out.write(ctx.constPool.indexOf(CONSTANT_CLASS, jvmType));
        ctx.opStack.pop();
        ctx.opStack.push(BOOLEAN);
        type = BOOLEAN;
        exprTypeStack.push(BOOLEAN);
      } else if (token instanceof TernaryExpression) {
        type = generateTernary(out, type, (TernaryExpression) token);
        exprTypeStack.push(type);
      } else if (token instanceof LambdaExpr) {
        type = generateLambda(out, type, (LambdaExpr) token);
        exprTypeStack.push(type);
      } else if (token instanceof SwitchExpr) {
        type = generateSwitch(out, type, (SwitchExpr) token);
        exprTypeStack.push(type);
      } else if (token instanceof CastOperator) {
        // e.g. "(int) b" in "a + (int) b": converts the operand on top of the stack
        String castType = ((CastOperator) token).type;
        ctx.basicGen.checkCastable(null, exprTypeStack.type(), castType);
        ctx.basicGen.convert(out, exprTypeStack.type(), castType);
        exprTypeStack.pop();
        exprTypeStack.push(castType);
      } else if (token instanceof Operator) {
        Operators op = ((Operator) token).opValue();
        if (List.of(POST_INCREMENT, POST_DECREMENT, PRE_INCREMENT, PRE_DECREMENT).contains(op)) {
          VariableInfo varInfo = ctx.classIdMap.variables.get(tokens.get(i - 1));
          ctx.incrGen.inc(out, varInfo, op, false);
        } else if (op.equals(POW)) {
          type = generatePow(out, exprTypeStack, DOUBLE.equals(type));
        } else if (op == ASSIGN) {
          if (assignTargets.containsKey(i)) {
            // Assignment expression: RHS is on stack, LHS old value was NOT loaded.
            // DUP rhs so one copy stays for the expression value; store the other into the var.
            VariableInfo target = assignTargets.get(i);
            String rhsType = ctx.opStack.type();
            boolean isWide = LONG.equals(rhsType) || DOUBLE.equals(rhsType);
            boolean instanceField =
                target.isField && !target.accFlags.contains(AccessFlag.ACC_STATIC);
            if (instanceField) {
              // Stack is [this, rhsValue] (loadReference(this) was emitted when the LHS token
              // was skipped). DUP_X1/DUP2_X1 duplicates rhsValue below "this" so PUTFIELD sees
              // [this, rhsValue] and the duplicate is left as the expression's value.
              out.write(isWide ? DUP2_X1 : DUP_X1);
            } else {
              out.write(isWide ? DUP2 : DUP);
            }
            ctx.opStack.push(rhsType != null ? rhsType : REF);
            ctx.storeGen.store(out, target);
            ctx.opStack.pop();
            if (instanceField) {
              ctx.opStack.pop(); // the "this" reference, consumed by PUTFIELD
            }
            type = rhsType != null ? rhsType : REF;
          }
          // else: complex LHS (field/array) — fall through, do nothing (unsupported)
        } else if (op.equals(PLUS)
            && (STRING.equals(exprTypeStack.type()) || STRING.equals(exprTypeStack.secondType()))) {
          String leftType = exprTypeStack.secondType();
          // topType is the actual type of the right operand (top of expression type stack)
          String topType = exprTypeStack.type();
          // (a String that may be null becomes "null", like an Object: concat() would throw)
          if (STRING.equals(topType) && mayBeNull(tokens.get(i - 1))) {
            topType = REF;
          }
          if (STRING.equals(leftType)
              && mayBeNull(tokens.get(findRightOperandStart(tokens, i) - 1))) {
            leftType = REF;
          }
          // the right operand as a String, then the left one (under it)
          if (!STRING.equals(topType)) {
            if (isNonPrimitive(topType)) {
              ctx.invokeGen.invokeStatic(
                  out,
                  new Method("java.lang.String", "valueOf", STRING, List.of("java.lang.Object")));
            } else {
              ctx.basicGen.primitiveToString(out, topType);
            }
          }
          if (STRING.equals(leftType)) {
            // (already a String)
          } else if (!LONG.equals(leftType) && !DOUBLE.equals(leftType)) {
            // X + String (single-slot X): SWAP, valueOf(X), SWAP, then concat
            out.write(SWAP);
            if (isNonPrimitive(leftType)) {
              ctx.invokeGen.invokeStatic(
                  out,
                  new Method("java.lang.String", "valueOf", STRING, List.of("java.lang.Object")));
            } else {
              ctx.basicGen.primitiveToString(out, leftType);
            }
            out.write(SWAP);
          } else {
            // long/double + String: a 2-slot value can't be SWAPped, so park the String in a
            // temporary local while converting the left operand
            String tmpName = "$concat_" + ctx.classIdMap.variables.localSize;
            VariableInfo tmp =
                new VariableInfo(
                    tmpName,
                    STRING,
                    emptyList(),
                    false,
                    (short) ctx.classIdMap.variables.localSize,
                    null);
            ctx.classIdMap.variables.putLocal(Token.id(tmpName), tmp);
            ctx.storeGen.store(out, tmp);
            ctx.basicGen.primitiveToString(out, leftType);
            ctx.loadGen.load(out, tmp);
          }
          ctx.invokeGen.invokeVirtual(
              out, new Method("java.lang.String", "concat", STRING, List.of(STRING)));
          exprTypeStack.pop2();
          exprTypeStack.push(STRING);
          type = STRING;
        } else if (op.equals(NEGATE) && reverseComp) {
          if (branchEmitted && lastBranchPos >= 0) {
            // A comparison already emitted a branch opcode. Flip it in place instead of
            // emitting another branch (which would corrupt the instruction stream).
            byte flipped = reverseComp(lastBranchOpCode);
            out.patch(lastBranchPos, new byte[] {flipped});
            lastBranchOpCode = flipped;
          } else {
            boolean logicalOpFollows = false;
            for (int j = i + 1; j < tokens.size(); j++) {
              if (tokens.get(j) instanceof Operator) {
                Operators jType = ((Operator) tokens.get(j)).opValue();
                if (jType == LOGICAL_AND || jType == LOGICAL_OR) {
                  logicalOpFollows = true;
                  break;
                }
              }
            }
            if (!logicalOpFollows) {
              // Jump (skip body) when the value is non-zero, i.e. when !value is false.
              out.write(IFNE);
              ctx.opStack.pop();
              ctx.opStack.push(BOOLEAN);
              branchEmitted = true;
            } else {
              // Logical op follows: emit boolean NOT as value (ICONST_1 + IXOR) without a branch.
              out.write(ICONST_1);
              out.write(IXOR);
            }
          }
        } else {
          byte[] opCode = null;
          // In a string concatenation context, a numeric sub-expression (e.g. "i + j" in
          // "i + j + str") is computed in its operands' type, not as a String.
          String typeBefore = type;
          boolean numericInStringContext =
              STRING.equals(type)
                  && TypeUtils.isPrimitive(exprTypeStack.type())
                  && TypeUtils.isPrimitive(exprTypeStack.secondType());
          if (numericInStringContext) {
            type = widerNumericType(exprTypeStack.secondType(), exprTypeStack.type());
          }
          String promoted = promoteOperands(out, op, exprTypeStack);
          if (promoted != null) {
            type = promoted;
          }
          if (COMPARATORS.contains(op) && exprTypeStack.oneOfTopTwoElementsIsZero()) {
            opCode = new byte[] {COMPARE_TO_ZERO_OPS.get(op)};
            type = BOOLEAN;
          } else {
            opCode = ARITHMETIC_OPS.getOrDefault(op, Collections.emptyMap()).get(type);
          }
          // Fall back to reference comparison for non-primitive types not in ARITHMETIC_OPS
          if (opCode == null && COMPARATORS.contains(op)) {
            opCode = ARITHMETIC_OPS.getOrDefault(op, Collections.emptyMap()).get(REF);
          }
          if (!reverseComp && COMPARATORS.contains(op)) {
            // A comparator always produces a boolean result. Only do this for a value-context
            // caller (reverseComp=false) — reverseComp=true callers (If/While/For/ternary
            // conditions) read `type` afterward too, and forcing BOOLEAN there changed codegen
            // decisions for other (self-compiled) sources in ways not yet root-caused.
            type = BOOLEAN;
          }
          if (reverseComp && opCode != null) {
            // creating a copy is important, otherwise we would modify the original array
            opCode = Arrays.copyOf(opCode, opCode.length);
            for (int j = 0; j < opCode.length; j++) {
              opCode[j] = reverseComp(opCode[j]);
            }
          }
          // there are no opcodes for arithmetic operations specific to byte and short
          // so it's important to indicate that the type changes to int to ensure that,
          // if necessary, we cast it back to byte and short later
          if (type.equals(BYTE) || type.equals(SHORT)) {
            type = INT;
          }
          if (opCode != null) {
            ctx.opStack.pop2();
            // A reverseComp comparator emits a bare branch opcode (its 2-byte offset is patched
            // in later by the caller — If/While/For/ternary) that consumes both operands and
            // leaves no value on the real JVM stack; only push a tracked value for the other
            // cases (arithmetic ops, and value-context comparators materialized via evalAsValue).
            if (!(reverseComp && COMPARATORS.contains(op))) {
              ctx.opStack.push(type);
            }
            exprTypeStack.pop2();
            exprTypeStack.push(type);
            if (COMPARATORS.contains(op)) {
              lastBranchPos = out.size();
              lastBranchOpCode = opCode[0];
              branchEmitted = true;
            }
            out.write(opCode);
          }
          if (numericInStringContext) {
            type = typeBefore;
          }
        }
      }
    }
    if (expr.cast != null) {
      ctx.basicGen.checkCastable(null, ctx.opStack.type(), expr.cast.name);
      String currType = ctx.opStack.pop();
      ctx.basicGen.convert(out, currType, expr.cast.name);
      type = expr.cast.name;
      ctx.opStack.push(type);
    }
    // If this expression is used as a branch condition (reverseComp=true) but no branch opcode
    // was generated (e.g. a plain boolean variable or method call with no comparator operator),
    // emit IFEQ so IfGenerator can append the 2-byte branch offset correctly.
    if (reverseComp && !branchEmitted && BOOLEAN.equals(type)) {
      out.write(IFEQ);
    }
    return new ExpressionResult(type, val);
  }

  private String handleId(
      DynamicByteArray out,
      GlobalIdentifierMap classIdMap,
      List<Token> tokens,
      int i,
      Token token,
      String type) {
    Identifier curr = (Identifier) token;
    Identifier prev = null;
    String returnType = type;
    boolean prevLoadedRef = false;
    do {
      if ("null".equals(curr.val)) {
        out.write(ACONST_NULL);
        ctx.opStack.push(REF);
        return REF;
      }
      if (curr instanceof CastMethodCall) {
        CastMethodCall castCall = (CastMethodCall) curr;
        String decided =
            (castCall.castType != null)
                ? new NumExprTypeDecider(ctx).getType(classIdMap, castCall.innerExpr)
                : null;
        boolean lambda =
            castCall.innerExpr.tokens.size() == 1
                && castCall.innerExpr.tokens.get(0) instanceof LambdaExpr;
        // (a lambda's target type is the cast type, e.g. in "((Runnable) () -> {}).run()")
        ExpressionResult innerResult =
            eval(out, lambda ? castCall.castType : null, castCall.innerExpr);
        if (castCall.castType != null) {
          ctx.basicGen.checkCastable(decided, innerResult.type, castCall.castType);
          ctx.basicGen.convert(out, innerResult.type, castCall.castType);
          returnType = castCall.castType;
        } else {
          returnType = innerResult.type; // "(expr).method()": no cast
        }
        prevLoadedRef = true;
        if (curr.arrSel != null && returnType != null && returnType.startsWith("[")) {
          // an element, e.g. "((int[]) o)[2]"
          ctx.loadGen.loadValueFromArrayOnStack(out, classIdMap, curr.arrSel.exprs, returnType);
          returnType =
              TypeUtils.jvmDescriptorToTypeName(returnType.substring(curr.arrSel.exprs.size()));
          prevLoadedRef = isNonPrimitive(returnType);
        }
        ctx.methodCallGen.lastGenericType = null;
      } else if (curr instanceof ConstructorCall) {
        ConstructorCall constructorCall = (ConstructorCall) curr;
        returnType = generateConstructorCall(out, classIdMap, constructorCall);
        ctx.methodCallGen.lastGenericType = null;
        prevLoadedRef = true; // e.g. "new Foo().field"
      } else if (curr instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) curr;
        if (prev instanceof MethodCall
            || prev instanceof ConstructorCall
            || prev instanceof CastMethodCall) {
          returnType = ctx.methodCallGen.generateChained(out, classIdMap, methodCall, returnType);
        } else if (prev != null && prevLoadedRef) {
          // Call on a reference loaded by the previous selector, e.g. "arr[i].length()" or
          // "make().field.method()"
          ctx.methodCallGen.lastGenericType = null;
          returnType = ctx.methodCallGen.generateChained(out, classIdMap, methodCall, returnType);
        } else if (prev == null && methodCall.literalReceiver) {
          // String literal method chain: "literal".method(arg) — receiver already on stack
          returnType =
              ctx.methodCallGen.generateChained(out, classIdMap, methodCall, "java.lang.String");
        } else {
          returnType = ctx.methodCallGen.generate(out, classIdMap, methodCall);
        }
        if (curr.arrSel != null) {
          ctx.loadGen.loadValueFromArrayOnStack(out, classIdMap, curr.arrSel.exprs, returnType);
          if (!returnType.isEmpty()) {
            // remove leading '[' from type (a reflected array type is a JVM descriptor, e.g.
            // "[Ljava.lang.reflect.Type;")
            returnType =
                TypeUtils.jvmDescriptorToTypeName(
                    returnType.substring(methodCall.arrSel.exprs.size()));
          }
        }
        // A trailing non-call selector (e.g. make().val) is an instance field access on the
        // just-returned reference, not a local variable lookup.
        prevLoadedRef = isNonPrimitive(returnType) && curr.arrSel == null;
      } else {
        VariableInfo prevInfo = (prev != null) ? ctx.classIdMap.variables.get(prev) : null;
        boolean prevWasArray =
            (prevInfo != null && prevInfo.arrType != null)
                || (returnType != null && returnType.startsWith("["));
        if (prev != null && prevWasArray) {
          ctx.basicGen.arrayLength(out);
          returnType = INT;
          prevLoadedRef = false;
        } else if (prev == null && enclosingThis(curr) != null) {
          // "Outer.this": an enclosing instance
          Clazz outer = enclosingThis(curr);
          List<Integer> path = EnclosingInstances.path(ctx, ctx.clazz, outer);
          if (path == null) {
            ctx.errors.addError(
                LOGGER_NAME,
                "not an enclosing class: " + curr.val,
                new RuntimeException().getStackTrace());
            path = new ArrayList<>();
          }
          ctx.loadGen.loadReference(out, (short) 0);
          for (int field : path) {
            ctx.loadGen.getField(out, (short) field);
          }
          ctx.opStack.push(REF);
          returnType = outer.fqn();
          prevLoadedRef = true;
          curr = curr.next; // (the "this")
        } else if ("this".equals(curr.val.toString()) || "super".equals(curr.val.toString())) {
          ctx.loadGen.loadReference(out, (short) 0);
          ctx.opStack.push(REF);
          // the real class (not REF): a following field is looked up in it
          returnType =
              "super".equals(curr.val.toString())
                  ? (ctx.clazz != null
                          && ctx.clazz.baseClass != null
                          && !"java.lang.Object".equals(ctx.clazz.baseClass)
                      ? ctx.clazz.baseClass
                      : "java.lang.Object")
                  : (ctx.clazz != null ? ctx.clazz.fqn() : REF);
          prevLoadedRef = true;
        } else {
          VariableInfo varInfo = ctx.classIdMap.variables.get(curr);
          // When a ref is on the stack, the selector is a field of that object, even if a local
          // variable or a field of the current class has the same name (e.g. "ctx.errors" in a
          // class with its own "errors" field).
          if (prevLoadedRef) {
            java.util.Map<String, FieldProcessor.InstanceField> fields =
                new FieldProcessor().getInstanceFieldMap(returnType, ctx.allClazzes);
            FieldProcessor.InstanceField instanceField = fields.get(curr.val.toString());
            if (instanceField != null) {
              String className = instanceField.className.replace('.', '/');
              String fieldType = instanceField.fieldType;
              String fieldTypeDescr = TypeUtils.descr(fieldType);
              ctx.constPool.addClass(className);
              ctx.constPool.addFieldRef(className, instanceField.fieldName, fieldTypeDescr);
              ctx.opStack.pop();
              ctx.loadGen.getField(
                  out,
                  ctx.constPool.indexOf(
                      io.github.martinschneider.orzo.codegen.constants.ConstantTypes
                          .CONSTANT_FIELDREF,
                      className,
                      instanceField.fieldName,
                      fieldTypeDescr));
              ctx.opStack.push(fieldType);
              returnType = fieldType;
              if (curr.arrSel != null && fieldType.startsWith("[")) {
                String elemDescriptor = fieldType.substring(1);
                ctx.loadGen.loadValueFromArrayOnStack(
                    out, classIdMap, curr.arrSel.exprs, elemDescriptor);
                ctx.opStack.pop();
                String elemType = TypeUtils.jvmDescriptorToTypeName(elemDescriptor);
                ctx.opStack.push(elemType);
                returnType = elemType;
                prevLoadedRef = isNonPrimitive(elemType);
              } else {
                prevLoadedRef = isNonPrimitive(fieldType) && curr.arrSel == null;
              }
              prev = curr;
              curr = curr.next;
              continue;
            }
            if (varInfo == null) {
              return handleStaticInstanceChain(out, curr, type);
            }
            // varInfo != null but no instance field found: load the local variable
          }
          if (varInfo == null) {
            // Try to resolve mixed static/instance field access chain
            return handleStaticInstanceChain(out, curr, type);
          }
          String varType = varInfo.type;
          String arrType = varInfo.arrType;
          if (prevLoadedRef
              && varInfo.isField
              && !varInfo.accFlags.contains(AccessFlag.ACC_STATIC)) {
            // Reference already on stack from previous iteration: use GETFIELD directly
            ctx.opStack.pop();
            ctx.loadGen.getField(out, varInfo.idx);
            ctx.opStack.push(varType);
            returnType = varType;
            prevLoadedRef = isNonPrimitive(varType);
          } else {
            // look ahead for ++ or -- operators because in that case we do not push the
            // value to the stack (the increment does, of the variable's type)
            boolean incremented =
                i + 1 < tokens.size()
                    && (tokens.get(i + 1).eq(op(POST_DECREMENT))
                        || tokens.get(i + 1).eq(op(POST_INCREMENT))
                        || tokens.get(i + 1).eq(op(PRE_INCREMENT))
                        || tokens.get(i + 1).eq(op(PRE_DECREMENT)));
            if (!incremented) {
              if (curr.arrSel != null) {
                // array
                ctx.loadGen.loadValueFromArray(out, classIdMap, curr.arrSel.exprs, varInfo);
                returnType = varInfo.arrType;
              } else {
                ctx.loadGen.load(out, varInfo);
                if (curr.next != null) {
                  returnType = varType;
                }
              }
            }
            // Defer narrowing conversion from long to a smaller type when more tokens
            // follow: operators should work on the long, not on a prematurely truncated int.
            // E.g. (byte)((val >> 56) & 255) must use LSHR/LAND, not L2I+ISHR.
            boolean deferNarrowing =
                LONG.equals(varType)
                    && !LONG.equals(type)
                    && !DOUBLE.equals(type)
                    && !FLOAT.equals(type)
                    && (i + 1 < tokens.size());
            if (incremented) {
              returnType = varType;
            } else if (!type.equals(varType)
                && arrType == null
                && !deferNarrowing
                && !(isNonPrimitive(varType) && isNonPrimitive(type))
                && !(isNonPrimitive(varType) && curr.next != null)
                // (an operand isn't converted to boolean, e.g. "h1" in "boolean same = h1 == h2;",
                // except
                // for unboxing a java.lang.Boolean)
                && !(BOOLEAN.equals(type)
                    && !BOOLEAN.equals(varType)
                    && !"java.lang.Boolean".equals(varType))) {
              // Defer primitive→String conversion to the PLUS operator so that arithmetic
              // sub-expressions (e.g. n+1 in "sum="+(n+1)) are computed before converting.
              if (STRING.equals(type) && TypeUtils.isPrimitive(varType)) {
                returnType = varType;
              } else {
                ctx.basicGen.convert1(out, varType, type);
              }
            } else if (STRING.equals(type)
                && isNonPrimitive(varType)
                && !STRING.equals(varType)
                && curr.next == null
                && curr.arrSel == null) {
              // Non-String reference in string concat context: convert via String.valueOf(Object)
              ctx.invokeGen.invokeStatic(
                  out,
                  new Method("java.lang.String", "valueOf", STRING, List.of("java.lang.Object")));
              returnType = STRING;
            }
            if (deferNarrowing) {
              returnType = varType;
            }
            prevLoadedRef =
                (curr.arrSel == null)
                    ? isNonPrimitive(varType)
                    : (arrType != null && isNonPrimitive(arrType));
          }
        }
      }
      prev = curr;
      curr = curr.next;
    } while (curr != null);
    // A call result used in a wider numeric context (e.g. "longVar != list.size()"): widen it
    // like a variable load is widened above.
    if (prev instanceof MethodCall && isWideningPrimitive(returnType, type)) {
      ctx.basicGen.convert1(out, returnType, type);
      returnType = type;
    }
    return returnType;
  }

  private static final List<String> NUMERIC = List.of(BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE);

  private static boolean isNumeric(String type) {
    return type != null && NUMERIC.contains(type);
  }

  // Converts the value below the top of the stack (from -> to): the top value (of type topType)
  // is parked in a temporary local meanwhile
  void convertBelowTop(DynamicByteArray out, String topType, String from, String to) {
    String tmpName = "$tmp_" + ctx.classIdMap.variables.localSize;
    VariableInfo tmp =
        new VariableInfo(
            tmpName, topType, emptyList(), false, (short) ctx.classIdMap.variables.localSize, null);
    ctx.classIdMap.variables.putLocal(Token.id(tmpName), tmp);
    ctx.storeGen.store(out, tmp);
    ctx.basicGen.convert1(out, from, to);
    ctx.loadGen.load(out, tmp);
  }

  // "base ** exponent" (both on the stack): Math.pow() for a floating-point operand (or context),
  // otherwise BigInteger.valueOf(base).pow(exponent).longValue()
  private String generatePow(
      DynamicByteArray out, OperandStack exprTypeStack, boolean doubleContext) {
    String exponent = widerNumericType(exprTypeStack.type(), INT);
    String base = widerNumericType(exprTypeStack.secondType(), INT);
    boolean floating =
        doubleContext
            || List.of(FLOAT, DOUBLE).contains(base)
            || List.of(FLOAT, DOUBLE).contains(exponent);
    String exponentType = floating ? DOUBLE : INT;
    String baseType = floating ? DOUBLE : LONG;
    ctx.basicGen.convert1(out, exponent, exponentType);
    // the base is below the exponent: park the exponent in a temporary local
    String tmpName = "$pow_" + ctx.classIdMap.variables.localSize;
    VariableInfo tmp =
        new VariableInfo(
            tmpName,
            exponentType,
            emptyList(),
            false,
            (short) ctx.classIdMap.variables.localSize,
            null);
    ctx.classIdMap.variables.putLocal(Token.id(tmpName), tmp);
    ctx.storeGen.store(out, tmp);
    ctx.basicGen.convert1(out, base, baseType);
    if (floating) {
      ctx.loadGen.load(out, tmp);
      ctx.constPool.addClass("java/lang/Math");
      ctx.invokeGen.invokeStatic(
          out, new Method("java.lang.Math", "pow", DOUBLE, List.of(DOUBLE, DOUBLE)));
    } else {
      ctx.constPool.addClass("java/math/BigInteger");
      ctx.constPool.addMethodRef("java/math/BigInteger", "valueOf", "(J)Ljava/math/BigInteger;");
      ctx.constPool.addMethodRef("java/math/BigInteger", "pow", "(I)Ljava/math/BigInteger;");
      ctx.constPool.addMethodRef("java/math/BigInteger", "longValue", "()J");
      ctx.invokeGen.invokeStatic(
          out,
          new Method("java.math.BigInteger", "valueOf", "Ljava/math/BigInteger;", List.of(LONG)));
      ctx.loadGen.load(out, tmp);
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/math/BigInteger", "pow", "Ljava/math/BigInteger;", List.of(INT)));
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/math/BigInteger", "longValue", LONG, emptyList()));
    }
    exprTypeStack.pop2();
    exprTypeStack.push(baseType);
    return baseType;
  }

  // Binary numeric promotion (JLS 5.6) of the two operands of a binary operator when their types
  // differ, e.g. the int in "x - aLong" becomes a long (for a shift only the left operand is
  // promoted, the right one becomes an int). Returns the operation's type (the operands' type,
  // whatever the context's type, e.g. int for "boolean b = x - 1 == y;"), or null if the operands
  // aren't both numeric.
  private String promoteOperands(DynamicByteArray out, Operators op, OperandStack exprTypeStack) {
    String right = exprTypeStack.type();
    String left = exprTypeStack.secondType();
    // unboxing (JLS 5.6), e.g. "list.get(i) > x" on a List<Integer> (but "==" and "!=" compare two
    // wrapper objects as references)
    String leftUnboxed = TypeUtils.unboxedType(left);
    String rightUnboxed = TypeUtils.unboxedType(right);
    boolean bothNumeric =
        (isNumeric(left) || isNumeric(leftUnboxed))
            && (isNumeric(right) || isNumeric(rightUnboxed));
    boolean referenceEquality =
        (op == EQUAL || op == NOTEQUAL) && leftUnboxed != null && rightUnboxed != null;
    if (bothNumeric
        && (leftUnboxed != null || rightUnboxed != null)
        && !referenceEquality
        && (ARITHMETIC_OPS.containsKey(op) || COMPARATORS.contains(op))) {
      if (rightUnboxed != null) {
        ctx.basicGen.convert1(out, right, rightUnboxed);
        right = rightUnboxed;
      }
      if (leftUnboxed != null) {
        convertBelowTop(out, right, left, leftUnboxed);
        left = leftUnboxed;
      }
      exprTypeStack.pop2();
      exprTypeStack.push(left);
      exprTypeStack.push(right);
    }
    if (!isNumeric(left)
        || !isNumeric(right)
        || !(ARITHMETIC_OPS.containsKey(op) || COMPARATORS.contains(op))) {
      return null;
    }
    boolean shift = op == LSHIFT || op == RSHIFT || op == RSHIFTU;
    String target = shift ? widerNumericType(left, INT) : widerNumericType(left, right);
    String rightTarget = shift ? INT : target;
    String leftType = widerNumericType(left, INT); // (byte, short and char are ints)
    String rightType = widerNumericType(right, INT);
    if (leftType.equals(target) && rightType.equals(rightTarget)) {
      return target;
    }
    if (!rightType.equals(rightTarget)) {
      ctx.basicGen.convert1(out, rightType, rightTarget);
    }
    if (!leftType.equals(target)) {
      convertBelowTop(out, rightTarget, leftType, target);
    }
    exprTypeStack.pop2();
    exprTypeStack.push(target);
    exprTypeStack.push(rightTarget);
    return target;
  }

  // The type binary numeric promotion gives two operands, e.g. long for (int, long)
  private static String widerNumericType(String a, String b) {
    List<String> order = List.of(INT, LONG, FLOAT, DOUBLE);
    int ia = Math.max(0, order.indexOf(a));
    int ib = Math.max(0, order.indexOf(b));
    return order.get(Math.max(ia, ib));
  }

  private static boolean isWideningPrimitive(String from, String to) {
    List<String> order = List.of(BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE);
    int fromIdx = order.indexOf(from);
    int toIdx = order.indexOf(to);
    return fromIdx >= 0 && toIdx > fromIdx && toIdx >= order.indexOf(LONG);
  }

  // Whether the selectors after the "class" of a class literal are more than calls, e.g. an array
  // element or "length"
  private static boolean selectsBeyondCalls(Identifier curr, int classIdx) {
    Identifier link = curr;
    for (int i = 0; i <= classIdx; i++) {
      link = link.next;
    }
    for (; link != null; link = link.next) {
      if (!(link instanceof MethodCall) || link.arrSel != null) {
        return true;
      }
    }
    return false;
  }

  private String handleStaticInstanceChain(DynamicByteArray out, Identifier curr, String type) {
    // Try to find the longest prefix that resolves to a static field
    List<String> identifierParts = new ArrayList<>();
    Identifier tempCurr = curr;
    while (tempCurr != null) {
      identifierParts.add(tempCurr.val.toString()); // Use val instead of toString()
      tempCurr = tempCurr.next;
    }

    // Try progressively shorter prefixes as static fields
    for (int splitPoint = identifierParts.size() - 1; splitPoint > 0; splitPoint--) {
      // Build the static field name from the prefix
      StringBuilder staticFieldName = new StringBuilder();
      for (int i = 0; i < splitPoint; i++) {
        if (staticFieldName.length() > 0) {
          staticFieldName.append(".");
        }
        staticFieldName.append(identifierParts.get(i));
      }

      FieldProcessor.StaticField staticField = ctx.staticFieldMap.get(staticFieldName.toString());
      if (staticField != null) {
        // Found a static field! Generate GETSTATIC for the prefix
        String className = staticField.className.replace('.', '/');
        String fieldType = TypeUtils.descr(staticField.fieldType);
        ctx.constPool.addClass(className);
        ctx.constPool.addFieldRef(className, staticField.fieldName, fieldType);

        // Generate GETSTATIC bytecode
        out.write(io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC);
        out.write(
            ctx.constPool.indexOf(
                io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF,
                className,
                staticField.fieldName,
                fieldType));
        ctx.opStack.push(staticField.fieldType);

        // Now handle the remaining part as instance field access
        String currentType = staticField.fieldType;
        for (int i = splitPoint; i < identifierParts.size(); i++) {
          String instanceFieldName = identifierParts.get(i);

          // Get instance fields for the current type
          FieldProcessor fieldProcessor = new FieldProcessor();
          Map<String, FieldProcessor.InstanceField> instanceFields =
              fieldProcessor.getInstanceFieldMap(currentType, ctx.allClazzes);

          FieldProcessor.InstanceField instanceField = instanceFields.get(instanceFieldName);
          if (instanceField == null) {
            ctx.errors.addError(
                LOGGER_NAME,
                String.format(
                    "Unknown instance field: %s in type %s", instanceFieldName, currentType),
                new RuntimeException().getStackTrace());
            return type;
          }

          // Generate GETFIELD for instance field access
          String instClassName = instanceField.className.replace('.', '/');
          String instFieldType = TypeUtils.descr(instanceField.fieldType);
          ctx.constPool.addClass(instClassName);
          ctx.constPool.addFieldRef(instClassName, instanceField.fieldName, instFieldType);

          out.write(io.github.martinschneider.orzo.codegen.OpCodes.GETFIELD);
          out.write(
              ctx.constPool.indexOf(
                  io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF,
                  instClassName,
                  instanceField.fieldName,
                  instFieldType));
          ctx.opStack.pop(); // Pop object reference
          ctx.opStack.push(instanceField.fieldType); // Push field value

          currentType = instanceField.fieldType;
        }

        return currentType;
      }
    }

    // No static field found, try simple static field resolution
    String fieldName = curr.toString();
    FieldProcessor.StaticField staticField = ctx.staticFieldMap.get(fieldName);
    if (staticField != null) {
      // Generate GETSTATIC instruction for static field access
      String className = staticField.className.replace('.', '/');
      String fieldType = TypeUtils.descr(staticField.fieldType);
      ctx.constPool.addClass(className);
      ctx.constPool.addFieldRef(className, staticField.fieldName, fieldType);

      // Generate GETSTATIC bytecode
      out.write(io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC);
      out.write(
          ctx.constPool.indexOf(
              io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF,
              className,
              staticField.fieldName,
              fieldType));
      ctx.opStack.push(staticField.fieldType);
      if (type != null
          && !staticField.fieldType.equals(type)
          && TypeUtils.isPrimitive(staticField.fieldType)
          && TypeUtils.isPrimitive(type)) {
        ctx.basicGen.convert1(out, staticField.fieldType, type);
        return type;
      }
      return staticField.fieldType;
    }

    // Class literal, e.g. "String.class" or "java.util.List.class", possibly followed by calls
    // ("Foo.class.getName()")
    int classIdx = identifierParts.indexOf("class");
    if (classIdx > 0 && selectsBeyondCalls(curr, classIdx)) {
      // e.g. "Foo.class.getMethods()[0].getName()" or "Foo.class.getMethods().length": the class
      // literal as a receiver, like "(Foo.class).getMethods()[0].getName()"
      Identifier literal = new Identifier(curr.val.toString());
      Identifier last = literal;
      Identifier link = curr.next;
      for (int i = 1; i <= classIdx; i++, link = link.next) {
        last.next = new Identifier(link.val.toString());
        last = last.next;
      }
      CastMethodCall receiver =
          new CastMethodCall(null, new Expression(new ArrayList<>(List.of(literal))));
      receiver.next = link;
      return eval(out, type, new Expression(new ArrayList<>(List.of(receiver)))).type;
    }
    if (classIdx > 0) {
      String className = String.join(".", identifierParts.subList(0, classIdx));
      String wrapper = PRIMITIVE_WRAPPERS.get(className);
      if (className.endsWith("[]")) {
        // an array class, e.g. "int[].class": its class name is its descriptor, e.g. "[I"
        int dims = 0;
        String element = className;
        while (element.endsWith("[]")) {
          element = element.substring(0, element.length() - 2);
          dims++;
        }
        if (!TypeUtils.isPrimitive(element) && !element.contains(".")) {
          element = resolveJvmClassName(element).replace('/', '.');
        }
        String arrayClass = TypeUtils.descr(element, dims);
        ctx.constPool.addClass(arrayClass);
        ctx.loadGen.ldc(out, CONSTANT_CLASS, arrayClass);
      } else if (wrapper != null) {
        // a primitive type's Class object, e.g. int.class: Integer.TYPE
        ctx.constPool.addClass(wrapper);
        ctx.constPool.addFieldRef(wrapper, "TYPE", "Ljava/lang/Class;");
        ctx.invokeGen.getStatic(out, wrapper, "TYPE", "Ljava/lang/Class;");
      } else {
        Class<?> nested = (classIdx == 1) ? null : TypeUtils.loadClass(className);
        String jvmClass =
            (classIdx == 1)
                ? resolveJvmClassName(className)
                : ((nested != null) ? nested.getName() : className).replace('.', '/');
        ctx.constPool.addClass(jvmClass);
        ctx.loadGen.ldc(out, CONSTANT_CLASS, jvmClass);
      }
      String returnType = "java.lang.Class";
      Identifier link = curr;
      for (int i = 0; i <= classIdx; i++) {
        link = link.next;
      }
      for (; link instanceof MethodCall; link = link.next) {
        returnType =
            ctx.methodCallGen.generateChained(out, ctx.classIdMap, (MethodCall) link, returnType);
      }
      return returnType;
    }
    // A static field of a (possibly fully-qualified) class, optionally followed by instance
    // fields, e.g. "java.lang.Integer.MAX_VALUE" or "AccessFlag.ACC_PUBLIC.val"
    for (int split = identifierParts.size() - 1; split >= 1; split--) {
      if (split == identifierParts.size() - 1 && split == 1) {
        continue; // "Class.FIELD" is handled below
      }
      String first = resolveJvmClassName(identifierParts.get(0)).replace('/', '.');
      String rest = String.join(".", identifierParts.subList(1, split));
      // (e.g. "java.util.Map.Entry" or "Thread.State": a nested class with dots)
      Class<?> fieldOwner = TypeUtils.loadClass(rest.isEmpty() ? first : first + "." + rest);
      if (fieldOwner == null && split > 1) {
        fieldOwner = TypeUtils.loadClass(String.join(".", identifierParts.subList(0, split)));
      }
      java.lang.reflect.Field reflField;
      try {
        if (fieldOwner == null) {
          continue; // try a shorter class name
        }
        reflField = fieldOwner.getField(identifierParts.get(split));
      } catch (NoSuchFieldException | LinkageError e) {
        continue; // try a shorter class name
      }
      String fqnClass = fieldOwner.getName();
      if (!java.lang.reflect.Modifier.isStatic(reflField.getModifiers())) {
        break;
      }
      String jvmClass = fqnClass.replace('.', '/');
      String fieldType = reflField.getType().getName();
      String descr = TypeUtils.descr(fieldType);
      ctx.constPool.addClass(jvmClass);
      ctx.constPool.addFieldRef(jvmClass, reflField.getName(), descr);
      out.write(io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC);
      out.write(
          ctx.constPool.indexOf(
              io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF,
              jvmClass,
              reflField.getName(),
              descr));
      ctx.opStack.push(fieldType);
      String currentType = fieldType;
      for (int i = split + 1; i < identifierParts.size(); i++) {
        FieldProcessor.InstanceField instanceField =
            new FieldProcessor()
                .getInstanceFieldMap(currentType, ctx.allClazzes)
                .get(identifierParts.get(i));
        if (instanceField == null) {
          ctx.errors.addError(
              LOGGER_NAME,
              String.format(
                  "Unknown instance field: %s in type %s", identifierParts.get(i), currentType),
              new RuntimeException().getStackTrace());
          return type;
        }
        String owner = instanceField.className.replace('.', '/');
        String instDescr = TypeUtils.descr(instanceField.fieldType);
        ctx.constPool.addClass(owner);
        ctx.constPool.addFieldRef(owner, instanceField.fieldName, instDescr);
        out.write(io.github.martinschneider.orzo.codegen.OpCodes.GETFIELD);
        out.write(
            ctx.constPool.indexOf(
                io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF,
                owner,
                instanceField.fieldName,
                instDescr));
        ctx.opStack.pop();
        ctx.opStack.push(instanceField.fieldType);
        currentType = instanceField.fieldType;
      }
      return "java.lang.String".equals(currentType) ? STRING : currentType;
    }
    // Try to resolve as ImportedClass.fieldName (e.g. Operators.LESS, Byte.MAX_VALUE)
    if (identifierParts.size() == 2) {
      String partClass = identifierParts.get(0);
      String partField = identifierParts.get(1);
      String jvmClass = resolveJvmClassName(partClass);
      String fqnClass = jvmClass.replace('/', '.');
      try {
        java.lang.reflect.Field reflField = Class.forName(fqnClass).getField(partField);
        String reflFieldType = reflField.getType().getName();
        String reflDescr = TypeUtils.descr(reflFieldType);
        ctx.constPool.addClass(jvmClass);
        ctx.constPool.addFieldRef(jvmClass, partField, reflDescr);
        out.write(io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC);
        out.write(
            ctx.constPool.indexOf(
                io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF,
                jvmClass,
                partField,
                reflDescr));
        ctx.opStack.push(reflFieldType);
        if (type != null
            && !reflFieldType.equals(type)
            && TypeUtils.isPrimitive(reflFieldType)
            && TypeUtils.isPrimitive(type)) {
          ctx.basicGen.convert1(out, reflFieldType, type);
          return type;
        }
        return reflFieldType;
      } catch (ClassNotFoundException | NoSuchFieldException e) {
        // fall through
      }
    }
    if ("null".equals(curr.val.toString())) {
      out.write(ACONST_NULL);
      ctx.opStack.push(REF);
      return REF;
    }
    // Try as a class-prefixed static method call: e.g. ParserContext.build(errors)
    if (curr.next instanceof MethodCall) {
      MethodCall mc = (MethodCall) curr.next;
      String dottedName = curr.val.toString() + "." + mc.name;
      String returnType =
          ctx.methodCallGen.generate(out, ctx.classIdMap, new MethodCall(dottedName, mc.params));
      // the rest of the chain, e.g. ".length()" in "String.format(...).length()"
      for (Identifier link = mc.next; link instanceof MethodCall; link = link.next) {
        returnType =
            ctx.methodCallGen.generateChained(out, ctx.classIdMap, (MethodCall) link, returnType);
      }
      return returnType;
    }
    ctx.errors.addError(
        LOGGER_NAME,
        String.format("cannot find variable %s", curr),
        new RuntimeException().getStackTrace());
    return type;
  }

  public HasOutput generateArray(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, ArrayInit arrInit) {
    String type = arrInit.type;
    byte storeOpCode = getStoreOpCode(type);
    createArray(out, classIdMap, type, arrInit.dims);
    // (multi-dimensional arrays are reported by createArray())
    if (arrInit.vals.size() == 1) {
      for (int i = 0; i < arrInit.vals.get(0).size(); i++) {
        out.write(DUP);
        ctx.pushGen.push(out, INT, i);
        ctx.exprGen.eval(out, type, arrInit.vals.get(0).get(i));
        String valueType = ctx.opStack.type();
        if (TypeUtils.isPrimitive(type) && valueType != null && !TypeUtils.isPrimitive(valueType)) {
          // unboxing, e.g. "new byte[] {map.get(key)}" on a Map<K, Byte>
          ctx.basicGen.convert1(out, valueType, type);
        }
        out.write(storeOpCode);
        // the store consumes the (untracked) DUP'ed array reference, the index and the value
        ctx.opStack.pop();
        ctx.opStack.pop();
      }
    }
    // ctx.assignGen.assignArray(out, variables, type, decl.arrDim, decl.name);
    return out;
  }

  private void createArray(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, String type, List<Expression> dims) {
    if (dims.size() == 1) {
      ctx.exprGen.eval(out, INT, dims.get(0));
      if (TypeUtils.isPrimitive(type)) {
        out.write(NEWARRAY);
        out.write(getArrayType(type));
      } else {
        String descr = TypeUtils.descr(type);
        String jvmClass =
            (descr.startsWith("L") && descr.endsWith(";"))
                ? descr.substring(1, descr.length() - 1)
                : descr;
        ctx.constPool.addClass(jvmClass);
        out.write(ANEWARRAY);
        out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, jvmClass));
      }
      // NEWARRAY/ANEWARRAY consumes the dimension int and produces an array reference;
      // opStack still had the dimension's int on top, so swap it for the resulting ref type.
      ctx.opStack.pop();
      ctx.opStack.push(REF);
    } else {
      ctx.errors.addError(
          "array code generator",
          "multi-dimensional arrays are not supported",
          new RuntimeException().getStackTrace());
    }
  }

  // A switch expression: its statement (see SwitchExpr) assigns the result to a temporary variable
  // (of the target type, if there is one), which is initialized first (a switch over an enum
  // without a default case assigns it on all paths, too)
  private String generateSwitch(DynamicByteArray out, String type, SwitchExpr expr) {
    if (type == null || LambdaExpr.PSEUDO_TYPE.equals(type)) {
      type = "java.lang.Object";
    }
    VariableInfo result =
        new VariableInfo(
            expr.var, type, emptyList(), false, (short) ctx.classIdMap.variables.localSize, null);
    ctx.classIdMap.variables.putLocal(Token.id(expr.var), result);
    if (BOOLEAN.equals(type)) {
      ctx.pushGen.pushBool(out, false);
    } else if (TypeUtils.isPrimitive(type)) {
      ctx.pushGen.push(out, type, 0);
    } else {
      out.write(ACONST_NULL);
      ctx.opStack.push(type);
    }
    ctx.storeGen.store(out, result);
    ctx.delegator.generate(out, ctx.method, expr.statement);
    ctx.loadGen.load(out, result);
    return type;
  }

  private String generateTernary(DynamicByteArray out, String type, TernaryExpression ternary) {
    // Generate condition bytes: eval with reverseComp=true appends the conditional jump opcode
    // but NOT its 2-byte offset operand. We append the offset ourselves.
    DynamicByteArray condOut = new DynamicByteArray();
    ctx.exprGen.eval(condOut, null, ternary.condition, false, true);

    // numeric branches of different types are promoted, e.g. "c ? 9 : 9.9" is a double
    String trueType = new NumExprTypeDecider(ctx).getType(ctx.classIdMap, ternary.trueBranch);
    String falseType = new NumExprTypeDecider(ctx).getType(ctx.classIdMap, ternary.falseBranch);
    String promoted =
        (trueType != null
                && !trueType.equals(falseType)
                && ExpressionTypes.isNumeric(trueType)
                && ExpressionTypes.isNumeric(falseType))
            ? ExpressionTypes.promote(trueType, falseType)
            : null;

    // Generate true branch
    DynamicByteArray trueOut = new DynamicByteArray();
    ExpressionResult trueResult =
        ctx.exprGen.eval(trueOut, promoted != null ? promoted : type, ternary.trueBranch);
    String resultType = (trueResult != null && trueResult.type != null) ? trueResult.type : type;
    if (promoted != null && !promoted.equals(resultType)) {
      ctx.basicGen.convert1(trueOut, resultType, promoted);
    }
    resultType = (promoted != null) ? promoted : resultType;

    // Generate false branch
    DynamicByteArray falseOut = new DynamicByteArray();
    ExpressionResult falseResult = ctx.exprGen.eval(falseOut, resultType, ternary.falseBranch);
    if (promoted != null
        && falseResult != null
        && falseResult.type != null
        && !promoted.equals(falseResult.type)) {
      ctx.basicGen.convert1(falseOut, falseResult.type, promoted);
    }

    // condition (ending with a branch opcode), its offset, true branch, GOTO, false branch
    int trueLen = trueOut.size();
    int falseLen = falseOut.size();
    short ifeqOffset = (short) (trueLen + 6);
    short gotoOffset = (short) (falseLen + 3);

    out.write(condOut.getBytes());
    out.write(ByteUtils.shortToByteArray(ifeqOffset));
    out.write(trueOut.getBytes());
    out.write(GOTO);
    out.write(ByteUtils.shortToByteArray(gotoOffset));
    out.write(falseOut.getBytes());

    return resultType;
  }

  // "Owner::method" as a lambda with the given number of parameters: "(a, b) -> Owner.method(a, b)"
  // for a static method, "(a, b) -> a.method(b)" for an instance method of Owner, "(a, b) ->
  // owner.method(a, b)" for a local variable owner (null after an error)
  private LambdaExpr methodRefAsLambda(LambdaExpr ref, int arity) {
    if (ref.methodRefOwner.endsWith("[]") && ref.methodRefName.equals("new") && arity == 1) {
      // an array constructor reference, e.g. "int[]::new": "n -> new int[n]"
      String element = ref.methodRefOwner.substring(0, ref.methodRefOwner.length() - 2);
      if (!TypeUtils.isPrimitive(element)) {
        element = resolveJvmClassName(element).replace('/', '.');
      }
      List<Expression> dims =
          new ArrayList<>(
              List.of(new Expression(new ArrayList<>(List.of(new Identifier("$ref0"))))));
      return new LambdaExpr(
          new ArrayList<>(List.of("$ref0")), new ArrayInit(element, dims, new ArrayList<>()));
    } else if (ref.methodRefName.equals("new")) {
      // a constructor reference, e.g. "ArrayList::new": "(a, b) -> new ArrayList(a, b)"
      List<String> params = new ArrayList<>();
      List<Expression> args = new ArrayList<>();
      for (int i = 0; i < arity; i++) {
        params.add("$ref" + i);
        args.add(new Expression(new ArrayList<>(List.of(new Identifier("$ref" + i)))));
      }
      return new LambdaExpr(
          params,
          new Expression(new ArrayList<>(List.of(new ConstructorCall(ref.methodRefOwner, args)))));
    } else if (ref.methodRefOwner.equals("super")) {
      ctx.errors.addError(
          "lambda expression",
          ref.methodRefOwner + "::" + ref.methodRefName + " is not supported yet",
          new RuntimeException().getStackTrace());
      return null;
    }
    // (on a local variable or "this", whose methods a lambda may call, see usesEnclosingInstance())
    boolean bound =
        ref.methodRefOwner.equals("this")
            || ctx.classIdMap.variables.localMap.containsKey(ref.methodRefOwner);
    // static if the owner declares such a method with this arity: a class of this compilation run
    // (also a private method) or one loaded by reflection
    boolean isStatic = false;
    List<Method> sourceMethods = ctx.methods(ref.methodRefOwner + "." + ref.methodRefName);
    for (Method m : sourceMethods) {
      if (m.args.size() == arity && m.accFlags.contains(AccessFlag.ACC_STATIC)) {
        isStatic = true;
      }
    }
    if (sourceMethods.isEmpty()) {
      try {
        Class<?> owner = Class.forName(resolveJvmClassName(ref.methodRefOwner).replace('/', '.'));
        for (java.lang.reflect.Method m : owner.getMethods()) {
          if (m.getName().equals(ref.methodRefName)
              && m.getParameterCount() == arity
              && java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
            isStatic = true;
          }
        }
      } catch (ClassNotFoundException | LinkageError e) {
        isStatic = true; // (e.g. an unknown class: reported when the call is generated)
      }
    }
    if (!bound && !isStatic && arity == 0) {
      ctx.errors.addError(
          "lambda expression",
          "invalid method reference " + ref.methodRefOwner + "::" + ref.methodRefName,
          new RuntimeException().getStackTrace());
      return null;
    }
    // (the arguments are cast to the parameter types of a static method of this compilation run,
    // if there's only one: the functional interface's parameters may be erased, e.g. Object)
    Method target = null;
    for (Method m : sourceMethods) {
      if (m.args.size() == arity && m.accFlags.contains(AccessFlag.ACC_STATIC) && !bound) {
        target = (target == null) ? m : null;
        if (target == null) {
          break;
        }
      }
    }
    isStatic = isStatic || bound;
    List<String> params = new ArrayList<>();
    List<Expression> args = new ArrayList<>();
    for (int i = 0; i < arity; i++) {
      params.add("$ref" + i);
      if (isStatic || i > 0) {
        String type = (target != null) ? target.args.get(i).type : null;
        args.add(
            (type != null && !TypeUtils.isPrimitive(type) && !type.startsWith("["))
                ? new Expression(
                    new ArrayList<>(List.of(new Identifier("$ref" + i))),
                    new io.github.martinschneider.orzo.lexer.tokens.Type(type))
                : new Expression(new ArrayList<>(List.of(new Identifier("$ref" + i)))));
      }
    }
    String callee = (isStatic ? ref.methodRefOwner : params.get(0)) + "." + ref.methodRefName;
    LambdaExpr lambda =
        new LambdaExpr(
            params, new Expression(new ArrayList<>(List.of(new MethodCall(callee, args)))));
    if (!isStatic) {
      // (the receiver is an instance of the owner, e.g. a Map.Entry for "Map.Entry::getKey")
      lambda.paramTypes = new ArrayList<>(Collections.nCopies(arity, (String) null));
      lambda.paramTypes.set(0, resolveJvmClassName(ref.methodRefOwner).replace('/', '.'));
    }
    return lambda;
  }

  // the interfaces of a lambda's class: its functional interface (and those of an intersection
  // cast, e.g. Serializable)
  private static List<String> interfaces(String functional, List<String> markers) {
    List<String> interfaces = new ArrayList<>(List.of(functional));
    interfaces.addAll(markers);
    return interfaces;
  }

  // A lambda is a synthetic class implementing the target type's single abstract method (see
  // ctx.addSyntheticClazz()), instantiated here. Captured variables are constructor arguments.
  private String generateLambda(DynamicByteArray out, String targetType, LambdaExpr lambda) {
    String ifaceName = null;
    Sam sam = null;
    try {
      Class<?> iface = Class.forName(targetType);
      ifaceName = iface.getName();
      sam = Sam.of(findSingleAbstractMethod(iface));
    } catch (ClassNotFoundException | NullPointerException e) {
      Clazz source =
          (targetType != null)
              ? TypeUtils.sourceClazz(
                  convertToJVMClassName(targetType).replace('/', '.'), ctx.allClazzes)
              : null;
      if (source == null || !source.isInterface) {
        ctx.errors.addError(
            "lambda expression",
            "cannot resolve target functional interface: " + targetType,
            new RuntimeException().getStackTrace());
        return targetType;
      }
      ifaceName = source.fqn();
      sam = Sam.of(source, ctx.allClazzes);
    }
    if (sam == null) {
      ctx.errors.addError(
          "lambda expression",
          targetType + " is not a functional interface (no single abstract method found)",
          new RuntimeException().getStackTrace());
      return targetType;
    }
    if (lambda.methodRefName != null) {
      if (lambda.methodRefReceiver != null) {
        // e.g. "new Foo()::bar": the receiver is evaluated once (into a variable of the lambda)
        String receiver = "$receiver" + ctx.classIdMap.variables.localSize;
        String receiverType =
            new NumExprTypeDecider(ctx).getType(ctx.classIdMap, lambda.methodRefReceiver);
        ExpressionResult result = eval(out, receiverType, lambda.methodRefReceiver);
        VariableInfo var =
            new VariableInfo(
                receiver,
                (result.type != null) ? result.type : receiverType,
                emptyList(),
                false,
                (short) ctx.classIdMap.variables.localSize,
                null);
        ctx.classIdMap.variables.putLocal(Token.id(receiver), var);
        ctx.storeGen.store(out, var);
        LambdaExpr named = LambdaExpr.methodRef(receiver, lambda.methodRefName);
        lambda = named;
      }
      lambda = methodRefAsLambda(lambda, sam.paramTypes.size());
      if (lambda == null) {
        return targetType;
      }
    }
    if (sam.paramTypes.size() != lambda.params.size()) {
      ctx.errors.addError(
          "lambda expression",
          String.format(
              "lambda has %d parameter(s) but %s.%s expects %d",
              lambda.params.size(), targetType, sam.name, sam.paramTypes.size()),
          new RuntimeException().getStackTrace());
      return targetType;
    }

    // (nested in the enclosing class, e.g. "Outer$$Lambda$1")
    String syntheticName = "$Lambda$" + (++ctx.syntheticClassCounter);
    String syntheticFqn = ctx.clazz.fqn() + "$" + syntheticName;
    String outerName = ctx.clazz.fqn();
    outerName = outerName.substring(outerName.lastIndexOf('.') + 1);
    // (one using the enclosing instance, e.g. one of its fields, gets it like an inner class)
    boolean inner = !isStaticContext() && usesEnclosingInstance(lambda);
    // The SAM's parameters are erased (e.g. Object for Function.apply); with the target's generic
    // type known, a parameter gets its real type via a casting local of the same name, e.g.
    // "Argument arg = (Argument) $lambda_arg;"
    List<String> typedParams =
        (lambdaTargetGeneric != null) && sam.reflected != null
            ? GenericTypes.samParameterTypes(lambdaTargetGeneric, sam.reflected)
            : emptyList();
    lambdaTargetGeneric = null;
    // Local variables of the enclosing method used in the lambda body are captured: they become
    // fields of the synthetic class, passed to its constructor.
    List<VariableInfo> captured = new ArrayList<>();
    List<Expression> bodyExprs =
        (lambda.block != null) ? Statements.expressions(lambda.block) : List.of(lambda.body);
    for (Expression expr : bodyExprs) {
      visitBody(expr, lambda.params, captured);
      if (inner) {
        replaceThis(expr);
      }
    }
    List<Argument> args = new ArrayList<>();
    List<Statement> body = new ArrayList<>();
    for (int i = 0; i < lambda.params.size(); i++) {
      String erased = sam.paramTypes.get(i);
      String typed = (i < typedParams.size()) ? typedParams.get(i) : null;
      if (lambda.paramTypes != null
          && lambda.paramTypes.get(i) != null
          && !lambda.paramTypes.get(i).startsWith("[")) {
        // a declared type, e.g. "(String s) -> ..." (rather than the inferred one, which may be a
        // type variable)
        typed = TypeUtils.resolveSimpleType(lambda.paramTypes.get(i), ctx.allClazzes, ctx.clazz);
      }
      String name = lambda.params.get(i);
      // (a generic type is erased, e.g. java.util.Map$Entry for Map.Entry<String, Integer>)
      String generic = (typed != null && typed.indexOf('<') > 0) ? typed : null;
      typed = (generic != null) ? typed.substring(0, typed.indexOf('<')) : typed;
      if (typed != null && !typed.equals(erased)) {
        args.add(new Argument(erased, new Identifier("$lambda_" + name)));
        Expression cast =
            new Expression(
                List.of(new Identifier("$lambda_" + name)),
                new io.github.martinschneider.orzo.lexer.tokens.Type(typed));
        Declaration local = new Declaration(emptyList(), typed, new Identifier(name), cast);
        local.genericType = generic;
        body.add(new ParallelDeclaration(new ArrayList<>(List.of(local))));
      } else {
        args.add(new Argument(erased, new Identifier(name)));
      }
    }
    if (lambda.block != null) {
      body.addAll(lambda.block);
    } else if (sam.returnType.equals(VOID)) {
      body.add(new ExpressionStatement(lambda.body)); // e.g. "x -> list.add(x)" for a Consumer
    } else {
      body.add(new ReturnStatement(lambda.body));
    }
    String returnType = sam.returnType;
    Method samImpl =
        new Method(
            syntheticFqn,
            List.of(AccessFlag.ACC_PUBLIC),
            returnType,
            Token.id(sam.name),
            args,
            body);
    samImpl.exceptions = sam.exceptions; // (e.g. Callable.call() throws Exception)
    List<Argument> ctorArgs = new ArrayList<>();
    List<Statement> ctorBody = new ArrayList<>();
    ctorBody.add(new MethodCall("super", emptyList()));
    List<ParallelDeclaration> fields = new ArrayList<>();
    for (VariableInfo var : captured) {
      boolean isArray = var.arrType != null;
      String type = isArray ? var.arrType : var.type;
      ctorArgs.add(new Argument(isArray ? "[" + type : type, new Identifier(var.name)));
      // (package-private: a nested lambda's class reads it, too)
      Declaration field =
          new Declaration(emptyList(), type, isArray ? 1 : 0, new Identifier(var.name), null);
      field.isField = true;
      field.genericType = var.genericType;
      fields.add(new ParallelDeclaration(new ArrayList<>(List.of(field))));
      Identifier thisField = new Identifier("this");
      thisField.next = new Identifier(var.name);
      ctorBody.add(
          new Assignment(
              new ArrayList<>(List.of(thisField)),
              new ArrayList<>(List.of(new Expression(List.of(new Identifier(var.name)))))));
    }
    Method ctor =
        new Constructor(
            syntheticFqn,
            List.of(AccessFlag.ACC_PUBLIC),
            Token.id(Method.CONSTRUCTOR_NAME),
            ctorArgs,
            ctorBody);
    Clazz syntheticClazz =
        new Clazz(
            ctx.clazz.packageName,
            // Same imports as the enclosing class, so simple names in the lambda body (e.g. "new
            // ArrayList<>()") still resolve - ctx.clazz is still the ENCLOSING class here, since
            // the synthetic class itself hasn't been generated yet.
            ctx.clazz.imports,
            new Scope(Scopes.PUBLIC),
            syntheticName,
            false,
            false,
            interfaces(ifaceName, lambda.markerInterfaces),
            Clazz.JAVA_LANG_OBJECT,
            List.of(ctor, samImpl),
            fields,
            ctx.clazz.sourceFile);
    syntheticClazz.outerName = outerName;
    if (inner) {
      InnerClasses.makeInner(syntheticClazz, ctx.clazz);
    }
    ctx.addSyntheticClazz(syntheticClazz);

    String jvmClassName = syntheticClazz.fqn('/');
    ctx.constPool.addClass(jvmClassName);
    ctx.invokeGen.newInstance(out, jvmClassName);
    out.write(DUP);
    ctx.opStack.push(REF);
    if (inner && !pushEnclosingInstance(out, syntheticClazz)) {
      return targetType;
    }
    for (VariableInfo var : captured) {
      ctx.loadGen.load(out, var);
    }
    ctx.invokeGen.invokeSpecial(out, ctor);
    return targetType;
  }

  // "this" in a lambda body is the enclosing instance: the field this$0 of the lambda's class (not
  // in nested lambdas)
  private static void replaceThis(Expression expr) {
    if (expr == null) {
      return;
    }
    if (expr.tree != null) {
      expr.tree = new Renaming("this", InnerClasses.OUTER_INSTANCE).apply(expr.tree);
    }
    for (Token token : expr.tokens) {
      if (token instanceof TernaryExpression) {
        replaceThis(((TernaryExpression) token).condition);
        replaceThis(((TernaryExpression) token).trueBranch);
        replaceThis(((TernaryExpression) token).falseBranch);
      } else if (token instanceof Identifier) {
        Identifier root = (Identifier) token;
        if (root instanceof CastMethodCall) {
          replaceThis(((CastMethodCall) root).innerExpr);
        } else if (root instanceof MethodCall && ((MethodCall) root).name.startsWith("this.")) {
          MethodCall call = (MethodCall) root;
          call.name = InnerClasses.OUTER_INSTANCE + call.name.substring("this".length());
          call.val = call.name;
        } else if (!(root instanceof MethodCall)
            && !(root instanceof ConstructorCall)
            && "this".equals(root.val.toString())) {
          root.val = InnerClasses.OUTER_INSTANCE;
        }
        for (Identifier link = root; link != null; link = link.next) {
          if (link instanceof MethodCall) {
            ((MethodCall) link).params.forEach(ExpressionGenerator::replaceThis);
          } else if (link instanceof ConstructorCall) {
            ((ConstructorCall) link).args.forEach(ExpressionGenerator::replaceThis);
          }
          if (link.arrSel != null) {
            link.arrSel.exprs.forEach(ExpressionGenerator::replaceThis);
          }
        }
      }
    }
  }

  // Whether a lambda body uses the enclosing instance: "this", an instance field or method
  private boolean usesEnclosingInstance(LambdaExpr lambda) {
    Set<String> locals = new HashSet<>(lambda.params);
    List<Expression> exprs = List.of(lambda.body);
    if (lambda.block != null) {
      locals.addAll(Statements.declaredNames(lambda.block));
      exprs = Statements.expressions(lambda.block);
    }
    for (Expression expr : exprs) {
      if (usesEnclosingInstance(expr, locals)) {
        return true;
      }
    }
    return false;
  }

  private boolean usesEnclosingInstance(Expression expr, Set<String> locals) {
    if (expr == null) {
      return false;
    }
    for (Token token : expr.tokens) {
      if (token instanceof TernaryExpression) {
        TernaryExpression ternary = (TernaryExpression) token;
        if (usesEnclosingInstance(ternary.condition, locals)
            || usesEnclosingInstance(ternary.trueBranch, locals)
            || usesEnclosingInstance(ternary.falseBranch, locals)) {
          return true;
        }
      } else if (token instanceof LambdaExpr) {
        if (usesEnclosingInstance((LambdaExpr) token)) {
          return true;
        }
      } else if (token instanceof Identifier && usesEnclosingInstance((Identifier) token, locals)) {
        return true;
      }
    }
    return false;
  }

  // "this", "super" or an instance field (not a local variable)
  private boolean isInstanceName(String name, Set<String> locals) {
    if (name.equals("this") || name.equals("super")) {
      return true;
    }
    VariableInfo field = ctx.classIdMap.variables.fieldMap.get(name);
    return !locals.contains(name)
        && !ctx.classIdMap.variables.localMap.containsKey(name)
        && field != null
        && !field.accFlags.contains(AccessFlag.ACC_STATIC);
  }

  private boolean usesEnclosingInstance(Identifier root, Set<String> locals) {
    if (root instanceof CastMethodCall) {
      return usesEnclosingInstance(((CastMethodCall) root).innerExpr, locals);
    } else if (root instanceof MethodCall) {
      String name = ((MethodCall) root).name;
      if (name.contains(".")) {
        // e.g. "this.m()" or "field.m()"
        if (isInstanceName(name.substring(0, name.indexOf('.')), locals)) {
          return true;
        }
      } else {
        for (Method method : ctx.methods(name)) {
          if (!method.accFlags.contains(AccessFlag.ACC_STATIC)
              && !(method instanceof Constructor)) {
            return true;
          }
        }
      }
    } else if (!(root instanceof ConstructorCall) && isInstanceName(root.val.toString(), locals)) {
      return true;
    }
    for (Identifier link = root; link != null; link = link.next) {
      List<Expression> nested = new ArrayList<>();
      if (link instanceof MethodCall) {
        nested.addAll(((MethodCall) link).params);
      } else if (link instanceof ConstructorCall) {
        nested.addAll(((ConstructorCall) link).args);
      }
      if (link.arrSel != null) {
        nested.addAll(link.arrSel.exprs);
      }
      for (Expression expr : nested) {
        if (usesEnclosingInstance(expr, locals)) {
          return true;
        }
      }
    }
    return false;
  }

  // Finds the enclosing method's local variables a lambda body (or local class) uses (to be
  // captured)
  private void visitBody(Expression expr, List<String> lambdaParams, List<VariableInfo> captured) {
    for (Token token : expr.tokens) {
      if (token instanceof TernaryExpression) {
        TernaryExpression ternary = (TernaryExpression) token;
        visitBody(ternary.condition, lambdaParams, captured);
        visitBody(ternary.trueBranch, lambdaParams, captured);
        visitBody(ternary.falseBranch, lambdaParams, captured);
      } else if (token instanceof LambdaExpr) {
        // (a nested lambda: its own parameters and locals aren't captured)
        LambdaExpr nested = (LambdaExpr) token;
        List<String> names = new ArrayList<>(lambdaParams);
        names.addAll(nested.params);
        List<Expression> exprs = List.of(nested.body);
        if (nested.block != null) {
          names.addAll(Statements.declaredNames(nested.block));
          exprs = Statements.expressions(nested.block);
        }
        for (Expression nestedExpr : exprs) {
          visitBody(nestedExpr, names, captured);
        }
      } else if (token instanceof Identifier) {
        Identifier root = (Identifier) token;
        if (root instanceof CastMethodCall) {
          visitBody(((CastMethodCall) root).innerExpr, lambdaParams, captured);
        }
        String rootName =
            (root instanceof MethodCall)
                ? ((MethodCall) root).name.split("\\.")[0]
                : root.val.toString();
        boolean isCall = root instanceof MethodCall && !((MethodCall) root).name.contains(".");
        VariableInfo var = ctx.classIdMap.variables.localMap.get(rootName);
        if (!isCall && var != null && !lambdaParams.contains(rootName) && !captured.contains(var)) {
          captured.add(var);
        }
        for (Identifier link = root; link != null; link = link.next) {
          if (link instanceof MethodCall) {
            for (Expression param : ((MethodCall) link).params) {
              visitBody(param, lambdaParams, captured);
            }
          } else if (link instanceof ConstructorCall) {
            for (Expression arg : ((ConstructorCall) link).args) {
              visitBody(arg, lambdaParams, captured);
            }
          }
          if (link.arrSel != null) {
            for (Expression index : link.arrSel.exprs) {
              visitBody(index, lambdaParams, captured);
            }
          }
        }
      }
    }
  }

  // The single non-default, non-static abstract method of a functional interface, or null if
  // zero or more than one such method exists (not a valid functional interface).
  // The single abstract method of a functional interface: of the JDK (by reflection) or of this
  // compilation
  private static class Sam {
    String name;
    List<String> paramTypes = new ArrayList<>();
    String returnType;
    List<String> exceptions = new ArrayList<>();
    java.lang.reflect.Method reflected;

    static Sam of(java.lang.reflect.Method method) {
      if (method == null) {
        return null;
      }
      Sam sam = new Sam();
      sam.name = method.getName();
      for (Class<?> type : method.getParameterTypes()) {
        sam.paramTypes.add(type.getName());
      }
      sam.returnType = method.getReturnType().getName();
      for (Class<?> exception : method.getExceptionTypes()) {
        sam.exceptions.add(exception.getName());
      }
      sam.reflected = method;
      return sam;
    }

    static Sam of(Clazz iface, List<Clazz> clazzes) {
      Sam sam = null;
      for (Method method : iface.methods) {
        if (!method.accFlags.contains(AccessFlag.ACC_ABSTRACT)) {
          continue;
        } else if (sam != null) {
          return null;
        }
        sam = new Sam();
        sam.name = method.name.val.toString();
        for (Argument arg : method.args) {
          sam.paramTypes.add(TypeUtils.resolveSimpleType(arg.type, clazzes, iface));
        }
        sam.returnType = TypeUtils.resolveSimpleType(method.type, clazzes, iface);
        sam.exceptions = method.exceptions;
      }
      return sam;
    }
  }

  private java.lang.reflect.Method findSingleAbstractMethod(Class<?> iface) {
    java.lang.reflect.Method found = null;
    for (java.lang.reflect.Method m : iface.getMethods()) {
      int mods = m.getModifiers();
      if (java.lang.reflect.Modifier.isAbstract(mods) && !m.isDefault() && !isObjectMethod(m)) {
        if (found != null) {
          return null; // more than one abstract method — not a SAM interface
        }
        found = m;
      }
    }
    return found;
  }

  // e.g. Comparator's "boolean equals(Object)" (not one of its abstract methods)
  private static boolean isObjectMethod(java.lang.reflect.Method method) {
    try {
      Object.class.getMethod(method.getName(), method.getParameterTypes());
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  String generateConstructorCall(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, ConstructorCall constructorCall) {
    String className = (String) constructorCall.val; // The type name from "new TypeName(...)"

    // Convert to JVM class name format (e.g., String -> java/lang/String)
    String jvmClassName = convertToJVMClassName(className);
    if (constructorCall.outer != null && !className.contains(".")) {
      // (a class nested in the enclosing instance's class)
      Clazz outerClazz =
          TypeUtils.sourceClazz(
              new NumExprTypeDecider(ctx).getType(classIdMap, constructorCall.outer),
              ctx.allClazzes);
      Clazz member =
          (outerClazz != null)
              ? TypeUtils.sourceClazzNamed(className, ctx.allClazzes, outerClazz)
              : null;
      if (member != null) {
        jvmClassName = member.fqn('/');
      }
    }
    if (!TypeUtils.classExists(jvmClassName, ctx.allClazzes)) {
      ctx.errors.addError(
          "constructor call generator",
          "cannot find class " + className,
          new RuntimeException().getStackTrace());
      return className;
    }
    if (constructorCall.anonymousBody != null && constructorCall.outer != null) {
      ctx.errors.addError(
          "constructor call generator",
          "anonymous subclasses of inner classes are not supported yet",
          new RuntimeException().getStackTrace());
      return className;
    } else if (constructorCall.anonymousBody != null) {
      return generateAnonymousClass(out, classIdMap, constructorCall, jvmClassName);
    }
    Clazz source = TypeUtils.sourceClazz(jvmClassName.replace('/', '.'), ctx.allClazzes);
    if (source != null && (source.isAbstract || source.isInterface)) {
      ctx.errors.addError(
          "constructor call generator",
          className + " is abstract; cannot be instantiated",
          new RuntimeException().getStackTrace());
      return className;
    }

    // Add class to constant pool before generating NEW instruction
    ctx.constPool.addClass(jvmClassName);

    // Generate NEW instruction
    ctx.invokeGen.newInstance(out, jvmClassName);

    // DUP the object reference for constructor call
    out.write(DUP);
    ctx.opStack.push(REF); // Track the duplicated reference

    // Phase 1: collect argument types without emitting bytecode
    List<String> argTypes = new ArrayList<>();
    // (an inner class's constructors take its enclosing instance first)
    boolean inner = source != null && source.isInner;
    if (constructorCall.outer != null && !inner) {
      ctx.errors.addError(
          "constructor call generator",
          "qualified new of static class",
          new RuntimeException().getStackTrace());
      return className;
    } else if (constructorCall.outer != null) {
      // the given enclosing instance, e.g. "outer" in "outer.new Inner()"
      eval(out, EnclosingInstances.outerOf(source, ctx.allClazzes).fqn(), constructorCall.outer);
    } else if (inner && !pushEnclosingInstance(out, source)) {
      return className;
    }
    if (inner) {
      argTypes.add(EnclosingInstances.outerOf(source, ctx.allClazzes).fqn());
    }
    for (Expression arg : constructorCall.args) {
      argTypes.add(new NumExprTypeDecider(ctx).getType(classIdMap, arg));
    }
    // (a local class's constructors take the variables it captures last)
    List<Argument> captured =
        (source != null && source.captured != null) ? source.captured : emptyList();
    for (Argument var : captured) {
      argTypes.add(var.type);
    }

    Method constructor = findConstructor(jvmClassName, argTypes);
    if (constructor == null) {
      ctx.errors.addError(
          "constructor call generator",
          String.format("cannot find constructor %s(%s)", className, String.join(", ", argTypes)),
          new RuntimeException().getStackTrace());
      return className; // Return the class type even if constructor not found
    }

    // Phase 2: eval each argument and box if needed (the enclosing instance is on the stack
    // already)
    int from = inner ? 1 : 0;
    Method withoutSynthetic = constructor;
    if (from > 0 || !captured.isEmpty()) {
      withoutSynthetic =
          new Method(
              constructor.fqClassName,
              constructor.accFlags,
              constructor.type,
              constructor.name,
              constructor.args.subList(from, constructor.args.size() - captured.size()),
              constructor.body);
    }
    ctx.methodCallGen.pushArgs(
        out,
        constructorCall.args,
        argTypes.subList(from, argTypes.size() - captured.size()),
        withoutSynthetic);
    for (Argument var : captured) {
      ctx.loadGen.load(out, ctx.classIdMap.variables.get(var.name));
    }

    // Generate INVOKESPECIAL <init>
    ctx.invokeGen.invokeSpecial(out, constructor);

    // the object type (the binary name of a class of this compilation run: there may be several of
    // the same simple name, e.g. nested in different classes)
    return (source != null) ? source.fqn() : className;
  }

  // (constructors have the method name "<init>")
  private Method findConstructor(String jvmClassName, List<String> argTypes) {
    Method constructor = findMatchingConstructor(jvmClassName, argTypes);
    if (constructor == null) {
      constructor = ctx.methodCallGen.findConstructorViaReflectionTyped(jvmClassName, argTypes);
    }
    if (constructor == null) {
      String simpleName = jvmClassName.substring(jvmClassName.lastIndexOf('/') + 1);
      constructor = ctx.methodCallGen.findVarargsMethod(simpleName + ".<init>", argTypes);
    }
    if (constructor == null) {
      constructor = ctx.methodCallGen.findConstructorViaReflection(jvmClassName, argTypes.size());
    }
    return constructor;
  }

  // "new T(args) { members }": a synthetic class (e.g. "Outer$1") extending (or implementing) T,
  // like a lambda's. Its constructor takes the enclosing instance (unless in a static context),
  // the arguments of T's constructor and the local variables the members use (captured as fields).
  private String generateAnonymousClass(
      DynamicByteArray out,
      GlobalIdentifierMap classIdMap,
      ConstructorCall constructorCall,
      String jvmClassName) {
    String superName = jvmClassName.replace('/', '.');
    boolean isInterface = TypeUtils.isInterface(superName, ctx.allClazzes);
    List<String> argTypes = new ArrayList<>();
    for (Expression arg : constructorCall.args) {
      argTypes.add(new NumExprTypeDecider(ctx).getType(classIdMap, arg));
    }
    Method superConstructor = isInterface ? null : findConstructor(jvmClassName, argTypes);
    Clazz superSource = TypeUtils.sourceClazz(superName, ctx.allClazzes);
    if (isInterface ? !argTypes.isEmpty() : superConstructor == null) {
      ctx.errors.addError(
          "constructor call generator",
          String.format(
              "cannot find constructor %s(%s)", constructorCall.val, String.join(", ", argTypes)),
          new RuntimeException().getStackTrace());
      return superName;
    }
    if (superSource != null && superSource.isInner) {
      ctx.errors.addError(
          "constructor call generator",
          "anonymous subclasses of inner classes are not supported yet",
          new RuntimeException().getStackTrace());
      return superName;
    }
    Clazz anon =
        new Clazz(
            ctx.clazz.packageName,
            ctx.clazz.imports,
            new Scope(Scopes.DEFAULT),
            String.valueOf(++ctx.syntheticClassCounter),
            false,
            false,
            isInterface ? List.of(superName) : emptyList(),
            isInterface ? Clazz.JAVA_LANG_OBJECT : superName,
            new ArrayList<>(),
            new ArrayList<>(),
            ctx.clazz.sourceFile);
    anon.outerName =
        (ctx.clazz.outerName != null ? ctx.clazz.outerName + "$" : "") + ctx.clazz.name;
    boolean inner = !isStaticContext();
    String anonFqn = anon.fqn();
    // its members (and the local variables they use)
    List<Statement> inits = new ArrayList<>();
    for (ClassMember member : constructorCall.anonymousBody) {
      if (member instanceof Method) {
        ((Method) member).fqClassName = anonFqn;
        anon.methods.add((Method) member);
      } else if (member instanceof ParallelDeclaration) {
        anon.fields.add((ParallelDeclaration) member);
        inits.add((ParallelDeclaration) member);
      } else if (member instanceof InstanceInitializer) {
        inits.add(new Block(((InstanceInitializer) member).body));
      }
    }
    List<VariableInfo> captured = capturedBy(anon.methods, inits);
    // its constructor passes the arguments on, the enclosing instance and the captured variables
    // become fields
    List<Argument> ctorArgs = new ArrayList<>();
    List<Expression> superArgs = new ArrayList<>();
    for (int i = 0; superConstructor != null && i < superConstructor.args.size(); i++) {
      String name = "$super" + i;
      ctorArgs.add(new Argument(superConstructor.args.get(i).type, new Identifier(name)));
      superArgs.add(new Expression(List.of(new Identifier(name))));
    }
    anon.methods.add(
        new Constructor(
            anonFqn,
            emptyList(),
            Token.id(Method.CONSTRUCTOR_NAME),
            ctorArgs,
            new ArrayList<>(List.of(new MethodCall("super", superArgs)))));
    anon.instanceInits = inits;
    if (inner) {
      InnerClasses.makeInner(anon, ctx.clazz);
    }
    InnerClasses.addFields(anon, false, asArguments(captured));
    Method ctor = anon.getConstructors().get(0);
    if (superConstructor != null) {
      ctor.exceptions = superConstructor.exceptions;
    }
    ctx.addSyntheticClazz(anon);

    String anonClassName = anon.fqn('/');
    ctx.constPool.addClass(anonClassName);
    ctx.invokeGen.newInstance(out, anonClassName);
    out.write(DUP);
    ctx.opStack.push(REF);
    if (inner && !pushEnclosingInstance(out, anon)) {
      return superName;
    }
    if (superConstructor != null) {
      ctx.methodCallGen.pushArgs(out, constructorCall.args, argTypes, superConstructor);
    }
    for (VariableInfo var : captured) {
      ctx.loadGen.load(out, var);
    }
    ctx.invokeGen.invokeSpecial(out, ctor);
    return superName;
  }

  // (no enclosing instance)
  boolean isStaticContext() {
    return ctx.method == null
        || ctx.method.accFlags.contains(AccessFlag.ACC_STATIC)
        || ctx.clazz.isInterface;
  }

  // The local variables used in the methods or initializers of a local or anonymous class (to be
  // captured)
  List<VariableInfo> capturedBy(List<Method> methods, List<Statement> inits) {
    List<String> declared = new ArrayList<>(Statements.declaredNames(inits));
    List<Expression> exprs = new ArrayList<>(Statements.expressions(inits));
    for (Method method : methods) {
      for (Argument arg : method.args) {
        declared.add(arg.name.val.toString());
      }
      declared.addAll(Statements.declaredNames(method.body));
      exprs.addAll(Statements.expressions(method.body));
    }
    List<VariableInfo> captured = new ArrayList<>();
    for (Expression expr : exprs) {
      visitBody(expr, declared, captured);
    }
    return captured;
  }

  // (an array type starts with "[")
  static List<Argument> asArguments(List<VariableInfo> vars) {
    List<Argument> args = new ArrayList<>();
    for (VariableInfo var : vars) {
      Argument arg =
          new Argument(
              var.arrType != null ? "[" + var.arrType : var.type, new Identifier(var.name));
      arg.genericType = var.genericType;
      args.add(arg);
    }
    return args;
  }

  // The class C of "C.this" (this class or an enclosing one), or null for anything else
  private Clazz enclosingThis(Identifier id) {
    if (id.next == null || !"this".equals(id.next.val.toString()) || ctx.clazz == null) {
      return null;
    }
    String name = id.val.toString();
    if (name.equals(ctx.clazz.name)) {
      return ctx.clazz;
    }
    for (Clazz outer : FieldProcessor.enclosingClasses(ctx.clazz, ctx.allClazzes)) {
      if (outer.name.equals(name)) {
        return outer;
      }
    }
    return null;
  }

  // Pushes the enclosing instance for a new instance of an inner class, e.g. "this" for "new
  // Inner()"
  // in a method of its enclosing class (an error in a static method)
  private boolean pushEnclosingInstance(DynamicByteArray out, Clazz inner) {
    Clazz outer = EnclosingInstances.outerOf(inner, ctx.allClazzes);
    List<Integer> path = EnclosingInstances.path(ctx, ctx.clazz, outer);
    if (path == null
        && outer != null
        && TypeUtils.superclasses(ctx.clazz.fqn(), ctx.allClazzes).contains(outer.fqn())) {
      path = new ArrayList<>(); // (this is an instance of the outer class, e.g. a subclass)
    }
    if (path == null
        || (ctx.method != null && ctx.method.accFlags.contains(AccessFlag.ACC_STATIC))) {
      ctx.errors.addError(
          "constructor call generator",
          "non-static variable this cannot be referenced from a static context",
          new RuntimeException().getStackTrace());
      return false;
    }
    ctx.loadGen.loadReference(out, (short) 0);
    for (int field : path) {
      ctx.loadGen.getField(out, (short) field);
    }
    ctx.opStack.push(REF);
    return true;
  }

  Method findMatchingConstructor(String className, List<String> argTypes) {
    // Constructors are registered under "<init>" (current class / java.lang classes) and
    // "SimpleName.<init>" (classes of the same compilation run)
    String simpleName =
        className.contains("/") ? className.substring(className.lastIndexOf('/') + 1) : className;
    Method constructor =
        ctx.methodCallGen.findBestMatch(
            "<init>",
            argTypes,
            c ->
                c.fqClassName.equals(className)
                    || c.fqClassName.endsWith("." + className)
                    || c.fqClassName.replace('.', '/').equals(className));
    if (constructor == null) {
      // by binary name, e.g. "a.b.Outer$Nested.<init>" for a nested class
      constructor =
          ctx.methodCallGen.findBestMatch(className.replace('/', '.') + ".<init>", argTypes, null);
    }
    if (constructor == null) {
      constructor = ctx.methodCallGen.findBestMatch(simpleName + ".<init>", argTypes, null);
    }
    return constructor;
  }

  private String convertToJVMClassName(String className) {
    // Handle common types that map to java.lang classes
    switch (className) {
      case "String":
        return "java/lang/String";
      case "Object":
        return "java/lang/Object";
      case "Integer":
        return "java/lang/Integer";
      case "Double":
        return "java/lang/Double";
      case "Float":
        return "java/lang/Float";
      case "Long":
        return "java/lang/Long";
      case "Boolean":
        return "java/lang/Boolean";
      case "Character":
        return "java/lang/Character";
      case "Byte":
        return "java/lang/Byte";
      case "Short":
        return "java/lang/Short";
      default:
        // (a local class of this method)
        if (ctx.localClasses.containsKey(className)) {
          return ctx.localClasses.get(className).fqn('/');
        }
        // Check if it's a user-defined class in the current compilation unit
        Clazz source = TypeUtils.sourceClazzNamed(className, ctx.allClazzes, ctx.clazz);
        if (source != null) {
          return source.fqn('/');
        }
        // Try resolving via import list
        if (ctx.clazz != null && ctx.clazz.imports != null && !className.contains(".")) {
          for (io.github.martinschneider.orzo.parser.productions.Import imp : ctx.clazz.imports) {
            if (!imp.isStatic && imp.id != null) {
              String simpleName =
                  imp.id.contains(".") ? imp.id.substring(imp.id.lastIndexOf('.') + 1) : imp.id;
              if (simpleName.equals(className)) {
                return imp.id.replace('.', '/');
              } else if ("*".equals(simpleName)) {
                String pkg = imp.id.substring(0, imp.id.lastIndexOf('.'));
                String candidate = pkg + "." + className;
                try {
                  Class.forName(candidate);
                  return candidate.replace('.', '/');
                } catch (ClassNotFoundException e) {
                  // not in this wildcard package
                }
              }
            }
          }
        }
        // Try java.lang.* auto-import (e.g. StringBuilder, Math, System)
        if (!className.contains(".")) {
          try {
            Class.forName("java.lang." + className);
            return "java/lang/" + className;
          } catch (ClassNotFoundException e) {
            // not in java.lang; fall through
          }
        }
        // Try resolving via current class's package (for same-package references)
        if (ctx.clazz != null && !className.contains(".")) {
          String pkg = ctx.clazz.packageName;
          if (pkg != null && !pkg.isEmpty()) {
            String candidate = pkg + "." + className;
            try {
              Class.forName(candidate);
              return candidate.replace('.', '/');
            } catch (ClassNotFoundException e) {
              // not in same package; fall through
            }
          }
        }
        int dot = className.indexOf('.');
        if (dot > 0 && Character.isUpperCase(className.charAt(0))) {
          // a nested class of a (simply named) class, e.g. "Outer.Inner" -> "a/b/Outer$Inner"
          return convertToJVMClassName(className.substring(0, dot))
              + "$"
              + className.substring(dot + 1).replace('.', '$');
        }
        // For fully qualified names, just replace dots with slashes
        return className.replace('.', '/');
    }
  }

  String resolveJvmClassName(String typeName) {
    return convertToJVMClassName(typeName);
  }

  private static boolean isNonPrimitive(String type) {
    return type != null
        && !INT.equals(type)
        && !LONG.equals(type)
        && !BYTE.equals(type)
        && !SHORT.equals(type)
        && !DOUBLE.equals(type)
        && !FLOAT.equals(type)
        && !CHAR.equals(type)
        && !BOOLEAN.equals(type)
        && !VOID.equals(type)
        && !"int_zero".equals(type);
  }

  // Whether a String operand ending with this (postfix) token may be null: not a literal or a
  // concatenation
  private static boolean mayBeNull(Token last) {
    return !(last instanceof Str) && !last.eq(op(PLUS));
  }

  // Evaluates an expression with reverseComp=false and ensures the result is a 0/1 value.
  // If eval() emits a bare branch opcode (e.g. IF_ACMPEQ from a comparator), append a
  // value-conversion pattern: branch → ICONST_1 (true), fall-through → ICONST_0 (false).
  private DynamicByteArray evalAsValue(String type, Expression expr) {
    DynamicByteArray subOut = new DynamicByteArray();
    eval(subOut, type, expr, false, false);
    byte[] bytes = subOut.getBytes();
    if (bytes.length > 0) {
      int lastOp = lastOpcode(bytes);
      if (lastOp >= 153 && lastOp <= 166) {
        // Branch opcode without its 2-byte operand: add offset + value-producing tail.
        // Branch fires when condition is TRUE → jump to ICONST_1 (7 bytes ahead of opcode).
        subOut.write((short) 7); // 2-byte offset to ICONST_1
        subOut.write(ICONST_0); // false case (fall-through)
        subOut.write(GOTO);
        subOut.write((short) 4); // 4 bytes from GOTO opcode → past ICONST_1
        subOut.write(ICONST_1); // true case
      }
    }
    return subOut;
  }

  // Returns the outermost operator of a postfix token list if it's a &, | or ^ applied to boolean
  // operands (e.g. comparisons), otherwise null.
  private Operators findOutermostBooleanBitOp(List<Token> tokens) {
    if (tokens.isEmpty() || !(tokens.get(tokens.size() - 1) instanceof Operator)) {
      return null;
    }
    Operators op = ((Operator) tokens.get(tokens.size() - 1)).opValue();
    if (op != BITWISE_AND && op != BITWISE_OR && op != BITWISE_XOR) {
      return null;
    }
    int rStart = findRightOperandStart(tokens, tokens.size() - 1);
    List<Token> left = tokens.subList(0, rStart);
    List<Token> right = tokens.subList(rStart, tokens.size() - 1);
    return (isBooleanOperand(left) || isBooleanOperand(right)) ? op : null;
  }

  private boolean isBooleanOperand(List<Token> operand) {
    if (operand.isEmpty()) {
      return false;
    }
    Token last = operand.get(operand.size() - 1);
    if (last instanceof Operator) {
      Operators op = ((Operator) last).opValue();
      return COMPARATORS.contains(op) || op == LOGICAL_AND || op == LOGICAL_OR || op == NEGATE;
    }
    return BOOLEAN.equals(
        new NumExprTypeDecider(ctx)
            .getType(ctx.classIdMap, new Expression(new ArrayList<>(operand))));
  }

  // Returns the index of the outermost LOGICAL_AND or LOGICAL_OR in a postfix token list,
  // or -1 if neither is present. In postfix, the outermost binary logical op appears last.
  // In postfix notation, the outermost operator is always the last token.
  // Only return its index if it is a logical AND or OR; otherwise short-circuit does not apply.
  private int findOutermostLogicalOp(List<Token> tokens) {
    if (tokens.isEmpty()) return -1;
    Token last = tokens.get(tokens.size() - 1);
    if (last instanceof Operator) {
      Operators opType = ((Operator) last).opValue();
      if (opType == LOGICAL_AND || opType == LOGICAL_OR) {
        return tokens.size() - 1;
      }
    }
    return -1;
  }

  // Given a postfix token list and the index of a binary operator, return the start index
  // of the right operand. Uses stack-balance counting: each leaf pushes +1, binary ops -1,
  // unary ops 0; the right operand starts where balance first reaches 1 from opPos-1.
  private int findRightOperandStart(List<Token> tokens, int opPos) {
    int balance = 0;
    for (int i = opPos - 1; i >= 0; i--) {
      Token t = tokens.get(i);
      if (t instanceof InstanceofToken) {
        // instanceof is postfix unary: consumes 1, produces 1 → no net stack change
      } else if (t instanceof io.github.martinschneider.orzo.lexer.tokens.Type) {
        // A bare cast type marker (e.g. "char" in "(char) expr"), emitted immediately before
        // the expression it casts. The main eval() loop has no case for it at all — it's a
        // pure no-op there (the narrowing/widening happens via the target-type hint passed to
        // eval(), not by processing this token) — so it must not count as a leaf here either.
      } else if (t instanceof MethodCall && ((MethodCall) t).literalReceiver) {
        // "lit".method(): consumes the preceding Str operand, produces 1 → no net stack change
      } else if (t instanceof Operator) {
        Operators opType = ((Operator) t).opValue();
        if (opType == NEGATE
            || opType == CAST
            || opType == POST_INCREMENT
            || opType == POST_DECREMENT
            || opType == PRE_INCREMENT
            || opType == PRE_DECREMENT) {
          // unary: no net stack change
        } else {
          balance--; // binary: consumes 2, produces 1
        }
      } else {
        balance++; // leaf token (identifier, literal, etc.)
      }
      if (balance == 1) {
        // A cast's Type marker always sits immediately before the expression it casts, as one
        // glued-together unit — swallow it into this operand's start too, since the backward
        // scan above stops at the inner expression's own leaf without ever looking at it.
        while (i > 0
            && tokens.get(i - 1) instanceof io.github.martinschneider.orzo.lexer.tokens.Type) {
          i--;
        }
        return i;
      }
    }
    return 0;
  }

  // Scan the postfix token list for ASSIGN operators where the LHS is a single simple local
  // variable or field. Returns a map from ASSIGN token index to the target VariableInfo, and
  // populates skipLoad with the indices of LHS tokens that should NOT be loaded (since the
  // assignment handler will DUP+STORE the RHS instead).
  private Map<Integer, VariableInfo> findAssignmentTargets(
      List<Token> tokens, Map<Integer, VariableInfo> skipLoad) {
    Map<Integer, VariableInfo> result = new HashMap<>();
    for (int i = 0; i < tokens.size(); i++) {
      if (!(tokens.get(i) instanceof Operator)) continue;
      if (((Operator) tokens.get(i)).opValue() != ASSIGN) continue;
      // Note: an outer assignment statement's own "=" is never part of these tokens — it's
      // consumed by AssignmentParser/DeclarationParser before the RHS expression is even parsed.
      // So an ASSIGN found here — even as the last token (e.g. chained "a = b = c;", or an
      // assignment used as a method argument like "f(x = g())") — always needs an explicit
      // store; nothing else will perform it.
      int rStart = findRightOperandStart(tokens, i);
      int lStart = (rStart > 0) ? findRightOperandStart(tokens, rStart) : 0;
      if (lStart != rStart - 1) continue;
      if (!(tokens.get(lStart) instanceof Identifier)) continue;
      Identifier lhsId = (Identifier) tokens.get(lStart);
      if (lhsId.next != null || lhsId.arrSel != null) continue;
      VariableInfo vi = ctx.classIdMap.variables.get(lhsId);
      if (vi == null) continue;
      result.put(i, vi);
      skipLoad.put(lStart, vi);
    }
    return result;
  }

  // To generate code for control structures it is often useful to get the inverse comparator. For
  // example, for "if (x==0)" we will use the opcode 154 (IFNE) to jump beyond the code in the if
  // block. This method performs this conversion.
  private byte reverseComp(byte opCode) {
    switch (opCode) {
      case IFGT:
        return IFLE;
      case IFGE:
        return IFLT;
      case IFLT:
        return IFGE;
      case IFLE:
        return IFGT;
      case IFEQ:
        return IFNE;
      case IFNE:
        return IFEQ;
      case IF_ICMPEQ:
        return IF_ICMPNE;
      case IF_ICMPNE:
        return IF_ICMPEQ;
      case IF_ICMPLT:
        return IF_ICMPGE;
      case IF_ICMPGT:
        return IF_ICMPLE;
      case IF_ICMPLE:
        return IF_ICMPGT;
      case IF_ICMPGE:
        return IF_ICMPLT;
      case IF_ACMPEQ:
        return IF_ACMPNE;
      case IF_ACMPNE:
        return IF_ACMPEQ;
    }
    return opCode;
  }
}
