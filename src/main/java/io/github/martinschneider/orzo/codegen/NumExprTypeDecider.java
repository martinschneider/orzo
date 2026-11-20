package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;

import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NumExprTypeDecider {
  private CGContext ctx;

  public NumExprTypeDecider(CGContext ctx) {
    this.ctx = ctx;
  }

  public String getType(GlobalIdentifierMap classIdMap, Expression expr) {
    if (expr == null) return null;
    if (expr.cast != null) {
      return expr.cast.name;
    }
    Set<String> types = new HashSet<>();
    if (expr instanceof ArrayInit) {
      types.add(((ArrayInit) expr).typeDescr());
    }
    for (Token token : expr.tokens) {
      if (token instanceof IntLiteral) {
        long intValue = ((BigInteger) token.val).longValue();
        if (intValue > Integer.MAX_VALUE) {
          types.add(LONG);
        } else {
          types.add(INT);
        }
      } else if (token instanceof FPLiteral) {
        types.add(DOUBLE);
      } else if (token instanceof Chr) {
        types.add(CHAR);
      } else if (token instanceof BoolLiteral) {
        types.add(BOOLEAN);
      } else if (token instanceof Str) {
        types.add(STRING);
      } else if (token instanceof io.github.martinschneider.orzo.lexer.tokens.LambdaExpr) {
        types.add(io.github.martinschneider.orzo.lexer.tokens.LambdaExpr.PSEUDO_TYPE);
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        List<String> argTypes = new ArrayList<>();
        for (Expression exp : methodCall.params) {
          argTypes.add(new NumExprTypeDecider(ctx).getType(classIdMap, exp));
        }
        Method method = ctx.methodCallGen.findMatchingMethod(methodCall.name.toString(), argTypes);
        if (method == null && methodCall.name.contains(".")) {
          int dot = methodCall.name.lastIndexOf('.');
          String className = methodCall.name.substring(0, dot);
          String simpleName = methodCall.name.substring(dot + 1);
          VariableInfo receiverVar = classIdMap.variables.localMap.get(className);
          if (receiverVar == null) receiverVar = classIdMap.variables.fieldMap.get(className);
          String resolvedClass =
              (receiverVar != null && receiverVar.type != null) ? receiverVar.type : className;
          method =
              ctx.methodCallGen.findMethodViaReflection(resolvedClass, simpleName, argTypes.size());
        }
        if (method != null) {
          if (methodCall.arrSel != null) {
            types.add(method.type.substring(methodCall.arrSel.exprs.size()));
          } else {
            types.add(method.type);
          }
        }
      } else if (token instanceof ConstructorCall) {
        // token.val is the simple, as-written class name (e.g. "ByteArrayOutputStream") - resolve
        // it against the import list so overload resolution downstream (isArgTypeCompatible) can
        // actually Class.forName() it, instead of silently defaulting to "assume compatible" for
        // every overload when used as a constructor/method argument (see commit history: picked
        // the wrong PrintStream(String) overload over PrintStream(OutputStream) for
        // "new PrintStream(new ByteArrayOutputStream())").
        types.add(TypeUtils.resolveSimpleType(token.val.toString(), ctx.allClazzes, ctx.clazz));
      } else if (token instanceof Identifier) {
        Identifier id = (Identifier) token;
        if ("this".equals(id.val.toString())) {
          types.add("java.lang.Object");
        } else {
          VariableInfo var = ctx.classIdMap.variables.get(token);
          if (var != null) {
            if (id.arrSel != null) {
              types.add(var.arrType);
            } else if (var.arrType != null) {
              if (id.next != null && "length".equals(id.next.val.toString())) {
                types.add(INT);
              } else {
                types.add("[" + var.arrType);
              }
            } else if (id.next != null) {
              String chainType = var.type;
              Identifier link = id.next;
              while (link != null && chainType != null) {
                if ("length".equals(link.val.toString()) && chainType.startsWith("[")) {
                  chainType = INT;
                  link = link.next;
                  continue;
                }
                FieldProcessor.InstanceField field =
                    new FieldProcessor()
                        .getInstanceFieldMap(chainType, ctx.allClazzes)
                        .get(link.val.toString());
                if (field != null) {
                  chainType = field.fieldType;
                  if (link.arrSel != null && chainType.startsWith("[")) {
                    chainType = chainType.substring(1);
                  }
                  link = link.next;
                } else {
                  chainType = null;
                }
              }
              types.add(chainType != null ? chainType : var.type);
            } else {
              types.add(var.type);
            }
          } else {
            FieldProcessor.StaticField sf = ctx.staticFieldMap.get(id.val.toString());
            if (sf != null) {
              types.add(sf.fieldType);
            } else if (id.next != null && "class".equals(id.next.val.toString())) {
              // Class literal, e.g. String.class
              types.add("java.lang.Class");
            } else if (id.next != null) {
              // ClassName.fieldName pattern (e.g. Operators.LESS, Byte.MAX_VALUE)
              String resolvedType = resolveChainedType(id.val.toString(), id.next.val.toString());
              if (resolvedType != null) {
                types.add(resolvedType);
              }
            }
          }
        }
      }
    }
    return getSmallestType(types);
  }

  private String resolveChainedType(String className, String fieldName) {
    String fqn = null;
    if (ctx.clazz != null && ctx.clazz.imports != null) {
      for (io.github.martinschneider.orzo.parser.productions.Import imp : ctx.clazz.imports) {
        if (imp.isStatic || imp.id == null) continue;
        if (imp.id.endsWith(".*")) {
          String pkg = imp.id.substring(0, imp.id.length() - 2);
          String candidate = pkg + "." + className;
          try {
            Class.forName(candidate);
            fqn = candidate;
            break;
          } catch (ClassNotFoundException e) {
            // try next
          }
        } else {
          String simpleName =
              imp.id.contains(".") ? imp.id.substring(imp.id.lastIndexOf('.') + 1) : imp.id;
          if (simpleName.equals(className)) {
            fqn = imp.id;
            break;
          }
        }
      }
    }
    if (fqn == null) {
      try {
        Class.forName("java.lang." + className);
        fqn = "java.lang." + className;
      } catch (ClassNotFoundException e) {
        // not in java.lang
      }
    }
    if (fqn == null) return null;
    try {
      java.lang.reflect.Field field = Class.forName(fqn).getField(fieldName);
      return field.getType().getName();
    } catch (ClassNotFoundException | NoSuchFieldException e) {
      return null;
    }
  }

  private String getSmallestType(Set<String> types) {
    String arrayType = getArrayType(types);
    if (arrayType != null) {
      return arrayType;
    }
    // A string literal/value can only appear in Java mixed with other operand types via "+"
    // string concatenation (e.g. "x=" + intVar) — there's no other valid combination — so its
    // presence always means the overall expression is a String, regardless of what other
    // numeric-looking types got collected from the individual tokens.
    if (types.contains(STRING)) {
      return STRING;
    }
    if (types.contains(CHAR)) {
      return CHAR;
    } else if (types.contains(BOOLEAN)) {
      return BOOLEAN;
    } else if (types.contains(DOUBLE)) {
      return DOUBLE;
    } else if (types.contains(FLOAT)) {
      return FLOAT;
    } else if (types.contains(LONG)) {
      return LONG;
    } else if (types.contains(INT)) {
      return INT;
    } else if (types.contains(SHORT)) {
      return SHORT;
    } else if (types.contains(BYTE)) {
      return BYTE;
    }
    // For reference types not matched above, return as-is rather than defaulting to INT
    for (String type : types) {
      if (type != null && !type.isEmpty()) {
        return type;
      }
    }
    return INT;
  }

  private String getArrayType(Set<String> types) {
    for (String type : types) {
      if (type.contains("[")) {
        return type;
      }
    }
    return null;
  }
}
