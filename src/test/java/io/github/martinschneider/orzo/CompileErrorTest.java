package io.github.martinschneider.orzo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Programs that must not compile: each reports the expected error (with its position) and no class
 * files are written. Most of these used to compile to wrong code without any error.
 */
public class CompileErrorTest {
  @TempDir Path dir;

  // the statement is on line 5, starting at column 5
  private static final String TEMPLATE =
      "public class N {\n"
          + "  static int f(int a) { return a; }\n"
          + "  public static void main(String[] args) {\n"
          + "    int x = 1;\n"
          + "    %s\n"
          + "  }\n"
          + "}\n";

  private static Stream<Arguments> statementErrors() {
    return Stream.of(
        Arguments.of("int y = ;", "N.java:5:13: error: expected an expression but found ';'"),
        Arguments.of("int z = 1 +;", "N.java:5:16: error: expected an expression but found ';'"),
        Arguments.of("x = ;", "N.java:5:9: error: expected an expression but found ';'"),
        Arguments.of("x = (1 + 2;", "N.java:5:15: error: expected ')' but found ';'"),
        Arguments.of("x = #;", "N.java:5:9: error: unexpected character '#'"),
        Arguments.of("x + 1;", "N.java:5:5: error: expected a statement but found 'x'"),
        Arguments.of("int[] a = new int[];", "N.java:5:24: error: array dimension missing"),
        Arguments.of("y = x + 1;", "N.java:5:5: error: cannot find variable y"),
        Arguments.of("f(1, 2);", "N.java:5:5: error: cannot find method f(int, int)"),
        Arguments.of("Object o = new Unknown();", "N.java:5:5: error: cannot find class Unknown"),
        Arguments.of("Foo g = null;", "N.java:5:5: error: cannot find class Foo"),
        Arguments.of(
            "int s = \"text\";",
            "N.java:5:5: error: incompatible types: String cannot be converted to int"),
        Arguments.of(
            "String t = 1;",
            "N.java:5:5: error: incompatible types: int cannot be converted to String"),
        Arguments.of("return 5;", "N.java:5:5: error: unexpected return value"),
        Arguments.of(
            "int[][] m = new int[2][3];",
            "N.java:5:5: error: multi-dimensional arrays are not supported"),
        Arguments.of(
            "String q = \"\"\"\n  a\n  \"\"\";",
            "N.java:5:16: error: text blocks are not supported"),
        Arguments.of("int x = 2;", "N.java:5:5: error: variable x is already defined"),
        Arguments.of(
            "for (int i = 0; i < 2; i++) { int i = 1; }",
            "N.java:5:35: error: variable i is already defined"),
        Arguments.of(
            "if (x > 0) { int x = 3; }", "N.java:5:18: error: variable x is already defined"),
        Arguments.of(
            "try { x++; } catch (Exception args) { }",
            "N.java:5:5: error: variable args is already defined"),
        Arguments.of("break;", "N.java:5:5: error: break outside of a loop or switch"),
        Arguments.of("continue;", "N.java:5:5: error: continue outside of a loop"),
        Arguments.of(
            "while (x < 3) { break outer; }", "N.java:5:21: error: undefined label: outer"));
  }

  @ParameterizedTest
  @MethodSource
  public void statementErrors(String statement, String expectedError) throws IOException {
    List<String> errors = compile(Map.of("N.java", String.format(TEMPLATE, statement)));
    assertTrue(errors.contains(expectedError), errors.toString());
  }

  @Test
  public void unknownFieldType() throws IOException {
    List<String> errors = compile(Map.of("F.java", "public class F {\n  Bar bar;\n}\n"));
    assertEquals(List.of("F.java:2:3: error: cannot find class Bar"), errors);
  }

  @Test
  public void missingSemicolons() throws IOException {
    // (reported right after the statement, like javac does)
    List<String> errors =
        compile(
            Map.of(
                "S.java",
                "public class S {\n"
                    + "  int field = 1\n"
                    + "  public static void main(String[] args) {\n"
                    + "    int a = 1;\n"
                    + "    a = 2;\n"
                    + "    a++;\n"
                    + "    System.out.println(a)\n"
                    + "    while (a < 3) {\n"
                    + "      if (a == 2) continue\n"
                    + "      break\n"
                    + "    }\n"
                    + "    do { a++; } while (a < 5)\n"
                    + "    if (a > 0) {\n"
                    + "      return\n"
                    + "    }\n"
                    + "    throw new RuntimeException()\n"
                    + "  }\n"
                    + "}\n"));
    assertEquals(
        List.of(
            "S.java:2:16: error: expected ';'",
            "S.java:7:26: error: expected ';'",
            "S.java:9:27: error: expected ';'",
            "S.java:10:12: error: expected ';'",
            "S.java:12:30: error: expected ';'",
            "S.java:14:13: error: expected ';'",
            "S.java:16:33: error: expected ';'"),
        errors);
  }

