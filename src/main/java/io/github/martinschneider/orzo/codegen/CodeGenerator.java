package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_UTF8;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static java.util.Collections.emptyList;
import static java.util.List.of;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CodeGenerator {
  private static final short JAVA_CLASS_MAJOR_VERSION = 49;
  private static final short JAVA_CLASS_MINOR_VERSION = 0;
  private CGContext ctx;
  private List<Output> outputs;
  private List<Clazz> clazzes;
  private CompilerErrors errors;
  private VariableMap fields;
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
    }
    out.write(modifiers);
  }

  private void attributes() {
    out.write((short) 0);
  }

  private void classIndex() {
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, ctx.clazz.fqn('/')));
  }

  private HasOutput constPool(HasOutput out) {
    out.write(ctx.constPool.getBytes());
    return out;
  }

  private void fields() {
    out.write((short) fields.size);
    for (VariableInfo varInfo : fields.getVariables().values()) {
      writeField(out, varInfo);
    }
  }

  private void init(int idx) {
    out = outputs.get(idx);
    ctx.init(errors, idx, clazzes);
  }

  public void generate() {
    for (int i = 0; i < clazzes.size(); i++) {
      Clazz clazz = clazzes.get(i);
      init(i);
      header();
      supportPrint(clazz);
      processFields();
      HasOutput methods = methods(new DynamicByteArray(), clazz);
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
  }

  private void processFields() {
    fields = new VariableMap(new HashMap<>());
    for (ParallelDeclaration pDecl : ctx.clazz.fields) {
      for (Declaration decl : pDecl.declarations) {
        fields.put(
            decl.name,
            new VariableInfo(
                decl.name.id().toString(),
                (decl.arrDim > 0) ? REF : decl.type,
                (decl.arrDim > 0) ? decl.type : null,
                decl.accFlags,
                true,
                ctx.constPool.indexOf(
                    CONSTANT_FIELDREF,
                    ctx.clazz.fqn('/'),
                    decl.name.id().toString(),
                    TypeUtils.descr(decl.type, decl.arrDim)),
                decl.val));
        if (decl.accFlags.contains(AccessFlag.ACC_FINAL)) {
          ctx.constPool.addUtf8("ConstantValue");
          ctx.constPool.addByType(decl.type, decl.val.getConstantValue(decl.type));
        }
      }
    }
  }

  private void header() {
    out.write(0xCAFEBABE);
    out.write(JAVA_CLASS_MINOR_VERSION);
    out.write(JAVA_CLASS_MAJOR_VERSION);
  }

  private void interfaces(Clazz clazz) {
    out.write((short) clazz.interfaces.size());
    for (String interfaceName : clazz.interfaces) {
      // TODO: support interfaces from different packages
      out.write(
          (short)
              ctx.constPool.indexOf(
                  CONSTANT_CLASS, (clazz.packageName + "." + interfaceName).replace('.', '/')));
    }
  }

  private HasOutput methods(HasOutput out, Clazz clazz) {
    ctx.opStack = new OperandStack();
    List<Method> methods = ctx.clazz.methods;
    if (!clazz.isInterface) {
      ctx.constPool.addUtf8("Code");
      addClInit(methods);
    }
    out.write((short) methods.size());
    for (Method method : methods) {
      ctx.methodGen.generate(out, method, fields, clazz);
    }
    return out;
  }

  private void writeField(HasOutput out, VariableInfo varInfo) {
    short accFlags = 0;
    for (AccessFlag accFlag : varInfo.accFlags) {
      accFlags += accFlag.val;
    }
    out.write(accFlags);
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, varInfo.name));
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, TypeUtils.descr(varInfo)));
    // TODO: for some reason this still breaks for long and double
    if (varInfo.accFlags.contains(AccessFlag.ACC_FINAL)) {
      out.write((short) 1); // attribute size
      // https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.2
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "ConstantValue"));
      out.write(2);
      out.write(
          ctx.constPool.indexOf(
              ctx.constPool.getTypeByte(varInfo.type), varInfo.val.getConstantValue(varInfo.type)));
    } else {
      out.write((short) 0); // attribute size
    }
  }

  private void addClInit(List<Method> methods) {
    List<ParallelDeclaration> staticInits = new ArrayList<>();
    List<ParallelDeclaration> constrInits = new ArrayList<>();
    if (ctx.clazz.fields == null) {
      return;
    }
    for (ParallelDeclaration pDecl : ctx.clazz.fields) {
      if (!staticInits.isEmpty() && !constrInits.isEmpty()) {
        break;
      }
      for (Declaration decl : pDecl.declarations) {
        if (decl.val != null) {
          // the values of final static fields are set with the ConstantValue attribute
          if (decl.accFlags.contains(AccessFlag.ACC_STATIC)
              && !decl.accFlags.contains(AccessFlag.ACC_FINAL)) {
            staticInits.add(pDecl);
          } else if (!decl.accFlags.contains(AccessFlag.ACC_STATIC)) {
            constrInits.add(pDecl);
          }
          break;
        }
      }
    }
    // for now, a static initialiser is only necessary if there is at least one
    // public field with a non-default value (because its value must be set in the initialiser)
    // TODO: support explicit use of static initialiser blocks, e.g. support static
    // { ... } in the
    // source code
    if (!staticInits.isEmpty()) {
      Method clInit =
          new Method(
              "", of(AccessFlag.ACC_STATIC), "void", id("<clinit>"), emptyList(), emptyList());
      ctx.constPool.addUtf8(clInit.name.val.toString());
      ctx.constPool.addUtf8(TypeUtils.methodDescr(clInit));
      List<Statement> statements = new ArrayList<>();
      statements.addAll(staticInits);
      clInit.body = statements;
      methods.add(clInit);
    }
    if (!constrInits.isEmpty()) {
      List<Method> constructors = ctx.clazz.getConstructors();
      if (constructors.isEmpty()) {
        ctx.errors.addError(
            "codegen", "missing default constructor", new RuntimeException().getStackTrace());
      }
      // add initializer calls after super();
      for (Method constr : constructors) {
        List<Statement> body = new ArrayList<>();
        boolean startsWithSuper = ctx.methodGen.startsWithCallToSuper(constr.body);
        if (startsWithSuper) {
          body.add(constr.body.get(0));
        }
        body.addAll(constrInits);
        int idx = startsWithSuper ? 1 : 0;
        for (int i = idx; i < constr.body.size(); i++) {
          body.add(constr.body.get(i));
        }
        constr.body = body;
      }
    }
  }

  private void superClassIndex() {
    out.write(ctx.constPool.indexOf(CONSTANT_CLASS, "java/lang/Object"));
  }

  private void supportPrint(Clazz clazz) {
    // TODO: only add when needed
    if (!clazz.isInterface) {
      // hard-coded support for print
      ctx.constPool.addClass("java/lang/System");
      ctx.constPool.addClass("java/io/PrintStream");
      ctx.constPool.addFieldRef("java/lang/System", "out", "Ljava/io/PrintStream;");
    }
  }
}
