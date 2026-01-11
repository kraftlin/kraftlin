import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.axion.release)
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

scmVersion {
    tag {
        prefix.set("v")
    }
    versionIncrementer("incrementMinor")
}

group = "io.github.kraftlin"
version = scmVersion.version

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    extensions.configure<KotlinJvmProjectExtension> {
        explicitApi()
        jvmToolchain(21)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    extensions.configure<DokkaExtension> {
        dokkaSourceSets.configureEach {
            val docFile = file("docs/${project.name}.md")
            if (docFile.isFile) {
                includes.from(docFile)
            }
        }
    }

    extensions.configure<MavenPublishBaseExtension> {

        publishToMavenCentral()
        signAllPublications()

        coordinates(project.group.toString(), project.name.toString(), project.version.toString())

        pom {
            name.set(project.name)
            description.set(
                when (project.name) {
                    "kraftlin-command-core" -> "Kotlin DSL wrapper for Mojang Brigadier."
                    "kraftlin-command-paper" -> "Paper integration for Kraftlin commands."
                    else -> "Kraftlin module: ${project.name}"
                }
            )
            inceptionYear.set("2026")
            url.set("https://github.com/kraftlin/kraftlin")

            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("minoneer")
                    name.set("minoneer")
                    email.set("minoneer@gmail.com")
                    url.set("https://github.com/minoneer")
                }
            }

            scm {
                url.set("https://github.com/kraftlin/kraftlin")
                connection.set("scm:git:https://github.com/kraftlin/kraftlin.git")
                developerConnection.set("scm:git:ssh://git@github.com:kraftlin/kraftlin.git")
            }

            issueManagement {
                system.set("GitHub Issues")
                url.set("https://github.com/kraftlin/kraftlin/issues")
            }

            ciManagement {
                system.set("GitHub Actions")
                url.set("https://github.com/kraftlin/kraftlin/actions")
            }

            organization {
                name.set("kraftlin")
                url.set("https://github.com/kraftlin")
            }
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

val semverStrict = Regex("^\\d+\\.\\d+\\.\\d+$")

tasks.matching { it.name == "publishAndReleaseToMavenCentral" }.configureEach {
    doFirst {
        val v = project.version.toString()
        require(semverStrict.matches(v)) {
            "Refusing to publish a release with a non-semver version: version=$v (expected X.Y.Z)"
        }
    }
}
