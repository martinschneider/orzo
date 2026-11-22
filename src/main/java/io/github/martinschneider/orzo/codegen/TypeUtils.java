package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.ArrayTypes.BYTE_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.CHAR_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.DOUBLE_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.FLOAT_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.INT_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.LONG_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.SHORT_ARRAY;
import static io.github.martinschneider.orzo.codegen.OpCodes.AALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.AASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.BALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.BASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.CALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.CASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.DALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.FALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.IASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.LALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.SALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.SASTORE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypeUtils {
  public static boolean isPrimitive(String type) {
    if (type == null) return false;
    switch (type) {
      case "int":
      case "long":
      case "byte":
      case "short":
      case "char":
      case "float":
      case "double":
      case "boolean":
      case "void":
        return true;
      default:
        return false;
    }
  }

  // "I" → "int", "Ljava/lang/String;" → "java.lang.String"; anything else is returned as-is
  public static String jvmDescriptorToTypeName(String descriptor) {
    switch (descriptor) {
      case "I":
        return INT;
      case "B":
        return BYTE;
      case "S":
        return SHORT;
      case "J":
        return LONG;
      case "D":
        return DOUBLE;
      case "F":
        return FLOAT;
      case "C":
        return CHAR;
      case "Z":
        return BOOLEAN;
      default:
        if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
          return descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
        }
        return descriptor;
    }
  }

  public static String descr(String type) {
    return descr(type, 0);
  }

  public static String descr(VariableInfo varInfo) {
    if (varInfo.type.equals(REF)) {
      // TODO: multi-arrays
      return descr(varInfo.arrType, 1);
    }
    return descr(varInfo.type);
  }

  public static String descr(String type, int arrDim) {
    for (int i = 0; i < arrDim; i++) {
      type = '[' + type;
    }
    // Already a fully-formed descriptor
    if (type.startsWith("L") && type.endsWith(";")) {
      return type;
    }
    // Separate array prefix from base type name
    int depth = 0;
    while (depth < type.length() && type.charAt(depth) == '[') {
      depth++;
    }
    String prefix = type.substring(0, depth);
    String base = type.substring(depth);
    // Already a formed array element (e.g. "[I" → base="I", "[Ljava/lang/Object;" → has ';')
    if (!prefix.isEmpty() && (base.length() == 1 || base.contains(";"))) {
      return type.replace('.', '/');
    }
    // Exact primitive type matching (no substring confusion)
    switch (base) {
      case "byte":
        return prefix + "B";
      case "char":
        return prefix + "C";
      case "double":
        return prefix + "D";
      case "float":
        return prefix + "F";
      case "int":
        return prefix + "I";
      case "long":
        return prefix + "J";
      case "short":
        return prefix + "S";
      case "void":
        return prefix + "V";
      case "boolean":
        return prefix + "Z";
      case "Object":
        return prefix + "Ljava/lang/Object;";
      case "String":
        return prefix + "Ljava/lang/String;";
    }
    // Reference type (possibly qualified): wrap in L...;
    if (base.equals("java.lang.String")) {
      return prefix + "Ljava/lang/String;";
    }
    return prefix + "L" + base.replace('.', '/') + ";";
  }

  public static String methodDescr(Method method) {
    StringBuilder strBuilder = new StringBuilder(argsDescr(method.args));
    strBuilder.append(TypeUtils.descr(method.type));
    return strBuilder.toString();
  }

  public static String methodDescr(Method method, List<Clazz> allClazzes, Clazz currentClazz) {
    StringBuilder sb = new StringBuilder("(");
    sb.append(
        method.args.stream()
            .map(a -> descr(resolveSimpleType(a.type, allClazzes, currentClazz)))
            .collect(Collectors.joining("")));
    sb.append(')');
    sb.append(descr(resolveSimpleType(method.type, allClazzes, currentClazz)));
    return sb.toString();
  }

  /**
   * Whether a value of one type can certainly not be assigned to a variable of another type, e.g. a
   * String to an int or an int to a String (only these clear cases are detected; there is no full
   * type check yet).
   */
  public static boolean isIncompatible(String valueType, String targetType) {
    if (valueType == null || targetType == null) {
      return false;
    }
    boolean targetIsString = STRING.equals(targetType) || "java.lang.String".equals(targetType);
    boolean valueIsString = STRING.equals(valueType) || "java.lang.String".equals(valueType);
    boolean targetNumeric = isPrimitive(targetType) && !BOOLEAN.equals(targetType);
    boolean valueNumeric = isPrimitive(valueType) && !BOOLEAN.equals(valueType);
    return (targetNumeric && valueIsString)
        || (targetIsString && (valueNumeric || BOOLEAN.equals(valueType)));
  }

  // Whether a class (binary name with '.' or '/') exists: a class of this compilation run or a
  // reflectively loadable one
  public static boolean classExists(String className, List<Clazz> allClazzes) {
    String name = className.replace('/', '.');
    if (allClazzes != null) {
      for (Clazz clazz : allClazzes) {
        if (clazz.fqn().equals(name)) {
          return true;
        }
      }
    }
    try {
      Class.forName(name, false, TypeUtils.class.getClassLoader());
      return true;
    } catch (ClassNotFoundException | LinkageError e) {
      return false;
    }
  }

  // Whether a class (binary name with '.' or '/') is an interface: a class of this compilation run
  // or a reflectively loadable one
  public static boolean isInterface(String className, List<Clazz> allClazzes) {
    if (className == null) {
      return false;
    }
    String name = className.replace('/', '.');
    if (allClazzes != null) {
      for (Clazz clazz : allClazzes) {
        if (clazz.fqn().equals(name)) {
          return clazz.isInterface;
        }
      }
    }
    try {
      return Class.forName(name, false, TypeUtils.class.getClassLoader()).isInterface();
    } catch (ClassNotFoundException | LinkageError e) {
      return false;
    }
  }

  public static String resolveSimpleType(String type, List<Clazz> allClazzes, Clazz currentClazz) {
    if (type == null) return null;
    if (isPrimitive(type)
        || type.contains(".")
        || type.startsWith("[")
        || "void".equals(type)
        || "String".equals(type)
        || "Object".equals(type)) {
      return type;
    }
    if (allClazzes != null) {
      for (Clazz clazz : allClazzes) {
        if (type.equals(clazz.name)) {
          return clazz.fqn();
        }
      }
    }
    if (currentClazz != null && currentClazz.imports != null) {
      for (Import imp : currentClazz.imports) {
        if (imp.isStatic || imp.id == null) continue;
        if (imp.id.endsWith("." + type)) {
          return imp.id;
        }
      }
    }
    // a (reflectively loadable) class of the same package, a wildcard import or java.lang
    List<String> candidates = new ArrayList<>();
    if (currentClazz != null) {
      if (currentClazz.packageName != null && !currentClazz.packageName.isEmpty()) {
        candidates.add(currentClazz.packageName + "." + type);
      }
      if (currentClazz.imports != null) {
        for (Import imp : currentClazz.imports) {
          if (!imp.isStatic && imp.id != null && imp.id.endsWith(".*")) {
            candidates.add(imp.id.substring(0, imp.id.length() - 1) + type);
          }
        }
      }
    }
    candidates.add("java.lang." + type);
    for (String candidate : candidates) {
      try {
        Class.forName(candidate);
        return candidate;
      } catch (ClassNotFoundException | LinkageError e) {
        // try the next one
      }
    }
    return type;
  }

  public static String argsDescr(List<Argument> args) {
    StringBuilder strBuilder = new StringBuilder("(");
    strBuilder.append(
        args.stream().map(x -> TypeUtils.descr(x.type)).collect(Collectors.joining("")));
    strBuilder.append(')');
    return strBuilder.toString();
  }

  public static String typesDescr(List<String> types) {
    StringBuilder strBuilder = new StringBuilder("(");
    strBuilder.append(types.stream().map(x -> TypeUtils.descr(x)).collect(Collectors.joining("")));
    strBuilder.append(')');
    return strBuilder.toString();
  }

  public static byte getLoadOpCode(String type) {
    switch (type) {
      case INT:
      case "I":
        return IALOAD;
      case BYTE:
      case "B":
        return BALOAD;
      case SHORT:
      case "S":
        return SALOAD;
      case LONG:
      case "J":
        return LALOAD;
      case DOUBLE:
      case "D":
        return DALOAD;
      case FLOAT:
      case "F":
        return FALOAD;
      case CHAR:
      case "C":
        return CALOAD;
    }
    return AALOAD;
  }

  public static byte getStoreOpCode(String type) {
    switch (type) {
      case INT:
        return IASTORE;
      case BYTE:
        return BASTORE;
      case SHORT:
        return SASTORE;
      case LONG:
        return LASTORE;
      case DOUBLE:
        return DASTORE;
      case FLOAT:
        return FASTORE;
      case CHAR:
        return CASTORE;
    }
    return AASTORE;
  }

  public static byte getArrayType(String type) {
    switch (type) {
      case INT:
        return INT_ARRAY;
      case BYTE:
        return BYTE_ARRAY;
      case SHORT:
        return SHORT_ARRAY;
      case LONG:
        return LONG_ARRAY;
      case DOUBLE:
        return DOUBLE_ARRAY;
      case FLOAT:
        return FLOAT_ARRAY;
      case CHAR:
        return CHAR_ARRAY;
    }
    return 0;
  }

  // (boxing conversions come after widening ones: a more specific overload is preferred)
  private static final String NUMBER = "java.lang.Number";

  public static List<String> assignableTo(String type) {
    if (type.contains("[")) {
      return List.of(type, "java.lang.Object", "Object"); // an array is an Object, too
    }
    switch (type) {
      case STRING:
        return List.of("Ljava/lang/String;", "Ljava/lang/Object;", "java.lang.Object", "Object");
      case INT:
        return List.of(
            INT, LONG, DOUBLE, "java.lang.Integer", NUMBER, "java.lang.Object", "Object");
      case BYTE:
        return List.of(
            BYTE, SHORT, INT, LONG, "java.lang.Byte", NUMBER, "java.lang.Object", "Object");
      case SHORT:
        return List.of(SHORT, INT, LONG, "java.lang.Short", NUMBER, "java.lang.Object", "Object");
      case LONG:
        return List.of(LONG, "java.lang.Long", NUMBER, "java.lang.Object", "Object");
      case DOUBLE:
        return List.of(DOUBLE, "java.lang.Double", NUMBER, "java.lang.Object", "Object");
      case FLOAT:
        return List.of(FLOAT, DOUBLE, "java.lang.Float", NUMBER, "java.lang.Object", "Object");
      case CHAR:
        return List.of(CHAR, INT, LONG, "java.lang.Character", "java.lang.Object", "Object");
      case BOOLEAN:
        return List.of(BOOLEAN, "java.lang.Boolean", "java.lang.Object", "Object");
    }
    // Reference types are assignable to java.lang.Object for method lookup purposes.
    if (type != null && !type.isEmpty()) {
      if (type.equals("java.lang.Object")
          || type.equals("Object")
          || type.equals("java/lang/Object")) {
        return List.of("java.lang.Object");
      }
      List<String> assignables = new ArrayList<>();
      assignables.add(type);
      try {
        Class<?> clazz = Class.forName(type.replace('/', '.'));
        // superclasses first, then all (also inherited and super-) interfaces
        List<Class<?>> interfaces = new ArrayList<>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
          if (c != clazz && !c.getName().equals("java.lang.Object")) {
            assignables.add(c.getName());
            assignables.add(c.getSimpleName());
          }
          interfaces.addAll(List.of(c.getInterfaces()));
        }
        for (int i = 0; i < interfaces.size(); i++) {
          Class<?> iface = interfaces.get(i);
          if (!assignables.contains(iface.getName())) {
            assignables.add(iface.getName());
            assignables.add(iface.getSimpleName());
            interfaces.addAll(List.of(iface.getInterfaces()));
          }
        }
      } catch (ClassNotFoundException e) {
        // not on classpath
      }
      assignables.add("java.lang.Object");
      assignables.add("Object");
      String primitive = UNBOXED.get(type);
      if (primitive != null) {
        // unboxing (and widening), e.g. an Integer argument for an int or long parameter
        for (String unboxed : assignableTo(primitive)) {
          if (isPrimitive(unboxed)) {
            assignables.add(unboxed);
          }
        }
      }
      return assignables;
    }
    return emptyList();
  }

  /** The primitive type of a wrapper class, e.g. "int" for "java.lang.Integer" (else null). */
  public static String unboxedType(String type) {
    return (type == null) ? null : UNBOXED.get(type.replace('/', '.'));
  }

  private static final Map<String, String> UNBOXED =
      Map.of(
          "java.lang.Integer", INT,
          "java.lang.Long", LONG,
          "java.lang.Double", DOUBLE,
          "java.lang.Float", FLOAT,
          "java.lang.Byte", BYTE,
          "java.lang.Short", SHORT,
          "java.lang.Character", CHAR,
          "java.lang.Boolean", BOOLEAN);
}
