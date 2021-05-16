package io.github.martinschneider.orzo.codegen.generators;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.*;
import java.util.HashMap;
import java.util.Map;

public class StatementDelegator {
  public Map<Class<? extends Statement>, StatementGenerator> reg = new HashMap<>();
  public CGContext ctx;

  public void init() {
    reg.put(ParallelDeclaration.class, new DeclarationGenerator(ctx));
    reg.put(DoStatement.class, new DoGenerator(ctx));
    reg.put(ForStatement.class, new ForGenerator(ctx));
    reg.put(IfStatement.class, new IfGenerator(ctx));
    reg.put(MethodCall.class, new MethodCallGenerator(ctx));
    reg.put(Assignment.class, new AssignmentGenerator(ctx));
    reg.put(ReturnStatement.class, new RetGenerator(ctx));
    reg.put(WhileStatement.class, new WhileGenerator(ctx));
    reg.put(IncrementStatement.class, new IncrementGenerator(ctx));
    reg.put(EmptyStatement.class, new DoNothingGenerator());
  }

  public HasOutput generate(
      VariableMap variables, DynamicByteArray out, Method method, Statement stmt) {
    return (stmt != null) ? reg.get(stmt.getClass()).generate(out, variables, method, stmt) : out;
  }
}
