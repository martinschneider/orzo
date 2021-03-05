package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static io.github.martinschneider.orzo.parser.TestHelper.varInfo;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.generators.ForGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.ForParser;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;

@TestInstance(Lifecycle.PER_CLASS)
public class ForGeneratorTest extends StatementGeneratorTest<ForStatement> {
  private static Stream<Arguments> test() throws IOException {
    return stream(
        args(
            "for (x=0; x<10; x++) { x++; }",
            list(varInfo("x", "int", 2)),
            emptyList(),
            list(
                "iconst_0",
                "istore_2",
                "iload_2",
                "bipush 10",
                "if_icmpge 12",
                "iinc 2 1",
                "iinc 2 1",
                "goto 244")),
        args(
            "for (x=0; x>10; x--) { x++; }",
            list(varInfo("x", "int", 2)),
            emptyList(),
            list(
                "iconst_0",
                "istore_2",
                "iload_2",
                "bipush 10",
                "if_icmple 12",
                "iinc 2 1",
                "iinc 2 -1",
                "goto 244")));
  }

  @BeforeEach
  public void init() {
    super.init();
    target = new ForGenerator(ctx);
    parser = new ForParser(ParserContext.build(new CompilerErrors()));
  }
}
