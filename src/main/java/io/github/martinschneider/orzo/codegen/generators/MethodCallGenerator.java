package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;
import static io.github.martinschneider.orzo.util.FactoryHelper.defaultConstr;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.util.ArrayList;
import java.util.List;

public class MethodCallGenerator implements StatementGenerator<MethodCall> {
  private static final String LOGGER_NAME = "method call code generator";
  public CGContext ctx;

  public MethodCallGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public String generate(DynamicByteArray out, VariableMap variables, MethodCall methodCall) {
    List<String> types = new ArrayList<>();
    for (Expression exp : methodCall.params) {
      types.add(new NumExprTypeDecider(ctx).getType(variables, exp));
    }
    String methodName = methodCall.name.toString();
    Method method = findMatchingMethod(methodName, types);
    if (method == null) {
      ctx.errors.addError(
          LOGGER_NAME,
          methodCall.loc.toString() + " missing method declaration \"" + methodName + types + "\"");
      return "";
    }
    for (int i = 0; i < types.size(); i++) {
      ExpressionResult exprResult =
          ctx.exprGen.eval(out, variables, method.args.get(i).type, methodCall.params.get(i));
      ctx.basicGen.convert1(out, exprResult.type, method.args.get(i).type);
    }
    ctx.invokeGen.invokeStatic(out, method);
    return method.type;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, MethodCall methodCall) {
    if ("super".equals(methodCall.name.toString())) {
      callSuperConstr(out);
    } else if ("System.out.println".equals(methodCall.name.toString())) {
      for (Expression param : methodCall.params) {
        ctx.invokeGen.getStatic(out, "java/lang/System", "out", "Ljava/io/PrintStream;");
        ExpressionResult result = ctx.exprGen.eval(out, variables, null, param);
        if (result != null) {
          print(out, result.type);
        } else {
          ctx.errors.addError(LOGGER_NAME, "error evaluating expression " + param);
          return null;
        }
      }
    } else {
      generate(out, variables, methodCall);
    }
    return out;
  }

  public HasOutput callSuperConstr(HasOutput out) {
    ctx.loadGen.loadReference(out, (short) 0);
    ctx.opStack.push(SHORT);
    ctx.invokeGen.invokeSpecial(out, defaultConstr("java/lang/Object"));
    return out;
  }

  /**
   * generate code to call the appropriate println method for the specified type this will print the
   * top element on the stack
   */
  private DynamicByteArray print(DynamicByteArray out, String type) {
    if (type.equals(STRING)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of("Ljava/lang/String;")));
    } else if (type.equals(INT) || type.equals(BYTE) || type.equals(SHORT)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(INT)));
    } else if (type.equals(LONG)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(LONG)));
    } else if (type.equals(DOUBLE)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(DOUBLE)));
    } else if (type.equals(FLOAT)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(FLOAT)));
    } else if (type.equals(CHAR)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(CHAR)));
    } else if (type.equals(BOOLEAN)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(BOOLEAN)));
    } else {
      // TODO: call toString() first
    }
    return out;
  }

  public Method findMatchingMethod(String methodName, List<String> types) {
    List<List<String>> typesList = new ArrayList<>();
    for (int i = 0; i < types.size(); i++) {
      typesList.add(TypeUtils.assignableTo(types.get(i)));
    }
    Method method = null;
    for (List<String> assignTypes : TypeUtils.combinations(typesList)) {
      String methodKey = methodName + TypeUtils.typesDescr(assignTypes);
      method = ctx.methodMap.get(methodKey);
      if (method != null) {
        break;
      }
    }
    return method;
  }
}
