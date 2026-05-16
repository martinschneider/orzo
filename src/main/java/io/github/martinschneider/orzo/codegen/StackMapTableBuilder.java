package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.OpCodes.AALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.AASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ACONST_NULL;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.ANEWARRAY;
import static io.github.martinschneider.orzo.codegen.OpCodes.ARETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.ARRAYLENGTH;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.ATHROW;
import static io.github.martinschneider.orzo.codegen.OpCodes.BALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.BASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.orzo.codegen.OpCodes.CALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.CASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.CHECKCAST;
import static io.github.martinschneider.orzo.codegen.OpCodes.D2F;
import static io.github.martinschneider.orzo.codegen.OpCodes.D2I;
import static io.github.martinschneider.orzo.codegen.OpCodes.D2L;
import static io.github.martinschneider.orzo.codegen.OpCodes.DADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.DCMPG;
import static io.github.martinschneider.orzo.codegen.OpCodes.DCMPL;
import static io.github.martinschneider.orzo.codegen.OpCodes.DCONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.DCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.DMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.DNEG;
import static io.github.martinschneider.orzo.codegen.OpCodes.DREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.DRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP2;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP2_X1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP2_X2;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP_X1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP_X2;
import static io.github.martinschneider.orzo.codegen.OpCodes.F2D;
import static io.github.martinschneider.orzo.codegen.OpCodes.F2I;
import static io.github.martinschneider.orzo.codegen.OpCodes.F2L;
import static io.github.martinschneider.orzo.codegen.OpCodes.FADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCMPG;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCMPL;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.FDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.FMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.FNEG;
import static io.github.martinschneider.orzo.codegen.OpCodes.FREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.FRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.GETFIELD;
import static io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO_W;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2B;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2C;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2D;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2F;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2L;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2S;
import static io.github.martinschneider.orzo.codegen.OpCodes.IADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.IALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.IAND;
import static io.github.martinschneider.orzo.codegen.OpCodes.IASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_4;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_5;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_M1;
import static io.github.martinschneider.orzo.codegen.OpCodes.IDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFGE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFGT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFLE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFLT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFNONNULL;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFNULL;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ACMPEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ACMPNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPGE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPGT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPLE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPLT;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IINC;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.IMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.INEG;
import static io.github.martinschneider.orzo.codegen.OpCodes.INSTANCEOF;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKEDYNAMIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKEINTERFACE;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKESPECIAL;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKESTATIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKEVIRTUAL;
import static io.github.martinschneider.orzo.codegen.OpCodes.IOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.IREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.IRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISHL;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.IUSHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.IXOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.JSR;
import static io.github.martinschneider.orzo.codegen.OpCodes.JSR_W;
import static io.github.martinschneider.orzo.codegen.OpCodes.L2D;
import static io.github.martinschneider.orzo.codegen.OpCodes.L2F;
import static io.github.martinschneider.orzo.codegen.OpCodes.L2I;
import static io.github.martinschneider.orzo.codegen.OpCodes.LADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LAND;
import static io.github.martinschneider.orzo.codegen.OpCodes.LASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.LCMP;
import static io.github.martinschneider.orzo.codegen.OpCodes.LCONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.LCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDC;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDC2_W;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDC_W;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.LMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.LNEG;
import static io.github.martinschneider.orzo.codegen.OpCodes.LOOKUPSWITCH;
import static io.github.martinschneider.orzo.codegen.OpCodes.LOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.LRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSHL;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.LUSHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LXOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.MONITORENTER;
import static io.github.martinschneider.orzo.codegen.OpCodes.MONITOREXIT;
import static io.github.martinschneider.orzo.codegen.OpCodes.MULTIANEWARRAY;
import static io.github.martinschneider.orzo.codegen.OpCodes.NEW;
import static io.github.martinschneider.orzo.codegen.OpCodes.NEWARRAY;
import static io.github.martinschneider.orzo.codegen.OpCodes.NOP;
import static io.github.martinschneider.orzo.codegen.OpCodes.POP;
import static io.github.martinschneider.orzo.codegen.OpCodes.POP2;
import static io.github.martinschneider.orzo.codegen.OpCodes.PUTFIELD;
import static io.github.martinschneider.orzo.codegen.OpCodes.PUTSTATIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.RET;
import static io.github.martinschneider.orzo.codegen.OpCodes.RETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.SALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.SASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.SIPUSH;
import static io.github.martinschneider.orzo.codegen.OpCodes.SWAP;
import static io.github.martinschneider.orzo.codegen.OpCodes.TABLESWITCH;
import static io.github.martinschneider.orzo.codegen.OpCodes.WIDE;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds the StackMapTable attribute for a method.
 *
 * <p>Records branch targets and associated local variable types during code generation, then
 * serializes them into the StackMapTable attribute format (JVM spec §4.7.4). Every frame uses
 * full_frame (tag 255) with an empty operand stack, which is always true for branch targets in
 * Orzo's structured-control-flow output.
 */
public class StackMapTableBuilder {

  private static final class Frame {
    final List<String> locals;
    final List<String> stack;

    Frame(List<String> locals, List<String> stack) {
      this.locals = locals;
      this.stack = stack;
    }
  }

