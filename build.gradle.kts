plugins {
    `java-gradle-plugin`
    id("io.freefair.lombok") version "8.0.1"
}

group = "org.nightcrafts"
version = "0.0.1-SNAPSHOT"

testing {
    suites {

        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        val functionalTest by registering(JvmTestSuite::class) {
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

tasks.named("check") {
    dependsOn(testing.suites.named("functionalTest"))
}

dependencies {
    implementation(gradleTestKit())
    implementation("org.freemarker:freemarker:2.3.32")
}

gradlePlugin {
    testSourceSets(sourceSets["functionalTest"])
    plugins {
        create("updateVersionStringPlugin") {
            id = "org.nightcrafts.update-version-string-gradle-plugin"
            implementationClass = "org.nightcrafts.updateversionstring.gradle.plugin.UpdateVersionStringPlugin"
        }
    }
}