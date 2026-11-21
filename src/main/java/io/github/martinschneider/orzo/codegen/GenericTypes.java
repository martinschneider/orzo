package io.github.martinschneider.orzo.codegen;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal generic type tracking. A generic type is represented as a string of fully-qualified
 * names, e.g. "java.util.Map<java.lang.String,java.util.List<a.b.Foo>>". Used to recover the real
 * return type of a method on a generic receiver (e.g. List.get() returns E, erased to Object).
 */
public class GenericTypes {

  private GenericTypes() {}

  /**
   * The JVM signature (e.g. for a field's Signature attribute) of a generic type string, e.g.
   * "Ljava/util/Map<Ljava/lang/String;[B>;" for "java.util.Map<java.lang.String,[B>".
   */
  public static String toSignature(String genericType) {
    if (genericType.startsWith("[")) {
      return genericType.replace('.', '/'); // already a descriptor, e.g. "[B"
    }
    String raw = erasure(genericType);
    StringBuilder sig = new StringBuilder("L").append(raw.replace('.', '/'));
    if (genericType.indexOf('<') >= 0) {
      sig.append('<');
      for (String arg : typeArgs(genericType)) {
        sig.append(toSignature(arg));
      }
      sig.append('>');
    }
    return sig.append(';').toString();
  }

  /**
   * The JVM signature of a method with generic parameter types (e.g.
   * "(Ljava/util/List<Ljava/lang/String;>;)V"), or null if it has none (or one that uses a type
   * variable or a wildcard, as type parameter declarations aren't tracked).
   */
  public static String methodSignature(
      io.github.martinschneider.orzo.parser.productions.Method method,
      List<io.github.martinschneider.orzo.parser.productions.Clazz> allClazzes,
      io.github.martinschneider.orzo.parser.productions.Clazz clazz) {
    boolean generic = false;
    StringBuilder sig = new StringBuilder("(");
    for (io.github.martinschneider.orzo.parser.productions.Argument arg : method.args) {
      if (arg.genericType != null) {
        if (!isFullyResolved(arg.genericType)) {
          return null;
        }
        sig.append(toSignature(arg.genericType));
        generic = true;
      } else {
        sig.append(TypeUtils.descr(TypeUtils.resolveSimpleType(arg.type, allClazzes, clazz)));
      }
    }
    if (!generic) {
      return null;
    }
    sig.append(')');
    sig.append(TypeUtils.descr(TypeUtils.resolveSimpleType(method.type, allClazzes, clazz)));
    return sig.toString();
  }

  // no type variables or wildcards, e.g. "java.util.Map<java.lang.String,[B>"
  private static boolean isFullyResolved(String genericType) {
    if (genericType.startsWith("[")) {
      return true;
    }
    if (!erasure(genericType).contains(".")) {
      return false;
    }
    for (String arg : typeArgs(genericType)) {
      if (!isFullyResolved(arg)) {
        return false;
      }
    }
    return true;
  }

  /** "java.util.List<a.b.Foo>" → "java.util.List" */
  public static String erasure(String genericType) {
    int lt = genericType.indexOf('<');
    return (lt < 0) ? genericType : genericType.substring(0, lt);
  }

  /** "java.util.Map<K,java.util.List<V>>" → ["K", "java.util.List<V>"] */
  public static List<String> typeArgs(String genericType) {
    List<String> args = new ArrayList<>();
    int lt = genericType.indexOf('<');
    if (lt < 0 || !genericType.endsWith(">")) {
      return args;
    }
    int depth = 0;
    int start = lt + 1;
    for (int i = start; i < genericType.length() - 1; i++) {
      char c = genericType.charAt(i);
      if (c == '<') {
        depth++;
      } else if (c == '>') {
        depth--;
      } else if (c == ',' && depth == 0) {
        args.add(genericType.substring(start, i).trim());
        start = i + 1;
      }
    }
    args.add(genericType.substring(start, genericType.length() - 1).trim());
    return args;
  }

