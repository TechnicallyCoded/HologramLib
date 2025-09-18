plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0-beta14"
}

group = "com.tcoded"
version = "1.3.4"

allprojects {
    repositories {
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        mavenCentral()

        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }
        maven { url = uri("https://maven.evokegames.gg/snapshots") }
        maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }

        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://repo.tcoded.com/releases/") }
        maven { url = uri("https://repo.extendedclip.com/releases/") }
    }
}

subprojects {
    apply(plugin = "java")

    project.group = "com.tcoded"
    project.version = rootProject.version

    dependencies {
        compileOnly("com.tcoded:FoliaLib:0.5.1")
        compileOnly("net.kyori:adventure-text-minimessage:4.17.0")

        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.test {
        useJUnitPlatform()
    }
}

dependencies {
    implementation("com.tcoded:FoliaLib:0.5.1")
    implementation(project(":common"))
    implementation(project(":nms_1_20_4", "reobf"))
    implementation(project(":nms_1_21_4", "shadow"))


    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
}

val targetJavaVersion = 21

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release = targetJavaVersion
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveFileName.set("HologramLib-${version}.jar")

    relocate("com.tcoded.folialib", "com.tcoded.hologramlib.lib.folialib")

    exclude("org/jetbrains/**")
    exclude("org/intellij/**")
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJavaLocal") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()

            artifact(tasks.shadowJar.get().archiveFile.get().asFile)
        }
    }
}

tasks.named("publishMavenJavaLocalPublicationToMavenLocal") {
    dependsOn(tasks.shadowJar, tasks.jar)
}

val enablePublishing: Boolean = project.findProperty("enableUploadPublish")?.toString()?.toBoolean() == true

if (enablePublishing) {
    publishing {
        repositories {
            maven {
                name = "reposilite"
                url = uri("https://repo.tcoded.com/releases")

                credentials {
                    username = project.findProperty("REPOSILITE_USER")?.toString()
                        ?: System.getenv("REPOSILITE_USER")
                                ?: error("REPOSILITE_USER property or environment variable is not set")
                    password = project.findProperty("REPOSILITE_PASS")?.toString()
                        ?: System.getenv("REPOSILITE_PASS")
                                ?: error("REPOSILITE_PASS property or environment variable is not set")
                }

                authentication {
                    register<BasicAuthentication>("basic")
                }
            }
        }
    }

    tasks.named("publishMavenJavaLocalPublicationToReposiliteRepository") {
        dependsOn(tasks.jar)
        dependsOn(tasks.shadowJar)
    }

}