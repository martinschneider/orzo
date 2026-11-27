package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_ABSTRACT;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_FINAL;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_PRIVATE;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_PROTECTED;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_PUBLIC;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_STATIC;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_SYNCHRONIZED;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_TRANSIENT;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_VOLATILE;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks the declarations of the classes before their code is generated: file names, cyclic
 * inheritance, modifiers, overrides and unimplemented abstract methods.
 */
public class ClassChecker {
  private static final String LOG_NAME = "class checker";
  private CompilerErrors errors;
  private List<Clazz> clazzes;

  public ClassChecker(CompilerErrors errors, List<Clazz> clazzes) {
    this.errors = errors;
    this.clazzes = clazzes;
  }

  public void check() {
    Set<String> names = new HashSet<>();
    for (Clazz clazz : clazzes) {
      errors.file = clazz.sourceFile;
      errors.position = errors.startTokens.get(clazz);
      checkFileName(clazz);
      if (!names.add(clazz.fqn())) {
        error(
            clazz.outerName != null
                ? "class "
                    + clazz.name
                    + " is already defined in class "
                    + clazz.outerName.substring(clazz.outerName.lastIndexOf('$') + 1)
                : "duplicate class: " + clazz.fqn());
        continue;
      }
      checkDuplicateMembers(clazz);
      checkBlankFinals(clazz);
      if (isCyclic(clazz)) {
        error("cyclic inheritance involving " + clazz.name);
        continue;
      }
      checkSupertypes(clazz);
      checkModifiers(clazz);
      for (Method method : clazz.methods) {
        if (isOverridable(method)) {
          errors.position = errors.startTokens.getOrDefault(method, errors.position);
          checkOverride(clazz, method);
        }
      }
      errors.position = errors.startTokens.get(clazz);
      checkAbstractMethodsImplemented(clazz);
    }
    errors.position = null;
  }

  // A final field without an initializer must be assigned: a static one in a static initializer, an
  // instance one in an instance initializer or in every constructor (not calling "this(...)").
  // (Only whether it is assigned at all, not on every path.)
  private void checkBlankFinals(Clazz clazz) {
    if (clazz.isInterface) {
      return;
    }
    Set<String> staticAssigned = new HashSet<>();
    for (Method method : clazz.methods) {
      if (method.name.val.toString().equals("<clinit>")) {
        staticAssigned.addAll(Statements.assignedNames(method.body));
      }
    }
    Set<String> initAssigned = Statements.assignedNames(clazz.instanceInits);
    for (ParallelDeclaration pDecl : clazz.fields) {
      for (Declaration decl : pDecl.declarations) {
        String name = decl.name.val.toString();
        if (!decl.accFlags.contains(ACC_FINAL) || decl.val != null || decl.enumArgs != null) {
          continue;
        }
        boolean assigned =
            decl.accFlags.contains(ACC_STATIC)
                ? staticAssigned.contains(name)
                : initAssigned.contains(name) || assignedByConstructors(clazz, name);
        if (!assigned) {
          errors.position = errors.startTokens.getOrDefault(pDecl, errors.position);
          error("variable " + name + " might not have been initialized");
        }
      }
    }
    errors.position = errors.startTokens.get(clazz);
  }

  private static boolean assignedByConstructors(Clazz clazz, String name) {
    for (Method method : clazz.getConstructors()) {
      boolean delegates =
          !method.body.isEmpty()
              && method.body.get(0) instanceof MethodCall
              && ((MethodCall) method.body.get(0)).name.equals("this");
      if (!delegates && !Statements.assignedNames(method.body).contains(name)) {
        return false;
      }
    }
    return true;
  }

