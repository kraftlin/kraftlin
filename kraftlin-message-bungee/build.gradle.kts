dependencies {
    api(project(":kraftlin-message-core"))
    compileOnly(libs.bungeecord.api)
    implementation(libs.adventure.serializer.bungeecord)
}
