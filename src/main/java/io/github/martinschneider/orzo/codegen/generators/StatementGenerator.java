package io.github.martinschneider.orzo.codegen.generators;

import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;

public interface StatementGenerator {
  HasOutput generate(DynamicByteArray out, VariableMap variables, Method method, Statement stmt);
}
