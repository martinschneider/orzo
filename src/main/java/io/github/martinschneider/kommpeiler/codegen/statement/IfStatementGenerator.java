package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GOTO;

import io.github.martinschneider.kommpeiler.codegen.ConditionalCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.IfBlock;
import io.github.martinschneider.kommpeiler.parser.productions.IfStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IfStatementGenerator implements StatementGenerator {
  private StatementGeneratorDelegator statementGeneratorDelegator;
  private ConditionalCodeGenerator conditionalCodeGenerator;
  private ConstantPool constantPool;

  public IfStatementGenerator(
      StatementGeneratorDelegator statementGeneratorDelegator,
      ConditionalCodeGenerator conditionalCodeGenerator,
      ConstantPool constantPool) {
    this.statementGeneratorDelegator = statementGeneratorDelegator;
    this.conditionalCodeGenerator = conditionalCodeGenerator;
    this.constantPool = constantPool;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement statement,
      Method method,
      Clazz clazz) {
    IfStatement ifStatement = (IfStatement) statement;
    List<DynamicByteArray> bodyOutputs = new ArrayList<>();
    List<DynamicByteArray> conditionOutputs = new ArrayList<>();
    for (int i = 0; i < ifStatement.getIfBlocks().size(); i++) {
      IfBlock ifBlock = ifStatement.getIfBlocks().get(i);
      DynamicByteArray bodyOut = new DynamicByteArray();
      for (Statement stmt : ifBlock.getBody()) {
        statementGeneratorDelegator.generate(variables, bodyOut, stmt, method, clazz);
      }
      DynamicByteArray conditionOut = new DynamicByteArray();
      if (ifBlock.getCondition() != null) { // null for else blocks
        short branchBytes = (short) (3 + bodyOut.getBytes().length);
        if (i != ifStatement.getIfBlocks().size() - 1) {
          branchBytes += 3;
        }
        conditionalCodeGenerator.generateCondition(
            conditionOut, clazz, variables, constantPool, ifBlock.getCondition(), branchBytes);
      }
      bodyOutputs.add(bodyOut);
      conditionOutputs.add(conditionOut);
    }
    int blocks = bodyOutputs.size();
    short offset = (short) (3 + bodyOutputs.get(blocks - 1).size());
    if (!ifStatement.isHasElseBlock()) {
      offset += conditionOutputs.get(blocks - 1).size();
    }
    // if there's no "else" then the last "else if" can fall through (doesn't require a goto)
    for (int i = blocks - 2; i >= 0; i--) {
      bodyOutputs.get(i).write(GOTO);
      bodyOutputs.get(i).write(shortToByteArray(offset));
      offset += conditionOutputs.get(i).size() + bodyOutputs.get(i).size();
    }
    for (int i = 0; i < bodyOutputs.size(); i++) {
      if (conditionOutputs.get(i) != null) {
        out.write(conditionOutputs.get(i).getBytes());
      }
      out.write(bodyOutputs.get(i).getBytes());
    }
    return out;
  }
}
