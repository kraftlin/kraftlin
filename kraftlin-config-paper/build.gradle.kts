dependencies {
    api(project(":kraftlin-config-core"))
    compileOnly(libs.paper.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.paper.api)
}
