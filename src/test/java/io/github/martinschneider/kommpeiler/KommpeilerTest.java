package io.github.martinschneider.kommpeiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.kommpeiler.codegen.Kommpeiler;
import io.github.martinschneider.kommpeiler.codegen.Output;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class KommpeilerTest {
  private class ByteClassLoader extends ClassLoader {
    private HashMap<String, byte[]> byteDataMap = new HashMap<>();

    public ByteClassLoader(ClassLoader parent) {
      super(parent);
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
      byte[] extractedBytes = byteDataMap.get(className);
      return defineClass(className, extractedBytes, 0, extractedBytes.length);
    }

    public void put(String className, byte[] byteData) {
      byteDataMap.put(className, byteData);
    }
  }

  private static Stream<Arguments> testKommpeiler() {
    return Stream.of(
        Arguments.of("K001_HelloWorld"),
        Arguments.of("K002_IntegerConstants"),
        Arguments.of("K003_VariableAssignments"),
        Arguments.of("K004_IntegerExpressions"),
        Arguments.of("K005_IfConditions"),
        Arguments.of("K006_WhileLoop"),
        Arguments.of("K007_ForLoop"),
        Arguments.of("K008_DoLoop"),
        Arguments.of("K009_Fibonacci"),
        Arguments.of("K010_NestedLoops"));
  }

  @ParameterizedTest
  @MethodSource
  public void testKommpeiler(String programName)
      throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException {
    String inputPath =
        this.getClass().getResource("examples" + File.separator + programName + ".java").getPath();
    // compile using Kommpeiler
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    new Kommpeiler(new File(inputPath), new Output(ps), null).compile();
    ps.flush();
    ByteClassLoader classLoader = new ByteClassLoader(ClassLoader.getSystemClassLoader());
    classLoader.put(programName, baos.toByteArray());
    Class<?> clazz = classLoader.loadClass(programName);
    baos = new ByteArrayOutputStream();
    ps = new PrintStream(baos);
    PrintStream old = System.out;
    System.setOut(ps);
    clazz.getMethod("main", String[].class).invoke(null, (Object) null);
    System.out.flush();
    System.setOut(old);
    String actual = baos.toString();
    String expected =
        Files.readString(
            Path.of(
                this.getClass()
                    .getResource("examples" + File.separator + programName + ".output")
                    .getPath()));
    assertEquals(expected, actual);
  }
}
