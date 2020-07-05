package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import java.util.List;
import java.util.stream.Collectors;

public class Clazz {
  public List<Method> methods;
  public List<Import> imports;
  public List<ParallelDeclaration> fields;
  public Identifier name;
  public String packageName;
  public Scope scope;

  public Clazz(
      String packageName,
      List<Import> imports,
      Scope scope,
      Identifier name,
      List<Method> methods,
      List<ParallelDeclaration> fields) {
    this.packageName = packageName;
    this.imports = imports;
    this.scope = scope;
    this.name = name;
    this.methods = methods;
    this.fields = fields;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((methods == null) ? 0 : methods.hashCode());
    result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
    if (methods == null) {
      if (other.methods != null) {
        return false;
      }
    } else if (!methods.equals(other.methods)) {
      return false;
    }
    if (fields == null) {
      if (other.fields != null) {
        return false;
      }
    } else if (!fields.equals(other.fields)) {
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

  public String fqn() {
    return fqn('.');
  }

  public String fqn(char sep) {
    StringBuilder strBuilder = new StringBuilder();
    if (packageName != null) {
      strBuilder.append(packageName.replace('.', sep));
      strBuilder.append(sep);
    }
    strBuilder.append(name);
    return strBuilder.toString();
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
    strBuilder.append(methods.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("], fields[");
    strBuilder.append(fields.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    return strBuilder.toString();
  }
}
