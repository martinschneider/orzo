package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldProcessor {

  public static class StaticField {
    public final String className;
    public final String fieldName;
    public final String fieldType;
    public final boolean isEnum;
    // full generic type, e.g. "java.util.List<a.b.Foo>" (null if not generic or unknown)
    public String genericType;

    public StaticField(String className, String fieldName, String fieldType, boolean isEnum) {
      this.className = className;
      this.fieldName = fieldName;
      this.fieldType = fieldType;
      this.isEnum = isEnum;
    }
  }

  public static class InstanceField {
    public final String className;
    public final String fieldName;
    public final String fieldType;
    // full generic type, e.g. "java.util.List<a.b.Foo>" (null if not generic or unknown)
    public String genericType;

    public InstanceField(String className, String fieldName, String fieldType) {
      this.className = className;
      this.fieldName = fieldName;
      this.fieldType = fieldType;
    }
  }

  public Map<String, StaticField> getStaticFieldMap(Clazz currentClazz, List<Clazz> clazzes) {
    Map<String, StaticField> fieldMap = new HashMap<>();

    // Later registrations take precedence for the same simple name: the current class's own
    // fields shadow statically imported ones, which in turn shadow same-named fields of other
    // classes (e.g. a static import of OpCodes.* must win over the enum constant
    // Keywords.RETURN for a bare "RETURN").

    // Add fields from imported classes
    addImportedFields(fieldMap, currentClazz, clazzes);

    // Add fields from static imports (e.g. import static Type.VOID)
    addStaticImportFields(fieldMap, currentClazz, clazzes);

    // Add fields from current class
    addFieldsFromClass(fieldMap, currentClazz, true);

    return fieldMap;
  }

  public Map<String, InstanceField> getInstanceFieldMap(String className, List<Clazz> clazzes) {
    Map<String, InstanceField> fieldMap = new HashMap<>();

    // Clean the class name - remove L prefix and ; suffix if present
    String cleanClassName = className;
    if (cleanClassName.startsWith("L") && cleanClassName.endsWith(";")) {
      cleanClassName = cleanClassName.substring(1, cleanClassName.length() - 1);
    }
    cleanClassName = cleanClassName.replace('/', '.');

    // Find the class with the given name
    Clazz targetClass = null;
    for (Clazz clazz : clazzes) {
      if (clazz.fqn().equals(cleanClassName) || clazz.name.equals(cleanClassName)) {
        targetClass = clazz;
        break;
      }
    }

    if (targetClass != null) {
      // Add instance fields from the class
      for (ParallelDeclaration parallelDecl : targetClass.fields) {
        for (Declaration decl : parallelDecl.declarations) {
          if (isInstanceField(decl)) {
            String fieldName = decl.name.val.toString();
            String fieldType =
                decl.arrDim > 0 ? TypeUtils.descr(decl.type, decl.arrDim) : decl.type;

            InstanceField field = new InstanceField(targetClass.fqn(), fieldName, fieldType);
            field.genericType = decl.genericType;
            fieldMap.put(fieldName, field);
          }
        }
      }
      // Add inherited instance fields from parent class
      String baseClass = targetClass.baseClass;
      if (baseClass != null && !Clazz.JAVA_LANG_OBJECT.equals(baseClass)) {
        Map<String, InstanceField> parentFields = getInstanceFieldMap(baseClass, clazzes);
        for (Map.Entry<String, InstanceField> entry : parentFields.entrySet()) {
          if (!fieldMap.containsKey(entry.getKey())) {
            fieldMap.put(entry.getKey(), entry.getValue());
          }
        }
      }
    } else {
      // Class not being compiled: fall back to reflection
      try {
        Class<?> reflClass = Class.forName(cleanClassName);
        while (reflClass != null && reflClass != Object.class) {
          for (Field field : reflClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())
                && !fieldMap.containsKey(field.getName())) {
              InstanceField instanceField =
                  new InstanceField(cleanClassName, field.getName(), field.getType().getName());
              instanceField.genericType = GenericTypes.fieldType(field);
              fieldMap.put(field.getName(), instanceField);
            }
          }
          reflClass = reflClass.getSuperclass();
        }
      } catch (ClassNotFoundException e) {
        // class not on classpath; no fields added
      }
    }

    return fieldMap;
  }

  // (bare field names only for the current class's own fields, others need a static import)
  private void addFieldsFromClass(
      Map<String, StaticField> fieldMap, Clazz clazz, boolean bareNames) {
    for (StaticField field : staticFields(clazz)) {
      if (bareNames) {
        fieldMap.put(field.fieldName, field);
      }
      fieldMap.put(clazz.name + "." + field.fieldName, field);
      fieldMap.put(clazz.fqn() + "." + field.fieldName, field);
    }
  }

  // The static fields (incl. enum constants) declared by a class of this compilation run
  private List<StaticField> staticFields(Clazz clazz) {
    List<StaticField> fields = new ArrayList<>();
    for (ParallelDeclaration parallelDecl : clazz.fields) {
      for (Declaration decl : parallelDecl.declarations) {
        if (isStaticField(decl)) {
          boolean isEnum = clazz.isEnum && hasPublicStaticFinalFlags(decl);
          StaticField field =
              new StaticField(clazz.fqn(), decl.name.val.toString(), decl.type, isEnum);
          field.genericType = decl.genericType;
          fields.add(field);
        }
      }
    }
    return fields;
  }

  private void addImportedFields(
      Map<String, StaticField> fieldMap, Clazz currentClazz, List<Clazz> clazzes) {
    for (Clazz clazz : clazzes) {
      if (!currentClazz.equals(clazz)) {
        addFieldsFromClass(fieldMap, clazz, false);
      }
    }
  }

  private void addStaticImportFields(
      Map<String, StaticField> fieldMap, Clazz currentClazz, List<Clazz> clazzes) {
    if (currentClazz.imports == null) {
      return;
    }
    // wildcard imports first, so that explicit single-member imports take precedence
    List<Import> staticImports = new ArrayList<>();
    for (Import imp : currentClazz.imports) {
      if (imp.isStatic && imp.id != null) {
        if (imp.id.endsWith(".*")) {
          staticImports.add(0, imp);
        } else {
          staticImports.add(imp);
        }
      }
    }
    for (Import imp : staticImports) {
      String importPath = imp.id;
      int lastDot = importPath.lastIndexOf('.');
      if (lastDot < 0) {
        continue;
      }
      String memberName = importPath.substring(lastDot + 1);
      Clazz sameRun = findClazz(clazzes, importPath.substring(0, lastDot));
      if (sameRun != null) {
        // a class of this compilation run
        for (StaticField field : staticFields(sameRun)) {
          if ("*".equals(memberName) || field.fieldName.equals(memberName)) {
            fieldMap.put(field.fieldName, field);
          }
        }
        continue;
      }
      if ("*".equals(memberName)) {
        // Wildcard static import - resolve all static fields from that class
        String className = importPath.substring(0, lastDot);
        try {
          Class<?> clazz = Class.forName(className);
          for (Field field : clazz.getFields()) {
            if (Modifier.isPublic(field.getModifiers())
                && Modifier.isStatic(field.getModifiers())) {
              String fieldName = field.getName();
              String fieldType = field.getType().getName();
              StaticField staticField = new StaticField(className, fieldName, fieldType, false);
              staticField.genericType = GenericTypes.fieldType(field);
              fieldMap.put(fieldName, staticField);
              fieldMap.put(clazz.getSimpleName() + "." + fieldName, staticField);
            }
          }
        } catch (ClassNotFoundException e) {
          // not on classpath, skip
        }
        continue;
      }
      String className = importPath.substring(0, lastDot);
      try {
        Class<?> clazz = Class.forName(className);
        Field field = clazz.getField(memberName);
        if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
          String fieldType = field.getType().getName();
          StaticField staticField = new StaticField(className, memberName, fieldType, false);
          staticField.genericType = GenericTypes.fieldType(field);
          fieldMap.put(memberName, staticField);
          fieldMap.put(clazz.getSimpleName() + "." + memberName, staticField);
          fieldMap.put(className + "." + memberName, staticField);
        }
      } catch (ClassNotFoundException | NoSuchFieldException e) {
        // might be a static method import, not a field - skip
      }
    }
  }

  private static Clazz findClazz(List<Clazz> clazzes, String fqn) {
    for (Clazz clazz : clazzes) {
      if (clazz.fqn().equals(fqn)) {
        return clazz;
      }
    }
    return null;
  }

  private boolean isStaticField(Declaration decl) {
    return decl.accFlags.contains(AccessFlag.ACC_STATIC);
  }

  private boolean isInstanceField(Declaration decl) {
    return !decl.accFlags.contains(AccessFlag.ACC_STATIC);
  }

  private boolean hasPublicStaticFinalFlags(Declaration decl) {
    return decl.accFlags.contains(AccessFlag.ACC_PUBLIC)
        && decl.accFlags.contains(AccessFlag.ACC_STATIC)
        && decl.accFlags.contains(AccessFlag.ACC_FINAL);
  }
}