  /**
   * Returns the generic return type of the given method invoked on a receiver of the given generic
   * type, e.g. "a.b.Foo" for List.get(int) on "java.util.List<a.b.Foo>". Returns null if it can't
   * be determined (receiver not generic, method not found via reflection, return type not depending
   * on the receiver's type arguments etc.).
   */
  public static String returnType(
      String receiverGenericType, String methodName, List<String> paramTypes) {
    if (receiverGenericType == null || receiverGenericType.indexOf('<') < 0) {
      return null;
    }
    try {
      Class<?> clazz = Class.forName(erasure(receiverGenericType));
      java.lang.reflect.Method method = findMethod(clazz, methodName, paramTypes);
      if (method == null) {
        return null;
      }
      Map<String, String> bindings = bindings(clazz, typeArgs(receiverGenericType));
      Map<String, String> declBindings = bindingsFor(clazz, bindings, method.getDeclaringClass());
      if (declBindings == null) {
        return null;
      }
      if (method.getReturnType().isArray()) {
        return null; // generic arrays (e.g. Class<?>[]) aren't modelled
      }
      return resolve(method.getGenericReturnType(), declBindings);
    } catch (ClassNotFoundException | LinkageError e) {
      return null;
    }
  }

  /**
   * The return type of a generic method like {@code <T> T[] copyOf(T[], int)} with its method type
   * variables bound from the argument types, e.g. "[Ljava.lang.String;" for a String[] argument.
   * Returns null if the method can't be found or its return type doesn't use a type variable bound
   * by a parameter of type T or T[].
   */
  public static String inferredReturnType(
      String className, String methodName, List<String> paramTypes, List<String> argTypes) {
    try {
      java.lang.reflect.Method method =
          findMethod(Class.forName(className.replace('/', '.')), methodName, paramTypes);
      if (method == null) {
        return null;
      }
      Map<String, String> bindings = new HashMap<>();
      Type[] params = method.getGenericParameterTypes();
      for (int i = 0; i < params.length && i < argTypes.size(); i++) {
        String argType = argTypes.get(i);
        if (argType == null || "reference".equals(argType) || "null".equals(argType)) {
          continue; // no usable type information
        }
        if (params[i] instanceof TypeVariable && !TypeUtils.isPrimitive(argType)) {
          bindings.putIfAbsent(((TypeVariable<?>) params[i]).getName(), referenceName(argType));
        } else if (params[i] instanceof java.lang.reflect.GenericArrayType
            && ((java.lang.reflect.GenericArrayType) params[i]).getGenericComponentType()
                instanceof TypeVariable
            && argType.startsWith("[")) {
          String component = TypeUtils.jvmDescriptorToTypeName(argType.substring(1));
          if (!TypeUtils.isPrimitive(component)) {
            TypeVariable<?> var =
                (TypeVariable<?>)
                    ((java.lang.reflect.GenericArrayType) params[i]).getGenericComponentType();
            bindings.putIfAbsent(var.getName(), referenceName(component));
          }
        }
      }
      Type ret = method.getGenericReturnType();
      if (ret instanceof TypeVariable) {
        return bindings.get(((TypeVariable<?>) ret).getName());
      }
      if (ret instanceof java.lang.reflect.GenericArrayType
          && ((java.lang.reflect.GenericArrayType) ret).getGenericComponentType()
              instanceof TypeVariable) {
        String bound =
            bindings.get(
                ((TypeVariable<?>)
                        ((java.lang.reflect.GenericArrayType) ret).getGenericComponentType())
                    .getName());
        return (bound == null) ? null : "[L" + bound + ";";
      }
      return null;
    } catch (ClassNotFoundException | LinkageError e) {
      return null;
    }
  }

  // "String" → "java.lang.String"; other names are kept
  private static String referenceName(String type) {
    return "String".equals(type) ? "java.lang.String" : type.replace('/', '.');
  }

  /**
   * The generic type of parameter {@code index} of a method invoked on a receiver of the given
   * generic type (null if not generic), e.g. "java.util.function.Function<a.Foo,java.lang.Object>"
   * for Stream.map(Function<? super T, ? extends R>) on "java.util.stream.Stream<a.Foo>". Returns
   * null unless the result is a parameterized type.
   */
  public static String parameterType(
      String receiverGeneric,
      String className,
      String methodName,
      List<String> paramTypes,
      int index) {
    try {
      Class<?> clazz =
          Class.forName(
              (receiverGeneric != null) ? erasure(receiverGeneric) : className.replace('/', '.'));
      java.lang.reflect.Method method = findMethod(clazz, methodName, paramTypes);
      if (method == null || index >= method.getParameterCount()) {
        return null;
      }
      Map<String, String> bindings = new HashMap<>();
      if (receiverGeneric != null) {
        bindings =
            bindingsFor(
                clazz, bindings(clazz, typeArgs(receiverGeneric)), method.getDeclaringClass());
        if (bindings == null) {
          return null;
        }
      }
      String type = resolve(method.getGenericParameterTypes()[index], bindings);
      return (type.indexOf('<') >= 0) ? type : null;
    } catch (ClassNotFoundException | LinkageError e) {
      return null;
    }
  }

