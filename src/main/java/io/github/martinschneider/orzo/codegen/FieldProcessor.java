package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldProcessor {

  public static class StaticField {
    public final String className;
    public final String fieldName;
    public final String fieldType;
    public final boolean isEnum;

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

    public InstanceField(String className, String fieldName, String fieldType) {
      this.className = className;
      this.fieldName = fieldName;
      this.fieldType = fieldType;
    }
  }

  public Map<String, StaticField> getStaticFieldMap(Clazz currentClazz, List<Clazz> clazzes) {
    Map<String, StaticField> fieldMap = new HashMap<>();

    // Add fields from current class
    addFieldsFromClass(fieldMap, currentClazz);

    // Add fields from imported classes
    addImportedFields(fieldMap, currentClazz, clazzes);

    // Add fields from java.lang classes
    addJavaLangFields(fieldMap);

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
            String fieldType = decl.type;

            InstanceField field = new InstanceField(targetClass.fqn(), fieldName, fieldType);
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
              fieldMap.put(
                  field.getName(),
                  new InstanceField(cleanClassName, field.getName(), field.getType().getName()));
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

  private void addFieldsFromClass(Map<String, StaticField> fieldMap, Clazz clazz) {
    // Add enum constants and static fields from declarations
    for (ParallelDeclaration parallelDecl : clazz.fields) {
      for (Declaration decl : parallelDecl.declarations) {
        if (isStaticField(decl)) {
          String fieldName = decl.name.val.toString();
          String fieldType = decl.type;
          boolean isEnum = clazz.isEnum && hasPublicStaticFinalFlags(decl);

          StaticField field = new StaticField(clazz.fqn(), fieldName, fieldType, isEnum);

          // Add with simple field name for direct access
          fieldMap.put(fieldName, field);

          // Add with class.field format
          fieldMap.put(clazz.name + "." + fieldName, field);
          fieldMap.put(clazz.fqn() + "." + fieldName, field);
        }
      }
    }
  }

  private void addImportedFields(
      Map<String, StaticField> fieldMap, Clazz currentClazz, List<Clazz> clazzes) {
    for (Clazz clazz : clazzes) {
      if (!currentClazz.equals(clazz)) {
        addFieldsFromClass(fieldMap, clazz);
      }
    }
  }

  private void addJavaLangFields(Map<String, StaticField> fieldMap) {
    // Add common static fields from java.lang classes
    for (Class<?> clazz :
        List.of(
            Math.class,
            Double.class,
            Float.class,
            Integer.class,
            Long.class,
            Character.class,
            System.class)) {

      for (Field field : clazz.getFields()) {
        if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
          String fieldName = field.getName();
          String fieldType = field.getType().getName();

          StaticField staticField = new StaticField(clazz.getName(), fieldName, fieldType, false);

          // Add with class.field format
          fieldMap.put(clazz.getName() + "." + fieldName, staticField);
          fieldMap.put(clazz.getSimpleName() + "." + fieldName, staticField);

          // For static imports, add direct field name access
          fieldMap.put(fieldName, staticField);
        }
      }
    }
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
