package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GOTO;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.IfBlock;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class IfStatementGenerator implements StatementGenerator {
  private CGContext context;

  public IfStatementGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    IfStatement ifStmt = (IfStatement) stmt;
    List<DynamicByteArray> bodyOutputs = new ArrayList<>();
    List<DynamicByteArray> condOutputs = new ArrayList<>();
    for (int i = 0; i < ifStmt.getIfBlocks().size(); i++) {
      IfBlock ifBlock = ifStmt.getIfBlocks().get(i);
      DynamicByteArray bodyOut = new DynamicByteArray();
      for (Statement innerStmt : ifBlock.getBody()) {
        context.delegator.generate(variables, bodyOut, method, innerStmt);
      }
      DynamicByteArray conditionOut = new DynamicByteArray();
      if (ifBlock.getCondition() != null) { // null for else blocks
        short branchBytes = (short) (3 + bodyOut.getBytes().length);
        if (i != ifStmt.getIfBlocks().size() - 1) {
          branchBytes += 3;
        }
        context.condGenerator.generateCondition(
            conditionOut, variables, ifBlock.getCondition(), branchBytes);
      }
      bodyOutputs.add(bodyOut);
      condOutputs.add(conditionOut);
    }
    int blocks = bodyOutputs.size();
    short offset = (short) (3 + bodyOutputs.get(blocks - 1).size());
    if (!ifStmt.isHasElseBlock()) {
      offset += condOutputs.get(blocks - 1).size();
    }
    // if there's no "else" then the last "else if" can fall through (doesn't require a goto)
    for (int i = blocks - 2; i >= 0; i--) {
      bodyOutputs.get(i).write(GOTO);
      bodyOutputs.get(i).write(shortToByteArray(offset));
      offset += condOutputs.get(i).size() + bodyOutputs.get(i).size();
    }
    for (int i = 0; i < bodyOutputs.size(); i++) {
      if (condOutputs.get(i) != null) {
        out.write(condOutputs.get(i).getBytes());
      }
      out.write(bodyOutputs.get(i).getBytes());
    }
    return out;
  }
}
