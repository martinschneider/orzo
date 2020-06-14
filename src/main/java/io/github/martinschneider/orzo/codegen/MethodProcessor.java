package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodProcessor {
  // TODO: does this justify its own class?
  public Map<String, Method> getMethodMap(Clazz currentClazz, List<Clazz> clazzes) {
    Map<String, Method> methodMap = new HashMap<>();
    for (Method method : currentClazz.body) {
      methodMap.put(getKey(method), method);
    }
    addImported(methodMap, currentClazz, clazzes);
    return methodMap;
  }

  private Map<String, Method> addImported(
      Map<String, Method> methodMap, Clazz currentClazz, List<Clazz> clazzes) {
    List<Import> imports = currentClazz.imports;
    List<String> importStrings = imports.stream().map(x -> x.id).collect(Collectors.toList());
    for (Clazz clazz : clazzes) {
      if (currentClazz != clazz
          && (currentClazz.packageName.equals(clazz.packageName)
              || importStrings.contains(clazz.fqn()))) {
        for (Method method : clazz.body) {
          methodMap.put(clazz.name.val.toString() + '.' + getKey(method), method);
          methodMap.put(clazz.fqn() + '.' + getKey(method), method);
          // TODO: only do this for static import
          methodMap.put(getKey(method), method);
        }
      }
    }
    return methodMap;
  }

  private String getKey(Method method) {
    return method.name.val.toString() + TypeUtils.argsDescr(method.args);
  }
}
