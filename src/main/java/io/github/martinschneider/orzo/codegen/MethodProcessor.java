package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodProcessor {
  // TODO: does this justify its own class?
  public Map<String, Method> getMethodMap(Clazz clazz) {
    Map<String, Method> methodMap = new HashMap<>();
    for (Method method : clazz.getBody()) {
      methodMap.put(method.getName().getValue().toString(), method);
    }
    return methodMap;
  }
}
