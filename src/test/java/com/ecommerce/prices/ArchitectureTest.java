package com.ecommerce.prices;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.ecommerce.prices", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainShouldNotDependOnAdapters = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..webapp..");

    @ArchTest
    static final ArchRule domainModelPortsAndExceptionsShouldNotDependOnFrameworks = noClasses()
            .that().resideInAnyPackage("..domain.model..", "..domain.port..", "..domain.exception..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..");

    @ArchTest
    static final ArchRule databaseAdapterShouldNotDependOnWebapp = noClasses()
            .that().resideInAPackage("..adapter..")
            .should().dependOnClassesThat().resideInAPackage("..webapp..");

    @ArchTest
    static final ArchRule webappShouldNotDependOnDatabaseAdapter = noClasses()
            .that().resideInAPackage("..webapp..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..");
}
