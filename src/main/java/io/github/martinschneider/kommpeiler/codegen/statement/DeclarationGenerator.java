package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.CodeGenerator.INTEGER_DEFAULT_VALUE;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.OpsCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class DeclarationGenerator implements StatementGenerator {
  private ExpressionCodeGenerator expressionCodeGenerator;
  private OpsCodeGenerator opsCodeGenerator;
  private ConstantPool constantPool;

  public DeclarationGenerator(
      ExpressionCodeGenerator expressionCodeGenerator,
      OpsCodeGenerator opsCodeGenerator,
      ConstantPool constantPool) {
    this.expressionCodeGenerator = expressionCodeGenerator;
    this.opsCodeGenerator = opsCodeGenerator;
    this.constantPool = constantPool;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement statement,
      Method method,
      Clazz clazz) {
    Declaration decl = (Declaration) statement;
    if (decl.hasValue()) {
      expressionCodeGenerator.evaluateExpression(out, variables, decl.getValue());
    } else {
      if (decl.getType().eq(type(INT))) {
        opsCodeGenerator.pushInteger(out, constantPool, INTEGER_DEFAULT_VALUE);
      }
    }
    opsCodeGenerator.assignValue(out, variables, decl.getType(), decl.getName());
    return out;
  }
}
