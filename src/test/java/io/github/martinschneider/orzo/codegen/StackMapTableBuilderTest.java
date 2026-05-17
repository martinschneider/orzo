package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.GETFIELD;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;
import static io.github.martinschneider.orzo.codegen.OpCodes.IADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPLE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.PUTFIELD;
import static io.github.martinschneider.orzo.codegen.OpCodes.RETURN;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StackMapTableBuilderTest {

  private MockConstantPool constPool;

  @BeforeEach
  public void setUp() {
    CGContext ctx = new CGContext();
    constPool = new MockConstantPool(ctx, emptyList());
  }

  @Test
  public void testSimpleGetter() {
    // int size() { return pointer; }
    // aload_0; getfield #1; ireturn  →  max stack = 1
    byte[] code = {ALOAD_0, GETFIELD, 0, 1, IRETURN};
    assertEquals(1, StackMapTableBuilder.computeMaxStack(code, constPool));
  }

  @Test
  public void testTwoLoadsAndAdd() {
    // int add(int a, int b) { return a + b; }
    // iconst_1; iconst_2; iadd; ireturn  →  max stack = 2
    byte[] code = {ICONST_1, ICONST_2, IADD, IRETURN};
    assertEquals(2, StackMapTableBuilder.computeMaxStack(code, constPool));
  }

  @Test
  public void testPutFieldPopsTwo() {
    // this.field = 1;  →  aload_0; iconst_1; putfield #1; return  →  max stack = 2
    byte[] code = {ALOAD_0, ICONST_1, PUTFIELD, 0, 1, RETURN};
    assertEquals(2, StackMapTableBuilder.computeMaxStack(code, constPool));
  }

  @Test
  public void testConditionalBranchPopsTwo() {
    // if (a <= b) goto end; body; end: return
    // iconst_1; iconst_2; if_icmple +3; iconst_1; istore_2; return
    // peak stack = 2 (before if_icmple pops both)
    byte[] code = {
      ICONST_1, // +1 → depth 1
      ICONST_2, // +1 → depth 2  (peak here)
      IF_ICMPLE,
      0,
      3, // pop 2 → depth 0; jump to offset 8
      ICONST_1, // depth 1 (dead in taken branch, but not dead in fallthrough)
      ISTORE_2, // pop → depth 0
      RETURN // offset 8: branch target
    };
    assertEquals(2, StackMapTableBuilder.computeMaxStack(code, constPool));
  }

  @Test
  public void testDeadCodeAfterReturn() {
    // return; dead: iconst_1; iconst_2; iadd; ireturn
    // dead code is simulated with empty stack (max from dead path = 2)
    byte[] code = {
      ICONST_1,
      IRETURN, // real path: depth 1
      ICONST_1,
      ICONST_2,
      IADD,
      IRETURN // dead code re-entered with empty stack
    };
    assertEquals(2, StackMapTableBuilder.computeMaxStack(code, constPool));
  }

  @Test
  public void testExceptionHandlerEntryDepth() {
    // try { return; } catch (Exception e) { return; }
    // offset 0: return   (try body, always exits)
    // offset 1: astore_1 (catch handler — JVM pushes exception before entering)
    // offset 2: return
    byte[] code = {RETURN, ASTORE_1, RETURN};

    // Without exception table: handler entered with empty stack (depth 0 throughout)
    assertEquals(0, StackMapTableBuilder.computeMaxStack(code, constPool));

    // With exception table: handler at offset 1 is seeded with the exception object (depth 1)
    List<int[]> exTable = Collections.singletonList(new int[] {0, 1, 1, 0});
    Map<Integer, String> handlerTypes = Collections.singletonMap(1, "java/lang/Exception");
    assertEquals(1, StackMapTableBuilder.computeMaxStack(code, constPool, exTable, handlerTypes));
  }

  @Test
  public void testWhileLoopBackEdge() {
    // while (i <= 10) { i++; }  (simplified: check then goto back)
    // offset 0: iconst_1; iconst_2; if_icmple +6(→9)
    // offset 5: iconst_1; istore_2; goto -8(→0)
    // offset 11: return
    byte[] code = {
      ICONST_1,
      ICONST_2,
      IF_ICMPLE,
      0,
      6, // 0-4: pop 2 if <=; branch to 11
      ICONST_1,
      ISTORE_2, // 5-6: body (depth: 1, then 0)
      GOTO,
      (byte) -8,
      0, // 7-9: goto 0 (back edge)  [but GOTO is 3 bytes: op+offset16]
      RETURN // 10: exit
    };
    // Wait, GOTO takes a signed 16-bit offset: bytes [offset_high, offset_low]
    // goto -8 means: (byte)0xFF, (byte)0xF8 but stored as short
    // Let me fix this: offset from GOTO instruction start (offset 7) → target 0 → delta = 0-7 = -7
    // Encoded as: 0xFF, 0xF9
    byte[] code2 = {
      ICONST_1,
      ICONST_2,
      IF_ICMPLE,
      0,
      6, // 0: depth 2 peak; branch to offset 0+5+6=11
      ICONST_1,
      ISTORE_2, // 5: body
      GOTO,
      (byte) 0xFF,
      (byte) 0xF9, // 7: goto offset 7+(-7) = 0
      RETURN // 10: target of if_icmple
    };
    // peak is 2 (at iconst_1; iconst_2 before the if_icmple)
    assertEquals(2, StackMapTableBuilder.computeMaxStack(code2, constPool));
  }
}
