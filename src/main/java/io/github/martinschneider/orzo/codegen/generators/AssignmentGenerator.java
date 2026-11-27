package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.AALOAD;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.FieldProcessor;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.IdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.TernaryExpression;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.CastMethodCall;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.util.List;
import java.util.Map;

public class AssignmentGenerator implements StatementGenerator<Assignment> {
  private CGContext ctx;

  private static final String LOG_NAME = "assignment generator";

  public AssignmentGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, Assignment assignment) {
    // Handle field access assignments (this.field = value)
    if (assignment.left.size() == 2 && assignment.right.size() == 1) {
      // Check if this is a field assignment like "this.field = value"
      Identifier first = assignment.left.get(0);
      Identifier second = assignment.left.get(1);

      if ("this".equals(first.val.toString()) || "super".equals(first.val.toString())) {
        // Handle this.field = value (or super.field = value) as a single field assignment
        return handleThisFieldAssignment(out, method, second, assignment.right.get(0));
      }
    }

    // Handle chained field assignment (a.b = value where a.next = b)
    // Exclude this/super - those are handled by the existing while loop below
    if (assignment.left.size() == 1
        && assignment.right.size() == 1
        && assignment.left.get(0).next != null
        && !"this".equals(assignment.left.get(0).val.toString())
        && !"super".equals(assignment.left.get(0).val.toString())) {
      return handleChainedFieldAssignment(
          out, method, assignment.left.get(0), assignment.right.get(0));
    }

    // Safety check to prevent IndexOutOfBoundsException
    if (assignment.left.size() != assignment.right.size()) {
      ctx.errors.addError(
          "assignment generator",
          String.format(
              "Mismatch between left (%d) and right (%d) sides of assignment",
              assignment.left.size(), assignment.right.size()),
          new RuntimeException().getStackTrace());
      return out;
    }

    for (int i = 0; i < assignment.left.size(); i++) {
      Identifier left = assignment.left.get(i);
      Expression right = assignment.right.get(i);
      Identifier id = left;
      boolean thisLoaded = false;
      while (id != null) {
        if ("this".equals(id.val) || "super".equals(id.val)) {
          ctx.loadGen.loadReference(out, (short) 0);
          thisLoaded = true;
        } else {
          // When 'this' was loaded, look up the field directly to avoid the local/field name
          // collision (e.g. constructor param 'type' vs field 'type')
          VariableInfo varInfo =
              thisLoaded
                  ? ctx.classIdMap.variables.fieldMap.get(id.val.toString())
                  : ctx.classIdMap.variables.get(id);
          String valueType = new NumExprTypeDecider(ctx).getType(ctx.classIdMap, right);
          if (varInfo == null) {
            ctx.errors.addError(
                LOG_NAME,
                String.format("cannot find variable %s", id.val),
                new RuntimeException().getStackTrace());
          } else if (left.arrSel == null && TypeUtils.isIncompatible(valueType, varInfo.type)) {
            ctx.errors.addError(
                LOG_NAME,
                String.format(
                    "incompatible types: %s cannot be converted to %s",
                    TypeUtils.displayName(valueType), TypeUtils.displayName(varInfo.type)),
                new RuntimeException().getStackTrace());
          } else {
            String type = varInfo.type;
            // if the current left side variable appears anywhere on the right side
            // we store its value in a tmp variable
            if (i < assignment.left.size() - 1
                && replaceIds(assignment.right, left, ctx.classIdMap.variables.tmpCount)) {
              if (left.arrSel != null) {
                type = varInfo.arrType;
              }
              Identifier tpmId = id("tmp_" + ctx.classIdMap.variables.tmpCount);
              ctx.classIdMap.variables.putLocal(
                  tpmId,
                  new VariableInfo(
                      tpmId.val.toString(),
                      type,
                      emptyList(),
                      false,
                      (short) ctx.classIdMap.variables.localSize,
                      null));
              VariableInfo tmpInfo = ctx.classIdMap.variables.get(tpmId);
              if (left.arrSel == null) {
                ctx.loadGen.load(out, varInfo);
              } else {
                ctx.loadGen.loadValueFromArray(out, ctx.classIdMap, left.arrSel.exprs, varInfo);
              }
              ctx.storeGen.store(out, tmpInfo);
              ctx.classIdMap.variables.tmpCount++;
            }
            if (left.arrSel == null) {
              if (!thisLoaded
                  && varInfo.isField
                  && !varInfo.accFlags.contains(AccessFlag.ACC_STATIC)) {
                ctx.loadGen.loadOwner(out, varInfo);
              }
              ctx.exprGen.eval(out, type, right);
              // (eval() converts primitives; boxing and reference casts, e.g. of
              // Deque<String>.pop(), are left)
              String actualType = ctx.opStack.type();
              if (varInfo.arrType == null) {
                ctx.basicGen.checkAssignable(valueType, actualType, type);
              }
              boolean needsAutobox =
                  actualType != null
                      && TypeUtils.isPrimitive(actualType)
                      && !TypeUtils.isPrimitive(type);
              boolean needsRefCast =
                  actualType != null
                      && !actualType.equals(type)
                      && !TypeUtils.isPrimitive(actualType)
                      && !TypeUtils.isPrimitive(type);
              // unboxing, e.g. "int x = deque.pop();" on a Deque<Integer>
              boolean needsUnbox =
                  actualType != null
                      && !TypeUtils.isPrimitive(actualType)
                      && TypeUtils.isPrimitive(type);
              if (needsAutobox || needsRefCast || needsUnbox) {
                ctx.basicGen.convert1(out, actualType, type);
              }
              ctx.storeGen.store(out, varInfo);
            } else {
              assignInArray(out, ctx.classIdMap, left, right);
            }
          }
        }
        id = id.next;
      }
    }
    return out;
  }

  public HasOutput assign(
      DynamicByteArray out, IdentifierMap variables, String type, Identifier id) {
    // a new local: looked up in localMap only (a field of the same name doesn't prevent a shadowing
    // local, and a same-named local of another block gets its own slot)
    VariableInfo existing = variables.localMap.get(id.val.toString());
    if (existing == null
        || (type != null && existing.type != null && !type.equals(existing.type))) {
      variables.putLocal(
          id,
          new VariableInfo(
              id.val.toString(), type, emptyList(), false, (short) variables.localSize, null));
    }
    VariableInfo varInfo = variables.get(id);
    if (varInfo.isField) {
      if (varInfo.accFlags.contains(AccessFlag.ACC_STATIC)) {
        return ctx.storeGen.putStatic(out, varInfo.idx);
      } else {
        return ctx.storeGen.putField(out, varInfo.idx);
      }
    } else {
      return ctx.storeGen.store(out, varInfo);
    }
  }

  public HasOutput assignInArray(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, Identifier id, Expression val) {
    if (!ctx.classIdMap.variables.containsKey(id)) {
      classIdMap.variables.putLocal(
          id,
          new VariableInfo(
              id.val.toString(),
              REF,
              emptyList(),
              false,
              (short) classIdMap.variables.localSize,
              null));
    }
    VariableInfo varInfo = ctx.classIdMap.variables.get(id);
    String type = varInfo.arrType;
    ctx.loadGen.load(out, varInfo);
    for (Expression arrIdx : id.arrSel.exprs) {
      ctx.exprGen.eval(out, INT, arrIdx);
    }
    ExpressionResult value = ctx.exprGen.eval(out, type, val);
    if (value != null && TypeUtils.isPrimitive(type) && TypeUtils.unboxedType(value.type) != null) {
      // unboxing, e.g. "bytes[i] = map.get(k);" on a Map<String, Byte>
      ctx.basicGen.convert1(out, value.type, type);
    }
    ctx.storeGen.storeInArray(out, type);
    return out;
  }

  public HasOutput assignArray(
      DynamicByteArray out,
      GlobalIdentifierMap classIdMap,
      String type,
      int arrDim,
      Identifier id) {
    if (!ctx.classIdMap.variables.containsKey(id)) {
      classIdMap.variables.putLocal(
          id,
          new VariableInfo(
              id.val.toString(),
              REF,
              type,
              emptyList(),
              false,
              (short) classIdMap.variables.localSize,
              null));
    }
    return ctx.storeGen.store(out, ctx.classIdMap.variables.get(id));
  }

  private boolean replaceIds(List<Expression> expressions, Identifier id, int idx) {
    Identifier tmpId = new Identifier("tmp_" + idx, null);
    boolean retValue = false;
    for (Expression expression : expressions) {
      retValue |= replaceId(expression, id, tmpId);
    }
    return retValue;
  }

  // (also in nested expressions, e.g. the argument of the call "√(x * b)")
  private static boolean replaceId(Expression expression, Identifier id, Identifier tmpId) {
    if (expression == null) {
      return false;
    }
    boolean replaced = false;
    for (int i = 0; i < expression.tokens.size(); i++) {
      Token token = expression.tokens.get(i);
      if (token.eq(id)) {
        expression.tokens.set(i, tmpId);
        replaced = true;
      } else if (token instanceof TernaryExpression) {
        TernaryExpression ternary = (TernaryExpression) token;
        replaced |= replaceId(ternary.condition, id, tmpId);
        replaced |= replaceId(ternary.trueBranch, id, tmpId);
        replaced |= replaceId(ternary.falseBranch, id, tmpId);
      } else if (token instanceof Identifier) {
        for (Identifier link = (Identifier) token; link != null; link = link.next) {
          if (link instanceof CastMethodCall) {
            replaced |= replaceId(((CastMethodCall) link).innerExpr, id, tmpId);
          } else if (link instanceof MethodCall) {
            for (Expression param : ((MethodCall) link).params) {
              replaced |= replaceId(param, id, tmpId);
            }
          } else if (link instanceof ConstructorCall) {
            for (Expression arg : ((ConstructorCall) link).args) {
              replaced |= replaceId(arg, id, tmpId);
            }
          }
          if (link.arrSel != null) {
            for (Expression index : link.arrSel.exprs) {
              replaced |= replaceId(index, id, tmpId);
            }
          }
        }
      }
    }
    if (replaced) {
      expression.tree = null; // (now generated from the changed tokens)
    }
    return replaced;
  }

  private HasOutput handleChainedFieldAssignment(
      DynamicByteArray out, Method method, Identifier startId, Expression value) {
    Identifier id = startId;
    String currentType = null;

    // a static field of a class, e.g. "Counter.total = 1"
    FieldProcessor.StaticField staticField =
        (id.next.next == null && ctx.classIdMap.variables.get(id) == null)
            ? ctx.staticFieldMap.get(id.val + "." + id.next.val)
            : null;
    if (staticField != null) {
      ExpressionResult result = ctx.exprGen.eval(out, staticField.fieldType, value);
      if (result != null && !staticField.fieldType.equals(result.type)) {
        ctx.basicGen.convert1(out, result.type, staticField.fieldType);
      }
      String owner = staticField.className.replace('.', '/');
      String descr = TypeUtils.descr(staticField.fieldType);
      ctx.constPool.addFieldRef(owner, staticField.fieldName, descr);
      ctx.storeGen.putStatic(
          out, ctx.constPool.indexOf(CONSTANT_FIELDREF, owner, staticField.fieldName, descr));
      ctx.opStack.pop();
      return out;
    }
    // Load all intermediate identifiers, tracking the current object type
    while (id.next != null) {
      if (currentType == null) {
        // First identifier: look up in our identifier maps and load it
        VariableInfo varInfo = ctx.classIdMap.variables.get(id);
        if (varInfo == null) {
          ctx.errors.addError(
              LOG_NAME,
              String.format("cannot find variable %s", id.val),
              new RuntimeException().getStackTrace());
          return out;
        }
        ctx.loadGen.load(out, varInfo);
        currentType = varInfo.type;
      } else {
        // Subsequent intermediate: object already on stack, getfield via FieldProcessor
        Map<String, FieldProcessor.InstanceField> fields =
            new FieldProcessor().getInstanceFieldMap(currentType, ctx.allClazzes);
        FieldProcessor.InstanceField instanceField = fields.get(id.val.toString());
        if (instanceField == null) {
          ctx.errors.addError(
              LOG_NAME,
              String.format("cannot find field %s in %s", id.val, currentType),
              new RuntimeException().getStackTrace());
          return out;
        }
        String className = instanceField.className.replace('.', '/');
        String fieldTypeDescr = TypeUtils.descr(instanceField.fieldType);
        ctx.constPool.addClass(className);
        ctx.constPool.addFieldRef(className, instanceField.fieldName, fieldTypeDescr);
        ctx.loadGen.getField(
            out,
            ctx.constPool.indexOf(
                CONSTANT_FIELDREF, className, instanceField.fieldName, fieldTypeDescr));
        ctx.opStack.pop();
        ctx.opStack.push(instanceField.fieldType);
        currentType = instanceField.fieldType;
      }
      if (id.arrSel != null) {
        // an element of an array field along the chain, e.g. the "b[i]" in "a.b[i].c = x"
        currentType = loadElements(out, id, currentType);
      }
      id = id.next;
    }

    // Last identifier: store into it using FieldProcessor to look up in the outer type
    if (currentType != null) {
      Map<String, FieldProcessor.InstanceField> fields =
          new FieldProcessor().getInstanceFieldMap(currentType, ctx.allClazzes);
      FieldProcessor.InstanceField instanceField = fields.get(id.val.toString());
      if (instanceField != null) {
        String className = instanceField.className.replace('.', '/');
        String fieldType = instanceField.fieldType;
        String fieldTypeDescr = TypeUtils.descr(fieldType);
        ctx.constPool.addClass(className);
        ctx.constPool.addFieldRef(className, instanceField.fieldName, fieldTypeDescr);
        if (id.arrSel != null) {
          // an element of an array field, e.g. "a.b[i] = x"
          ctx.loadGen.getField(
              out,
              ctx.constPool.indexOf(
                  CONSTANT_FIELDREF, className, instanceField.fieldName, fieldTypeDescr));
          ctx.opStack.pop();
          ctx.opStack.push(fieldType);
          List<Expression> indices = id.arrSel.exprs;
          String arrayType = fieldType;
          for (int i = 0; i < indices.size() - 1; i++) {
            ctx.exprGen.eval(out, INT, indices.get(i));
            out.write(AALOAD);
            ctx.opStack.pop(2);
            arrayType = arrayType.substring(1);
            ctx.opStack.push(arrayType);
          }
          String elemType = TypeUtils.jvmDescriptorToTypeName(arrayType.substring(1));
          ctx.exprGen.eval(out, INT, indices.get(indices.size() - 1));
          ExpressionResult result = ctx.exprGen.eval(out, elemType, value);
          ctx.basicGen.convert1(out, result.type, elemType);
          out.write(TypeUtils.getStoreOpCode(elemType));
          ctx.opStack.pop(3);
          return out;
        }
        ctx.exprGen.eval(out, fieldType, value);
        ctx.storeGen.putField(
            out,
            ctx.constPool.indexOf(
                CONSTANT_FIELDREF, className, instanceField.fieldName, fieldTypeDescr));
      } else {
        ctx.errors.addError(
            LOG_NAME,
            String.format("cannot find field %s in %s", id.val, currentType),
            new RuntimeException().getStackTrace());
      }
    }
    return out;
  }

  // Loads the selected element(s) of the array on top of the stack, returns the element type
  private String loadElements(DynamicByteArray out, Identifier id, String arrayType) {
    for (Expression index : id.arrSel.exprs) {
      ctx.exprGen.eval(out, INT, index);
      arrayType = arrayType.substring(1);
      out.write(TypeUtils.getLoadOpCode(TypeUtils.jvmDescriptorToTypeName(arrayType)));
      ctx.opStack.pop(2);
      ctx.opStack.push(TypeUtils.jvmDescriptorToTypeName(arrayType));
    }
    return TypeUtils.jvmDescriptorToTypeName(arrayType);
  }

  private HasOutput handleThisFieldAssignment(
      DynamicByteArray out, Method method, Identifier fieldName, Expression value) {
    // Load this reference
    ctx.loadGen.loadReference(out, (short) 0);

    // Evaluate the right-hand side expression
    String fieldType = null;

    // Find the field info to get the correct type (use fieldMap directly to avoid local shadowing)
    VariableInfo fieldInfo = ctx.classIdMap.variables.fieldMap.get(fieldName.val.toString());
    if (fieldInfo != null) {
      fieldType = fieldInfo.type;
    }

    ctx.exprGen.eval(out, fieldType, value);

    // Store the value in the field
    if (fieldInfo != null && fieldInfo.isField) {
      ctx.storeGen.putField(out, fieldInfo.idx);
    } else {
      ctx.errors.addError(
          "assignment generator",
          "Field not found: " + fieldName.val.toString(),
          new RuntimeException().getStackTrace());
    }

    return out;
  }
}
