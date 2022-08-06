package io.github.martinschneider.orzo.util;

import static io.github.martinschneider.orzo.lexer.tokens.Token.integer;
import static io.github.martinschneider.orzo.lexer.tokens.Token.str;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.identifier.IdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.error.CompilerError;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.AssignmentParser;
import io.github.martinschneider.orzo.parser.DeclarationParser;
import io.github.martinschneider.orzo.parser.ExpressionParser;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.IncrementStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FactoryHelper {
  public static Expression expr(String input) throws IOException {
    return new ExpressionParser(ParserContext.build(new CompilerErrors()))
        .parse(new Lexer().getTokens(input));
  }

  public static Expression expr(List<Token> input) throws IOException {
    return new Expression(input);
  }

  public static Expression expr(List<Token> input, Type cast) throws IOException {
    return new Expression(input, cast);
  }

  public static Argument arg(String type, Identifier name) throws IOException {
    return new Argument(type, name);
  }

  public static ArraySelector arrSel(List<Expression> selectors) {
    return new ArraySelector(selectors);
  }

  public static ArrayInit arrInit(
      String type, List<Expression> dimensions, List<List<Expression>> vals) {
    return new ArrayInit(type, dimensions, vals);
  }

  public static ArrayInit arrInit(String type, int dim, List<Expression> vals) throws IOException {
    return arrInit(type, list(expr(list(integer(dim)))), list(vals));
  }

  public static IncrementStatement inc(Identifier id, Operator op) throws IOException {
    return new IncrementStatement(expr(list(id, op)));
  }

  public static Assignment assign(Identifier left, Expression right) {
    return new Assignment(list(left), list(right));
  }

  public static Assignment assign(String input) throws IOException {
    return new AssignmentParser(ParserContext.build(new CompilerErrors()))
        .parse(new Lexer().getTokens(input));
  }

  public static Clazz clazz(
      String packageName,
      List<Import> imports,
      Scope scope,
      String name,
      List<String> interfaces,
      String baseClass,
      List<Method> body,
      List<ParallelDeclaration> decls) {
    return new Clazz(
        packageName, imports, scope, name, false, false, interfaces, baseClass, body, decls, null);
  }

  public static Clazz iface(
      String packageName,
      List<Import> imports,
      Scope scope,
      String name,
      String baseClass,
      List<Method> body,
      List<ParallelDeclaration> decls) {
    return new Clazz(
        packageName,
        imports,
        scope,
        name,
        true,
        false,
        Collections.emptyList(),
        baseClass,
        body,
        decls,
        null);
  }

  public static Clazz enumm(
      String packageName,
      List<Import> imports,
      Scope scope,
      String name,
      List<Method> body,
      List<ParallelDeclaration> decls) {
    return new Clazz(
        packageName,
        imports,
        scope,
        name,
        false,
        true,
        Collections.emptyList(),
        "java.lang.Enum",
        body,
        decls,
        null);
  }

  public static ParallelDeclaration pDecl(List<Declaration> decls) {
    return new ParallelDeclaration(decls);
  }

  public static Declaration decl(
      List<AccessFlag> accFlags, Identifier name, String type, Expression val) {
    return new Declaration(accFlags, type, 0, name, val);
  }

  public static ParallelDeclaration pDecl(
      List<AccessFlag> accFlags, Identifier name, String type, Expression val) {
    return new ParallelDeclaration(list(new Declaration(accFlags, type, 0, name, val)));
  }

  public static ParallelDeclaration pDecl(
      List<AccessFlag> accFlags, Identifier name, String type, int array, Expression val) {
    return new ParallelDeclaration(list(new Declaration(accFlags, type, array, name, val)));
  }

  public static ParallelDeclaration pDecl(String input) throws IOException {
    return new DeclarationParser(ParserContext.build(new CompilerErrors()))
        .parse(new Lexer().getTokens(input));
  }

  public static DoStatement doStmt(Expression condition, List<Statement> body) {
    return new DoStatement(condition, body);
  }

  public static ForStatement forStmt(
      Statement initialization,
      Expression condition,
      Statement loopStatement,
      List<Statement> body) {
    return new ForStatement(initialization, condition, loopStatement, body);
  }

  public static CompilerError err(String msg) {
    return new CompilerError(msg, null);
  }

  public static Identifier id(String val, ArraySelector selector) {
    return new Identifier(val, selector);
  }

  public static ConstructorCall constr(String type, List<Expression> args) {
    return new ConstructorCall(type, args);
  }

  public static Identifier id(String... vals) {
    Identifier id = new Identifier(vals[0], null);
    Identifier root = id;
    for (int i = 1; i < vals.length; i++) {
      id.next = new Identifier(vals[i]);
      id = id.next;
    }
    return root;
  }

  public static IfStatement ifStmt(List<IfBlock> ifBlocks, boolean hasElse) {
    return new IfStatement(ifBlocks, false);
  }

  public static IfBlock ifBlk(Expression condition, List<Statement> body) {
    return new IfBlock(condition, body);
  }

  public static Method method(
      List<AccessFlag> accFlags,
      String type,
      Identifier name,
      List<Argument> arguments,
      List<Statement> body) {
    return new Method(null, accFlags, type, name, arguments, body);
  }

  public static Constructor defaultConstr(String fqn) {
    return new Constructor(
        fqn,
        List.of(AccessFlag.ACC_PUBLIC),
        id(Method.CONSTRUCTOR_NAME),
        emptyList(),
        list(new MethodCall("super", emptyList())));
  }

  public static Constructor constr(
      Scope scope, String type, Identifier name, List<Argument> arguments, List<Statement> body) {
    return new Constructor(null, list(((Scopes) scope.val).accFlag), name, arguments, body);
  }

  public static MethodCall methodCall(String name, List<Expression> parameters) {
    return new MethodCall(name, parameters);
  }

  public static MethodCall methodCall(
      String name, List<Expression> parameters, ArraySelector arrSel) {
    return new MethodCall(name, parameters, arrSel);
  }

  public static Assignment assign(List<Identifier> left, List<Expression> right) {
    return new Assignment(left, right);
  }

  public static WhileStatement whileStmt(Expression condition, List<Statement> body)
      throws IOException {
    return new WhileStatement(condition, body);
  }

  public static VariableInfo varInfo(String name, String type, int idx) {
    return new VariableInfo(name, type, emptyList(), false, (short) idx, null);
  }

  public static VariableInfo varInfoArr(String name, String type, int idx) {
    return new VariableInfo(name, "reference", type, emptyList(), false, (short) idx, null);
  }

  public static IdentifierMap varMap(List<VariableInfo> varInfos) {
    Map<String, VariableInfo> map = new HashMap<>();
    for (VariableInfo varInfo : varInfos) {
      map.put(varInfo.name, varInfo);
    }
    IdentifierMap idMap = new IdentifierMap();
    idMap.localMap = map;
    return idMap;
  }

  @SafeVarargs
  public static <E> List<E> list(E... e) {
    return List.of(e);
  }

  @SafeVarargs
  public static <T> Stream<T> stream(T... t) {
    return Stream.of(t);
  }

  // TODO this is currently unused
  private static final Token[] TOKENS = new Token[] {str("test")};
}
