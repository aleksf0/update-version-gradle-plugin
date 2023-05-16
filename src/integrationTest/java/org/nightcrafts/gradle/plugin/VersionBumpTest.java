package org.nightcrafts.gradle.plugin;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nightcrafts.gradle.plugin.task.VersionBumpTask;

import java.io.File;
import java.io.IOException;

import static org.gradle.testkit.runner.TaskOutcome.FAILED;

public class VersionBumpTest {

    private static final String VERSION_FILE_PATH = "src/main/kotlin/org.nightcrafts.java-conventions.gradle.kts";

    @TempDir
    File testProjectDir;

    TestHelper testHelper;

    @BeforeEach
    public void setup() {
        testHelper = new TestHelper(testProjectDir);
    }

    @Test
    public void shouldUpdateVersionWhenConfigIsValid() throws IOException {

        createSettingsFile();
        createBuildFile();
        createVersionFile();

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(VersionBumpPlugin.NAME, "--stacktrace", "-PadditionalVersionParts=buildId=3we4fdF,pipelineId=98kSo2", "-PoverrideVersionX=9.9.9-coocoo-SNAPSHOT", "-PincrementMode=minor", "-PisSnapshot=false", "-PcurrentVersionFile=" + VERSION_FILE_PATH)
                .withPluginClasspath()
                .build();

        System.out.println();
        System.out.println(">> Plugin output:");
        System.out.println(result.getOutput());

        printTestProjectDirectoryFiles();

//        Assertions.assertEquals(FAILED, result.task(":sortFiles").getOutcome());
//        Assertions.assertTrue(
//                result.getOutput().contains("Invalid property sortType value provided [wrong]. Valid values are ['extension','date','alphabet']"));
    }

    private void printTestProjectDirectoryFiles() {
        System.out.println(">> Test project directory files");
        System.out.println();
        System.out.println("Absolute path:");
        System.out.println(testProjectDir.getAbsolutePath());
        System.out.println("");

        System.out.println("Root:");
        testHelper.printAllFilesIn("");
        System.out.println("");

        System.out.println(String.format("Version file contents (%s):", VERSION_FILE_PATH));
        testHelper.printFileContents(VERSION_FILE_PATH);
    }

    private void createSettingsFile() throws IOException {
        testHelper.createNewFileWithContent("settings.gradle.kts", """
                rootProject.name = "versionBumpPlugin"
                """);
    }

    private void createBuildFile() throws IOException {
        testHelper.createNewFileWithContent("build.gradle.kts", """
                plugins {
                    id("org.nightcrafts.version-bump-plugin")
                }
                
                versionBump {
                    currentVersionFile.set(File("%s"))
                    isSnapshot.set(true)
                    incrementMode.set("patch")
                    additionalVersionParts.set(mapOf("pipelineId" to "08kSo2", "javaVersion" to "17"))
                    advanced {
                      versionLineRegex.set("(?m)^version\\\\s*=\\\\s*\\"(?<version>.*)\\"$")
                      versionParseRegex.set("^(?<major>\\\\d+)\\\\.(?<minor>\\\\d+)\\\\.(?<patch>\\\\d+)(?:-(?<infix>(?!SNAPSHOT).*?))?(?<snapshot>-SNAPSHOT)?$")
                      versionFormatTemplate.set("\\${major}.\\${minor}.\\${patch}<#if infix??>-\\${infix}</#if>\\${isSnapshot?boolean?then('-SNAPSHOT', '')}")
                      versionLineTemplate.set("version = \\"\\${version}\\"")
                    }
                    updatedVersionFile.set(File("%s"))
                }
                """.formatted(VERSION_FILE_PATH, VERSION_FILE_PATH));
    }

    private void createVersionFile() throws IOException {
        testHelper.createNewFileWithContent(VERSION_FILE_PATH, """
                plugins {
                    `java-library`
                }
                
                group = "com.example"
                version = "1.2.3-7.0.19-SNAPSHOT"
                
                repositories {
                    mavenLocal()
                    maven {
                        url = uri("https://repo.maven.apache.org/maven2/")
                    }
                }
                """);
    }
}