  @Test
  public void missingSemicolonsAfterExpressions() throws IOException {
    // (the expression parser used to take "1 a = 2 a++" for one expression)
    List<String> errors =
        compile(
            Map.of(
                "S.java",
                "public class S {\n"
                    + "  void m() {\n"
                    + "    int a = 1\n"
                    + "    a = 2\n"
                    + "    a++\n"
                    + "  }\n"
                    + "}\n"));
    assertEquals(
        List.of(
            "S.java:3:14: error: expected ';'",
            "S.java:4:10: error: expected ';'",
            "S.java:5:8: error: expected ';'"),
        errors);
  }

  @Test
  public void missingReturnStatement() throws IOException {
    List<String> errors =
        compile(Map.of("N.java", "public class N {\n  int f() {\n    int x = 1;\n  }\n}\n"));
    assertEquals(List.of("N.java:2:3: error: missing return statement"), errors);
  }

  @Test
  public void independentErrorsAreReported() throws IOException {
    // (the parser continues after a statement with an error)
    List<String> errors =
        compile(
            Map.of(
                "M.java",
                "public class M {\n"
                    + "  void m(int x) {\n"
                    + "    int a = ;\n"
                    + "    x + 1;\n"
                    + "    while (true {\n"
                    + "      x++;\n"
                    + "    }\n"
                    + "    int b = 1 +;\n"
                    + "  }\n"
                    + "  void n() {\n"
                    + "    foo(;\n"
                    + "  }\n"
                    + "}\n"));
    assertEquals(
        List.of(
            "M.java:3:13: error: expected an expression but found ';'",
            "M.java:4:5: error: expected a statement but found 'x'",
            "M.java:5:17: error: expected ')' but found '{'",
            "M.java:8:16: error: expected an expression but found ';'",
            "M.java:11:9: error: expected ')' but found ';'"),
        errors);
  }

  @Test
  public void independentMemberErrorsAreReported() throws IOException {
    List<String> errors =
        compile(
            Map.of(
                "C.java",
                "public class C {\n"
                    + "  int a = ;\n"
                    + "  + 3;\n"
                    + "  int f() {\n"
                    + "    return 1;\n"
                    + "  }\n"
                    + "  ) junk {\n"
                    + "  }\n"
                    + "  void n() {\n"
                    + "    x + 1;\n"
                    + "  }\n"
                    + "}\n"));
    assertEquals(
        List.of(
            "C.java:2:11: error: expected an expression but found ';'",
            "C.java:3:3: error: expected a field, method or class declaration but found '+'",
            "C.java:7:3: error: expected a field, method or class declaration but found ')'",
            "C.java:10:5: error: expected a statement but found 'x'"),
        errors);
  }

  @Test
  public void unreadableFileIsAnError() throws IOException {
    Orzo orzo =
        new Orzo(List.of(dir.resolve("Missing.java").toFile()), dir.resolve("out").toString(), 0);
    PrintStream stderr = System.err;
    System.setErr(new PrintStream(new ByteArrayOutputStream()));
    try {
      assertFalse(orzo.compile());
    } finally {
      System.setErr(stderr);
    }
    assertTrue(
        orzo.errors.errors.get(0).toString().startsWith("Missing.java: error: cannot read file"),
        orzo.errors.toString());
  }

  @Test
  public void errorsOfAllFilesAreReported() throws IOException {
    // (only the errors of the last file used to be reported)
    List<String> errors =
        compile(
            Map.of(
                "A.java",
                "public class A {\n  void m() {\n    while (true {\n    }\n  }\n}\n",
                "B.java",
                "public class B {\n  int x = ;\n}\n"));
    assertEquals(
        List.of(
            "A.java:3:17: error: expected ')' but found '{'",
            "B.java:2:11: error: expected an expression but found ';'"),
        errors);
  }

  // Compiles the given files (name -> source, in name order), asserts that it fails without
  // writing class files and returns the errors
  private List<String> compile(Map<String, String> sources) throws IOException {
    List<File> inputs = new java.util.ArrayList<>();
    for (String name : new java.util.TreeSet<>(sources.keySet())) {
      Path file = dir.resolve(name);
      Files.writeString(file, sources.get(name));
      inputs.add(file.toFile());
    }
    Path out = dir.resolve("out");
    Orzo orzo = new Orzo(inputs, out.toString(), 0);
    PrintStream stderr = System.err;
    System.setErr(new PrintStream(new ByteArrayOutputStream()));
    boolean success;
    try {
      success = orzo.compile();
    } finally {
      System.setErr(stderr);
    }
    assertFalse(success, "expected compile errors");
    assertFalse(Files.exists(out), "no class files must be written");
    return orzo.errors.errors.stream().map(e -> e.toString()).collect(Collectors.toList());
  }
}
