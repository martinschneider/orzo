package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFNE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ACMPEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKEVIRTUAL;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.parser.productions.Break;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.SwitchCase;
import io.github.martinschneider.orzo.parser.productions.SwitchStatement;
import java.util.ArrayList;
import java.util.List;

public class SwitchGenerator implements StatementGenerator<SwitchStatement> {
  private CGContext ctx;

  private static final String EQUALS_DESCR = "(Ljava/lang/Object;)Z";

  public SwitchGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, SwitchStatement switchStmt) {
    String switchType = new NumExprTypeDecider(ctx).getType(ctx.classIdMap, switchStmt.switchExpr);

    // Allocate temp local for switch expression value
    short tmpSlot = (short) ctx.classIdMap.variables.localSize;
    String tmpName = "$switch_" + tmpSlot;
    ctx.classIdMap.variables.putLocal(
        id(tmpName), new VariableInfo(tmpName, switchType, emptyList(), false, tmpSlot, null));

    // Evaluate switch expression and store in temp slot
    ctx.exprGen.eval(out, null, switchStmt.switchExpr);
    ctx.storeGen.store(out, ctx.classIdMap.variables.get(id(tmpName)));

    // Find default case index
    int defaultIdx = -1;
    for (int i = 0; i < switchStmt.cases.size(); i++) {
      SwitchCase c = switchStmt.cases.get(i);
      if (c.labels == null || c.labels.isEmpty()) {
        defaultIdx = i;
        break;
      }
    }

    boolean isStringSwitch = STRING.equals(switchType);
    // Enum (or any other reference-typed) switch subject: the case labels are loaded as object
    // references (e.g. Color.RED), so comparing them needs reference equality (IF_ACMPEQ), not
    // IF_ICMPEQ — enum constants are singletons, so reference comparison is also semantically
    // correct, with no need to go via .ordinal()/.name().
    boolean isRefSwitch = !isStringSwitch && !TypeUtils.isPrimitive(switchType);

    // Emit check blocks: for each non-default case, for each label → IFNE/IF_ICMPEQ placeholder
    // Record (absolute position of IFNE opcode, target case index)
    List<int[]> checkPatches = new ArrayList<>(); // {opcode_pos, case_index}
    for (int i = 0; i < switchStmt.cases.size(); i++) {
      SwitchCase c = switchStmt.cases.get(i);
      if (c.labels == null || c.labels.isEmpty()) {
        continue; // default: no check needed
      }
      for (Expression label : c.labels) {
        if (isStringSwitch) {
          ctx.loadGen.loadReference(out, tmpSlot);
          ctx.exprGen.eval(out, null, label);
          emitStringEquals(out);
          checkPatches.add(new int[] {out.size(), i});
          out.write(IFNE);
          out.write((short) 0); // placeholder
        } else {
          // int/byte/short/char switch: load tmp and label, compare with if_icmpeq.
          // enum (or other reference-typed) switch: compare the loaded references directly
          // with if_acmpeq instead.
          ctx.loadGen.load(out, ctx.classIdMap.variables.get(id(tmpName)));
          ctx.exprGen.eval(out, switchType, label);
          checkPatches.add(new int[] {out.size(), i});
          out.write(isRefSwitch ? IF_ACMPEQ : IF_ICMPEQ);
          out.write((short) 0); // placeholder
        }
      }
    }

    // No-match: always GOTO default body (or end if no default)
    int noMatchGotoPos = out.size();
    out.write(GOTO);
    out.write((short) 0); // placeholder → defaultBody or end

    // Emit case bodies
    int[] bodyStarts = new int[switchStmt.cases.size()];
    List<Integer> gotoEndPositions = new ArrayList<>();
    // Breaks belong to the nearest enclosing loop/switch: snapshot how many are pending before
    // generating this switch's bodies so only breaks introduced here get redirected to its end.
    int pendingBreaksBefore = out.pendingBreaks.size();

    for (int i = 0; i < switchStmt.cases.size(); i++) {
      bodyStarts[i] = out.size();
      SwitchCase c = switchStmt.cases.get(i);
      if (c.body != null) {
        for (Statement s : c.body) {
          ctx.delegator.generate(out, method, s);
        }
      }

      if (!bodyEndsWithReturn(c.body) && !bodyEndsWithBreak(c.body)) {
        gotoEndPositions.add(out.size()); // GOTO opcode position
        out.write(GOTO);
        out.write((short) 0); // placeholder → end
      }
    }

    int endPos = out.size();

    // Patch IFNE/IF_ICMPEQ → bodyStart[case_index]
    for (int[] patch : checkPatches) {
      int opcodePos = patch[0];
      int caseIdx = patch[1];
      short offset = (short) (bodyStarts[caseIdx] - opcodePos);
      out.patch(opcodePos + 1, shortToByteArray(offset));
    }

    // Patch no-match GOTO → default body or end
    int noMatchTarget = (defaultIdx >= 0) ? bodyStarts[defaultIdx] : endPos;
    out.patch(noMatchGotoPos + 1, shortToByteArray((short) (noMatchTarget - noMatchGotoPos)));

    // Patch break GOTOs introduced by this switch's case bodies → end; breaks pending from an
    // outer loop/switch (recorded before this switch started) are left untouched.
    List<Integer> ownBreaks =
        new ArrayList<>(out.pendingBreaks.subList(pendingBreaksBefore, out.pendingBreaks.size()));
    for (int brkPos : ownBreaks) {
      out.patch(brkPos + 1, shortToByteArray((short) (endPos - brkPos)));
    }
    out.pendingBreaks.subList(pendingBreaksBefore, out.pendingBreaks.size()).clear();

    // Patch body-end GOTOs → end
    for (int gotoPos : gotoEndPositions) {
      out.patch(gotoPos + 1, shortToByteArray((short) (endPos - gotoPos)));
    }

    return out;
  }

  private void emitStringEquals(DynamicByteArray out) {
    ctx.constPool.addClass("java/lang/String");
    ctx.constPool.addMethodRef("java/lang/String", "equals", EQUALS_DESCR);
    out.write(INVOKEVIRTUAL);
    out.write(
        ctx.constPool.indexOf(CONSTANT_METHODREF, "java/lang/String", "equals", EQUALS_DESCR));
    ctx.opStack.pop(2);
    ctx.opStack.push(INT);
  }

  private boolean bodyEndsWithReturn(List<Statement> body) {
    return body != null && !body.isEmpty() && body.get(body.size() - 1) instanceof ReturnStatement;
  }

  private boolean bodyEndsWithBreak(List<Statement> body) {
    return body != null && !body.isEmpty() && body.get(body.size() - 1) instanceof Break;
  }
}
