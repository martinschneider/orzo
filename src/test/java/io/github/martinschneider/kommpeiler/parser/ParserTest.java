package io.github.martinschneider.kommpeiler.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.martinschneider.kommpeiler.parser.productions.ArraySelector;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.DoStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.ExpressionFactor;
import io.github.martinschneider.kommpeiler.parser.productions.Factor;
import io.github.martinschneider.kommpeiler.parser.productions.FieldSelector;
import io.github.martinschneider.kommpeiler.parser.productions.IdFactor;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.parser.productions.IntFactor;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.Selector;
import io.github.martinschneider.kommpeiler.parser.productions.SimpleExpression;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.Term;
import io.github.martinschneider.kommpeiler.parser.productions.WhileStatement;
import io.github.martinschneider.kommpeiler.scanner.Scanner;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import io.github.martinschneider.kommpeiler.scanner.tokens.TokenType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unittests for the parser.
 *
 * @author Martin Schneider
 */
public class ParserTest {
  private Parser parser;
  private Scanner scanner = new Scanner();
  private List<Token> tokens;

  /** @throws IOException I/O-error */
  @Test
  public void parseMethodCall() throws IOException {
    tokens = scanner.getTokens("calculateSomething()");
    parser = new Parser(tokens);
    MethodCall methodCall = parser.parseMethodCall();
    assertNotNull(methodCall);
    assertEquals(methodCall.getParameters().size(), 0);
    tokens = scanner.getTokens("calculateSomething(x)");
    parser = new Parser(tokens);
    methodCall = parser.parseMethodCall();
    assertNotNull(methodCall);
    assertEquals(methodCall.getNames().get(0).getValue(), "calculateSomething");
    assertEquals(methodCall.getParameters().size(), 1);
    assertEquals(methodCall.getParameters().get(0).getValue(), "x");
    tokens = scanner.getTokens("calculateSomething(a,b,c)");
    parser = new Parser(tokens);
    methodCall = parser.parseMethodCall();
    assertNotNull(methodCall);
    assertEquals(methodCall.getParameters().size(), 3);
    assertEquals(methodCall.getParameters().get(0).getValue(), "a");
    assertEquals(methodCall.getParameters().get(1).getValue(), "b");
    assertEquals(methodCall.getParameters().get(2).getValue(), "c");
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

  /** @throws IOException I/O-error */
  @Test
  public void testActualParameters() throws IOException {
    tokens = scanner.getTokens("()");
    parser = new Parser(tokens);
    List<Factor> parameters = parser.parseParameters();
    assertTrue(parameters.isEmpty());
    tokens = scanner.getTokens("(x)");
    parser = new Parser(tokens);
    parameters = parser.parseParameters();
    assertEquals(parameters.size(), 1);
    assertEquals(parameters.get(0).getValue(), "x");
    tokens = scanner.getTokens("(ab,cd,efg)");
    parser = new Parser(tokens);
    parameters = parser.parseParameters();
    assertEquals(parameters.size(), 3);
    assertEquals(parameters.get(0).getValue(), "ab");
    assertEquals(parameters.get(1).getValue(), "cd");
    assertEquals(parameters.get(2).getValue(), "efg");
  }

  @Test
  public void testAssignment() throws IOException {
    tokens = scanner.getTokens("x=5");
    parser = new Parser(tokens);
    Assignment assignment = parser.parseAssignment();
    assertTrue(assignment instanceof Assignment);
    tokens = scanner.getTokens("x=5*12-3/6+12%4");
    parser = new Parser(tokens);
    assignment = parser.parseAssignment();
    assertTrue(assignment instanceof Assignment);
    tokens = scanner.getTokens("x.y=3");
    parser = new Parser(tokens);
    assignment = parser.parseAssignment();
    assertTrue(assignment instanceof Assignment);
    assertNotNull(assignment.getLeft().getSelector());
    assertTrue(assignment.getLeft().getSelector() instanceof FieldSelector);
    assertEquals(
        ((FieldSelector) assignment.getLeft().getSelector()).getIdentifier().getValue(), "y");
    tokens = scanner.getTokens("x[a+b*c]=z*y");
    parser = new Parser(tokens);
    assignment = parser.parseAssignment();
    assertTrue(assignment instanceof Assignment);
    assertNotNull(assignment.getLeft().getSelector());
    assertTrue(assignment.getLeft().getSelector() instanceof ArraySelector);
  }

  @Test
  public void testClass() throws IOException {
    tokens = scanner.getTokens("public class Martin{public void test(){x=0;}}");
    parser = new Parser(tokens);
    Clazz clazz = parser.parseClass();
    assertEquals(clazz.getClass(), Clazz.class);
    tokens = scanner.getTokens("public class Laura{}");
    parser = new Parser(tokens);
    clazz = parser.parseClass();
    assertEquals(clazz.getClass(), Clazz.class);
  }

  /** test evaluation of expressions at compile time */
  @Test
  public void testCompileTimeEvaluation() {
    Term term1 = new Term(new IntFactor(2), new Operator(TokenType.TIMES), new IntFactor(5));
    assertEquals(term1.getValue(), 10);
    Term term2 = new Term(new IntFactor(12), new Operator(TokenType.DIV), new IntFactor(6));
    assertEquals(term2.getValue(), 2);
    Term term3 = new Term(new IntFactor(5), new Operator(TokenType.MOD), new IntFactor(2));
    assertEquals(term3.getValue(), 1);
    SimpleExpression expr = new SimpleExpression(term1, new Operator(TokenType.PLUS), term2);
    assertEquals(expr.getValue(), 12);
    expr = new SimpleExpression(term3, new Operator(TokenType.MINUS), term1);
    assertEquals(expr.getValue(), -9);
  }

  @Test
  public void testDoStatement() throws IOException {
    tokens = scanner.getTokens("do{x=x*3-1;}while(x<0);");
    parser = new Parser(tokens);
    DoStatement doStatement = parser.parseDoStatement();
    assertNotNull(doStatement);
  }

  @Test
  public void testDoStatementError() throws IOException {
    tokens = scanner.getTokens("do x=x*3-1;}while(x<0);");
    parser = new Parser(tokens);
    parser.parseDoStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testDoStatementError1() throws IOException {
    tokens = scanner.getTokens("do{x=x*3-1; while(x<0);");
    parser = new Parser(tokens);
    parser.parseDoStatement();
    assertTrue(parser.getErrors().count() > 0);
  }

  @Test
  public void testDoStatementError2() throws IOException {
    tokens = scanner.getTokens("do{x=x*3-1;}while x<0);");
    parser = new Parser(tokens);
    parser.parseDoStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testDoStatementError3() throws IOException {
    tokens = scanner.getTokens("do{x=x*3-1;}while(x<0;");
    parser = new Parser(tokens);
    parser.parseDoStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testExpression() throws IOException {
    tokens = scanner.getTokens("y==0");
    parser = new Parser(tokens);
    Factor factor = parser.parseExpression();
    assertEquals(factor.getClass(), Expression.class);
    tokens = scanner.getTokens("x<=z");
    parser = new Parser(tokens);
    factor = parser.parseExpression();
    assertEquals(factor.getClass(), Expression.class);
    tokens = scanner.getTokens("abc<xyz");
    parser = new Parser(tokens);
    factor = parser.parseExpression();
    assertEquals(factor.getClass(), Expression.class);
    tokens = scanner.getTokens("basketball > fussball");
    parser = new Parser(tokens);
    factor = parser.parseExpression();
    assertEquals(factor.getClass(), Expression.class);
    tokens = scanner.getTokens(";");
    parser = new Parser(tokens);
    factor = parser.parseExpression();
  }

  @Test
  public void testFactor() throws IOException {
    tokens = scanner.getTokens("3");
    parser = new Parser(tokens);
    Factor factor = parser.parseTerm();
    assertTrue(factor instanceof IntFactor);
    assertEquals(((IntFactor) factor).getValue(), 3);
    tokens = scanner.getTokens("xy");
    parser = new Parser(tokens);
    factor = parser.parseTerm();
    assertTrue(factor instanceof IdFactor);
    assertEquals(((IdFactor) factor).getToken().getValue(), "xy");
    tokens = scanner.getTokens("(y+z*3)");
    parser = new Parser(tokens);
    factor = parser.parseTerm();
    assertTrue(factor instanceof ExpressionFactor);
    tokens = scanner.getTokens("x.abc");
    parser = new Parser(tokens);
    factor = parser.parseTerm();
    assertTrue(factor instanceof IdFactor);
    assertTrue(((IdFactor) factor).getSelector() instanceof FieldSelector);
    tokens = scanner.getTokens("xy[a*b+c]");
    parser = new Parser(tokens);
    factor = parser.parseTerm();
    assertTrue(factor instanceof IdFactor);
    assertTrue(((IdFactor) factor).getSelector() instanceof ArraySelector);
    tokens = scanner.getTokens("x.abc[76]");
    parser = new Parser(tokens);
    factor = parser.parseTerm();
    assertTrue(factor instanceof IdFactor);
    assertTrue(((IdFactor) factor).getSelector() instanceof FieldSelector);
  }

  @Test
  public void testIfStatement() throws IOException {
    tokens = scanner.getTokens("if (x==9){x=8;};");
    parser = new Parser(tokens);
    IfStatement ifStatement = parser.parseIfStatement();
    assertNotNull(ifStatement);
  }

  @Test
  public void testIfStatementError() throws IOException {
    tokens = scanner.getTokens("if x==9){x=8;};");
    parser = new Parser(tokens);
    parser.parseIfStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testIfStatementError1() throws IOException {
    tokens = scanner.getTokens("if (x==9{x=8;};");
    parser = new Parser(tokens);
    parser.parseIfStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testIfStatementError2() throws IOException {
    tokens = scanner.getTokens("if (x==9)x=8;};");
    parser = new Parser(tokens);
    parser.parseIfStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testIfStatementError3() throws IOException {
    tokens = scanner.getTokens("if (x==9){x=8;;");
    parser = new Parser(tokens);
    parser.parseIfStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testMethod() throws IOException {
    tokens = scanner.getTokens("public void test(){x=1;y=2;if(x==y){fehler=1;}");
    parser = new Parser(tokens);
    Method method = parser.parseMethod();
    assertEquals(method.getClass(), Method.class);
    tokens = scanner.getTokens("void test(){x=1;y=2;if(x==y){fehler=1;}");
    parser = new Parser(tokens);
    method = parser.parseMethod();
    assertTrue(method instanceof Method);
    tokens = scanner.getTokens("protected int huber(){while(x>0){}}");
    parser = new Parser(tokens);
    method = parser.parseMethod();
    assertEquals(method.getClass(), Method.class);
    tokens = scanner.getTokens("private double calculateMean(){}");
    parser = new Parser(tokens);
    method = parser.parseMethod();
    assertEquals(method.getClass(), Method.class);
  }

  /** @throws IOException I/O-error */
  @Test
  public void testSelector() throws IOException {
    tokens = scanner.getTokens(".y");
    parser = new Parser(tokens);
    Selector selector = parser.parseSelector();
    assertNotNull(selector);
    assertTrue(selector instanceof FieldSelector);
    assertEquals(((FieldSelector) selector).getIdentifier().getValue(), "y");
    tokens = scanner.getTokens("[x]");
    parser = new Parser(tokens);
    selector = parser.parseSelector();
    assertNotNull(selector);
    assertTrue(selector instanceof ArraySelector);
    assertEquals(
        ((IdFactor) ((ArraySelector) selector).getExpression()).getToken().getValue(), "x");
    tokens = scanner.getTokens("[x].a");
    parser = new Parser(tokens);
    selector = parser.parseSelector();
    assertNotNull(selector);
    assertTrue(selector instanceof ArraySelector);
  }

  @Test
  public void testSimpleExpression() throws IOException {
    tokens = scanner.getTokens("3+4");
    parser = new Parser(tokens);
    Factor factor = parser.parseSimpleExpression();
    assertEquals(factor.getClass(), SimpleExpression.class);
    tokens = scanner.getTokens("3+x*y-7");
    parser = new Parser(tokens);
    factor = parser.parseSimpleExpression();
    assertEquals(factor.getClass(), SimpleExpression.class);
    tokens = scanner.getTokens("-5*7+4");
    parser = new Parser(tokens);
    factor = parser.parseSimpleExpression();
    assertEquals(factor.getClass(), SimpleExpression.class);
    tokens = scanner.getTokens("x*3+7-8/1+0%6");
    parser = new Parser(tokens);
    factor = parser.parseSimpleExpression();
    assertEquals(factor.getClass(), SimpleExpression.class);
  }

  @Test
  public void testStatement() throws IOException {
    tokens = scanner.getTokens("x=5*12-3/6+12;");
    parser = new Parser(tokens);
    Statement statement = parser.parseStatement();
    assertTrue(statement instanceof Statement);
    tokens = scanner.getTokens("if (x==1){ x=2; }");
    parser = new Parser(tokens);
    statement = parser.parseStatement();
    assertTrue(statement instanceof IfStatement);
    tokens = scanner.getTokens("while (x>=0){ x=x-1; }");
    parser = new Parser(tokens);
    statement = parser.parseStatement();
    assertTrue(statement instanceof WhileStatement);
    tokens = scanner.getTokens("do{x=y+1;y=y-1;} while(i>0)");
    parser = new Parser(tokens);
    statement = parser.parseStatement();
    assertTrue(statement instanceof DoStatement);
    tokens = scanner.getTokens("int z");
    parser = new Parser(tokens);
    statement = parser.parseStatement();
    assertTrue(statement instanceof Declaration);
    tokens = scanner.getTokens("int z=300");
    parser = new Parser(tokens);
    statement = parser.parseStatement();
    assertTrue(statement instanceof Declaration);
  }

  @Test
  public void testStatementSequence() throws IOException {
    tokens = scanner.getTokens("x=5*12-3/6+12%4;");
    parser = new Parser(tokens);
    List<Statement> statement = parser.parseStatementSequence();
    assertTrue(statement instanceof List<?>);
    assertEquals(statement.size(), 1);
    tokens = scanner.getTokens("x=5*12-3/6+12%4;abc=9");
    parser = new Parser(tokens);
    statement = parser.parseStatementSequence();
    assertTrue(statement instanceof List<?>);
    assertEquals(statement.size(), 2);
    tokens = scanner.getTokens("y=9%(4+7)*3;");
    parser = new Parser(tokens);
    statement = parser.parseStatementSequence();
    assertTrue(statement instanceof List<?>);
    assertEquals(statement.size(), 1);
    tokens = scanner.getTokens("int x=10;x=x+1;int y=20;y=x+2;double z=x+y");
    parser = new Parser(tokens);
    statement = parser.parseStatementSequence();
    assertTrue(statement instanceof List<?>);
    assertEquals(statement.size(), 5);
    tokens = scanner.getTokens("");
    parser = new Parser(tokens);
    statement = parser.parseStatementSequence();
    assertTrue(statement instanceof List<?>);
    assertEquals(statement.size(), 0);
  }

  /** @throws IOException I/O exception */
  @Test
  public void testSymTable1() throws IOException {
    tokens = scanner.getTokens("int x=100; x=200/100+2;");
    parser = new Parser(tokens);
    parser.parseStatementSequence();
    assertEquals(parser.getSymbolTable().size(), 1);
    assertEquals(parser.getSymbolTable().get("x").getAdress(), 0);
    assertEquals(parser.getSymbolTable().get("x").getValue(), 4);
  }

  @Test
  public void testTerm() throws IOException {
    tokens = scanner.getTokens("3*4");
    parser = new Parser(tokens);
    Factor term = parser.parseTerm();
    assertEquals(term.getClass(), Term.class);
    tokens = scanner.getTokens("x*abc");
    parser = new Parser(tokens);
    term = parser.parseTerm();
    assertEquals(term.getClass(), Term.class);
    tokens = scanner.getTokens("3*y");
    parser = new Parser(tokens);
    term = parser.parseTerm();
    assertEquals(term.getClass(), Term.class);
  }

  @Test
  public void testVarDeclaration() throws IOException {
    tokens = scanner.getTokens("int x=100;");
    parser = new Parser(tokens);
    Declaration declaration = parser.parseDeclaration();
    assertNotNull(declaration);
    assertEquals(declaration.getType(), "INT");
    assertTrue(declaration.hasValue());
    assertEquals(declaration.getName().getValue(), "x");
    assertTrue(declaration.getValue() instanceof IntFactor);
    assertEquals(((IntFactor) declaration.getValue()).getValue(), 100);
    tokens = scanner.getTokens("String martin;");
    parser = new Parser(tokens);
    declaration = parser.parseDeclaration();
    assertNotNull(declaration);
    assertFalse(declaration.hasValue());
    assertEquals(declaration.getType(), "STRING");
    assertEquals(declaration.getName().getValue(), "martin");
  }

  @Test
  public void testWhileStatement() throws IOException {
    tokens = scanner.getTokens("while (x==9){x=8;y=7;};");
    parser = new Parser(tokens);
    WhileStatement whileStatement = parser.parseWhileStatement();
    assertNotNull(whileStatement);
  }

  @Test
  public void testWhileStatementError() throws IOException {
    tokens = scanner.getTokens("while x==9){x=8;};");
    parser = new Parser(tokens);
    parser.parseWhileStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testWhileStatementError1() throws IOException {
    tokens = scanner.getTokens("while (x==9{x=8;};");
    parser = new Parser(tokens);
    parser.parseWhileStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testWhileStatementError2() throws IOException {
    tokens = scanner.getTokens("while (x==9)x=8;};");
    parser = new Parser(tokens);
    parser.parseWhileStatement();
    assertEquals(parser.getErrors().count(), 1);
  }

  @Test
  public void testWhileStatementError3() throws IOException {
    tokens = scanner.getTokens("while (x==9){x=8;;");
    parser = new Parser(tokens);
    parser.parseWhileStatement();
    assertEquals(parser.getErrors().count(), 1);
  }
}
