package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scope;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Method
 *
 * @author Martin Schneider
 */
public class Method {
  private List<Argument> arguments;
  private List<Statement> body;
  private Identifier name;
  private Scope scope;
  private Type type;

  /**
   * @param scope scope
   * @param type return-type
   * @param name name
   * @param body method-body
   */
  public Method(final Scope scope, final Type type, final Identifier name,
      final List<Argument> arguments, final List<Statement> body) {
    this.scope = scope;
    this.type = type;
    this.name = name;
    this.setArguments(arguments);
    this.body = body;
  }

  public List<Argument> getArguments() {
    return arguments;
  }

  public List<Statement> getBody() {
    return body;
  }

  public Identifier getName() {
    return name;
  }

  public Scope getScope() {
    return scope;
  }

  public Type getType() {
    return type;
  }

  public String getTypeDescr() {
    StringBuilder strBuilder = new StringBuilder("(");
    strBuilder.append(arguments.stream().map(x -> x.getType()).collect(Collectors.joining(", ")));
    strBuilder.append(')');
    strBuilder.append(type.getLabel());
    return strBuilder.toString();
  }

  public void setArguments(List<Argument> arguments) {
    this.arguments = arguments;
  }

  public void setBody(final List<Statement> body) {
    this.body = body;
  }

  public void setName(final Identifier name) {
    this.name = name;
  }

  public void setScope(final Scope scope) {
    this.scope = scope;
  }

  public void setType(final Type type) {
    this.type = type;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(scope);
    strBuilder.append(' ');
    strBuilder.append(type);
    strBuilder.append(' ');
    strBuilder.append(name);
    strBuilder.append(", args{");
    strBuilder.append(arguments.stream().map(x -> x.getName().getValue().toString())
        .collect(Collectors.joining(", ")));
    strBuilder.append("}");
    strBuilder.append(", code{");
    strBuilder.append(body.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("}");
    return strBuilder.toString();
  }
}
