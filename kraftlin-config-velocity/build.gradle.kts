dependencies {
    api(project(":kraftlin-config-core"))
    compileOnly(libs.slf4j.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}
