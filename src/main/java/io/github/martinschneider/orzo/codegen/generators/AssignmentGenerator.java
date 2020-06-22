package io.github.martinschneider.orzo.codegen.generators;

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

public class AssignmentGenerator implements StatementGenerator {
  private CGContext ctx;

  public AssignmentGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    Assignment assignment = (Assignment) stmt;
    Identifier id = assignment.left;
    if (id.arrSel != null) {
      assignInArray(out, variables, id, assignment.right);
    } else {
      String type = variables.get(id).type;
      ctx.exprGen.eval(out, variables, type, assignment.right);
      assign(out, variables, type, id);
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
}
