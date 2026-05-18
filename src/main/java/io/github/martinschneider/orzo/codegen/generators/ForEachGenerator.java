package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.AALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.ARRAYLENGTH;
import static io.github.martinschneider.orzo.codegen.OpCodes.BALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.CALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.CHECKCAST;
import static io.github.martinschneider.orzo.codegen.OpCodes.DALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;
import static io.github.martinschneider.orzo.codegen.OpCodes.IALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFEQ;
import static io.github.martinschneider.orzo.codegen.OpCodes.IF_ICMPGE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IINC;
import static io.github.martinschneider.orzo.codegen.OpCodes.LALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.SALOAD;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
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
import java.util.HashMap;
import java.util.Map;

public class ForEachGenerator implements StatementGenerator<ForEachStatement> {
  private CGContext ctx;

  private static final String ITERATOR_CLASS = "java/util/Iterator";
  private static final String ITERABLE_CLASS = "java/lang/Iterable";

  // Maps element type → array load opcode (for array for-each)
  private static final Map<String, Byte> ARRAY_LOAD = new HashMap<>();

  static {
    ARRAY_LOAD.put("byte", BALOAD);
    ARRAY_LOAD.put("short", SALOAD);
    ARRAY_LOAD.put("int", IALOAD);
    ARRAY_LOAD.put("long", LALOAD);
    ARRAY_LOAD.put("float", FALOAD);
    ARRAY_LOAD.put("double", DALOAD);
    ARRAY_LOAD.put("char", CALOAD);
    ARRAY_LOAD.put("boolean", BALOAD);
  }

  // Maps primitive element type name → [boxed JVM class, unboxing method name, unboxing descriptor]
  private static final Map<String, String[]> UNBOX = new HashMap<>();

  static {
    UNBOX.put("byte", new String[] {"java/lang/Byte", "byteValue", "()B"});
    UNBOX.put("short", new String[] {"java/lang/Short", "shortValue", "()S"});
    UNBOX.put("int", new String[] {"java/lang/Integer", "intValue", "()I"});
    UNBOX.put("long", new String[] {"java/lang/Long", "longValue", "()J"});
    UNBOX.put("float", new String[] {"java/lang/Float", "floatValue", "()F"});
    UNBOX.put("double", new String[] {"java/lang/Double", "doubleValue", "()D"});
    UNBOX.put("char", new String[] {"java/lang/Character", "charValue", "()C"});
    UNBOX.put("boolean", new String[] {"java/lang/Boolean", "booleanValue", "()Z"});
  }

  public ForEachGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ForEachStatement stmt) {
    if (isArrayIterable(stmt)) {
      return generateArray(out, method, stmt);
    }
    return generateIterable(out, method, stmt);
  }

  private boolean isArrayIterable(ForEachStatement stmt) {
    if (stmt.iterable == null || stmt.iterable.tokens == null || stmt.iterable.tokens.size() != 1) {
      return false;
    }
    if (!(stmt.iterable.tokens.get(0) instanceof Identifier)) return false;
    Identifier id = (Identifier) stmt.iterable.tokens.get(0);
    VariableInfo info = ctx.classIdMap.variables.get(id);
    return info != null && info.arrType != null;
  }

  private HasOutput generateArray(DynamicByteArray out, Method method, ForEachStatement stmt) {
    // Look up the iterable's type info so $arr inherits the correct array type for StackMapTable
    Identifier iterableId = (Identifier) stmt.iterable.tokens.get(0);
    VariableInfo iterableInfo = ctx.classIdMap.variables.get(iterableId);
    String arrVarType = iterableInfo != null ? iterableInfo.type : REF;
    String arrVarArrType = iterableInfo != null ? iterableInfo.arrType : stmt.elemType;

    // Allocate synthetic $arr and $idx slots
    short arrSlot = (short) ctx.classIdMap.variables.localSize;
    Identifier arrId = id("$arr_" + arrSlot);
    ctx.classIdMap.variables.putLocal(
        arrId,
        new VariableInfo(
            "$arr_" + arrSlot, arrVarType, arrVarArrType, emptyList(), false, arrSlot, null));

    short idxSlot = (short) ctx.classIdMap.variables.localSize;
    Identifier idxId = id("$idx_" + idxSlot);
    ctx.classIdMap.variables.putLocal(
        idxId, new VariableInfo("$idx_" + idxSlot, INT, emptyList(), false, idxSlot, null));

    // Evaluate iterable → push array ref, then store to $arr
    ctx.exprGen.eval(out, null, stmt.iterable);
    ctx.storeGen.store(out, ctx.classIdMap.variables.get(arrId));

    // Initialize index to 0
    out.write(ICONST_0);
    ctx.storeGen.store(out, ctx.classIdMap.variables.get(idxId));

    // Build bodyOut: load arr[idx], store to elemVar, body, increment idx
    DynamicByteArray bodyOut = new DynamicByteArray();
    ctx.loadGen.loadReference(bodyOut, arrSlot);
    ctx.loadGen.load(bodyOut, ctx.classIdMap.variables.get(idxId));
    bodyOut.write(ARRAY_LOAD.getOrDefault(stmt.elemType, AALOAD));
    ctx.assignGen.assign(bodyOut, ctx.classIdMap.variables, stmt.elemType, stmt.elemVar);

    for (Statement innerStmt : stmt.body) {
      ctx.delegator.generate(bodyOut, method, innerStmt);
    }

    // iinc $idx, 1
    bodyOut.write(IINC);
    bodyOut.write((byte) idxSlot);
    bodyOut.write((byte) 1);

    // Build condOut: if $idx >= arr.length → exit
    DynamicByteArray condOut = new DynamicByteArray();
    ctx.loadGen.load(condOut, ctx.classIdMap.variables.get(idxId));
    ctx.loadGen.loadReference(condOut, arrSlot);
    condOut.write(ARRAYLENGTH);
    short skipOffset = (short) (3 + bodyOut.getBytes().length + 3);
    condOut.write(IF_ICMPGE);
    condOut.write(skipOffset);

    out.write(condOut.getBytes());
    out.write(bodyOut.getBytes());
    out.write(GOTO);
    out.write(shortToByteArray((short) -(condOut.getBytes().length + bodyOut.getBytes().length)));
    return out;
  }

  private HasOutput generateIterable(DynamicByteArray out, Method method, ForEachStatement stmt) {
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

    String[] unboxInfo = UNBOX.get(stmt.elemType);
    if (unboxInfo != null) {
      // Primitive element type: CHECKCAST to boxed class, then unbox
      String boxedClass = unboxInfo[0];
      String unboxMethodName = unboxInfo[1];
      String primitiveType = stmt.elemType;
      ctx.constPool.addClass(boxedClass);
      bodyOut.write(CHECKCAST);
      bodyOut.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, boxedClass));
      ctx.invokeGen.invokeVirtual(
          bodyOut, new Method(boxedClass, unboxMethodName, primitiveType, Collections.emptyList()));
    } else {
      // Reference element type: resolve simple names like "String" to JVM internal names
      String descriptor = TypeUtils.descr(stmt.elemType);
      String jvmElemType =
          (descriptor.startsWith("L") && descriptor.endsWith(";"))
              ? descriptor.substring(1, descriptor.length() - 1)
              : stmt.elemType.replace('.', '/');
      ctx.constPool.addClass(jvmElemType);
      bodyOut.write(CHECKCAST);
      bodyOut.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, jvmElemType));
    }

    // Store the cast/unboxed element into the loop variable (allocates slot if needed)
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
