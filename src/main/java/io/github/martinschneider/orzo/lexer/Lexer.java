package io.github.martinschneider.orzo.lexer;

import static io.github.martinschneider.orzo.lexer.tokens.Operator.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_AND_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_OR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.BITWISE_XOR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.DIV_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.EQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.GREATEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.LESS;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.LESSEQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.LOGICAL_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.LOGICAL_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.LSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MINUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.MOD_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.NEGATE;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.NOTEQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.PLUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.POW;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.PRE_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.PRE_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.RSHIFTU_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.RSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Operator.TIMES_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LFLOOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RFLOOR;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Symbol.SQRT;
import static io.github.martinschneider.orzo.lexer.tokens.Token.boolLit;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.lexer.tokens.Token.type;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.CHAR_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.DOUBLE_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.FLOAT_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.ID;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.INT_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.LONG_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.STRING_LITERAL;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.tokens.BasicType;
import io.github.martinschneider.orzo.lexer.tokens.Keyword;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.TokenType;

public class Lexer {
  private StringBuffer buffer;
  private char character;
  private final CompilerErrors errors = new CompilerErrors();
  // a PushbackReader is used to be able to jump forward and backward in the input stream
  private LineAwareReader inputReader;
  private List<Token> tokenList;

  public CompilerErrors getErrors() {
    return errors;
  }

  public TokenList getTokens(File file) throws IOException {
    inputReader = new LineAwareReader(new FileReader(file));
    return getTokens(inputReader);
  }

  public TokenList getTokens(String string) throws IOException {
    inputReader = new LineAwareReader(new StringReader(string));
    return getTokens(inputReader);
  }

  public TokenList getTokens(PushbackReader fileReader) throws IOException {
    tokenList = new ArrayList<>();
    buffer = new StringBuffer();
    int tokenCount;
    int c;
    // scan each char of the input
    while ((c = fileReader.read()) != -1) {
      character = (char) c;
      tokenCount = tokenList.size();
      // parenthesis
      scanParen();
      // identifier/keyword
      if (tokenList.size() == tokenCount) {
        scanId();
      }
      // symbol
      if (tokenList.size() == tokenCount) {
        scanSym();
      }
      // operator
      if (tokenList.size() == tokenCount) {
        scanOps();
      }
      // character
      if (tokenList.size() == tokenCount) {
        scanChr();
      }
      // string
      if (tokenList.size() == tokenCount) {
        scanStr();
      }
      // number
      if (tokenList.size() == tokenCount) {
        scanNum();
      }
      // comment
      if (tokenList.size() == tokenCount) {
        scanComment();
      }
      // annotation
      if (tokenList.size() == tokenCount) {
        scanAnnotation();
      }
    }
    return new TokenList(tokenList);
  }

  private void scanComment() throws IOException {
    if ((character == '/')) {
      if ((character = (char) inputReader.read()) == '*') {
        scanComment1();
      } else if (character == '/') {
        scanComment2();
      }
    }
  }

  private void scanAnnotation() throws IOException {
    if ((character == '@')) {
      // we do not support annotation -> ignore
      int c;
      do {
        c = inputReader.read();
      } while (c != 10 && c != 13 && c != -1);
      inputReader.unread(c);
    }
  }

  private void scanComment1() throws IOException {
    int c;
    int nested = 1;
    while ((c = inputReader.read()) != -1) {
      if ((character = (char) c) == '*') {
        if (((character = (char) inputReader.read()) == '/')) {
          nested--;
          if (nested == 0) {
            break;
          }
        }
      } else if (character == '/') {
        if (((character = (char) inputReader.read()) == '*')) {
          nested++;
        }
      }
    }
    if (c == -1) {
      errors.addError("scan comment", "missing */", new RuntimeException().getStackTrace());
    }
  }

  private void scanComment2() throws IOException {
    int c;
    do {
      c = inputReader.read();
    } while (c != 10 && c != 13 && c != -1);
    inputReader.unread(c);
  }

  /** Scans for double after '.' was found. */
  private void scanDouble() throws IOException {
    while (Character.isDigit(character = (char) inputReader.read())) {
      buffer.append(character);
    }
    boolean isFloat = false;
    if (character == 'd' || character == 'D') {
      // do nothing
    } else if (character == 'f' || character == 'F') {
      isFloat = true;
    } else {
      inputReader.unread(character);
    }
    TokenType literalType = isFloat ? FLOAT_LITERAL : DOUBLE_LITERAL;
    tokenList.add(Token.of(literalType, buffer.toString()).wLoc(inputReader.getLoc()));
    buffer.setLength(0);
  }

