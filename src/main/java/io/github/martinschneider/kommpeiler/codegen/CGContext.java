package io.github.martinschneider.kommpeiler.codegen;

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.codegen.statement.ConditionalGenerator;
import io.github.martinschneider.kommpeiler.codegen.statement.ExpressionGenerator;
import io.github.martinschneider.kommpeiler.codegen.statement.OpCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.statement.StatementDelegator;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import java.util.Map;

public class CGContext {
  public Clazz clazz;
  public ConstantPool constPool;
  public Map<String, Method> methodMap;
  public StatementDelegator delegator;
  public OpCodeGenerator opsGenerator;
  public ExpressionGenerator exprGenerator;
  public ConditionalGenerator condGenerator;
  public ConstantPoolProcessor constPoolProcessor;
}
