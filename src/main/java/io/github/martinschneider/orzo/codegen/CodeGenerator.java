package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.ARETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.CHECKCAST;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKEVIRTUAL;
import static io.github.martinschneider.orzo.codegen.OpCodes.IRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.RETURN;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.orzo.codegen.identifier.DefiniteAssignment;
import io.github.martinschneider.orzo.codegen.identifier.LocalVariableChecker;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
  private static final short JAVA_CLASS_MAJOR_VERSION = 71;
  private static final short JAVA_CLASS_MINOR_VERSION = 0;
  private CGContext ctx;
  private List<Output> outputs;
  private List<Clazz> clazzes;
  private CompilerErrors errors;
  Output out;

  public CodeGenerator(List<Clazz> clazzes, List<Output> outputs, CompilerErrors errors) {
    this.outputs = outputs;
    this.clazzes = clazzes;
    this.errors = errors;
    ctx = new CGContext();
  }

  public CompilerErrors getErrors() {
    return ctx.errors;
  }

  void accessModifiers(Clazz clazz) {
    short modifiers = (short) (AccessFlag.ACC_SUPER.val + AccessFlag.ACC_PUBLIC.val);
    if (clazz.isInterface) {
      modifiers =
          (short)
              (AccessFlag.ACC_INTERFACE.val
                  + AccessFlag.ACC_ABSTRACT.val
                  + AccessFlag.ACC_PUBLIC.val);
    } else if (clazz.isEnum) {
      modifiers =
          (short)
              (AccessFlag.ACC_PUBLIC.val
                  + AccessFlag.ACC_FINAL.val
                  + AccessFlag.ACC_SUPER.val
                  + AccessFlag.ACC_ENUM.val);
    } else if (clazz.isAbstract) {
      modifiers += AccessFlag.ACC_ABSTRACT.val;
    }
    out.write(modifiers);
  }

  private void attributes() {
    out.write((short) (1 + (nestHost != null ? 1 : 0) + (nestMembers.isEmpty() ? 0 : 1)));
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "SourceFile"));
    out.write(2);
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, ctx.clazz.sourceFile));
    if (nestHost != null) {
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "NestHost"));
      out.write(2);
      out.write(ctx.constPool.indexOf(CONSTANT_CLASS, nestHost.fqn('/')));
    }
    if (!nestMembers.isEmpty()) {
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "NestMembers"));
      out.write(2 + 2 * nestMembers.size());
      out.write((short) nestMembers.size());
      for (Clazz member : nestMembers) {
        out.write(ctx.constPool.indexOf(CONSTANT_CLASS, member.fqn('/')));
      }
    }
  }

  // A nested class and its (outermost) enclosing class are nestmates: they may access each other's
  // private members (the classes of lambdas and anonymous classes, generated later, aren't).
  private void nest(Clazz clazz) {
    nestHost = nestHost(clazz);
    nestMembers = new ArrayList<>();
    if (nestHost != null) {
      ctx.constPool.addUtf8("NestHost");
      ctx.constPool.addClass(nestHost.fqn('/'));
      return;
    }
    for (Clazz other : clazzes) {
      if (nestHost(other) == clazz) {
        nestMembers.add(other);
        ctx.constPool.addClass(other.fqn('/'));
      }
    }
    if (!nestMembers.isEmpty()) {
      ctx.constPool.addUtf8("NestMembers");
    }
  }

  // the outermost class enclosing a class (also a lambda's, e.g. "Outer$$Lambda$1"), null for a
  // top-level class
  private Clazz nestHost(Clazz clazz) {
    String fqn = clazz.fqn();
    int lambda = fqn.indexOf("$$Lambda$");
    if (lambda > 0) {
      Clazz owner = TypeUtils.sourceClazz(fqn.substring(0, lambda), clazzes);
      Clazz host = (owner != null) ? nestHost(owner) : null;
      return (host != null) ? host : owner;
    }
    List<Clazz> outers = FieldProcessor.enclosingClasses(clazz, clazzes);
    return outers.isEmpty() ? null : outers.get(0);
  }

  private void classIndex() {
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, ctx.clazz.fqn('/')));
  }

  private HasOutput constPool(HasOutput out) {
    out.write(ctx.constPool.getBytes());
    return out;
  }

  private void fields() {
    int ownCount =
        ctx.classIdMap.variables.fieldMap.size()
            - ctx.classIdMap.variables.inheritedFieldNames.size();
    out.write((short) ownCount);
    for (VariableInfo varInfo : ctx.classIdMap.variables.fieldMap.values()) {
      if (!ctx.classIdMap.variables.inheritedFieldNames.contains(varInfo.name)) {
        writeField(out, varInfo);
      }
    }
  }

  // see nest()
  private Clazz nestHost;
  private List<Clazz> nestMembers = new ArrayList<>();

  private void init(int idx) {
    out = outputs.get(idx);
    ctx.init(errors, idx, clazzes, outputs);
    ctx.constPool.addUtf8("SourceFile");
    ctx.constPool.addUtf8(ctx.clazz.sourceFile);
  }

  public void generate() {
    // clazzes.size() is re-read every iteration, so a class appended mid-loop via
    // ctx.addSyntheticClazz() (e.g. a lambda's synthetic functional-interface implementation) is
    // picked up and generated in a later iteration of this same loop — no separate pass needed.
    for (int i = 0; i < clazzes.size(); i++) {
      Clazz clazz = clazzes.get(i);
      errors.file = clazz.sourceFile;
      errors.position = null;
      try {
        generate(i, clazz);
      } catch (RuntimeException e) {
        // a bug in the compiler: report it (at the statement being generated) and go on with the
        // next class instead of crashing (no class files are written after errors)
        errors.addError(
            "code generator",
            "internal compiler error in " + clazz.fqn() + ": " + e,
            e.getStackTrace());
      }
    }
  }

  private void generate(int i, Clazz clazz) {
    init(i);
    ctx.classIdMap.variables.fieldMap.clear();
    ctx.classIdMap.variables.localMap.clear();
    header();
    supportPrint(clazz);
    ctx.memberProc.processFields();
    HasOutput methods = methods(new DynamicByteArray(), clazz);
    nest(clazz);
    HasOutput constPool = constPool(new DynamicByteArray());
    out.write(constPool.getBytes());
    accessModifiers(clazz);
    classIndex();
    superClassIndex();
    interfaces(clazz);
    fields();
    out.write(methods.getBytes());
    attributes();
    out.flush();
  }

  private void header() {
    out.write(0xCAFEBABE);
    out.write(JAVA_CLASS_MINOR_VERSION);
    out.write(JAVA_CLASS_MAJOR_VERSION);
  }

  private void interfaces(Clazz clazz) {
    out.write((short) clazz.interfaces.size());
    for (String interfaceName : clazz.interfaces) {
      // (e.g. a nested interface named before it was known)
      Clazz source = TypeUtils.sourceClazz(interfaceName, ctx.allClazzes);
      String ifaceFqn =
          (source != null)
              ? source.fqn()
              : (interfaceName.contains(".")
                      || clazz.packageName == null
                      || clazz.packageName.isEmpty())
                  ? interfaceName
                  : (clazz.packageName + "." + interfaceName);
      out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, ifaceFqn.replace('.', '/')));
    }
  }

  private HasOutput methods(HasOutput out, Clazz clazz) {
    ctx.opStack = new OperandStack();
    List<Method> methods = ctx.clazz.methods;
    if (!clazz.isInterface) {
      ctx.constPool.addUtf8("Code");
      ctx.memberProc.addClInit(methods);
    } else if (methods.stream().anyMatch(m -> !m.accFlags.contains(AccessFlag.ACC_ABSTRACT))) {
      ctx.constPool.addUtf8("Code"); // (default, static or private methods)
    }
    List<String[]> bridges = clazz.isInterface ? new ArrayList<>() : collectBridges(clazz);
    out.write((short) (methods.size() + bridges.size()));
    for (Method method : methods) {
      ctx.method = method;
      ctx.localClasses.clear();
      ctx.exceptionChecker.reset();
      ctx.classIdMap.variables.localMap.clear();
      ctx.classIdMap.variables.allLocalsBySlot.clear();
      ctx.classIdMap.variables.localSize = 0;
      if (!method.accFlags.contains(AccessFlag.ACC_STATIC)) {
        // Slot 0 is reserved for 'this' in all non-static methods (including constructors)
        ctx.classIdMap.variables.localSize = 1;
      }
      ctx.classIdMap.variables.localSizeMax = ctx.classIdMap.variables.localSize;
      ctx.memberProc.processMethodArgs(method);
      ctx.memberProc.processLocalVars(method);
      errors.position = errors.startTokens.get(method);
      new LocalVariableChecker(errors).check(method);
      new DefiniteAssignment(errors).check(method);
      ctx.methodGen.generate(out, method, clazz);
    }
    for (String[] bridge : bridges) {
      writeBridgeMethod(out, bridge);
    }
    return out;
  }

  /**
   * Collects bridge methods needed for generic interface implementations. Returns a list of
   * [methodName, bridgeDescriptor, concreteDescriptor, concreteParamDescriptors...] arrays.
   *
   * <p>Uses erased descriptor comparison rather than TypeVariable detection so that this works even
   * when the interface was compiled by Orzo (which does not emit Signature attributes).
   */
  private List<String[]> collectBridges(Clazz clazz) {
    List<String[]> bridges = new ArrayList<>();
    if (clazz.interfaces == null || clazz.interfaces.isEmpty()) {
      return bridges;
    }
    for (String ifaceName : clazz.interfaces) {
      String fqn =
          (ifaceName.contains(".")
                  ? ifaceName
                  : (clazz.packageName != null && !clazz.packageName.isEmpty()
                          ? clazz.packageName + "."
                          : "")
                      + ifaceName)
              .replace('/', '.');
      try {
        Class<?> iface = Class.forName(fqn);
        for (java.lang.reflect.Method ifaceMethod : iface.getDeclaredMethods()) {
          Class<?>[] erasedParams = ifaceMethod.getParameterTypes();
          Class<?> erasedReturn = ifaceMethod.getReturnType();
          String methodName = ifaceMethod.getName();

          Method concrete = findConcreteMethod(clazz, methodName, erasedParams);
          if (concrete == null) continue;

          String bridgeDescr = buildErasedDescriptor(erasedParams, erasedReturn);
          String concreteDescr = TypeUtils.methodDescr(concrete);
          if (bridgeDescr.equals(concreteDescr)) continue;

          // Encode: [name, bridgeDescr, concreteDescr, param0ConcreteDescr, ...]
          // param slot i is non-null when its erased type differs from the concrete type
          // (CHECKCAST)
          String[] entry = new String[3 + erasedParams.length];
          entry[0] = methodName;
          entry[1] = bridgeDescr;
          entry[2] = concreteDescr;
          for (int i = 0; i < erasedParams.length; i++) {
            if (concrete.args != null && i < concrete.args.size()) {
              String concrParamDescr = TypeUtils.descr(concrete.args.get(i).type);
              String erasedParamDescr = classToDescr(erasedParams[i]);
              if (!concrParamDescr.equals(erasedParamDescr)) {
                entry[3 + i] = concrParamDescr;
              }
            }
          }
          bridges.add(entry);
        }
      } catch (ClassNotFoundException e) {
        // Interface not on classpath (defined in source files) — skip
      }
    }
    return bridges;
  }

  /**
   * Finds a concrete method in clazz matching the given name and erased parameter types.
   *
   * <p>Phase 1: exact match (erased param descriptor == concrete param descriptor). Phase 2
   * fallback: name + param-count match, for generic methods where the erased param type (e.g.
   * Object) differs from the concrete type (e.g. TokenList). A differing parameter must be a
   * subtype of the erased one, so an unrelated overload with the same arity is never picked.
   */
  private Method findConcreteMethod(Clazz clazz, String name, Class<?>[] erasedParams) {
    Method fallback = null;
    outer:
    for (Method m : clazz.methods) {
      if (!m.name.val.equals(name)) continue;
      int count = m.args == null ? 0 : m.args.size();
      if (count != erasedParams.length) continue;
      boolean candidate = true;
      for (int i = 0; candidate && i < erasedParams.length; i++) {
        candidate = isBridgeableParam(erasedParams[i], m.args.get(i).type, clazz);
      }
      if (fallback == null && candidate) fallback = m;
      // Try exact match on all parameter descriptors
      for (int i = 0; i < erasedParams.length; i++) {
        String expected = classToDescr(erasedParams[i]);
        String actual = TypeUtils.descr(m.args.get(i).type);
        if (!expected.equals(actual)) continue outer;
      }
      return m; // exact match
    }
    return fallback; // fallback: name+count (handles TypeVariable erased-to-Object params)
  }

  // Whether a concrete parameter type can implement an erased interface parameter: the same type
  // or a subtype of it (e.g. "IfStatement" for "T extends Statement"). Types that can't be loaded
  // (e.g. compiled in the same run) are accepted.
  private boolean isBridgeableParam(Class<?> erased, String concreteType, Clazz clazz) {
    String resolved = TypeUtils.resolveSimpleType(concreteType, clazzes, clazz);
    if (classToDescr(erased).equals(TypeUtils.descr(resolved))) {
      return true;
    }
    if (erased.isPrimitive() || TypeUtils.isPrimitive(resolved)) {
      return false;
    }
    try {
      return erased.isAssignableFrom(
          Class.forName(resolved, false, CodeGenerator.class.getClassLoader()));
    } catch (ClassNotFoundException | LinkageError e) {
      return true;
    }
  }

  private String buildErasedDescriptor(Class<?>[] params, Class<?> ret) {
    StringBuilder sb = new StringBuilder("(");
    for (Class<?> p : params) sb.append(classToDescr(p));
    sb.append(')');
    sb.append(classToDescr(ret));
    return sb.toString();
  }

  private String classToDescr(Class<?> c) {
    if (c == void.class) return "V";
    if (c == boolean.class) return "Z";
    if (c == byte.class) return "B";
    if (c == char.class) return "C";
    if (c == short.class) return "S";
    if (c == int.class) return "I";
    if (c == long.class) return "J";
    if (c == float.class) return "F";
    if (c == double.class) return "D";
    if (c.isArray()) return "[" + classToDescr(c.getComponentType());
    return "L" + c.getName().replace('.', '/') + ";";
  }

  /**
   * Writes a synthetic bridge method. {@code bridge} is [name, bridgeDescr, concreteDescr,
   * concreteParam0Descr, ...].
   */
  private void writeBridgeMethod(HasOutput out, String[] bridge) {
    String name = bridge[0];
    String bridgeDescr = bridge[1];
    String concreteDescr = bridge[2];

    short flags =
        (short)
            (AccessFlag.ACC_PUBLIC.val | AccessFlag.ACC_BRIDGE.val | AccessFlag.ACC_SYNTHETIC.val);
    out.write(flags);
    ctx.constPool.addUtf8(name);
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, name));
    ctx.constPool.addUtf8(bridgeDescr);
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, bridgeDescr));
    out.write((short) 1); // 1 attribute: Code
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "Code"));

    DynamicByteArray code = new DynamicByteArray();
    code.write(ALOAD_0);
    int slot = 1;
    // Parse erased descriptor params to determine load opcodes and slots
    int paramStart = 1; // skip '('
    int pi = 0;
    while (paramStart < bridgeDescr.length() && bridgeDescr.charAt(paramStart) != ')') {
      char c = bridgeDescr.charAt(paramStart);
      boolean twoSlot = c == 'J' || c == 'D';
      byte op =
          (c == 'J')
              ? LLOAD
              : (c == 'F') ? FLOAD : (c == 'D') ? DLOAD : (c == 'L' || c == '[') ? ALOAD : ILOAD;
      writeLoad(code, op, slot);
      // If this param is a type-variable in the interface → CHECKCAST to concrete type
      if (pi + 3 < bridge.length && bridge[pi + 3] != null) {
        String concr = bridge[pi + 3];
        if (concr.startsWith("L") && concr.endsWith(";")) {
          String className = concr.substring(1, concr.length() - 1);
          ctx.constPool.addClass(className);
          code.write(CHECKCAST);
          code.write(ctx.constPool.indexOf(CONSTANT_CLASS, className));
        }
      }
      slot += twoSlot ? 2 : 1;
      pi++;
      // Advance paramStart past this descriptor token
      if (c == 'L') paramStart = bridgeDescr.indexOf(';', paramStart) + 1;
      else if (c == '[') {
        paramStart++;
        while (paramStart < bridgeDescr.length() && bridgeDescr.charAt(paramStart) == '[')
          paramStart++;
        if (bridgeDescr.charAt(paramStart) == 'L')
          paramStart = bridgeDescr.indexOf(';', paramStart) + 1;
        else paramStart++;
      } else {
        paramStart++;
      }
    }

    // INVOKEVIRTUAL owning-class . name . concreteDescr
    ctx.constPool.addUtf8(concreteDescr);
    ctx.constPool.addMethodRef(ctx.clazz.fqn('/'), name, concreteDescr);
    code.write(INVOKEVIRTUAL);
    code.write(ctx.constPool.indexOf(CONSTANT_METHODREF, ctx.clazz.fqn('/'), name, concreteDescr));

    // Determine return opcode from concrete descriptor
    char retChar = concreteDescr.charAt(concreteDescr.lastIndexOf(')') + 1);
    byte retOp;
    switch (retChar) {
      case 'V':
        retOp = RETURN;
        break;
      case 'J':
        retOp = LRETURN;
        break;
      case 'F':
        retOp = FRETURN;
        break;
      case 'D':
        retOp = DRETURN;
        break;
      case 'I':
      case 'B':
      case 'C':
      case 'S':
      case 'Z':
        retOp = IRETURN;
        break;
      default:
        retOp = ARETURN;
    }
    code.write(retOp);

    int codeLen = code.size();
    // attrSize = max_stack(2)+max_locals(2)+code_len(4)+code+ex_table_len(2)+attrs_count(2)
    int attrSize = 12 + codeLen;
    out.write(attrSize);
    out.write((short) (slot + 1)); // max_stack: this + params + 1 for invoke result
    out.write((short) slot); // max_locals
    out.write(codeLen);
    out.write(code.getBytes());
    out.write((short) 0); // no exception table
    out.write((short) 0); // no StackMapTable
  }

  private void writeLoad(DynamicByteArray code, byte op, int slot) {
    if (op == ALOAD) {
      if (slot == 0) {
        code.write(ALOAD_0);
        return;
      }
      if (slot == 1) {
        code.write(ALOAD_1);
        return;
      }
      if (slot == 2) {
        code.write(ALOAD_2);
        return;
      }
      if (slot == 3) {
        code.write(ALOAD_3);
        return;
      }
    }
    code.write(op);
    code.write((byte) slot);
  }

  private void writeField(HasOutput out, VariableInfo varInfo) {
    short accFlags = 0;
    for (AccessFlag accFlag : varInfo.accFlags) {
      accFlags += accFlag.val;
    }
    out.write(accFlags);
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, varInfo.name));
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, TypeUtils.descr(varInfo)));
    // Only add ConstantValue for primitive types and String (compile-time constants), not for
    // reference types (e.g. List<String> fields) whose initializers are not constant expressions.
    Object constVal = varInfo.val != null ? varInfo.val.getConstantValue(varInfo.type) : null;
    boolean constantValue =
        varInfo.accFlags.contains(AccessFlag.ACC_FINAL)
            && constVal != null
            && !varInfo.type.startsWith("L")
            && !varInfo.type.startsWith("[");
    boolean signature = varInfo.genericType != null;
    out.write((short) ((constantValue ? 1 : 0) + (signature ? 1 : 0))); // attribute count
    if (constantValue) {
      // https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.2
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "ConstantValue"));
      out.write(2);
      out.write(ctx.constPool.indexOf(ctx.constPool.getTypeByte(varInfo.type), constVal));
    }
    if (signature) {
      // https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.9
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "Signature"));
      out.write(2);
      out.write(
          ctx.constPool.indexOf(CONSTANT_UTF8, GenericTypes.toSignature(varInfo.genericType)));
    }
  }

  private void superClassIndex() {
    String superClass =
        ctx.clazz.baseClass != null ? ctx.clazz.baseClass.replace('.', '/') : "java/lang/Object";
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, superClass));
  }

  private void supportPrint(Clazz clazz) {
    // TODO: only if the class uses System.out
    if (!clazz.isInterface) {
      ctx.constPool.addClass("java/lang/System");
      ctx.constPool.addClass("java/io/PrintStream");
      ctx.constPool.addFieldRef("java/lang/System", "out", "Ljava/io/PrintStream;");
    }
  }
}
