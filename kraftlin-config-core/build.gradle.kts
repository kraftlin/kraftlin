dependencies {
    api(libs.snakeyaml.engine)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
}
