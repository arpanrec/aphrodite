import java.io.FileNotFoundException

logging.captureStandardOutput(LogLevel.INFO)

version = "1.4.2"

plugins {
    id("com.diffplug.spotless") version "8.1.0"
}

var allBuildDirs: List<String> = listOf()

allprojects.forEach { project ->
    allBuildDirs +=
        project.layout.buildDirectory
            .get()
            .asFile.absolutePath
    project.version = rootProject.version
}

allprojects {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        if (System.getenv("GITHUB_TOKEN") != null) {
            maven {
                url = uri("https://maven.pkg.github.com/arpanrec/aphrodite")
                credentials(HttpHeaderCredentials::class) {
                    name = "Authorization"
                    value = "Bearer ${System.getenv("GITHUB_TOKEN")}"
                }
                authentication {
                    create("header", HttpHeaderAuthentication::class)
                }
            }
        }
    }
}

val buildDirPath: String =
    layout.buildDirectory
        .get()
        .asFile
        .relativeTo(projectDir)
        .path

spotless {
    var spotlessDir: File = projectDir.resolve("spotless")
    var spotlessExcludes: List<String> = allBuildDirs + spotlessDir.absolutePath
    kotlin {
        targetExclude(spotlessExcludes.toTypedArray())
        target("**/*.kt")
        ktlint()
        licenseHeaderFile(spotlessDir.resolve("copyright.kt.txt"))
    }
    java {
        targetExclude(spotlessExcludes.toTypedArray())
        target("**/*.java")
        palantirJavaFormat().apply {
            formatJavadoc(true)
            removeUnusedImports()
            endWithNewline()
            leadingTabsToSpaces()
            trimTrailingWhitespace()
        }
        licenseHeaderFile(spotlessDir.resolve("copyright.java.txt"))
    }
}

tasks.register("printVersion") {
    // ./gradlew -Dorg.gradle.warning.mode=none -Dorg.gradle.logging.level=quiet printVersion
    outputs.upToDateWhen { false }
    doLast {
        println(version)
    }
}

tasks.register("setVersion") {
    inputs.property("newVersion") {
        findProperty("newVersion")?.toString()
    }
    outputs.file(file("build.gradle.kts"))

    doLast {
        val newVersion =
            findProperty("newVersion")?.toString()
                ?: throw IllegalArgumentException("Please provide the new version using -PnewVersion=<version>")

        val propertiesFile = file("build.gradle.kts")
        if (!propertiesFile.exists()) {
            throw FileNotFoundException("build.gradle.kts not found")
        }

        val lines =
            propertiesFile.readLines().map { line ->
                if (line.startsWith("version = ")) {
                    "version = \"$newVersion\""
                } else {
                    line
                }
            }
        propertiesFile.writeText(lines.joinToString("\n"))
        println("Version updated to $newVersion in build.gradle.kts")
    }
}

tasks.named<Delete>("clean") {
    delete(
        project.fileTree(project.projectDir) {
            include("**/*.log")
        }
    )

    project.rootDir.walk().forEach { file ->
        if (file.name.startsWith(".terraform")
            || file.name == ".DS_Store"
            || file.name.startsWith("foo")
            || file.name.endsWith(".sqlite")
        ) {
            delete(file)
        }
    }
}