  // fields of the same name, methods with the same parameter types
  private void checkDuplicateMembers(Clazz clazz) {
    Set<String> fields = new HashSet<>();
    for (ParallelDeclaration pDecl : clazz.fields) {
      for (Declaration decl : pDecl.declarations) {
        String name = decl.name.val.toString();
        if (!fields.add(name)) {
          errors.position = errors.startTokens.getOrDefault(pDecl, errors.position);
          error("variable " + name + " is already defined in class " + clazz.name);
        }
      }
    }
    Set<String> signatures = new HashSet<>();
    for (Method method : clazz.methods) {
      String name = method.name.val.toString();
      if (name.equals("<clinit>")) {
        continue; // (one per static initializer)
      }
      String descr = TypeUtils.methodDescr(method, clazzes, clazz);
      if (!signatures.add(name + descr.substring(0, descr.indexOf(')') + 1))) {
        errors.position = errors.startTokens.getOrDefault(method, errors.position);
        error(
            (method instanceof Constructor ? "constructor " + clazz.name : "method " + name)
                + "("
                + params(method)
                + ") is already defined in class "
                + clazz.name);
      }
    }
    errors.position = errors.startTokens.get(clazz);
  }

  private void checkFileName(Clazz clazz) {
    String file = clazz.name + ".java";
    if (clazz.outerName == null
        && clazz.modifiers.contains(ACC_PUBLIC)
        && clazz.sourceFile != null
        && !clazz.sourceFile.equals(file)) {
      error("class " + clazz.name + " is public, should be declared in a file named " + file);
    }
  }

  // A class depending on itself through its superclass or superinterfaces (also by naming a class
  // nested in it, e.g. "class A implements A.Named")
  private boolean isCyclic(Clazz clazz) {
    List<Clazz> todo = new ArrayList<>(supertypes(clazz));
    List<Clazz> seen = new ArrayList<>();
    while (!todo.isEmpty()) {
      Clazz c = todo.remove(todo.size() - 1);
      if (c == clazz || FieldProcessor.enclosingClasses(c, clazzes).contains(clazz)) {
        return true;
      } else if (!seen.contains(c)) {
        seen.add(c);
        todo.addAll(supertypes(c));
      }
    }
    return false;
  }

  // the direct supertypes of this compilation run
  private List<Clazz> supertypes(Clazz clazz) {
    List<Clazz> supertypes = new ArrayList<>();
    List<String> names = new ArrayList<>(clazz.interfaces);
    names.add(clazz.baseClass);
    for (String name : names) {
      Clazz supertype = TypeUtils.sourceClazz(name, clazzes);
      if (supertype != null) {
        supertypes.add(supertype);
      }
    }
    return supertypes;
  }

  // not a final class (or an enum) as the superclass, not an interface as the superclass and not a
  // class as a superinterface
  private void checkSupertypes(Clazz clazz) {
    if (clazz.baseClass != null && !Clazz.JAVA_LANG_OBJECT.equals(clazz.baseClass)) {
      Clazz base = TypeUtils.sourceClazz(clazz.baseClass, clazzes);
      Class<?> reflected = (base == null) ? reflect(clazz.baseClass) : null;
      String name = clazz.baseClass.substring(clazz.baseClass.lastIndexOf('.') + 1);
      if ((base != null && (base.modifiers.contains(ACC_FINAL) || base.isEnum))
          || (reflected != null
              && !reflected.isInterface()
              && Modifier.isFinal(reflected.getModifiers()))) {
        error("cannot inherit from final " + name.substring(name.lastIndexOf('$') + 1));
      } else if (!clazz.isInterface
          && ((base != null && base.isInterface)
              || (reflected != null && reflected.isInterface()))) {
        error("no interface expected here");
      }
    }
    if (!clazz.isInterface) {
      for (String name : clazz.interfaces) {
        Clazz iface = TypeUtils.sourceClazz(name, clazzes);
        Class<?> reflected = (iface == null) ? reflect(name) : null;
        if ((iface != null && !iface.isInterface)
            || (reflected != null && !reflected.isInterface())) {
          error("interface expected here");
        }
      }
    }
  }

  private static Class<?> reflect(String name) {
    try {
      return Class.forName(name.replace('/', '.'));
    } catch (ClassNotFoundException | LinkageError e) {
      return null;
    }
  }

