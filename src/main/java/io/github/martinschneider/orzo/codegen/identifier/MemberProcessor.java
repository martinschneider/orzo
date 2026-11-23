package io.github.martinschneider.orzo.codegen.identifier;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static java.util.Collections.emptyList;
import static java.util.List.of;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.FieldProcessor;
import io.github.martinschneider.orzo.codegen.GenericTypes;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemberProcessor {

  public CGContext ctx;
  // the implicit first parameters of an enum constructor (names a Java parameter can't clash with)
  public static final String ENUM_NAME_PARAM = "$enum$name";
  public static final String ENUM_ORDINAL_PARAM = "$enum$ordinal";

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
        if (!TypeUtils.typeExists(decl.type, ctx.allClazzes, ctx.clazz)) {
          ctx.errors.addError(
              ctx.errors.startTokens.get(pDecl),
              "member processor",
              "cannot find class " + decl.type.substring(decl.type.lastIndexOf('.') + 1),
              new RuntimeException().getStackTrace());
        }
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
        ctx.classIdMap.variables.fieldMap.get(decl.name.id().toString()).genericType =
            decl.genericType;
        if (decl.genericType != null) {
          // for the field's Signature attribute (so reflection/javac see its type arguments)
          ctx.constPool.addUtf8("Signature");
          ctx.constPool.addUtf8(GenericTypes.toSignature(decl.genericType));
        }
        if (decl.accFlags.contains(AccessFlag.ACC_FINAL) && decl.val != null) {
          ctx.constPool.addUtf8("ConstantValue");
          ctx.constPool.addByType(decl.type, decl.val.getConstantValue(decl.type));
        }
      }
    }
    // Add inherited instance fields from parent class so they are accessible in this class
    String baseClass = ctx.clazz.baseClass;
    if (baseClass != null && !Clazz.JAVA_LANG_OBJECT.equals(baseClass)) {
      FieldProcessor fieldProcessor = new FieldProcessor();
      Map<String, FieldProcessor.InstanceField> parentFields =
          fieldProcessor.getInstanceFieldMap(baseClass, ctx.allClazzes);
      for (Map.Entry<String, FieldProcessor.InstanceField> entry : parentFields.entrySet()) {
        String fieldName = entry.getKey();
        if (!ctx.classIdMap.variables.fieldMap.containsKey(fieldName)) {
          FieldProcessor.InstanceField inheritedField = entry.getValue();
          String fieldType = inheritedField.fieldType;
          VariableInfo inheritedVar =
              new VariableInfo(
                  fieldName,
                  fieldType,
                  null,
                  emptyList(),
                  true,
                  ctx.constPool.indexOf(
                      CONSTANT_FIELDREF, ctx.clazz.fqn('/'), fieldName, TypeUtils.descr(fieldType)),
                  null);
          inheritedVar.genericType = inheritedField.genericType;
          ctx.classIdMap.variables.putInheritedField(fieldName, inheritedVar);
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
          if (decl.isField) {
            continue; // field declarations are in fieldMap, not localMap
          }
          VariableInfo localVar =
              new VariableInfo(
                  decl.name.id().toString(),
                  (decl.arrDim > 0) ? REF : decl.type,
                  (decl.arrDim > 0) ? decl.type : null,
                  decl.accFlags,
                  false,
                  (byte) ctx.classIdMap.variables.localSize,
                  decl.val);
          localVar.genericType = decl.genericType;
          ctx.classIdMap.variables.putLocal(decl.name, localVar);
        }
      }
    }
  }

  // add all method args (=local args) to the constant pool and idMap
  public void processMethodArgs(Method method) {
    for (Argument arg : method.args) {
      String type = arg.type;
      String arrayType = null;
      if (arg.type.startsWith("[")) {
        type = REF;
        arrayType = arg.type.replaceAll("\\[", "");
      }
      VariableInfo argVar =
          new VariableInfo(
              arg.name.val.toString(),
              type,
              arrayType,
              emptyList(),
              false,
              (byte) ctx.classIdMap.variables.localSize,
              null);
      argVar.genericType = arg.genericType;
      ctx.classIdMap.variables.putLocal(arg.name, argVar);
    }
  }

  // add initializer code for fields
  public void addClInit(List<Method> methods) {
    // Handle enum-specific initialization
    // (the enum constants are created by the synthesized start of <clinit> and the constructors
    // call Enum.<init>(name, ordinal) first, see MethodGenerator; field initializers, static
    // blocks and constructor bodies are handled like in any other class)
    if (ctx.clazz.isEnum) {
      addEnumMethods(methods);
      if (ctx.clazz.getConstructors().isEmpty()) {
        addEnumConstructor(methods);
      } else {
        transformEnumConstructors(methods);
      }
    }

    List<ParallelDeclaration> staticInits = new ArrayList<>();
    List<ParallelDeclaration> fields =
        (ctx.clazz.fields != null) ? ctx.clazz.fields : new ArrayList<>();
    for (ParallelDeclaration pDecl : fields) {
      if (initializedIn(pDecl, true)) {
        staticInits.add(pDecl);
      }
    }
    // the instance field initializers and initializer blocks, in source order
    List<Statement> constrInits = new ArrayList<>();
    for (Statement init : ctx.clazz.instanceInits) {
      if (!(init instanceof ParallelDeclaration)
          || initializedIn((ParallelDeclaration) init, false)) {
        constrInits.add(init);
      }
    }
    // Merge all static initializer blocks (in source order) into the first one: a class file may
    // only contain a single <clinit>.
    Method userClinit = null;
    for (int i = 0; i < methods.size(); i++) {
      Method m = methods.get(i);
      if ("<clinit>".equals(m.name.val.toString())) {
        if (userClinit == null) {
          userClinit = m;
        } else {
          List<Statement> merged = new ArrayList<>(userClinit.body);
          merged.addAll(m.body);
          userClinit.body = merged;
          methods.remove(i);
          i--;
        }
      }
    }
    if (!staticInits.isEmpty() || userClinit != null || ctx.clazz.isEnum) {
      if (userClinit != null) {
        List<Statement> merged = new ArrayList<>(staticInits);
        merged.addAll(userClinit.body);
        userClinit.body = merged;
        ctx.constPool.addUtf8(userClinit.name.val.toString());
        ctx.constPool.addUtf8(TypeUtils.methodDescr(userClinit));
      } else {
        Method clInit =
            new Method(
                "", of(AccessFlag.ACC_STATIC), "void", id("<clinit>"), emptyList(), emptyList());
        ctx.constPool.addUtf8(clInit.name.val.toString());
        ctx.constPool.addUtf8(TypeUtils.methodDescr(clInit));
        List<Statement> statements = new ArrayList<>(staticInits);
        clInit.body = statements;
        methods.add(clInit);
      }
    }
    if (!constrInits.isEmpty()) {
      List<Method> constructors = ctx.clazz.getConstructors();
      if (constructors.isEmpty()) {
        ctx.errors.addError(
            "codegen", "missing default constructor", new RuntimeException().getStackTrace());
      }
      // add initializer calls after super();
      for (Method constr : constructors) {
        // Constructors delegating to this() must not receive field inits
        if (ctx.methodGen.startsWithCallToThis(constr.body)) {
          continue;
        }
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

  // Whether a field declaration is initialized in <clinit> (static) or the constructors (a constant
  // final field is initialized by its ConstantValue attribute instead)
  private static boolean initializedIn(ParallelDeclaration pDecl, boolean isStatic) {
    for (Declaration decl : pDecl.declarations) {
      if (decl.enumArgs != null) {
        continue; // an enum constant
      }
      boolean isConstantFinal =
          decl.accFlags.contains(AccessFlag.ACC_FINAL)
              && decl.val != null
              && decl.val.getConstantValue(decl.type) != null;
      if (decl.val != null && !isConstantFinal) {
        return decl.accFlags.contains(AccessFlag.ACC_STATIC) == isStatic;
      }
    }
    return false;
  }

  // Constant pool entries for the code of the enum methods values(), valueOf() and $values()
  private void addEnumMethods(List<Method> methods) {
    // Add required constants to pool for enum method generation
    String arrayType = "[L" + ctx.clazz.fqn('/') + ";";

    // Add constants needed for values() method
    ctx.constPool.addMethodRef("java/lang/Object", "clone", "()Ljava/lang/Object;");
    ctx.constPool.addClass(arrayType);

    // Add constants needed for valueOf() method
    ctx.constPool.addClass(ctx.clazz.fqn('/'));
    ctx.constPool.addMethodRef(
        "java/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;");

    // Add constants needed for $values() method
    ctx.constPool.addClass(ctx.clazz.fqn('/'));

    // (the methods themselves are declared by ClassParser)
    for (Method method : methods) {
      String name = method.name.val.toString();
      if (List.of("values", "valueOf", "$values").contains(name)) {
        ctx.constPool.addUtf8(name);
        ctx.constPool.addUtf8(TypeUtils.methodDescr(method));
      }
    }
  }

  // Add enum constructor that takes (String name, int ordinal)
  private void addEnumConstructor(List<Method> methods) {
    Method enumConstructor =
        new Method(
            ctx.clazz.fqn(),
            of(AccessFlag.ACC_PRIVATE), // Enum constructors are private
            "void",
            id("<init>"),
            of(
                new Argument("java.lang.String", id(ENUM_NAME_PARAM)),
                new Argument("int", id(ENUM_ORDINAL_PARAM))),
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
      newArgs.add(new Argument("java.lang.String", id(ENUM_NAME_PARAM)));
      newArgs.add(new Argument("int", id(ENUM_ORDINAL_PARAM)));
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
