package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.CodeGenerator.INTEGER_DEFAULT_VALUE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BYTE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.SHORT;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;

public class DeclarationGenerator implements StatementGenerator {
  public DeclarationGenerator(CGContext context) {
    this.context = context;
  }

  private CGContext context;

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    Declaration decl = (Declaration) stmt;
    if (decl.hasValue()) {
      context.exprGenerator.eval(out, variables, decl.getType(), decl.getValue());
    } else {
      if (decl.getType().equals(INT)) {
        context.opsGenerator.pushInteger(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.getType().equals(SHORT)) {
        context.opsGenerator.sipush(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.getType().equals(BYTE)) {
        context.opsGenerator.bipush(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.getType().equals(LONG)) {
        context.opsGenerator.pushLong(out, INTEGER_DEFAULT_VALUE);
      }
    }
    context.opsGenerator.assignValue(out, variables, decl.getType(), decl.getName());
    return out;
  }
}
