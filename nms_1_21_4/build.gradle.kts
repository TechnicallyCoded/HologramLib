plugins {
    id("com.gradleup.shadow") version "9.0.0-beta14"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
}

dependencies {
    paperweight.foliaDevBundle("1.21.4-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}