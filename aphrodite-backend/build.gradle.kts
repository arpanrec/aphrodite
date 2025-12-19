import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

logging.captureStandardOutput(LogLevel.INFO)

plugins {
    java
    application
    idea
    `maven-publish`

    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"

    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.spring") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.jpa") version "2.3.0"

    id("org.graalvm.buildtools.native") version "0.11.3"

    id("org.hibernate.orm") version "7.2.0.Final"
    id("org.flywaydb.flyway") version "11.19.0"
}

group = "com.arpanrec"

if (project.version == "unspecified" || project.version.toString().isEmpty()) {
    throw GradleException("Project version is not set")
}

openApi {
    val docsDir: File = projectDir.absoluteFile.resolve("docs")
    apiDocsUrl.set("http://127.0.0.1:8083/aphrodite-web-api/api-docs/openapi.json")
    outputDir.set(docsDir)
    outputFileName.set("swagger.json")
    waitTimeInSeconds.set(100)
    customBootRun {
        args.set(listOf("--spring.profiles.active=default,production"))
    }
}

tasks.named("forkedSpringBootRun") {
    dependsOn("compileAotJava")
    dependsOn("compileAotKotlin")
    dependsOn("processAotResources")
    dependsOn("processAot")
}

dependencies {
    implementation("org.apache.commons:commons-lang3")
    implementation("commons-codec:commons-codec")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains:annotations")

    implementation("org.bouncycastle:bcpg-jdk18on:1.83")
    implementation("org.bouncycastle:bcprov-jdk18on:1.83")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.83")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("org.xerial:sqlite-jdbc:3.51.1.0")
    implementation("org.hibernate.orm:hibernate-community-dialects")

    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-flyway") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-security") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-starter-actuator") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    // Log4j2 org.springframework.boot:spring-boot-starter-log4j2 and module replacement not needed because it's
    // excluded from spring-boot-starter but still it's here for clarity
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    modules {
        module("org.springframework.boot:spring-boot-starter-logging") {
            replacedBy("org.springframework.boot:spring-boot-starter-log4j2", "Use Log4j2 instead of Logback")
        }
    }
    // Core Log4j2 libraries
    implementation("org.apache.logging.log4j:log4j-api")
    implementation("org.apache.logging.log4j:log4j-core")

    // SLF4J bridge libraries for routing other logging frameworks to SLF4J
    implementation("org.slf4j:jcl-over-slf4j") // Redirects Apache Commons Logging to SLF4J
    implementation("org.slf4j:jul-to-slf4j") // Redirects java.util.logging to SLF4J
    implementation("org.slf4j:log4j-over-slf4j") // Redirects Log4j 1.x to SLF4J
    implementation("org.slf4j:osgi-over-slf4j:2.1.0-alpha1") // Redirects OSGi LogService to SLF4J

    // Log4j2 SLF4J implementation for routing SLF4J logs to Log4j2
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl") // Finally, routes SLF4J logs to Log4j2
    implementation("org.apache.logging.log4j:log4j-slf4j-impl")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }

    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.0")

    testImplementation("org.springframework.security:spring-security-test") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    testImplementation("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

graalvmNative {
    binaries {
        all {
            javaLauncher.set(
                javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(25))
                    vendor.set(JvmVendorSpec.GRAAL_VM)
                    buildArgs.add("--enable-native-access=ALL-UNNAMED")
                },
            )
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:unchecked")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

sourceSets {
    main {
        java { srcDirs("src/main/java") }
        kotlin { srcDirs("src/main/kotlin") }
    }
    test {
        java { srcDirs("src/test/java") }
        kotlin { srcDirs("src/test/kotlin") }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

configure<IdeaModel> {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

tasks {
    getByName<Jar>("jar") {
        enabled = true
//        archiveAppendix.set("original")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        manifest {
            attributes(
                "Implementation-Title" to getMainClassName(),
                "Implementation-Version" to project.version,
            )
        }
        archiveClassifier.set("")
    }
    getByName<BootJar>("bootJar") {
        enabled = true
        mainClass = getMainClassName()
        archiveAppendix.set("boot")
        manifest {
            attributes(
                "Implementation-Title" to getMainClassName(),
                "Implementation-Version" to project.version,
            )
        }
    }
    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")

        systemProperty("spring.profiles.active", "test")
        reports {
            html.required = true
            junitXml.required = true
        }
    }
}

tasks.named<BootRun>("bootRun") {
    jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    systemProperties["spring.profiles.active"] = "default,production"
    systemProperties.putAll(System.getProperties().map { it.key.toString() to it.value }.toMap())
}

allOpen {
    annotation(jakarta.persistence.Entity::class.qualifiedName!!)
    annotation(jakarta.persistence.MappedSuperclass::class.qualifiedName!!)
    annotation(jakarta.persistence.Embeddable::class.qualifiedName!!)
}

fun getMainClassName(): String = "com.arpanrec.aphrodite.Application"

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

tasks.named<Jar>("javadocJar") {
    dependsOn(tasks.javadoc)
}
tasks.named<Jar>("sourcesJar") {
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/arpanrec/aphrodite")
            credentials(HttpHeaderCredentials::class) {
                name = "Authorization"
                value = "Bearer ${System.getenv("GITHUB_TOKEN")}"
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
        publications {
            register<MavenPublication>("gpr") {
                from(components["java"])
                artifact(tasks.getByName<Jar>("bootJar")) {
                    classifier = "boot"
                }
                artifact(javadocJar) {
                    classifier = "javadoc"
                }
                artifact(sourcesJar) {
                    classifier = "sources"
                }
            }
        }
    }
}
