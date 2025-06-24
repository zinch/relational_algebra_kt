plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val kotestVersion = "6.0.0.M4"
dependencies {
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