  // sorted map: absolute bytecode offset → frame (locals + operand stack types)
  private final TreeMap<Integer, Frame> frames = new TreeMap<>();

  /** Record a stack-map frame at the given absolute bytecode offset within the method. */
  private void addFrame(int offset, List<String> localTypes, List<String> stackTypes) {
    if (offset >= 0) {
      frames.put(offset, new Frame(new ArrayList<>(localTypes), new ArrayList<>(stackTypes)));
    }
  }

  /**
   * Snapshot the current local variable types for the given method, in slot order. Returns a list
   * of JVM type descriptors (e.g. "Ljava/lang/String;", "I", "J").
   */
  public static List<String> snapshotLocals(Method method, CGContext ctx) {
    List<String> types = new ArrayList<>();
    boolean isStatic = method.accFlags.contains(AccessFlag.ACC_STATIC);
    if (!isStatic) {
      types.add("L" + ctx.clazz.fqn('/') + ";");
    }
    List<VariableInfo> sorted = new ArrayList<>(ctx.classIdMap.variables.localMap.values());
    sorted.sort(Comparator.comparingInt(v -> v.idx));
    for (VariableInfo var : sorted) {
      types.add(TypeUtils.descr(var));
    }
    return types;
  }

  /**
   * Build the complete StackMapTable attribute bytes (attribute_name_index + attribute_length +
   * content), or null if there are no frames to emit.
   */
  public byte[] build(ConstantPool constPool) {
    if (frames.isEmpty()) {
      return null;
    }

    DynamicByteArray content = new DynamicByteArray();
    content.write((short) frames.size()); // number_of_entries

    int previousOffset = -1;
    for (java.util.Map.Entry<Integer, Frame> entry : frames.entrySet()) {
      int offset = entry.getKey();
      Frame frame = entry.getValue();
      int offsetDelta = (previousOffset == -1) ? offset : (offset - previousOffset - 1);

      content.write((byte) 255); // full_frame tag
      content.write((short) offsetDelta); // offset_delta
      content.write((short) frame.locals.size()); // number_of_locals
      for (String localType : frame.locals) {
        writeVerificationType(content, localType, constPool);
      }
      content.write((short) frame.stack.size()); // number_of_stack_items
      for (String stackType : frame.stack) {
        writeVerificationType(content, stackType, constPool);
      }

      previousOffset = offset;
    }

    constPool.addUtf8("StackMapTable");
    DynamicByteArray attr = new DynamicByteArray();
    attr.write(constPool.indexOf(CONSTANT_UTF8, "StackMapTable")); // u2 attribute_name_index
    attr.write(content.size()); // u4 attribute_length
    attr.write(content.getBytes());
    return attr.getBytes();
  }

