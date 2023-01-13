package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.util.Factory.list;
import static io.github.martinschneider.orzo.util.Factory.stream;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.generators.RetGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.RetParser;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;

@TestInstance(Lifecycle.PER_CLASS)
public class RetGeneratorTest extends StatementGeneratorTest<ReturnStatement> {
  // TODO: add more interesting tests
  private static Stream<Arguments> test() throws IOException {
    return stream(args("return;", emptyList(), emptyList(), list("return")));
  }

  @BeforeAll
  public void init() {
    super.init();
    target = new RetGenerator(ctx);
    parser = new RetParser(ParserContext.build(new CompilerErrors()));
  }
}
