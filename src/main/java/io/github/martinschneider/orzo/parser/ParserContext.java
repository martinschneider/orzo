package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserContext {
  public Clazz currClazz;
  // the number of repeat loops parsed (for the names of their counters)
  public int repeatCount;
  // static nested classes of the parsed file, compiled as separate classes (e.g. "Outer$Inner")
  public List<Clazz> nestedClazzes = new ArrayList<>();
  // Maps simple class name → fully-qualified name, built from import statements
  public Map<String, String> importMap = new HashMap<>();
  // the packages of on-demand imports ("import java.io.*;")
  public List<String> onDemandImports = new ArrayList<>();
  // Maps class-level type variable names to their erasure types (e.g. T → java.lang.Object)
  public Map<String, String> classTypeParams = new HashMap<>();
  public CompilerErrors errors;
  public ArrayDefParser arrayDefParser;
  public ArrayInitParser arrayInitParser;
  public ArraySelectorParser arraySelectorParser;
  public AssignmentParser assignParser;
  public BreakParser breakParser;
  public ClassParser classParser;
  public ConstructorCallParser constrCallParser;
  public ContinueParser continueParser;
  public DeclarationParser declParser;
  public DoParser doParser;
  public EnumParser enumParser;
  public ExpressionParser exprParser;
  public ForEachParser forEachParser;
  public ForParser forParser;
  public IfParser ifParser;
  public LambdaParser lambdaParser;
  public MethodCallParser methodCallParser;
  public MethodParser methodParser;
  public ModifiersParser modifiersParser;
  public PostIncrementParser postIncrementParser;
  public PreIncrementParser preIncrementParser;
  public RetParser retParser;
  public ScopeParser scopeParser;
  public StatementParser stmtParser;
  public WhileParser whileParser;
  public RepeatParser repeatParser;
  public SwitchParser switchParser;
  public ThrowParser throwParser;
  public TryParser tryParser;
  public TypeArgsParser typeArgsParser;
  public TypeMap typeMap;

  public static ParserContext build(CompilerErrors errors) {
    ParserContext ctx = new ParserContext();
    ctx.errors = errors;
    ctx.arrayDefParser = new ArrayDefParser();
    ctx.arrayInitParser = new ArrayInitParser(ctx);
    ctx.arraySelectorParser = new ArraySelectorParser(ctx);
    ctx.assignParser = new AssignmentParser(ctx);
    ctx.breakParser = new BreakParser();
    ctx.classParser = new ClassParser(ctx);
    ctx.constrCallParser = new ConstructorCallParser(ctx);
    ctx.continueParser = new ContinueParser();
    ctx.declParser = new DeclarationParser(ctx);
    ctx.doParser = new DoParser(ctx);
    ctx.enumParser = new EnumParser(ctx);
    ctx.exprParser = new ExpressionParser(ctx);
    ctx.forEachParser = new ForEachParser(ctx);
    ctx.forParser = new ForParser(ctx);
    ctx.ifParser = new IfParser(ctx);
    ctx.lambdaParser = new LambdaParser(ctx);
    ctx.methodCallParser = new MethodCallParser(ctx);
    ctx.methodParser = new MethodParser(ctx);
    ctx.modifiersParser = new ModifiersParser();
    ctx.postIncrementParser = new PostIncrementParser(ctx);
    ctx.preIncrementParser = new PreIncrementParser(ctx);
    ctx.retParser = new RetParser(ctx);
    ctx.scopeParser = new ScopeParser();
    ctx.stmtParser = new StatementParser(ctx);
    ctx.whileParser = new WhileParser(ctx);
    ctx.repeatParser = new RepeatParser(ctx);
    ctx.switchParser = new SwitchParser(ctx);
    ctx.throwParser = new ThrowParser(ctx);
    ctx.tryParser = new TryParser(ctx);
    ctx.typeArgsParser = new TypeArgsParser(ctx);
    ctx.typeMap = new TypeMap();
    return ctx;
  }
}
