plugins {
    id("com.gradleup.shadow") version "9.0.0-beta14"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

dependencies {
    paperweight.foliaDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}