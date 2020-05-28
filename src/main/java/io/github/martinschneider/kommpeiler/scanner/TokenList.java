package io.github.martinschneider.kommpeiler.scanner;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.eof;

import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.List;

public class TokenList {
  private List<Token> tokens;
  private int idx;

  public TokenList(List<Token> tokens) {
    this(tokens, 0);
  }

  public TokenList(List<Token> tokens, int idx) {
    this.tokens = tokens;
    this.idx = idx;
  }

  public Token curr() {
    if (idx < 0 || idx >= tokens.size()) {
      return eof();
    }
    return tokens.get(idx);
  }

  public Token next() {
    idx++;
    return curr();
  }

  public Token prev() {
    idx--;
    return curr();
  }

  public Token fw(int steps) {
    idx += steps;
    return curr();
  }

  public Token bw(int steps) {
    idx -= steps;
    return curr();
  }

  public void insert(Token token) {
    tokens.add(idx + 1, token);
  }

  public int idx() {
    return idx;
  }

  public int size() {
    return tokens.size();
  }

  public List<Token> list() {
    return tokens;
  }

  // for testing
  // TODO: should be possible to remove from prod code
  public boolean setIdx(int idx) {
    this.idx = idx;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
    return result;
  }

  // doesn't factor in idx, only tokens!
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    TokenList other = (TokenList) obj;
    if (tokens == null) {
      if (other.tokens != null) {
        return false;
      }
    } else if (!tokens.equals(other.tokens)) {
      return false;
    }
    return true;
  }
}
