package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import java.util.List;
import java.util.stream.Collectors;

public class Clazz {
  private List<Method> body;
  private List<Import> imports;
  private Identifier name;
  private String packageName;
  private Scope scope;

  public Clazz(
      final String packageName,
      final List<Import> imports,
      final Scope scope,
      final Identifier name,
      final List<Method> body) {
    this.packageName = packageName;
    this.imports = imports;
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

  public List<Import> getImports() {
    return imports;
  }

  public void setImports(List<Import> imports) {
    this.imports = imports;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + ((imports == null) ? 0 : imports.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
    result = prime * result + ((scope == null) ? 0 : scope.hashCode());
    return result;
  }

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
    Clazz other = (Clazz) obj;
    if (body == null) {
      if (other.body != null) {
        return false;
      }
    } else if (!body.equals(other.body)) {
      return false;
    }
    if (imports == null) {
      if (other.imports != null) {
        return false;
      }
    } else if (!imports.equals(other.imports)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (packageName == null) {
      if (other.packageName != null) {
        return false;
      }
    } else if (!packageName.equals(other.packageName)) {
      return false;
    }
    if (scope == null) {
      if (other.scope != null) {
        return false;
      }
    } else if (!scope.equals(other.scope)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    if (packageName != null && !packageName.isEmpty()) {
      strBuilder.append(packageName);
      strBuilder.append('.');
    }
    strBuilder.append(name.toString());
    strBuilder.append(", imports[");
    strBuilder.append(imports.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("], methods[");
    strBuilder.append(body.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("]");
    return strBuilder.toString();
  }
}
