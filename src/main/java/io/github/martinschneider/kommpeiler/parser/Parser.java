package io.github.martinschneider.kommpeiler.parser;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.BREAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.CLASS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.DO;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.ELSE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.FOR;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.IF;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.PACKAGE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.RETURN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.STATIC;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Keywords.WHILE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.BITWISE_AND;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.BITWISE_AND_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.BITWISE_OR;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.BITWISE_OR_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.BITWISE_XOR;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.BITWISE_XOR_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.DIV;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.DIV_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.LSHIFT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.LSHIFT_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MINUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MINUS_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MOD;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MOD_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.PLUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.PLUS_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.RSHIFT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.RSHIFTU_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.RSHIFT_ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.TIMES;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.TIMES_ASSIGN;
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

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.error.ErrorType;
import io.github.martinschneider.kommpeiler.parser.productions.Argument;
import io.github.martinschneider.kommpeiler.parser.productions.ArraySelector;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Break;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.DoStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.FieldSelector;
import io.github.martinschneider.kommpeiler.parser.productions.ForStatement;
import io.github.martinschneider.kommpeiler.parser.productions.IfBlock;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.parser.productions.LoopStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.ParallelAssignment;
import io.github.martinschneider.kommpeiler.parser.productions.ReturnStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Selector;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
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
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import io.github.martinschneider.kommpeiler.scanner.tokens.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Parser {
  private CompilerErrors errors = new CompilerErrors();
  private int index;
  private Token token;
  private List<Token> tokenList;

  public Parser(final List<Token> tokenList) {
    this.tokenList = tokenList;
    if (tokenList != null && tokenList.size() > 0) {
      token = tokenList.get(0);
    } else {
      token = eof();
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
      type = ((Type) token).getName();
      nextToken();
      if (token.eq(sym(LBRAK))) {
        nextToken();
        if (token.eq(sym(RBRAK))) {
          type = "[" + type;
        } else {
          errors.addParserError("missing ] in type declaration");
        }
        nextToken();
      }
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

  public ParallelAssignment parseParallelAssignment() {
    List<Identifier> left = new ArrayList<>();
    List<Expression> right = new ArrayList<>();
    while (token instanceof Identifier || token.eq(sym(COMMA))) {
      if (token instanceof Identifier) {
        left.add((Identifier) token);
      }
      nextToken();
    }
    if (!token.eq(op(ASSIGN))) {
      return null;
    }
    nextToken();
    Expression expression;
    while ((expression = parseExpression()) != null || token.eq(sym(COMMA))) {
      if (expression != null) {
        right.add(expression);
      } else {
        nextToken();
      }
    }
    if (left.size() != right.size()) {
      errors.addParserError(
          "left and right side must have the same number of variables in parallel assignment");
      return null;
    }
    return new ParallelAssignment(left, right);
  }

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
    if (token instanceof Operator) {
      if (token.eq(op(ASSIGN))) {
        nextToken();
      } else if (token.eq(op(POST_INCREMENT)) || token.eq(op(POST_DECREMENT))) {
        previousToken();
      } else if (token.eq(op(PLUS_ASSIGN))) {
        insertToken(op(PLUS));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(MINUS_ASSIGN))) {
        insertToken(op(MINUS));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(TIMES_ASSIGN))) {
        insertToken(op(TIMES));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(DIV_ASSIGN))) {
        insertToken(op(DIV));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(MOD_ASSIGN))) {
        insertToken(op(MOD));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(LSHIFT_ASSIGN))) {
        insertToken(op(LSHIFT));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(RSHIFT_ASSIGN))) {
        insertToken(op(RSHIFT));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(RSHIFTU_ASSIGN))) {
        insertToken(op(RSHIFTU));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(BITWISE_AND_ASSIGN))) {
        insertToken(op(BITWISE_AND));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(BITWISE_OR_ASSIGN))) {
        insertToken(op(BITWISE_OR));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else if (token.eq(op(BITWISE_XOR_ASSIGN))) {
        insertToken(op(BITWISE_XOR));
        insertToken(left);
        insertToken(op(ASSIGN));
        nextToken();
      } else {
        previousToken();
        return null;
      }
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
      return Collections.emptyList();
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

  public Declaration parseDeclaration() {
    Type type;
    Identifier name;
    Expression value;
    if (token instanceof Type && !token.eq("VOID")) {
      type = (Type) token;
      nextToken();
      if (token instanceof Identifier) {
        name = (Identifier) token;
        nextToken();
        if (token.eq(op(ASSIGN))) {
          nextToken();
          if ((value = parseExpression()) != null) {
            return new Declaration(name, type.getName(), value, true);
          }
          previousToken();
        }
        return new Declaration(name, type.getName(), null, false);
      } else {
        previousToken();
      }
    }
    return null;
  }

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
        errors.addParserError("do{ must be followed by a valid stmt sequence");
      }
      if (!token.eq(sym(RBRACE))) {
        previousToken();
        errors.addParserError("missing } in do-clause");
      }
      nextToken();
      if (!token.eq(keyword(WHILE))) {
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
      if (!token.eq(sym(RPAREN))) {
        previousToken();
        errors.addParserError("missing } in do-clause");
      } else {
        insertToken(sym(SEMICOLON));
        nextToken();
      }
      return new DoStatement(condition, body);
    } else {
      return null;
    }
  }

  public Expression parseExpression() {
    Expression expression = new Expression();
    boolean negative = false;
    if (token.eq(op(MINUS))) {
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
    int parenthesis = 0;
    while (token instanceof Num
        || token instanceof Str
        || token instanceof Identifier
        || token instanceof Operator
        || token.eq(sym(LPAREN))
        || token.eq(sym(RPAREN))) {
      int idx = savePointer();
      MethodCall methodCall = parseMethodCall();
      if (methodCall != null) {
        expression.addToken(methodCall);
      } else {
        restorePointer(idx);
        if (token.eq(sym(LPAREN))) {
          parenthesis--;
        } else if (token.eq(sym(RPAREN))) {
          parenthesis++;
        }
        if (parenthesis > 0) {
          break;
        }
        expression.addToken(token);
        nextToken();
      }
    }
    return (expression.size() > 0) ? expression : null;
  }

  public IfStatement parseIfStatement() {
    IfStatement ifStatement;
    List<IfBlock> ifBlocks = new ArrayList<>();
    IfBlock ifBlock = parseIfBlock(keyword(IF));
    if (ifBlock != null) {
      ifBlocks.add(ifBlock);
      IfBlock elseIfBlock;
      int idx1 = savePointer();
      int idx2 = idx1;
      while ((elseIfBlock = parseIfBlock(keyword(ELSE), keyword(IF))) != null) {
        ifBlocks.add(elseIfBlock);
        idx2 = savePointer();
      }
      restorePointer(idx2);
      IfBlock elseBlock = parseElseBlock();
      if (elseBlock != null) {
        ifBlocks.add(elseBlock);
        ifStatement = new IfStatement(ifBlocks, true);
      } else {
        ifStatement = new IfStatement(ifBlocks, false);
      }
      previousToken();
      insertToken(sym(SEMICOLON));
      nextToken();
      return ifStatement;
    }
    return null;
  }

  public IfBlock parseIfBlock(Token... expectedTokens) {
    Condition condition;
    List<Statement> body;
    if (token == null) {
      return null;
    }
    for (Token expectedToken : expectedTokens) {
      if (!token.eq(expectedToken)) {
        return null;
      }
      nextToken();
    }
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
      nextToken();
    }
    if (condition != null & body != null) {
      return new IfBlock(condition, body);
    }
    return null;
  }

  public IfBlock parseElseBlock() {
    List<Statement> body;
    if (!token.eq(keyword(ELSE))) {
      return null;
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
      nextToken();
    }
    if (body != null) {
      return new IfBlock(null, body);
    }
    return null;
  }

  public Method parseMethod() {
    Scope scope = scope(DEFAULT);
    String type = null;
    Identifier name;
    List<Argument> arguments = new ArrayList<>();
    List<Statement> body;
    if (token instanceof Scope) {
      scope = (Scope) token;
      nextToken();
    }
    if (token.eq(keyword(STATIC))) {
      // TODO: handle static (for now we just ignore it)
      nextToken();
    }
    if (token instanceof Type) {
      type = ((Type) token).getName();
      nextToken();
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
    if (type != null && name != null && arguments != null & body != null) {
      return new Method(scope, type, name, arguments, body);
    }
    return null;
  }

  public MethodCall parseMethodCall() {
    List<Expression> parameters;
    List<Identifier> names = new ArrayList<>();
    if (token instanceof Identifier) {
      do {
        names.add((Identifier) token);
        nextToken();
      } while ((token.eq(sym(DOT)) && nextToken()));
      parameters = parseParameters();
      return (parameters == null) ? null : new MethodCall(names, parameters);
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

  public List<Expression> parseParameters() {
    List<Expression> parameters = new ArrayList<>();
    if (token.eq(sym(LPAREN))) {
      nextToken();
      Expression factor;
      if ((factor = parseExpression()) != null) {
        parameters.add(factor);
      }
      while (token.eq(sym(COMMA))) {
        nextToken();
        if ((factor = parseExpression()) != null) {
          parameters.add(factor);
        }
        // TODO: else
      }
      if (!token.eq(sym(RPAREN))) {
        errors.addError(") expected", ErrorType.PARSER);
      }
      nextToken();
      return parameters;
    }
    // else
    return null;
  }

  public Selector parseSelector() {
    if (token.eq(sym(DOT))) {
      nextToken();
      if (token instanceof Identifier) {
        return new FieldSelector((Identifier) token);
      } else {
        errors.addError("identifier expected", ErrorType.PARSER);
        previousToken();
      }
    } else if (token.eq(sym(LBRAK))) {
      nextToken();
      Expression expression = parseExpression();
      if (expression != null) {
        if (token.eq(sym(RBRAK))) {
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

  public Statement parseStatement() {
    Statement stmt;
    int idx = savePointer();
    if ((stmt = parseAssignment()) != null) {
      return stmt;
    } else if (restorePointer(idx) && (stmt = parseIfStatement()) != null) {
      return stmt;
    } else if (restorePointer(idx) && (stmt = parseDoStatement()) != null) {
      return stmt;
    } else if (restorePointer(idx) && (stmt = parseWhileStatement()) != null) {
      return stmt;
    } else if (restorePointer(idx) && (stmt = parseForStatement()) != null) {
      return stmt;
    } else if (restorePointer(idx) && (stmt = parseDeclaration()) != null) {
      return stmt;
    } else if (restorePointer(idx) && (stmt = parseMethodCall()) != null) {
      return stmt;
    } else if (restorePointer(idx) && (stmt = parseReturn()) != null) {
      return stmt;
    } else if (restorePointer(idx) && (stmt = parseBreak()) != null) {
      return stmt;
    } else if (restorePointer(idx) && (stmt = parseParallelAssignment()) != null) {
      return stmt;
    } else {
      return null;
    }
  }

  public ReturnStatement parseReturn() {
    if (token.eq(keyword(RETURN))) {
      nextToken();
      Expression expression = parseExpression();
      nextToken();
      return new ReturnStatement(expression);
    }
    return null;
  }

  public Break parseBreak() {
    if (token.eq(keyword(BREAK))) {
      nextToken();
      return new Break();
    }
    return null;
  }

  private LoopStatement parseForStatement() {
    Statement initialization;
    Condition condition;
    Statement loopStatement;
    List<Statement> body;
    if (token == null) {
      return null;
    }
    if (token.eq(keyword(FOR))) {
      nextToken();
      if (!token.eq(sym(LPAREN))) {
        previousToken();
        errors.addParserError("for must be followed by (");
      }
      nextToken();
      initialization = parseStatement();
      if (initialization == null) {
        previousToken();
        errors.addParserError("for stmt must contain an initialization stmt");
      }
      nextToken();
      condition = parseCondition();
      if (condition == null) {
        previousToken();
        errors.addParserError("for stmt must contain a condition");
      }
      nextToken();
      loopStatement = parseStatement();
      if (loopStatement == null) {
        previousToken();
        errors.addParserError("for stmt must contain a loop stmt");
      }
      nextToken();
      if (!token.eq(sym(RPAREN))) {
        previousToken();
        errors.addParserError("missing ) in for stmt");
      }
      nextToken();
      if (!token.eq(sym(LBRACE))) {
        previousToken();
        errors.addParserError("missing { in for stmt");
      }
      nextToken();
      body = parseStatementSequence();
      if (body == null) {
        errors.addParserError("invalid body of for stmt");
      }
      if (!token.eq(sym(RBRACE))) {
        previousToken();
        errors.addParserError("missing } in for-clause");
      } else {
        insertToken(sym(SEMICOLON));
        nextToken();
      }
      return new ForStatement(initialization, condition, loopStatement, body);
    } else {
      return null;
    }
  }

  public List<Statement> parseStatementSequence() {
    List<Statement> stmtSequence = new ArrayList<>();
    Statement stmt;
    if ((stmt = parseStatement()) != null) {
      stmtSequence.add(stmt);
    }
    while (token.eq(sym(SEMICOLON))) {
      nextToken();
      if ((stmt = parseStatement()) != null) {
        stmtSequence.add(stmt);
      }
    }
    return stmtSequence;
  }

  public WhileStatement parseWhileStatement() {
    Condition condition;
    List<Statement> body;
    if (token == null) {
      return null;
    }
    if (token.eq(keyword(WHILE))) {
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
        previousToken();
        errors.addParserError("missing } in while-clause");
      } else {
        insertToken(sym(SEMICOLON));
        nextToken();
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

  public int savePointer() {
    return index;
  }
}
