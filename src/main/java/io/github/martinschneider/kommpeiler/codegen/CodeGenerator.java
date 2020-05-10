package io.github.martinschneider.kommpeiler.codegen;

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
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.INVOKEVIRTUAL;
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

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.ExpressionType;
import io.github.martinschneider.kommpeiler.parser.productions.Factor;
import io.github.martinschneider.kommpeiler.parser.productions.IdFactor;
import io.github.martinschneider.kommpeiler.parser.productions.IntFactor;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.SimpleExpression;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.StringFactor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {
  private static final short JAVA_CLASS_MAJOR_VERSION = 50;
  private static final short JAVA_CLASS_MINOR_VERSION = 0;
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

  private void addition(
      Map<Variable, Integer> variables, DynamicByteArray out, String left, String right) {
    loadInteger(out, variables.get(new Variable("INT", left)));
    loadInteger(out, variables.get(new Variable("INT", right)));
    out.write(IADD);
  }

  private void assignInteger(
      Map<Variable, Integer> variables, DynamicByteArray out, String varName) {
    int index = variables.size();
    storeInteger(out, index);
    variables.put(new Variable("INT", varName), index);
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
      Map<Variable, Integer> variables, DynamicByteArray out, Statement statement) {
    if (statement instanceof Declaration) {
      Declaration decl = (Declaration) statement;
      if (decl.getType().equals("INT")) {
        if (decl.getValue() instanceof SimpleExpression) {
          SimpleExpression expression = (SimpleExpression) decl.getValue();
          if (expression.getType().equals(ExpressionType.VALUE)) {
            pushInteger(out, (Integer) expression.getValue());
            assignInteger(variables, out, decl.getName().getValue());
          } else if (expression.getType().equals(ExpressionType.VARIABLE_AND_VARIABLE)) {
            if (expression.getOperator().getValue().equals("PLUS")) {
              addition(
                  variables,
                  out,
                  ((IdFactor) expression.getLeft()).getToken().toString(),
                  ((IdFactor) expression.getRight()).getToken().toString());
            } else if (expression.getOperator().getValue().equals("MINUS")) {
              subtraction(
                  variables,
                  out,
                  ((IdFactor) expression.getLeft()).getToken().toString(),
                  ((IdFactor) expression.getRight()).getToken().toString());
            }
            assignInteger(variables, out, decl.getName().getValue());
          }
        } else if (decl.getValue() instanceof IntFactor) {
          pushInteger(out, ((IntFactor) decl.getValue()).getIntValue());
          assignInteger(variables, out, decl.getName().getValue());
        }
      }
    } else if (statement instanceof MethodCall) {
      MethodCall methodCall = (MethodCall) statement;
      if ("System.out.println".equals(methodCall.getQualifiedName())) {
        for (Factor param : methodCall.getParameters()) {
          print(out, variables, param);
        }
      }
    }
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
      Map<Variable, Integer> variables = new HashMap<>();
      out.write((short) 9); // public static
      out.write(constantPool.indexOf(CONSTANT_UTF8, method.getName().getValue()));
      out.write(constantPool.indexOf(CONSTANT_UTF8, method.getTypeDescr()));
      out.write((short) 1); // attribute size
      out.write(constantPool.indexOf(CONSTANT_UTF8, "Code"));
      DynamicByteArray code = new DynamicByteArray();
      for (Statement statement : method.getBody()) {
        generateCode(variables, code, statement);
      }
      code.write(RETURN);
      out.write(code.size() + 12); // stack size (2) + local var size (2) + code size (4) +
      // exception table size (2) + attribute count size (2)
      out.write((short) 2); // max stack size
      out.write((short) (1 + variables.size())); // max local var size
      out.write(code.size());
      out.write(code.flush());
      out.write((short) 0); // exception table of size 0
      out.write((short) 0); // attribute count for this attribute of 0
    }
  }

  private DynamicByteArray print(
      DynamicByteArray out, Map<Variable, Integer> variables, Factor param) {
    getStatic(out, "java/lang/System", "out", "Ljava/io/PrintStream;");
    if (param instanceof StringFactor) {
      ldc(out, CONSTANT_STRING, ((StringFactor) param).getValue());
      invokeVirtual(out, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    } else if (param instanceof IntFactor) {
      int number = (((IntFactor) param).getIntValue());
      pushInteger(out, number);
      invokeVirtual(out, "java/io/PrintStream", "println", "(I)V");
    } else if (param instanceof IdFactor) {
      IdFactor idFactor = (IdFactor) param;
      // todo: support other types
      loadInteger(out, variables.get(new Variable("INT", idFactor.getToken().getValue())));
      invokeVirtual(out, "java/io/PrintStream", "println", "(I)V");
    }
    return out;
  }

  private void processConstant(Factor param) {
    if (param instanceof StringFactor) {
      StringFactor strFactor = (StringFactor) param;
      constantPool.addString(strFactor.getValue());
    } else if (param instanceof IntFactor) {
      IntFactor intFactor = (IntFactor) param;
      int intValue = intFactor.getIntValue();
      if (intValue < -32768 || intValue >= 32768) {
        constantPool.addInteger(intValue);
      }
    }
  }

  private void processConstantPool() {
    constantPool = new ConstantPool();
    constantPool.addClass(clazz.getName().getValue());
    constantPool.addClass("java/lang/Object");
    for (Method method : clazz.getBody()) {
      // add method name to constant pool
      constantPool.addUtf8(method.getName().getValue());
      // add type descriptor to constant pool
      constantPool.addUtf8(method.getTypeDescr());
      // add constants from method body to constant pool
      for (Statement statement : method.getBody()) {
        if (statement instanceof MethodCall) {
          MethodCall methodCall = (MethodCall) statement;
          for (Factor param : methodCall.getParameters()) {
            processConstant(param);
          }
        } else if (statement instanceof Declaration) {
          Declaration decl = (Declaration) statement;
          Factor value = decl.getValue();
          if (value != null) {
            processConstant(value);
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

  private DynamicByteArray storeInteger(DynamicByteArray out, int idx) {
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

  private void subtraction(
      Map<Variable, Integer> variables, DynamicByteArray out, String left, String right) {
    loadInteger(out, variables.get(new Variable("INT", left)));
    loadInteger(out, variables.get(new Variable("INT", right)));
    out.write(ISUB);
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
