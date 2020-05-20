package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.CodeGenerator.INTEGER_DEFAULT_VALUE;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class DeclarationGenerator implements StatementGenerator {
  private ExpressionGenerator exprGenerator;
  private OpsCodeGenerator opsGenerator;
  private ConstantPool constPool;

  public DeclarationGenerator(
      ExpressionGenerator exprGenerator, OpsCodeGenerator opsGenerator, ConstantPool constPool) {
    this.exprGenerator = exprGenerator;
    this.opsGenerator = opsGenerator;
    this.constPool = constPool;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement stmt,
      Method method,
      Clazz clazz) {
    Declaration decl = (Declaration) stmt;
    if (decl.hasValue()) {
      exprGenerator.eval(out, variables, decl.getValue());
    } else {
      if (decl.getType().eq(type(INT))) {
        opsGenerator.pushInteger(out, constPool, INTEGER_DEFAULT_VALUE);
      }
    }
    opsGenerator.assignValue(out, variables, decl.getType(), decl.getName());
    return out;
  }
}
