package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.OpCodes.RETURN;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_UTF8;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;

import io.github.martinschneider.orzo.codegen.statement.ConditionalGenerator;
import io.github.martinschneider.orzo.codegen.statement.ExpressionGenerator;
import io.github.martinschneider.orzo.codegen.statement.OpCodeGenerator;
import io.github.martinschneider.orzo.codegen.statement.StatementDelegator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.HashMap;
import java.util.List;

public class CodeGenerator {
  private static final short JAVA_CLASS_MAJOR_VERSION = 49;
  private static final short JAVA_CLASS_MINOR_VERSION = 0;
  private CGContext ctx;
  private Output out;

  public CodeGenerator(Clazz clazz, Output out, ParserContext parserCtx) {
    this.out = out;
    ctx = new CGContext();
    ctx.clazz = clazz;
    ctx.errors = parserCtx.errors;
    ctx.condGenerator = new ConditionalGenerator();
    ctx.constPoolProcessor = new ConstantPoolProcessor();
    ctx.constPool = ctx.constPoolProcessor.processConstantPool(clazz);
    ctx.delegator = new StatementDelegator();
    ctx.exprGenerator = new ExpressionGenerator();
    ctx.methodMap = new MethodProcessor().getMethodMap(clazz);
    ctx.opsGenerator = new OpCodeGenerator();
    ctx.parserCtx = parserCtx;
    ctx.condGenerator.ctx = ctx;
    ctx.delegator.ctx = ctx;
    ctx.exprGenerator.ctx = ctx;
    ctx.opsGenerator.ctx = ctx;
    ctx.delegator.init();
  }

  public CompilerErrors getErrors() {
    return ctx.errors;
  }

  private void accessModifiers() {
    // super + public
    out.write((short) 0x0021);
  }

  private void attributes() {
    out.write((short) 0);
  }

  private void classIndex() {
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, ctx.clazz.name.val));
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
    List<Method> methods = ctx.clazz.body;
    // number of methods
    out.write((short) methods.size());
    for (Method method : methods) {
      // TODO: handle global variables
      VariableMap variables = new VariableMap(new HashMap<>());
      for (Argument arg : method.args) {
        // TODO: this code is ugly
        String type = arg.type;
        String arrayType = null;
        if (arg.type.startsWith("[")) {
          type = REF;
          arrayType = arg.type.replaceAll("\\[", "");
        }
        variables.put(
            arg.name,
            new VariableInfo(arg.name.val.toString(), type, arrayType, (byte) variables.size));
      }
      out.write((short) 9); // public static
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, method.name.val));
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, method.getTypeDescr()));
      out.write((short) 1); // attribute size
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "Code"));
      DynamicByteArray methodOut = new DynamicByteArray();
      boolean returned = false;
      for (Statement stmt : method.body) {
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
      // TODO: set this dynamically
      out.write((short) 100); // max stack size
      out.write((short) (variables.size)); // max local var size
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
    ctx.constPoolProcessor.addMethodRef(ctx.constPool, "java/io/PrintStream", "println", "(D)V");
    ctx.constPoolProcessor.addMethodRef(ctx.constPool, "java/io/PrintStream", "println", "(F)V");
  }
}