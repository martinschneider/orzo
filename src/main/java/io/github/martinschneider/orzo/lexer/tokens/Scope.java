package io.github.martinschneider.orzo.lexer.tokens;

import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_DEFAULT;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_PRIVATE;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_PROTECTED;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_PUBLIC;

import io.github.martinschneider.orzo.parser.productions.AccessFlag;

public enum Scope {
  PUBLIC(ACC_PUBLIC),
  PRIVATE(ACC_PRIVATE),
  PROTECTED(ACC_PROTECTED),
  DEFAULT(ACC_DEFAULT);

  public AccessFlag accFlag;

  private Scope(AccessFlag accFlag) {
    this.accFlag = accFlag;
  }
}
