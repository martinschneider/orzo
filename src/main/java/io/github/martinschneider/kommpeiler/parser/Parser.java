package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.DOUBLE;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.VOID;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.CLASS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.DO;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.IF;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.PACKAGE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.STATIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.WHILE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MINUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.PLUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.TIMES;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.DEFAULT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PROTECTED;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.COMMA;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.DOT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.eof;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.integer;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.scope;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.error.ErrorType;
import io.github.martinschneider.kommpeiler.parser.productions.Argument;
import io.github.martinschneider.kommpeiler.parser.productions.ArraySelector;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import io.github.martinschneider.kommpeiler.parser.productions.ConditionalStatement;
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
import io.github.martinschneider.kommpeiler.scanner.tokens.Comparator;
import io.github.martinschneider.kommpeiler.scanner.tokens.EOF;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Keyword;
import io.github.martinschneider.kommpeiler.scanner.tokens.Num;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scope;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scopes;
import io.github.martinschneider.kommpeiler.scanner.tokens.Str;
import io.github.martinschneider.kommpeiler.scanner.tokens.Sym;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser class for Kommpeiler
 *
 * @author Martin Schneider
 */
public class Parser {
  private CompilerErrors errors = new CompilerErrors();
  private int index;
  private Token token;
  private List<Token> tokenList;

  /**
   * Constructor
   *
   * @param tokenList list of tokens
   */
  public Parser(final List<Token> tokenList) {
    this.tokenList = tokenList;
    if (tokenList != null && tokenList.size() > 0) {
      token = tokenList.get(0);
    }
  }

  public CompilerErrors getErrors() {
    return errors;
  }

  /**
   * reads the next token
   *
   * <p>returns false if EOF and true otherwise
   */
  private boolean nextToken() {
    index++;
    if (index >= tokenList.size()) {
      token = eof();
      return false;
    } else {
      token = tokenList.get(index);
      return true;
    }
  }

  private void insertToken(Token token) {
    tokenList.add(index + 1, token);
  }

  /** Parse a list of tokens. */
  public void parse() {
    parseClass();
  }

  private List<Argument> parseArguments() {
    List<Argument> arguments = new ArrayList<>();
    while (!token.eq(sym(RPAREN))) {
      String type = null;
      Identifier name = null;
      if (!(token instanceof Type)) {
        break;
      }
      if (token.eq(type("STRING"))) {
        type = "Ljava/lang/String;";
      } else {
        type = token.getValue().toString();
      }
      nextToken();
      if (token.eq(sym(LBRAK))) {
        nextToken();
        if (token.eq(sym(RBRAK))) {
          type = "[" + type;
        } else {
          errors.addParserError("missing ] in type declaration");
        }
      }
      nextToken();
      if (token instanceof Identifier) {
        name = (Identifier) token;
      }
      arguments.add(new Argument(type, name));
      nextToken();
      if (token.eq(sym(COMMA))) {
        nextToken();
      } else {
        break;
      }
    }
    return arguments;
  }

  /**
   * assignment = identifier selector "=" expression
   *
   * @return Assignment
   */
  public Assignment parseAssignment() {
    Identifier left;
    Expression right;
    if (token instanceof Identifier) {
      left = (Identifier) token;
    } else {
      return null;
    }
    nextToken();
    Selector selector = parseSelector();
    if (selector != null) {
      left.setSelector(selector);
      nextToken();
    }
    if (token instanceof Operator && token.eq(op(ASSIGN))) {
      nextToken();
      if ((right = parseExpression()) == null) {
        previousToken();
      } else {
        Assignment assignment = new Assignment(left, right);
        return assignment;
      }
    } else {
      previousToken();
      return null;
    }
    return null;
  }

  public Clazz parseClass() {
    int saveIndex = index;
    Identifier name;
    List<Method> body;
    String packageDeclaration = parsePackageDeclaration();
    Scope scope = parseScope();
    if (scope != null) {
      nextToken();
    }
    if (token instanceof Keyword) {
      if (token.eq(keyword(CLASS))) {
        nextToken();
        if (token instanceof Identifier) {
          name = (Identifier) token;
        } else {
          name = null;
          errors.addParserError("identifier expected");
        }
        nextToken();
        if (!token.eq(sym(LBRACE))) {
          previousToken();
          errors.addParserError("class-declaration must be followed by {");
        }
        nextToken();
        body = parseClassBody();
        if (body == null) {
          errors.addParserError("invalid class body");
        }
        if (!token.eq(sym(RBRACE))) {
          previousToken();
          errors.addParserError("method must be closed by }");
        }
        return new Clazz(packageDeclaration, scope, name, body);
      }
    }
    index = saveIndex;
    token = tokenList.get(index);
    return null;
  }

