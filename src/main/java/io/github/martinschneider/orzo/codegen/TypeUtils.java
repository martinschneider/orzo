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
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TypeUtils {
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
    if (type.startsWith("L") && type.endsWith(";")) {
      return type;
    }
    // TODO: use a single check
    else if (type.contains(STRING) && !type.contains("java.lang.String")) {
      return type.replaceAll(STRING, "Ljava/lang/String;");
    } else if (type.contains(BYTE)) {
      return type.replaceAll(BYTE, "B");
    } else if (type.contains(CHAR)) {
      return type.replaceAll(CHAR, "C");
    } else if (type.contains(DOUBLE)) {
      return type.replaceAll(DOUBLE, "D");
    } else if (type.contains(FLOAT)) {
      return type.replaceAll(FLOAT, "F");
    } else if (type.contains(INT)) {
      return type.replaceAll(INT, "I");
    } else if (type.contains(LONG)) {
      return type.replaceAll(LONG, "J");
    } else if (type.contains(SHORT)) {
      return type.replaceAll(SHORT, "S");
    } else if (type.contains(VOID)) {
      return type.replaceAll(VOID, "V");
    } else if (type.contains(BOOLEAN)) {
      return type.replaceAll(BOOLEAN, "Z");
    } else {
      return "L" + type.replaceAll("\\.", "/") + ";";
    }
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
        return List.of(INT, LONG, DOUBLE);
      case BYTE:
        return List.of(BYTE, SHORT, INT, LONG);
      case SHORT:
        return List.of(SHORT, INT, LONG);
      case LONG:
        return List.of(LONG);
      case DOUBLE:
        return List.of(DOUBLE);
      case FLOAT:
        return List.of(FLOAT, DOUBLE);
      case CHAR:
        return List.of(CHAR, INT, LONG);
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
