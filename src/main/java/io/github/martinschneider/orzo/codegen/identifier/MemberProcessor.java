package io.github.martinschneider.orzo.codegen.identifier;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static java.util.Collections.emptyList;
import static java.util.List.of;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class MemberProcessor {

  public CGContext ctx;

  public MemberProcessor(CGContext ctx) {
    this.ctx = ctx;
  }

  // add all fields to the constant pool and idMap
  public void processFields() {
    for (ParallelDeclaration pDecl : ctx.clazz.fields) {
      for (Declaration decl : pDecl.declarations) {
        ctx.classIdMap.variables.putField(
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

  // add all local vars to the constant pool and idMap
  public void processLocalVars(Method method) {
    for (Statement stmt : method.body) {
      if (stmt instanceof ParallelDeclaration) {
        ParallelDeclaration pDecl = (ParallelDeclaration) stmt;
        for (Declaration decl : pDecl.declarations) {
          ctx.classIdMap.variables.putLocal(
              decl.name,
              new VariableInfo(
                  decl.name.id().toString(),
                  (decl.arrDim > 0) ? REF : decl.type,
                  (decl.arrDim > 0) ? decl.type : null,
                  decl.accFlags,
                  false,
                  (byte) ctx.classIdMap.variables.localSize,
                  decl.val));
        }
      }
    }
  }

  // add all method args (=local args) to the constant pool and idMap
  public void processMethodArgs(Method method) {
    for (Argument arg : method.args) {
      // TODO: this code is ugly
      String type = arg.type;
      String arrayType = null;
      if (arg.type.startsWith("[")) {
        type = REF;
        arrayType = arg.type.replaceAll("\\[", "");
      }
      ctx.classIdMap.variables.putLocal(
          arg.name,
          new VariableInfo(
              arg.name.val.toString(),
              type,
              arrayType,
              emptyList(),
              false,
              (byte) ctx.classIdMap.variables.localSize,
              null));
    }
  }

  // add initializer code for fields
  public void addClInit(List<Method> methods) {
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
        // the values of final fields are set with the ConstantValue Attribute
        if (decl.val != null && !decl.accFlags.contains(AccessFlag.ACC_FINAL)) {
          if (decl.accFlags.contains(AccessFlag.ACC_STATIC)) {
            staticInits.add(pDecl);
          } else {
            constrInits.add(pDecl);
          }
          break;
        }
      }
    }
    // for now, a static initialiser is only necessary if there is at least one
    // public field with a non-default value (because its value must be set in the
    // initialiser)
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
}
