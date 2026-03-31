dependencies {
    api(project(":kraftlin-command-core"))
    compileOnly(libs.velocity.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.velocity.api)
}
