package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.Clazz;

public class ParserContext {
  public Clazz currClazz;
  public CompilerErrors errors;
  public ArrayInitParser arrayInitParser;
  public ArraySelectorParser arraySelectorParser;
  public AssignmentParser assignParser;
  public BreakParser breakParser;
  public ConditionParser condParser;
  public DeclarationParser declParser;
  public DoParser doParser;
  public ExpressionParser exprParser;
  public ForParser forParser;
  public IfParser ifParser;
  public MethodCallParser methodCallParser;
  public MethodParser methodParser;
  public ParallelAssignmentParser parallelAssignParser;
  public PostIncrementParser postIncrementParser;
  public PreIncrementParser preIncrementParser;
  public ReturnParser retParser;
  public ScopeParser scopeParser;
  public StatementParser stmtParser;
  public WhileParser whileParser;

  public static ParserContext build(CompilerErrors errors) {
    ParserContext ctx = new ParserContext();
    ctx.errors = errors;
    ctx.arrayInitParser = new ArrayInitParser(ctx);
    ctx.arraySelectorParser = new ArraySelectorParser(ctx);
    ctx.assignParser = new AssignmentParser(ctx);
    ctx.breakParser = new BreakParser();
    ctx.condParser = new ConditionParser(ctx);
    ctx.declParser = new DeclarationParser(ctx);
    ctx.doParser = new DoParser(ctx);
    ctx.exprParser = new ExpressionParser(ctx);
    ctx.forParser = new ForParser(ctx);
    ctx.ifParser = new IfParser(ctx);
    ctx.methodCallParser = new MethodCallParser(ctx);
    ctx.methodParser = new MethodParser(ctx);
    ctx.parallelAssignParser = new ParallelAssignmentParser(ctx);
    ctx.postIncrementParser = new PostIncrementParser(ctx);
    ctx.preIncrementParser = new PreIncrementParser(ctx);
    ctx.retParser = new ReturnParser(ctx);
    ctx.scopeParser = new ScopeParser();
    ctx.stmtParser = new StatementParser(ctx);
    ctx.whileParser = new WhileParser(ctx);
    return ctx;
  }
}
