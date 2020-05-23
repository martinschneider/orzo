package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.RETURN;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.kommpeiler.codegen.statement.ConditionalGenerator;
import io.github.martinschneider.kommpeiler.codegen.statement.ExpressionGenerator;
import io.github.martinschneider.kommpeiler.codegen.statement.OpsCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.statement.StatementDelegator;
import io.github.martinschneider.kommpeiler.parser.productions.Argument;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.ReturnStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import java.util.HashMap;
import java.util.List;

public class CodeGenerator {
  private static final short JAVA_CLASS_MAJOR_VERSION = 49;
  private static final short JAVA_CLASS_MINOR_VERSION = 0;
  public static final int INTEGER_DEFAULT_VALUE = 0;
  private CGContext ctx;
  private Output out;

  public CodeGenerator(Clazz clazz, Output out) {
    this.out = out;
    ctx = new CGContext();
    ctx.clazz = clazz;
    ctx.condGenerator = new ConditionalGenerator();
    ctx.constPoolProcessor = new ConstantPoolProcessor();
    ctx.constPool = ctx.constPoolProcessor.processConstantPool(clazz);
    ctx.delegator = new StatementDelegator();
    ctx.exprGenerator = new ExpressionGenerator();
    ctx.methodMap = new MethodProcessor().getMethodMap(clazz);
    ctx.opsGenerator = new OpsCodeGenerator();
    ctx.condGenerator.context = ctx;
    ctx.delegator.context = ctx;
    ctx.exprGenerator.context = ctx;
    ctx.opsGenerator.context = ctx;
    ctx.delegator.init();
  }

  private void accessModifiers() {
    // super + public
    out.write((short) 0x0021);
  }

  private void attributes() {
    out.write((short) 0);
  }

  private void classIndex() {
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, ctx.clazz.getName().getValue()));
  }

  private HasOutput constPool(HasOutput out) {
    out.write(ctx.constPool.getBytes());
    return out;
  }

  private void fields() {
    out.write((short) 0);
  }

  public void generate() {
    supportPrint();
    header();
    HasOutput methods = methods(new DynamicByteArray());
    HasOutput constPool = constPool(new DynamicByteArray());
    out.write(constPool.getBytes());
    accessModifiers();
    classIndex();
    superClassIndex();
    interfaces();
    fields();
    out.write(methods.getBytes());
    attributes();
    out.flush();
  }

  private void generateCode(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    ctx.delegator.generate(variables, out, method, stmt);
  }

  private void header() {
    out.write(0xCAFEBABE);
    out.write(JAVA_CLASS_MINOR_VERSION);
    out.write(JAVA_CLASS_MAJOR_VERSION);
  }

  private void interfaces() {
    out.write((short) 0);
  }

  private HasOutput methods(HasOutput out) {
    List<Method> methods = ctx.clazz.getBody();
    // number of methods
    out.write((short) methods.size());
    for (Method method : methods) {
      // todo: handle global variables
      VariableMap variables = new VariableMap(new HashMap<>());
      for (Argument arg : method.getArguments()) {
        variables.put(
            arg.getName(),
            new VariableInfo(
                arg.getName().getValue().toString(), arg.getType(), (byte) variables.size()));
      }
      out.write((short) 9); // public static
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, method.getName().getValue()));
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, method.getTypeDescr()));
      out.write((short) 1); // attribute size
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "Code"));
      DynamicByteArray methodOut = new DynamicByteArray();
      boolean returned = false;
      for (Statement stmt : method.getBody()) {
        generateCode(methodOut, variables, method, stmt);
        if (stmt instanceof ReturnStatement) {
          returned = true;
        }
      }
      if (!returned) {
        methodOut.write(RETURN);
      }
      out.write(methodOut.size() + 12); // stack size (2) + local var size (2) + code size (4) +
      // exception table size (2) + attribute count size (2)
      // TODO: set this dynamically (for now, 6 allows 3 long/double values)
      out.write((short) 6); // max stack size
      out.write((short) (variables.size())); // max local var size
      out.write(methodOut.size());
      out.write(methodOut.flush());
      out.write((short) 0); // exception table of size 0
      out.write((short) 0); // attribute count for this attribute of 0
    }
    return out;
  }

  private void superClassIndex() {
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, "java/lang/Object"));
  }

  private void supportPrint() {
    // hard-coded support for print
    ctx.constPool.addClass("java/lang/System");
    ctx.constPool.addClass("java/io/PrintStream");
    ctx.constPool.addFieldRef("java/lang/System", "out", "Ljava/io/PrintStream;");
    ctx.constPoolProcessor.addMethodRef(
        ctx.constPool, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    ctx.constPoolProcessor.addMethodRef(ctx.constPool, "java/io/PrintStream", "println", "(I)V");
    ctx.constPoolProcessor.addMethodRef(ctx.constPool, "java/io/PrintStream", "println", "(J)V");
  }
}
