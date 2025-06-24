plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation(project(":dsl"))
}
