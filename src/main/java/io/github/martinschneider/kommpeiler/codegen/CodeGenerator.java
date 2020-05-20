package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.RETURN;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.codegen.statement.StatementGeneratorDelegator;
import io.github.martinschneider.kommpeiler.parser.productions.Argument;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.ReturnStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {
  private static final short JAVA_CLASS_MAJOR_VERSION = 49;
  private static final short JAVA_CLASS_MINOR_VERSION = 0;
  public static final int INTEGER_DEFAULT_VALUE = 0;
  private Clazz clazz;
  private ConstantPool constantPool;
  private StatementGeneratorDelegator delegator;
  private OpsCodeGenerator opsCodeGenerator;
  private ExpressionCodeGenerator expressionCodeGenerator;
  private ConditionalCodeGenerator conditionalCodeGenerator;
  private ConstantPoolProcessor constantPoolProcessor;
  private MethodProcessor methodProcessor;
  private Map<String, Method> methodMap;
  private Output out;

  public CodeGenerator(Clazz clazz, Output out) {
    this.clazz = clazz;
    this.out = out;
    constantPoolProcessor = new ConstantPoolProcessor();
    constantPool = constantPoolProcessor.processConstantPool(clazz);
    conditionalCodeGenerator = new ConditionalCodeGenerator();
    opsCodeGenerator = new OpsCodeGenerator(constantPool);
    methodProcessor = new MethodProcessor();
    methodMap = methodProcessor.getMethodMap(clazz);
    expressionCodeGenerator =
        new ExpressionCodeGenerator(clazz, constantPool, methodMap, opsCodeGenerator);
    opsCodeGenerator.setExpressionCodeGenerator(expressionCodeGenerator);
    conditionalCodeGenerator.setExpressionCodeGenerator(expressionCodeGenerator);
    delegator =
        new StatementGeneratorDelegator(
            expressionCodeGenerator,
            opsCodeGenerator,
            conditionalCodeGenerator,
            constantPool,
            methodMap);
  }

  private void accessModifiers() {
    // super + public
    out.write((short) 0x0021);
  }

  private void attributes() {
    out.write((short) 0);
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
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement statement,
      Method method,
      Clazz clazz) {
    delegator.generate(variables, out, statement, method, clazz);
  }

  private void header() {
    out.write(0xCAFEBABE);
    out.write(JAVA_CLASS_MINOR_VERSION);
    out.write(JAVA_CLASS_MAJOR_VERSION);
  }

  private void interfaces() {
    out.write((short) 0);
  }

  private void methods() {
    List<Method> methods = clazz.getBody();
    // number of methods
    out.write((short) methods.size());
    for (Method method : methods) {
      // todo: handle global variables
      Map<Identifier, Integer> variables = new HashMap<>();
      for (Argument arg : method.getArguments()) {
        variables.put(arg.getName(), variables.size());
      }
      out.write((short) 9); // public static
      out.write(constantPool.indexOf(CONSTANT_UTF8, method.getName().getValue()));
      out.write(constantPool.indexOf(CONSTANT_UTF8, method.getTypeDescr()));
      out.write((short) 1); // attribute size
      out.write(constantPool.indexOf(CONSTANT_UTF8, "Code"));
      DynamicByteArray methodCode = new DynamicByteArray();
      boolean returned = false;
      for (Statement statement : method.getBody()) {
        generateCode(variables, methodCode, statement, method, clazz);
        if (statement instanceof ReturnStatement) {
          returned = true;
        }
      }
      if (!returned) {
        methodCode.write(RETURN);
      }
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

  private void superClassIndex() {
    out.write(constantPool.indexOf(CONSTANT_CLASS, "java/lang/Object"));
  }

  private void supportPrint() {
    // hard-coded support for print
    constantPool.addClass("java/lang/System");
    constantPool.addClass("java/io/PrintStream");
    constantPool.addFieldRef("java/lang/System", "out", "Ljava/io/PrintStream;");
    constantPoolProcessor.addMethodRef(
        constantPool, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    constantPoolProcessor.addMethodRef(constantPool, "java/io/PrintStream", "println", "(I)V");
  }
}
