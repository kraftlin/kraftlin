plugins {
    `java-platform`
}

dependencies {
    constraints {
        api(project(":kraftlin-command-core"))
        api(project(":kraftlin-command-paper"))
        api(project(":kraftlin-config-core"))
        api(project(":kraftlin-config-paper"))
        api(project(":kraftlin-config-bungee"))
        api(project(":kraftlin-config-velocity"))
        api(project(":kraftlin-message-core"))
        api(project(":kraftlin-message-paper"))
    }
}
