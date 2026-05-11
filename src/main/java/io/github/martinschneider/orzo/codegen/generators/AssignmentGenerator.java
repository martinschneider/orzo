package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.FieldProcessor;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.IdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
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
          if (varInfo == null) {
            ctx.errors.addError(
                LOG_NAME,
                String.format("Unknown variable %s", id.val),
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
                ctx.loadGen.loadReference(out, varInfo.objectRef);
              }
              ctx.exprGen.eval(out, type, right);
              // Insert CHECKCAST when a generic/erased method returns a wider reference type
              // than the variable's declared type (e.g. Object → String via Deque<String>.pop()).
              String actualType = ctx.opStack.type();
              if (actualType != null
                  && !TypeUtils.isPrimitive(actualType)
                  && !TypeUtils.isPrimitive(type)) {
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
    if (!variables.containsKey(id)) {
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
    ctx.exprGen.eval(out, type, val);
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
      expression.tokens.replaceAll(x -> (x.eq(id)) ? tmpId : x);
      if (expression.tokens.contains(tmpId)) {
        retValue = true;
      }
    }
    return retValue;
  }

  private HasOutput handleChainedFieldAssignment(
      DynamicByteArray out, Method method, Identifier startId, Expression value) {
    Identifier id = startId;
    String currentType = null;

    // Load all intermediate identifiers, tracking the current object type
    while (id.next != null) {
      if (currentType == null) {
        // First identifier: look up in our identifier maps and load it
        VariableInfo varInfo = ctx.classIdMap.variables.get(id);
        if (varInfo == null) {
          ctx.errors.addError(
              LOG_NAME,
              String.format("Unknown variable in chained assignment: %s", id.val),
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
              String.format("Unknown field %s in type %s", id.val, currentType),
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
        ctx.exprGen.eval(out, fieldType, value);
        ctx.storeGen.putField(
            out,
            ctx.constPool.indexOf(
                CONSTANT_FIELDREF, className, instanceField.fieldName, fieldTypeDescr));
      } else {
        ctx.errors.addError(
            LOG_NAME,
            String.format("Unknown field %s in type %s", id.val, currentType),
            new RuntimeException().getStackTrace());
      }
    }
    return out;
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
