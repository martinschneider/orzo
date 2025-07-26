package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
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
import static io.github.martinschneider.orzo.codegen.OpCodes.NEWARRAY;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getArrayType;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getStoreOpCode;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.ARITHMETIC_OPS;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.COMPARE_TO_ZERO_OPS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.EQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.GREATEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LESSEQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NOTEQUAL;
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
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT_ZERO;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.OperandStack;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExpressionGenerator {
  private static final String LOGGER_NAME = "expression code generator";

  public CGContext ctx;

  private static final List<Operators> COMPARATORS =
      List.of(EQUAL, NOTEQUAL, GREATER, LESS, LESSEQ, GREATEREQ);

  public ExpressionResult eval(DynamicByteArray out, String type, Expression expr) {
    return eval(out, type, expr, true, false);
  }

  public ExpressionResult eval(
      DynamicByteArray out, String type, Expression expr, boolean pushIfZero, boolean reverseComp) {
    // TODO: support String concatenation
    // TODO: support different types
    // TODO: error handling, e.g. only "+" operator is valid for String
    // concatenation, "%" is not
    // valid for doubles etc.
    if (expr instanceof ArrayInit) {
      ArrayInit arrInit = (ArrayInit) expr;
      generateArray(out, ctx.classIdMap, arrInit);
      return new ExpressionResult(arrInit.type, null);
    }
    if (type == null) {
      type = new NumExprTypeDecider(ctx).getType(ctx.classIdMap, expr);
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
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (!type.equals(DOUBLE)
          && ((i + 2 < tokens.size() && tokens.get(i + 2).eq(op(POW)))
              || (i + 1 < tokens.size() && tokens.get(i + 1).eq(op(POW))))) {
        // Calculating powers for integer types uses BigInteger and requires loading the
        // operands in
        // a different order. Therefore, we skip processing them here.
      } else if (token instanceof Identifier) {
        type = handleId(out, ctx.classIdMap, tokens, i, token, type);
        exprTypeStack.push(type);
      } else if (token instanceof IntLiteral) {
        BigInteger bigInt = (BigInteger) ((IntLiteral) token).val;
        Long intValue = bigInt.longValue();
        // look ahead for <<, >> or >>> operators which require the second argument to
        // be an integer
        if (i + 1 < tokens.size()
            && ((tokens.get(i + 1).eq(op(LSHIFT))
                || (tokens.get(i + 1).eq(op(RSHIFT)))
                || (tokens.get(i + 1).eq(op(RSHIFTU)))))) {
          ctx.pushGen.push(out, INT, BigDecimal.valueOf(intValue));
        } else if (!type.equals(INT) || intValue != 0 || pushIfZero) {
          ctx.pushGen.push(out, type, BigDecimal.valueOf(intValue));
        }
        // special handling for 0 because there are different opcodes to compare to 0
        if ((type.equals(INT) || type.equals(SHORT) || type.equals(BYTE) || type.equals(CHAR))
            && intValue.intValue() == 0) {
          exprTypeStack.push(INT_ZERO);
        } else {
          exprTypeStack.push(type);
        }
        val = bigInt;
      } else if (token instanceof BoolLiteral) {
        Boolean bool = (Boolean) ((BoolLiteral) token).val;
        ctx.pushGen.pushBool(out, bool);
        val = bool;
        exprTypeStack.push(BOOLEAN);
      } else if (token instanceof FPLiteral) {
        BigDecimal bigDec = (BigDecimal) ((FPLiteral) token).val;
        ctx.pushGen.push(out, type, bigDec);
        val = bigDec;
        exprTypeStack.push(DOUBLE);
      } else if (token instanceof Str) {
        ctx.loadGen.ldc(out, CONSTANT_STRING, ((Str) token).strValue());
        type = STRING;
        exprTypeStack.push(STRING);
      } else if (token instanceof Chr) {
        char chr = (char) ((Chr) token).val;
        ctx.pushGen.push(out, CHAR, chr);
        type = CHAR;
        exprTypeStack.push(CHAR);
      } else if (token instanceof Operator) {
        Operators op = ((Operator) token).opValue();
        if (List.of(POST_INCREMENT, POST_DECREMENT, PRE_INCREMENT, PRE_DECREMENT).contains(op)) {
          VariableInfo varInfo = ctx.classIdMap.variables.get(tokens.get(i - 1));
          ctx.incrGen.inc(out, varInfo, op, false);
        } else if (op.equals(POW)) {
          if (type.equals(DOUBLE)) {
            ctx.constPool.addClass("java/lang/Math");
            ctx.invokeGen.invokeStatic(
                out, new Method("java.lang.Math", "pow", DOUBLE, List.of(DOUBLE, DOUBLE)));
          } else {
            eval(out, LONG, new Expression(List.of(tokens.get(i - 1))));
            ctx.constPool.addClass("java/math/BigInteger");
            ctx.constPool.addMethodRef(
                "java/math/BigInteger", "valueOf", "(J)Ljava/math/BigInteger;");
            ctx.constPool.addMethodRef("java/math/BigInteger", "pow", "(I)Ljava/math/BigInteger;");
            ctx.constPool.addMethodRef("java/math/BigInteger", "longValue", "()J");
            ctx.invokeGen.invokeStatic(
                out,
                new Method(
                    "java.math.BigInteger", "valueOf", "Ljava/math/BigInteger;", List.of(LONG)));
            eval(out, INT, new Expression(List.of(tokens.get(i - 2))));
            ctx.invokeGen.invokeVirtual(
                out,
                new Method("java/math/BigInteger", "pow", "Ljava/math/BigInteger;", List.of(INT)));
            ctx.invokeGen.invokeVirtual(
                out, new Method("java/math/BigInteger", "longValue", LONG, emptyList()));
            type = LONG;
          }
        } else {
          byte[] opCode = null;
          if (COMPARATORS.contains(op) && exprTypeStack.oneOfTopTwoElementsIsZero()) {
            opCode = new byte[] {COMPARE_TO_ZERO_OPS.get(op)};
            type = BOOLEAN;
          } else {
            opCode = ARITHMETIC_OPS.getOrDefault(op, Collections.emptyMap()).get(type);
          }
          if (reverseComp) {
            if (opCode != null) {
              // creating a copy is important, otherwise we would modify the original array
              opCode = Arrays.copyOf(opCode, opCode.length);
            }
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
            ctx.opStack.push(type);
            out.write(opCode);
          }
        }
      }
    }
    if (expr.cast != null) {
      String currType = ctx.opStack.pop();
      ctx.basicGen.convert(out, currType, expr.cast.name);
      type = expr.cast.name;
      ctx.opStack.push(type);
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
    do {
      // In handleId() method, add this BEFORE line 241:
      if (token instanceof ConstructorCall) {
        ConstructorCall constructorCall = (ConstructorCall) token;
        type = generateConstructorCall(out, classIdMap, constructorCall);
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        type = ctx.methodCallGen.generate(out, classIdMap, methodCall);
        if (curr.arrSel != null) {
          ctx.loadGen.loadValueFromArrayOnStack(out, classIdMap, curr.arrSel.exprs, type);
          if (!type.isEmpty()) {
            // remove leading '[' from type
            type = type.substring(methodCall.arrSel.exprs.size());
          }
        }
      } else {
        if (prev != null && ctx.classIdMap.variables.get(prev).arrType != null) {
          ctx.basicGen.arrayLength(out);
          type = INT;
        } else {
          VariableInfo varInfo = ctx.classIdMap.variables.get(curr);
          if (varInfo == null) {
            ctx.errors.addError(
                LOGGER_NAME,
                String.format("Unknown variable: %s", curr),
                new RuntimeException().getStackTrace());
            return type;
          }
          String varType = varInfo.type;
          String arrType = varInfo.arrType;
          // look ahead for ++ or -- operators because in that case we do not push the
          // value to the
          // stack
          if (i + 1 == tokens.size()
              || (!tokens.get(i + 1).eq(op(POST_DECREMENT))
                  && !tokens.get(i + 1).eq(op(POST_INCREMENT))
                  && !tokens.get(i + 1).eq(op(PRE_INCREMENT))
                  && !tokens.get(i + 1).eq(op(PRE_DECREMENT)))) {
            if (curr.arrSel != null) {
              // array
              ctx.loadGen.loadValueFromArray(out, classIdMap, curr.arrSel.exprs, varInfo);
              type = varInfo.arrType;
            } else {
              ctx.loadGen.load(out, varInfo);
            }
          }
          if (!type.equals(varType) && arrType == null) {
            ctx.basicGen.convert1(out, varType, type);
          }
        }
      }
      prev = curr;
      curr = curr.next;
    } while (curr != null);
    return type;
  }

  public HasOutput generateArray(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, ArrayInit arrInit) {
    String type = arrInit.type;
    byte arrayType = getArrayType(type);
    byte storeOpCode = getStoreOpCode(type);
    createArray(out, classIdMap, arrayType, arrInit.dims);
    // multi-dim array
    if (arrInit.vals.size() >= 2) {
      // TODO
    }
    // one-dim array
    else if (arrInit.vals.size() == 1) {
      for (int i = 0; i < arrInit.vals.get(0).size(); i++) {
        out.write(DUP);
        ctx.pushGen.push(out, INT, i);
        ctx.exprGen.eval(out, type, arrInit.vals.get(0).get(i));
        out.write(storeOpCode);
      }
    }
    // ctx.assignGen.assignArray(out, variables, type, decl.arrDim, decl.name);
    return out;
  }

  private void createArray(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, byte arrayType, List<Expression> dims) {
    if (dims.size() == 1) {
      ctx.exprGen.eval(out, INT, dims.get(0));
      out.write(NEWARRAY);
      out.write(arrayType);
    } else {
      // TODO:
    }
  }

  String generateConstructorCall(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, ConstructorCall constructorCall) {
    String className = (String) constructorCall.val; // The type name from "new TypeName(...)"

    // Convert to JVM class name format (e.g., String -> java/lang/String)
    String jvmClassName = convertToJVMClassName(className);

    // Add class to constant pool before generating NEW instruction
    ctx.constPool.addClass(jvmClassName);

    // Generate NEW instruction
    ctx.invokeGen.newInstance(out, jvmClassName);

    // DUP the object reference for constructor call
    out.write(DUP);
    ctx.opStack.push(REF); // Track the duplicated reference

    // Push constructor arguments onto stack
    List<String> argTypes = new ArrayList<>();
    for (Expression arg : constructorCall.args) {
      String argType = new NumExprTypeDecider(ctx).getType(classIdMap, arg);
      ctx.exprGen.eval(out, argType, arg);
      argTypes.add(argType);
    }

    // Find matching constructor (constructors have method name "<init>")
    Method constructor = findMatchingConstructor(jvmClassName, argTypes);
    if (constructor == null) {
      ctx.errors.addError(
          "constructor call generator",
          String.format("No matching constructor found for %s(%s)", className, argTypes),
          new RuntimeException().getStackTrace());
      return className; // Return the class type even if constructor not found
    }

    // Generate INVOKESPECIAL <init>
    ctx.invokeGen.invokeSpecial(out, constructor);

    return className; // Return the object type
  }

  private Method findMatchingConstructor(String className, List<String> argTypes) {
    // Look for constructors in the context's method map
    // Constructors have the method name "<init>"
    List<List<String>> typesList = new ArrayList<>();
    for (int i = 0; i < argTypes.size(); i++) {
      typesList.add(TypeUtils.assignableTo(argTypes.get(i)));
    }

    Method constructor = null;
    for (List<String> assignTypes : TypeUtils.combinations(typesList)) {
      String constructorKey = "<init>" + TypeUtils.typesDescr(assignTypes);
      constructor = ctx.methodMap.get(constructorKey);
      if (constructor != null && constructor.fqClassName.equals(className)) {
        break;
      }
    }

    // If no constructor found, create a default constructor for basic types
    if (constructor == null && argTypes.isEmpty()) {
      // Default no-arg constructor
      constructor = new Method(className, "<init>", "void", argTypes);
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
        // For fully qualified names, just replace dots with slashes
        return className.replace('.', '/');
    }
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
    }
    return opCode;
  }
}
