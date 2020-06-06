package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.lexer.tokens.Token.id;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableInfo;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelAssignment;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.List;

public class ParallelAssignmentGenerator implements StatementGenerator {
  private CGContext ctx;

  public ParallelAssignmentGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  // TODO: support array types
  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    ParallelAssignment assignment = (ParallelAssignment) stmt;
    for (int i = 0; i < assignment.left.size(); i++) {
      Identifier left = assignment.left.get(i);
      Expression right = assignment.right.get(i);
      VariableInfo varInfo = variables.get(left);
      String type = varInfo.type;
      byte leftIdx = varInfo.idx;
      ctx.exprGenerator.eval(out, variables, type, right);
      if (replaceIds(assignment.right, left, variables.tmpCount)) {
        Identifier id = id("tmp_" + variables.tmpCount);
        if (!variables.getVariables().containsKey(id.val.toString())) {
          variables.put(id, new VariableInfo(id.val.toString(), type, (byte) variables.size));
        }
        byte tmpIdx = variables.get(id).idx;
        ctx.opsGenerator.loadValue(out, type, leftIdx);
        ctx.opsGenerator.storeValue(out, type, tmpIdx);
        variables.tmpCount++;
      }
      ctx.opsGenerator.storeValue(out, type, leftIdx);
    }
    return out;
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
