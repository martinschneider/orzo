package io.github.martinschneider.orzo.lexer.tokens;

public enum Keywords {
  IF,
  ELSE,
  DO,
  WHILE,
  FOR,
  RETURN,
  STATIC,
  CLASS,
  INTERFACE,
  ENUM,
  PACKAGE,
  IMPORT,
  BREAK,
  CONTINUE,
  NEW,
  FINAL,
  IMPLEMENTS,
  EXTENDS,
  REPEAT,
  UNLESS,
  TRY,
  CATCH,
  FINALLY,
  INSTANCEOF,
  SWITCH,
  CASE,
  THROW;
  // THIS
  // SUPER
  // DEFAULT is intentionally not here — it's already in Scopes as a method access modifier
}
