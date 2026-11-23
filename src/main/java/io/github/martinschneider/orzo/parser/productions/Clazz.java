package io.github.martinschneider.orzo.parser.productions;

import static io.github.martinschneider.orzo.parser.productions.Method.CONSTRUCTOR_NAME;

import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.ArrayList;
import java.util.List;

public class Clazz {
  public static final String JAVA_LANG_OBJECT = "java.lang.Object";
  public static final String JAVA_LANG_ENUM = "java.lang.Enum";
  public boolean isInterface;
  public boolean isEnum;
  public boolean isAbstract;
  public List<Method> methods;
  public List<Import> imports;
  public List<ParallelDeclaration> fields;
  // the field declarations and instance initializer blocks, in source order (see MemberProcessor)
  public List<Statement> instanceInits = new ArrayList<>();
  public List<String> interfaces;
  public String baseClass;
  public String name;
  // the enclosing class's binary name (e.g. "Outer" or "Outer$Inner") of a nested class
  public String outerName;
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
    if (fields != null) {
      instanceInits.addAll(fields); // (no initializer blocks)
    }
    this.interfaces = interfaces;
    this.baseClass = baseClass;
    this.isInterface = isInterface;
    this.isEnum = isEnum;
    this.sourceFile = sourceFile;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return ObjectUtils.equals(this, obj);
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
    if (outerName != null) {
      strBuilder.append(outerName).append('$');
    }
    strBuilder.append(name);
    return strBuilder.toString();
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
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
