package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.CHECKCAST;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFEQ;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.ForEachStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.Collections;

public class ForEachGenerator implements StatementGenerator<ForEachStatement> {
  private CGContext ctx;

  private static final String ITERATOR_CLASS = "java/util/Iterator";
  private static final String ITERABLE_CLASS = "java/lang/Iterable";

  public ForEachGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ForEachStatement stmt) {
    // Allocate synthetic iterator variable before body (so it gets a stable slot)
    String iterName = "$iter_" + ctx.classIdMap.variables.localSize;
    Identifier iterId = id(iterName);
    short iterSlot = (short) ctx.classIdMap.variables.localSize;
    ctx.classIdMap.variables.putLocal(
        iterId, new VariableInfo(iterName, ITERATOR_CLASS, emptyList(), false, iterSlot, null));

    // Setup: evaluate iterable, call .iterator(), store result in $iter
    Method iteratorMethod =
        new Method(ITERABLE_CLASS, "iterator", ITERATOR_CLASS, Collections.emptyList());
    ctx.exprGen.eval(out, null, stmt.iterable);
    ctx.invokeGen.invokeInterface(out, iteratorMethod);
    ctx.storeGen.store(out, ctx.classIdMap.variables.get(iterId));

    // Build bodyOut first so we know its size for the IFEQ branch offset
    DynamicByteArray bodyOut = new DynamicByteArray();

    // Load $iter and call .next() to get the next element
    Method nextMethod =
        new Method(ITERATOR_CLASS, "next", "java/lang/Object", Collections.emptyList());
    ctx.loadGen.load(bodyOut, ctx.classIdMap.variables.get(iterId));
    ctx.invokeGen.invokeInterface(bodyOut, nextMethod);

    // CHECKCAST to the declared element type (resolve simple names like "String" to JVM internal
    // names)
    String descriptor = TypeUtils.descr(stmt.elemType);
    String jvmElemType =
        (descriptor.startsWith("L") && descriptor.endsWith(";"))
            ? descriptor.substring(1, descriptor.length() - 1)
            : stmt.elemType.replace('.', '/');
    ctx.constPool.addClass(jvmElemType);
    bodyOut.write(CHECKCAST);
    bodyOut.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, jvmElemType));

    // Store the cast element into the loop variable (allocates slot if needed)
    ctx.assignGen.assign(bodyOut, ctx.classIdMap.variables, stmt.elemType, stmt.elemVar);

    // Emit body statements
    for (Statement innerStmt : stmt.body) {
      ctx.delegator.generate(bodyOut, method, innerStmt);
    }

    // Build condOut: load $iter, call .hasNext(), branch if false
    DynamicByteArray condOut = new DynamicByteArray();
    Method hasNextMethod =
        new Method(ITERATOR_CLASS, "hasNext", "boolean", Collections.emptyList());
    ctx.loadGen.load(condOut, ctx.classIdMap.variables.get(iterId));
    ctx.invokeGen.invokeInterface(condOut, hasNextMethod);
    // IFEQ offset is from the IFEQ opcode itself: 3 (own instruction) + body + 3 (GOTO instruction)
    short ifeqOffset = (short) (3 + bodyOut.getBytes().length + 3);
    condOut.write(IFEQ);
    condOut.write(ifeqOffset);

    // Write condOut + bodyOut + GOTO back to start of condOut
    out.write(condOut.getBytes());
    out.write(bodyOut.getBytes());
    out.write(GOTO);
    out.write(shortToByteArray((short) -(condOut.getBytes().length + bodyOut.getBytes().length)));
    return out;
  }
}