  private Scope parseScope() {
    if (token instanceof Scope) {
      switch ((Scopes) token.getValue()) {
        case PUBLIC:
          return scope(PUBLIC);
        case PRIVATE:
          return scope(PRIVATE);
        case PROTECTED:
          return scope(PROTECTED);
        default:
          return scope(DEFAULT);
      }
    }
    return null;
  }

  /**
   * Parse the class body
   *
   * @return list of methods
   */
  public List<Method> parseClassBody() {
    List<Method> classBody = new ArrayList<>();
    Method method;
    while ((method = parseMethod()) != null) {
      classBody.add(method);
      nextToken();
    }
    if (!classBody.isEmpty()) {
      return classBody;
    } else {
      return new ArrayList<>();
    }
  }

  public Condition parseCondition() {
    Expression left;
    Comparator operator;
    Expression right;
    left = parseExpression();
    if (left != null && token instanceof Comparator) {
      operator = (Comparator) token;
      nextToken();
      right = parseExpression();
      if (right != null) {
        return new Condition(left, operator, right);
      }
    }
    return null;
  }

  /**
   * declaration = basicType identifier ["=" value]
   *
   * @return Declaration
   */
  public Declaration parseDeclaration() {
    Type type;
    Identifier name;
    Expression value;
    if (token instanceof Type && !token.eq(type(VOID))) {
      type = (Type) token;
      nextToken();
      if (token instanceof Identifier) {
        name = (Identifier) token;
        nextToken();
        if (token instanceof Operator && token.eq(op(ASSIGN))) {
          nextToken();
          if ((value = parseExpression()) != null) {
            return new Declaration(name, type, value, true);
          }
          previousToken();
        }
        return new Declaration(name, type, null, false);
      } else {
        previousToken();
      }
    }
    return null;
  }

  /**
   * doStatement = "do" "(" statementSequence ")" "while" "(" expression ")"
   *
   * @return DoStatement
   */
  public DoStatement parseDoStatement() {
    Condition condition;
    List<Statement> body;
    if (token == null) {
      return null;
    }
    if (token instanceof Keyword && token.eq(keyword(DO))) {
      nextToken();
      if (!token.eq(sym(LBRACE))) {
        previousToken();
        errors.addParserError("do must be followed by {");
      }
      nextToken();
      body = parseStatementSequence();
      if (body == null) {
        previousToken();
        errors.addParserError("do{ must be followed by a valid statement sequence");
      }
      if (!token.eq(sym(RBRACE))) {
        previousToken();
        errors.addParserError("missing } in do-clause");
      }
      nextToken();
      if (!(token instanceof Keyword && token.eq(keyword(WHILE)))) {
        previousToken();
        errors.addParserError("missing while in do-clause");
      }
      nextToken();
      if (!token.eq(sym(LPAREN))) {
        previousToken();
        errors.addParserError("missing ( in do-clause");
      }
      nextToken();
      condition = parseCondition();
      if (condition == null) {
        previousToken();
        errors.addParserError("invalid condition in do-clause");
      }
      nextToken();
      if (!token.eq(sym(RPAREN))) {
        previousToken();
        errors.addParserError("missing ) in do-clause");
      }
      return new DoStatement(condition, body);
    } else {
      return null;
    }
  }

  public Expression parseExpression() {
    Expression expression = new Expression();
    boolean negative = false;
    if (token instanceof Operator && token.eq(op(MINUS))) {
      negative = true;
      nextToken();
      if (token instanceof Num) {
        Num number = (Num) token;
        if (negative) {
          number.changeSign();
        }
        expression.addToken(token);
        nextToken();
      } else if (token instanceof Identifier) {
        expression.addToken(sym(LPAREN));
        expression.addToken(integer(-1));
        expression.addToken(sym(RPAREN));
        expression.addToken(op(TIMES));
        expression.addToken(token);
      } else {
        errors.addParserError("Unexpected symbol " + token + " after starting \"-\" in expression");
        return null;
      }
    }
    // TODO: support parentheses
    while (token instanceof Num
        || token instanceof Str
        || token instanceof Identifier
        || token instanceof Operator) {
      expression.addToken(token);
      nextToken();
    }
    return (expression.size() > 0) ? expression : null;
  }

