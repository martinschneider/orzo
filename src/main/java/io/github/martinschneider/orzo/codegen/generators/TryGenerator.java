package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.ByteUtils;
import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.CatchBlock;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.ThrowStatement;
import io.github.martinschneider.orzo.parser.productions.TryStatement;
import java.util.ArrayList;
import java.util.List;

public class TryGenerator implements StatementGenerator<TryStatement> {
  private CGContext ctx;

  public TryGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Adjusts exception table entries added during sub-buffer generation so their PCs are relative to
   * the parent buffer instead of the sub-buffer. Called by IfGenerator, WhileGenerator,
   * ForGenerator, and DoGenerator before writing sub-buffer bytes to the parent output.
   */
  static void adjustExceptionTableEntries(CGContext ctx, int fromIdx, int toIdx, int baseOffset) {
    for (int i = fromIdx; i < toIdx; i++) {
      int[] e = ctx.exceptionTable.get(i);
      String type = ctx.exceptionHandlerType.remove(e[2]);
      e[0] += baseOffset;
      e[1] += baseOffset;
      e[2] += baseOffset;
      if (type != null) {
        ctx.exceptionHandlerType.put(e[2], type);
      }
    }
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, TryStatement stmt) {
    int startPc = out.size();

    // Generate try body
    for (Statement s : stmt.tryBody) {
      ctx.delegator.generate(out, method, s);
    }

    int endPc = out.size(); // exclusive end of protected region

    // Only write GOTO to skip catch handlers if try body doesn't already return
    boolean tryBodyReturns = endsWithReturn(stmt.tryBody);
    int tryGotoOffsetPos = -1;
    if (!tryBodyReturns) {
      out.write(GOTO);
      tryGotoOffsetPos = out.size(); // position of the 2-byte offset placeholder
      out.write((short) 0);
    }

    // Track GOTO positions that need to be patched for inter-catch jumps
    List<Integer> catchGotoOffsetPositions = new ArrayList<>();

    for (int ci = 0; ci < stmt.catchBlocks.size(); ci++) {
      CatchBlock catchBlock = stmt.catchBlocks.get(ci);
      int handlerPc = out.size();

      // Resolve exception class names (several for a multi-catch)
      List<String> jvmExTypes = new ArrayList<>();
      for (String exceptionType : catchBlock.exceptionTypes) {
        String jvmType = resolveExceptionType(exceptionType);
        ctx.constPool.addClass(jvmType);
        jvmExTypes.add(jvmType);
      }
      // the catch variable's type: the closest common superclass of all caught types
      String jvmExType = commonSuperclass(jvmExTypes);

      // Allocate local variable slot for the catch variable
      short catchVarSlot = (short) ctx.classIdMap.variables.localSize;
      ctx.classIdMap.variables.putLocal(
          Token.id(catchBlock.varName),
          new VariableInfo(
              catchBlock.varName, jvmExType, null, emptyList(), false, catchVarSlot, null));

      // Store exception object in the catch variable slot
      storeRef(out, catchVarSlot);

      // Generate catch body
      for (Statement s : catchBlock.body) {
        ctx.delegator.generate(out, method, s);
      }

      // Add GOTO to skip remaining handlers if this catch block doesn't return
      if (ci < stmt.catchBlocks.size() - 1 && !endsWithReturn(catchBlock.body)) {
        out.write(GOTO);
        catchGotoOffsetPositions.add(out.size()); // position of the 2-byte placeholder
        out.write((short) 0);
      }

      // Record exception table entries: {startPc, endPc, handlerPc, catchTypeIdx}
      for (String jvmType : jvmExTypes) {
        short catchTypeIdx = ctx.constPool.indexOf(CONSTANT_CLASS, jvmType);
        ctx.exceptionTable.add(new int[] {startPc, endPc, handlerPc, catchTypeIdx & 0xFFFF});
      }
      ctx.exceptionHandlerType.put(handlerPc, jvmExType);
    }

    int afterPc = out.size();

    // Patch the try-body GOTO offset (if one was written)
    if (tryGotoOffsetPos >= 0) {
      int gotoInstrPc = tryGotoOffsetPos - 1;
      short tryGotoOffset = (short) (afterPc - gotoInstrPc);
      out.patch(tryGotoOffsetPos, ByteUtils.shortToByteArray(tryGotoOffset));
    }

    // Patch inter-catch GOTO offsets
    for (int gotoOffsetPos : catchGotoOffsetPositions) {
      int gotoInstrPc = gotoOffsetPos - 1; // GOTO opcode is 1 byte before offset bytes
      short catchGotoOffset = (short) (afterPc - gotoInstrPc);
      out.patch(gotoOffsetPos, ByteUtils.shortToByteArray(catchGotoOffset));
    }

    return out;
  }

  private boolean endsWithReturn(List<Statement> body) {
    if (body == null || body.isEmpty()) {
      return false;
    }
    Statement last = body.get(body.size() - 1);
    return last instanceof ReturnStatement || last instanceof ThrowStatement;
  }

  private void storeRef(DynamicByteArray out, short slot) {
    switch (slot) {
      case 0:
        out.write(ASTORE_0);
        break;
      case 1:
        out.write(ASTORE_1);
        break;
      case 2:
        out.write(ASTORE_2);
        break;
      case 3:
        out.write(ASTORE_3);
        break;
      default:
        out.write(ASTORE);
        out.write((byte) slot);
    }
  }

  private String commonSuperclass(List<String> jvmTypes) {
    String result = jvmTypes.get(0);
    if (jvmTypes.size() == 1) {
      return result;
    }
    try {
      Class<?> common = Class.forName(result.replace('/', '.'));
      for (int i = 1; i < jvmTypes.size(); i++) {
        Class<?> other = Class.forName(jvmTypes.get(i).replace('/', '.'));
        while (!common.isAssignableFrom(other)) {
          common = common.getSuperclass();
        }
      }
      return common.getName().replace('.', '/');
    } catch (ClassNotFoundException e) {
      return "java/lang/Throwable";
    }
  }

  private String resolveExceptionType(String simpleType) {
    if (simpleType.contains(".")) {
      return simpleType.replace('.', '/');
    }
    try {
      Class.forName("java.lang." + simpleType);
      return "java/lang/" + simpleType;
    } catch (ClassNotFoundException e) {
      // not in java.lang
    }
    // Check imports
    if (ctx.clazz != null && ctx.clazz.imports != null) {
      for (io.github.martinschneider.orzo.parser.productions.Import imp : ctx.clazz.imports) {
        if (!imp.isStatic && imp.id != null) {
          if (imp.id.endsWith(".*")) {
            String pkg = imp.id.substring(0, imp.id.length() - 2);
            try {
              Class.forName(pkg + "." + simpleType);
              return (pkg + "." + simpleType).replace('.', '/');
            } catch (ClassNotFoundException e) {
              // not in this wildcard-imported package
            }
            continue;
          }
          String simple =
              imp.id.contains(".") ? imp.id.substring(imp.id.lastIndexOf('.') + 1) : imp.id;
          if (simple.equals(simpleType)) {
            return imp.id.replace('.', '/');
          }
        }
      }
    }
    return simpleType.replace('.', '/');
  }
}
