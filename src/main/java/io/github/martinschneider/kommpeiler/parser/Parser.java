package io.github.martinschneider.kommpeiler.parser;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.error.ErrorType;
import io.github.martinschneider.kommpeiler.parser.productions.Argument;
import io.github.martinschneider.kommpeiler.parser.productions.ArraySelector;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.BasicType;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.ConditionalStatement;
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
import io.github.martinschneider.kommpeiler.parser.productions.Scope;
import io.github.martinschneider.kommpeiler.parser.productions.Selector;
import io.github.martinschneider.kommpeiler.parser.productions.SimpleExpression;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.StringFactor;
import io.github.martinschneider.kommpeiler.parser.productions.Term;
import io.github.martinschneider.kommpeiler.parser.productions.WhileStatement;
import io.github.martinschneider.kommpeiler.parser.symboltable.SymbolTable;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Keyword;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Str;
import io.github.martinschneider.kommpeiler.scanner.tokens.Sym;
import io.github.martinschneider.kommpeiler.scanner.tokens.SymbolType;
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
  private SymbolTable symbolTable = new SymbolTable();
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

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  /**
   * @param token
   * @return true if the token is a legal operator in an expression
   */
  private boolean isExprOp(final Token token) {
    if (token.getValue().equals("EQUAL")
        || token.getValue().equals("NOTEQUAL")
        || token.getValue().equals("SMALLER")
        || token.getValue().equals("GREATER")
        || token.getValue().equals("SMALLEREQ")
        || token.getValue().equals("GREATEREQ")) {
      return true;
    }
    return false;
  }

  /**
   * @param token
   * @return true if the token is a legal operator in a factor
   */
  private boolean isFacOp(final Token token) {
    if (token.getValue().equals("TIMES")
        || token.getValue().equals("DIV")
        || token.getValue().equals("MOD")) {
      return true;
    }
    return false;
  }

  /**
   * @param token
   * @return true if the token is a legal operator in a SimpleExpression
   */
  private boolean isSimpleExprOp(final Token token) {
    if (token.getValue().equals("PLUS") || token.getValue().equals("MINUS")) {
      return true;
    }
    return false;
  }

  /**
   * reads the next token
   *
   * <p>returns false if EOF and true otherwise
   */
  private boolean nextToken() {
    index++;
    if (index < tokenList.size()) {
      token = tokenList.get(index);
      return true;
    } else {
      // FIXME
      token = new Token("EOF"); // EOF
      return false;
    }
  }

  /** Parse a list of tokens. */
  public void parse() {
    parseClass();
  }

  private List<Argument> parseArguments() {
    List<Argument> arguments = new ArrayList<>();
    while (!token.getValue().equals("RPAREN")) {
      String type = null;
      Identifier name = null;
      switch (token.getValue()) {
        case "STRING":
          type = "Ljava/lang/String;";
          break;
        default:
          type = token.getValue();
      }
      nextToken();
      if (token.getValue().equals("LBRAK")) {
        nextToken();
        if (token.getValue().equals("RBRAK")) {
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
      if (token.getValue().equals(",")) {
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
    Factor right;
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
    if (token instanceof Operator && token.getValue().equals("ASSIGN")) {
      nextToken();
      if ((right = parseExpression()) == null) {
        previousToken();
      } else {
        symbolTable.update(left.toString(), right);
        if (right instanceof Term) {
          // ((Term)right).generateCode(instructionList, registers);
        }
        Assignment assignment = new Assignment(left, right);
        // assignment.generateCode(instructionList, registers);
        return assignment;
      }
    } else {
      previousToken();
      return null;
    }
    return null;
  }

  /**
   * class = [scope] "class" identifier "{" {method} "}"
   *
   * @return Clazz
   */
  public Clazz parseClass() {
    int saveIndex = index;
    Scope scope = Scope.PRIVATE;
    Identifier name;
    List<Method> body;
    String packageName = parsePackageDeclaration();
    if (token.getClass().equals(Keyword.class)) {
      if (token.getValue().equals("PUBLIC")) {
        scope = Scope.PUBLIC;
        nextToken();
      } else if (token.getValue().equals("PRIVATE")) {
        scope = Scope.PRIVATE;
        nextToken();
      } else if (token.getValue().equals("PROTECTED")) {
        scope = Scope.PROTECTED;
        nextToken();
      }
      if (token.getValue().equals("CLASS")) {
        nextToken();
        if (token.getClass().equals(Identifier.class)) {
          name = (Identifier) token;
        } else {
          name = null;
          errors.addParserError("identifier expected");
        }
        nextToken();
        if (!token.getValue().equals("LBRACE")) {
          previousToken();
          errors.addParserError("class-declaration must be followed by {");
        }
        nextToken();
        body = parseClassBody();
        if (body == null) {
          errors.addParserError("invalid class body");
        }
        if (!token.getValue().equals("RBRACE")) {
          previousToken();
          errors.addParserError("method must be closed by }");
        }
        return new Clazz(packageName, scope, name, body);
      }
    }
    index = saveIndex;
    token = tokenList.get(index);
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

  /**
   * declaration = basicType identifier ["=" value]
   *
   * @return Declaration
   */
  public Declaration parseDeclaration() {
    String type;
    Identifier name;
    Factor value;
    if (token.getValue() != "void") {
      type = token.getValue();
      nextToken();
      if (token instanceof Identifier) {
        name = (Identifier) token;
        nextToken();
        if (token instanceof Operator && token.getValue().equals("ASSIGN")) {
          nextToken();
          if ((value = parseSimpleExpression()) != null) {
            return new Declaration(name, type, value, true);
          } else {
            previousToken();
          }
        } else {
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
  // CHECKSTYLE:OFF
  public DoStatement parseDoStatement() {
    // CHECKSTYLE:ON
    Factor condition;
    List<Statement> body;
    if (token == null) {
      return null;
    }
    if (token.getClass().equals(Keyword.class) && token.getValue().equals("DO")) {
      nextToken();
      if (!token.getValue().equals("LBRACE")) {
        previousToken();
        errors.addParserError("do must be followed by {");
      }
      nextToken();
      body = parseStatementSequence();
      if (body == null) {
        previousToken();
        errors.addParserError("do{ must be followed by a valid statement sequence");
      }
      if (!token.getValue().equals("RBRACE")) {
        previousToken();
        errors.addParserError("missing } in do-clause");
      }
      nextToken();
      if (!(token.getClass().equals(Keyword.class) && token.getValue().equals("WHILE"))) {
        previousToken();
        errors.addParserError("missing while in do-clause");
      }
      nextToken();
      if (!token.getValue().equals("LPAREN")) {
        previousToken();
        errors.addParserError("missing ( in do-clause");
      }
      nextToken();
      condition = parseExpression();
      if (condition == null) {
        previousToken();
        errors.addParserError("invalid condition in do-clause");
      }
      nextToken();
      if (!token.getValue().equals("RPAREN")) {
        previousToken();
        errors.addParserError("missing ) in do-clause");
      }
      return new DoStatement(condition, body);
    } else {
      return null;
    }
  }

  /**
   * expression = simpleExpression [ ("==", "<", ">", "<=", ">=", "!=") simpleExpression ]
   *
   * @return Factor
   */
  public Factor parseExpression() {
    Factor left;
    Factor right;
    Token operator;
    if ((left = parseSimpleExpression()) == null) {
      return left;
    } else {
      index++;
      if (index < tokenList.size()) {
        token = tokenList.get(index);
      } else {
        return left;
      }
      if ((token instanceof Operator) && isExprOp(token)) {
        operator = token;
      } else {
        previousToken();
        return left;
      }
      nextToken();
      int saveIndex = index;
      if ((right = parseSimpleExpression()) == null) {
        index = saveIndex;
        if ((right = parseTerm()) == null) {
          previousToken();
          return null;
        } else {
          return new Expression(left, operator, right);
        }
      } else {
        return new Expression(left, operator, right);
      }
    }
  }

  /**
   * factor = identifier selector | number | "(" expression ")"
   *
   * @return Factor
   */
  public Factor parseFactor() {
    if (token.getValue().equals("MINUS")) {
      int pointer = savePointer();
      nextToken();
      Factor factor = parseFactor();
      if (factor != null && factor instanceof IntFactor) {
        return new IntFactor(((IntNum) token).parseValue() * -1);
      } else {
        restorePointer(pointer);
        return null;
      }
    } else if (token instanceof Identifier) {
      Identifier identifier = (Identifier) token;
      nextToken();
      Selector selector = parseSelector();
      if (selector != null) {
        identifier.setSelector(selector);
        nextToken();
      } else {
        previousToken();
      }
      return new IdFactor(identifier);
    } else if (token instanceof IntNum) {
      return new IntFactor(((IntNum) token).parseValue());
    } else if (token instanceof Str) {
      return new StringFactor(((Str) token).getValue());
    } else {
      int saveIndex = index;
      if (!token.getValue().equals("LPAREN")) {
        return null;
      }
      nextToken();
      Factor expression = parseExpression();
      if (expression == null) {
        return null;
      }
      nextToken();
      if (token.getValue().equals("RPAREN")) {
        return new ExpressionFactor(expression);
      }
      index = saveIndex;
      token = tokenList.get(index);
      return null;
    }
  }

  /**
   * ifStatement = "if" "(" expression ")" "{" statementSequence "}" {elseifBlock} [elseBlock]
   *
   * @return IfStatement
   */
  public IfStatement parseIfStatement() {
    Factor condition;
    List<Statement> body;
    if (token == null) {
      return null;
    }
    if (token.getClass().equals(Keyword.class) && token.getValue().equals("IF")) {
      nextToken();
      if (!token.getValue().equals("LPAREN")) {
        previousToken();
        errors.addParserError("if must be followed by (");
      }
      nextToken();
      condition = parseExpression();
      if (condition == null) {
        previousToken();
        errors.addParserError("if( must be followed by a valid expression");
      }
      nextToken();
      if (!token.getValue().equals("RPAREN")) {
        previousToken();
        errors.addParserError("missing ) in if-clause");
      }
      nextToken();
      if (!token.getValue().equals("LBRACE")) {
        previousToken();
        errors.addParserError("missing { in if-clause");
      }
      nextToken();
      body = parseStatementSequence();
      if (body == null) {
        errors.addParserError("invalid body of if-clause");
      }
      if (!token.getValue().equals("RBRACE")) {
        previousToken();
        errors.addParserError("missing } in if-clause");
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
    Scope scope = Scope.PRIVATE;
    BasicType type;
    Identifier name;
    List<Argument> arguments = new ArrayList<>();
    List<Statement> body;
    if ((token instanceof Keyword) || (token instanceof SymbolType)) {
      if (token.getValue().equals("PUBLIC")) {
        scope = Scope.PUBLIC;
        nextToken();
      } else if (token.getValue().equals("PRIVATE")) {
        scope = Scope.PRIVATE;
        nextToken();
      } else if (token.getValue().equals("PROTECTED")) {
        scope = Scope.PROTECTED;
        nextToken();
      }
      if (token.getValue().equals("STATIC")) {
        // TODO: handle static (for now we just ignore it)
        nextToken();
      }
      if (token.getValue().equals("INT")) {
        type = BasicType.INT;
        nextToken();
      } else if (token.getValue().equals("DOUBLE")) {
        type = BasicType.DOUBLE;
        nextToken();
      } else if (token.getValue().equals("VOID")) {
        type = BasicType.VOID;
        nextToken();
      } else {
        index = saveIndex;
        nextToken();
        return null;
      }
      if (token.getClass().equals(Identifier.class)) {
        name = (Identifier) token;
      } else {
        name = null;
        errors.addParserError("identifier expected");
      }
      nextToken();
      if (!token.getValue().equals("LPAREN")) {
        previousToken();
        errors.addParserError("missing ( in method-declaration");
      }
      nextToken();
      arguments = parseArguments();
      if (!token.getValue().equals("RPAREN")) {
        previousToken();
        errors.addParserError("missing ) in method-declaration");
      }
      nextToken();
      if (!token.getValue().equals("LBRACE")) {
        previousToken();
        errors.addParserError("method-declaration must be followed by {");
      }
      nextToken();
      body = parseStatementSequence();
      if (body == null) {
        errors.addParserError("invalid method body");
      }
      if (!token.getValue().equals("RBRACE")) {
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
    List<Factor> parameters;
    List<Identifier> names = new ArrayList<>();
    if (token instanceof Identifier) {
      do {
        names.add((Identifier) token);
        nextToken();
      } while ((token.getValue().equals("DOT") && nextToken()));
      parameters = parseParameters();
      return new MethodCall(names, parameters);
    }
    return null;
  }

  private String parsePackageDeclaration() {
    if (token.getValue().equals("PACKAGE")) {
      nextToken();
      StringBuilder packageName = new StringBuilder();
      while (!token.getValue().equals("SEMICOLON") && !token.getValue().equals("EOF")) {
        if (token.getValue().equals("DOT")) {
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
  public List<Factor> parseParameters() {
    List<Factor> parameters = new ArrayList<>();
    if (token.getValue().equals("LPAREN")) {
      nextToken();
      Factor factor;
      if ((factor = parseFactor()) != null) {
        parameters.add(factor);
      }
      nextToken();
      while (token instanceof Sym && ((Sym) token).getValue().equals("COMMA")) {
        nextToken();
        if ((factor = parseFactor()) != null) {
          parameters.add(factor);
        }
        nextToken();
        // FIXME: else
      }
      nextToken();
      if (!token.getValue().equals("RPAREN")) {
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
    if (token.getValue().equals("DOT")) {
      nextToken();
      if (token instanceof Identifier) {
        return new FieldSelector((Identifier) token);
      } else {
        errors.addError("identifier expected", ErrorType.PARSER);
        previousToken();
      }
    } else if (token instanceof Sym && token.getValue().equals("LBRAK")) {
      nextToken();
      Factor expression = parseExpression();
      if (expression != null) {
        nextToken();
        if (token instanceof Sym && token.getValue().equals("RBRAK")) {
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
   * simpleExpression = ["+"|"-"] term {("+"|"-") term}.
   *
   * @return Factor
   */
  public Factor parseSimpleExpression() {
    Factor left;
    Factor right;
    Operator operator;
    int factor = parseSimpleExpressionSign();
    if (factor == 0) // simple expression starts with an invalid operator (only + and - are
    // allowed)
    {
      return null;
    }
    if ((left = parseTerm()) == null) // no term
    {
      return null;
    } else {
      index++;
      if (index < tokenList.size()) {
        token = tokenList.get(index);
      } else {
        return left;
      }
      if ((token instanceof Operator) && isSimpleExprOp(token)) {
        operator = (Operator) token;
      } else {
        previousToken();
        if (factor == -1) {
          previousToken();
          return null;
        } else {
          return left;
        }
      }
      nextToken();
      int saveIndex = index;
      if ((right = parseSimpleExpression()) == null) {
        index = saveIndex;
        if ((right = parseTerm()) == null) {
          previousToken();
          return null;
        } else {
          // FIXME: exchange left with new Term(left,times,-1)
          ((Term) left).setValue(((Term) left).getIntValue() * factor);
          return new SimpleExpression(left, operator, right);
        }
      } else {
        return new SimpleExpression(left, operator, right);
      }
    }
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
    if ((symbol = tokenList.get(index)).getClass().equals(Operator.class)) {
      if (symbol.getValue().equals("MINUS")) {
        nextToken();
        return -1;
      } else if (symbol.getValue().equals("PLUS")) {
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
      nextToken();
      if (token.getClass().equals(Sym.class) && token.getValue().equals("SEMICOLON")) {
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
      nextToken();
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
    while (token instanceof Sym && ((Sym) token).getValue().equals("SEMICOLON")) {
      nextToken();
      if ((statement = parseStatement()) != null) {
        statementSequence.add(statement);
      }
      // FIXME: else
    }
    return statementSequence;
  }

  /**
   * term = factor {("*" | "/" | "%") factor}
   *
   * @return Factor
   */
  public Factor parseTerm() {
    Factor left;
    Factor right;
    Token operator;
    if ((left = parseFactor()) == null) {
      return null;
    } else {
      index++;
      if (index < tokenList.size()) {
        token = tokenList.get(index);
      } else {
        return left;
      }
      if ((token instanceof Operator) && isFacOp(token)) {
        operator = token;
      } else {
        previousToken();
        return left;
      }
      nextToken();
      if ((right = parseTerm()) == null) {
        if ((right = parseFactor()) == null) {
          previousToken();
          return null;
        } else {
          return new Term(left, operator, right);
        }
      } else {
        return new Term(left, operator, right);
      }
    }
  }

  /**
   * whileStatement = "while" "(" expression ")" "{" statementSequence "}"
   *
   * @return WhileStatement
   */
  public WhileStatement parseWhileStatement() {
    Factor condition;
    List<Statement> body;
    if (token == null) {
      return null;
    }
    if (token.getClass().equals(Keyword.class) && token.getValue().equals("WHILE")) {
      nextToken();
      if (!token.getValue().equals("LPAREN")) {
        previousToken();
        errors.addParserError("while must be followed by (");
      }
      nextToken();
      condition = parseExpression();
      if (condition == null) {
        previousToken();
        errors.addParserError("while( must be followed by a valid expression");
      }
      nextToken();
      if (!token.getValue().equals("RPAREN")) {
        previousToken();
        errors.addParserError("missing ) in while-clause");
      }
      nextToken();
      if (!token.getValue().equals("LBRACE")) {
        previousToken();
        errors.addParserError("missing { in while-clause");
      }
      nextToken();
      body = parseStatementSequence();
      if (body == null) {
        errors.addParserError("invalid body of while-clause");
      }
      if (!token.getValue().equals("RBRACE")) {
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
    token = tokenList.get(index);
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
