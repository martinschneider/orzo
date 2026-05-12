package io.github.martinschneider.orzo.codegen;

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
 * serialises them into the StackMapTable attribute format (JVM spec §4.7.4). Every frame uses
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
      int op = code[i] & 0xFF;
      if ((op >= 153 && op <= 167) || op == 198 || op == 199) {
        if (i + 2 < code.length) {
          short branchOffset = (short) (((code[i + 1] & 0xFF) << 8) | (code[i + 2] & 0xFF));
          int target = i + branchOffset;
          if (target >= 0 && target < code.length) {
            targets.add(target);
            branchesToTarget.computeIfAbsent(target, k -> new ArrayList<>()).add(i);
          }
        }
        if (op == 167) { // GOTO: instruction-after-GOTO also needs a frame
          int next = i + 3;
          if (next < code.length) {
            targets.add(next);
          }
        }
      } else if ((op >= 172 && op <= 177) || op == 191) {
        // xRETURN (172-177) / ATHROW (191): instruction after unconditional transfer needs a frame
        int next = i + 1;
        if (next < code.length) {
          targets.add(next);
        }
      } else if (op == 200 || op == 201) {
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
        if (op == 200) {
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
    for (int[] entry : ctx.exceptionTable) {
      int handlerPc = entry[2];
      String handlerType = ctx.exceptionHandlerType.get(handlerPc);
      if (handlerType != null) {
        targets.add(handlerPc);
        List<String> handlerStack = new ArrayList<>();
        handlerStack.add("L" + handlerType + ";");
        branchTargetStacks.put(handlerPc, handlerStack);
      }
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

      // Determine the highest initialized slot so we know when to stop emitting locals.
      int maxInitSlot = -1;
      for (VariableInfo var : sorted) {
        int slot = (int) var.idx;
        boolean initialized =
            isInitialized(
                slot,
                target,
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
                  target,
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

      int op = code[i] & 0xFF;

      // Conditional branches: pop comparison operands, record target stack
      if ((op >= 153 && op <= 158) || op == 198 || op == 199) {
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
      } else if ((op >= 159 && op <= 166)) {
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
      } else if (op == 167) { // GOTO
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
      } else if (op == 200) { // GOTO_W
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
      } else if ((op >= 172 && op <= 177) || op == 191) {
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
      int op, byte[] code, int pos, List<String> stack, ConstantPool constPool) {
    switch (op) {
      case 0: // nop
      case 132: // iinc
        break;

      // Push ref
      case 1: // aconst_null
      case 42:
      case 43:
      case 44:
      case 45: // aload_0..3
      case 25: // aload (wide form handled separately)
      case 50: // aaload (pops ref+I, pushes ref)
      case 89:
      case 90:
      case 91:
      case 92:
      case 93:
      case 94: // dup variants
        // For dup: push a copy of the top (type unknown without full stack view, use Object)
        if (op >= 89 && op <= 94) {
          if (!stack.isEmpty()) {
            stack.add(stack.get(stack.size() - 1));
          } else {
            stack.add("Ljava/lang/Object;");
          }
        } else if (op == 50) { // aaload
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
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8: // iconst_m1..5
      case 16:
      case 17: // bipush, sipush
      case 21:
      case 26:
      case 27:
      case 28:
      case 29: // iload, iload_0..3
      case 46: // iaload
      case 51:
      case 52:
      case 53: // baload, caload, saload
      case 190: // arraylength
      case 193: // instanceof
        if (op == 46 || op == 51 || op == 52 || op == 53) { // xaload: pop ref+I
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
        } else if (op == 190 || op == 193) { // arraylength, instanceof: pop 1
          if (!stack.isEmpty()) {
            stack.remove(stack.size() - 1);
          }
        }
        stack.add("I");
        break;

      // Push J
      case 9:
      case 10: // lconst_0..1
      case 22:
      case 30:
      case 31:
      case 32:
      case 33: // lload, lload_0..3
      case 47: // laload: pop ref+I, push J
        if (op == 47) {
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
        }
        stack.add("J");
        break;

      // Push F
      case 11:
      case 12:
      case 13: // fconst_0..2
      case 23:
      case 34:
      case 35:
      case 36:
      case 37: // fload, fload_0..3
      case 48: // faload: pop ref+I, push F
        if (op == 48) {
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
        }
        stack.add("F");
        break;

      // Push D
      case 14:
      case 15: // dconst_0..1
      case 24:
      case 38:
      case 39:
      case 40:
      case 41: // dload, dload_0..3
      case 49: // daload: pop ref+I, push D
        if (op == 49) {
          if (stack.size() >= 2) {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
          }
        }
        stack.add("D");
        break;

      // Pop 1 (store, return, throw)
      case 54:
      case 59:
      case 60:
      case 61:
      case 62: // istore, istore_0..3
      case 55:
      case 63:
      case 64:
      case 65:
      case 66: // lstore, lstore_0..3
      case 56:
      case 67:
      case 68:
      case 69:
      case 70: // fstore, fstore_0..3
      case 57:
      case 71:
      case 72:
      case 73:
      case 74: // dstore, dstore_0..3
      case 58:
      case 75:
      case 76:
      case 77:
      case 78: // astore, astore_0..3
      case 87: // pop
      case 172:
      case 173:
      case 174:
      case 175:
      case 176: // ireturn..areturn
      case 191: // athrow
      case 194:
      case 195: // monitorenter, monitorexit
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        break;

      case 177: // return
        break;

      // pop2
      case 88:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        } else if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        break;

      // iadd, isub, imul, idiv, irem: pop 2, push I
      case 96:
      case 100:
      case 104:
      case 108:
      case 112:
      case 120:
      case 122:
      case 124: // ishl, ishr, iushr
      case 126:
      case 128:
      case 130: // iand, ior, ixor
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;

      // ineg: pop I, push I
      case 116:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;

      // ladd, lsub, lmul, ldiv, lrem, land, lor, lxor: pop 2, push J
      case 97:
      case 101:
      case 105:
      case 109:
      case 113:
      case 127:
      case 129:
      case 131:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;

      // lshl, lshr, lushr: pop J+I, push J
      case 121:
      case 123:
      case 125:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;

      // lneg: pop J, push J
      case 117:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;

      // fadd, fsub, fmul, fdiv, frem: pop 2, push F
      case 98:
      case 102:
      case 106:
      case 110:
      case 114:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;

      // fneg: pop F, push F
      case 118:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;

      // dadd, dsub, dmul, ddiv, drem: pop 2, push D
      case 99:
      case 103:
      case 107:
      case 111:
      case 115:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;

      // dneg: pop D, push D
      case 119:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;

      // lcmp, fcmpl/g, dcmpl/g: pop 2, push I
      case 148:
      case 149:
      case 150:
      case 151:
      case 152:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;

      // i2l
      case 133:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;
      // i2f
      case 134:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;
      // i2d
      case 135:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;
      // l2i
      case 136:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;
      // l2f
      case 137:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;
      // l2d
      case 138:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;
      // f2i
      case 139:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;
      // f2l
      case 140:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;
      // f2d
      case 141:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("D");
        break;
      // d2i
      case 142:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;
      // d2l
      case 143:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("J");
        break;
      // d2f
      case 144:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("F");
        break;
      // i2b, i2c, i2s
      case 145:
      case 146:
      case 147:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("I");
        break;

      // Array stores: pop ref+I+value = 3 items
      case 79:
      case 80:
      case 81:
      case 82:
      case 83:
      case 84:
      case 85:
      case 86: // iastore..sastore (79-86)
        if (stack.size() >= 3) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        break;

      // swap: exchange top two
      case 95:
        if (stack.size() >= 2) {
          String a = stack.remove(stack.size() - 1);
          String b = stack.remove(stack.size() - 1);
          stack.add(a);
          stack.add(b);
        }
        break;

      // checkcast: no stack change (just verifies)
      case 192:
        break;

      // new: push ref
      case 187:
      case 189: // anewarray
        if (!stack.isEmpty() && op == 189) {
          stack.remove(stack.size() - 1); // pop count
        }
        stack.add("Ljava/lang/Object;");
        break;

      // newarray: pop count, push typed array ref
      case 188:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        stack.add("Ljava/lang/Object;");
        break;

      // multianewarray: pop dimensions items, push ref
      case 197:
        {
          int dims = pos + 3 < code.length ? (code[pos + 3] & 0xFF) : 1;
          for (int d = 0; d < dims && !stack.isEmpty(); d++) {
            stack.remove(stack.size() - 1);
          }
          stack.add("Ljava/lang/Object;");
          break;
        }

      // LDC: push constant
      case 18:
        {
          int cpIdx = code[pos + 1] & 0xFF;
          String ldcType = constPool.getLdcType(cpIdx);
          stack.add(ldcType != null ? ldcType : "Ljava/lang/Object;");
          break;
        }
      // LDC_W
      case 19:
        {
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String ldcType = constPool.getLdcType(cpIdx);
          stack.add(ldcType != null ? ldcType : "Ljava/lang/Object;");
          break;
        }
      // LDC2_W (long or double)
      case 20:
        {
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String ldcType = constPool.getLdcType(cpIdx);
          stack.add(ldcType != null ? ldcType : "J");
          break;
        }

      // GETSTATIC: push field type
      case 178:
        {
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String desc = constPool.getFieldDescriptor(cpIdx);
          stack.add(descriptorToVerifType(desc));
          break;
        }

      // PUTSTATIC: pop 1
      case 179:
        if (!stack.isEmpty()) {
          stack.remove(stack.size() - 1);
        }
        break;

      // GETFIELD: pop ref, push field type
      case 180:
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
      case 181:
        if (stack.size() >= 2) {
          stack.remove(stack.size() - 1);
          stack.remove(stack.size() - 1);
        }
        break;

      // INVOKEVIRTUAL, INVOKESPECIAL, INVOKEINTERFACE: pop receiver + args, push return
      case 182:
      case 183:
      case 185:
        {
          int cpIdx = ((code[pos + 1] & 0xFF) << 8) | (code[pos + 2] & 0xFF);
          String desc = constPool.getMethodDescriptor(cpIdx);
          applyInvokeEffect(stack, desc, true);
          break;
        }

      // INVOKESTATIC, INVOKEDYNAMIC: pop args, push return
      case 184:
      case 186:
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
    int op = code[pos] & 0xFF;
    return op == 167 || op == 200 || (op >= 172 && op <= 177) || op == 191;
  }

  /**
   * Returns true if the instruction at {@code pos} stores a value into local variable {@code slot}.
   */
  private static boolean isStoreToSlot(byte[] code, int pos, int slot) {
    int op = code[pos] & 0xFF;
    if (op >= 59 && op <= 62) return (op - 59) == slot; // istore_0..3
    if (op >= 63 && op <= 66) return (op - 63) == slot; // lstore_0..3
    if (op >= 67 && op <= 70) return (op - 67) == slot; // fstore_0..3
    if (op >= 71 && op <= 74) return (op - 71) == slot; // dstore_0..3
    if (op >= 75 && op <= 78) return (op - 75) == slot; // astore_0..3
    if (op >= 54 && op <= 58 && pos + 1 < code.length) { // xstore slot
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
      int op = code[i] & 0xFF;
      int slot = -1;
      if (op >= 59 && op <= 62) {
        slot = op - 59; // ISTORE_0..ISTORE_3
      } else if (op >= 63 && op <= 66) {
        slot = op - 63; // LSTORE_0..LSTORE_3
      } else if (op >= 67 && op <= 70) {
        slot = op - 67; // FSTORE_0..FSTORE_3
      } else if (op >= 71 && op <= 74) {
        slot = op - 71; // DSTORE_0..DSTORE_3
      } else if (op >= 75 && op <= 78) {
        slot = op - 75; // ASTORE_0..ASTORE_3
      } else if (op >= 54 && op <= 58) {
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
    int op = code[pos] & 0xFF;
    switch (op) {
      // 2-byte instructions
      case 16: // BIPUSH
      case 18: // LDC
      case 21:
      case 22:
      case 23:
      case 24:
      case 25: // xLOAD (generic)
      case 54:
      case 55:
      case 56:
      case 57:
      case 58: // xSTORE (generic)
      case 169: // RET
      case 188: // NEWARRAY
        return 2;

      // 3-byte instructions
      case 17: // SIPUSH
      case 19: // LDC_W
      case 20: // LDC2_W
      case 132: // IINC
      case 153:
      case 154:
      case 155:
      case 156:
      case 157:
      case 158: // IFEQ..IFLE
      case 159:
      case 160:
      case 161:
      case 162:
      case 163:
      case 164: // IF_ICMPEQ..IF_ICMPLE
      case 165:
      case 166: // IF_ACMPEQ, IF_ACMPNE
      case 167: // GOTO
      case 168: // JSR
      case 178:
      case 179:
      case 180:
      case 181: // GETSTATIC..PUTFIELD
      case 182:
      case 183:
      case 184: // INVOKEVIRTUAL..INVOKESTATIC
      case 187: // NEW
      case 189: // ANEWARRAY
      case 192: // CHECKCAST
      case 193: // INSTANCEOF
      case 198: // IFNULL
      case 199: // IFNONNULL
        return 3;

      // 4-byte instructions
      case 197: // MULTIANEWARRAY (u2 class_index + u1 dimensions)
        return 4;

      // 5-byte instructions
      case 185: // INVOKEINTERFACE
      case 186: // INVOKEDYNAMIC
      case 200: // GOTO_W
      case 201: // JSR_W
        return 5;

      // Variable-length
      case 196: // WIDE
        return wideOpcodeLength(code, pos);
      case 170: // TABLESWITCH
        return tableswitchLength(code, pos);
      case 171: // LOOKUPSWITCH
        return lookupswitchLength(code, pos);

      default:
        return 1;
    }
  }

  private static int wideOpcodeLength(byte[] code, int pos) {
    if (pos + 1 >= code.length) {
      return 2;
    }
    int modOp = code[pos + 1] & 0xFF;
    return (modOp == 132) ? 6 : 4; // WIDE IINC = 6; all other WIDE variants = 4
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
