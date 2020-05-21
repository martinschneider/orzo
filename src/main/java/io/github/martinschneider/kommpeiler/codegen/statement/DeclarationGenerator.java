package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.CodeGenerator.INTEGER_DEFAULT_VALUE;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.BYTE;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.SHORT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class DeclarationGenerator implements StatementGenerator {
  public DeclarationGenerator(CGContext context) {
    this.context = context;
  }

  private CGContext context;

  @Override
  public HasOutput generate(
      DynamicByteArray out,
      Map<Identifier, VariableInfo> variables,
      Method method,
      Statement stmt) {
    Declaration decl = (Declaration) stmt;
    if (decl.hasValue()) {
      context.exprGenerator.eval(out, variables, decl.getValue());
    } else {
      if (decl.getType().eq(type(INT))) {
        context.opsGenerator.pushInteger(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.getType().eq(type(SHORT))) {
        context.opsGenerator.sipush(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.getType().eq(type(BYTE))) {
        context.opsGenerator.bipush(out, INTEGER_DEFAULT_VALUE);
      }
    }
    context.opsGenerator.assignValue(
        out, variables, decl.getType().getValue().toString(), decl.getName());
    return out;
  }
}
