package io.github.martinschneider.orzo.codegen.generators;

import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;

public class DoNothingGenerator implements StatementGenerator<Statement> {
  @Override
  public HasOutput generate(DynamicByteArray out, Method method, Statement stmt) {
    return out;
  }
}