  /** Scans for identifiers, primitive types and keywords */
  private void scanId() throws IOException {
    if (Character.isLetter(character) || character == '_') {
      buffer.append(character);
      while (isAlphanumeric(character = (char) inputReader.read())) {
        buffer.append(character);
      }
      inputReader.unread(character);
      String str = buffer.toString();
      // keywords
      for (Keyword keyword : Keyword.values()) {
        // TODO: specify case-sensitive keywords (instead of assuming they are all lowercase)
        if (str.equals(keyword.name().toLowerCase())) {
          tokenList.add(keyword(keyword).wLoc(inputReader.getLoc()));
          buffer.setLength(0);
        }
      }
      // scopes
      for (Scope scope : Scope.values()) {
        if (str.equals(scope.name().toLowerCase())) {
          tokenList.add(scope(scope).wLoc(inputReader.getLoc()));
          buffer.setLength(0);
        }
      }
      // bool literal
      if (str.equals("true") || str.equals("false")) {
        tokenList.add(boolLit(str));
        buffer.setLength(0);
      }
      // basic types
      if (BasicType.isBasicType(str)) {
        tokenList.add(type(str));
        buffer.setLength(0);
      }
      if (buffer.length() > 0) {
        tokenList.add(id(buffer.toString()).wLoc(inputReader.getLoc()));
      }
      buffer.setLength(0);
    }
  }

  private boolean isAlphanumeric(char c) {
    return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_';
  }

  private void scanNum() throws IOException {
    if (Character.isDigit(character)) {
      buffer.append(character);
      while (Character.isDigit(character = (char) inputReader.read())) {
        buffer.append(character);
      }
      boolean isLong = false;
      if (character == '.') {
        buffer.append(character);
        scanDouble();
        return;
      } else if (character == 'l' || character == 'L') {
        isLong = true;
      } else {
        inputReader.unread(character);
      }
      TokenType type = isLong ? LONG_LITERAL : INT_LITERAL;
      tokenList.add(Token.of(type, buffer.toString()).wLoc(inputReader.getLoc()));
      buffer.setLength(0);
    }
  }

