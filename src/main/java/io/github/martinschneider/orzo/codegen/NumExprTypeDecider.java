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
import io.github.martinschneider.orzo.lexer.tokens.TernaryExpression;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.CastMethodCall;
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
      } else if (token instanceof TernaryExpression) {
        // cond ? a : b - the type of its branches (combined like any other operands)
        TernaryExpression ternary = (TernaryExpression) token;
        types.add(new NumExprTypeDecider(ctx).getType(classIdMap, ternary.trueBranch));
        types.add(new NumExprTypeDecider(ctx).getType(classIdMap, ternary.falseBranch));
      } else if (token instanceof CastMethodCall) {
        // ((Type) expr).method()...
        CastMethodCall castCall = (CastMethodCall) token;
        String startType =
            (castCall.castType != null)
                ? castCall.castType
                : new NumExprTypeDecider(ctx).getType(classIdMap, castCall.innerExpr);
        String chainType = followChain(classIdMap, startType, null, castCall.next);
        if (chainType != null) {
          types.add(chainType);
        }
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        List<String> argTypes = new ArrayList<>();
        for (Expression exp : methodCall.params) {
          argTypes.add(new NumExprTypeDecider(ctx).getType(classIdMap, exp));
        }
        Method method = ctx.methodCallGen.findMatchingMethod(methodCall.name.toString(), argTypes);
        String methodType = (method != null) ? method.type : null;
        String genericType = null;
        if (method == null && methodCall.name.contains(".")) {
          int dot = methodCall.name.lastIndexOf('.');
          String className = methodCall.name.substring(0, dot);
          String simpleName = methodCall.name.substring(dot + 1);
          // receiver: a variable, optionally followed by a field chain (e.g. "stmt.blocks")
          String[] parts = className.split("\\.");
          VariableInfo receiverVar = classIdMap.variables.localMap.get(parts[0]);
          if (receiverVar == null) receiverVar = classIdMap.variables.fieldMap.get(parts[0]);
          String resolvedClass = className;
          String receiverGeneric = null;
          FieldProcessor.StaticField staticField =
              (receiverVar == null && ctx.staticFieldMap != null)
                  ? ctx.staticFieldMap.get(parts[0])
                  : null;
          if (staticField != null && parts.length == 1) {
            resolvedClass = staticField.fieldType;
            receiverGeneric = staticField.genericType;
          } else if (receiverVar != null && receiverVar.type != null) {
            resolvedClass = receiverVar.type;
            receiverGeneric = receiverVar.genericType;
            for (int p = 1; p < parts.length && resolvedClass != null; p++) {
              FieldProcessor.InstanceField field =
                  new FieldProcessor()
                      .getInstanceFieldMap(resolvedClass, ctx.allClazzes)
                      .get(parts[p]);
              resolvedClass = (field != null) ? field.fieldType : null;
              receiverGeneric = (field != null) ? field.genericType : null;
            }
          }
          if (resolvedClass != null) {
            method =
                ctx.methodCallGen.findMethodViaReflectionTyped(resolvedClass, simpleName, argTypes);
            if (method == null) {
              method =
                  ctx.methodCallGen.findMethodViaReflection(
                      resolvedClass, simpleName, argTypes.size());
            }
          }
          if (method != null) {
            methodType = genericReturnType(method, receiverGeneric, argTypes);
            genericType = lastGeneric;
          }
        }
        if (methodType != null) {
          if (methodCall.arrSel != null) {
            methodType =
                TypeUtils.jvmDescriptorToTypeName(
                    methodType.substring(methodCall.arrSel.exprs.size()));
            genericType = null;
          }
          if (methodCall.next != null) {
            methodType = followChain(classIdMap, methodType, genericType, methodCall.next);
          }
          if (methodType != null) {
            types.add(methodType);
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
        if ("null".equals(id.val.toString()) && id.next == null) {
          types.add(NULL_TYPE);
        } else if ("this".equals(id.val.toString())) {
          types.add("java.lang.Object");
        } else {
          VariableInfo var = ctx.classIdMap.variables.get(token);
          if (var != null) {
            if (id.arrSel != null) {
              String elemType =
                  (id.next != null)
                      ? followChain(classIdMap, var.arrType, null, id.next)
                      : var.arrType;
              if (elemType != null) {
                types.add(elemType);
              }
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
            } else if (classLiteralLink(id) != null) {
              // Class literal, e.g. String.class or a.b.Foo.class, possibly followed by calls
              String classType =
                  followChain(classIdMap, "java.lang.Class", null, classLiteralLink(id).next);
              if (classType != null) {
                types.add(classType);
              }
            } else if (qualifiedStaticFieldType(id) != null) {
              // e.g. java.lang.Integer.MAX_VALUE
              types.add(qualifiedStaticFieldType(id));
            } else if (id.next instanceof MethodCall) {
              // static call on a class name, e.g. String.valueOf(1)
              String className = ctx.methodCallGen.resolveClassName(id.val.toString());
              String resolvedType = followChain(classIdMap, className, null, id.next);
              if (resolvedType != null) {
                types.add(resolvedType);
              }
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

  // Generic type of the most recent genericReturnType() result (null if not generic)
  private String lastGeneric;

  // The return type of a reflectively resolved method, taking the receiver's generic type
  // arguments into account (e.g. List<Foo>.get() returns Foo, not Object).
  private String genericReturnType(Method method, String receiverGeneric, List<String> argTypes) {
    lastGeneric = null;
    if (TypeUtils.isPrimitive(method.type)) {
      return method.type;
    }
    List<String> paramTypes = new ArrayList<>();
    for (Argument arg : method.args) {
      paramTypes.add(arg.type);
    }
    String generic = null;
    if (receiverGeneric != null && !method.type.startsWith("[")) {
      generic = GenericTypes.returnType(receiverGeneric, method.name.val.toString(), paramTypes);
    }
    if (generic == null && method.fqClassName != null) {
      generic =
          GenericTypes.inferredReturnType(
              method.fqClassName, method.name.val.toString(), paramTypes, argTypes);
    }
    if (generic == null) {
      return method.type;
    }
    if (generic.indexOf('<') >= 0) {
      lastGeneric = generic;
    }
    String erased = GenericTypes.erasure(generic);
    return "java.lang.String".equals(erased) ? STRING : erased;
  }

  // Follows a selector chain (".method(...)" / ".field") starting from a value of the given type.
  // Returns null if any link can't be resolved.
  private String followChain(
      GlobalIdentifierMap classIdMap, String type, String generic, Identifier link) {
    while (link != null && type != null) {
      if (link instanceof MethodCall) {
        MethodCall call = (MethodCall) link;
        List<String> argTypes = new ArrayList<>();
        for (Expression exp : call.params) {
          argTypes.add(new NumExprTypeDecider(ctx).getType(classIdMap, exp));
        }
        // a dotted name is a field chain followed by the call, e.g. "val.toString"
        String[] segments = call.name.split("\\.");
        for (int f = 0; f < segments.length - 1 && type != null; f++) {
          FieldProcessor.InstanceField field =
              new FieldProcessor().getInstanceFieldMap(type, ctx.allClazzes).get(segments[f]);
          type = (field != null) ? field.fieldType : null;
          generic = (field != null) ? field.genericType : null;
        }
        if (type == null) {
          return null;
        }
        String methodName = segments[segments.length - 1];
        int lastDot = type.lastIndexOf('.');
        String simpleType = (lastDot >= 0) ? type.substring(lastDot + 1) : type;
        Method method =
            ctx.methodCallGen.findMatchingMethod(simpleType + "." + methodName, argTypes);
        if (method != null) {
          type = method.type;
          generic = null;
        } else {
          method = ctx.methodCallGen.findMethodViaReflectionTyped(type, methodName, argTypes);
          if (method == null) {
            method = ctx.methodCallGen.findMethodViaReflection(type, methodName, argTypes.size());
          }
          if (method == null) {
            // e.g. toString() on an interface type (Class.getMethods() omits Object's methods)
            method =
                ctx.methodCallGen.findMethodViaReflection(
                    "java.lang.Object", methodName, argTypes.size());
          }
          if (method == null) {
            return null;
          }
          type = genericReturnType(method, generic, argTypes);
          generic = lastGeneric;
        }
      } else {
        FieldProcessor.InstanceField field =
            new FieldProcessor().getInstanceFieldMap(type, ctx.allClazzes).get(link.val.toString());
        if (field == null) {
          return null;
        }
        type = field.fieldType;
        generic = field.genericType;
      }
      if (link.arrSel != null && type.startsWith("[")) {
        type = TypeUtils.jvmDescriptorToTypeName(type.substring(link.arrSel.exprs.size()));
        generic = null;
      }
      link = link.next;
    }
    return type;
  }

  // The "class" link of a class literal chain, e.g. in String.class.getName(), or null
  private static Identifier classLiteralLink(Identifier id) {
    for (Identifier link = id.next; link != null; link = link.next) {
      if ("class".equals(link.val.toString())) {
        return link;
      }
    }
    return null;
  }

  // The type of a fully-qualified static field reference like "java.lang.Integer.MAX_VALUE"
  private static String qualifiedStaticFieldType(Identifier id) {
    List<String> parts = new ArrayList<>();
    for (Identifier link = id; link != null; link = link.next) {
      if (link instanceof MethodCall) {
        return null;
      }
      parts.add(link.val.toString());
    }
    if (parts.size() < 3) {
      return null;
    }
    try {
      java.lang.reflect.Field field =
          Class.forName(String.join(".", parts.subList(0, parts.size() - 1)))
              .getField(parts.get(parts.size() - 1));
      String type = field.getType().getName();
      return "java.lang.String".equals(type) ? STRING : type;
    } catch (ClassNotFoundException | NoSuchFieldException | LinkageError e) {
      return null;
    }
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

  /** The type of the null literal: assignable to any reference type (see TypeUtils). */
  public static final String NULL_TYPE = "null";

  private String getSmallestType(Set<String> types) {
    if (types.size() > 1) {
      types.remove(NULL_TYPE); // e.g. "x == null": the other operand's type
    }
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
