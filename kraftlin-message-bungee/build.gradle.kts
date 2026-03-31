dependencies {
    api(project(":kraftlin-message-core"))
    compileOnly(libs.bungeecord.api)
    api(libs.adventure.serializer.bungeecord)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
}