  /**
   * ifStatement = "if" "(" expression ")" "{" statementSequence "}" {elseifBlock} [elseBlock]
   *
   * @return IfStatement
   */
  public IfStatement parseIfStatement() {
    Condition condition;
    List<Statement> body;
    if (token == null) {
      return null;
    }
    if (token instanceof Keyword && token.eq(keyword(IF))) {
      nextToken();
      if (!token.eq(sym(LPAREN))) {
        previousToken();
        errors.addParserError("if must be followed by (");
      }
      nextToken();
      condition = parseCondition();
      if (condition == null) {
        previousToken();
        errors.addParserError("if( must be followed by a valid expression");
      }
      if (!token.eq(sym(RPAREN))) {
        previousToken();
        errors.addParserError("missing ) in if-clause");
      }
      nextToken();
      if (!token.eq(sym(LBRACE))) {
        previousToken();
        errors.addParserError("missing { in if-clause");
      }
      nextToken();
      body = parseStatementSequence();
      if (body == null) {
        errors.addParserError("invalid body of if-clause");
      }
      if (!token.eq(sym(RBRACE))) {
        previousToken();
        errors.addParserError("missing } in if-clause");
      } else {
        insertToken(sym(SEMICOLON));
        nextToken();
      }
      return new IfStatement(condition, body);
    } else {
      return null;
    }
  }

  /**
   * method = [scope] returnType identifier "{" StatementSequence "}"
   *
   * @return method
   */
  // CHECKSTYLE:OFF
  public Method parseMethod()
        // CHECKSTYLE:ON
      {
    int saveIndex = index;
    Scope scope = scope(DEFAULT);
    Type type;
    Identifier name;
    List<Argument> arguments = new ArrayList<>();
    List<Statement> body;
    if (token instanceof Scope) {
      scope = (Scope) token;
      nextToken();
    }
    if (token instanceof Keyword && token.eq(keyword(STATIC))) {
      // TODO: handle static (for now we just ignore it)
      nextToken();
    }
    if (token instanceof Type) {
      if (token.eq(type(INT)) || token.eq(type(DOUBLE)) || token.eq(type(VOID))) {
        type = (Type) token;
        nextToken();
      } else {
        index = saveIndex;
        nextToken();
        return null;
      }
      if (token instanceof Identifier) {
        name = (Identifier) token;
      } else {
        name = null;
        errors.addParserError("identifier expected");
      }
      nextToken();
      if (!token.eq(sym(LPAREN))) {
        previousToken();
        errors.addParserError("missing ( in method-declaration");
      }
      nextToken();
      arguments = parseArguments();
      if (!token.eq(sym(RPAREN))) {
        previousToken();
        errors.addParserError("missing ) in method-declaration");
      }
      nextToken();
      if (!token.eq(sym(LBRACE))) {
        previousToken();
        errors.addParserError("method-declaration must be followed by {");
      }
      nextToken();
      body = parseStatementSequence();
      if (body == null) {
        errors.addParserError("invalid method body");
      }
      if (!token.eq(sym(RBRACE))) {
        previousToken();
        errors.addParserError("method must be closed by }");
      }
      return new Method(scope, type, name, arguments, body);
    } else {
      return null;
    }
  }

  /**
   * MethodCall = ident [parameters]
   *
   * @return method call
   */
  public MethodCall parseMethodCall() {
    List<Expression> parameters;
    List<Identifier> names = new ArrayList<>();
    if (token instanceof Identifier) {
      do {
        names.add((Identifier) token);
        nextToken();
      } while ((token.eq(sym(DOT)) && nextToken()));
      parameters = parseParameters();
      return new MethodCall(names, parameters);
    }
    return null;
  }

  private String parsePackageDeclaration() {
    if (token.eq(keyword(PACKAGE))) {
      nextToken();
      StringBuilder packageName = new StringBuilder();
      while (!token.eq(sym(SEMICOLON)) && !(token instanceof EOF)) {
        if (token.eq(sym(DOT))) {
          packageName.append('.');
        } else if (token instanceof Identifier) {
          packageName.append(token.getValue());
        } else {
          errors.addParserError("Invalid token " + token + " in package declaration.");
          return packageName.toString();
        }
        nextToken();
      }
      nextToken();
      return packageName.toString();
    }
    return null;
  }

  /**
   * parameters = "(" [expression {"," expression}] ")"
   *
   * @return parameters
   */
  public List<Expression> parseParameters() {
    List<Expression> parameters = new ArrayList<>();
    if (token.eq(sym(LPAREN))) {
      nextToken();
      Expression factor;
      if ((factor = parseExpression()) != null) {
        parameters.add(factor);
      }
      while (token instanceof Sym && token.eq(sym(COMMA))) {
        nextToken();
        if ((factor = parseExpression()) != null) {
          parameters.add(factor);
        }
        // FIXME: else
      }
      nextToken();
      if (!token.eq(sym(RPAREN))) {
        errors.addError(") expected", ErrorType.PARSER);
      }
      return parameters;
    }
    // else
    return null;
  }

