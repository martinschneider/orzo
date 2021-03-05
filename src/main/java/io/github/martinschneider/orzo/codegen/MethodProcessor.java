package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;

import io.github.martinschneider.orzo.lexer.tokens.Scopes;
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
import java.util.stream.Collectors;

public class MethodProcessor {
  public Map<String, Method> getMethodMap(Clazz currentClazz, List<Clazz> clazzes) {
    Map<String, Method> methodMap = new HashMap<>();
    for (Method method : currentClazz.methods) {
      methodMap.put(getKey(method), method);
    }
    addImported(methodMap, currentClazz, clazzes);
    addJavaLang(methodMap, currentClazz);
    return methodMap;
  }

  private Map<String, Method> addImported(
      Map<String, Method> methodMap, Clazz currentClazz, List<Clazz> clazzes) {
    List<Import> imports = currentClazz.imports;
    List<String> importStrings = imports.stream().map(x -> x.id).collect(Collectors.toList());
    for (Clazz clazz : clazzes) {
      if (!currentClazz.equals(clazz)
          && (currentClazz.packageName.equals(clazz.packageName)
              || importStrings.contains(clazz.fqn()))) {
        for (Method method : clazz.methods) {
          methodMap.put(clazz.name + '.' + getKey(method), method);
          methodMap.put(clazz.fqn() + '.' + getKey(method), method);
          // TODO: only do this for static imports
          methodMap.put(getKey(method), method);
        }
      }
    }
    return methodMap;
  }

  // Java specs Chapter 7: Code in a compilation unit automatically has access to all types declared
  // in its package and also automatically imports all of the public types declared in the
  // predefined package java.lang.
  private Map<String, Method> addJavaLang(Map<String, Method> methodMap, Clazz currentClazz) {
    // TODO: Decide which classes to include. For simplicity, we limit this to what's actually used
    // in the samples.
    for (Class<?> clazz :
        List.of(Math.class, Double.class, Character.class, Integer.class, Long.class)) {
      for (java.lang.reflect.Method m : clazz.getMethods()) {
        if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
          List<Argument> args = mapArgs(m.getParameters());
          Method method =
              new Method(
                  clazz.getName(),
                  scope(Scopes.PUBLIC),
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
    }
    return methodMap;
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
