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
  private CGContext context;

  public ParallelAssignmentGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    ParallelAssignment assignment = (ParallelAssignment) stmt;
    int tmpCount = 0;
    for (int i = 0; i < assignment.getLeft().size(); i++) {
      Identifier left = assignment.getLeft().get(i);
      Expression right = assignment.getRight().get(i);
      String type = variables.get(left).getType();
      byte leftIdx = variables.get(left).getIdx();
      context.exprGenerator.eval(out, variables, type, right);
      if (replaceIds(assignment.getRight(), left, tmpCount)) {
        replaceIds(assignment.getRight(), left, tmpCount);
        Identifier id = id("tmp_" + tmpCount);
        if (!variables.getVariables().containsKey(id)) {
          variables.put(
              id, new VariableInfo(id.getValue().toString(), type, (byte) variables.size()));
        }
        byte tmpIdx = variables.get(id).getIdx();
        context.opsGenerator.loadInteger(out, leftIdx);
        context.opsGenerator.storeInteger(out, tmpIdx);
        tmpCount++;
      }
      context.opsGenerator.storeInteger(out, leftIdx);
    }
    return out;
  }

  private boolean replaceIds(List<Expression> expressions, Identifier id, int idx) {
    Identifier tmpId = new Identifier("tmp_" + idx, null);
    boolean retValue = false;
    for (Expression expression : expressions) {
      expression.getInfix().replaceAll(x -> (x.eq(id)) ? tmpId : x);
      if (expression.getInfix().contains(tmpId)) {
        retValue = true;
      }
    }
    return retValue;
  }
}
