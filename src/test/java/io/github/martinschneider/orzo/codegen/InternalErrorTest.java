package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.util.FactoryHelper.clazz;
import static io.github.martinschneider.orzo.util.FactoryHelper.defaultConstr;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class InternalErrorTest {
  // a statement no generator exists for (standing in for any bug in the code generator)
  private static class UnknownStatement implements Statement {}

  @Test
  public void anExceptionInTheCodeGeneratorIsReportedAsAnError() {
    Method broken =
        new Method(
            "Broken",
            List.of(AccessFlag.ACC_STATIC),
            "void",
            new Identifier("m"),
            new ArrayList<>(),
            new ArrayList<>(List.of(new UnknownStatement())));
    Clazz brokenClazz =
        clazz("", list(), null, "Broken", list(), Clazz.JAVA_LANG_OBJECT, list(broken), list());
    Clazz okClazz =
        clazz(
            "",
            list(),
            null,
            "Ok",
            list(),
            Clazz.JAVA_LANG_OBJECT,
            list(defaultConstr("Ok")),
            list());
    brokenClazz.sourceFile = "Broken.java";
    okClazz.sourceFile = "Ok.java";
    CompilerErrors errors = new CompilerErrors();
    List<Output> outputs =
        list(
            new Output(new PrintStream(new ByteArrayOutputStream())),
            new Output(new PrintStream(new ByteArrayOutputStream())));
    new CodeGenerator(new ArrayList<>(List.of(brokenClazz, okClazz)), outputs, errors).generate();
    assertEquals(1, errors.errors.size(), errors.toString());
    assertTrue(
        errors.errors.get(0).toString().startsWith("Broken.java: error: internal compiler error"),
        errors.toString());
    // the next class is still generated
    assertTrue(outputs.get(1).getBytes().length > 0);
  }
}
