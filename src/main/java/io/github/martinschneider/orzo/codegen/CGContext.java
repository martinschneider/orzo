package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import io.github.martinschneider.orzo.codegen.statement.ConditionGenerator;
import io.github.martinschneider.orzo.codegen.statement.ExpressionGenerator;
import io.github.martinschneider.orzo.codegen.statement.IncrementGenerator;
import io.github.martinschneider.orzo.codegen.statement.MethodCallGenerator;
import io.github.martinschneider.orzo.codegen.statement.StatementDelegator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.Map;

public class CGContext {
  public Clazz clazz;
  public int currentIdx;
  public ConstantPool constPool;
  public ClassMembers classMembers;
  public Map<String, Method> methodMap;
  public StatementDelegator delegator;
  public BasicCodeGenerator opsGenerator;
  public ExpressionGenerator exprGenerator;
  public IncrementGenerator incrGenerator;
  public MethodCallGenerator methodCallGenerator;
  public ConditionGenerator condGenerator;
  public ConstantPoolProcessor constPoolProcessor;
  public CompilerErrors errors;
}
