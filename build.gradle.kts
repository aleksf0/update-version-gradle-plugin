plugins {
    `java-gradle-plugin`
    id("io.freefair.lombok") version "8.0.1"
}

group = "org.nightcrafts"
version = "0.0.1-SNAPSHOT"

//sourceSets {
//    create("integrationTest") {
//        compileClasspath += sourceSets.main.get().output
//        runtimeClasspath += sourceSets.main.get().output
//    }
//}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    implementation(gradleTestKit())
    implementation("org.freemarker:freemarker:2.3.32")
}

gradlePlugin {
    testSourceSets(sourceSets["integrationTest"])
    plugins {
        create("updateVersionPlugin") {
            id = "org.nightcrafts.update-version-gradle-plugin"
            implementationClass = "org.nightcrafts.updateversion.gradle.plugin.UpdateVersionPlugin"
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}