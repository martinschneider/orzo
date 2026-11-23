package io.github.martinschneider.orzo.parser.productions;

/** A local class: compiled like a nested class, see ClassParser.parseLocalClass(). */
public class LocalClassDeclaration implements Statement {
  public Clazz clazz;

  public LocalClassDeclaration(Clazz clazz) {
    this.clazz = clazz;
  }
}
