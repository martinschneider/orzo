package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GOTO;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.RETURN;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_UTF8;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Argument;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.DoStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.ForStatement;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.ParallelAssignment;
import io.github.martinschneider.kommpeiler.parser.productions.Return;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.Type;
import io.github.martinschneider.kommpeiler.parser.productions.WhileStatement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {
  private static final short JAVA_CLASS_MAJOR_VERSION = 49;
  private static final short JAVA_CLASS_MINOR_VERSION = 0;
  private static final int INTEGER_DEFAULT_VALUE = 0;
  private Clazz clazz;
  private ConstantPool constantPool;
  private StackCodeGenerator scg;
  private ExpressionCodeGenerator ecg;
  private ConditionalCodeGenerator ccg;
  private ConstantPoolProcessor cpp;
  private MethodProcessor methodProcessor;
  private Map<String, Method> methodMap;
  private Output out;

  public CodeGenerator(Clazz clazz, Output out) {
    this.clazz = clazz;
    this.out = out;
    ccg = new ConditionalCodeGenerator();
    cpp = new ConstantPoolProcessor();
    ecg = new ExpressionCodeGenerator();
    constantPool = cpp.processConstantPool(clazz);
    scg = new StackCodeGenerator(constantPool);
    ccg.setExpressionCodeGenerator(ecg);
    methodProcessor = new MethodProcessor();
    methodMap = methodProcessor.getMethodMap(clazz);
    ecg.setStackCodeGenerator(clazz, constantPool, methodMap, scg);
    scg.setExpressionCodeGenerator(ecg);
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
    constantPool = cpp.processConstantPool(clazz);
    methodMap = methodProcessor.getMethodMap(clazz);
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
    if (statement instanceof Declaration) {
      Declaration decl = (Declaration) statement;
      if (decl.hasValue()) {
        ecg.evaluateExpression(out, variables, decl.getValue());
      } else {
        if (decl.getType().eq(type(INT))) {
          scg.pushInteger(out, constantPool, INTEGER_DEFAULT_VALUE);
        }
      }
      scg.assignValue(out, variables, decl.getType(), decl.getName());
    } else if (statement instanceof Assignment) {
      Assignment assignment = (Assignment) statement;
      // TODO: store type in variable map
      // TODO: type conversion
      ExpressionResult result = ecg.evaluateExpression(out, variables, assignment.getRight());
      scg.assignValue(out, variables, result.getType(), assignment.getLeft());
    } else if (statement instanceof ParallelAssignment) {
      ParallelAssignment assignment = (ParallelAssignment) statement;
      int tmpCount = 0;
      for (int i = 0; i < assignment.getLeft().size(); i++) {
        Identifier left = assignment.getLeft().get(i);
        Expression right = assignment.getRight().get(i);
        byte leftIdx = variables.get(left).byteValue();
        ecg.evaluateExpression(out, variables, right);
        // TODO: replace recursively
        if (assignment.getRight().contains(new Expression(List.of(left)))) {
          Identifier tmpId = id("tmp_" + tmpCount);
          assignment.getRight().replaceAll(x -> new Expression(List.of(tmpId)));
          byte tmpIdx =
              variables.computeIfAbsent(id("tmp_" + tmpCount), x -> variables.size()).byteValue();
          scg.loadInteger(out, leftIdx);
          scg.storeInteger(out, tmpIdx);
        }
        scg.storeInteger(out, leftIdx);
      }
    } else if (statement instanceof MethodCall) {
      MethodCall methodCall = (MethodCall) statement;
      if ("System.out.println".equals(methodCall.getQualifiedName())) {
        for (Expression param : methodCall.getParameters()) {
          scg.getStatic(out, constantPool, "java/lang/System", "out", "Ljava/io/PrintStream;");
          ExpressionResult result = ecg.evaluateExpression(out, variables, param);
          print(out, result.getType());
        }
      } else {
        String methodName =
            methodCall.getNames().get(methodCall.getNames().size() - 1).getValue().toString();
        for (Expression exp : methodCall.getParameters()) {
          ecg.evaluateExpression(out, variables, exp);
        }
        scg.invokeStatic(
            out,
            constantPool,
            clazz.getName().getValue().toString(),
            methodName,
            methodMap.get(methodName).getTypeDescr());
      }
    } else if (statement instanceof IfStatement) {
      IfStatement ifStatement = (IfStatement) statement;
      DynamicByteArray bodyOut = new DynamicByteArray();
      for (Statement stmt : ifStatement.getBody()) {
        generateCode(variables, bodyOut, stmt, method, clazz);
      }
      DynamicByteArray conditionOut = new DynamicByteArray();
      short branchBytes = (short) (3 + bodyOut.getBytes().length);
      ccg.generateCondition(
          conditionOut, clazz, variables, constantPool, ifStatement.getCondition(), branchBytes);
      out.write(conditionOut.getBytes());
      out.write(bodyOut.getBytes());
    } else if (statement instanceof WhileStatement) {
      WhileStatement whileStatement = (WhileStatement) statement;
      DynamicByteArray bodyOut = new DynamicByteArray();
      for (Statement stmt : whileStatement.getBody()) {
        generateCode(variables, bodyOut, stmt, method, clazz);
      }
      DynamicByteArray conditionOut = new DynamicByteArray();
      short branchBytes = (short) (3 + bodyOut.getBytes().length + 3);
      ccg.generateCondition(
          conditionOut, clazz, variables, constantPool, whileStatement.getCondition(), branchBytes);
      out.write(conditionOut.getBytes());
      out.write(bodyOut.getBytes());
      out.write(GOTO);
      out.write(shortToByteArray(-(bodyOut.getBytes().length + conditionOut.getBytes().length)));
    } else if (statement instanceof DoStatement) {
      DoStatement doStatement = (DoStatement) statement;
      DynamicByteArray bodyOut = new DynamicByteArray();
      for (Statement stmt : doStatement.getBody()) {
        generateCode(variables, bodyOut, stmt, method, clazz);
      }
      DynamicByteArray conditionOut = new DynamicByteArray();
      short branchBytes = (short) -(bodyOut.getBytes().length + conditionOut.getBytes().length);
      ccg.generateCondition(
          conditionOut,
          clazz,
          variables,
          constantPool,
          doStatement.getCondition(),
          branchBytes,
          true);
      out.write(bodyOut.getBytes());
      out.write(conditionOut.getBytes());
    } else if (statement instanceof ForStatement) {
      ForStatement forStatement = (ForStatement) statement;
      generateCode(variables, out, forStatement.getInitialization(), method, clazz);
      DynamicByteArray bodyOut = new DynamicByteArray();
      for (Statement stmt : forStatement.getBody()) {
        generateCode(variables, bodyOut, stmt, method, clazz);
      }
      generateCode(variables, bodyOut, forStatement.getLoopStatement(), method, clazz);
      DynamicByteArray conditionOut = new DynamicByteArray();
      short branchBytes = (short) (3 + bodyOut.getBytes().length + 3);
      ccg.generateCondition(
          conditionOut, clazz, variables, constantPool, forStatement.getCondition(), branchBytes);
      out.write(conditionOut.getBytes());
      out.write(bodyOut.getBytes());
      out.write(GOTO);
      out.write(shortToByteArray(-(bodyOut.getBytes().length + conditionOut.getBytes().length)));
    } else if (statement instanceof Return) {
      Return returnStatement = (Return) statement;
      scg.ret(
          out,
          clazz,
          variables,
          constantPool,
          method.getType().getValue().toString(),
          returnStatement.getRetValue());
    }
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
        if (statement instanceof Return) {
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

  /**
   * generate code to call the appropriate println method for the specified type this will print the
   * top element on the stack
   */
  private DynamicByteArray print(DynamicByteArray out, Type type) {
    if (type.getValue().equals("java.lang.String".toUpperCase())) {
      scg.invokeVirtual(
          out, constantPool, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    } else if (type.getValue().equals("INT")) {
      scg.invokeVirtual(out, constantPool, "java/io/PrintStream", "println", "(I)V");
    } else {
      // TODO: call toString() first
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
    cpp.addMethodRef(constantPool, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    cpp.addMethodRef(constantPool, "java/io/PrintStream", "println", "(I)V");
  }
}
