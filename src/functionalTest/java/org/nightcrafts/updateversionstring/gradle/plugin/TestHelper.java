package org.nightcrafts.updateversionstring.gradle.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class TestHelper {

    private final File testProjectDir;

    public TestHelper(File testProjectDir) {
        this.testProjectDir = testProjectDir;
    }

    public File createNewFileWithContent(String file, String content) throws IOException {
        File newFile = new File(testProjectDir, file);
        File parent = newFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        writeFile(newFile, content);
        return newFile;
    }

    private void writeFile(File destination, String content) throws IOException {
        try (BufferedWriter output = new BufferedWriter(new FileWriter(destination))) {
            output.write(content);
        }
    }

    public void printFileContents(String filePath) {
        try {
            String contents = Files.readString(Paths.get(testProjectDir.toString(), filePath));
            System.out.println(contents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void printAllFilesIn(String filePath) {
        File directoryToPrint = new File(testProjectDir, filePath);
        if (directoryToPrint.exists()) {
            Arrays.stream(directoryToPrint.listFiles()).forEach(file ->
                    System.out.println(" - "+ file.getName())
            );
        }
    }

    public File createDirectory(String directory) {
        File newDirectory = new File(testProjectDir, directory);
        newDirectory.mkdirs();
        return newDirectory;
    }

    public void removeDirectory(String filePath) throws IOException {
        File directoryToRemove = new File(testProjectDir, filePath);
        Files.walk(directoryToRemove.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}