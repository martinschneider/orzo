package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GETSTATIC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_4;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_5;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_M1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IDIV;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFEQ;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFGE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFGT;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFLE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFLT;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFNE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPEQ;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPGE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPGT;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPLE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPLT;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPNE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IMUL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.INVOKEVIRTUAL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IREM;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISUB;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LDC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.RETURN;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.SIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_UTF8;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.VOID;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.Type;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operators;
import io.github.martinschneider.kommpeiler.scanner.tokens.Str;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {
  private static final short JAVA_CLASS_MAJOR_VERSION = 49;
  private static final short JAVA_CLASS_MINOR_VERSION = 0;
  private static final int INTEGER_DEFAULT_VALUE = 0;
  private Clazz clazz;
  private ConstantPool constantPool;
  private Output out;

  public CodeGenerator(Clazz clazz, Output out) {
    this.clazz = clazz;
    this.out = out;
  }

  private void accessModifiers() {
    // super + public
    out.write((short) 0x0021);
  }

  private void assignInteger(Map<Identifier, Integer> variables, HasOutput out, Identifier var) {
    variables.computeIfAbsent(var, x -> variables.size());
    int index = variables.get(var);
    storeInteger(out, index);
  }

  private void attributes() {
    out.write((short) 0);
  }

  private void bipush(DynamicByteArray out, int number) {
    out.write(BIPUSH);
    out.write((byte) number);
  }

  private void classIndex() {
    out.write(constantPool.indexOf(CONSTANT_CLASS, clazz.getName().getValue()));
  }

  private void constantPool() {
    out.write(constantPool.getBytes());
  }

  private void fields() {
    out.write((short) 0);
  }

  public void generate() {
    processConstantPool();
    supportPrint();
    header();
    constantPool();
    accessModifiers();
    classIndex();
    superClassIndex();
    interfaces();
    fields();
    methods();
    attributes();
    out.flush();
  }

  private void generateCode(
      Map<Identifier, Integer> variables, DynamicByteArray out, Statement statement) {
    if (statement instanceof Declaration) {
      Declaration decl = (Declaration) statement;
      if (decl.hasValue()) {
        evaluateExpression(variables, out, decl.getValue());
      } else {
        if (decl.getType().eq(type(INT))) {
          pushInteger(out, INTEGER_DEFAULT_VALUE);
        }
      }
      assignValue(variables, out, decl.getType(), decl.getName());
    } else if (statement instanceof Assignment) {
      Assignment assignment = (Assignment) statement;
      // TODO: store type in variable map
      // TODO: type conversion
      ExpressionResult result = evaluateExpression(variables, out, assignment.getRight());
      assignValue(variables, out, result.getType(), assignment.getLeft());
    } else if (statement instanceof MethodCall) {
      MethodCall methodCall = (MethodCall) statement;
      if ("System.out.println".equals(methodCall.getQualifiedName())) {
        for (Expression param : methodCall.getParameters()) {
          getStatic(out, "java/lang/System", "out", "Ljava/io/PrintStream;");
          ExpressionResult result = evaluateExpression(variables, out, param);
          print(out, result.getType());
        }
      }
    } else if (statement instanceof IfStatement) {
      IfStatement ifStatement = (IfStatement) statement;
      DynamicByteArray bodyOut = new DynamicByteArray();
      for (Statement stmt : ifStatement.getBody()) {
        generateCode(variables, bodyOut, stmt);
      }
      DynamicByteArray conditionOut = new DynamicByteArray();
      short branchBytes = (short) (3 + bodyOut.getBytes().length);
      generateCondition(variables, conditionOut, ifStatement.getCondition(), branchBytes);
      out.write(conditionOut.getBytes());
      out.write(bodyOut.getBytes());
    }
  }

  private void generateCondition(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Condition condition,
      short branchBytes) {
    // TODO: support other boolean conditions
    ExpressionResult left = evaluateExpression(variables, out, condition.getLeft(), false);
    ExpressionResult right = evaluateExpression(variables, out, condition.getRight(), false);
    boolean leftZero = isZero(left.getValue());
    boolean rightZero = isZero(right.getValue());
    if (!leftZero && rightZero) {
      switch (condition.getOperator().cmpValue()) {
          // use the inverse comparison because jumping means we execute the "else" part of the
          // condition
        case EQUAL:
          out.write(IFNE);
          break;
        case NOTEQUAL:
          out.write(IFEQ);
          break;
        case GREATER:
          out.write(IFLE);
          break;
        case GREATEREQ:
          out.write(IFLT);
          break;
        case SMALLER:
          out.write(IFGE);
          break;
        case SMALLEREQ:
          out.write(IFGT);
          break;
      }
      out.write(shortToByteArray(branchBytes));
      return;
    }
    if (leftZero && !rightZero) {
      switch (condition.getOperator().cmpValue()) {
        case EQUAL:
          out.write(IFEQ);
          break;
        case NOTEQUAL:
          out.write(IFNE);
          break;
        case GREATER:
          out.write(IFGT);
          break;
        case GREATEREQ:
          out.write(IFGE);
          break;
        case SMALLER:
          out.write(IFLT);
          break;
        case SMALLEREQ:
          out.write(IFLE);
          break;
      }
      out.write(shortToByteArray(branchBytes));
      return;
    }
    switch (condition.getOperator().cmpValue()) {
        // use the inverse comparison because jumping means we execute the "else" part of the
        // condition
      case EQUAL:
        out.write(IF_ICMPNE);
        break;
      case NOTEQUAL:
        out.write(IF_ICMPEQ);
        break;
      case GREATER:
        out.write(IF_ICMPLE);
        break;
      case GREATEREQ:
        out.write(IF_ICMPLT);
        break;
      case SMALLER:
        out.write(IF_ICMPGE);
        break;
      case SMALLEREQ:
        out.write(IF_ICMPGT);
        break;
    }
    out.write(shortToByteArray(branchBytes));
  }

  private boolean isZero(Object value) {
    return (value instanceof Integer && ((Integer) value).intValue() == 0);
  }

  private void assignValue(
      Map<Identifier, Integer> variables, DynamicByteArray out, Type type, Identifier name) {
    if (type.getValue().equals(INT.name())) {
      assignInteger(variables, out, name);
    }
  }

  /**
   * evaluates expression
   *
   * @return the "return" type of the expression
   */
  private ExpressionResult evaluateExpression(
      Map<Identifier, Integer> variables, DynamicByteArray out, Expression expression) {
    return evaluateExpression(variables, out, expression, true);
  }

  private ExpressionResult evaluateExpression(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Expression expression,
      boolean pushIfZero) {
    // TODO: support String concatenation
    // TODO: support different types
    // TODO: error handling, e.g. only "+" operator is valid for String concatenation, "%" is not
    // valid for doubles etc.
    Type type = type(VOID);
    Object value = null;
    for (Token token : expression.getPostfix()) {
      if (token instanceof Identifier) {
        Identifier id = (Identifier) token;
        loadInteger(out, variables.get(id));
        type = type(INT);
      } else if (token instanceof IntNum) {
        Integer intValue = ((IntNum) token).intValue();
        if (intValue != 0 || pushIfZero) {
          pushInteger(out, intValue);
        }
        type = type(INT);
        value = intValue;
      } else if (token instanceof Str) {
        ldc(out, CONSTANT_STRING, ((Str) token).strValue());
        type = type("java.lang.String");
      } else if (token instanceof Operator) {
        Operators op = ((Operator) token).opValue();
        switch (op) {
          case PLUS:
            out.write(IADD);
            break;
          case MINUS:
            out.write(ISUB);
            break;
          case TIMES:
            out.write(IMUL);
            break;
          case DIV:
            out.write(IDIV);
            break;
          case MOD:
            out.write(IREM);
            break;
          default:
        }
      }
    }
    return new ExpressionResult(type, value);
  }

  private DynamicByteArray getStatic(
      DynamicByteArray out, String clazz, String field, String type) {
    out.write(GETSTATIC);
    out.write(constantPool.indexOf(CONSTANT_FIELDREF, clazz, field, type));
    return out;
  }

  private void header() {
    out.write(0xCAFEBABE);
    out.write(JAVA_CLASS_MINOR_VERSION);
    out.write(JAVA_CLASS_MAJOR_VERSION);
  }

  private void interfaces() {
    out.write((short) 0);
  }

  private DynamicByteArray invokeVirtual(
      DynamicByteArray out, String clazz, String field, String type) {
    out.write(INVOKEVIRTUAL);
    out.write(constantPool.indexOf(CONSTANT_METHODREF, clazz, field, type));
    return out;
  }

  private DynamicByteArray ldc(DynamicByteArray out, byte type, Object key) {
    out.write(LDC);
    out.write((byte) constantPool.indexOf(type, key));
    return out;
  }

  private DynamicByteArray loadInteger(DynamicByteArray out, Integer idx) {
    if (idx == 0) {
      out.write(ILOAD_0);
    } else if (idx == 1) {
      out.write(ILOAD_1);
    } else if (idx == 2) {
      out.write(ILOAD_2);
    } else if (idx == 3) {
      out.write(ILOAD_3);
    } else {
      out.write(ILOAD);
      out.write(idx.byteValue());
    }
    return out;
  }

  private void methods() {
    List<Method> methods = clazz.getBody();
    // number of methods
    out.write((short) methods.size());
    for (Method method : methods) {
      // todo: handle global variables
      Map<Identifier, Integer> variables = new HashMap<>();
      out.write((short) 9); // public static
      out.write(constantPool.indexOf(CONSTANT_UTF8, method.getName().getValue()));
      out.write(constantPool.indexOf(CONSTANT_UTF8, method.getTypeDescr()));
      out.write((short) 1); // attribute size
      out.write(constantPool.indexOf(CONSTANT_UTF8, "Code"));
      DynamicByteArray methodCode = new DynamicByteArray();
      for (Statement statement : method.getBody()) {
        generateCode(variables, methodCode, statement);
      }
      methodCode.write(RETURN);
      out.write(methodCode.size() + 12); // stack size (2) + local var size (2) + code size (4) +
      // exception table size (2) + attribute count size (2)
      out.write((short) 3); // max stack size
      out.write((short) (1 + variables.size())); // max local var size
      out.write(methodCode.size());
      out.write(methodCode.flush());
      out.write((short) 0); // exception table of size 0
      out.write((short) 0); // attribute count for this attribute of 0
    }
  }

  /**
   * generate code to call the appropriate println method for the specified type this will print the
   * top element on the stack
   */
  private DynamicByteArray print(DynamicByteArray out, Type type) {
    if (type.getValue().equals("java.lang.String".toUpperCase())) {
      invokeVirtual(out, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    } else if (type.getValue().equals("INT")) {
      invokeVirtual(out, "java/io/PrintStream", "println", "(I)V");
    } else {
      // TODO: call toString() first
    }
    return out;
  }

  private void processConstants(Expression param) {
    for (Token token : param.getInfix()) {
      if (token instanceof Str) {
        constantPool.addString(token.getValue().toString());
      } else if (token instanceof IntNum) {
        int intValue = (Integer) (token.getValue());
        if (intValue < -32768 || intValue >= 32768) {
          constantPool.addInteger(intValue);
        }
      }
    }
  }

  private void processConstantPool() {
    constantPool = new ConstantPool();
    constantPool.addClass(clazz.getName().getValue().toString());
    constantPool.addClass("java/lang/Object");
    for (Method method : clazz.getBody()) {
      // add method name to constant pool
      constantPool.addUtf8(method.getName().getValue().toString());
      // add type descriptor to constant pool
      constantPool.addUtf8(method.getTypeDescr());
      // add constants from method body to constant pool
      for (Statement statement : method.getBody()) {
        if (statement instanceof MethodCall) {
          MethodCall methodCall = (MethodCall) statement;
          for (Expression param : methodCall.getParameters()) {
            processConstants(param);
          }
        } else if (statement instanceof Declaration) {
          Declaration decl = (Declaration) statement;
          Expression value = decl.getValue();
          if (value != null) {
            processConstants(value);
          }
        }
      }
      constantPool.addUtf8("Code");
    }
  }

  DynamicByteArray pushInteger(DynamicByteArray out, int number) {
    if (number == -1) {
      out.write(ICONST_M1);
    } else if (number == 0) {
      out.write(ICONST_0);
    } else if (number == 1) {
      out.write(ICONST_1);
    } else if (number == 2) {
      out.write(ICONST_2);
    } else if (number == 3) {
      out.write(ICONST_3);
    } else if (number == 4) {
      out.write(ICONST_4);
    } else if (number == 5) {
      out.write(ICONST_5);
    } else if (number >= -128 && number < 128) {
      bipush(out, number);
    } else if (number >= -32768 && number < 32768) {
      sipush(out, number);
    } else {
      ldc(out, CONSTANT_INTEGER, number);
    }
    return out;
  }

  private void sipush(DynamicByteArray out, int number) {
    out.write(SIPUSH);
    out.write((short) number);
  }

  private HasOutput storeInteger(HasOutput out, int idx) {
    if (idx == 0) {
      out.write(ISTORE_0);
    } else if (idx == 1) {
      out.write(ISTORE_1);
    } else if (idx == 2) {
      out.write(ISTORE_2);
    } else if (idx == 3) {
      out.write(ISTORE_3);
    } else {
      out.write(ISTORE);
      out.write((byte) idx);
    }
    return out;
  }

  private void superClassIndex() {
    out.write(constantPool.indexOf(CONSTANT_CLASS, "java/lang/Object"));
  }

  private void supportPrint() {
    // hard-coded support for print
    constantPool.addClass("java/lang/System");
    constantPool.addClass("java/io/PrintStream");
    constantPool.addFieldRef("java/lang/System", "out", "Ljava/io/PrintStream;");
    constantPool.addMethodRef("java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    constantPool.addMethodRef("java/io/PrintStream", "println", "(I)V");
  }
}
