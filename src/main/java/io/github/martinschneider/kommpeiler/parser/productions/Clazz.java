package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class
 *
 * @author Martin Schneider
 */
public class Clazz {
  private List<Method> body;
  private Identifier name;
  private Scope scope;

  /**
   * @param scope scope
   * @param name name
   * @param body body
   */
  public Clazz(final Scope scope, final Identifier name, final List<Method> body) {
    this.scope = scope;
    this.name = name;
    this.body = body;
  }

  public List<Method> getBody() {
    return body;
  }

  public Identifier getName() {
    return name;
  }

  public Scope getScope() {
    return scope;
  }

  public void setBody(final List<Method> body) {
    this.body = body;
  }

  public void setName(final Identifier name) {
    this.name = name;
  }

  public void setScope(final Scope scope) {
    this.scope = scope;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder(name.toString());
    strBuilder.append(", methods[");
    strBuilder.append(body.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("]");
    return strBuilder.toString();
  }
}
