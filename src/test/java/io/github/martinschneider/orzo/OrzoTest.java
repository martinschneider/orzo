package io.github.martinschneider.orzo;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;
import static io.github.martinschneider.orzo.util.FactoryHelper.stream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.codegen.Output;
import io.github.martinschneider.orzo.parser.productions.Clazz;
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

  private static Stream<Arguments> tests() {
    return stream(
        args(list("examples/HelloWorld")),
        args(list("tests/IntegerConstants")),
        args(list("tests/VariableAssignments")),
        args(list("tests/IntegerExpressions")),
        args(list("tests/IfConditions")),
        args(list("tests/IfElseConditions")),
        args(list("tests/WhileLoops")),
        args(list("tests/ForLoops")),
        args(list("tests/DoLoops")),
        args(list("examples/Fibonacci")),
        args(list("tests/NestedLoops")),
        args(list("tests/MethodCalls")),
        args(list("examples/Factorial")),
        args(list("examples/CollatzConjecture")),
        args(list("tests/ParallelAssignmentsInteger")),
        args(list("tests/ParallelAssignmentsLong")),
        args(list("tests/ParallelAssignmentsShort")),
        args(list("tests/ParallelAssignmentsByte")),
        args(list("tests/ParallelAssignmentsFloat")),
        args(list("tests/ParallelAssignmentsDouble")),
        args(list("tests/ParallelAssignmentsArray")),
        args(list("tests/ParallelAssignmentsMultiple")),
        args(list("tests/BreakLoops")),
        args(list("tests/Bytes")),
        args(list("tests/Shorts")),
        args(list("tests/Longs")),
        args(list("examples/CollatzConjecture2")),
        args(list("tests/Doubles")),
        args(list("tests/Floats")),
        args(list("tests/Booleans")),
        args(list("examples/Pi", "examples/MathUtils")),
        args(list("tests/BitShifts")),
        args(list("tests/UnsignedRightShift")),
        args(list("tests/CompoundAssignments")),
        args(list("tests/BitOperators")),
        args(list("examples/RussianPeasant")),
        args(list("tests/IntArrays")),
        args(list("tests/DoubleArrays")),
        args(list("tests/ByteArrays")),
        args(list("tests/ShortArrays")),
        args(list("tests/LongArrays")),
        args(list("tests/FloatArrays")),
        args(list("examples/QuickSort")),
        /** args(list("examples/MergeSort")), */
        args(list("tests/Chars")),
        args(list("tests/Chars2")),
        /** args(list("tests/MultidimensionalArrays")), */
        args(list("tests/Circles", "examples/MathUtils")),
        args(list("tests/JavaLangImports")),
        args(list("tests/Pow")),
        args(list("tests/PostIncrementDouble")),
        args(list("tests/PostIncrementFloat")),
        args(list("tests/PostIncrementInt")),
        args(list("tests/PostIncrementShort")),
        args(list("tests/PostIncrementByte")),
        args(list("tests/PostIncrementLong")),
        args(list("tests/PostIncrementChar")),
        args(list("tests/PostDecrementDouble")),
        args(list("tests/PostDecrementFloat")),
        args(list("tests/PostDecrementInt")),
        args(list("tests/PostDecrementShort")),
        args(list("tests/PostDecrementByte")),
        args(list("tests/PostDecrementLong")),
        args(list("tests/PostDecrementChar")),
        args(list("tests/PreIncrementDouble")),
        args(list("tests/PreIncrementFloat")),
        args(list("tests/PreIncrementInt")),
        args(list("tests/PreIncrementShort")),
        args(list("tests/PreIncrementByte")),
        args(list("tests/PreIncrementLong")),
        args(list("tests/PreIncrementChar")),
        args(list("tests/PreDecrementDouble")),
        args(list("tests/PreDecrementFloat")),
        args(list("tests/PreDecrementInt")),
        args(list("tests/PreDecrementShort")),
        args(list("tests/PreDecrementByte")),
        args(list("tests/PreDecrementLong")),
        args(list("tests/PreDecrementChar")),
        args(list("examples/Fibonacci2", "examples/MathUtils")),
        args(list("tests/BasicTypeCasts")),
        args(list("tests/GlobalVariables")),
        args(list("tests/ArrayLength")),
        // TODO: support infinite loops
        // args(list("tests/InfiniteRepeat")),
        args(list("tests/ArrayReturnType")));
  }

  @ParameterizedTest
  @MethodSource("tests")
  public void testCorrectness(List<String> programs)
      throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException {
    List<File> inputs = new ArrayList<>();
    List<Output> outputs = new ArrayList<>();
    List<String> classNames = new ArrayList<>();
    List<ByteArrayOutputStream> streams = new ArrayList<>();
    ByteClassLoader classLoader = new ByteClassLoader(ClassLoader.getSystemClassLoader());
    for (int i = 0; i < programs.size(); i++) {
      String[] tmp = programs.get(i).split("/");
      inputs.add(
          new File(
              this.getClass().getResource(tmp[0] + File.separator + tmp[1] + ".java").getPath()));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      outputs.add(new Output(ps));
      streams.add(baos);
    }
    Orzo orzo = new Orzo(inputs, null, 0);
    orzo.compile(outputs);
    for (Clazz clazz : orzo.clazzes) {
      classNames.add(clazz.fqn());
    }
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

  // @ParameterizedTest
  @MethodSource("tests")
  public void testBytecode(List<String> programs)
      throws IOException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
          InvocationTargetException, NoSuchMethodException, SecurityException {
    List<File> inputs = new ArrayList<>();
    List<Output> outputs = new ArrayList<>();
    List<Path> expectedClasses = new ArrayList<>();
    List<ByteArrayOutputStream> streams = new ArrayList<>();
    for (int i = 0; i < programs.size(); i++) {
      String[] tmp = programs.get(i).split("/");
      inputs.add(
          new File(
              this.getClass().getResource(tmp[0] + File.separator + tmp[1] + ".java").getPath()));
      expectedClasses.add(
          Path.of(
              this.getClass()
                  .getResource(
                      tmp[0] + File.separator + "class" + File.separator + tmp[1] + ".class")
                  .getPath()));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(baos);
      outputs.add(new Output(ps));
      streams.add(baos);
    }
    Orzo orzo = new Orzo(inputs, null, 0);
    orzo.compile(outputs);
    for (int i = 0; i < streams.size(); i++) {
      assertArrayEquals(Files.readAllBytes(expectedClasses.get(i)), streams.get(i).toByteArray());
    }
  }
}
