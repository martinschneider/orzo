package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.OpCodes.RETURN;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_UTF8;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;

import io.github.martinschneider.orzo.codegen.generators.AssignmentGenerator;
import io.github.martinschneider.orzo.codegen.generators.BasicGenerator;
import io.github.martinschneider.orzo.codegen.generators.ConditionGenerator;
import io.github.martinschneider.orzo.codegen.generators.ExpressionGenerator;
import io.github.martinschneider.orzo.codegen.generators.IncrementGenerator;
import io.github.martinschneider.orzo.codegen.generators.InvokeGenerator;
import io.github.martinschneider.orzo.codegen.generators.LoadGenerator;
import io.github.martinschneider.orzo.codegen.generators.MethodCallGenerator;
import io.github.martinschneider.orzo.codegen.generators.PushGenerator;
import io.github.martinschneider.orzo.codegen.generators.StatementDelegator;
import io.github.martinschneider.orzo.codegen.generators.StoreGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
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
  private List<Output> outputs;
  private List<Clazz> clazzes;
  private CompilerErrors errors;
  private Output out;

  public CodeGenerator(List<Clazz> clazzes, List<Output> outputs, CompilerErrors errors) {
    this.outputs = outputs;
    this.clazzes = clazzes;
    this.errors = errors;
    ctx = new CGContext();
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
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, ctx.clazz.fqn('/')));
  }

  private HasOutput constPool(HasOutput out) {
    out.write(ctx.constPool.getBytes());
    return out;
  }

  private void fields() {
    out.write((short) 0);
  }

  private void init(int idx) {
    out = outputs.get(idx);
    ctx.clazz = clazzes.get(idx);
    ctx.errors = errors;
    ctx.condGenerator = new ConditionGenerator();
    ctx.constPoolProc = new ConstantPoolProcessor();
    ctx.constPool = ctx.constPoolProc.processConstantPool(ctx.clazz);
    ctx.delegator = new StatementDelegator();
    ctx.exprGen = new ExpressionGenerator();
    ctx.incrGen = new IncrementGenerator(ctx);
    ctx.methodCallGen = new MethodCallGenerator(ctx);
    ctx.methodMap = new MethodProcessor().getMethodMap(ctx.clazz, clazzes);
    ctx.assignGen = new AssignmentGenerator(ctx);
    ctx.basicGen = new BasicGenerator(ctx);
    ctx.invokeGen = new InvokeGenerator(ctx);
    ctx.pushGen = new PushGenerator(ctx);
    ctx.loadGen = new LoadGenerator(ctx);
    ctx.storeGen = new StoreGenerator(ctx);
    ctx.condGenerator.ctx = ctx;
    ctx.delegator.ctx = ctx;
    ctx.exprGen.ctx = ctx;
    ctx.delegator.init();
  }

  public void generate() {
    for (int i = 0; i < clazzes.size(); i++) {
      init(i);
      header();
      supportPrint();
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
    ctx.opStack = new OperandStack();
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
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, TypeUtils.methodDescr(method)));
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
      out.write((short) (ctx.opStack.maxSize() + 1)); // max stack size
      out.write((short) (variables.size + 1)); // max local var size
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
  }
}