  /**
   * Post-processing scanner: scans the fully-generated method bytecode, finds all branch targets,
   * and emits a StackMapTable with full_frame entries.
   *
   * <p>The cut offset for each target T is computed by propagating through fallthrough chains to
   * find the earliest bytecode position from which T is reachable. A local at slot S is considered
   * initialized at T only if its first STORE appears before the cut offset. Long and Double
   * variables emit an extra Top_variable_info for their second JVM slot when uninitialized.
   *
   * <p>Operand-stack types at each branch target are determined by forward simulation of the stack.
   */
  public static byte[] buildFromBytecode(
      byte[] code, Method method, CGContext ctx, ConstantPool constPool) {
    if (code == null || code.length == 0) {
      return null;
    }

    Map<Integer, Integer> firstStoreOffset = buildFirstStoreMap(code);
    int paramSlots = paramSlotCount(method);
    boolean isStatic = method.accFlags.contains(AccessFlag.ACC_STATIC);

    List<VariableInfo> sorted = new ArrayList<>(ctx.classIdMap.variables.localMap.values());
    sorted.sort(Comparator.comparingInt(v -> v.idx));

    Map<Integer, VariableInfo> slotToVar = new HashMap<>();
    for (VariableInfo var : sorted) {
      slotToVar.put((int) var.idx, var);
    }

    // Scan 1: collect branch targets, branchesToTarget (all branch sources per target),
    // prevInstrOffset
    Map<Integer, List<Integer>> branchesToTarget = new HashMap<>();
    Map<Integer, Integer> prevInstrOffset = new HashMap<>();
    java.util.TreeSet<Integer> targets = new java.util.TreeSet<>();

    int prev = -1;
    int i = 0;
    while (i < code.length) {
      if (prev >= 0) {
        prevInstrOffset.put(i, prev);
      }
      byte op = code[i];
      if ((op >= IFEQ && op <= GOTO) || op == IFNULL || op == IFNONNULL) {
        if (i + 2 < code.length) {
          short branchOffset = (short) (((code[i + 1] & 0xFF) << 8) | (code[i + 2] & 0xFF));
          int target = i + branchOffset;
          if (target >= 0 && target < code.length) {
            targets.add(target);
            branchesToTarget.computeIfAbsent(target, k -> new ArrayList<>()).add(i);
          }
        }
        if (op == GOTO) { // GOTO: instruction-after-GOTO also needs a frame
          int next = i + 3;
          if (next < code.length) {
            targets.add(next);
          }
        }
      } else if ((op >= IRETURN && op <= RETURN) || op == ATHROW) {
        // xRETURN (172-177) / ATHROW (191): instruction after unconditional transfer needs a frame
        int next = i + 1;
        if (next < code.length) {
          targets.add(next);
        }
      } else if (op == GOTO_W || op == JSR_W) {
        if (i + 4 < code.length) {
          int branchOffset =
              ((code[i + 1] & 0xFF) << 24)
                  | ((code[i + 2] & 0xFF) << 16)
                  | ((code[i + 3] & 0xFF) << 8)
                  | (code[i + 4] & 0xFF);
          int target = i + branchOffset;
          if (target >= 0 && target < code.length) {
            targets.add(target);
            branchesToTarget.computeIfAbsent(target, k -> new ArrayList<>()).add(i);
          }
        }
        if (op == GOTO_W) {
          int next = i + 5;
          if (next < code.length) {
            targets.add(next);
          }
        }
      }
      prev = i;
      i += opcodeLength(code, i);
    }

    if (targets.isEmpty()) {
      return null;
    }

    // Scan 2: forward stack simulation to determine operand stack at each branch target
    // (simulateStacks still uses minBranchToTarget for legacy; rebuild it here)
    Map<Integer, Integer> minBranchToTarget = new HashMap<>();
    for (java.util.Map.Entry<Integer, List<Integer>> e : branchesToTarget.entrySet()) {
      int tgt = e.getKey();
      for (int src : e.getValue()) {
        minBranchToTarget.merge(tgt, src, Math::min);
      }
    }
    Map<Integer, List<String>> branchTargetStacks =
        simulateStacks(code, constPool, targets, minBranchToTarget);

    // Add exception handler entry points: stack = [exception_class]
    // Also build a map from handler PC to the minimum try-start PC so that locals at handler frames
    // are computed conservatively (only variables definitely initialized before the try range).
    Map<Integer, Integer> handlerToTryStart = new HashMap<>();
    for (int[] entry : ctx.exceptionTable) {
      int tryStart = entry[0];
      int handlerPc = entry[2];
      String handlerType = ctx.exceptionHandlerType.get(handlerPc);
      if (handlerType != null) {
        targets.add(handlerPc);
        List<String> handlerStack = new ArrayList<>();
        handlerStack.add("L" + handlerType + ";");
        branchTargetStacks.put(handlerPc, handlerStack);
      }
      handlerToTryStart.merge(handlerPc, tryStart, Math::min);
    }

    // Build frames
    StackMapTableBuilder builder = new StackMapTableBuilder();
    for (int target : targets) {
      // Fresh cache per target: avoids stale sentinel values from cycle-breaking in previous
      // targets corrupting the cut computation for later targets.
      // (slot << 17 | position) → extended-cut value
      Map<Long, Integer> slotCutCache = new HashMap<>();
      List<String> targetLocals = new ArrayList<>();
      if (!isStatic) {
        targetLocals.add("L" + ctx.clazz.fqn('/') + ";");
      }

      // For exception handlers, a variable is only guaranteed to be initialized if it was
      // definitely assigned before the try range started (the handler can be entered from any
      // instruction within the try range, including the very first one). Use the try-start PC
      // as the effective target for isInitialized so that variables first stored within the
      // try body are not incorrectly included.
      int effectiveTarget = handlerToTryStart.getOrDefault(target, target);

      // Determine the highest initialized slot so we know when to stop emitting locals.
      int maxInitSlot = -1;
      for (VariableInfo var : sorted) {
        int slot = (int) var.idx;
        boolean initialized =
            isInitialized(
                slot,
                effectiveTarget,
                paramSlots,
                firstStoreOffset,
                branchesToTarget,
                prevInstrOffset,
                code,
                slotCutCache);
        if (initialized) {
          maxInitSlot = Math.max(maxInitSlot, slot);
        }
      }

      int slot = isStatic ? 0 : 1;
      while (slot <= maxInitSlot) {
        VariableInfo var = slotToVar.get(slot);
        if (var == null) {
          targetLocals.add(null);
          slot++;
        } else {
          String desc = TypeUtils.descr(var);
          boolean doubleSize = "J".equals(desc) || "D".equals(desc);
          boolean initialized =
              isInitialized(
                  slot,
                  effectiveTarget,
                  paramSlots,
                  firstStoreOffset,
                  branchesToTarget,
                  prevInstrOffset,
                  code,
                  slotCutCache);
          if (initialized) {
            targetLocals.add(desc);
          } else {
            targetLocals.add(null);
            if (doubleSize) {
              targetLocals.add(null);
            }
          }
          slot++;
          if (doubleSize) {
            slot++;
          }
        }
      }

      List<String> stackTypes =
          branchTargetStacks.getOrDefault(target, java.util.Collections.emptyList());
      builder.addFrame(target, targetLocals, stackTypes);
    }
    return builder.build(constPool);
  }

