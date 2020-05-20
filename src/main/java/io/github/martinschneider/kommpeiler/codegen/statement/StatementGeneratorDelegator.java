package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.ConditionalCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.OpsCodeGenerator;
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

public class StatementGeneratorDelegator {
  private Map<Class<? extends Statement>, StatementGenerator> statementGeneratorRegistry;

  public StatementGeneratorDelegator(
      ExpressionCodeGenerator expressionCodeGenerator,
      OpsCodeGenerator opsCodeGenerator,
      ConditionalCodeGenerator conditionalCodeGenerator,
      ConstantPool constantPool,
      Map<String, Method> methodMap) {
    statementGeneratorRegistry = new HashMap<>();
    statementGeneratorRegistry.put(
        Declaration.class,
        new DeclarationGenerator(expressionCodeGenerator, opsCodeGenerator, constantPool));
    statementGeneratorRegistry.put(
        Assignment.class, new AssignmentGenerator(expressionCodeGenerator, opsCodeGenerator));
    statementGeneratorRegistry.put(
        ParallelAssignment.class,
        new ParallelAssignmentGenerator(expressionCodeGenerator, opsCodeGenerator));
    statementGeneratorRegistry.put(
        MethodCall.class,
        new MethodCallGenerator(
            expressionCodeGenerator, opsCodeGenerator, constantPool, methodMap));
    statementGeneratorRegistry.put(
        IfStatement.class, new IfStatementGenerator(this, conditionalCodeGenerator, constantPool));
    statementGeneratorRegistry.put(
        WhileStatement.class,
        new WhileStatementGenerator(this, conditionalCodeGenerator, constantPool));
    statementGeneratorRegistry.put(
        DoStatement.class, new DoStatementGenerator(this, conditionalCodeGenerator, constantPool));
    statementGeneratorRegistry.put(
        ForStatement.class,
        new ForStatementGenerator(this, conditionalCodeGenerator, constantPool));
    statementGeneratorRegistry.put(
        ReturnStatement.class, new ReturnStatementGenerator(opsCodeGenerator, constantPool));
  }

  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement statement,
      Method method,
      Clazz clazz) {
    return statementGeneratorRegistry
        .get(statement.getClass())
        .generate(variables, out, statement, method, clazz);
  }
}
