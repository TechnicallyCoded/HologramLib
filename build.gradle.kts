plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0-beta11"
}

group = "com.tcoded.hologramlib"
version = "1.2.0"

allprojects {
    repositories {
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
        mavenCentral()
        //    maven {
        //        name = "spigotmc-repo"
        //        url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
        //    }

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
    }
}

subprojects {
    apply(plugin = "java")

    project.group = "com.tcoded.hologramlib"
    project.version = rootProject.version

    dependencies {
        compileOnly("com.github.technicallycoded:FoliaLib:0.4.4")
        compileOnly("net.kyori:adventure-text-minimessage:4.17.0")

        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.test {
        useJUnitPlatform()
    }
}

dependencies {
    implementation("com.github.technicallycoded:FoliaLib:0.4.4")
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

tasks.build {
    dependsOn(tasks.publishToMavenLocal)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()

            artifact(tasks.shadowJar.get().archiveFile.get().asFile)
        }
    }
}

tasks.named("publishMavenPublicationToMavenLocal") {
    dependsOn(tasks.shadowJar, tasks.jar)
}