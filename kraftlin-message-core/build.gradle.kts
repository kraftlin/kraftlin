dependencies {
    api(libs.adventure.api)
    api(libs.adventure.gson)
    api(libs.adventure.legacy)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.jsonassert)
    testImplementation(libs.mockk)
}
