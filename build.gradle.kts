plugins {
    id("ecbuild.java-conventions")
    id("ecbuild.copy-conventions")
}

extra.set("copyTo", listOf("{server}/plugins", "{login}/plugins"))

dependencies {
    compileOnly(project(":nukkit"))
    compileOnly(libs.fastutil)
    testImplementation(libs.jupiter.engine)
}

description = "ECCommons"
