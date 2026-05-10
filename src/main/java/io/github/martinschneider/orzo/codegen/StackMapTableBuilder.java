package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

  // sorted map: absolute bytecode offset → ordered list of JVM type descriptors for locals
  private final TreeMap<Integer, List<String>> frames = new TreeMap<>();

  /** Record a stack-map frame at the given absolute bytecode offset within the method. */
  private void addFrame(int offset, List<String> localTypes) {
    if (offset >= 0) {
      frames.put(offset, new ArrayList<>(localTypes));
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
    for (java.util.Map.Entry<Integer, List<String>> entry : frames.entrySet()) {
      int offset = entry.getKey();
      List<String> locals = entry.getValue();
      int offsetDelta = (previousOffset == -1) ? offset : (offset - previousOffset - 1);

      content.write((byte) 255); // full_frame tag
      content.write((short) offsetDelta); // offset_delta
      content.write((short) locals.size()); // number_of_locals
      for (String localType : locals) {
        writeVerificationType(content, localType, constPool);
      }
      content.write((short) 0); // number_of_stack_items = 0 (stack always empty at branch targets)

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
   * and emits a StackMapTable with full_frame entries using the final local variable snapshot.
   *
   * <p>Using the final localMap state works correctly for methods whose local variable set is the
   * same at every branch target (no variables declared inside loop bodies). This covers the vast
   * majority of Orzo's generated code.
   */
  public static byte[] buildFromBytecode(
      byte[] code, Method method, CGContext ctx, ConstantPool constPool) {
    if (code == null || code.length == 0) {
      return null;
    }
    List<String> locals = snapshotLocals(method, ctx);
    java.util.TreeSet<Integer> targets = new java.util.TreeSet<>();

    int i = 0;
    while (i < code.length) {
      int op = code[i] & 0xFF;
      // Conditional branches: opcodes 153–167 (IFEQ..GOTO), 198 (IFNULL), 199 (IFNONNULL)
      if ((op >= 153 && op <= 167) || op == 198 || op == 199) {
        if (i + 2 < code.length) {
          short branchOffset = (short) (((code[i + 1] & 0xFF) << 8) | (code[i + 2] & 0xFF));
          int target = i + branchOffset;
          if (target >= 0 && target < code.length) {
            targets.add(target);
          }
        }
      } else if (op == 200 || op == 201) { // GOTO_W, JSR_W – 5-byte, s4 offset
        if (i + 4 < code.length) {
          int branchOffset =
              ((code[i + 1] & 0xFF) << 24)
                  | ((code[i + 2] & 0xFF) << 16)
                  | ((code[i + 3] & 0xFF) << 8)
                  | (code[i + 4] & 0xFF);
          int target = i + branchOffset;
          if (target >= 0 && target < code.length) {
            targets.add(target);
          }
        }
      }
      i += opcodeLength(code, i);
    }

    if (targets.isEmpty()) {
      return null;
    }

    StackMapTableBuilder builder = new StackMapTableBuilder();
    for (int target : targets) {
      builder.addFrame(target, locals);
    }
    return builder.build(constPool);
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
