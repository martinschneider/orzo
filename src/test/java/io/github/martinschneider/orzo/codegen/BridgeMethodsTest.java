package io.github.martinschneider.orzo.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.martinschneider.orzo.Orzo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

public class BridgeMethodsTest {

  private static class ByteClassLoader extends ClassLoader {
    ByteClassLoader() {
      super(ClassLoader.getSystemClassLoader());
    }

    Class<?> define(String name, byte[] bytes) {
      return defineClass(name, bytes, 0, bytes.length);
    }
  }

  @Test
  public void testBridgeMethodGeneratedForCovariantReturn() throws Exception {
    File source =
        new File(
            getClass()
                .getResource("/io/github/martinschneider/orzo/tests/BridgeMethods.java")
                .getPath());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Output output = new Output(new PrintStream(baos));
    Orzo orzo = new Orzo(List.of(source), null, 0);
    List<Output> outputs = new ArrayList<>();
    outputs.add(output);
    orzo.compile(outputs);

    byte[] classBytes = baos.toByteArray();
    ByteClassLoader cl = new ByteClassLoader();
    Class<?> clazz = cl.define("io.github.martinschneider.orzo.tests.BridgeMethods", classBytes);

    // Verify the bridge method Object get() was generated (covariant return: String → Object)
    Method bridge =
        Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> m.getName().equals("get") && m.isBridge())
            .findFirst()
            .orElse(null);
    assertNotNull(bridge, "Bridge method Object get() should be generated for Supplier<String>");
    assertEquals(Object.class, bridge.getReturnType());

    // Verify calling through the Supplier interface works
    @SuppressWarnings("unchecked")
    Supplier<String> supplier = (Supplier<String>) clazz.getDeclaredConstructor().newInstance();
    assertEquals("bridge", supplier.get());
  }

  @Test
  public void testBridgeMethodGeneratedForTypeVariableParam() throws Exception {
    File source =
        new File(
            getClass()
                .getResource("/io/github/martinschneider/orzo/tests/BridgeMethodsWithParam.java")
                .getPath());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Output output = new Output(new PrintStream(baos));
    Orzo orzo = new Orzo(List.of(source), null, 0);
    List<Output> outputs = new ArrayList<>();
    outputs.add(output);
    orzo.compile(outputs);

    byte[] classBytes = baos.toByteArray();
    ByteClassLoader cl = new ByteClassLoader();
    Class<?> clazz =
        cl.define("io.github.martinschneider.orzo.tests.BridgeMethodsWithParam", classBytes);

    // Verify the bridge method void accept(Object) was generated (erased TypeVariable param)
    Method bridge =
        Arrays.stream(clazz.getDeclaredMethods())
            .filter(m -> m.getName().equals("accept") && m.isBridge())
            .findFirst()
            .orElse(null);
    assertNotNull(
        bridge, "Bridge method void accept(Object) should be generated for Consumer<String>");
    assertEquals(Object.class, bridge.getParameterTypes()[0]);

    // Verify calling through the Consumer interface works (exercises writeLoad + CHECKCAST)
    @SuppressWarnings("unchecked")
    java.util.function.Consumer<String> consumer =
        (java.util.function.Consumer<String>) clazz.getDeclaredConstructor().newInstance();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream old = System.out;
    System.setOut(new PrintStream(out));
    consumer.accept("parambridge");
    System.setOut(old);
    assertEquals("parambridge\n", out.toString());
  }
}
