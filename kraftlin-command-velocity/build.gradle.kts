dependencies {
    api(project(":kraftlin-command-core"))
    compileOnly(libs.velocity.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}
