package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Method implements ClassMember {

  public List<Argument> args;
  public List<Statement> body;
  public Identifier name;
  public List<AccessFlag> accFlags;
  public String type;
  public String fqClassName;
  public static final String CONSTRUCTOR_NAME = "<init>";

  public Method(
      String fqClassName,
      List<AccessFlag> accFlags,
      String type,
      Identifier name,
      List<Argument> arguments,
      List<Statement> body) {
    this.fqClassName = fqClassName;
    this.accFlags = accFlags;
    this.type = type;
    this.name = name;
    this.args = arguments;
    this.body = body;
  }

  public Method(String clazz, String field, String type, List<String> args) {
    this.fqClassName = clazz;
    this.name = new Identifier(field);
    this.type = type;
    this.args = new ArrayList<>();
    for (int i = 0; i < args.size(); i++) {
      this.args.add(new Argument(args.get(i), new Identifier("arg" + i)));
    }
  }

  public short accessFlags(boolean isInterface) {
    // TODO: handle the general case
    short flags = 0;
    if (isInterface) {
      flags += (short) 0x0401; // ACC_ABSTRACT, ACC_PUBLIC
    } else {
      for (AccessFlag accFlag : accFlags) {
        flags += accFlag.val;
      }
    }
    return flags;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accFlags == null) ? 0 : accFlags.hashCode());
    result = prime * result + ((args == null) ? 0 : args.hashCode());
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Method other = (Method) obj;
    if (accFlags == null) {
      if (other.accFlags != null) return false;
    } else if (!accFlags.equals(other.accFlags)) return false;
    if (args == null) {
      if (other.args != null) return false;
    } else if (!args.equals(other.args)) return false;
    if (body == null) {
      if (other.body != null) return false;
    } else if (!body.equals(other.body)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(accFlags.stream().map(x -> x.name()).collect(Collectors.joining(" ")));
    if (strBuilder.length() > 0) {
      strBuilder.append(' ');
    }
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
