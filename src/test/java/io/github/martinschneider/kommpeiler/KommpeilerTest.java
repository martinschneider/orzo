package io.github.martinschneider.kommpeiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.codegen.Kommpeiler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class KommpeilerTest {

  private static Stream<Arguments> testKommpeiler() {
    return Stream.of(Arguments.of("HelloWorld"), Arguments.of("IntegerConstants"));
  }

  /**
   * verify that demo programs generate the same output (stdout when executed) when compared with
   * Kommpeiler and javac (this requires javac on the PATH)
   */
  @ParameterizedTest
  @MethodSource
  public void testKommpeiler(String programName) throws IOException, InterruptedException {
    String tmpDir = System.getProperty("java.io.tmpdir");
    String inputPath = this.getClass().getResource(programName + ".code").getPath();
    String outputPath = tmpDir + programName + ".class";
    Files.deleteIfExists(Paths.get(outputPath));
    Files.deleteIfExists(Paths.get(tmpDir + programName + ".java"));

    // compile using Kommpeiler and run using java
    Kommpeiler.main(new String[] {inputPath, outputPath});
    Runtime rt = Runtime.getRuntime();
    Process proc = rt.exec(new String[] {"java", "-cp", tmpDir, programName});
    proc.waitFor();
    String actual =
        new BufferedReader(new InputStreamReader(proc.getInputStream()))
            .lines()
            .collect(Collectors.joining("\n"));
    Files.deleteIfExists(Paths.get(outputPath));

    // compile using javac and run using java
    rt.exec(new String[] {"cp", inputPath, tmpDir + programName + ".java"}).waitFor();
    rt.exec(new String[] {"javac", tmpDir + programName + ".java"}).waitFor();
    proc = rt.exec(new String[] {"java", "-cp", tmpDir, programName});
    proc.waitFor();
    String expected =
        new BufferedReader(new InputStreamReader(proc.getInputStream()))
            .lines()
            .collect(Collectors.joining("\n"));

    // compare stdout for both executions
    assertEquals(expected, actual);
  }
}
