plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openapiGenerator)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.gson)
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set(rootProject.file("../backend/openapi-schema/openapi.yaml").absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
    modelPackage.set("com.github.ai.split.api")

    globalProperties.set(
        mapOf(
            "models" to "",
            "modelDocs" to "false",
            "modelTests" to "false",
            "apis" to "false",
            "supportingFiles" to "false"
        )
    )

    configOptions.set(
        mapOf(
            "serializationLibrary" to "gson",
            "dateLibrary" to "java8",
            "modelMutable" to "false"
        )
    )
}

sourceSets {
    main {
        kotlin.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
    }
}

tasks.named("compileKotlin") {
    dependsOn(tasks.named("openApiGenerate"))
}
