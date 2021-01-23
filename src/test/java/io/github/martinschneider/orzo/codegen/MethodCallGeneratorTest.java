package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;

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
    return stream(
        args("testMethod()", list(), list("invokestatic 5")),
        args("System.out.println(0)", list(), list("getstatic 10", "iconst_0", "invokevirtual 14")),
        args(
            "System.out.println(1)", list(), list("getstatic 10", "iconst_1", "invokevirtual 14")));
  }

  @BeforeAll
  public void init() {
    super.init();
    target = new MethodCallGenerator(ctx);
    parser = new MethodCallParser(ParserContext.build(new CompilerErrors()));
  }
}
