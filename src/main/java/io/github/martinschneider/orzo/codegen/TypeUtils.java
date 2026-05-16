package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.ArrayTypes.BYTE_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.CHAR_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.DOUBLE_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.FLOAT_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.INT_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.LONG_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.SHORT_ARRAY;
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
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        return IALOAD;
      case BYTE:
        return BALOAD;
      case SHORT:
        return SALOAD;
      case LONG:
        return LALOAD;
      case DOUBLE:
        return DALOAD;
      case FLOAT:
        return FALOAD;
      case CHAR:
        return CALOAD;
    }
    return 0;
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
    return 0;
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

  public static List<String> assignableTo(String type) {
    if (type.contains("[")) {
      return List.of(type);
    }
    switch (type) {
      case STRING:
        return List.of("Ljava/lang/String;");
      case INT:
        return List.of(INT, LONG, DOUBLE, "java.lang.Object", "Object");
      case BYTE:
        return List.of(BYTE, SHORT, INT, LONG, "java.lang.Object", "Object");
      case SHORT:
        return List.of(SHORT, INT, LONG, "java.lang.Object", "Object");
      case LONG:
        return List.of(LONG, "java.lang.Object", "Object");
      case DOUBLE:
        return List.of(DOUBLE, "java.lang.Object", "Object");
      case FLOAT:
        return List.of(FLOAT, DOUBLE, "java.lang.Object", "Object");
      case CHAR:
        return List.of(CHAR, INT, LONG, "java.lang.Object", "Object");
      case BOOLEAN:
        return List.of(BOOLEAN, "java.lang.Object", "Object");
    }
    // Reference types are assignable to java.lang.Object for method lookup purposes.
    if (type != null && !type.isEmpty()) {
      if (type.equals("java.lang.Object")
          || type.equals("Object")
          || type.equals("java/lang/Object")) {
        return List.of("java.lang.Object");
      }
      return List.of(type, "java.lang.Object", "Object");
    }
    return emptyList();
  }

  // cartesian product
  // see:
  // https://codereview.stackexchange.com/questions/67804/generate-cartesian-product-of-list-in-java
  // input: a list of possible types for each argument of a method, for example if
  // the method call is `m(a,b)` with `a` being float and `b` being `int`, typesList = {{float,
  // double}, {int, long}}.
  // output: a list of all possible combinations of types these arguments can be matched
  // to, in the given example: {{float, int}, {float, long}, {double, int}, {double, long}}
  // this can be used to find methods signatures that match input argument types
  public static <T> List<List<T>> combinations(List<List<T>> lists) {
    List<List<T>> combinations = Arrays.asList(Arrays.asList());
    for (List<T> list : lists) {
      List<List<T>> extraColumnCombinations = new ArrayList<>();
      for (List<T> combination : combinations) {
        for (T element : list) {
          List<T> newCombination = new ArrayList<>(combination);
          newCombination.add(element);
          extraColumnCombinations.add(newCombination);
        }
      }
      combinations = extraColumnCombinations;
    }
    return combinations;
  }
}
