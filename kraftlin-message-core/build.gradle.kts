dependencies {
    api(libs.adventure.api)
    implementation(libs.adventure.legacy)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.jsonassert)
    testImplementation(libs.adventure.gson)
    testImplementation(libs.mockk)
}
