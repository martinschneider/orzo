package io.github.martinschneider.kommpeiler.parser.productions;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BOOLEAN;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BYTE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.CHAR;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.DOUBLE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.FLOAT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.SHORT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.STRING;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.VOID;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Scope;
import java.util.List;
import java.util.stream.Collectors;

public class Method {
  private List<Argument> arguments;
  private List<Statement> body;
  private Identifier name;
  private Scope scope;
  private String type;

  /**
   * @param scope scope
   * @param type return-type
   * @param name name
   * @param body method-body
   */
  public Method(
      final Scope scope,
      final String type,
      final Identifier name,
      final List<Argument> arguments,
      final List<Statement> body) {
    this.scope = scope;
    this.type = type;
    this.name = name;
    this.arguments = arguments;
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

  public String getType() {
    return type;
  }

  public String getTypeDescr() {
    String typeDescr = getDescr(type);
    StringBuilder strBuilder = new StringBuilder("(");
    strBuilder.append(
        arguments.stream().map(x -> getDescr(x.getType())).collect(Collectors.joining("")));
    strBuilder.append(')');
    strBuilder.append(typeDescr);
    return strBuilder.toString();
  }

  public String getDescr(String type) {
    // TODO: general handling of reference types and arrays
    if (type.contains(STRING)) {
      type = type.replaceAll(STRING, "Ljava/lang/String;");
      return type;
    } else if (type.contains(BYTE)) {
      type = type.replaceAll(BYTE, "B");
    } else if (type.contains(CHAR)) {
      type = type.replaceAll(CHAR, "C");
    } else if (type.contains(DOUBLE)) {
      type = type.replaceAll(DOUBLE, "D");
    } else if (type.contains(FLOAT)) {
      type = type.replaceAll(FLOAT, "F");
    } else if (type.contains(INT)) {
      type = type.replaceAll(INT, "I");
    } else if (type.contains(LONG)) {
      type = type.replaceAll(LONG, "J");
    } else if (type.contains(SHORT)) {
      type = type.replaceAll(SHORT, "S");
    } else if (type.contains(VOID)) {
      type = type.replaceAll(VOID, "V");
    } else if (type.contains(BOOLEAN)) {
      type = type.replaceAll(BOOLEAN, "Z");
    }
    return type;
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

  public void setType(final String type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
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
    if (arguments == null) {
      if (other.arguments != null) {
        return false;
      }
    } else if (!arguments.equals(other.arguments)) {
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
        arguments.stream()
            .map(x -> x.getName().getValue().toString())
            .collect(Collectors.joining(", ")));
    strBuilder.append("}");
    strBuilder.append(", code{");
    strBuilder.append(body.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("}");
    return strBuilder.toString();
  }
}
