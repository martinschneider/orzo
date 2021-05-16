package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;
import static io.github.martinschneider.orzo.util.FactoryHelper.stream;
import static io.github.martinschneider.orzo.util.FactoryHelper.varInfo;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.generators.DoGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.DoParser;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;

@TestInstance(Lifecycle.PER_CLASS)
public class DoGeneratorTest extends StatementGeneratorTest<DoStatement> {

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args(
            "do { x--; } while (x>0)",
            list(varInfo("x", "int", 2)),
            emptyList(),
            list("iinc 2 -1", "iload_2", "ifgt 252")),
        args(
            "do { x++; } while (x<0)",
            list(varInfo("x", "int", 2)),
            emptyList(),
            list("iinc 2 1", "iload_2", "iflt 252")),
        args(
            "do { x--; } while (x>5)",
            list(varInfo("x", "int", 2)),
            emptyList(),
            list("iinc 2 -1", "iload_2", "iconst_5", "if_icmpgt 251")),
        args(
            "do { x++; } while (x<=5)",
            list(varInfo("x", "int", 2)),
            emptyList(),
            list("iinc 2 1", "iload_2", "iconst_5", "if_icmple 251")));
  }

  @BeforeAll
  public void init() {
    super.init();
    target = new DoGenerator(ctx);
    parser = new DoParser(ParserContext.build(new CompilerErrors()));
  }
}