  /**
   * The parameter types of a functional interface's single abstract method for the given generic
   * interface type, e.g. ["a.Foo"] for Function.apply on "java.util.function.Function<a.Foo,R>".
   */
  public static List<String> samParameterTypes(
      String interfaceGeneric, java.lang.reflect.Method sam) {
    List<String> result = new ArrayList<>();
    try {
      Class<?> iface = Class.forName(erasure(interfaceGeneric));
      Map<String, String> bindings =
          bindingsFor(iface, bindings(iface, typeArgs(interfaceGeneric)), sam.getDeclaringClass());
      for (Type param : sam.getGenericParameterTypes()) {
        result.add(bindings == null ? null : resolve(param, bindings));
      }
    } catch (ClassNotFoundException | LinkageError e) {
      // no type information
    }
    return result;
  }

  /** The generic type of a reflected field, or null if it isn't parameterized. */
  public static String fieldType(java.lang.reflect.Field field) {
    if (field.getGenericType() instanceof ParameterizedType) {
      return resolve(field.getGenericType(), new HashMap<>());
    }
    return null;
  }

  private static java.lang.reflect.Method findMethod(
      Class<?> clazz, String name, List<String> paramTypes) {
    for (java.lang.reflect.Method m : clazz.getMethods()) {
      if (!m.getName().equals(name) || m.isBridge() || m.getParameterCount() != paramTypes.size()) {
        continue;
      }
      boolean match = true;
      Class<?>[] params = m.getParameterTypes();
      for (int i = 0; i < params.length; i++) {
        String expected = paramTypes.get(i);
        if (!params[i].getName().equals(expected) && !params[i].getSimpleName().equals(expected)) {
          match = false;
          break;
        }
      }
      if (match) {
        return m;
      }
    }
    return null;
  }

  private static Map<String, String> bindings(Class<?> clazz, List<String> args) {
    Map<String, String> bindings = new HashMap<>();
    TypeVariable<?>[] params = clazz.getTypeParameters();
    for (int i = 0; i < params.length && i < args.size(); i++) {
      bindings.put(params[i].getName(), args.get(i));
    }
    return bindings;
  }

  // Walks the supertype hierarchy from clazz to target, carrying type-variable bindings along
  // (e.g. ArrayList<E=Foo> → List<E=Foo> → Collection<E=Foo>).
  private static Map<String, String> bindingsFor(
      Class<?> clazz, Map<String, String> bindings, Class<?> target) {
    if (clazz == target) {
      return bindings;
    }
    List<Type> supers = new ArrayList<>();
    if (clazz.getGenericSuperclass() != null) {
      supers.add(clazz.getGenericSuperclass());
    }
    for (Type t : clazz.getGenericInterfaces()) {
      supers.add(t);
    }
    for (Type sup : supers) {
      Class<?> raw;
      Map<String, String> supBindings = new HashMap<>();
      if (sup instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType) sup;
        raw = (Class<?>) pt.getRawType();
        TypeVariable<?>[] params = raw.getTypeParameters();
        Type[] actual = pt.getActualTypeArguments();
        for (int i = 0; i < params.length; i++) {
          supBindings.put(params[i].getName(), resolve(actual[i], bindings));
        }
      } else {
        raw = (Class<?>) sup;
      }
      Map<String, String> result = bindingsFor(raw, supBindings, target);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static String resolve(Type type, Map<String, String> bindings) {
    if (type instanceof Class) {
      return ((Class<?>) type).getName();
    } else if (type instanceof TypeVariable) {
      String bound = bindings.get(((TypeVariable<?>) type).getName());
      if (bound != null) {
        return bound;
      }
      return resolve(((TypeVariable<?>) type).getBounds()[0], new HashMap<>());
    } else if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      StringBuilder str = new StringBuilder(((Class<?>) pt.getRawType()).getName());
      str.append('<');
      Type[] actual = pt.getActualTypeArguments();
      for (int i = 0; i < actual.length; i++) {
        if (i > 0) {
          str.append(',');
        }
        str.append(resolve(actual[i], bindings));
      }
      str.append('>');
      return str.toString();
    } else if (type instanceof WildcardType) {
      // "? super T" → T, "? extends T" / "?" → the upper bound
      WildcardType wildcard = (WildcardType) type;
      return resolve(
          (wildcard.getLowerBounds().length > 0)
              ? wildcard.getLowerBounds()[0]
              : wildcard.getUpperBounds()[0],
          bindings);
    }
    return "java.lang.Object"; // e.g. a generic array type
  }
}
