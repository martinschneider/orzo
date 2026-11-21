package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.ATHROW;
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

  private static final String THROWABLE = "java/lang/Throwable";

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
    List<Statement> finallyBody = stmt.finallyBody;
    if (finallyBody != null) {
      // a return inside the try or catch blocks must run the finally block first
      ctx.finallyStack.push(finallyBody);
    }
    int startPc = out.size();

    // Generate try body
    for (Statement s : stmt.tryBody) {
      ctx.delegator.generate(out, method, s);
    }

    int endPc = out.size(); // exclusive end of protected region
    // Regions covered by the finally block's catch-all handler: the try body and every catch body
    List<int[]> finallyRanges = new ArrayList<>();
    if (endPc > startPc) {
      finallyRanges.add(new int[] {startPc, endPc});
    }

    // GOTOs to patch to the end of the whole statement (positions of their 2-byte offsets)
    List<Integer> gotoOffsetPositions = new ArrayList<>();

    // Normal completion of the try body: run finally, skip the handlers
    if (!endsWithReturn(stmt.tryBody)) {
      inlineFinally(out, method, finallyBody);
      out.write(GOTO);
      gotoOffsetPositions.add(out.size());
      out.write((short) 0);
    }

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
      if (finallyBody != null) {
        finallyRanges.add(new int[] {handlerPc, out.size()});
      }

      // Skip the remaining handlers (and the finally block's catch-all handler) unless this
      // catch block returns; the last one falls through to the end if there's no finally.
      if (!endsWithReturn(catchBlock.body)) {
        inlineFinally(out, method, finallyBody);
        if (ci < stmt.catchBlocks.size() - 1 || finallyBody != null) {
          out.write(GOTO);
          gotoOffsetPositions.add(out.size());
          out.write((short) 0);
        }
      }

      // Record exception table entries: {startPc, endPc, handlerPc, catchTypeIdx}
      for (String jvmType : jvmExTypes) {
        short catchTypeIdx = ctx.constPool.indexOf(CONSTANT_CLASS, jvmType);
        ctx.exceptionTable.add(new int[] {startPc, endPc, handlerPc, catchTypeIdx & 0xFFFF});
      }
      ctx.exceptionHandlerType.put(handlerPc, jvmExType);
    }

    if (finallyBody != null) {
      ctx.finallyStack.pop();
      // Catch-all handler: save the exception, run finally, rethrow
      int anyHandlerPc = out.size();
      short tmpSlot = (short) ctx.classIdMap.variables.localSize;
      String tmpName = "$finally_" + tmpSlot;
      ctx.classIdMap.variables.putLocal(
          Token.id(tmpName),
          new VariableInfo(tmpName, THROWABLE, null, emptyList(), false, tmpSlot, null));
      storeRef(out, tmpSlot);
      for (Statement s : finallyBody) {
        ctx.delegator.generate(out, method, s);
      }
      ctx.loadGen.loadReference(out, tmpSlot);
      out.write(ATHROW);
      for (int[] range : finallyRanges) {
        ctx.exceptionTable.add(new int[] {range[0], range[1], anyHandlerPc, 0});
      }
      ctx.exceptionHandlerType.put(anyHandlerPc, THROWABLE);
    }

    int afterPc = out.size();
    for (int gotoOffsetPos : gotoOffsetPositions) {
      int gotoInstrPc = gotoOffsetPos - 1; // GOTO opcode is 1 byte before offset bytes
      out.patch(gotoOffsetPos, ByteUtils.shortToByteArray((short) (afterPc - gotoInstrPc)));
    }
    return out;
  }

  // Generates a copy of the finally block (if any) at the current position, e.g. at the end of the
  // try body. Its own returns must not run it again, so it's taken off the finally stack meanwhile.
  private void inlineFinally(DynamicByteArray out, Method method, List<Statement> finallyBody) {
    if (finallyBody == null) {
      return;
    }
    ctx.finallyStack.pop();
    for (Statement s : finallyBody) {
      ctx.delegator.generate(out, method, s);
    }
    ctx.finallyStack.push(finallyBody);
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