  private void checkModifiers(Clazz clazz) {
    if (clazz.outerName == null) {
      for (AccessFlag flag : List.of(ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC)) {
        if (clazz.modifiers.contains(flag)) {
          error("modifier " + name(flag) + " not allowed here");
        }
      }
    }
    if (clazz.modifiers.contains(ACC_ABSTRACT) && clazz.modifiers.contains(ACC_FINAL)) {
      error("illegal combination of modifiers: abstract and final");
    }
    notAllowed(clazz.modifiers, List.of(ACC_SYNCHRONIZED, ACC_TRANSIENT, ACC_VOLATILE));
    for (ParallelDeclaration pDecl : clazz.fields) {
      errors.position = errors.startTokens.getOrDefault(pDecl, errors.position);
      for (Declaration decl : pDecl.declarations) {
        notAllowed(decl.accFlags, List.of(ACC_ABSTRACT, ACC_SYNCHRONIZED));
        if (clazz.isInterface) {
          notAllowed(
              decl.accFlags, List.of(ACC_PRIVATE, ACC_PROTECTED, ACC_TRANSIENT, ACC_VOLATILE));
        }
        break; // (the same modifiers)
      }
    }
    for (Method method : clazz.methods) {
      errors.position = errors.startTokens.getOrDefault(method, errors.position);
      notAllowed(method.accFlags, List.of(ACC_TRANSIENT, ACC_VOLATILE));
      if (clazz.isInterface) {
        notAllowed(method.accFlags, List.of(ACC_PROTECTED, ACC_FINAL, ACC_SYNCHRONIZED));
      } else if (clazz.isEnum && method instanceof Constructor) {
        notAllowed(method.accFlags, List.of(ACC_PUBLIC, ACC_PROTECTED));
      }
    }
    errors.position = errors.startTokens.get(clazz);
    for (Method method : clazz.methods) {
      if (method.accFlags.contains(ACC_ABSTRACT)) {
        for (AccessFlag flag : List.of(ACC_PRIVATE, ACC_STATIC, ACC_FINAL)) {
          if (method.accFlags.contains(flag)) {
            errors.position = errors.startTokens.getOrDefault(method, errors.position);
            error("illegal combination of modifiers: abstract and " + name(flag));
          }
        }
      }
    }
    errors.position = errors.startTokens.get(clazz);
  }

  // A method overriding one of a supertype: the same return type (or a subtype), no weaker access,
  // and not a final one
  private void checkOverride(Clazz clazz, Method method) {
    String descr = TypeUtils.methodDescr(method, clazzes, clazz);
    String params = descr.substring(0, descr.indexOf(')') + 1);
    for (Overridable overridden : supertypeMethods(clazz)) {
      if (!overridden.name.equals(method.name.val.toString())
          || !overridden.descr.startsWith(params)
          || overridden.isStatic
          || overridden.isPrivate) {
        continue;
      }
      String prefix =
          signature(method) + " in " + clazz.name + " cannot override " + overridden.display();
      String returnType = descr.substring(params.length());
      String overriddenReturn = overridden.descr.substring(params.length());
      if (overridden.isFinal) {
        error(prefix + "; overridden method is final");
      } else if (!clazz.isInterface && access(method.accFlags) < overridden.access) {
        error(
            prefix
                + "; attempting to assign weaker access privileges; was "
                + accessName(overridden.access));
      } else if (!isReturnTypeSubstitutable(returnType, overriddenReturn)) {
        error(prefix + "; return type " + typeName(returnType) + " is not compatible");
      }
      return;
    }
  }

