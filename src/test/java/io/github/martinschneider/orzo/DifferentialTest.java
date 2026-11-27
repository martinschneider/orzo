package io.github.martinschneider.orzo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Compiles each end-to-end test program with javac and checks that it prints the expected output,
 * the same output OrzoTest expects from the Orzo-compiled program. So the expected outputs are
 * those of Java itself (some used to encode Orzo's bugs). Programs that aren't valid Java (e.g.
 * using Orzo-only syntax) are skipped.
 */
public class DifferentialTest {
  @TempDir Path dir;

  static Stream<Arguments> tests() {
    return OrzoTest.tests();
  }

  @ParameterizedTest
  @MethodSource("tests")
  public void javacProducesTheExpectedOutput(List<String> programs) throws Exception {
    Path resources = Path.of(getClass().getResource("").toURI());
    List<String> sources = new ArrayList<>();
    for (String program : programs) {
      sources.add(resources.resolve(program + ".java").toString());
    }
    Path classes = dir.resolve("classes");
    Files.createDirectories(classes);
    List<String> args = new ArrayList<>(List.of("-d", classes.toString(), "-nowarn", "-proc:none"));
    args.addAll(sources);
    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    StringWriter diagnostics = new StringWriter();
    int status =
        javac
                .getTask(
                    diagnostics,
                    null,
                    null,
                    args.subList(0, 4),
                    null,
                    javac
                        .getStandardFileManager(null, null, null)
                        .getJavaFileObjectsFromStrings(sources))
                .call()
            ? 0
            : 1;
    assumeTrue(status == 0, () -> "not valid Java: " + programs.get(0));

    String[] mainProgram = programs.get(0).split("/");
    // (the package doesn't always match the directory)
    String mainClass = mainProgram[1];
    for (String line : Files.readAllLines(Path.of(sources.get(0)))) {
      if (line.startsWith("package ")) {
        mainClass = line.substring(8, line.indexOf(';')).trim() + "." + mainProgram[1];
        break;
      }
    }
    String expected =
        Files.readString(
            resources.resolve(mainProgram[0] + "/output/" + mainProgram[1] + ".output"));
    assertEquals(expected, run(classes, mainClass), programs.get(0));
  }

  private static String run(Path classes, String mainClass) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream stdout = System.out;
    System.setOut(new PrintStream(out));
    // the javac-compiled program first, then Orzo's own classes (some programs use them, e.g.
    // ObjectUtils), but not the test classpath (which may contain Orzo-compiled test programs)
    URL orzoClasses = Orzo.class.getProtectionDomain().getCodeSource().getLocation();
    try (URLClassLoader loader =
        new URLClassLoader(
            new URL[] {classes.toUri().toURL(), orzoClasses},
            ClassLoader.getPlatformClassLoader())) {
      loader.setDefaultAssertionStatus(true); // (like "java -ea")
      loader
          .loadClass(mainClass)
          .getMethod("main", String[].class)
          .invoke(null, (Object) new String[0]);
    } finally {
      System.out.flush();
      System.setOut(stdout);
    }
    return out.toString();
  }
}
