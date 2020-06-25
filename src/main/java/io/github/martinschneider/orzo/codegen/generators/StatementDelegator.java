package io.github.martinschneider.orzo.codegen.generators;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.Increment;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;
import java.util.HashMap;
import java.util.Map;

public class StatementDelegator {
  public Map<Class<? extends Statement>, StatementGenerator> reg = new HashMap<>();
  public CGContext ctx;

  public void init() {
    reg.put(ParallelDeclaration.class, new DeclarationGenerator(ctx));
    reg.put(DoStatement.class, new DoStatementGenerator(ctx));
    reg.put(ForStatement.class, new ForStatementGenerator(ctx));
    reg.put(IfStatement.class, new IfStatementGenerator(ctx));
    reg.put(MethodCall.class, new MethodCallGenerator(ctx));
    reg.put(Assignment.class, new AssignmentGenerator(ctx));
    reg.put(ReturnStatement.class, new RetGenerator(ctx));
    reg.put(WhileStatement.class, new WhileStatementGenerator(ctx));
    reg.put(Increment.class, new IncrementGenerator(ctx));
  }

  public HasOutput generate(
      VariableMap variables, DynamicByteArray out, Method method, Statement stmt) {
    return reg.get(stmt.getClass()).generate(out, variables, method, stmt);
  }
}
