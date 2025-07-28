package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.Clazz;

public class ParserContext {
  public Clazz currClazz;
  public CompilerErrors errors;
  public ArrayDefParser arrayDefParser;
  public ArrayInitParser arrayInitParser;
  public ArraySelectorParser arraySelectorParser;
  public AssignmentParser assignParser;
  public BreakParser breakParser;
  public CastParser castParser;
  public ConstructorCallParser constrCallParser;
  public DeclarationParser declParser;
  public DoParser doParser;
  public EnumParser enumParser;
  public ExpressionParser exprParser;
  public FloorParser floorParser;
  public ForParser forParser;
  public IfParser ifParser;
  public MethodCallParser methodCallParser;
  public MethodParser methodParser;
  public PostIncrementParser postIncrementParser;
  public PreIncrementParser preIncrementParser;
  public RetParser retParser;
  public ScopeParser scopeParser;
  public SqrtParser sqrtParser;
  public StatementParser stmtParser;
  public WhileParser whileParser;
  public RepeatParser repeatParser;
  public TypeMap typeMap;

  public static ParserContext build(CompilerErrors errors) {
    ParserContext ctx = new ParserContext();
    ctx.errors = errors;
    ctx.arrayDefParser = new ArrayDefParser();
    ctx.arrayInitParser = new ArrayInitParser(ctx);
    ctx.arraySelectorParser = new ArraySelectorParser(ctx);
    ctx.assignParser = new AssignmentParser(ctx);
    ctx.breakParser = new BreakParser();
    ctx.castParser = new CastParser(ctx);
    ctx.constrCallParser = new ConstructorCallParser(ctx);
    ctx.declParser = new DeclarationParser(ctx);
    ctx.doParser = new DoParser(ctx);
    ctx.enumParser = new EnumParser(ctx);
    ctx.exprParser = new ExpressionParser(ctx);
    ctx.floorParser = new FloorParser(ctx);
    ctx.forParser = new ForParser(ctx);
    ctx.ifParser = new IfParser(ctx);
    ctx.methodCallParser = new MethodCallParser(ctx);
    ctx.methodParser = new MethodParser(ctx);
    ctx.postIncrementParser = new PostIncrementParser(ctx);
    ctx.preIncrementParser = new PreIncrementParser(ctx);
    ctx.retParser = new RetParser(ctx);
    ctx.scopeParser = new ScopeParser();
    ctx.sqrtParser = new SqrtParser(ctx);
    ctx.stmtParser = new StatementParser(ctx);
    ctx.whileParser = new WhileParser(ctx);
    ctx.repeatParser = new RepeatParser(ctx);
    ctx.typeMap = new TypeMap();
    return ctx;
  }
}
