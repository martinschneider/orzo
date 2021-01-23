package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static io.github.martinschneider.orzo.parser.TestHelper.varInfo;

import io.github.martinschneider.orzo.codegen.generators.WhileGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.WhileParser;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;

@TestInstance(Lifecycle.PER_CLASS)
public class WhileGeneratorTest extends StatementGeneratorTest<WhileStatement> {

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args(
            "while (x>0) { x--; }",
            list(varInfo("x", "int", 2)),
            list("iload_2", "ifle 9", "iinc 2 -1", "goto -7")));
  }

  @BeforeAll
  public void init() {
    super.init();
    target = new WhileGenerator(ctx);
    parser = new WhileParser(ParserContext.build(new CompilerErrors()));
  }
}
