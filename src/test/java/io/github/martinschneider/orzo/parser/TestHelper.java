package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Token.str;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerError;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Comparator;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.ArrayInitialiser;
import io.github.martinschneider.orzo.parser.productions.ArraySelector;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Condition;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelAssignment;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;
import java.io.IOException;
import java.util.List;

public class TestHelper {
  public static Expression exp(String input) throws IOException {
    return new ExpressionParser(ParserContext.build(new CompilerErrors()))
        .parse(new Lexer().getTokens(input));
  }

  public static Expression exp(List<Token> input) throws IOException {
    return new Expression(input);
  }

  public static Argument arg(String type, Identifier name) throws IOException {
    return new Argument(type, name);
  }

  public static ArraySelector arrSel(List<Expression> selectors) {
    return new ArraySelector(selectors);
  }

  public static ArrayInitialiser arrInit(String type, int dim, List<Expression> values) {
    return new ArrayInitialiser(type, dim, values);
  }

  public static Assignment assign(Identifier left, Expression right) {
    return new Assignment(left, right);
  }

  public static Assignment assign(String input) throws IOException {
    return new AssignmentParser(ParserContext.build(new CompilerErrors()))
        .parse(new Lexer().getTokens(input));
  }

  public static Clazz clazz(
      String packageName, List<Import> imports, Scope scope, Identifier name, List<Method> body) {
    return new Clazz(packageName, imports, scope, name, body);
  }

  public static Condition cond(String input) throws IOException {
    return new ConditionParser(ParserContext.build(new CompilerErrors()))
        .parse(new Lexer().getTokens(input));
  }

  public static Condition cond(Expression left, Comparator comp, Expression right) {
    return new Condition(left, comp, right);
  }

  public static Declaration decl(Identifier name, String type, Expression value, boolean hasValue) {
    return new Declaration(type, name, value, hasValue);
  }

  public static Declaration decl(
      Identifier name, String type, int array, Expression value, boolean hasValue) {
    return new Declaration(type, array, name, value, hasValue);
  }

  public static Declaration decl(String input) throws IOException {
    return new DeclarationParser(ParserContext.build(new CompilerErrors()))
        .parse(new Lexer().getTokens(input));
  }

  public static DoStatement doStmt(Condition condition, List<Statement> body) {
    return new DoStatement(condition, body);
  }

  public static ForStatement forStmt(
      Statement initialization,
      Condition condition,
      Statement loopStatement,
      List<Statement> body) {
    return new ForStatement(initialization, condition, loopStatement, body);
  }

  public static CompilerError err(String msg) {
    return new CompilerError(msg);
  }

  public static Identifier id(String value, ArraySelector selector) {
    return new Identifier(value, selector);
  }

  public static IfStatement ifStmt(List<IfBlock> ifBlocks, boolean hasElse) {
    return new IfStatement(ifBlocks, false);
  }

  public static IfBlock ifBlk(Condition condition, List<Statement> body) {
    return new IfBlock(condition, body);
  }

  public static Method method(
      Scope scope, String type, Identifier name, List<Argument> arguments, List<Statement> body) {
    return new Method(scope, type, name, arguments, body);
  }

  public static MethodCall methodCall(Identifier name, List<Expression> parameters) {
    return new MethodCall(name, parameters);
  }

  public static ParallelAssignment pAssign(List<Identifier> left, List<Expression> right) {
    return new ParallelAssignment(left, right);
  }

  public static WhileStatement whileStmt(Condition condition, List<Statement> body)
      throws IOException {
    return new WhileStatement(condition, body);
  }

  /** @see io.github.martinschneider.orzo.parser.ProdParser * */
  public static void assertTokenIdx(TokenList tokens, CompilerErrors errors, boolean resetIdx) {
    if (!errors.getErrors().isEmpty()) {
      // TODO: define and test error scenarios
      return;
    }
    assertTokenIdx(tokens, resetIdx);
  }

  public static void assertTokenIdx(TokenList tokens, boolean resetIdx) {
    if (resetIdx) {
      assertEquals(0, tokens.idx());
    } else {
      assertEquals(tokens.size(), tokens.idx());
    }
  }

  // TODO
  private static final Token[] TOKENS = new Token[] {str("test")};

  public static Token randomToken() {
    return TOKENS[(int) (Math.random() * TOKENS.length)];
  }
}
