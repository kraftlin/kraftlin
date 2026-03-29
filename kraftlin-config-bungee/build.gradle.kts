dependencies {
    api(project(":kraftlin-config-core"))
    compileOnly(libs.bungeecord.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}
