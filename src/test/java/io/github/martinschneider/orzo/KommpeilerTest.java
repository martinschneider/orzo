package io.github.martinschneider.orzo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.codegen.Kommpeiler;
import io.github.martinschneider.orzo.codegen.Output;
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
        Arguments.of("HelloWorld"),
        Arguments.of("IntegerConstants"),
        Arguments.of("VariableAssignments"),
        Arguments.of("IntegerExpressions"),
        Arguments.of("IfConditions"),
        Arguments.of("IfElseConditions"),
        Arguments.of("WhileLoops"),
        Arguments.of("ForLoops"),
        Arguments.of("DoLoops"),
        Arguments.of("Fibonacci"),
        Arguments.of("NestedLoops"),
        Arguments.of("MethodCalls"),
        Arguments.of("Factorial"),
        Arguments.of("CollatzConjecture"),
        Arguments.of("ParallelAssignments"),
        Arguments.of("BreakLoops"),
        Arguments.of("ByteAndShorts"),
        Arguments.of("Longs"),
        Arguments.of("CollatzConjecture2"),
        Arguments.of("Doubles"),
        Arguments.of("Floats"),
        Arguments.of("PiLeibniz"),
        Arguments.of("BitShifts"),
        Arguments.of("UnsignedRightShift"),
        Arguments.of("CompoundAssignments"),
        Arguments.of("BitOperators"),
        Arguments.of("RussianPeasant"),
        Arguments.of("IntArrays"),
        Arguments.of("DoubleArrays"),
        Arguments.of("ByteArrays"),
        Arguments.of("ShortArrays"),
        Arguments.of("LongArrays"),
        Arguments.of("FloatArrays"),
        Arguments.of("QuickSort"));
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
                    .getResource(
                        "examples"
                            + File.separator
                            + "output"
                            + File.separator
                            + programName
                            + ".output")
                    .getPath()));
    assertEquals(expected, actual);
  }
}
