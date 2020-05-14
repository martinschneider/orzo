package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.DOUBLE;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.VOID;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.cmp;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.integer;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.scope;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.martinschneider.kommpeiler.parser.productions.ArraySelector;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.DoStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.FieldSelector;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.Selector;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.Type;
import io.github.martinschneider.kommpeiler.parser.productions.WhileStatement;
import io.github.martinschneider.kommpeiler.scanner.Scanner;
import io.github.martinschneider.kommpeiler.scanner.tokens.Comparator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Comparators;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operators;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scope;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unittests for the parser.
 *
 * @author Martin Schneider
 */
public class ParserTest {
  private Parser parser;
  private Scanner scanner = new Scanner();
  private List<Token> tokens;

  private static Stream<Arguments> testMethodCall() throws IOException {
    return Stream.of(
        Arguments.of(
            "calculateSomething()", List.of(id("calculateSomething")), Collections.emptyList()),
        Arguments.of("calculateSomething(x)", List.of(id("calculateSomething")), List.of(exp("x"))),
        Arguments.of(
            "calculateSomething(a,b,c)",
            List.of(id("calculateSomething")),
            List.of(exp("a"), exp("b"), exp("c"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testMethodCall(
      String input, List<String> expectedNames, List<List<Token>> expectedParameters)
      throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    MethodCall methodCall = parser.parseMethodCall();
    assertNotNull(methodCall);
    assertEquals(expectedNames, methodCall.getNames());
    assertEquals(expectedParameters.size(), methodCall.getParameters().size());
    if (expectedParameters.size() > 0) {
      assertEquals(expectedParameters, methodCall.getParameters());
    }
  }

  @Test
  public void randomInputTest() throws IOException {
    final int nrOfChars = 1000000;
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 1; i <= nrOfChars; i++) {
      stringBuffer.append((char) (int) (Math.random() * 256 + 1));
    }
    tokens = scanner.getTokens(stringBuffer.toString());
    parser = new Parser(tokens);
    parser.parse();
  }

  @Test
  public void randomTokensTest() throws IOException {
    final int nrOfTokens = 100000;
    List<String> tokenList =
        Arrays.asList(
            "while",
            "do",
            "if",
            "public",
            "private",
            "protected",
            "x",
            "y",
            "0",
            "=",
            "==",
            "!=",
            "{",
            "}",
            "<",
            ">",
            ">=",
            "<=",
            "+",
            "-",
            "[",
            "]");
    StringBuffer stringBuffer = new StringBuffer();
    for (int i = 1; i <= nrOfTokens; i++) {
      int index = (int) (Math.random() * tokenList.size());
      stringBuffer.append(tokenList.get(index));
      stringBuffer.append(" ");
    }
    tokens = scanner.getTokens(stringBuffer.toString());
    parser = new Parser(tokens);
    parser.parse();
  }

  private static Stream<Arguments> testParameters() throws IOException {
    return Stream.of(
        Arguments.of("()", Collections.emptyList()),
        Arguments.of("(x)", List.of(exp("x"))),
        Arguments.of("(ab,cd,efg)", List.of(exp("ab"), exp("cd"), exp("efg"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testParameters(String input, List<Expression> expectedParameters) throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    List<Expression> parameters = parser.parseParameters();
    assertEquals(expectedParameters, parameters);
  }

  private static Stream<Arguments> testArrayAssignment() {
    return Stream.of(Arguments.of("x[1]=3", List.of(integer(1))));
  }

  @ParameterizedTest
  @MethodSource
  public void testArrayAssignment(String input, List<Token> expectedSelector) throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Assignment assignment = parser.parseAssignment();
    assertTrue(assignment instanceof Assignment);
    assertNotNull(assignment.getLeft().getSelector());
    assertTrue(assignment.getLeft().getSelector() instanceof ArraySelector);
    assertEquals(
        expectedSelector,
        ((ArraySelector) assignment.getLeft().getSelector()).getExpression().getInfix());
  }

  private static Stream<Arguments> testSelectorAssignment() {
    return Stream.of(Arguments.of("x.y=3", "y"));
  }

  @ParameterizedTest
  @MethodSource
  public void testSelectorAssignment(String input, String expectedSelector) throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Assignment assignment = parser.parseAssignment();
    assertTrue(assignment instanceof Assignment);
    assertNotNull(assignment.getLeft().getSelector());
    assertTrue(assignment.getLeft().getSelector() instanceof FieldSelector);
    assertEquals(
        expectedSelector,
        ((FieldSelector) assignment.getLeft().getSelector()).getIdentifier().getValue());
  }

  private static Stream<Arguments> testAssignment() throws IOException {
    return Stream.of(
        Arguments.of("x=5", id("x"), exp("5")),
        Arguments.of("x=5*12-3/6+12%4", id("x"), exp("5*12-3/6+12%4")));
  }

  @ParameterizedTest
  @MethodSource
  public void testAssignment(String input, Identifier expectedLeft, Expression expectedRight)
      throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Assignment assignment = parser.parseAssignment();
    assertEquals(expectedLeft, assignment.getLeft());
    assertEquals(expectedRight, assignment.getRight());
  }

  private static Stream<Arguments> testClass() throws IOException {
    return Stream.of(
        Arguments.of(
            "public class Martin{public void test(){x=0;}}", id("Martin"), scope(PUBLIC), 1),
        Arguments.of("private class Laura{}", id("Laura"), scope(PRIVATE), 0),
        Arguments.of("class Empty{}", id("Empty"), null, 0));
  }

  @ParameterizedTest
  @MethodSource
  public void testClass(
      String input, Identifier expectedName, Scope expectedScope, int expectedMethodCount)
      throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Clazz clazz = parser.parseClass();
    assertEquals(expectedName, clazz.getName());
    assertEquals(expectedScope, clazz.getScope());
    assertEquals(expectedMethodCount, clazz.getBody().size());
  }

  private static Stream<Arguments> testCondition() throws IOException {
    return Stream.of(
        Arguments.of("y==0", exp("y"), cmp(Comparators.EQUAL), exp("0")),
        Arguments.of("x<=z", exp("x"), cmp(Comparators.SMALLEREQ), exp("z")),
        Arguments.of("abc<xyz", exp("abc"), cmp(Comparators.SMALLER), exp("xyz")),
        Arguments.of(
            "basketball > fussball", exp("basketball"), cmp(Comparators.GREATER), exp("fussball")));
  }

  @ParameterizedTest
  @MethodSource
  public void testCondition(
      String input, Expression expectedLeft, Comparator expectedOperator, Expression expectedRight)
      throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Condition condition = parser.parseCondition();
    assertEquals(expectedLeft, condition.getLeft());
    assertEquals(expectedOperator, condition.getOperator());
    assertEquals(expectedRight, condition.getRight());
  }

  private static Stream<Arguments> testDoStatement() throws IOException {
    return Stream.of(
        Arguments.of("do{x=x*3-1;}while(x>0);", cond("x>0"), List.of(assign("x=x*3-1;"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testDoStatement(
      String input, Condition expectedCondition, List<Statement> expectedStatements)
      throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    DoStatement doStatement = parser.parseDoStatement();
    assertNotNull(doStatement);
    assertEquals(expectedStatements, doStatement.getBody());
    assertEquals(expectedCondition, doStatement.getCondition());
  }

  private static Stream<Arguments> testExpression() {
    return Stream.of(
        Arguments.of("", null),
        Arguments.of("3+4", List.of(new IntNum(3), new IntNum(4), new Operator(Operators.PLUS))),
        Arguments.of(
            "3+x*y-7",
            List.of(
                new IntNum(3),
                new Identifier("x"),
                new Identifier("y"),
                new Operator(Operators.TIMES),
                new Operator(Operators.PLUS),
                new IntNum(7),
                new Operator(Operators.MINUS))),
        Arguments.of(
            "-5*7+4",
            List.of(
                new IntNum(-5),
                new IntNum(7),
                new Operator(Operators.TIMES),
                new IntNum(4),
                new Operator(Operators.PLUS))),
        Arguments.of(
            "x*3+7-8/1+0%6",
            List.of(
                new Identifier("x"),
                new IntNum(3),
                new Operator(Operators.TIMES),
                new IntNum(7),
                new Operator(Operators.PLUS),
                new IntNum(8),
                new IntNum(1),
                new Operator(Operators.DIV),
                new Operator(Operators.MINUS),
                new IntNum(0),
                new IntNum(6),
                new Operator(Operators.MOD),
                new Operator(Operators.PLUS))),
        Arguments.of("3+4;", List.of(new IntNum(3), new IntNum(4), new Operator(Operators.PLUS))),
        Arguments.of(
            "5+7/2;",
            List.of(
                new IntNum(5),
                new IntNum(7),
                new IntNum(2),
                new Operator(Operators.DIV),
                new Operator(Operators.PLUS))),
        Arguments.of(
            "(5+7)/2;",
            List.of(
                new IntNum(5),
                new IntNum(7),
                new Operator(Operators.PLUS),
                new IntNum(2),
                new Operator(Operators.DIV))));
  }

  @MethodSource
  @ParameterizedTest
  public void testExpression(String input, List<Token> expected) throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Expression expression = parser.parseExpression();
    List<Token> actual = (expression != null) ? expression.getPostfix() : null;
    assertEquals(expected, actual);
  }

  private static Stream<Arguments> testIfStatement() throws IOException {
    return Stream.of(Arguments.of("if (x==9){x=8;};", cond("x==9"), List.of(assign("x=8;"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testIfStatement(
      String input, Condition expectedCondition, List<Statement> expectedStatements)
      throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    IfStatement ifStatement = parser.parseIfStatement();
    assertNotNull(ifStatement);
    assertEquals(expectedCondition, ifStatement.getCondition());
    assertEquals(expectedStatements, ifStatement.getBody());
  }

  private static Stream<Arguments> testMethod() throws IOException {
    return Stream.of(
        Arguments.of(
            "public void test(){x=1;y=2;if(x==y){fehler=1;}",
            scope(PUBLIC),
            type(VOID),
            id("test"),
            List.of(
                assign("x=1"), assign("y=2"), ifStmt(cond("x==y"), List.of(assign("fehler=1"))))),
        Arguments.of(
            "void test(){x=100;while(x>0){x=x-1;}",
            scope(DEFAULT),
            type(VOID),
            id("test"),
            List.of(assign("x=100"), whileStmt(cond("x>0"), List.of(assign("x=x-1"))))),
        Arguments.of(
            "protected int huber(){do{}while(x>0)}",
            scope(PROTECTED),
            type(INT),
            id("huber"),
            List.of(doStmt(cond("x>0"), Collections.emptyList()))),
        Arguments.of(
            "private double calculateMean(){}",
            scope(PRIVATE),
            type(DOUBLE),
            id("calculateMean"),
            Collections.emptyList()));
  }

  @MethodSource
  @ParameterizedTest
  public void testMethod(
      String input,
      Scope expectedScope,
      Type expectedType,
      Identifier expectedName,
      List<Statement> expectedStatements)
      throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Method method = parser.parseMethod();
    assertNotNull(method);
    assertEquals(expectedScope, method.getScope());
    assertEquals(expectedType, method.getType());
    assertEquals(expectedStatements, method.getBody());
  }

  private static Stream<Arguments> testArraySelector() {
    return Stream.of(Arguments.of(".y", "y"), Arguments.of(".test", "test"));
  }

  @ParameterizedTest
  @MethodSource
  public void testArraySelector(String input, String expectedSelector) throws IOException {
    tokens = scanner.getTokens("[x]");
    parser = new Parser(tokens);
    Selector selector = parser.parseSelector();
    assertNotNull(selector);
    assertTrue(selector instanceof ArraySelector);
    assertEquals(List.of(id("x")), ((ArraySelector) selector).getExpression().getInfix());
  }

  private static Stream<Arguments> testFieldSelector() {
    return Stream.of(Arguments.of(".y", "y"), Arguments.of(".test", "test"));
  }

  @ParameterizedTest
  @MethodSource
  public void testFieldSelector(String input, String expectedSelector) throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Selector selector = parser.parseSelector();
    assertNotNull(selector);
    assertTrue(selector instanceof FieldSelector);
    assertEquals(expectedSelector, ((FieldSelector) selector).getIdentifier().getValue());
  }

  private static Stream<Arguments> testStatement() {
    return Stream.of(
        Arguments.of("x=5*12-3/6+12;", true),
        Arguments.of("if (x==1){ x=2; }", true),
        Arguments.of("while (x>=0){ x=x-1; }", true),
        Arguments.of("do{x=y+1;y=y-1;} while(i>0)", true),
        Arguments.of("while (x>=0){ x=x-1; }", true),
        Arguments.of("do{x=y+1;y=y-1;} while(i>0)", true),
        Arguments.of("int z;", true),
        Arguments.of("int z=300;", true));
  }

  @ParameterizedTest
  @MethodSource
  public void testStatement(String input, boolean valid) throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Statement statement = parser.parseStatement();
    assertTrue((statement != null) == valid);
  }

  private static Stream<Arguments> testStatementSequence() {
    return Stream.of(
        Arguments.of("", 0),
        Arguments.of("x=5*12-3/6+12%4;", 1),
        Arguments.of("x=5*12-3/6+12%4;abc=9", 2),
        Arguments.of("y=9%(4+7)*3;", 1),
        Arguments.of("int x=10;x=x+1;int y=20;y=x+2;double z=x+y", 5));
  }

  @ParameterizedTest
  @MethodSource
  public void testStatementSequence(String input, int count) throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    List<Statement> statement = parser.parseStatementSequence();
    assertEquals(count, statement.size());
  }

  private static Stream<Arguments> testDeclaration() {
    return Stream.of(
        Arguments.of("int x=100;", type("INT"), "x", true),
        Arguments.of("String martin", type("STRING"), "martin", false),
        Arguments.of("double d=1.23", type("DOUBLE"), "d", true));
  }

  @MethodSource
  @ParameterizedTest
  public void testDeclaration(
      String input, Type expectedType, String expectedName, boolean hasExpression)
      throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    Declaration declaration = parser.parseDeclaration();
    assertNotNull(declaration);
    assertEquals(expectedType, declaration.getType());
    assertEquals(expectedName, declaration.getName().getValue());
    assertEquals(hasExpression, declaration.getValue() != null);
  }

  private static Stream<Arguments> testWhileStatement() throws IOException {
    return Stream.of(
        Arguments.of(
            "while (x==9){x=8;y=7;};", cond("x==9"), List.of(assign("x=8;"), assign("y=7;"))));
  }

  @ParameterizedTest
  @MethodSource
  public void testWhileStatement(
      String input, Condition expectedCondition, List<Statement> expectedStatements)
      throws IOException {
    tokens = scanner.getTokens(input);
    parser = new Parser(tokens);
    WhileStatement whileStatement = parser.parseWhileStatement();
    assertNotNull(whileStatement);
    assertEquals(expectedCondition, whileStatement.getCondition());
    assertEquals(expectedStatements, whileStatement.getBody());
  }

  // Helper
  private static Expression exp(String input) throws IOException {
    return new Expression(new Scanner().getTokens(input));
  }

  private static Assignment assign(String input) throws IOException {
    return new Parser(new Scanner().getTokens(input)).parseAssignment();
  }

  private static Condition cond(String input) throws IOException {
    return new Parser(new Scanner().getTokens(input)).parseCondition();
  }

  private static IfStatement ifStmt(Condition condition, List<Statement> body) throws IOException {
    return new IfStatement(condition, body);
  }

  private static WhileStatement whileStmt(Condition condition, List<Statement> body)
      throws IOException {
    return new WhileStatement(condition, body);
  }

  private static DoStatement doStmt(Condition condition, List<Statement> body) throws IOException {
    return new DoStatement(condition, body);
  }
}
