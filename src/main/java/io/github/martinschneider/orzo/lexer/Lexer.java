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
  private final CompilerErrors errors;
  // a PushbackReader is used to be able to jump forward and backward in the input stream
  private PositionReader inputReader;
  private List<Token> tokenList;
  // the input, and the offsets at which its lines start (for the positions of tokens)
  private String source;
  private List<Integer> lineStarts;

  public Lexer() {
    this(new CompilerErrors());
  }

  public Lexer(CompilerErrors errors) {
    this.errors = errors;
  }

  public CompilerErrors getErrors() {
    return errors;
  }

  public TokenList getTokens(File file) throws IOException {
    StringBuilder content = new StringBuilder();
    try (Reader reader = new BufferedReader(new FileReader(file))) {
      int c;
      while ((c = reader.read()) != -1) {
        content.append((char) c);
      }
    }
    return getTokens(content.toString());
  }

  public TokenList getTokens(String string) {
    source = string;
    if (errors.file != null) {
      errors.sources.put(errors.file, string);
    }
    lineStarts = new ArrayList<>();
    lineStarts.add(0);
    for (int i = 0; i < string.length(); i++) {
      if (string.charAt(i) == '\n') {
        lineStarts.add(i + 1);
      }
    }
    inputReader = new PositionReader(new StringReader(string));
    try {
      return getTokens(inputReader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      errors.position = null;
    }
  }

  // A PushbackReader that knows its offset in the input
  private static class PositionReader extends PushbackReader {
    int offset;

    PositionReader(Reader reader) {
      super(reader);
    }

    @Override
    public int read() throws IOException {
      int c = super.read();
      if (c != -1) {
        offset++;
      }
      return c;
    }

    @Override
    public void unread(int c) throws IOException {
      super.unread(c);
      offset--;
    }
  }

  // Sets the position (and source text) of a token starting at the given offset of the input
  private void setPosition(Token token, int start, int end) {
    int line = lineStarts.size() - 1;
    while (lineStarts.get(line) > start) {
      line--;
    }
    token.line = line + 1;
    token.col = start - lineStarts.get(line) + 1;
    token.text = source.substring(start, Math.max(start, Math.min(end, source.length())));
  }

  private TokenList getTokens(PushbackReader fileReader) throws IOException {
    tokenList = new ArrayList<>();
    buffer = new StringBuffer();
    int tokenCount;
    int c;
    // scan each char of the input
    while ((c = fileReader.read()) != -1) {
      character = (char) c;
      tokenCount = tokenList.size();
      int start = inputReader.offset - 1;
      // the position of errors reported while scanning this token
      Token here = new Token(null);
      setPosition(here, start, start);
      errors.position = here;
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
      if (tokenList.size() == tokenCount
          && !Character.isWhitespace(character)
          && character != (char) -1 // the end of the input (read ahead by a scanner)
          && character != '/'
          && character != '@') {
        errors.addError(
            "scan",
            "unexpected character '" + character + "'",
            new RuntimeException().getStackTrace());
      }
      for (int i = tokenCount; i < tokenList.size(); i++) {
        setPosition(tokenList.get(i), start, inputReader.offset);
      }
    }
    TokenList result = new TokenList(tokenList);
    result.end = eof();
    setPosition(result.end, source.length(), source.length());
    return result;
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
    tokenList.add(fp(buffer.toString(), isFloat));
    buffer.setLength(0);
  }

  /** Scans for identifiers, primitive types and keywords */
  private void scanId() throws IOException {
    if (Character.isJavaIdentifierStart(character)) { // (letters, '_' and '$')
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
          tokenList.add(keyword(str));
          buffer.setLength(0);
        }
      }
      // scopes
      for (Scopes scope : Scopes.values()) {
        if (str.equals(scope.name().toLowerCase())) {
          tokenList.add(scope(Scopes.valueOf(str.toUpperCase())));
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
        tokenList.add(id(buffer.toString()));
      }
      buffer.setLength(0);
    }
  }

  private boolean isAlphanumeric(char c) {
    return Character.isJavaIdentifierPart(c) && !Character.isIdentifierIgnorable(c);
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
          tokenList.add(integer(buffer.toString(), isLong));
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
      tokenList.add(integer(buffer.toString(), isLong));
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
          tokenList.add(op(POST_DECREMENT));
        } else {
          tokenList.add(op(PRE_DECREMENT));
        }
      } else if (character == '=') {
        tokenList.add(op(MINUS_ASSIGN));
      } else if (character == '>') {
        tokenList.add(sym(ARROW));
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
      } else if (character == '=') {
        tokenList.add(op(DIV_ASSIGN));
      } else {
        inputReader.unread(character);
        tokenList.add(op(DIV));
      }
    } else if (character == '%') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(MOD_ASSIGN));
      } else {
        inputReader.unread(character);
        tokenList.add(op(MOD));
      }
    } else if (character == '*') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(TIMES_ASSIGN));
      } else if (character == '*') {
        tokenList.add(op(POW));
      } else {
        inputReader.unread(character);
        tokenList.add(op(TIMES));
      }
    } else if (character == '+') {
      char character;
      if ((character = (char) inputReader.read()) == '+') {
        if ((tokenList.size() > 0)
            && (tokenList.get(tokenList.size() - 1) instanceof Identifier
                || tokenList.get(tokenList.size() - 1).equals(sym(RBRAK)))) {
          tokenList.add(op(POST_INCREMENT));
        } else {
          tokenList.add(op(PRE_INCREMENT));
        }
      } else if (character == '=') {
        tokenList.add(op(PLUS_ASSIGN));
      } else {
        inputReader.unread(character);
        tokenList.add(op(PLUS));
      }
    } else if (character == '>') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(GREATEREQ));
      } else if (character == '>') {
        if ((character = (char) inputReader.read()) == '>') {
          if ((character = (char) inputReader.read()) == '=') {
            tokenList.add(op(RSHIFTU_ASSIGN));
          } else {
            inputReader.unread(character);
            tokenList.add(op(RSHIFTU));
          }
        } else if (character == '=') {
          tokenList.add(op(RSHIFT_ASSIGN));
        } else {
          inputReader.unread(character);
          tokenList.add(op(RSHIFT));
        }
      } else {
        tokenList.add(op(GREATER));
        inputReader.unread(character);
      }
    } else if (character == '<') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(LESSEQ));
      } else if (character == '<') {
        if ((character = (char) inputReader.read()) == '=') {
          tokenList.add(op(LSHIFT_ASSIGN));
        } else {
          inputReader.unread(character);
          tokenList.add(op(LSHIFT));
        }
      } else {
        tokenList.add(op(LESS));
        inputReader.unread(character);
      }
    } else if (character == '=') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(EQUAL));
      } else {
        tokenList.add(op(ASSIGN));
        inputReader.unread(character);
      }
    } else if (character == '!') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(NOTEQUAL));
      } else {
        inputReader.unread(character);
        tokenList.add(op(NEGATE));
      }
    } else if (character == '&') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(BITWISE_AND_ASSIGN));
      } else if (character == '&') {
        tokenList.add(op(LOGICAL_AND));
      } else {
        inputReader.unread(character);
        tokenList.add(op(BITWISE_AND));
      }
    } else if (character == '|') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(BITWISE_OR_ASSIGN));
      } else if (character == '|') {
        tokenList.add(op(LOGICAL_OR));
      } else {
        inputReader.unread(character);
        tokenList.add(op(BITWISE_OR));
      }
    } else if (character == '^') {
      if ((character = (char) inputReader.read()) == '=') {
        tokenList.add(op(BITWISE_XOR_ASSIGN));
      } else {
        inputReader.unread(character);
        tokenList.add(op(BITWISE_XOR));
      }
    } else if (character == '~') {
      tokenList.add(op(BITWISE_NOT));
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

  private void scanChr() throws IOException {
    if ((character == '\'')) {
      int c;
      if ((c = inputReader.read()) != -1) {
        if (c == '\\') {
          c = unescape(inputReader.read());
        }
        tokenList.add(chr((char) c));
      }
      if ((c = (char) inputReader.read()) != '\'') {
        errors.addError("scan character", "missing '", new RuntimeException().getStackTrace());
        inputReader.unread(c);
      }
    }
    buffer.setLength(0);
  }

  // Resolves the character after a backslash in a char/string literal escape sequence.
  private char unescape(int c) {
    switch (c) {
      case 'n':
        return '\n';
      case 't':
        return '\t';
      case 'r':
        return '\r';
      case 'b':
        return '\b';
      case 'f':
        return '\f';
      case '0':
        return '\0';
      default:
        return (char) c;
    }
  }

  private void scanStr() throws IOException {
    if ((character == '"')) {
      int c;
      while ((c = inputReader.read()) != -1) {
        if (c == '\\') {
          character = unescape(inputReader.read());
          buffer.append(character);
          continue;
        }
        if ((character = (char) c) == '"' || c == '\n') {
          break;
        }
        buffer.append(character);
      }
      if (c == -1 || c == '\n') {
        errors.addError(
            "scan string", "missing \" in string", new RuntimeException().getStackTrace());
      } else if (buffer.length() == 0 && peek() == '"') {
        errors.addError(
            "scan string", "text blocks are not supported", new RuntimeException().getStackTrace());
        skipTextBlock();
        tokenList.add(str("")); // (so that parsing continues without follow-up errors)
      } else {
        tokenList.add(str(buffer.toString()));
      }
    }
    buffer.setLength(0);
  }

  // skips the rest of a text block (the opening """ has been read except for its last ")
  private void skipTextBlock() throws IOException {
    inputReader.read();
    int quotes = 0;
    int c;
    while (quotes < 3 && (c = inputReader.read()) != -1) {
      quotes = (c == '"') ? quotes + 1 : 0;
    }
  }

  private int peek() throws IOException {
    int c = inputReader.read();
    if (c != -1) {
      inputReader.unread(c);
    }
    return c;
  }

  private void scanSym() throws IOException {
    if (character == ',') {
      tokenList.add(sym(COMMA));
    } else if (character == ';') {
      tokenList.add(sym(SEMICOLON));
    } else if (character == '?') {
      tokenList.add(sym(QUESTION));
    } else if (character == ':') {
      tokenList.add(sym(COLON));
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
