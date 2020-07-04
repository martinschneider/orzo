package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableInfo;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.List;

public class AssignmentGenerator implements StatementGenerator {
  private CGContext ctx;

  public AssignmentGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    Assignment assignment = (Assignment) stmt;
    for (int i = 0; i < assignment.left.size(); i++) {
      Identifier left = assignment.left.get(i);
      Expression right = assignment.right.get(i);
      VariableInfo varInfo = variables.get(left);
      String type = varInfo.type;
      byte leftIdx = varInfo.idx;
      // if the current left side variable appears anywhere on the right side
      // we store its value in a tmp variable
      if (i < assignment.left.size() - 1
          && replaceIds(assignment.right, left, variables.tmpCount)) {
        if (left.arrSel != null) {
          type = varInfo.arrType;
        }
        Identifier id = id("tmp_" + variables.tmpCount);
        variables.put(id, new VariableInfo(id.val.toString(), type, (byte) variables.size));
        byte tmpIdx = variables.get(id).idx;
        if (left.arrSel == null) {
          ctx.loadGen.load(out, type, leftIdx);
        } else {
          ctx.loadGen.loadValueFromArray(
              out, variables, left.arrSel.exprs, varInfo.arrType, leftIdx);
        }
        ctx.storeGen.storeValue(out, type, tmpIdx);
        variables.tmpCount++;
      }
      if (left.arrSel == null) {
        ctx.exprGen.eval(out, variables, type, right);
        ctx.storeGen.storeValue(out, type, leftIdx);
      } else {
        assignInArray(out, variables, left, right);
      }
    }
    return out;
  }

  public HasOutput assign(DynamicByteArray out, VariableMap variables, String type, Identifier id) {
    if (!variables.containsKey(id)) {
      variables.put(id, new VariableInfo(id.val.toString(), type, (byte) variables.size));
    }
    return ctx.storeGen.storeValue(out, type, variables.get(id).idx);
  }

  public HasOutput assignInArray(
      DynamicByteArray out, VariableMap variables, Identifier id, Expression val) {
    if (!variables.containsKey(id)) {
      variables.put(id, new VariableInfo(id.val.toString(), REF, (byte) variables.size));
    }
    VariableInfo varInfo = variables.get(id);
    byte idx = varInfo.idx;
    String type = varInfo.arrType;
    ctx.loadGen.load(out, REF, idx);
    for (Expression arrIdx : id.arrSel.exprs) {
      ctx.exprGen.eval(out, variables, INT, arrIdx);
    }
    ctx.exprGen.eval(out, variables, type, val);
    ctx.storeGen.storeInArray(out, type);
    return out;
  }

  public HasOutput assignArray(
      DynamicByteArray out, VariableMap variables, String type, int arrDim, Identifier id) {
    if (!variables.containsKey(id)) {
      variables.put(id, new VariableInfo(id.val.toString(), REF, type, (byte) variables.size));
    }
    byte idx = variables.get(id).idx;
    return ctx.storeGen.storeReference(out, idx);
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