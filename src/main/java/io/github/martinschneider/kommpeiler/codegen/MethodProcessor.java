package io.github.martinschneider.kommpeiler.codegen;

import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodProcessor {
  public Map<String, Method> getMethodMap(Clazz clazz) {
    Map<String, Method> methodMap = new HashMap<>();
    for (Method method : clazz.getBody()) {
      methodMap.put(method.getName().getValue().toString(), method);
    }
    return methodMap;
  }
}