  // A class that isn't abstract must implement the abstract methods of its supertypes (only checked
  // for supertypes without type parameters: their erased signatures may differ).
  private void checkAbstractMethodsImplemented(Clazz clazz) {
    // (enum constants with a class body aren't supported)
    if (clazz.isAbstract || clazz.isInterface) {
      return;
    }
    List<Overridable> abstractMethods = new ArrayList<>();
    for (Method method : clazz.methods) {
      if (method.accFlags.contains(ACC_ABSTRACT)) {
        abstractMethods.add(new Overridable(clazz, method, clazzes));
      }
    }
    List<Overridable> concrete = new ArrayList<>();
    for (Method method : clazz.methods) {
      if (!method.accFlags.contains(ACC_ABSTRACT)) {
        concrete.add(new Overridable(clazz, method, clazzes));
      }
    }
    for (Overridable method : supertypeMethods(clazz)) {
      if (method.isAbstract && !method.isGenericDeclarer) {
        abstractMethods.add(method);
      } else if (!method.isAbstract) {
        concrete.add(method);
      }
    }
    for (Overridable method : abstractMethods) {
      if (!isImplemented(method, concrete)) {
        error(
            clazz.name
                + " is not abstract and does not override abstract method "
                + method.display());
        return;
      }
    }
  }

  private static boolean isImplemented(Overridable method, List<Overridable> concrete) {
    String params = method.descr.substring(0, method.descr.indexOf(')') + 1);
    for (Overridable candidate : concrete) {
      if (candidate.name.equals(method.name) && candidate.descr.startsWith(params)) {
        return true;
      }
    }
    return false;
  }

  // the methods of all supertypes (of this compilation run or loaded by reflection)
  private List<Overridable> supertypeMethods(Clazz clazz) {
    List<Overridable> methods = new ArrayList<>();
    List<String> reflected = new ArrayList<>();
    collectSupertypes(clazz, methods, reflected, new ArrayList<>());
    for (String name : reflected) {
      try {
        Class<?> type = Class.forName(name.replace('/', '.'), false, getClass().getClassLoader());
        for (java.lang.reflect.Method method : type.getMethods()) {
          methods.add(new Overridable(method));
        }
        for (java.lang.reflect.Method method : type.getDeclaredMethods()) {
          if (Modifier.isProtected(method.getModifiers())) {
            methods.add(new Overridable(method));
          }
        }
      } catch (ClassNotFoundException | LinkageError e) {
        // (an unknown supertype is reported elsewhere)
      }
    }
    return methods;
  }

  private void collectSupertypes(
      Clazz clazz, List<Overridable> methods, List<String> reflected, List<Clazz> seen) {
    List<String> supertypes = new ArrayList<>();
    if (clazz.baseClass != null) {
      supertypes.add(clazz.baseClass);
    }
    supertypes.addAll(clazz.interfaces);
    for (String name : supertypes) {
      Clazz supertype = TypeUtils.sourceClazz(name, clazzes);
      if (supertype == null) {
        if (!reflected.contains(name)) {
          reflected.add(name);
        }
      } else if (!seen.contains(supertype)) {
        seen.add(supertype);
        for (Method method : supertype.methods) {
          if (isOverridable(method)) {
            methods.add(new Overridable(supertype, method, clazzes));
          }
        }
        collectSupertypes(supertype, methods, reflected, seen);
      }
    }
  }

  private static boolean isOverridable(Method method) {
    String name = method.name.val.toString();
    return !(method instanceof Constructor)
        && !name.startsWith("<")
        && !method.accFlags.contains(ACC_STATIC)
        && !method.accFlags.contains(ACC_PRIVATE);
  }

  // (a reference return type may be a subtype of the overridden one)
  private boolean isReturnTypeSubstitutable(String returnType, String overridden) {
    if (returnType.equals(overridden)) {
      return true;
    }
    if (!returnType.startsWith("L") || !overridden.startsWith("L")) {
      return false;
    }
    String type = typeName(returnType);
    String overriddenType = typeName(overridden);
    for (String assignable : TypeUtils.assignableTo(type, clazzes)) {
      if (assignable.equals(overriddenType)) {
        return true;
      }
    }
    return false;
  }

  // e.g. "java.lang.String" for "Ljava/lang/String;"
  private static String typeName(String descr) {
    if (descr.startsWith("L") && descr.endsWith(";")) {
      return descr.substring(1, descr.length() - 1).replace('/', '.');
    }
    return TypeUtils.jvmDescriptorToTypeName(descr);
  }

