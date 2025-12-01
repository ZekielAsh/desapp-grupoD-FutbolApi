package com.example.demo.archunit

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices

@AnalyzeClasses(
    packages = ["com.example.demo"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class PackageDependencyTest {

    @ArchTest
    val `controllers only depend on services and models` =
        classes()
            .that().resideInAPackage("..controller..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..service..",
                "..model..",
                "..config..",
                "org.springframework..",
                "io.swagger..",
                "jakarta..",
                "kotlin..",
                "java..",
                "org.jetbrains.."
            )

    @ArchTest
    val `services not depend on controllers` =
        noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..controller..")

    @ArchTest
    val `repositories only accessed by services` =
        classes()
            .that().resideInAPackage("..repository..")
            .should().onlyBeAccessed().byAnyPackage(
                "..service..",
                "..config..",
                "..repository.."
            )

    @ArchTest
    val `no package cycles` =
        slices()
            .matching("com.example.demo.(*)..")
            .should().beFreeOfCycles()

    @ArchTest
    val `model should be independent` =
        classes()
            .that().resideInAPackage("..model..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..model..",
                "java..",
                "kotlin..",
                "com.fasterxml.jackson..",
                "jakarta..",
                "org.springframework.data..",
                "org.jetbrains.."
            )

    @ArchTest
    val `rest controller has RestController annotation` =
        classes()
            .that().resideInAPackage("..controller..")
            .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController::class.java)
}
