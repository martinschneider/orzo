package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import io.github.martinschneider.orzo.codegen.statement.BasicCodeGenerator;
import io.github.martinschneider.orzo.codegen.statement.ConditionalGenerator;
import io.github.martinschneider.orzo.codegen.statement.ExpressionGenerator;
import io.github.martinschneider.orzo.codegen.statement.StatementDelegator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.Map;

public class CGContext {
  public Clazz clazz;
  public ConstantPool constPool;
  public Map<String, Method> methodMap;
  public StatementDelegator delegator;
  public BasicCodeGenerator opsGenerator;
  public ExpressionGenerator exprGenerator;
  public ConditionalGenerator condGenerator;
  public ConstantPoolProcessor constPoolProcessor;
  public CompilerErrors errors;
  public ParserContext parserCtx;
}
