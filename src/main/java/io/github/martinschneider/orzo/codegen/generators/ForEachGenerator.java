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
import io.github.martinschneider.orzo.codegen.GenericTypes;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.ForEachStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    boolean array = isArrayIterable(stmt);
    if (Type.VAR.equals(stmt.elemType)) {
      inferElementType(stmt, array);
    }
    if (array) {
      return generateArray(out, method, stmt);
    }
    return generateIterable(out, method, stmt);
  }

  // The element type of "for (var x : iterable)": an array's component type or the type argument
  // of an Iterable (if known, otherwise Object)
  private void inferElementType(ForEachStatement stmt, boolean array) {
    NumExprTypeDecider decider = new NumExprTypeDecider(ctx);
    String type = decider.getType(ctx.classIdMap, stmt.iterable);
    if (array && type != null && type.startsWith("[")) {
      stmt.elemArrDim = 0;
      for (type = type.substring(1); type.startsWith("["); type = type.substring(1)) {
        stmt.elemArrDim++;
      }
      stmt.elemType = TypeUtils.jvmDescriptorToTypeName(type);
      return;
    }
    String generic = decider.chainGeneric;
    List<Token> tokens = stmt.iterable.tokens;
    if (tokens.size() == 1
        && tokens.get(0) instanceof Identifier
        && !(tokens.get(0) instanceof MethodCall)
        && ((Identifier) tokens.get(0)).next == null) {
      VariableInfo var = ctx.classIdMap.variables.get((Identifier) tokens.get(0));
      generic = (var != null) ? var.genericType : null;
    }
    String iterator =
        (generic != null) ? GenericTypes.returnType(generic, "iterator", List.of()) : null;
    List<String> args = (iterator != null) ? GenericTypes.typeArgs(iterator) : List.of();
    String element = args.isEmpty() ? "java.lang.Object" : args.get(0);
    stmt.elemType = GenericTypes.erasure(element);
    stmt.elemGenericType = element.contains("<") ? element : null;
  }

  private boolean isArrayIterable(ForEachStatement stmt) {
    if (stmt.iterable instanceof ArrayInit) {
      return true; // e.g. "for (int i : new int[] {1, 2})"
    }
    if (stmt.iterable == null || stmt.iterable.tokens == null || stmt.iterable.tokens.size() != 1) {
      return false;
    }
    if (!(stmt.iterable.tokens.get(0) instanceof Identifier)) return false;
    Identifier id = (Identifier) stmt.iterable.tokens.get(0);
    VariableInfo info = ctx.classIdMap.variables.get(id);
    if (info != null && info.arrType != null) {
      return true;
    }
    // Not a plain array-typed local variable (e.g. the iterable is a method call like
    // "clazz.getDeclaredFields()"); infer array-ness from the expression's type instead.
    String inferredType = new NumExprTypeDecider(ctx).getType(ctx.classIdMap, stmt.iterable);
    return inferredType != null && inferredType.startsWith("[");
  }

  private HasOutput generateArray(DynamicByteArray out, Method method, ForEachStatement stmt) {
    // Look up the iterable's type info so $arr inherits the correct array type for StackMapTable
    VariableInfo iterableInfo =
        (stmt.iterable.tokens.size() == 1 && stmt.iterable.tokens.get(0) instanceof Identifier)
            ? ctx.classIdMap.variables.get(stmt.iterable.tokens.get(0))
            : null;
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
    int etStart = ctx.exceptionTable.size();
    DynamicByteArray bodyOut = new DynamicByteArray();
    ctx.loadGen.loadReference(bodyOut, arrSlot);
    ctx.loadGen.load(bodyOut, ctx.classIdMap.variables.get(idxId));
    bodyOut.write(ARRAY_LOAD.getOrDefault(stmt.elemType, AALOAD));
    ctx.assignGen.assign(bodyOut, ctx.classIdMap.variables, stmt.elemType, stmt.elemVar);

    for (Statement innerStmt : stmt.body) {
      ctx.delegator.generate(bodyOut, method, innerStmt);
    }

    // continue jumps here: past the body, straight to the index increment
    int incStart = bodyOut.size();

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

    // Fix up ET entries: body starts after condition
    TryGenerator.adjustExceptionTableEntries(
        ctx, etStart, ctx.exceptionTable.size(), out.size() + condOut.getBytes().length);
    out.write(condOut.getBytes());
    byte[] bodyBytes = bodyOut.getBytes();
    // break -> past the trailing backward GOTO (loop exit); continue -> index increment
    for (int pos : bodyOut.breaksFor(stmt.label)) {
      byte[] off = shortToByteArray((short) (bodyBytes.length - pos + 3));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    for (int pos : bodyOut.continuesFor(stmt.label)) {
      byte[] off = shortToByteArray((short) (incStart - pos));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    out.mergeLabeled(bodyOut, out.size()); // breaks/continues to outer loops
    out.write(bodyBytes);
    out.write(GOTO);
    out.write(shortToByteArray((short) -(condOut.getBytes().length + bodyBytes.length)));
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
    int etStart = ctx.exceptionTable.size();
    DynamicByteArray bodyOut = new DynamicByteArray();

    // Load $iter and call .next() to get the next element
    Method nextMethod =
        new Method(ITERATOR_CLASS, "next", "java/lang/Object", Collections.emptyList());
    ctx.loadGen.load(bodyOut, ctx.classIdMap.variables.get(iterId));
    ctx.invokeGen.invokeInterface(bodyOut, nextMethod);

    String[] unboxInfo = (stmt.elemArrDim > 0) ? null : UNBOX.get(stmt.elemType);
    if (stmt.elemArrDim > 0) {
      // Array element type, e.g. "for (int[] row : listOfRows)": cast to the array class
      String arrayClass = TypeUtils.descr(stmt.elemType, stmt.elemArrDim);
      ctx.constPool.addClass(arrayClass);
      bodyOut.write(CHECKCAST);
      bodyOut.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, arrayClass));
    } else if (unboxInfo != null) {
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
    if (stmt.elemArrDim > 0) {
      ctx.assignGen.assignArray(
          bodyOut, ctx.classIdMap, stmt.elemType, stmt.elemArrDim, stmt.elemVar);
    } else {
      ctx.assignGen.assign(bodyOut, ctx.classIdMap.variables, stmt.elemType, stmt.elemVar);
      ctx.classIdMap.variables.localMap.get(stmt.elemVar.val.toString()).genericType =
          stmt.elemGenericType;
    }

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

    // Fix up ET entries: body starts after condition
    TryGenerator.adjustExceptionTableEntries(
        ctx, etStart, ctx.exceptionTable.size(), out.size() + condOut.getBytes().length);
    // Write condOut + bodyOut + GOTO back to start of condOut
    out.write(condOut.getBytes());
    byte[] bodyBytes = bodyOut.getBytes();
    int condLen = condOut.getBytes().length;
    // break -> past the trailing backward GOTO (loop exit); continue -> top of loop (hasNext check)
    for (int pos : bodyOut.breaksFor(stmt.label)) {
      byte[] off = shortToByteArray((short) (bodyBytes.length - pos + 3));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    for (int pos : bodyOut.continuesFor(stmt.label)) {
      byte[] off = shortToByteArray((short) (-condLen - pos));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    out.mergeLabeled(bodyOut, out.size()); // breaks/continues to outer loops
    out.write(bodyBytes);
    out.write(GOTO);
    out.write(shortToByteArray((short) -(condLen + bodyBytes.length)));
    return out;
  }
}
