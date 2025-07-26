package io.github.martinschneider.orzo.lexer;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.*;
import static io.github.martinschneider.orzo.lexer.tokens.Symbols.*;
import static io.github.martinschneider.orzo.lexer.tokens.Token.*;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BASIC_TYPES;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Keywords;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    tokenList.add(fp(buffer.toString(), isFloat).wLoc(inputReader.getLoc()));
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
      for (Keywords keyword : Keywords.values()) {
        // TODO: specify case-sensitive keywords (instead of assuming they are all lowercase)
        if (str.equals(keyword.name().toLowerCase())) {
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
      // bool literal
      if (str.equals("true") || str.equals("false")) {
        tokenList.add(bool(str));
        buffer.setLength(0);
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

      // Check for hexadecimal literals (0x or 0X)
      if (character == '0') {
        char nextChar = (char) inputReader.read();
        if (nextChar == 'x' || nextChar == 'X') {
          buffer.append(nextChar);
          // Scan hexadecimal digits
          while (isHexDigit(character = (char) inputReader.read())) {
            buffer.append(character);
          }
          boolean isLong = false;
          if (character == 'l' || character == 'L') {
            isLong = true;
          } else {
            inputReader.unread(character);
          }
          tokenList.add(integer(buffer.toString(), isLong).wLoc(inputReader.getLoc()));
          buffer.setLength(0);
          return;
        } else {
          inputReader.unread(nextChar);
        }
      }

      // Regular decimal number scanning
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

  private boolean isHexDigit(char c) {
    return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  private void scanOps() throws IOException {
    if (character == '-') {
      if ((character = (char) inputReader.read()) == '-') {
        if (tokenList.size() > 0
            && (tokenList.get(tokenList.size() - 1) instanceof Identifier
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
            && (tokenList.get(tokenList.size() - 1) instanceof Identifier
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
        tokenList.add(chr((char) c).wLoc(inputReader.getLoc()));
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
