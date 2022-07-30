package io.github.martinschneider.orzo.parser.productions;

import static io.github.martinschneider.orzo.parser.productions.Method.CONSTRUCTOR_NAME;

import io.github.martinschneider.orzo.lexer.tokens.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Clazz {
  public static final String JAVA_LANG_OBJECT = "java.lang.Object";
  public static final String JAVA_LANG_ENUM = "java.lang.Enum";
  public boolean isInterface;
  public boolean isEnum;
  public List<Method> methods;
  public List<Import> imports;
  public List<ParallelDeclaration> fields;
  public List<String> interfaces;
  public String baseClass;
  public String name;
  public String packageName;
  public Scope scope;

  public Clazz(
      String packageName,
      List<Import> imports,
      Scope scope,
      String name,
      boolean isInterface,
      boolean isEnum,
      List<String> interfaces,
      String baseClass,
      List<Method> methods,
      List<ParallelDeclaration> fields) {
    this.packageName = packageName;
    this.imports = imports;
    this.scope = scope;
    this.name = name;
    this.methods = methods;
    this.fields = fields;
    this.interfaces = interfaces;
    this.baseClass = baseClass;
    this.isInterface = isInterface;
    this.isEnum = isEnum;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((baseClass == null) ? 0 : baseClass.hashCode());
    result = prime * result + ((fields == null) ? 0 : fields.hashCode());
    result = prime * result + ((imports == null) ? 0 : imports.hashCode());
    result = prime * result + ((interfaces == null) ? 0 : interfaces.hashCode());
    result = prime * result + (isEnum ? 1231 : 1237);
    result = prime * result + (isInterface ? 1231 : 1237);
    result = prime * result + ((methods == null) ? 0 : methods.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
    result = prime * result + ((scope == null) ? 0 : scope.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Clazz other = (Clazz) obj;
    if (baseClass == null) {
      if (other.baseClass != null) return false;
    } else if (!baseClass.equals(other.baseClass)) return false;
    if (fields == null) {
      if (other.fields != null) return false;
    } else if (!fields.equals(other.fields)) return false;
    if (imports == null) {
      if (other.imports != null) return false;
    } else if (!imports.equals(other.imports)) return false;
    if (interfaces == null) {
      if (other.interfaces != null) return false;
    } else if (!interfaces.equals(other.interfaces)) return false;
    if (isEnum != other.isEnum) return false;
    if (isInterface != other.isInterface) return false;
    if (methods == null) {
      if (other.methods != null) return false;
    } else if (!methods.equals(other.methods)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (packageName == null) {
      if (other.packageName != null) return false;
    } else if (!packageName.equals(other.packageName)) return false;
    if (scope == null) {
      if (other.scope != null) return false;
    } else if (!scope.equals(other.scope)) return false;
    return true;
  }

  public String fqn() {
    return fqn('.');
  }

  public String fqn(char sep) {
    StringBuilder strBuilder = new StringBuilder();
    if (packageName != null && !packageName.isEmpty()) {
      strBuilder.append(packageName.replace('.', sep));
      strBuilder.append(sep);
    }
    strBuilder.append(name);
    return strBuilder.toString();
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    if (isInterface) {
      strBuilder.append("interface ");
    } else if (isEnum) {
      strBuilder.append("enum ");
    } else {
      strBuilder.append("class ");
    }
    if (packageName != null && !packageName.isEmpty()) {
      strBuilder.append(packageName);
      strBuilder.append('.');
    }
    strBuilder.append(name.toString());
    strBuilder.append(", implements[");
    strBuilder.append(interfaces.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    if (baseClass != JAVA_LANG_OBJECT) {
      strBuilder.append("], extends[");
      strBuilder.append(baseClass);
    }
    strBuilder.append("], imports[");
    strBuilder.append(imports.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("], methods[");
    strBuilder.append(methods.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("], fields[");
    strBuilder.append(fields.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    return strBuilder.toString();
  }

  public List<Method> getConstructors() {
    List<Method> constructors = new ArrayList<>();
    for (Method method : methods) {
      if (method.name.eq(CONSTRUCTOR_NAME)) { // && method.args.isEmpty()) {
        constructors.add(method);
      }
    }
    return constructors;
  }
}
