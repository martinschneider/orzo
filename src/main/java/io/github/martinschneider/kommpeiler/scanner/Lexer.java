package io.github.martinschneider.kommpeiler.scanner;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.EQUAL;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.GREATER;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.GREATEREQ;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.NOTEQUAL;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.SMALLER;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Comparators.SMALLEREQ;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.ASSIGN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.DIV;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.LSHIFT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MINUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.MOD;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.PLUS;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.RSHIFT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.TIMES;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.COMMA;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.DOT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.LPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRACE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RBRAK;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.RPAREN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Symbols.SEMICOLON;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.cmp;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.fp;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.integer;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.keyword;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.scope;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.str;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.sym;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BASIC_TYPES;

import io.github.martinschneider.kommpeiler.error.CompilerErrors;
import io.github.martinschneider.kommpeiler.error.ErrorType;
import io.github.martinschneider.kommpeiler.scanner.tokens.Keywords;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scopes;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
  private static final List<Character> NUMERIC_LITERALS =
      List.of('i', 'I', 'l', 'L', 'd', 'D', 'F', 'f');
  private StringBuffer buffer;
  private char character;
  private CompilerErrors errors = new CompilerErrors();
  // a PushbackReader is used to be able to jump forward and backward in the input stream
  private PushbackReader inputReader;
  private List<Token> tokenList;

  public CompilerErrors getErrors() {
    return errors;
  }

  public List<Token> getTokens(final File file) throws IOException {
    errors.clear();
    Reader reader = new FileReader(file);
    inputReader = new PushbackReader(reader);
    return getTokens(inputReader);
  }

  public List<Token> getTokens(final PushbackReader fileReader) throws IOException {
    errors.clear();
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
    return tokenList;
  }

  public List<Token> getTokens(final String string) throws IOException {
    errors.clear();
    Reader reader = new StringReader(string);
    inputReader = new PushbackReader(reader);
    return getTokens(inputReader);
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
      errors.addError("No closing */ for comment found.", ErrorType.SCANNER);
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
    if (NUMERIC_LITERALS.contains(character)) {
      // ignore
    } else {
      inputReader.unread(character);
    }
    tokenList.add(fp(buffer.toString()));
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
          tokenList.add(keyword(str));
          buffer.setLength(0);
        }
      }
      // scopes
      for (Scopes scope : Scopes.values()) {
        if (str.equalsIgnoreCase(scope.name())) {
          tokenList.add(scope(Scopes.valueOf(str.toUpperCase())));
          buffer.setLength(0);
        }
      }
      // basic types
      if (BASIC_TYPES.contains(str)) {
        tokenList.add(type(str));
        buffer.setLength(0);
      }
      if (buffer.length() > 0) {
        tokenList.add(id(buffer.toString()));
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
      if (character == '.') {
        buffer.append(character);
        scanDouble();
        return;
      } else if (NUMERIC_LITERALS.contains(character)) {
        // ignore
      } else {
        inputReader.unread(character);
      }
      tokenList.add(integer(buffer.toString()));
      buffer.setLength(0);
    }
  }

  private void scanOps() throws IOException {
    if (character == '-') {
      if ((character = (char) inputReader.read()) == '-') {
        // TODO: distinction between pre and post increment operators
        tokenList.add(op(POST_DECREMENT));
      } else {
        inputReader.unread(character);
        tokenList.add(op(MINUS));
      }
    } else if (character == '/') {
      // look-ahead to check for comment
      char character;
      if ((character = (char) inputReader.read()) == '*') {
        scanComment1();
      } else if (character == '/') {
        scanComment2();
      } else {
        inputReader.unread(character);
        tokenList.add(op(DIV));
      }
    } else if (character == '%') {
      tokenList.add(op(MOD));
    } else if (character == '*') {
      tokenList.add(op(TIMES));
    } else if (character == '+') {
      char character;
      if ((character = (char) inputReader.read()) == '+') {
        // TODO: distinction between pre and post increment operators
        tokenList.add(op(POST_INCREMENT));
      } else {
        inputReader.unread(character);
        tokenList.add(op(PLUS));
      }
    } else if (character == '>') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(cmp(GREATEREQ));
      } else if (character == '>') {
        if ((character = (char) inputReader.read()) == '>') {
          tokenList.add(op(RSHIFTU));
        } else {
          inputReader.unread(character);
          tokenList.add(op(RSHIFT));
        }
      } else {
        tokenList.add(cmp(GREATER));
        inputReader.unread(character);
      }
    } else if (character == '<') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(cmp(SMALLEREQ));
      } else if (character == '<') {
        tokenList.add(op(LSHIFT));
      } else {
        tokenList.add(cmp(SMALLER));
        inputReader.unread(character);
      }
    } else if (character == '=') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(cmp(EQUAL));
      } else {
        tokenList.add(op(ASSIGN));
        inputReader.unread(character);
      }
    } else if (character == '!') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(cmp(NOTEQUAL));
      } else {
        inputReader.unread(character);
        errors.addError("! must be followed by =", ErrorType.SCANNER);
      }
    }
  }

  private void scanParen() {
    if (character == '(') {
      tokenList.add(sym(LPAREN));
    } else if (character == ')') {
      tokenList.add(sym(RPAREN));
    } else if (character == '{') {
      tokenList.add(sym(LBRACE));
    } else if (character == '}') {
      tokenList.add(sym(RBRACE));
    } else if (character == '[') {
      tokenList.add(sym(LBRAK));
    } else if (character == ']') {
      tokenList.add(sym(RBRAK));
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
        errors.addError("No closing \" for input string found.", ErrorType.SCANNER);
      } else {
        tokenList.add(str(buffer.toString()));
      }
    }
    buffer.setLength(0);
  }

  private void scanSym() throws IOException {
    if (character == ',') {
      tokenList.add(sym(COMMA));
    } else if (character == ';') {
      tokenList.add(sym(SEMICOLON));
    } else if (character == '.') {
      if (Character.isDigit(character = (char) inputReader.read())) {
        buffer.append('.');
        buffer.append(character);
        scanDouble();
      } else {
        inputReader.unread(character);
        tokenList.add(sym(DOT));
      }
    }
  }
}
