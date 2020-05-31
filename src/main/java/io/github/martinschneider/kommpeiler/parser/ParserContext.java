package io.github.martinschneider.kommpeiler.parser;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;

public class ParserContext {
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
    ctx.retParser = new ReturnParser(ctx);
    ctx.scopeParser = new ScopeParser();
    ctx.stmtParser = new StatementParser(ctx);
    ctx.whileParser = new WhileParser(ctx);
    return ctx;
  }
}