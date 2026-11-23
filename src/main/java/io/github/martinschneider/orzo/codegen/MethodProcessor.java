package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static java.util.List.of;

import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodProcessor {
  // the methods of all classes of the last list passed to getMethodMap() (see shared())
  private static List<Clazz> sharedClazzes;
  private static int sharedSize;
  private static Map<String, Method> shared;

  public Map<String, Method> getMethodMap(Clazz currentClazz, List<Clazz> clazzes) {
    Map<String, Method> methodMap = new HashMap<>(shared(clazzes));
    addStaticImportMethods(methodMap, currentClazz, clazzes);
    addInherited(methodMap, currentClazz, List.of("", currentClazz.name + '.'));
    // the static methods of the enclosing classes of a nested class
    for (Clazz outer : FieldProcessor.enclosingClasses(currentClazz, clazzes)) {
      for (Method method : outer.methods) {
        if (method.accFlags.contains(AccessFlag.ACC_STATIC)) {
          methodMap.put(getKey(method), method);
        }
      }
    }
    // the instance methods of the enclosing instances of an inner class (the innermost first)
    List<Clazz> outers = FieldProcessor.enclosingClasses(currentClazz, clazzes);
    Clazz inner = currentClazz;
    for (int i = outers.size() - 1; i >= 0 && inner.isInner; i--) {
      for (Method method : outers.get(i).methods) {
        if (!method.accFlags.contains(AccessFlag.ACC_STATIC) && !(method instanceof Constructor)) {
          methodMap.putIfAbsent(getKey(method), method);
        }
      }
      inner = outers.get(i);
    }
    // last: the class's own methods shadow same-named ones registered under a bare name above
    for (Method method : currentClazz.methods) {
      methodMap.put(getKey(method), method);
      methodMap.put(currentClazz.name + '.' + getKey(method), method);
    }
    return methodMap;
  }

  // The methods of all classes, by their qualified names (e.g. "Foo.bar(I)V"): the same for every
  // class, so built once for a list of classes (rebuilt when it changes)
  private Map<String, Method> shared(List<Clazz> clazzes) {
    if (sharedClazzes != clazzes || sharedSize != clazzes.size()) {
      shared = new HashMap<>();
      addImported(shared, null, clazzes);
      for (Clazz clazz : clazzes) {
        addInherited(shared, clazz, List.of(clazz.name + '.', clazz.fqn() + '.'));
      }
      sharedClazzes = clazzes;
      sharedSize = clazzes.size();
    }
    return shared;
  }

  // The public methods a class of this compilation run inherits from its (reflectively loadable)
  // superclass, e.g. name() and ordinal() of an enum (registered under the given key prefixes)
  private void addInherited(Map<String, Method> methodMap, Clazz clazz, List<String> prefixes) {
    if (clazz.baseClass == null) {
      return;
    }
    try {
      Class<?> superClass = Class.forName(clazz.baseClass.replace('/', '.'));
      for (java.lang.reflect.Method m : superClass.getMethods()) {
        if (m.isBridge()) {
          continue;
        }
        List<AccessFlag> flags = new ArrayList<>(of(AccessFlag.ACC_PUBLIC));
        if (Modifier.isStatic(m.getModifiers())) {
          flags.add(AccessFlag.ACC_STATIC);
        }
        Method method =
            new Method(
                m.getDeclaringClass().getName(),
                flags,
                m.getReturnType().getName(),
                id(m.getName()),
                mapArgs(m.getParameters()),
                null);
        for (String prefix : prefixes) {
          methodMap.putIfAbsent(prefix + getKey(method), method);
        }
      }
    } catch (ClassNotFoundException | LinkageError e) {
      // a superclass of this compilation run: its methods are registered as such
    }
  }

  private Map<String, Method> addImported(
      Map<String, Method> methodMap, Clazz currentClazz, List<Clazz> clazzes) {
    for (Clazz clazz : clazzes) {
      if (clazz != currentClazz) {
        for (Method method : clazz.methods) {
          methodMap.put(clazz.name + '.' + getKey(method), method);
          methodMap.put(clazz.fqn() + '.' + getKey(method), method);
        }
        // (and those it inherits from supertypes of this compilation run, e.g. superinterfaces)
        for (Clazz supertype : sourceSupertypes(clazz, clazzes)) {
          for (Method method : supertype.methods) {
            methodMap.putIfAbsent(clazz.name + '.' + getKey(method), method);
            methodMap.putIfAbsent(clazz.fqn() + '.' + getKey(method), method);
          }
        }
      }
    }
    return methodMap;
  }

  // the supertypes of a class that are classes of this compilation run (transitively)
  static List<Clazz> sourceSupertypes(Clazz clazz, List<Clazz> clazzes) {
    List<Clazz> supertypes = new ArrayList<>();
    List<String> names = new ArrayList<>();
    if (clazz.baseClass != null) {
      names.add(clazz.baseClass);
    }
    names.addAll(clazz.interfaces);
    for (String name : names) {
      Clazz supertype = TypeUtils.sourceClazz(name, clazzes);
      if (supertype != null && !supertypes.contains(supertype)) {
        supertypes.add(supertype);
        for (Clazz inherited : sourceSupertypes(supertype, clazzes)) {
          if (!supertypes.contains(inherited)) {
            supertypes.add(inherited);
          }
        }
      }
    }
    return supertypes;
  }

  private void addStaticImportMethods(
      Map<String, Method> methodMap, Clazz currentClazz, List<Clazz> clazzes) {
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
      String className = importPath.substring(0, lastDot);
      boolean isWildcard = "*".equals(memberName);
      Clazz sameRun = null;
      for (Clazz clazz : clazzes) {
        if (clazz.fqn().equals(className)) {
          sameRun = clazz;
        }
      }
      if (sameRun != null) {
        // a class of this compilation run
        for (Method method : sameRun.methods) {
          if (method.accFlags.contains(AccessFlag.ACC_STATIC)
              && !method.accFlags.contains(AccessFlag.ACC_PRIVATE)
              && (isWildcard || method.name.val.toString().equals(memberName))) {
            methodMap.put(getKey(method), method);
          }
        }
        continue;
      }
      try {
        Class<?> clazz = Class.forName(className);
        for (java.lang.reflect.Method m : clazz.getMethods()) {
          if (Modifier.isPublic(m.getModifiers())
              && Modifier.isStatic(m.getModifiers())
              && (isWildcard || m.getName().equals(memberName))) {
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
      Argument arg = new Argument(param.getType().getName(), id(param.getName()));
      arg.varargs = param.isVarArgs();
      args.add(arg);
    }
    return args;
  }

  private String getKey(Method method) {
    return method.name.val.toString() + TypeUtils.argsDescr(method.args);
  }
}