  /**
   * Forward stack simulation: walks the bytecode, tracking the operand stack type list. Records the
   * stack state at each branch target. For conditional branches, the comparison operands are popped
   * before recording the target stack (same as the fallthrough stack after the branch).
   */
  private static Map<Integer, List<String>> simulateStacks(
      byte[] code,
      ConstantPool constPool,
      java.util.Set<Integer> targets,
      Map<Integer, Integer> minBranchToTarget) {
    Map<Integer, List<String>> result = new HashMap<>();
    List<String> stack = new ArrayList<>();
    boolean dead = false;
    int i = 0;
    while (i < code.length) {
      // At a branch target with a previously-recorded stack, reinitialize
      if (result.containsKey(i)) {
        stack = new ArrayList<>(result.get(i));
        dead = false;
      } else if (dead) {
        stack = new ArrayList<>();
      }

      byte op = code[i];

      // Conditional branches: pop comparison operands, record target stack
      if ((op >= IFEQ && op <= IFLE) || op == IFNULL || op == IFNONNULL) {
        // ifeq..ifle, ifnull, ifnonnull: pop 1
        if (i + 2 < code.length) {
          short off = (short) (((code[i + 1] & 0xFF) << 8) | (code[i + 2] & 0xFF));
          int target = i + off;
          List<String> targetStack = new ArrayList<>(stack);
          if (!targetStack.isEmpty()) {
            targetStack.remove(targetStack.size() - 1);
          }
          result.putIfAbsent(target, targetStack);
          if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);
          }
        }
      } else if (op >= IF_ICMPEQ && op <= IF_ACMPNE) {
        // if_icmpxx (159-164), if_acmpxx (165-166): pop 2
        if (i + 2 < code.length) {
          short off = (short) (((code[i + 1] & 0xFF) << 8) | (code[i + 2] & 0xFF));
          int target = i + off;
          List<String> targetStack = new ArrayList<>(stack);
          if (targetStack.size() >= 2) {
            targetStack.remove(targetStack.size() - 1);
            targetStack.remove(targetStack.size() - 1);
          }
          result.putIfAbsent(target, targetStack);
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
        }
      } else if (op == GOTO) {
        if (i + 2 < code.length) {
          short off = (short) (((code[i + 1] & 0xFF) << 8) | (code[i + 2] & 0xFF));
          int target = i + off;
          result.putIfAbsent(target, new ArrayList<>(stack));
          // Instruction after GOTO is dead code; record empty stack
          int next = i + 3;
          if (next < code.length) {
            result.putIfAbsent(next, new ArrayList<>());
          }
          dead = true;
        }
      } else if (op == GOTO_W) {
        if (i + 4 < code.length) {
          int off =
              ((code[i + 1] & 0xFF) << 24)
                  | ((code[i + 2] & 0xFF) << 16)
                  | ((code[i + 3] & 0xFF) << 8)
                  | (code[i + 4] & 0xFF);
          int target = i + off;
          result.putIfAbsent(target, new ArrayList<>(stack));
          int next = i + 5;
          if (next < code.length) {
            result.putIfAbsent(next, new ArrayList<>());
          }
          dead = true;
        }
      } else if ((op >= IRETURN && op <= RETURN) || op == ATHROW) {
        // xRETURN / ATHROW: unconditional transfer; dead code after needs empty stack frame
        applyStackEffect(op, code, i, stack, constPool);
        int next = i + 1;
        if (next < code.length) {
          result.putIfAbsent(next, new ArrayList<>());
        }
        dead = true;
      } else {
        // Non-branch instruction: apply stack effect
        applyStackEffect(op, code, i, stack, constPool);
      }

