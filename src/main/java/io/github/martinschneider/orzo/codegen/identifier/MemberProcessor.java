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
    // Add $VALUES field for enums
    if (ctx.clazz.isEnum) {
      addEnumValuesField();
    }

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
        if (decl.accFlags.contains(AccessFlag.ACC_FINAL) && decl.val != null) {
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
    // Handle enum-specific initialization
    if (ctx.clazz.isEnum) {
      addEnumMethods(methods);
      // Add enum constructor
      if (ctx.clazz.getConstructors().isEmpty()) {
        addEnumConstructor(methods);
      } else {
        // Transform user-defined enum constructors to include implicit enum parameters
        transformEnumConstructors(methods);
      }
      addEnumStaticInitializer(methods);
      return;
    }

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

  // Add enum-specific methods: values() and valueOf()
  private void addEnumMethods(List<Method> methods) {
    // Add required constants to pool for enum method generation
    String arrayType = "[L" + ctx.clazz.fqn('/') + ";";
    String enumType = "L" + ctx.clazz.fqn('/') + ";";

    // Add constants needed for values() method
    ctx.constPool.addMethodRef("java/lang/Object", "clone", "()Ljava/lang/Object;");
    ctx.constPool.addClass(arrayType);

    // Add constants needed for valueOf() method
    ctx.constPool.addClass(ctx.clazz.fqn('/'));
    ctx.constPool.addMethodRef(
        "java/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;");

    // Add constants needed for $values() method
    ctx.constPool.addClass(ctx.clazz.fqn('/'));

    // Add values() method
    Method valuesMethod =
        new Method(
            ctx.clazz.fqn(),
            of(AccessFlag.ACC_PUBLIC, AccessFlag.ACC_STATIC),
            arrayType, // Return array of enum type
            id("values"),
            emptyList(),
            emptyList() // Empty body - will be generated in bytecode
            );
    ctx.constPool.addUtf8(valuesMethod.name.val.toString());
    ctx.constPool.addUtf8(TypeUtils.methodDescr(valuesMethod));
    methods.add(valuesMethod);

    // Add valueOf(String) method
    Method valueOfMethod =
        new Method(
            ctx.clazz.fqn(),
            of(AccessFlag.ACC_PUBLIC, AccessFlag.ACC_STATIC),
            "L" + ctx.clazz.fqn('/') + ";", // Return enum type
            id("valueOf"),
            of(new Argument("java.lang.String", id("name"))),
            emptyList() // Empty body - will be generated in bytecode
            );
    ctx.constPool.addUtf8(valueOfMethod.name.val.toString());
    ctx.constPool.addUtf8(TypeUtils.methodDescr(valueOfMethod));
    methods.add(valueOfMethod);

    // Add private $values() helper method
    Method dollarValuesMethod =
        new Method(
            ctx.clazz.fqn(),
            of(AccessFlag.ACC_PRIVATE, AccessFlag.ACC_STATIC),
            "[L" + ctx.clazz.fqn('/') + ";", // Return array of enum type
            id("$values"),
            emptyList(),
            emptyList() // Empty body - will be generated in bytecode
            );
    ctx.constPool.addUtf8(dollarValuesMethod.name.val.toString());
    ctx.constPool.addUtf8(TypeUtils.methodDescr(dollarValuesMethod));
    methods.add(dollarValuesMethod);
  }

  // Add enum static initializer that creates all enum instances
  private void addEnumStaticInitializer(List<Method> methods) {
    Method clInit =
        new Method(
            ctx.clazz.fqn(),
            of(AccessFlag.ACC_STATIC),
            "void",
            id("<clinit>"),
            emptyList(),
            emptyList() // Empty body - will be generated in bytecode
            );
    ctx.constPool.addUtf8(clInit.name.val.toString());
    ctx.constPool.addUtf8(TypeUtils.methodDescr(clInit));
    methods.add(clInit);
  }

  // Add enum constructor that takes (String name, int ordinal)
  private void addEnumConstructor(List<Method> methods) {
    Method enumConstructor =
        new Method(
            ctx.clazz.fqn(),
            of(AccessFlag.ACC_PRIVATE), // Enum constructors are private
            "void",
            id("<init>"),
            of(new Argument("java.lang.String", id("name")), new Argument("int", id("ordinal"))),
            emptyList() // Empty body - will call super constructor
            );
    ctx.constPool.addUtf8(enumConstructor.name.val.toString());
    ctx.constPool.addUtf8(TypeUtils.methodDescr(enumConstructor));
    methods.add(enumConstructor);
  }

  // Transform user-defined enum constructors to include implicit enum parameters
  private void transformEnumConstructors(List<Method> methods) {
    for (Method constructor : ctx.clazz.getConstructors()) {
      // Prepend the implicit enum parameters (name, ordinal) to the constructor's argument list
      List<Argument> newArgs = new ArrayList<>();
      newArgs.add(new Argument("java.lang.String", id("name")));
      newArgs.add(new Argument("int", id("ordinal")));
      newArgs.addAll(constructor.args);

      // Update the constructor's argument list
      constructor.args = newArgs;

      // Update the method descriptor in the constant pool
      ctx.constPool.addUtf8(constructor.name.val.toString());
      ctx.constPool.addUtf8(TypeUtils.methodDescr(constructor));
    }
  }

  // Add $VALUES field for enums
  private void addEnumValuesField() {
    String arrayType = "[L" + ctx.clazz.fqn('/') + ";";
    String elementType = "L" + ctx.clazz.fqn('/') + ";";
    ctx.classIdMap.variables.putField(
        id("$VALUES"),
        new VariableInfo(
            "$VALUES",
            REF,
            elementType, // Element type, not array type
            of(AccessFlag.ACC_PRIVATE, AccessFlag.ACC_STATIC),
            true,
            ctx.constPool.indexOf(
                CONSTANT_FIELDREF,
                ctx.clazz.fqn('/'),
                "$VALUES",
                arrayType), // Use full array type for constant pool
            null));
  }
}
