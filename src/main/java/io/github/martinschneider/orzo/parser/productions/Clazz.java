package io.github.martinschneider.orzo.parser.productions;

import static io.github.martinschneider.orzo.parser.productions.Method.CONSTRUCTOR_NAME;

import io.github.martinschneider.orzo.lexer.tokens.Scope;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
  public String sourceFile;

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
      List<ParallelDeclaration> fields,
      String sourceFile) {
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
    this.sourceFile = sourceFile;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        baseClass,
        fields,
        imports,
        interfaces,
        isEnum,
        isInterface,
        methods,
        name,
        packageName,
        scope,
        sourceFile);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Clazz other = (Clazz) obj;
    return Objects.equals(baseClass, other.baseClass)
        && Objects.equals(fields, other.fields)
        && Objects.equals(imports, other.imports)
        && Objects.equals(interfaces, other.interfaces)
        && isEnum == other.isEnum
        && isInterface == other.isInterface
        && Objects.equals(methods, other.methods)
        && Objects.equals(name, other.name)
        && Objects.equals(packageName, other.packageName)
        && Objects.equals(scope, other.scope)
        && Objects.equals(sourceFile, other.sourceFile);
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
