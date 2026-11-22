package io.github.martinschneider.orzo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.martinschneider.orzo.codegen.Output;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Regression test for a chained method call on an erased generic return (e.g. {@code
 * map.get(k).method()}) no longer silently matching an unrelated same-named method on the enclosing
 * class. Compiling must now fail loudly instead of producing a class that VerifyErrors or runs the
 * wrong method at runtime.
 */
public class GenericChainMethodResolutionTest {
  @Test
  public void chainedCallOnErasedGenericReturnFailsLoudlyInsteadOfMatchingEnclosingMethod()
      throws IOException {
    File input =
        new File(
            this.getClass()
                .getResource("tests" + File.separator + "GenericChainErrorRepro.java")
                .getPath());
    Output output = new Output(new PrintStream(new ByteArrayOutputStream()));
    PrintStream originalErr = System.err;
    ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
    System.setErr(new PrintStream(capturedErr));
    try {
      Orzo orzo = new Orzo(List.of(input), null, 0);
      orzo.compile(new java.util.ArrayList<>(List.of(output)));
    } finally {
      System.setErr(originalErr);
    }
    String stderr = capturedErr.toString();
    assertTrue(
        stderr.contains("error: cannot find method length()"),
        "expected a compile error naming the unresolved chained method, got: " + stderr);
  }
}
