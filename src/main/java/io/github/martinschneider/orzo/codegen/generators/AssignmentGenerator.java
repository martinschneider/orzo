package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.IdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.List;

public class AssignmentGenerator implements StatementGenerator<Assignment> {
  private CGContext ctx;

  private static final String LOG_NAME = "assignment generator";

  public AssignmentGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, Assignment assignment) {
    for (int i = 0; i < assignment.left.size(); i++) {
      Identifier left = assignment.left.get(i);
      Expression right = assignment.right.get(i);
      Identifier id = left;
      while (id != null) {
        // TODO: currently, we do not support fields and local variables having the same name :-(
        // TODO: properly handle all classes
        if ("this".equals(id.val)) {
          ctx.loadGen.loadReference(out, (short) 0);
        } else {
          VariableInfo varInfo = ctx.classIdMap.variables.get(id);
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
              ctx.exprGen.eval(out, type, right);
              if (varInfo.isField) {
                ctx.storeGen.putField(out, varInfo.idx);
              } else {
                ctx.storeGen.store(out, varInfo);
              }
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
}
