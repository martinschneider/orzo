package io.github.martinschneider.orzo;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.math.BigDecimal;
import java.math.BigInteger;

@AnalyzeClasses(
    packages = "io.github.martinschneider.orzo",
    importOptions = {ImportOption.DoNotIncludeTests.class})
public class ArchitectureTest {

  @ArchTest
  static final ArchRule restrict_imports =
      classes()
          .that()
          .resideInAPackage("io.github.martinschneider.orzo..")
          .should()
          .onlyDependOnClassesThat(
              // TODO: java.io should still be restricted
              resideInAnyPackage(
                      "io.github.martinschneider.orzo..", "java.util..", "java.lang..", "java.io..")
                  .or(belongToAnyOf(BigDecimal.class, BigInteger.class)));
}
