dependencies {
    api("com.mojang:brigadier:1.3.10")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.2")
}

tasks.test {
    useJUnitPlatform()
}
