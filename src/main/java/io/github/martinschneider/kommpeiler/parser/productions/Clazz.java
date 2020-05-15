package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scope;
import java.util.List;
import java.util.stream.Collectors;

public class Clazz {
  private List<Method> body;
  private Identifier name;
  private String packageName;
  private Scope scope;

  public Clazz(
      final String packageName, final Scope scope, final Identifier name, final List<Method> body) {
    this.packageName = packageName;
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

  public String getPackageName() {
    return packageName;
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

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setScope(final Scope scope) {
    this.scope = scope;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    if (packageName != null && !packageName.isEmpty()) {
      strBuilder.append(packageName);
      strBuilder.append('.');
    }
    strBuilder.append(name.toString());
    strBuilder.append(", methods[");
    strBuilder.append(body.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("]");
    return strBuilder.toString();
  }
}
