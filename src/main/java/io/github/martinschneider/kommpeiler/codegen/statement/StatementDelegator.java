package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
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
  private Map<Class<? extends Statement>, StatementGenerator> stmtGeneratorRegistry;

  public StatementDelegator(
      ExpressionGenerator expressionCodeGenerator,
      OpsCodeGenerator opsCodeGenerator,
      ConditionalGenerator conditionalCodeGenerator,
      ConstantPool constPool,
      Map<String, Method> methodMap) {
    stmtGeneratorRegistry = new HashMap<>();
    stmtGeneratorRegistry.put(
        Declaration.class,
        new DeclarationGenerator(expressionCodeGenerator, opsCodeGenerator, constPool));
    stmtGeneratorRegistry.put(
        Assignment.class, new AssignmentGenerator(expressionCodeGenerator, opsCodeGenerator));
    stmtGeneratorRegistry.put(
        ParallelAssignment.class,
        new ParallelAssignmentGenerator(expressionCodeGenerator, opsCodeGenerator));
    stmtGeneratorRegistry.put(
        MethodCall.class,
        new MethodCallGenerator(expressionCodeGenerator, opsCodeGenerator, constPool, methodMap));
    stmtGeneratorRegistry.put(
        IfStatement.class, new IfStatementGenerator(this, conditionalCodeGenerator, constPool));
    stmtGeneratorRegistry.put(
        WhileStatement.class,
        new WhileStatementGenerator(this, conditionalCodeGenerator, constPool));
    stmtGeneratorRegistry.put(
        DoStatement.class, new DoStatementGenerator(this, conditionalCodeGenerator, constPool));
    stmtGeneratorRegistry.put(
        ForStatement.class, new ForStatementGenerator(this, conditionalCodeGenerator, constPool));
    stmtGeneratorRegistry.put(
        ReturnStatement.class, new ReturnStatementGenerator(opsCodeGenerator, constPool));
  }

  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement stmt,
      Method method,
      Clazz clazz) {
    return stmtGeneratorRegistry.get(stmt.getClass()).generate(variables, out, stmt, method, clazz);
  }
}
