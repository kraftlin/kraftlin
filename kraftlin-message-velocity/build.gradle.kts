dependencies {
    api(project(":kraftlin-message-core"))
    compileOnly(libs.velocity.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}