      i += opcodeLength(code, i);
    }
    return result;
  }

  /**
   * Applies the operand-stack effect of the instruction at {@code pos} to {@code stack}. Handles
   * load, store, arithmetic, cast, field, invoke, and array opcodes. For CP-dependent opcodes
   * (getfield, invokevirtual, etc.) the ConstantPool is consulted for type information.
   */
  private static void applyStackEffect(
      byte op, byte[] code, int pos, List<String> stack, ConstantPool constPool) {
    switch (op) {
      case NOP:
      case IINC:
        break;

      // Push ref
      case ACONST_NULL:
      case ALOAD_0:
      case ALOAD_1:
      case ALOAD_2:
      case ALOAD_3:
      case ALOAD:
      case AALOAD:
      case DUP:
      case DUP_X1:
      case DUP_X2:
      case DUP2:
      case DUP2_X1:
      case DUP2_X2:
        // For dup: push a copy of the top (type unknown without full stack view, use Object)
        if (op >= DUP && op <= DUP2_X2) {
          if (!stack.isEmpty()) {
            stack.add(stack.get(stack.size() - 1));
          } else {
            stack.add("Ljava/lang/Object;");
          }
        } else if (op == AALOAD) {
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
          stack.add("Ljava/lang/Object;");
        } else {
          stack.add("Ljava/lang/Object;");
        }
        break;

      // Push I
      case ICONST_M1:
      case ICONST_0:
      case ICONST_1:
      case ICONST_2:
      case ICONST_3:
      case ICONST_4:
      case ICONST_5:
      case BIPUSH:
      case SIPUSH:
      case ILOAD:
      case ILOAD_0:
      case ILOAD_1:
      case ILOAD_2:
      case ILOAD_3:
      case IALOAD:
      case BALOAD:
      case CALOAD:
      case SALOAD:
      case ARRAYLENGTH:
      case INSTANCEOF:
        if (op == IALOAD || op == BALOAD || op == CALOAD || op == SALOAD) { // xaload: pop ref+I
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
        } else if (op == ARRAYLENGTH || op == INSTANCEOF) { // arraylength, instanceof: pop 1
          if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);
          }
        }
        stack.add("I");
        break;

      // Push J
      case LCONST_0:
      case LCONST_1:
      case LLOAD:
      case LLOAD_0:
      case LLOAD_1:
      case LLOAD_2:
      case LLOAD_3:
      case LALOAD:
        if (op == LALOAD) {
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
        }
        stack.add("J");
        break;

      // Push F
      case FCONST_0:
      case FCONST_1:
      case FCONST_2:
      case FLOAD:
      case FLOAD_0:
      case FLOAD_1:
      case FLOAD_2:
      case FLOAD_3:
      case FALOAD:
        if (op == FALOAD) {
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
        }
        stack.add("F");
        break;

      // Push D
      case DCONST_0:
      case DCONST_1:
      case DLOAD:
      case DLOAD_0:
      case DLOAD_1:
      case DLOAD_2:
      case DLOAD_3:
      case DALOAD:
        if (op == DALOAD) {
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
        }
        stack.add("D");
        break;

      // Pop 1 (store, return, throw)
      case ISTORE:
      case ISTORE_0:
      case ISTORE_1:
      case ISTORE_2:
      case ISTORE_3:
      case LSTORE:
      case LSTORE_0:
      case LSTORE_1:
      case LSTORE_2:
      case LSTORE_3:
      case FSTORE:
      case FSTORE_0:
      case FSTORE_1:
      case FSTORE_2:
      case FSTORE_3:
      case DSTORE:
      case DSTORE_0:
      case DSTORE_1:
      case DSTORE_2:
      case DSTORE_3:
      case ASTORE:
      case ASTORE_0:
      case ASTORE_1:
      case ASTORE_2:
      case ASTORE_3:
      case POP:
      case IRETURN:
      case LRETURN:
      case FRETURN:
      case DRETURN:
      case ARETURN:
      case ATHROW:
      case MONITORENTER:
      case MONITOREXIT:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        break;

      case RETURN:
        break;

      // pop2
      case POP2:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        } else if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        break;

      // iadd, isub, imul, idiv, irem: pop 2, push I
      case IADD:
      case ISUB:
      case IMUL:
      case IDIV:
      case IREM:
      case ISHL:
      case ISHR:
      case IUSHR:
      case IAND:
      case IOR:
      case IXOR:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;

      // ineg: pop I, push I
      case INEG:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;

      // ladd, lsub, lmul, ldiv, lrem, land, lor, lxor: pop 2, push J
      case LADD:
      case LSUB:
      case LMUL:
      case LDIV:
      case LREM:
      case LAND:
      case LOR:
      case LXOR:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;

      // lshl, lshr, lushr: pop J+I, push J
      case LSHL:
      case LSHR:
      case LUSHR:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;

      // lneg: pop J, push J
      case LNEG:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;

      // fadd, fsub, fmul, fdiv, frem: pop 2, push F
      case FADD:
      case FSUB:
      case FMUL:
      case FDIV:
      case FREM:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;

      // fneg: pop F, push F
      case FNEG:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;

      // dadd, dsub, dmul, ddiv, drem: pop 2, push D
      case DADD:
      case DSUB:
      case DMUL:
      case DDIV:
      case DREM:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;

      // dneg: pop D, push D
      case DNEG:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;

      // lcmp, fcmpl/g, dcmpl/g: pop 2, push I
      case LCMP:
      case FCMPL:
      case FCMPG:
      case DCMPL:
      case DCMPG:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;

      // i2l
      case I2L:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;
      // i2f
      case I2F:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;
      // i2d
      case I2D:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;
      // l2i
      case L2I:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;
      // l2f
      case L2F:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;
      // l2d
      case L2D:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;
      // f2i
      case F2I:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;
      // f2l
      case F2L:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;
      // f2d
      case F2D:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;
      // d2i
      case D2I:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;
      // d2l
      case D2L:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;
      // d2f
      case D2F:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;
      // i2b, i2c, i2s
      case I2B:
      case I2C:
      case I2S:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;

      // Array stores: pop ref+I+value = 3 items
      case IASTORE:
      case LASTORE:
      case FASTORE:
      case DASTORE:
      case AASTORE:
      case BASTORE:
      case CASTORE:
      case SASTORE:
        if (stack.size() >= 3) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        break;

      // swap: exchange top two
      case SWAP:
        if (stack.size() >= 2) {
          String a = stack.remove(stack.size() - 1);
          String b = stack.remove(stack.size() - 1);
          stack.add(a);
          stack.add(b);
        }
        break;

      // checkcast: no stack change (just verifies)
      case CHECKCAST:
        break;

      // new: push ref
      case NEW:
      case ANEWARRAY:
        if (!stack.isEmpty() && op == ANEWARRAY) {
          stack.remove(stack.size() - 1); // pop count
        }
        stack.add("Ljava/lang/Object;");
        break;

      // newarray: pop count, push typed array ref
      case NEWARRAY:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("Ljava/lang/Object;");
        break;

      // multianewarray: pop dimensions items, push ref
      case MULTIANEWARRAY:
        {
          int dims = pos + 3 < code.length ? (code[pos + 3] & 0xFF) : 1;
          for (int d = 0; d < dims && !stack.isEmpty(); d++) {
            stack.remove(stack.size() - 1);
          }
          stack.add("Ljava/lang/Object;");
          break;
        }

      // LDC: push constant
      case LDC:
        {
          int cpIdx = code[pos + 1] & 0xFF;
          String ldcType = constPool.getLdcType(cpIdx);
          stack.add(ldcType != null ? ldcType : "Ljava/lang/Object;");
          break;
        }
      // LDC_W
      case LDC_W:
        {
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String ldcType = constPool.getLdcType(cpIdx);
          stack.add(ldcType != null ? ldcType : "Ljava/lang/Object;");
          break;
        }
      // LDC2_W (long or double)
      case LDC2_W:
        {
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String ldcType = constPool.getLdcType(cpIdx);
          stack.add(ldcType != null ? ldcType : "J");
          break;
        }

      // GETSTATIC: push field type
      case GETSTATIC:
        {
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String desc = constPool.getFieldDescriptor(cpIdx);
          stack.add(descriptorToVerifType(desc));
          break;
        }

      // PUTSTATIC: pop 1
      case PUTSTATIC:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        break;

      // GETFIELD: pop ref, push field type
      case GETFIELD:
        {
          if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);
          }
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String desc = constPool.getFieldDescriptor(cpIdx);
          stack.add(descriptorToVerifType(desc));
          break;
        }

      // PUTFIELD: pop ref + value = 2
      case PUTFIELD:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        break;

      // INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE: pop receiver + args, push return
      case INVOKEVIRTUAL:
      case INVOKESPECIAL:
      case INVOKEINTERFACE:
        {
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String desc = constPool.getMethodDescriptor(cpIdx);
          applyInvokeEffect(stack, desc, true);
          break;
        }

      // INVOKESTATIC, INVOKEDYNAMIC: pop args, push return
      case INVOKESTATIC:
      case INVOKEDYNAMIC:
        {
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String desc = constPool.getMethodDescriptor(cpIdx);
          applyInvokeEffect(stack, desc, false);
          break;
        }

      default:
        break;
    }
  }

  /** Parses a JVM method descriptor and applies its stack effect. */
  private static void applyInvokeEffect(
      List<String> stack, String descriptor, boolean hasReceiver) {
    if (descriptor == null) {
      // Unknown descriptor: conservatively do nothing
      return;
    }
    int parenClose = descriptor.indexOf(')');
    if (parenClose < 0) {
      return;
    }
    // Count arguments (each type descriptor takes exactly 1 stack slot in our model)
    int argCount = countDescriptorArgs(descriptor.substring(1, parenClose));
    if (hasReceiver) {
      argCount++; // receiver
    }
    for (int a = 0; a < argCount && !stack.isEmpty(); a++) {
      stack.remove(stack.size() - 1);
    }
    String retDesc = descriptor.substring(parenClose + 1);
    if (!"V".equals(retDesc)) {
      stack.add(descriptorToVerifType(retDesc));
    }
  }

  /** Counts the number of parameter descriptors in a JVM parameter-list string (without parens). */
  private static int countDescriptorArgs(String params) {
    int count = 0;
    int j = 0;
    while (j < params.length()) {
      char c = params.charAt(j);
      if (c == 'L') {
        int semi = params.indexOf(';', j);
        j = (semi >= 0) ? semi + 1 : params.length();
      } else if (c == '[') {
        j++;
        continue; // array prefix; actual type follows
      } else {
        j++;
      }
      count++;
    }
    return count;
  }

  /**
   * Converts a JVM type descriptor to the corresponding JVM verification type ("I", "J", "F", "D",
   * or a reference type descriptor). Returns "Ljava/lang/Object;" for unknown reference types.
   */
  private static String descriptorToVerifType(String desc) {
    if (desc == null || desc.isEmpty()) {
      return "Ljava/lang/Object;";
    }
    switch (desc.charAt(0)) {
      case 'I':
      case 'B':
      case 'C':
      case 'S':
      case 'Z':
        return "I";
      case 'J':
        return "J";
      case 'F':
        return "F";
      case 'D':
        return "D";
      case 'L':
      case '[':
        return desc;
      default:
        return "Ljava/lang/Object;";
    }
  }

  /**
   * Returns true if local variable {@code slot} is guaranteed initialized when execution reaches
   * bytecode offset {@code target}. A slot is initialized iff its first STORE appears before every
   * path that can reach {@code target} – which we approximate with a per-slot extended cut.
   */
  private static boolean isInitialized(
      int slot,
      int target,
      int paramSlots,
      Map<Integer, Integer> firstStoreOffset,
      Map<Integer, List<Integer>> branchesToTarget,
      Map<Integer, Integer> prevInstrOffset,
      byte[] code,
      Map<Long, Integer> cache) {
    if (slot < paramSlots) {
      return true; // method parameter, always present
    }
    Integer firstStore = firstStoreOffset.get(slot);
    if (firstStore == null) {
      return false; // never stored
    }
    // Per-slot extended cut: the minimum bytecode offset from which {@code target} is reachable
    // without passing through a store to {@code slot}. Propagation stops at unconditional
    // transfers (no fallthrough) and at any store to this specific slot (because the sequential
    // path through such a store guarantees initialization – there is no path that bypasses it
    // at that point in the instruction stream).
    int cut =
        computeExtendedCutForSlot(target, slot, branchesToTarget, prevInstrOffset, code, cache);
    return firstStore < cut;
  }

  /**
   * Computes the per-slot extended cut for {@code target}: the minimum bytecode offset from which
   * {@code target} is reachable on any path that does not first execute a store to {@code slot}.
   *
   * <p>For each direct branch source B → target, recursively computes the cut at B (the minimum
   * offset reachable from B without a store to {@code slot}). This propagates backward through
   * indirect paths (e.g. a conditional branch at B1 → B2 → target, where B1 bypasses the store).
   * Propagation also follows sequential (fallthrough) predecessors, stopping at unconditional
   * transfers or stores to {@code slot}.
   */
  private static int computeExtendedCutForSlot(
      int target,
      int slot,
      Map<Integer, List<Integer>> branchesToTarget,
      Map<Integer, Integer> prevInstrOffset,
      byte[] code,
      Map<Long, Integer> cache) {
    long key = ((long) slot << 17) | target;
    Integer cached = cache.get(key);
    if (cached != null) {
      return cached;
    }
    // Sentinel to break any cycle (loops in control flow)
    cache.put(key, target);

    // Cap at target: a branch source offset ≥ target cannot create a path that bypasses stores
    // that appear before the target in bytecode order.
    int result = target;

    // Recurse into each direct branch source: use the CUT at the source, not just its offset.
    // This correctly propagates through indirect paths such as cond_branch → fallthrough → target.
    for (int src : branchesToTarget.getOrDefault(target, java.util.Collections.emptyList())) {
      result =
          Math.min(
              result,
              computeExtendedCutForSlot(src, slot, branchesToTarget, prevInstrOffset, code, cache));
    }

    // Propagate through sequential (fallthrough) predecessor, stopping at unconditional transfers
    // and at any store to this specific slot.
    Integer prevOff = prevInstrOffset.get(target);
    if (prevOff != null
        && !isUnconditionalTransfer(code, prevOff)
        && !isStoreToSlot(code, prevOff, slot)) {
      result =
          Math.min(
              result,
              computeExtendedCutForSlot(
                  prevOff, slot, branchesToTarget, prevInstrOffset, code, cache));
    }

    cache.put(key, result);
    return result;
  }

  /**
   * Returns true if the instruction at {@code pos} is an unconditional transfer (GOTO, xRETURN,
   * ATHROW).
   */
  private static boolean isUnconditionalTransfer(byte[] code, int pos) {
    byte op = code[pos];
    return op == GOTO || op == GOTO_W || (op >= IRETURN && op <= RETURN) || op == ATHROW;
  }

  /**
   * Returns true if the instruction at {@code pos} stores a value into local variable {@code slot}.
   */
  private static boolean isStoreToSlot(byte[] code, int pos, int slot) {
    byte op = code[pos];
    if (op >= ISTORE_0 && op <= ISTORE_3) return (op - ISTORE_0) == slot; // istore_0..3
    if (op >= LSTORE_0 && op <= LSTORE_3) return (op - LSTORE_0) == slot; // lstore_0..3
    if (op >= FSTORE_0 && op <= FSTORE_3) return (op - FSTORE_0) == slot; // fstore_0..3
    if (op >= DSTORE_0 && op <= DSTORE_3) return (op - DSTORE_0) == slot; // dstore_0..3
    if (op >= ASTORE_0 && op <= ASTORE_3) return (op - ASTORE_0) == slot; // astore_0..3
    if (op >= ISTORE && op <= ASTORE && pos + 1 < code.length) { // xstore slot
      return (code[pos + 1] & 0xFF) == slot;
    }
    return false;
  }

  /**
   * Scans the bytecode and returns a map from local variable slot index to the bytecode offset of
   * the first STORE instruction that writes to that slot.
   */
  private static Map<Integer, Integer> buildFirstStoreMap(byte[] code) {
    Map<Integer, Integer> firstStore = new HashMap<>();
    int i = 0;
    while (i < code.length) {
      byte op = code[i];
      int slot = -1;
      if (op >= ISTORE_0 && op <= ISTORE_3) {
        slot = op - ISTORE_0; // ISTORE_0..ISTORE_3
      } else if (op >= LSTORE_0 && op <= LSTORE_3) {
        slot = op - LSTORE_0; // LSTORE_0..LSTORE_3
      } else if (op >= FSTORE_0 && op <= FSTORE_3) {
        slot = op - FSTORE_0; // FSTORE_0..FSTORE_3
      } else if (op >= DSTORE_0 && op <= DSTORE_3) {
        slot = op - DSTORE_0; // DSTORE_0..DSTORE_3
      } else if (op >= ASTORE_0 && op <= ASTORE_3) {
        slot = op - ASTORE_0; // ASTORE_0..ASTORE_3
      } else if (op >= ISTORE && op <= ASTORE) {
        // xSTORE with explicit slot operand (2-byte instruction)
        if (i + 1 < code.length) {
          slot = code[i + 1] & 0xFF;
        }
      }
      if (slot >= 0) {
        firstStore.putIfAbsent(slot, i);
      }
      i += opcodeLength(code, i);
    }
    return firstStore;
  }

  /** Returns the number of local variable slots occupied by parameters (including 'this'). */
  private static int paramSlotCount(Method method) {
    boolean isStatic = method.accFlags.contains(AccessFlag.ACC_STATIC);
    int slots = isStatic ? 0 : 1;
    if (method.args != null) {
      for (Argument arg : method.args) {
        String t = arg.type;
        slots += ("long".equals(t) || "double".equals(t)) ? 2 : 1;
      }
    }
    return slots;
  }

  /** Returns the byte-length of the instruction at code[pos]. */
  private static int opcodeLength(byte[] code, int pos) {
    switch (code[pos]) {
      case BIPUSH:
      case LDC:
      case ILOAD:
      case LLOAD:
      case FLOAD:
      case DLOAD:
      case ALOAD:
      case ISTORE:
      case LSTORE:
      case FSTORE:
      case DSTORE:
      case ASTORE:
      case RET:
      case NEWARRAY:
        return 2;
      case SIPUSH:
      case LDC_W:
      case LDC2_W:
      case IINC:
      case IFEQ:
      case IFNE:
      case IFLT:
      case IFGE:
      case IFGT:
      case IFLE:
      case IF_ICMPEQ:
      case IF_ICMPNE:
      case IF_ICMPLT:
      case IF_ICMPGE:
      case IF_ICMPGT:
      case IF_ICMPLE:
      case IF_ACMPEQ:
      case IF_ACMPNE:
      case GOTO:
      case JSR:
      case GETSTATIC:
      case PUTSTATIC:
      case GETFIELD:
      case PUTFIELD:
      case INVOKEVIRTUAL:
      case INVOKESPECIAL:
      case INVOKESTATIC:
      case NEW:
      case ANEWARRAY:
      case CHECKCAST:
      case INSTANCEOF:
      case IFNULL:
      case IFNONNULL:
        return 3;
      case MULTIANEWARRAY:
        return 4;
      case INVOKEINTERFACE:
      case INVOKEDYNAMIC:
      case GOTO_W:
      case JSR_W:
        return 5;
      case WIDE:
        return wideOpcodeLength(code, pos);
      case TABLESWITCH:
        return tableswitchLength(code, pos);
      case LOOKUPSWITCH:
        return lookupswitchLength(code, pos);
      default:
        return 1;
    }
  }

  private static int wideOpcodeLength(byte[] code, int pos) {
    if (pos + 1 >= code.length) {
      return 2;
    }
    byte modOp = code[pos + 1];
    return (modOp == IINC) ? 6 : 4; // WIDE IINC = 6; all other WIDE variants = 4
  }

  private static int tableswitchLength(byte[] code, int pos) {
    int padding = (4 - ((pos + 1) % 4)) % 4;
    int base = pos + 1 + padding; // points at default offset (s4)
    if (base + 11 >= code.length) {
      return code.length - pos;
    }
    int low =
        ((code[base + 4] & 0xFF) << 24)
            | ((code[base + 5] & 0xFF) << 16)
            | ((code[base + 6] & 0xFF) << 8)
            | (code[base + 7] & 0xFF);
    int high =
        ((code[base + 8] & 0xFF) << 24)
            | ((code[base + 9] & 0xFF) << 16)
            | ((code[base + 10] & 0xFF) << 8)
            | (code[base + 11] & 0xFF);
    return 1 + padding + 12 + (high - low + 1) * 4;
  }

  private static int lookupswitchLength(byte[] code, int pos) {
    int padding = (4 - ((pos + 1) % 4)) % 4;
    int base = pos + 1 + padding;
    if (base + 7 >= code.length) {
      return code.length - pos;
    }
    int npairs =
        ((code[base + 4] & 0xFF) << 24)
            | ((code[base + 5] & 0xFF) << 16)
            | ((code[base + 6] & 0xFF) << 8)
            | (code[base + 7] & 0xFF);
    return 1 + padding + 8 + npairs * 8;
  }

  private void writeVerificationType(DynamicByteArray out, String type, ConstantPool constPool) {
    if (type == null) {
      out.write((byte) 0); // Top_variable_info
      return;
    }
    switch (type) {
      case "I":
      case "Z":
      case "B":
      case "C":
      case "S":
        out.write((byte) 1); // Integer_variable_info
        break;
      case "F":
        out.write((byte) 2); // Float_variable_info
        break;
      case "D":
        out.write((byte) 3); // Double_variable_info
        break;
      case "J":
        out.write((byte) 4); // Long_variable_info
        break;
      default:
        // Object_variable_info: tag 7 + u2 constant pool class index
        String className;
        if (type.startsWith("L") && type.endsWith(";")) {
          className = type.substring(1, type.length() - 1);
        } else if (type.startsWith("[")) {
          className = type; // array descriptor is the class name
        } else {
          className = type.replace('.', '/');
        }
        constPool.addClass(className);
        out.write((byte) 7);
        out.write(constPool.indexOf(CONSTANT_CLASS, className));
        break;
    }
  }
}
