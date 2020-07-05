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
import javax.annotation.processing.ProcessingEnvironment;

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
      if (currentClazz != clazz
          && (currentClazz.packageName.equals(clazz.packageName)
              || importStrings.contains(clazz.fqn()))) {
        for (Method method : clazz.methods) {
          methodMap.put(clazz.name.val.toString() + '.' + getKey(method), method);
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
    // TODO: not sure whether we should stick with the standard here or limit automatic imports to
    // ceratin classes
    for (Class<?> clazz :
        List.of(
            AbstractMethodError.class,
            Appendable.class,
            ArithmeticException.class,
            ArrayIndexOutOfBoundsException.class,
            ArrayStoreException.class,
            AssertionError.class,
            AutoCloseable.class,
            Boolean.class,
            BootstrapMethodError.class,
            Byte.class,
            Character.class,
            CharSequence.class,
            Class.class,
            ClassCastException.class,
            ClassCircularityError.class,
            ClassFormatError.class,
            ClassLoader.class,
            ClassNotFoundException.class,
            ClassValue.class,
            Cloneable.class,
            CloneNotSupportedException.class,
            Comparable.class,
            Deprecated.class,
            Double.class,
            Enum.class,
            EnumConstantNotPresentException.class,
            Error.class,
            Exception.class,
            ExceptionInInitializerError.class,
            Float.class,
            FunctionalInterface.class,
            IllegalAccessError.class,
            IllegalAccessException.class,
            IllegalArgumentException.class,
            IllegalCallerException.class,
            IllegalMonitorStateException.class,
            IllegalStateException.class,
            IncompatibleClassChangeError.class,
            InheritableThreadLocal.class,
            InstantiationError.class,
            InstantiationException.class,
            Integer.class,
            InternalError.class,
            InterruptedException.class,
            Iterable.class,
            LayerInstantiationException.class,
            LinkageError.class,
            Long.class,
            Math.class,
            Module.class,
            ModuleLayer.class,
            NegativeArraySizeException.class,
            NoClassDefFoundError.class,
            NoSuchFieldError.class,
            NoSuchFieldException.class,
            NoSuchMethodError.class,
            NoSuchMethodException.class,
            NullPointerException.class,
            Number.class,
            NumberFormatException.class,
            Object.class,
            OutOfMemoryError.class,
            Override.class,
            Package.class,
            Process.class,
            ProcessBuilder.class,
            ProcessingEnvironment.class,
            ProcessHandle.class,
            Readable.class,
            // Record.class,
            ReflectiveOperationException.class,
            Runnable.class,
            Runtime.class,
            RuntimeException.class,
            RuntimePermission.class,
            SafeVarargs.class,
            SecurityException.class,
            SecurityManager.class,
            Short.class,
            StackOverflowError.class,
            StackTraceElement.class,
            StackWalker.class,
            StrictMath.class,
            StringBuffer.class,
            StringBuilder.class,
            StringIndexOutOfBoundsException.class,
            SuppressWarnings.class,
            System.class,
            Thread.class,
            ThreadDeath.class,
            ThreadGroup.class,
            ThreadLocal.class,
            Throwable.class,
            TypeNotPresentException.class,
            UnknownError.class,
            UnsatisfiedLinkError.class,
            UnsupportedClassVersionError.class,
            UnsupportedOperationException.class,
            VerifyError.class,
            VirtualMachineError.class,
            Void.class)) {
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