  private void scanOps() throws IOException {
    if (character == '-') {
      if ((character = (char) inputReader.read()) == '-') {
        if (tokenList.size() > 0
            && (tokenList.get(tokenList.size() - 1).type.equals(ID)
                || tokenList.get(tokenList.size() - 1).equals(sym(RBRAK)))) {
          tokenList.add(op(POST_DECREMENT).wLoc(inputReader.getLoc()));
        } else {
          tokenList.add(op(PRE_DECREMENT).wLoc(inputReader.getLoc()));
        }
      } else if (character == '=') {
        tokenList.add(op(MINUS_ASSIGN).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(MINUS).wLoc(inputReader.getLoc()));
      }
    } else if (character == '/') {
      // look-ahead to check for comment
      char character;
      if ((character = (char) inputReader.read()) == '*') {
        scanComment1();
      } else if (character == '/') {
        scanComment2();
      } else if (character == '=') {
        tokenList.add(op(DIV_ASSIGN).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(DIV).wLoc(inputReader.getLoc()));
      }
    } else if (character == '%') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(MOD_ASSIGN).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(MOD).wLoc(inputReader.getLoc()));
      }
    } else if (character == '*') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(TIMES_ASSIGN).wLoc(inputReader.getLoc()));
      } else if (character == '*') {
        tokenList.add(op(POW).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(TIMES).wLoc(inputReader.getLoc()));
      }
    } else if (character == '+') {
      char character;
      if ((character = (char) inputReader.read()) == '+') {
        if ((tokenList.size() > 0)
            && (tokenList.get(tokenList.size() - 1).type.equals(ID)
                || tokenList.get(tokenList.size() - 1).equals(sym(RBRAK)))) {
          tokenList.add(op(POST_INCREMENT).wLoc(inputReader.getLoc()));
        } else {
          tokenList.add(op(PRE_INCREMENT).wLoc(inputReader.getLoc()));
        }
      } else if (character == '=') {
        tokenList.add(op(PLUS_ASSIGN).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(PLUS).wLoc(inputReader.getLoc()));
      }
    } else if (character == '>') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(GREATEREQ).wLoc(inputReader.getLoc()));
      } else if (character == '>') {
        if ((character = (char) inputReader.read()) == '>') {
          if ((character = (char) inputReader.read()) == '=') {
            tokenList.add(op(RSHIFTU_ASSIGN).wLoc(inputReader.getLoc()));
          } else {
            inputReader.unread(character);
            tokenList.add(op(RSHIFTU).wLoc(inputReader.getLoc()));
          }
        } else if (character == '=') {
          tokenList.add(op(RSHIFT_ASSIGN).wLoc(inputReader.getLoc()));
        } else {
          inputReader.unread(character);
          tokenList.add(op(RSHIFT).wLoc(inputReader.getLoc()));
        }
      } else {
        tokenList.add(op(GREATER).wLoc(inputReader.getLoc()));
        inputReader.unread(character);
      }
    } else if (character == '<') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(LESSEQ).wLoc(inputReader.getLoc()));
      } else if (character == '<') {
        if ((character = (char) inputReader.read()) == '=') {
          tokenList.add(op(LSHIFT_ASSIGN).wLoc(inputReader.getLoc()));
        } else {
          inputReader.unread(character);
          tokenList.add(op(LSHIFT).wLoc(inputReader.getLoc()));
        }
      } else {
        tokenList.add(op(LESS).wLoc(inputReader.getLoc()));
        inputReader.unread(character);
      }
    } else if (character == '=') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(EQUAL).wLoc(inputReader.getLoc()));
      } else {
        tokenList.add(op(ASSIGN).wLoc(inputReader.getLoc()));
        inputReader.unread(character);
      }
    } else if (character == '!') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(NOTEQUAL).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(NEGATE));
      }
    } else if (character == '&') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(BITWISE_AND_ASSIGN).wLoc(inputReader.getLoc()));
      } else if (character == '&') {
        tokenList.add(op(LOGICAL_AND).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(BITWISE_AND).wLoc(inputReader.getLoc()));
      }
    } else if (character == '|') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(BITWISE_OR_ASSIGN).wLoc(inputReader.getLoc()));
      } else if (character == '|') {
        tokenList.add(op(LOGICAL_OR).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(BITWISE_OR).wLoc(inputReader.getLoc()));
      }
    } else if (character == '^') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(BITWISE_XOR_ASSIGN).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(BITWISE_XOR).wLoc(inputReader.getLoc()));
      }
    } else if (character == '√') {
      tokenList.add(sym(SQRT));
    } else if (character == '⌊') {
      tokenList.add(sym(LFLOOR));
    } else if (character == '⌋') {
      tokenList.add(sym(RFLOOR));
    }
  }

  private void scanParen() {
    if (character == '(') {
      tokenList.add(sym(LPAREN).wLoc(inputReader.getLoc()));
    } else if (character == ')') {
      tokenList.add(sym(RPAREN).wLoc(inputReader.getLoc()));
    } else if (character == '{') {
      tokenList.add(sym(LBRACE).wLoc(inputReader.getLoc()));
    } else if (character == '}') {
      tokenList.add(sym(RBRACE).wLoc(inputReader.getLoc()));
    } else if (character == '[') {
      tokenList.add(sym(LBRAK).wLoc(inputReader.getLoc()));
    } else if (character == ']') {
      tokenList.add(sym(RBRAK).wLoc(inputReader.getLoc()));
    }
  }

  private void scanChr() throws IOException {
    if ((character == '\'')) {
      int c;
      if ((c = inputReader.read()) != -1) {
        tokenList.add(Token.of(CHAR_LITERAL, Character.toString(c)).wLoc(inputReader.getLoc()));
      }
      if ((c = (char) inputReader.read()) != '\'') {
        errors.addError("scan character", "missing '", new RuntimeException().getStackTrace());
        inputReader.unread(c);
      }
    }
    buffer.setLength(0);
  }

  private void scanStr() throws IOException {
    if ((character == '"')) {
      int c;
      while ((c = inputReader.read()) != -1) {
        if ((character = (char) c) == '"') {
          break;
        }
        buffer.append(character);
      }
      if (c == -1) {
        errors.addError(
            "scan string", "missing \" in string", new RuntimeException().getStackTrace());
      } else {
        tokenList.add(Token.of(STRING_LITERAL,buffer.toString()).wLoc(inputReader.getLoc()));
      }
    }
    buffer.setLength(0);
  }

  private void scanSym() throws IOException {
    if (character == ',') {
      tokenList.add(sym(COMMA).wLoc(inputReader.getLoc()));
    } else if (character == ';') {
      tokenList.add(sym(SEMICOLON).wLoc(inputReader.getLoc()));
    } else if (character == '.') {
      if (Character.isDigit(character = (char) inputReader.read())) {
        buffer.append('.');
        buffer.append(character);
        scanDouble();
      } else {
        inputReader.unread(character);
        tokenList.add(sym(DOT).wLoc(inputReader.getLoc()));
      }
    }
  }
}
