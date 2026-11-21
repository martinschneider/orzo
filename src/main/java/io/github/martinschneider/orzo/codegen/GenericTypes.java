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
      return resolve(((WildcardType) type).getUpperBounds()[0], bindings);
    }
    return "java.lang.Object"; // e.g. a generic array type
  }
}
