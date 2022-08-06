package io.github.martinschneider.orzo.codegen.generators;

import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;

public interface StatementGenerator<S extends Statement> {
  HasOutput generate(DynamicByteArray out, Method method, S stmt);
}
