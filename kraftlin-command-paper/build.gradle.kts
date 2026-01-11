dependencies {
    api(project(":kraftlin-command-core"))

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.2")
    testImplementation("io.mockk:mockk:1.14.7")
    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}
