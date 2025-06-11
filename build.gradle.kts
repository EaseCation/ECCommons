plugins {
    id("ecbuild.java-conventions")
    id("ecbuild.copy-conventions")
}

extra.set("copyTo", "{server}/plugins")

dependencies {
    compileOnly(project(":Nukkit"))
    compileOnly(libs.fastutil)
    testImplementation(libs.jupiter.api)
    testImplementation(libs.jupiter.engine)
}

description = "ECCommons"
