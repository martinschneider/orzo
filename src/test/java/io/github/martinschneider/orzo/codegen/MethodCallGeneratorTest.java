package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static io.github.martinschneider.orzo.parser.TestHelper.varInfo;

import io.github.martinschneider.orzo.codegen.generators.MethodCallGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.MethodCallParser;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;

@TestInstance(Lifecycle.PER_CLASS)
public class MethodCallGeneratorTest extends StatementGeneratorTest<MethodCall> {
  private static Stream<Arguments> test() throws IOException {
    // TODO: add more interesting tests
    return stream(args("testMethod()", list(varInfo("x", "int", 2)), list("invokestatic 5")));
  }

  @BeforeAll
  public void init() {
    super.init();
    target = new MethodCallGenerator(ctx);
    parser = new MethodCallParser(ParserContext.build(new CompilerErrors()));
  }
}