  /**
   * selector = {"." identifier | "[" expression "]"}
   *
   * @return Selector
   */
  public Selector parseSelector() {
    if (token instanceof Sym && token.eq(sym(DOT))) {
      nextToken();
      if (token instanceof Identifier) {
        return new FieldSelector((Identifier) token);
      } else {
        errors.addError("identifier expected", ErrorType.PARSER);
        previousToken();
      }
    } else if (token instanceof Sym && token.eq(sym(LBRAK))) {
      nextToken();
      Expression expression = parseExpression();
      if (expression != null) {
        if (token instanceof Sym && token.eq(sym(RBRAK))) {
          return new ArraySelector(expression);
        } else {
          errors.addError("] expected", ErrorType.PARSER);
        }
      } else {
        errors.addError("expression expected", ErrorType.PARSER);
      }
    }
    return null;
  }

  /**
   * A simple expression may start with "+" or "-". This is checked here.
   *
   * @return 1 if a + or no symbol is detected (that means the value of the first term in the
   *     expression is positive), -1 for a - and 0 for any other operator (not allowed for a simple
   *     expression)
   */
  public int parseSimpleExpressionSign() {
    Token symbol;
    if (index >= tokenList.size()) {
      return 0;
    }
    if ((symbol = tokenList.get(index)) instanceof Operator) {
      if (symbol.eq(op(MINUS))) {
        nextToken();
        return -1;
      } else if (symbol.eq(op(PLUS))) {
        nextToken();
        return 1;
      } else {
        previousToken();
        return 0;
      }
    }
    return 1;
  }

  /**
   * statement = [ assignment declaration | ifStatement | whileStatement | doStatement]
   *
   * @return Assignment
   */
  public Statement parseStatement() {
    Assignment assignment;
    ConditionalStatement conditionalStatement;
    Declaration declaration;
    MethodCall methodCall;
    int idx = savePointer();
    if ((assignment = parseAssignment()) != null) {
      if (token instanceof Sym && token.eq(sym(SEMICOLON))) {
        return new Assignment(assignment.getLeft(), assignment.getRight());
      } else {
        return assignment;
      }
    } else if (restorePointer(idx) && (conditionalStatement = parseIfStatement()) != null) {
      return conditionalStatement;
    } else if (restorePointer(idx) && (conditionalStatement = parseDoStatement()) != null) {
      return conditionalStatement;
    } else if (restorePointer(idx) && (conditionalStatement = parseWhileStatement()) != null) {
      return conditionalStatement;
    } else if (restorePointer(idx) && (declaration = parseDeclaration()) != null) {
      return declaration;
    } else if (restorePointer(idx) && (methodCall = parseMethodCall()) != null) {
      return methodCall;
    } else {
      return null;
    }
  }

  /**
   * StatementSequence = statement {";" statement }
   *
   * @return StatementSequence
   */
  public List<Statement> parseStatementSequence() {
    List<Statement> statementSequence = new ArrayList<>();
    Statement statement;
    if ((statement = parseStatement()) != null) {
      statementSequence.add(statement);
    }
    while (token.eq(sym(SEMICOLON))) {
      nextToken();
      if ((statement = parseStatement()) != null) {
        statementSequence.add(statement);
      }
      // FIXME: else
    }
    return statementSequence;
  }

  /**
   * whileStatement = "while" "(" expression ")" "{" statementSequence "}"
   *
   * @return WhileStatement
   */
  public WhileStatement parseWhileStatement() {
    Condition condition;
    List<Statement> body;
    if (token == null) {
      return null;
    }
    if (token instanceof Keyword && token.eq(keyword(WHILE))) {
      nextToken();
      if (!token.eq(sym(LPAREN))) {
        previousToken();
        errors.addParserError("while must be followed by (");
      }
      nextToken();
      condition = parseCondition();
      if (condition == null) {
        previousToken();
        errors.addParserError("while( must be followed by a valid expression");
      }
      nextToken();
      if (!token.eq(sym(RPAREN))) {
        previousToken();
        errors.addParserError("missing ) in while-clause");
      }
      nextToken();
      if (!token.eq(sym(LBRACE))) {
        previousToken();
        errors.addParserError("missing { in while-clause");
      }
      nextToken();
      body = parseStatementSequence();
      if (body == null) {
        errors.addParserError("invalid body of while-clause");
      }
      if (!token.eq(sym(RBRACE))) {
        errors.addParserError("Missing } in while-clause");
      }
      return new WhileStatement(condition, body);
    } else {
      return null;
    }
  }

  /** reads the previous token */
  private void previousToken() {
    index--;
    if (index >= tokenList.size()) {
      token = new Token("EOF");
    } else {
      token = tokenList.get(index);
    }
  }

  private boolean restorePointer(int idx) {
    index = idx;
    if (idx >= tokenList.size()) {
      token = new Token("EOF");
    } else {
      token = tokenList.get(idx);
    }
    return true;
  }

  /** helper methods * */
  private int savePointer() {
    return index;
  }
}
