package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.*;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.*;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class MethodGenerator {

  public CGContext ctx;

  public MethodGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public HasOutput generate(HasOutput out, Method method, Clazz clazz) {
    out.write(method.accessFlags(clazz.isInterface));
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, method.name.val));
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, TypeUtils.methodDescr(method)));
    DynamicByteArray methodOut = new DynamicByteArray();
    if (!clazz.isInterface) {
      out.write((short) 1); // attribute size
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "Code"));
      boolean returned = false;

      // Handle special enum methods
      if (clazz.isEnum && generateEnumMethod(methodOut, method, clazz)) {
        returned = true;
      } else {
        if (method instanceof Constructor && !startsWithCallToSuper(method.body)) {
          ctx.methodCallGen.callSuperConstr(methodOut);
        }
        for (Statement stmt : method.body) {
          generateCode(methodOut, method, stmt);
          if (stmt instanceof ReturnStatement) {
            returned = true;
          }
        }
      }

      if (!returned) {
        methodOut.write(RETURN);
      }
      out.write(methodOut.size() + 12); // stack size (2) + local var size (2) + code size (4) +
      // exception table size (2) + attribute count size (2)
      // Set appropriate max stack size for enum methods
      short maxStackSize = (short) (ctx.opStack.maxSize() + 1);
      if (clazz.isEnum && generateEnumMethod(new DynamicByteArray(), method, clazz)) {
        // Enum methods need larger stack: NEW + DUP + name + ordinal + constructor args = 5+ slots
        maxStackSize = 6; // Increased to handle constructor arguments
      }
      out.write(maxStackSize); // max stack size
      out.write((short) (ctx.classIdMap.variables.localSize + 1)); // max local var size
      out.write(methodOut.size());
      out.write(methodOut.flush());
      out.write((short) 0); // exception table of size 0
      out.write((short) 0); // attribute count for this attribute of 0
    } else {
      out.write((short) 0); // attribute size
      out.write(methodOut.flush());
    }
    ctx.opStack.reset();
    return out;
  }

  public boolean startsWithCallToSuper(List<Statement> body) {
    if (body.isEmpty() || !(body.get(0) instanceof MethodCall)) {
      return false;
    }
    MethodCall call = (MethodCall) body.get(0);
    return "super".equals(call.name);
  }

  private void generateCode(DynamicByteArray out, Method method, Statement stmt) {
    ctx.delegator.generate(out, method, stmt);
  }

  // Generate bytecode for special enum methods
  private boolean generateEnumMethod(DynamicByteArray out, Method method, Clazz clazz) {
    String methodName = method.name.val.toString();

    if ("values".equals(methodName)) {
      generateValuesMethod(out, clazz);
      return true;
    } else if ("valueOf".equals(methodName)) {
      generateValueOfMethod(out, clazz);
      return true;
    } else if ("$values".equals(methodName)) {
      generateDollarValuesMethod(out, clazz);
      return true;
    } else if ("<clinit>".equals(methodName)) {
      generateEnumStaticInit(out, clazz);
      return true;
    } else if ("<init>".equals(methodName)) {
      generateEnumConstructor(out, method, clazz);
      return true;
    }

    return false;
  }

  // Generate values() method: return $VALUES.clone()
  private void generateValuesMethod(DynamicByteArray out, Clazz clazz) {
    // Get static field $VALUES: getstatic $VALUES
    out.write(GETSTATIC);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_FIELDREF, clazz.fqn('/'), "$VALUES", "[L" + clazz.fqn('/') + ";"));

    // Call clone() method: invokevirtual clone
    out.write(INVOKEVIRTUAL);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_METHODREF, "java/lang/Object", "clone", "()Ljava/lang/Object;"));

    // Cast to correct array type: checkcast
    out.write(CHECKCAST);
    out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, "[L" + clazz.fqn('/') + ";"));

    // Return: areturn
    out.write(ARETURN);
  }

  // Generate valueOf(String) method: return Enum.valueOf(Class, String)
  private void generateValueOfMethod(DynamicByteArray out, Clazz clazz) {
    // Load class constant: ldc EnumType.class
    short classIndex = ctx.constPool.indexOf(CONSTANT_CLASS, clazz.fqn('/'));
    if (classIndex <= 255) {
      out.write(LDC);
      out.write((byte) classIndex);
    } else {
      out.write(LDC_W);
      out.write(classIndex);
    }

    // Load string parameter: aload_0
    out.write(ALOAD_0);

    // Call Enum.valueOf: invokestatic Enum.valueOf
    out.write(INVOKESTATIC);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_METHODREF,
                "java/lang/Enum",
                "valueOf",
                "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;"));

    // Cast to correct enum type: checkcast
    out.write(CHECKCAST);
    out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, clazz.fqn('/')));

    // Return: areturn
    out.write(ARETURN);
  }

  // Generate $values() helper method: return new array with all enum constants
  private void generateDollarValuesMethod(DynamicByteArray out, Clazz clazz) {
    // Get enum constants from the first field (which should be the enum constants)
    List<Declaration> enumConstants = getEnumConstants(clazz);
    int numConstants = enumConstants.size();

    // Create new array: new EnumType[numConstants]
    if (numConstants <= 127) {
      out.write(BIPUSH);
      out.write((byte) numConstants);
    } else {
      out.write(SIPUSH);
      out.write((short) numConstants);
    }
    out.write(ANEWARRAY);
    out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, clazz.fqn('/')));

    // Populate array with enum constants
    for (int i = 0; i < numConstants; i++) {
      Declaration constant = enumConstants.get(i);
      out.write(DUP); // Duplicate array reference

      // Array index
      if (i <= 127) {
        if (i <= 5) {
          out.write((byte) (ICONST_0 + i)); // ICONST_0 to ICONST_5
        } else {
          out.write(BIPUSH);
          out.write((byte) i);
        }
      } else {
        out.write(SIPUSH);
        out.write((short) i);
      }

      // Get enum constant: getstatic EnumType.CONSTANT_NAME
      out.write(GETSTATIC);
      out.write(
          (short)
              ctx.constPool.indexOf(
                  CONSTANT_FIELDREF,
                  clazz.fqn('/'),
                  constant.name.id().toString(),
                  "L" + clazz.fqn('/') + ";"));

      // Store in array: aastore
      out.write(AASTORE);
    }

    // Return array: areturn
    out.write(ARETURN);
  }

  // Generate <clinit> static initializer: create all enum instances and $VALUES array
  private void generateEnumStaticInit(DynamicByteArray out, Clazz clazz) {
    List<Declaration> enumConstants = getEnumConstants(clazz);

    // Create each enum constant
    for (int i = 0; i < enumConstants.size(); i++) {
      Declaration constant = enumConstants.get(i);

      // Create new enum instance: new EnumType("CONSTANT_NAME", ordinal)
      out.write(NEW);
      out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, clazz.fqn('/')));
      out.write(DUP);

      // Push string name of constant
      short stringIndex = ctx.constPool.indexOf(CONSTANT_STRING, constant.name.id().toString());
      if (stringIndex <= 255) {
        out.write(LDC);
        out.write((byte) stringIndex);
      } else {
        out.write(LDC_W);
        out.write(stringIndex);
      }

      // Push ordinal (index)
      if (i <= 5) {
        out.write((byte) (ICONST_0 + i)); // ICONST_0 to ICONST_5
      } else if (i <= 127) {
        out.write(BIPUSH);
        out.write((byte) i);
      } else {
        out.write(SIPUSH);
        out.write((short) i);
      }

      // Push constructor arguments if any (from enum constant declaration)
      List<String> argTypes = new ArrayList<>();
      argTypes.add("Ljava/lang/String;"); // name parameter
      argTypes.add("I"); // ordinal parameter

      if (constant.val != null) {
        // Generate code for constructor arguments based on actual argument type
        // Determine the argument type from the enum constructor
        List<Method> constructors = clazz.getConstructors();
        if (!constructors.isEmpty()) {
          Method constructor = constructors.get(0); // Use first constructor
          if (constructor.args.size() > 2) { // More than name and ordinal
            String argType = constructor.args.get(2).type; // User-defined parameter type
            ctx.exprGen.eval(out, argType, constant.val);

            // Add the correct argument type descriptor
            if ("int".equals(argType)) {
              argTypes.add("I");
            } else if ("short".equals(argType)) {
              argTypes.add("S");
            } else if ("String".equals(argType)) {
              argTypes.add("Ljava/lang/String;");
            } else {
              argTypes.add("L" + argType.replace('.', '/') + ";"); // Default reference type
            }
          }
        }
      }

      // Build constructor signature
      StringBuilder signature = new StringBuilder("(");
      for (String argType : argTypes) {
        signature.append(argType);
      }
      signature.append(")V");

      // Call constructor: invokespecial <init>
      out.write(INVOKESPECIAL);
      out.write(
          (short)
              ctx.constPool.indexOf(
                  CONSTANT_METHODREF, clazz.fqn('/'), "<init>", signature.toString()));

      // Store in static field: putstatic CONSTANT_NAME
      out.write(PUTSTATIC);
      out.write(
          (short)
              ctx.constPool.indexOf(
                  CONSTANT_FIELDREF,
                  clazz.fqn('/'),
                  constant.name.id().toString(),
                  "L" + clazz.fqn('/') + ";"));
    }

    // Create and store $VALUES array: putstatic $VALUES, $values()
    out.write(INVOKESTATIC);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_METHODREF, clazz.fqn('/'), "$values", "()[L" + clazz.fqn('/') + ";"));
    out.write(PUTSTATIC);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_FIELDREF, clazz.fqn('/'), "$VALUES", "[L" + clazz.fqn('/') + ";"));

    out.write(RETURN);
  }

  // Generate enum constructor: calls super(name, ordinal) and initializes instance fields
  private void generateEnumConstructor(DynamicByteArray out, Method method, Clazz clazz) {
    // Load this: aload_0
    out.write(ALOAD_0);

    // Load name parameter: aload_1 (always String name)
    out.write(ALOAD_1);

    // Load ordinal parameter: iload_2 (always int ordinal)
    out.write(ILOAD_2);

    // Call super constructor: invokespecial java/lang/Enum.<init>(String, int)
    out.write(INVOKESPECIAL);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_METHODREF, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V"));

    // Process constructor body to handle field assignments
    // The user-defined constructor body should handle field initialization
    // TODO: Fix variable resolution for transformed constructor parameters
    // for (Statement stmt : method.body) {
    //   ctx.delegator.generate(out, method, stmt);
    // }

    // Handle field assignment for enum constructors with parameters
    if (method.args.size() > 2) { // More than just name and ordinal
      // Find the field that should be assigned from the constructor parameter
      // For now, assume there's one non-static field that matches the constructor parameter
      if (clazz.fields != null) {
        for (ParallelDeclaration pDecl : clazz.fields) {
          for (Declaration field : pDecl.declarations) {
            // Skip static fields (enum constants)
            if (!field.accFlags.contains(
                io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_STATIC)) {

              // Load this: aload_0
              out.write(ALOAD_0);

              // Load constructor parameter (index 2 is the user-defined parameter)
              String paramType = method.args.get(2).type; // User-defined parameter type
              if ("int".equals(paramType)) {
                out.write(ILOAD_3); // Load int parameter
              } else if ("short".equals(paramType)) {
                out.write(ILOAD_3); // Load short parameter (uses iload like int)
              } else if ("String".equals(paramType)) {
                out.write(ALOAD_3); // Load String parameter
              } else {
                out.write(ALOAD_3); // Default to reference load
              }

              // Store in field: putfield
              out.write(PUTFIELD);
              out.write(
                  (short)
                      ctx.constPool.indexOf(
                          CONSTANT_FIELDREF,
                          clazz.fqn('/'),
                          field.name.id().toString(),
                          TypeUtils.descr(field.type)));
              break; // Assume only one field to assign
            }
          }
        }
      }
    }

    // Also handle default field values for fields not initialized by constructor
    if (clazz.fields != null) {
      for (ParallelDeclaration pDecl : clazz.fields) {
        for (Declaration decl : pDecl.declarations) {
          // Skip static fields (enum constants) and fields without default values
          if (!decl.accFlags.contains(
                  io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_STATIC)
              && decl.val != null) {
            // Load this: aload_0
            out.write(ALOAD_0);

            // Load the default value and store it in the field
            String fieldType = decl.type;
            if (fieldType.equals("String") || fieldType.equals("java.lang.String")) {
              // For string constants, load from constant pool
              String stringValue = decl.val.getConstantValue(fieldType).toString();
              ctx.loadGen.ldc(out, CONSTANT_STRING, stringValue);
            } else {
              // For other types, we'd need to handle them appropriately
              // For now, just handle String fields
              continue;
            }

            // Store in field: putfield
            out.write(PUTFIELD);
            out.write(
                (short)
                    ctx.constPool.indexOf(
                        CONSTANT_FIELDREF,
                        clazz.fqn('/'),
                        decl.name.id().toString(),
                        TypeUtils.descr(fieldType)));
          }
        }
      }
    }

    // Return: return
    out.write(RETURN);
  }

  // Helper method to get enum constants from the class fields
  private List<Declaration> getEnumConstants(Clazz clazz) {
    if (clazz.fields != null && !clazz.fields.isEmpty()) {
      // Enum constants are stored in the first ParallelDeclaration
      ParallelDeclaration enumConstantsDecl = clazz.fields.get(0);
      return enumConstantsDecl.declarations;
    }
    return java.util.Collections.emptyList();
  }
}
