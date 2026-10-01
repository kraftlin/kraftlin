dependencies {
    // Provided by every target platform (Paper: com.mojang:brigadier, Velocity: its velocity-brigadier fork),
    // so it must not leak into consumers as a transitive dependency.
    compileOnly(libs.brigadier)

    testImplementation(kotlin("test"))
    testImplementation(libs.brigadier)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
}
