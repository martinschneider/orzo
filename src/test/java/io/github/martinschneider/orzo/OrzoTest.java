package io.github.martinschneider.orzo;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

public class OrzoTest {
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
    return Stream.of(Arguments.of("examples", "HelloWorld"),
        Arguments.of("tests", "IntegerConstants"), Arguments.of("tests", "VariableAssignments"),
        Arguments.of("tests", "IntegerExpressions"), Arguments.of("tests", "IfConditions"),
        Arguments.of("tests", "IfElseConditions"), Arguments.of("tests", "WhileLoops"),
        Arguments.of("tests", "ForLoops"), Arguments.of("tests", "DoLoops"),
        Arguments.of("examples", "Fibonacci"), Arguments.of("tests", "NestedLoops"),
        Arguments.of("tests", "MethodCalls"), Arguments.of("examples", "Factorial"),
        Arguments.of("examples", "CollatzConjecture"),
        Arguments.of("tests", "ParallelAssignmentsInteger"),
        Arguments.of("tests", "ParallelAssignmentsLong"),
        Arguments.of("tests", "ParallelAssignmentsShort"),
        Arguments.of("tests", "ParallelAssignmentsByte"),
        Arguments.of("tests", "ParallelAssignmentsFloat"),
        Arguments.of("tests", "ParallelAssignmentsDouble"),
        Arguments.of("tests", "ParallelAssignmentsArray"),
        Arguments.of("tests", "ParallelAssignmentsMultiple"), Arguments.of("tests", "BreakLoops"),
        Arguments.of("tests", "ByteAndShorts"), Arguments.of("tests", "Longs"),
        Arguments.of("examples", "CollatzConjecture2"), Arguments.of("tests", "Doubles"),
        Arguments.of("tests", "Floats"), Arguments.of("examples", "PiLeibniz"),
        Arguments.of("tests", "BitShifts"), Arguments.of("tests", "UnsignedRightShift"),
        Arguments.of("tests", "CompoundAssignments"), Arguments.of("tests", "BitOperators"),
        Arguments.of("examples", "RussianPeasant"), Arguments.of("tests", "IntArrays"),
        Arguments.of("tests", "DoubleArrays"), Arguments.of("tests", "ByteArrays"),
        Arguments.of("tests", "ShortArrays"), Arguments.of("tests", "LongArrays"),
        Arguments.of("tests", "FloatArrays"), Arguments.of("examples", "QuickSort")
    /** ,Arguments.of("tests", "MultidimensionalArrays") */
    );
  }

  @ParameterizedTest
  @MethodSource
  public void testKommpeiler(String folder, String programName)
      throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException {
    String inputPath =
        this.getClass().getResource(folder + File.separator + programName + ".java").getPath();
    // compile using Kommpeiler
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    new Orzo(new File(inputPath), new Output(ps), null).compile();
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
    String expected = Files.readString(Path.of(this.getClass()
        .getResource(folder + File.separator + "output" + File.separator + programName + ".output")
        .getPath()));
    assertEquals(expected, actual);
  }
}
