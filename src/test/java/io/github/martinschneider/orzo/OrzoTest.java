package io.github.martinschneider.orzo;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;
import static io.github.martinschneider.orzo.util.FactoryHelper.stream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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

  static Stream<Arguments> tests() {
    return stream(
        args(list("examples/HelloWorld")),
        args(list("tests/IntegerConstants")),
        args(list("tests/VariableAssignments")),
        args(list("tests/AutoboxAssignment")),
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
        args(list("tests/CompoundAssignTernary")),
        args(list("tests/EscapeSequences")),
        args(list("tests/MethodThrowsClause")),
        args(list("tests/ChainedConstructorStatement")),
        args(list("tests/EmbeddedFieldAssignment")),
        args(list("tests/StaticImportMethodCall", "tests/StaticImportFieldHolder")),
        args(list("tests/CharOverloadResolution")),
        args(list("tests/AssignmentAsArgument")),
        args(list("tests/StringConcatOverload")),
        args(list("tests/BitOperators")),
        args(list("examples/RussianPeasant")),
        args(list("tests/IntArrays")),
        args(list("tests/DoubleArrays")),
        args(list("tests/ByteArrays")),
        args(list("tests/ShortArrays")),
        args(list("tests/LongArrays")),
        args(list("tests/FloatArrays")),
        args(list("tests/ArrayWriteOverload")),
        args(list("tests/UnsignedByteFromArray")),
        args(list("tests/ChainedCallStatement")),
        args(list("tests/TryInForEach")),
        args(list("tests/EmptyBlocks")),
        args(list("tests/ReferenceArrayLiteral")),
        args(list("tests/ForEachOverMethodCallArray")),
        args(list("tests/ObjectUtilsUsage", "tests/ObjectUtilsUsagePoint")),
        args(list("tests/ComparisonAsValue")),
        args(list("tests/FullyQualifiedTypeDeclaration")),
        args(list("tests/BoxedForEach")),
        args(list("tests/EnumSwitch", "tests/Color")),
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
        args(list("tests/Fields")),
        args(list("tests/ArrayLength")),
        args(list("tests/WhileTrueTrailingReturn")),
        args(list("tests/WhileTrueBreakInTry")),
        args(list("tests/WhileTrueBreakInCatch")),
        args(list("tests/NestedConstructorArg")),
        args(list("tests/VariableShadowsField")),
        args(list("tests/ArrayReturnType")),
        // Constructor call examples
        args(list("tests/Constructor")),
        args(list("tests/StaticFields", "tests/Constants")),
        args(list("tests/Enum", "tests/Color", "tests/Status", "tests/Size")),
        args(list("tests/TestEnumSupport", "tests/AccessFlagTest", "tests/Status")),
        args(list("tests/Ternary")),
        args(list("tests/OtherFieldAccess")),
        args(list("tests/Autoboxing", "tests/AutoboxBase")),
        args(list("tests/ChainedFieldAssignment", "tests/FieldHolder")),
        args(list("tests/TryCatch")),
        args(list("tests/TryCatchInIf")),
        args(list("tests/Instanceof")),
        args(list("tests/InstanceofAnd")),
        args(list("tests/CompareNegLiteral")),
        args(list("tests/BridgeMethods")),
        args(list("tests/StaticFieldAccess")),
        args(list("tests/ArrayMethodInit")),
        args(list("tests/NullCheckAnd")),
        args(list("tests/StaticInit")),
        args(list("tests/TryCatchLocals")),
        args(list("tests/ArrayLengthCast")),
        args(list("tests/ForEachLoops")),
        args(list("tests/ForEachPrimitive")),
        args(list("tests/ForEachNestedBranch")),
        args(list("tests/ForEachNestedFor")),
        args(list("tests/ForEachAndCondition")),
        args(list("tests/ArrayFieldAccess", "tests/ArrayFieldHolder")),
        args(list("tests/CastMethodChain")),
        args(list("tests/LongIntComparison")),
        args(list("tests/DoubleParen")),
        args(list("tests/ForEachArray")),
        args(list("tests/AssignInCondition")),
        args(list("tests/WhileAssign")),
        args(list("tests/Unless")),
        args(list("examples/BinarySearch")),
        args(list("tests/StringConcat")),
        args(list("tests/Switch")),
        args(list("tests/TernaryNoParen")),
        args(list("tests/FloatComparison")),
        args(list("tests/TernaryMethodBranch")),
        args(list("tests/ThrowStatement")),
        args(list("tests/ThrowNested")),
        args(list("tests/FloatCast")),
        args(list("tests/ReturnNull")),
        args(list("tests/StringLiteralMethod")),
        args(list("tests/StaticFieldMethodCall")),
        args(list("tests/FieldInitShadowing")),
        args(list("tests/ContinueLoops")),
        args(list("tests/BreakContinueNested")),
        args(list("tests/ForEachBreakContinue")),
        args(list("tests/ClassLiteral")),
        args(list("tests/MethodCallFieldAccess", "tests/Holder")),
        args(list("tests/IfConditionScopeLeak")),
        args(list("tests/IfSlotReuseLoop")),
        args(list("tests/CharNotEqualAssignment")),
        args(list("tests/SuperFieldShadowing", "tests/ParentFieldShadow")),
        args(list("tests/CharLessEqual")),
        args(list("tests/LambdaStreamMap")),
        args(list("tests/LambdaComputeIfAbsent")),
        args(list("tests/FullyQualifiedGenericDeclaration")),
        args(list("tests/GenericChainCalls", "tests/GenericChainHolder")),
        args(list("tests/TryCatchAfterBlockLocals")),
        args(list("tests/StringLiteralReceiverInLogicalOp")),
        args(list("tests/MultiCatch")),
        args(list("tests/StringConcatWithCall")),
        args(list("tests/FullyQualifiedMethodSignature")),
        args(list("tests/GenericCast")),
        args(list("tests/ArrayElementReceiver")),
        args(list("tests/ChainedArgumentTypes", "tests/GenericChainHolder")),
        args(list("tests/TernaryArgument")),
        args(list("tests/MultipleStaticBlocks")),
        args(list("tests/GenericArrayValue")),
        args(list("tests/ArrayElementDeclaration")),
        args(list("tests/Varargs", "tests/VarargsHolder")),
        args(list("tests/NonShortCircuitBoolean")),
        args(list("tests/ForEachArrayFieldInIf")),
        args(list("tests/CastChainOperand")),
        args(list("tests/NegatedLogicalOp")),
        args(list("tests/GenericStaticField", "tests/GenericStaticFieldHolder")),
        args(list("tests/GenericArrayTypeArgs")),
        args(list("tests/ForEachArrayElements")),
        args(list("tests/StaticImportPrecedence", "tests/StaticImportPrecedenceEnum")),
        args(list("tests/JdkEnumSwitch")),
        args(list("tests/FieldChainConstructorArg", "tests/GenericChainHolder")),
        args(list("tests/VoidReturnAfterCall")),
        args(list("tests/CStyleArrayParam")),
        args(list("tests/NestedTernary")),
        args(list("tests/WidenCallResult")),
        args(list("tests/TryFinally")),
        args(list("tests/ImplicitThisCallArg")),
        args(list("tests/StaticCallOnClassName")),
        args(list("tests/QualifiedInstanceof")),
        args(list("tests/NullArgument")),
        args(list("tests/ClassLiteralChains")),
        args(list("tests/NestedClassTypes")),
        args(list("tests/GenericMethodInference")),
        args(list("tests/QualifiedCast")),
        args(list("tests/TernaryGenericCast")),
        args(list("tests/LambdaTypedParams", "tests/GenericChainHolder")),
        args(list("tests/LambdaCaptures")),
        args(list("tests/PrimitiveFirstConcat")),
        args(list("tests/ChainedFieldIncrement")),
        args(list("tests/NestedAlwaysReturns")),
        args(list("tests/TernaryCommonSuperclass")),
        args(list("tests/FieldOfCallAssignment")),
        args(list("tests/ParenthesizedReceiver")),
        args(list("tests/LargeIntInArrayInit")),
        args(list("tests/OwnMethodPrecedence", "tests/OwnMethodPrecedenceOther")),
        args(list("tests/SwitchCaseBlocks")),
        args(list("tests/StaticFieldChain")),
        args(list("tests/SiblingBlockLocals")),
        args(list("tests/LabeledLoops")),
        args(list("tests/BracelessBodies")),
        args(list("tests/HexIntLiterals")),
        args(list("tests/SameNamedFieldChain", "tests/ChainedFieldIncrement")),
        args(list("tests/ArrayAsObjectArgument")),
        args(list("tests/GenericFieldSignature")),
        args(list("tests/BoxingOverloadAndNewChain")),
        args(list("tests/MostSpecificOverload")),
        args(list("tests/ArrayInitializerShorthand")),
        args(list("tests/UnboxGenericReturn")),
        args(list("tests/ForEachGenericVar")),
        args(list("tests/BridgeOverload")),
        args(list("tests/ChainedIncrementExpr")),
        args(list("tests/UnboxingCast")),
        args(list("tests/UnboxArrayInitElements")),
        args(list("tests/ArrayInitAssignment")),
        args(list("tests/MethodSignature")),
        args(list("tests/NestedClasses")),
        args(list("tests/NestedUser", "tests/NestedClasses")),
        args(list("tests/MethodReference")),
        args(list("tests/GenericTargetTyping")),
        args(list("tests/BooleanReturnArithmetic")),
        args(list("tests/ConcatForObject")),
        args(list("tests/LongDoubleConstants")),
        args(list("tests/EnumConstructors")),
        args(list("tests/EnumUsage", "tests/EnumUsageColor")),
        args(list("tests/ArrayElementIncrement")),
        args(list("tests/FieldArrayAssignment")),
        args(list("tests/UnaryOperators")),
        args(list("tests/NumericPromotion")),
        args(list("tests/MidExpressionCasts")),
        args(list("tests/SwitchFallThrough")),
        args(list("tests/QualifiedTypeBound")),
        args(list("tests/PowerOperator")),
        args(list("tests/FieldOfNewObject")),
        args(list("tests/SamePackageCast", "tests/SamePackageCastBox")),
        args(list("tests/InterfaceObjectMethods")),
        args(list("tests/TernaryArrayStore")),
        args(list("tests/CastPrecedence")),
        args(list("tests/RealDivision")),
        args(list("tests/ConcatComparison")),
        args(list("tests/FloorOperator")),
        args(list("tests/UnboxedArrayElement")),
        args(list("tests/InlineAnnotations")),
        args(list("tests/LambdaArguments")),
        args(list("tests/FloatLiterals")),
        args(list("tests/ModifierOrder")),
        args(list("tests/GenericSupertypes")),
        args(list("tests/InstanceInitializers")),
        args(list("tests/AbstractClasses")),
        args(list("tests/InnerClasses")),
        args(list("tests/AnonymousClasses")),
        args(list("tests/LocalClasses")),
        args(list("tests/InterfaceMethods")),
        args(list("tests/GenericMethods")),
        args(list("tests/ForClauses")),
        args(list("tests/BoxedValues")),
        args(list("tests/LambdaBlocks")),
        args(list("tests/StaticFieldAssignment")),
        args(list("examples/Calculus")),
        args(list("tests/SuperMethodCall")),
        args(list("tests/UnboxedOperands")),
        args(list("tests/TypeVariables")),
        args(list("tests/LocalScopes")));
  }

  // Orzo.compile(outputs) appended a new entry to `outputs` for every input file even when the
  // caller had already pre-supplied one at that index (the exact pattern every test in this class
  // uses) - duplicating it and leaving `outputs` one longer than `clazzes`. Dormant until
  // CodeGenerator started appending classes mid-generation (e.g. a lambda's synthetic
  // implementation, via ctx.addSyntheticClazz()), which assumes the two lists stay index-aligned:
  // the duplicate entry meant a later-appended class's Output landed at the wrong list index,
  // and the class actually read at that index was a second reference to an EARLIER class's
  // Output - so the two classes' bytecode got concatenated into one buffer ("Extra bytes at the
  // end of class file").
  @Test
  public void testCompileKeepsClazzesAndOutputsAligned() throws IOException {
    List<File> inputs =
        list(
            new File(
                this.getClass()
                    .getResource("examples" + File.separator + "HelloWorld.java")
                    .getPath()));
    List<Output> outputs =
        new ArrayList<>(list(new Output(new PrintStream(new ByteArrayOutputStream()))));
    Orzo orzo = new Orzo(inputs, null, 0);
    orzo.compile(outputs);
    assertEquals(outputs.size(), orzo.clazzes.size());
  }

  // CLI-style compilation (no caller-supplied outputs, so Orzo writes .class files to disk
  // itself): a synthetic class discovered mid-generation (e.g. a lambda's functional-interface
  // implementation) has no file-backed Output yet, unlike the ones from the original input
  // files - Orzo.compile() must persist it to its own file too, not just the main class's.
  @Test
  public void testCompileWritesSyntheticClassFilesToDisk(@TempDir File outputDir)
      throws IOException {
    List<File> inputs =
        list(
            new File(
                this.getClass()
                    .getResource("tests" + File.separator + "LambdaStreamMap.java")
                    .getPath()));
    Orzo orzo = new Orzo(inputs, outputDir.getAbsolutePath(), 0);
    orzo.compile();
    File packageDir =
        new File(
            outputDir, "io/github/martinschneider/orzo/tests".replace('/', File.separatorChar));
    assertTrue(new File(packageDir, "LambdaStreamMap.class").isFile());
    assertTrue(new File(packageDir, "LambdaStreamMap$$Lambda$1.class").isFile());
  }

  @ParameterizedTest
  @MethodSource("tests")
  public void testCorrectness(List<String> programs)
      throws IOException,
          ClassNotFoundException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
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
    assertTrue(orzo.compile(outputs), () -> programs + ": " + orzo.errors);
    for (Clazz clazz : orzo.clazzes) {
      classNames.add(clazz.fqn());
    }
    for (int i = 0; i < programs.size(); i++) {
      classLoader.put(classNames.get(i), streams.get(i).toByteArray());
    }
    // Classes discovered during codegen beyond the original input files (e.g. a lambda
    // expression's synthetic functional-interface implementation) have no entry in `streams` -
    // Output always buffers its own bytes internally, so read them straight from `outputs`.
    for (int i = programs.size(); i < classNames.size(); i++) {
      classLoader.put(classNames.get(i), outputs.get(i).getBytes());
    }
    // First class should have main method
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
      throws IOException,
          ClassNotFoundException,
          IllegalAccessException,
          IllegalArgumentException,
          InvocationTargetException,
          NoSuchMethodException,
          SecurityException {
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
