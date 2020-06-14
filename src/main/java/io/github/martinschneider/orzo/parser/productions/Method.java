package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import java.util.List;
import java.util.stream.Collectors;

public class Method {
  public List<Argument> args;
  public List<Statement> body;
  public Identifier name;
  public Scope scope;
  public String type;
  public String fqClassName;

  public Method(
      String fqClassName,
      Scope scope,
      String type,
      Identifier name,
      List<Argument> arguments,
      List<Statement> body) {
    this.fqClassName = fqClassName;
    this.scope = scope;
    this.type = type;
    this.name = name;
    this.args = arguments;
    this.body = body;
  }

  public String getTypeDescr() {
    String typeDescr = TypeUtils.descr(type);
    StringBuilder strBuilder = new StringBuilder("(");
    strBuilder.append(
        args.stream().map(x -> TypeUtils.descr(x.type)).collect(Collectors.joining("")));
    strBuilder.append(')');
    strBuilder.append(typeDescr);
    return strBuilder.toString();
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((args == null) ? 0 : args.hashCode());
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((scope == null) ? 0 : scope.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    Method other = (Method) obj;
    if (args == null) {
      if (other.args != null) {
        return false;
      }
    } else if (!args.equals(other.args)) {
      return false;
    }
    if (body == null) {
      if (other.body != null) {
        return false;
      }
    } else if (!body.equals(other.body)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (scope == null) {
      if (other.scope != null) {
        return false;
      }
    } else if (!scope.equals(other.scope)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    return true;
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
    strBuilder.append(
        args.stream().map(x -> x.name.val.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("}");
    strBuilder.append(", code{");
    strBuilder.append(body.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("}");
    return strBuilder.toString();
  }
}
