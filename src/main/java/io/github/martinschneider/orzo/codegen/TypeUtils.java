package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.ArrayTypes.BOOLEAN_ARRAY;
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

  // "Ljava/lang/String;" → "java.lang.String"; anything else is returned as-is (unlike
  // jvmDescriptorToTypeName(), a class named like a primitive type's descriptor, e.g. "C", stays)
  public static String referenceTypeName(String type) {
    return (type.startsWith("L") && type.endsWith(";"))
        ? type.substring(1, type.length() - 1).replace('/', '.')
        : type;
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
      // TODO: multi-dimensional arrays
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
  /**
   * Whether a value can't be assigned to a variable of the target type (also with boxing): see
   * isIncompatible(), a primitive boxed to another wrapper class (e.g. "Long l = 12;", but "Byte b
   * = 1;" for a constant) or class, or two classes neither of which is a subclass of the other
   * (also an error for a cast, see isInconvertible()).
   */
  public static boolean isIncompatible(String valueType, String targetType, List<Clazz> clazzes) {
    if (isIncompatible(valueType, targetType) || isInconvertible(valueType, targetType, clazzes)) {
      return true;
    } else if (valueType == null
        || targetType == null
        || !isPrimitive(valueType)
        || "void".equals(valueType)
        || isPrimitive(targetType)
        || targetType.startsWith("[")) {
      return false;
    }
    String target = className(targetType);
    String unboxed = unboxedType(target);
    if (unboxed != null) {
      return !unboxed.equals(valueType)
          && !(INT.equals(valueType) && List.of(BYTE, SHORT, CHAR).contains(unboxed));
    }
    Class<?> boxed = boxedClass(valueType);
    Class<?> reflected = reflect(target);
    if (sourceClazz(target, clazzes) != null) {
      return true; // (no wrapper class is a subclass of a class of this compilation run)
    }
    return boxed != null && reflected != null && !reflected.isAssignableFrom(boxed);
  }

  /**
   * Whether a value of one class type can't be cast to another: both are classes (not interfaces)
   * and neither is a subclass of the other.
   */
  public static boolean isInconvertible(String valueType, String targetType, List<Clazz> clazzes) {
    if (valueType == null
        || targetType == null
        || isPrimitive(valueType)
        || isPrimitive(targetType)
        || valueType.startsWith("[")
        || targetType.startsWith("[")) {
      return false;
    }
    String value = canonical(valueType, clazzes);
    String target = canonical(targetType, clazzes);
    if (value.equals(target) || !isClass(value, clazzes) || !isClass(target, clazzes)) {
      return false;
    }
    return !superclasses(value, clazzes).contains(target)
        && !superclasses(target, clazzes).contains(value);
  }

  /** The name of a type as javac displays it, e.g. "String" or "Outer.Inner". */
  public static String displayName(String type) {
    return displayName(type, null);
  }

  /** (also a class of this compilation run named by its simple name) */
  public static String displayName(String type, List<Clazz> clazzes) {
    String name = (clazzes != null) ? canonical(type, clazzes) : className(type);
    return name.substring(name.lastIndexOf('.') + 1).replace('$', '.');
  }

  // a class (not an interface) of this compilation run or loaded by reflection
  private static boolean isClass(String type, List<Clazz> clazzes) {
    Clazz source = sourceClazz(type, clazzes);
    if (source != null) {
      return !source.isInterface;
    }
    Class<?> reflected = reflect(type);
    return reflected != null && !reflected.isInterface() && !reflected.isPrimitive();
  }

  /** The class and its superclasses (of this compilation run or loaded by reflection). */
  public static List<String> superclasses(String type, List<Clazz> clazzes) {
    List<String> supertypes = new ArrayList<>();
    String name = canonical(type, clazzes);
    while (name != null && !supertypes.contains(name)) {
      supertypes.add(name);
      Clazz source = sourceClazz(name, clazzes);
      if (source != null) {
        name = canonical(source.baseClass, clazzes);
        continue;
      }
      Class<?> reflected = reflect(name);
      for (Class<?> c = (reflected != null) ? reflected.getSuperclass() : null;
          c != null;
          c = c.getSuperclass()) {
        supertypes.add(c.getName());
      }
      break;
    }
    return supertypes;
  }

  // e.g. "java.lang.String" for "String", "Ljava/lang/String;" or "java/lang/String"
  private static String className(String type) {
    if (type == null) {
      return null;
    } else if (type.startsWith("L") && type.endsWith(";")) {
      type = type.substring(1, type.length() - 1);
    }
    return STRING.equals(type) ? "java.lang.String" : type.replace('/', '.');
  }

  // the binary name, also of a class of this compilation run named by its simple name
  private static String canonical(String type, List<Clazz> clazzes) {
    String name = className(type);
    Clazz source = sourceClazz(name, clazzes);
    return (source != null) ? source.fqn() : name;
  }

  private static Class<?> reflect(String type) {
    try {
      return Class.forName(type);
    } catch (ClassNotFoundException | LinkageError e) {
      return null;
    }
  }

  private static Class<?> boxedClass(String primitive) {
    for (Map.Entry<String, String> entry : UNBOXED.entrySet()) {
      if (entry.getValue().equals(primitive)) {
        return reflect(entry.getKey());
      }
    }
    return null;
  }

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

  // Whether a declared type exists: a primitive type, a class of this compilation run or a
  // reflectively loadable one (e.g. "pkg.Foo" for an undefined "Foo" in package "pkg" doesn't)
  public static boolean typeExists(String type, List<Clazz> allClazzes, Clazz currentClazz) {
    if (type == null || isPrimitive(type) || STRING.equals(type) || "Object".equals(type)) {
      return true;
    }
    String name = type;
    while (name.endsWith("[]")) {
      name = name.substring(0, name.length() - 2);
    }
    if (name.startsWith("[") || name.startsWith("L") && name.endsWith(";")) {
      return true; // a JVM descriptor (synthesized declarations)
    }
    return classExists(resolveSimpleType(name, allClazzes, currentClazz), allClazzes);
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

  /**
   * The class of this compilation run with a simple name: of several, the one nested in the given
   * class or its innermost enclosing class (e.g. "Inner" in "class A { class Inner {} }" and "class
   * B { class Inner {} }"), else the first one (null if there's none).
   */
  public static Clazz sourceClazzNamed(String name, List<Clazz> clazzes, Clazz scope) {
    List<Clazz> matches = new ArrayList<>();
    for (Clazz clazz : (clazzes != null) ? clazzes : List.<Clazz>of()) {
      if (name.equals(clazz.name)) {
        matches.add(clazz);
      }
    }
    if (matches.size() > 1 && scope != null) {
      List<Clazz> scopes = new ArrayList<>(FieldProcessor.enclosingClasses(scope, clazzes));
      scopes.add(scope);
      for (int i = scopes.size() - 1; i >= 0; i--) {
        String binary = scopes.get(i).fqn();
        binary = binary.substring(binary.lastIndexOf('.') + 1);
        for (Clazz match : matches) {
          if (binary.equals(match.outerName)) {
            return match;
          }
        }
      }
    }
    return matches.isEmpty() ? null : matches.get(0);
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
    Clazz source = sourceClazzNamed(type, allClazzes, currentClazz);
    if (source != null) {
      return source.fqn();
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
      case BOOLEAN:
      case "Z":
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
      case BOOLEAN:
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

  /**
   * The class of the given name loaded by reflection, also a nested class named with dots (e.g.
   * "java.lang.Thread.State" for java.lang.Thread$State), or null.
   */
  public static Class<?> loadClass(String name) {
    for (String binary = name; binary != null; ) {
      try {
        return Class.forName(binary);
      } catch (ClassNotFoundException | LinkageError e) {
        int dot = binary.lastIndexOf('.');
        binary = (dot > 0) ? binary.substring(0, dot) + '$' + binary.substring(dot + 1) : null;
      }
    }
    return null;
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
      case BOOLEAN:
        return BOOLEAN_ARRAY;
    }
    return 0;
  }

  // (boxing conversions come after widening ones: a more specific overload is preferred)
  private static final String NUMBER = "java.lang.Number";

  /** Like assignableTo(type), with the supertypes of the classes of this compilation run, too. */
  public static List<String> assignableTo(String type, List<Clazz> clazzes) {
    List<String> assignables = assignableTo(type);
    Clazz clazz = sourceClazz(type, clazzes);
    if (clazz == null) {
      return assignables;
    }
    List<String> supertypes = new ArrayList<>();
    if (clazz.baseClass != null) {
      supertypes.add(clazz.baseClass);
    }
    supertypes.addAll(clazz.interfaces);
    // (the type, its supertypes, then Object)
    List<String> result = new ArrayList<>();
    result.add(type);
    for (String supertype : supertypes) {
      for (String assignable : assignableTo(supertype, clazzes)) {
        if (!result.contains(assignable) && !isObject(assignable)) {
          result.add(assignable);
        }
      }
    }
    for (String assignable : assignables) {
      if (!result.contains(assignable)) {
        result.add(assignable);
      }
    }
    return result;
  }

  // the classes of the last list passed to sourceClazz() by name (rebuilt when it changes)
  private static List<Clazz> indexedClazzes;
  private static int indexedSize;
  private static Map<String, Clazz> clazzByName;

  // the class of this compilation run with the given (binary) name, or null
  public static Clazz sourceClazz(String type, List<Clazz> clazzes) {
    if (type == null || clazzes == null) {
      return null;
    }
    if (indexedClazzes != clazzes || indexedSize != clazzes.size()) {
      clazzByName = new java.util.HashMap<>();
      Map<String, Clazz> bySimpleName = new java.util.HashMap<>();
      java.util.Set<String> ambiguous = new java.util.HashSet<>();
      for (Clazz clazz : clazzes) {
        clazzByName.putIfAbsent(clazz.fqn(), clazz);
        if (bySimpleName.putIfAbsent(clazz.name, clazz) != null) {
          ambiguous.add(clazz.name);
        }
      }
      // also by the canonical name of a nested class (e.g. "Outer.Inner") and by a unique simple
      // name (e.g. a supertype named before the nested classes were known)
      for (Clazz clazz : clazzes) {
        clazzByName.putIfAbsent(clazz.fqn().replace('$', '.'), clazz);
      }
      for (Map.Entry<String, Clazz> entry : bySimpleName.entrySet()) {
        if (!ambiguous.contains(entry.getKey())) {
          clazzByName.putIfAbsent(entry.getKey(), entry.getValue());
        }
      }
      indexedClazzes = clazzes;
      indexedSize = clazzes.size();
    }
    return clazzByName.get(type.replace('/', '.'));
  }

  private static boolean isObject(String type) {
    return type.equals("java.lang.Object") || type.equals("Object");
  }

  public static List<String> assignableTo(String type) {
    if (type.contains("[")) {
      return List.of(type, "java.lang.Object", "Object"); // an array is an Object, too
    }
    switch (type) {
      case STRING:
        return List.of(
            "Ljava/lang/String;",
            "java.lang.CharSequence",
            COMPARABLE,
            "Ljava/lang/Object;",
            "java.lang.Object",
            "Object");
      case INT:
        return List.of(
            INT,
            LONG,
            DOUBLE,
            "java.lang.Integer",
            NUMBER,
            COMPARABLE,
            "java.lang.Object",
            "Object");
      case BYTE:
        return List.of(
            BYTE,
            SHORT,
            INT,
            LONG,
            "java.lang.Byte",
            NUMBER,
            COMPARABLE,
            "java.lang.Object",
            "Object");
      case SHORT:
        return List.of(
            SHORT, INT, LONG, "java.lang.Short", NUMBER, COMPARABLE, "java.lang.Object", "Object");
      case LONG:
        return List.of(LONG, "java.lang.Long", NUMBER, COMPARABLE, "java.lang.Object", "Object");
      case DOUBLE:
        return List.of(
            DOUBLE, "java.lang.Double", NUMBER, COMPARABLE, "java.lang.Object", "Object");
      case FLOAT:
        return List.of(
            FLOAT, DOUBLE, "java.lang.Float", NUMBER, COMPARABLE, "java.lang.Object", "Object");
      case CHAR:
        return List.of(
            CHAR, INT, LONG, "java.lang.Character", COMPARABLE, "java.lang.Object", "Object");
      case BOOLEAN:
        return List.of(BOOLEAN, "java.lang.Boolean", COMPARABLE, "java.lang.Object", "Object");
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
  // (with boxing, e.g. for "<T extends Comparable<T>> T max(T a, T b)")
  private static final String COMPARABLE = "java.lang.Comparable";

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
