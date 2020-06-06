package io.github.martinschneider.orzo.lexer;

import static io.github.martinschneider.orzo.lexer.tokens.Comparators.EQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.GREATER;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.GREATEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.NOTEQUAL;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.SMALLER;
import static io.github.martinschneider.orzo.lexer.tokens.Comparators.SMALLEREQ;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_AND_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_OR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.BITWISE_XOR_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.DIV_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MOD_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.TIMES_ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.DOT;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.LPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRACE;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RBRAK;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.RPAREN;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.orzo.lexer.tokens.Token.cmp;
import static io.github.martinschneider.orzo.lexer.tokens.Token.fp;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.integer;
import static io.github.martinschneider.orzo.lexer.tokens.Token.keyword;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Token.str;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.lexer.tokens.Token.type;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BASIC_TYPES;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.tokens.Keywords;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
  private StringBuffer buffer;
  private char character;
  private CompilerErrors errors = new CompilerErrors();
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
      // parentheses
      scanParen();
      // identifiers & keywords
      if (tokenList.size() == tokenCount) {
        scanId();
      }
      // symbols
      if (tokenList.size() == tokenCount) {
        scanSym();
      }
      // operators
      if (tokenList.size() == tokenCount) {
        scanOps();
      }
      // Strings
      if (tokenList.size() == tokenCount) {
        scanStr();
      }
      // numbers
      if (tokenList.size() == tokenCount) {
        scanNum();
      }
      // comments
      if (tokenList.size() == tokenCount) {
        scanComment();
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
      errors.addError("scan comment", "missing */");
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
    tokenList.add(fp(buffer.toString(), isFloat).wLoc(inputReader.getLoc()));
    buffer.setLength(0);
  }

  /** Scans for identifiers, primitive types and keywords */
  private void scanId() throws IOException {
    if (Character.isLetter(character)) {
      buffer.append(character);
      while (isAlphanumeric(character = (char) inputReader.read())) {
        buffer.append(character);
      }
      inputReader.unread(character);
      String str = buffer.toString();
      // keywords
      for (Keywords keyword : Keywords.values()) {
        if (str.equalsIgnoreCase(keyword.name())) {
          tokenList.add(keyword(str).wLoc(inputReader.getLoc()));
          buffer.setLength(0);
        }
      }
      // scopes
      for (Scopes scope : Scopes.values()) {
        if (str.equals(scope.name().toLowerCase())) {
          tokenList.add(scope(Scopes.valueOf(str.toUpperCase())).wLoc(inputReader.getLoc()));
          buffer.setLength(0);
        }
      }
      // basic types
      if (BASIC_TYPES.contains(str)) {
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
      tokenList.add(integer(buffer.toString(), isLong).wLoc(inputReader.getLoc()));
      buffer.setLength(0);
    }
  }

  private void scanOps() throws IOException {
    if (character == '-') {
      if ((character = (char) inputReader.read()) == '-') {
        // TODO: distinction between pre and post increment operators
        tokenList.add(op(POST_DECREMENT).wLoc(inputReader.getLoc()));
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
      } else {
        inputReader.unread(character);
        tokenList.add(op(TIMES).wLoc(inputReader.getLoc()));
      }
    } else if (character == '+') {
      char character;
      if ((character = (char) inputReader.read()) == '+') {
        // TODO: distinction between pre and post increment operators
        tokenList.add(op(POST_INCREMENT).wLoc(inputReader.getLoc()));
      } else if (character == '=') {
        tokenList.add(op(PLUS_ASSIGN).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(PLUS).wLoc(inputReader.getLoc()));
      }
    } else if (character == '>') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(cmp(GREATEREQ).wLoc(inputReader.getLoc()));
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
        tokenList.add(cmp(GREATER).wLoc(inputReader.getLoc()));
        inputReader.unread(character);
      }
    } else if (character == '<') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(cmp(SMALLEREQ).wLoc(inputReader.getLoc()));
      } else if (character == '<') {
        if ((character = (char) inputReader.read()) == '=') {
          tokenList.add(op(LSHIFT_ASSIGN).wLoc(inputReader.getLoc()));
        } else {
          inputReader.unread(character);
          tokenList.add(op(LSHIFT).wLoc(inputReader.getLoc()));
        }
      } else {
        tokenList.add(cmp(SMALLER).wLoc(inputReader.getLoc()));
        inputReader.unread(character);
      }
    } else if (character == '=') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(cmp(EQUAL).wLoc(inputReader.getLoc()));
      } else {
        tokenList.add(op(ASSIGN).wLoc(inputReader.getLoc()));
        inputReader.unread(character);
      }
    } else if (character == '!') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(cmp(NOTEQUAL).wLoc(inputReader.getLoc()));
      } else {
        errors.addError("scan operator", "missing !, found " + character);
        inputReader.unread(character);
      }
    } else if (character == '&') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(BITWISE_AND_ASSIGN).wLoc(inputReader.getLoc()));
      } else {
        inputReader.unread(character);
        tokenList.add(op(BITWISE_AND).wLoc(inputReader.getLoc()));
      }
    } else if (character == '|') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(BITWISE_OR_ASSIGN).wLoc(inputReader.getLoc()));
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
        errors.addError("scan string", "missing \" in string");
      } else {
        tokenList.add(str(buffer.toString()).wLoc(inputReader.getLoc()));
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
