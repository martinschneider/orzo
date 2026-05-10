package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static java.util.List.of;

import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodProcessor {
  public Map<String, Method> getMethodMap(Clazz currentClazz, List<Clazz> clazzes) {
    Map<String, Method> methodMap = new HashMap<>();
    for (Method method : currentClazz.methods) {
      methodMap.put(getKey(method), method);
    }
    addImported(methodMap, currentClazz, clazzes);
    addJavaLang(methodMap, currentClazz);
    addStaticImportMethods(methodMap, currentClazz);
    return methodMap;
  }

  private Map<String, Method> addImported(
      Map<String, Method> methodMap, Clazz currentClazz, List<Clazz> clazzes) {
    for (Clazz clazz : clazzes) {
      if (!currentClazz.equals(clazz)) {
        for (Method method : clazz.methods) {
          methodMap.put(clazz.name + '.' + getKey(method), method);
          methodMap.put(clazz.fqn() + '.' + getKey(method), method);
          if (method.accFlags.contains(AccessFlag.ACC_STATIC)) {
            methodMap.put(getKey(method), method);
          }
        }
      }
    }
    return methodMap;
  }

  // Java specs Chapter 7 (https://docs.oracle.com/javase/specs/jls/se18/html/jls-7.html):
  // Code in a compilation unit automatically has access to all types declared in its package
  // and also automatically imports all of the public types declared in the predefined package
  // java.lang.
  private Map<String, Method> addJavaLang(Map<String, Method> methodMap, Clazz currentClazz) {
    // TODO: Decide which classes to include. For simplicity, we limit this to what's actually used
    // in the samples.
    for (Class<?> clazz :
        List.of(
            Math.class,
            Double.class,
            Float.class,
            Character.class,
            Integer.class,
            Long.class,
            String.class,
            io.github.martinschneider.orzo.util.ObjectUtils.class)) {
      for (java.lang.reflect.Method m : clazz.getMethods()) {
        if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
          List<Argument> args = mapArgs(m.getParameters());
          Method method =
              new Method(
                  clazz.getName(),
                  // TODO: process all flags
                  of(AccessFlag.ACC_PUBLIC, AccessFlag.ACC_STATIC),
                  m.getReturnType().getTypeName(),
                  id(m.getName()),
                  args,
                  null);
          methodMap.put(clazz.getName() + '.' + getKey(method), method);
          methodMap.put(clazz.getSimpleName() + '.' + getKey(method), method);
          // TODO: only do this for static imports
          methodMap.put(getKey(method), method);
        }
      }

      // Add constructors for basic types
      for (java.lang.reflect.Constructor<?> c : clazz.getConstructors()) {
        if (Modifier.isPublic(c.getModifiers())) {
          List<Argument> args = mapArgs(c.getParameters());
          Method constructor =
              new Method(
                  clazz.getName().replace('.', '/'), // Use JVM class name format
                  // Constructors are public but not static
                  of(AccessFlag.ACC_PUBLIC),
                  "void", // Constructors return void
                  id("<init>"), // Constructor method name
                  args,
                  null);
          // Add constructor to method map with <init> key
          String constructorKey =
              "<init>"
                  + TypeUtils.typesDescr(
                      args.stream()
                          .map(arg -> arg.type)
                          .collect(java.util.stream.Collectors.toList()));
          methodMap.put(constructorKey, constructor);
        }
      }
    }
    return methodMap;
  }

  private void addStaticImportMethods(Map<String, Method> methodMap, Clazz currentClazz) {
    if (currentClazz.imports == null) {
      return;
    }
    for (Import imp : currentClazz.imports) {
      if (!imp.isStatic) {
        continue;
      }
      String importPath = imp.id;
      int lastDot = importPath.lastIndexOf('.');
      if (lastDot < 0) {
        continue;
      }
      String memberName = importPath.substring(lastDot + 1);
      if ("*".equals(memberName)) {
        continue;
      }
      String className = importPath.substring(0, lastDot);
      try {
        Class<?> clazz = Class.forName(className);
        for (java.lang.reflect.Method m : clazz.getMethods()) {
          if (Modifier.isPublic(m.getModifiers())
              && Modifier.isStatic(m.getModifiers())
              && m.getName().equals(memberName)) {
            List<Argument> args = mapArgs(m.getParameters());
            Method method =
                new Method(
                    clazz.getName(),
                    of(AccessFlag.ACC_PUBLIC, AccessFlag.ACC_STATIC),
                    m.getReturnType().getName(),
                    id(m.getName()),
                    args,
                    null);
            methodMap.put(getKey(method), method);
          }
        }
      } catch (ClassNotFoundException e) {
        // not on classpath, skip
      }
    }
  }

  private List<Argument> mapArgs(Parameter[] params) {
    List<Argument> args = new ArrayList<>();
    for (Parameter param : params) {
      args.add(new Argument(param.getType().getName(), id(param.getName())));
    }
    return args;
  }

  private String getKey(Method method) {
    return method.name.val.toString() + TypeUtils.argsDescr(method.args);
  }
}
