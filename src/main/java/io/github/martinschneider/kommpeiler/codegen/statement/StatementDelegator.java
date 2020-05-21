package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.DoStatement;
import io.github.martinschneider.kommpeiler.parser.productions.ForStatement;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.ParallelAssignment;
import io.github.martinschneider.kommpeiler.parser.productions.ReturnStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.WhileStatement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.HashMap;
import java.util.Map;

public class StatementDelegator {
  private Map<Class<? extends Statement>, StatementGenerator> registry = new HashMap<>();
  public CGContext context;

  public void init() {
    registry.put(Assignment.class, new AssignmentGenerator(context));
    registry.put(Declaration.class, new DeclarationGenerator(context));
    registry.put(DoStatement.class, new DoStatementGenerator(context));
    registry.put(ForStatement.class, new ForStatementGenerator(context));
    registry.put(IfStatement.class, new IfStatementGenerator(context));
    registry.put(MethodCall.class, new MethodCallGenerator(context));
    registry.put(ParallelAssignment.class, new ParallelAssignmentGenerator(context));
    registry.put(ReturnStatement.class, new ReturnStatementGenerator(context));
    registry.put(WhileStatement.class, new WhileStatementGenerator(context));
  }

  public HasOutput generate(
      Map<Identifier, VariableInfo> variables,
      DynamicByteArray out,
      Method method,
      Statement stmt) {
    return registry.get(stmt.getClass()).generate(out, variables, method, stmt);
  }
}
