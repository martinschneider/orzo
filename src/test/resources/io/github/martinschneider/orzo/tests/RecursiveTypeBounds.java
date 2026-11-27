package io.github.martinschneider.orzo.tests;

import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.constant.ClassDesc;

public class RecursiveTypeBounds {
  // generic methods whose type variables are bounded by themselves, e.g. "<T extends Attribute<T>>
  // Optional<T> findAttribute(AttributeMapper<T>)" (resolving their types used to loop forever)
  public static void main(String[] args) {
    byte[] bytes = ClassFile.of().build(ClassDesc.of("Empty"), b -> {});
    ClassModel model = ClassFile.of().parse(bytes);
    System.out.println(model.findAttribute(Attributes.sourceFile()).isPresent());
    System.out.println(model.thisClass().asInternalName());
  }
}
