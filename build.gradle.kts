import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.10"
    id("org.jetbrains.intellij.platform") version "2.9.0"
}

group = "com.grit.ideaplugins"
version = "2.0.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", "2023.2.8")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("org.intellij.plugins.markdown")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "232"
            untilBuild = "253.*"
        }

        changeNotes = """
      Supports cross-module links, and links to methods as well. 
      Added Folding Support for links in markdown files. 
    """.trimIndent()
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    signPlugin {
        certificateChainFile = file(providers.environmentVariable("JETBRAINS_MARKETPLACE_CERTIFICATE_CHAIN"))
        privateKeyFile = file(providers.environmentVariable("JETBRAINS_MARKETPLACE_PRIVATE_KEY"))
        password.set(providers.environmentVariable("JETBRAINS_MARKETPLACE_PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(providers.environmentVariable("JETBRAINS_MARKETPLACE_PUBLISH_TOKEN"))
    }
}