  private static String signature(Method method) {
    return method.name.val + "(" + params(method) + ")";
  }

  // the parameter types as written, e.g. "int,Object..."
  private static String params(Method method) {
    List<String> types = new ArrayList<>();
    for (io.github.martinschneider.orzo.parser.productions.Argument arg : method.args) {
      // e.g. "Object..." or "int[]" for "[java/lang/Object" or "[I"
      String type = arg.type.replace('/', '.');
      int dims = 0;
      while (type.startsWith("[")) {
        type = type.substring(1);
        dims++;
      }
      if (dims > 0) {
        type = TypeUtils.jvmDescriptorToTypeName(type).replace('/', '.');
      }
      type = type.substring(type.lastIndexOf('.') + 1);
      for (int i = 0; i < dims; i++) {
        type += (arg.varargs && i == dims - 1) ? "..." : "[]";
      }
      types.add(type);
    }
    return String.join(",", types);
  }

  // 3: public, 2: protected, 1: package, 0: private
  static int access(List<AccessFlag> flags) {
    if (flags.contains(ACC_PUBLIC)) {
      return 3;
    } else if (flags.contains(ACC_PROTECTED)) {
      return 2;
    } else if (flags.contains(ACC_PRIVATE)) {
      return 0;
    }
    return 1;
  }

  private static String accessName(int access) {
    return List.of("private", "package", "protected", "public").get(access);
  }

  private void notAllowed(List<AccessFlag> modifiers, List<AccessFlag> forbidden) {
    for (AccessFlag flag : forbidden) {
      if (modifiers.contains(flag)) {
        error("modifier " + name(flag) + " not allowed here");
      }
    }
  }

  private static String name(AccessFlag flag) {
    return flag.name().substring("ACC_".length()).toLowerCase();
  }

  private void error(String message) {
    errors.addError(LOG_NAME, message, new RuntimeException().getStackTrace());
  }

  // a method of a supertype, from the source or loaded by reflection
  private static class Overridable {
    String name;
    String params; // e.g. "int,String"
    String descr;
    String owner;
    int access;
    boolean isStatic;
    boolean isPrivate;
    boolean isFinal;
    boolean isAbstract;
    // (the declaring type has type parameters)
    boolean isGenericDeclarer;

    Overridable(Clazz clazz, Method method, List<Clazz> clazzes) {
      name = method.name.val.toString();
      params = params(method);
      descr = TypeUtils.methodDescr(method, clazzes, clazz);
      owner = clazz.name;
      // (interface methods are public)
      access = clazz.isInterface ? 3 : access(method.accFlags);
      isStatic = method.accFlags.contains(ACC_STATIC);
      isPrivate = method.accFlags.contains(ACC_PRIVATE);
      isFinal = method.accFlags.contains(ACC_FINAL);
      isAbstract = method.accFlags.contains(ACC_ABSTRACT);
      isGenericDeclarer = clazz.isGeneric;
    }

    Overridable(java.lang.reflect.Method method) {
      int mods = method.getModifiers();
      name = method.getName();
      List<String> types = new ArrayList<>();
      for (Class<?> type : method.getParameterTypes()) {
        types.add(type.getSimpleName());
      }
      params = String.join(",", types);
      descr =
          MethodType.methodType(method.getReturnType(), method.getParameterTypes())
              .toMethodDescriptorString();
      owner = method.getDeclaringClass().getSimpleName();
      access = Modifier.isPublic(mods) ? 3 : (Modifier.isProtected(mods) ? 2 : 1);
      isStatic = Modifier.isStatic(mods);
      isPrivate = Modifier.isPrivate(mods);
      isFinal = Modifier.isFinal(mods);
      isAbstract = Modifier.isAbstract(mods);
      isGenericDeclarer =
          method.getDeclaringClass().getTypeParameters().length > 0
              || method.getTypeParameters().length > 0;
    }

    String display() {
      return name + "(" + params + ") in " + owner;
    }
  }
}
