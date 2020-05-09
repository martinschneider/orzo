package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GETSTATIC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_4;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_5;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_M1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.INVOKEVIRTUAL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LDC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.RETURNVOID;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.SIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Factor;
import io.github.martinschneider.kommpeiler.parser.productions.IntFactor;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.StringFactor;
import java.util.List;

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

  private void supportPrint() {
    // hard-coded support for print
    constantPool.addClass("java/lang/System");
    constantPool.addClass("java/io/PrintStream");
    constantPool.addFieldRef("java/lang/System", "out", "Ljava/io/PrintStream;");
    constantPool.addMethodRef("java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    constantPool.addMethodRef("java/io/PrintStream", "println", "(I)V");
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
            if (param instanceof StringFactor) {
              StringFactor strFactor = (StringFactor) param;
              constantPool.addString(strFactor.getValue());
            } else if (param instanceof IntFactor) {
              IntFactor intFactor = (IntFactor) param;
              constantPool.addInteger(intFactor.getIntValue());
            }
          }
        }
      }
      constantPool.addUtf8("Code");
    }
  }

  private void accessModifiers() {
    // ACC_PUBLIC 0x0001
    // ACC_FINAL 0x0010
    // ACC_SUPER 0x0020 ( Not final, can be extended )
    // ACC_INTERFACE 0x0200
    // ACC_ABSTRACT 0x0400
    // ACC_SYNTHETIC 0x1000 ( Not present in source code. Generated )
    // ACC_ANNOTATION 0x2000
    // ACC_ENUM 0x4000

    // super + public
    out.write((short) 0x0021);
  }

  private void header() {
    out.write(0xCAFEBABE);
    out.write(JAVA_CLASS_MINOR_VERSION);
    out.write(JAVA_CLASS_MAJOR_VERSION);
  }

  private void constantPool() {
    out.write(constantPool.getBytes());
  }

  private void classIndex() {
    out.write(constantPool.indexOf(CONSTANT_CLASS, clazz.getName().getValue()));
  }

  private void superClassIndex() {
    out.write(constantPool.indexOf(CONSTANT_CLASS, "java/lang/Object"));
  }

  private void interfaces() {
    out.write((short) 0);
  }

  private void fields() {
    out.write((short) 0);
  }

  private void methods() {
    List<Method> methods = clazz.getBody();

    // number of methods
    out.write((short) methods.size());

    for (Method method : methods) {
      out.write((short) 9); // public static
      out.write(constantPool.indexOf(CONSTANT_UTF8, method.getName().getValue()));
      out.write(constantPool.indexOf(CONSTANT_UTF8, method.getTypeDescr()));
      out.write((short) 1); // attribute size
      out.write(constantPool.indexOf(CONSTANT_UTF8, "Code"));
      DynamicByteArray code = new DynamicByteArray();
      for (Statement statement : method.getBody()) {
        generateCode(code, statement);
      }
      code.write(RETURNVOID);
      out.write(code.size() + 12); // stack size (2) + local var size (2) + code size (4) +
      // exception table size (2) + attribute count size (2)
      out.write((short) 2); // max stack size
      out.write((short) 1); // max local var size
      out.write(code.size());
      out.write(code.flush());
      out.write((short) 0); // exception table of size 0
      out.write((short) 0); // attribute count for this attribute of 0
    }
  }

  private void generateCode(DynamicByteArray out, Statement statement) {
    if (statement instanceof MethodCall) {
      MethodCall methodCall = (MethodCall) statement;
      if ("System.out.println".equals(methodCall.getQualifiedName())) {
        for (Factor param : methodCall.getParameters()) {
          print(out, param);
        }
      }
    }
  }

  private void attributes() {
    out.write((short) 0);
  }

  private DynamicByteArray print(DynamicByteArray out, Factor param) {
    getStatic(out, "java/lang/System", "out", "Ljava/io/PrintStream;");
    if (param instanceof StringFactor) {
      ldc(out, CONSTANT_STRING, ((StringFactor) param).getValue());
      invokeVirtual(out, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    } else if (param instanceof IntFactor) {
      int number = (((IntFactor) param).getIntValue());
      pushInteger(out, number);
      invokeVirtual(out, "java/io/PrintStream", "println", "(I)V");
    }
    return out;
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

  private void bipush(DynamicByteArray out, int number) {
    out.write(BIPUSH);
    out.write((byte) number);
  }

  private void sipush(DynamicByteArray out, int number) {
    out.write(SIPUSH);
    out.write((short) number);
  }

  private DynamicByteArray getStatic(
      DynamicByteArray out, String clazz, String field, String type) {
    out.write(GETSTATIC);
    out.write(constantPool.indexOf(CONSTANT_FIELDREF, clazz, field, type));
    return out;
  }

  private DynamicByteArray ldc(DynamicByteArray out, byte type, Object key) {
    out.write(LDC);
    out.write((byte) constantPool.indexOf(type, key));
    return out;
  }

  private DynamicByteArray invokeVirtual(
      DynamicByteArray out, String clazz, String field, String type) {
    out.write(INVOKEVIRTUAL);
    out.write(constantPool.indexOf(CONSTANT_METHODREF, clazz, field, type));
    return out;
  }
}
