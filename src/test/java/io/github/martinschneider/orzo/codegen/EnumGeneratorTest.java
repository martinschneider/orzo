package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.util.FactoryHelper.clazz;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;
import static io.github.martinschneider.orzo.util.decompiler.BytecodeDecompiler.decompile;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.codegen.generators.MethodGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(Lifecycle.PER_CLASS)
public class EnumGeneratorTest {

  private MethodGenerator target;
  private HasOutput out;

  @BeforeAll
  public void init() {
    target = new MethodGenerator(new CGContext());
  }

  @BeforeEach
  public void reset() {
    out = new DynamicByteArray();
  }

  // Create an enum test class for Color
  private static final Clazz createColorEnum() {
    Clazz enumClazz =
        clazz(
            "io.github.martinschneider.orzo.tests",
            emptyList(),
            scope(PUBLIC),
            "Color",
            emptyList(),
            "java.lang.Enum",
            emptyList(),
            emptyList());
    enumClazz.isEnum = true;

    // Add enum constants (RED, GREEN, BLUE)
    List<Declaration> enumConstants = new ArrayList<>();
    Declaration red = new Declaration(emptyList(), "Color", new Identifier("RED"), null);
    Declaration green = new Declaration(emptyList(), "Color", new Identifier("GREEN"), null);
    Declaration blue = new Declaration(emptyList(), "Color", new Identifier("BLUE"), null);

    enumConstants.add(red);
    enumConstants.add(green);
    enumConstants.add(blue);

    ParallelDeclaration enumConstantsDecl = new ParallelDeclaration(enumConstants);

    enumClazz.fields = list(enumConstantsDecl);

    return enumClazz;
  }

  // Create an enum test class for AccessFlag with short values
  private static final Clazz createAccessFlagEnum() {
    Clazz enumClazz =
        clazz(
            "io.github.martinschneider.orzo.tests",
            emptyList(),
            scope(PUBLIC),
            "AccessFlagTest",
            emptyList(),
            "java.lang.Enum",
            emptyList(),
            emptyList());
    enumClazz.isEnum = true;

    // Add enum constants with short constructor parameters
    List<Declaration> enumConstants = new ArrayList<>();
    Declaration accDefault =
        new Declaration(emptyList(), "AccessFlagTest", new Identifier("ACC_DEFAULT"), null);
    Declaration accPublic =
        new Declaration(emptyList(), "AccessFlagTest", new Identifier("ACC_PUBLIC"), null);

    enumConstants.add(accDefault);
    enumConstants.add(accPublic);

    ParallelDeclaration enumConstantsDecl = new ParallelDeclaration(enumConstants);

    enumClazz.fields = list(enumConstantsDecl);

    return enumClazz;
  }

  // Helper method to create enum methods
  private static Method createEnumMethod(String methodName, String signature, List<Argument> args) {
    Method method =
        new Method(
            "io.github.martinschneider.orzo.tests.Color",
            emptyList(),
            "void",
            new Identifier(methodName),
            args != null ? args : emptyList(),
            emptyList());
    return method;
  }

  private static Stream<Arguments> testEnumMethodGeneration() {
    return Stream.of(
        args(
            "values",
            "()[Lio/github/martinschneider/orzo/tests/Color;",
            emptyList(),
            list("getstatic", "invokevirtual", "checkcast", "areturn")),
        args(
            "valueOf",
            "(Ljava/lang/String;)Lio/github/martinschneider/orzo/tests/Color;",
            list(new Argument("String", new Identifier("name"))),
            list("ldc", "aload_0", "invokestatic", "checkcast", "areturn")),
        args(
            "$values",
            "()[Lio/github/martinschneider/orzo/tests/Color;",
            emptyList(),
            list(
                "bipush",
                "anewarray",
                "dup",
                "iconst_0",
                "getstatic",
                "aastore",
                "dup",
                "iconst_1",
                "getstatic",
                "aastore",
                "dup",
                "iconst_2",
                "getstatic",
                "aastore",
                "areturn")));
  }

  @ParameterizedTest
  @MethodSource
  public void testEnumMethodGeneration(
      String methodName, String signature, List<Argument> methodArgs, List<String> expectedBytecode)
      throws IOException {

    // Setup enum class and context
    Clazz enumClazz = createColorEnum();
    target.ctx.init(new CompilerErrors(), null, 0, list(enumClazz));

    // Create enum method
    Method enumMethod = createEnumMethod(methodName, signature, methodArgs);

    // Generate bytecode for enum method
    DynamicByteArray methodOut = new DynamicByteArray();
    boolean wasGenerated = callGenerateEnumMethod(methodOut, enumMethod, enumClazz);

    if (wasGenerated) {
      // Decompile the generated bytecode and split into lines
      String decompiled = decompile(methodOut.getBytes());
      List<String> actualBytecode = Arrays.asList(decompiled.split("\n"));

      // Extract just the opcode names (without parameters) for comparison
      List<String> actualOpcodes = new ArrayList<>();
      for (String line : actualBytecode) {
        String[] parts = line.trim().split(" ");
        if (parts.length > 0 && !parts[0].isEmpty()) {
          actualOpcodes.add(parts[0]);
        }
      }

      // Compare the exact sequence of opcodes
      assertEquals(
          expectedBytecode,
          actualOpcodes,
          "Bytecode mismatch for "
              + methodName
              + " method. Expected: "
              + expectedBytecode
              + ", Actual: "
              + actualOpcodes
              + ", Decompiled: "
              + decompiled);
    } else {
      throw new AssertionError("Enum method " + methodName + " was not generated");
    }
  }

  // Use reflection to access the private generateEnumMethod
  private boolean callGenerateEnumMethod(DynamicByteArray out, Method method, Clazz clazz) {
    try {
      java.lang.reflect.Method generateMethod =
          target
              .getClass()
              .getDeclaredMethod(
                  "generateEnumMethod", DynamicByteArray.class, Method.class, Clazz.class);
      generateMethod.setAccessible(true);
      return (Boolean) generateMethod.invoke(target, out, method, clazz);
    } catch (Exception e) {
      throw new RuntimeException("Failed to call generateEnumMethod", e);
    }
  }
}
