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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    return Stream.of(
        Arguments.of(List.of("examples/HelloWorld")),
        Arguments.of(List.of("tests/IntegerConstants")),
        Arguments.of(List.of("tests/VariableAssignments")),
        Arguments.of(List.of("tests/IntegerExpressions")),
        Arguments.of(List.of("tests/IfConditions")),
        Arguments.of(List.of("tests/IfElseConditions")),
        Arguments.of(List.of("tests/WhileLoops")),
        Arguments.of(List.of("tests/ForLoops")),
        Arguments.of(List.of("tests/DoLoops")),
        Arguments.of(List.of("examples/Fibonacci")),
        Arguments.of(List.of("tests/NestedLoops")),
        Arguments.of(List.of("tests/MethodCalls")),
        Arguments.of(List.of("examples/Factorial")),
        Arguments.of(List.of("examples/CollatzConjecture")),
        Arguments.of(List.of("tests/ParallelAssignmentsInteger")),
        Arguments.of(List.of("tests/ParallelAssignmentsLong")),
        Arguments.of(List.of("tests/ParallelAssignmentsShort")),
        Arguments.of(List.of("tests/ParallelAssignmentsByte")),
        Arguments.of(List.of("tests/ParallelAssignmentsFloat")),
        Arguments.of(List.of("tests/ParallelAssignmentsDouble")),
        Arguments.of(List.of("tests/ParallelAssignmentsArray")),
        Arguments.of(List.of("tests/ParallelAssignmentsMultiple")),
        Arguments.of(List.of("tests/BreakLoops")),
        Arguments.of(List.of("tests/Bytes")),
        Arguments.of(List.of("tests/Shorts")),
        Arguments.of(List.of("tests/Longs")),
        Arguments.of(List.of("examples/CollatzConjecture2")),
        Arguments.of(List.of("tests/Doubles")),
        Arguments.of(List.of("tests/Floats")),
        Arguments.of(List.of("examples/PiLeibniz", "examples/MathUtils")),
        Arguments.of(List.of("tests/BitShifts")),
        Arguments.of(List.of("tests/UnsignedRightShift")),
        Arguments.of(List.of("tests/CompoundAssignments")),
        Arguments.of(List.of("tests/BitOperators")),
        Arguments.of(List.of("examples/RussianPeasant")),
        Arguments.of(List.of("tests/IntArrays")),
        Arguments.of(List.of("tests/DoubleArrays")),
        Arguments.of(List.of("tests/ByteArrays")),
        Arguments.of(List.of("tests/ShortArrays")),
        Arguments.of(List.of("tests/LongArrays")),
        Arguments.of(List.of("tests/FloatArrays")),
        Arguments.of(List.of("examples/QuickSort")),
        Arguments.of(List.of("tests/Chars")),
        Arguments.of(List.of("tests/Chars2")),
        /** Arguments.of(List.of("tests/MultidimensionalArrays")), */
        Arguments.of(List.of("tests/Circles", "examples/MathUtils")),
        Arguments.of(List.of("tests/JavaLangImports")),
        Arguments.of(List.of("tests/Pow")),
        Arguments.of(List.of("tests/PostIncrementDouble")),
        Arguments.of(List.of("tests/PostIncrementFloat")),
        Arguments.of(List.of("tests/PostIncrementInt")),
        Arguments.of(List.of("tests/PostIncrementShort")),
        Arguments.of(List.of("tests/PostIncrementByte")),
        Arguments.of(List.of("tests/PostIncrementLong")),
        Arguments.of(List.of("tests/PostIncrementChar")),
        Arguments.of(List.of("tests/PostDecrementDouble")),
        Arguments.of(List.of("tests/PostDecrementFloat")),
        Arguments.of(List.of("tests/PostDecrementInt")),
        Arguments.of(List.of("tests/PostDecrementShort")),
        Arguments.of(List.of("tests/PostDecrementByte")),
        Arguments.of(List.of("tests/PostDecrementLong")),
        Arguments.of(List.of("tests/PostDecrementChar")),
        Arguments.of(List.of("tests/PreIncrementDouble")),
        Arguments.of(List.of("tests/PreIncrementFloat")),
        Arguments.of(List.of("tests/PreIncrementInt")),
        Arguments.of(List.of("tests/PreIncrementShort")),
        Arguments.of(List.of("tests/PreIncrementByte")),
        Arguments.of(List.of("tests/PreIncrementLong")),
        Arguments.of(List.of("tests/PreIncrementChar")),
        Arguments.of(List.of("tests/PreDecrementDouble")),
        Arguments.of(List.of("tests/PreDecrementFloat")),
        Arguments.of(List.of("tests/PreDecrementInt")),
        Arguments.of(List.of("tests/PreDecrementShort")),
        Arguments.of(List.of("tests/PreDecrementByte")),
        Arguments.of(List.of("tests/PreDecrementLong")),
        Arguments.of(List.of("tests/PreDecrementChar")));
  }

  @ParameterizedTest
  @MethodSource
  public void testKommpeiler(List<String> programs)
      throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException {
    List<File> inputs = new ArrayList<>();
    List<Output> outputs = new ArrayList<>();
    List<String> classNames = new ArrayList<>();
    List<ByteArrayOutputStream> streams = new ArrayList<>();
    ByteClassLoader classLoader = new ByteClassLoader(ClassLoader.getSystemClassLoader());
    for (int i = 0; i < programs.size(); i++) {
      String[] tmp = programs.get(i).split("/");
      classNames.add(tmp[1]);
      inputs.add(
          new File(
              this.getClass().getResource(tmp[0] + File.separator + tmp[1] + ".java").getPath()));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      outputs.add(new Output(ps));
      streams.add(baos);
    }
    new Orzo(inputs, null).compile(outputs);
    for (int i = 0; i < programs.size(); i++) {
      classLoader.put(classNames.get(i), streams.get(i).toByteArray());
    }
    Class<?> clazz = classLoader.loadClass(classNames.get(0));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    PrintStream old = System.out;
    System.setOut(ps);
    clazz.getMethod("main", String[].class).invoke(null, (Object) null);
    System.out.flush();
    System.setOut(old);
    String[] tmp = programs.get(0).split("/");
    String actual = baos.toString();
    String expected =
        Files.readString(
            Path.of(
                this.getClass()
                    .getResource(
                        tmp[0] + File.separator + "output" + File.separator + tmp[1] + ".output")
                    .getPath()));
    assertEquals(expected, actual);
  }
}
