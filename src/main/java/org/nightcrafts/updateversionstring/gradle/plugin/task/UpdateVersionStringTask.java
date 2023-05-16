package org.nightcrafts.updateversionstring.gradle.plugin.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.*;
import org.nightcrafts.updateversionstring.gradle.plugin.model.Version;
import org.nightcrafts.updateversionstring.gradle.plugin.util.FreemarkerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class UpdateVersionStringTask extends DefaultTask {

    public static final String DEFAULT_VERSION_LINE_REGEX = "(?m)^version\\s*=\\s*\"(?<version>.*)\"$";

    public static final String DEFAULT_VERSION_PARSE_REGEX
            = "^(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+)(?:-(?<infix>(?!SNAPSHOT).*?))?(?<snapshot>-SNAPSHOT)?$";

    public static final String DEFAULT_VERSION_FORMAT_TEMPLATE
            = "${major}.${minor}.${patch}<#if infix??>-${infix}</#if>${isSnapshot?boolean?then('-SNAPSHOT', '')}";

    public static final String DEFAULT_VERSION_LINE_TEMPLATE = "version = \"${version}\"";

    @InputFile
    public abstract RegularFileProperty getCurrentVersionFile();
    @Input
    public abstract Property<Boolean> getIsSnapshot();
    @Input
    public abstract Property<Version.IncrementMode> getIncrementMode();
    @Input
    public abstract MapProperty<String, String> getAdditionalVersionParts();
    @Input
    @Optional
    public abstract Property<String> getOverrideVersion();
    @Input
    public abstract Property<String> getVersionLineRegex();
    @Input
    public abstract Property<String> getVersionParseRegex();
    @Input
    public abstract Property<String> getVersionFormatTemplate();
    @Input
    public abstract Property<String> getVersionLineTemplate();
    @OutputFile
    public abstract RegularFileProperty getUpdatedVersionFile();

    public UpdateVersionStringTask() {
        setGroup("version");
        setDescription("Parses the version property in a file and updates it");
    }

    @TaskAction
    public void apply() {

        String currentVersion = extractVersion(getCurrentVersionFile());
        getLogger().quiet(String.format("Current version: %s", currentVersion));

        String newVersion = FreemarkerUtil.getInstance().process(getVersionFormatTemplate().get(),
                deriveNextVersion(currentVersion).getAsMap());
        getLogger().quiet(String.format("New version: %s", newVersion));

        updateFile(getCurrentVersionFile(), getUpdatedVersionFile(), newVersion);

        // TODO: Write thorough Unit Tests
        // TODO: Find a way to host in Gradle Plugin Portal
        // TODO: Create descriptive README
        // TODO: Fix integration test assert, possibly rearrange outputing/returning in helper
        // TODO: Configure 2 tasks - 'release' and 'bump'. Do it in this plugin, or up to consumer via convention plugin?
    }

    private String extractVersion(RegularFileProperty versionFile) {

        List<String> filteredMatches;
        Pattern pattern = Pattern.compile(getVersionLineRegex().get());

        try (Stream<String> stream = Files.lines(Paths.get(versionFile.get().toString()))) {
            filteredMatches = stream.filter(line -> pattern.matcher(line).matches()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (filteredMatches.size() != 1) {
            throw new IllegalStateException("Multiple version entries detected within the supplied file: " + versionFile);
        } else {
            Matcher matcher = pattern.matcher(filteredMatches.get(0));
            return matcher.find() ? matcher.group("version") : null;
        }
    }

    private Version deriveNextVersion(String currentVersion) {
        Version nextVersion;
        if (getOverrideVersion().isPresent()) {
            nextVersion = parseVersion(getOverrideVersion().get());
        } else {
            nextVersion = parseVersion(currentVersion);
            nextVersion.setIsSnapshot(getIsSnapshot().get());
            nextVersion.increment(getIncrementMode().get());
        }
        return nextVersion;
    }

    private Version parseVersion(String version) {
        Pattern pattern = Pattern.compile(getVersionParseRegex().get());
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            Map<String, String> versionParts = getNamedGroups(getVersionParseRegex().get()).stream()
                    // Not using the Collectors.toMap, since it fails when either key or value is null
                    // @see <a href="https://bugs.openjdk.org/browse/JDK-8148463">JDK-8148463</a>
                    .collect(HashMap::new, (map, groupName) -> map.put(groupName, matcher.group(groupName)), HashMap::putAll);
            versionParts.putAll(getAdditionalVersionParts().get());
            return new Version(versionParts);
        } else {
            throw new IllegalStateException("Cannot parse version: " + version);
        }
    }

    private Set<String> getNamedGroups(String regex) {
        Set<String> namedGroups = new TreeSet<>();
        Matcher m = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(regex);
        while (m.find()) {
            namedGroups.add(m.group(1));
        }
        return namedGroups;
    }

    private void updateFile(RegularFileProperty currentVersionFile, RegularFileProperty updatedVersionFile, String version) {
        try {
            Path currentVersionFilePath = Paths.get(currentVersionFile.get().toString());
            String currentContents = Files.readString(currentVersionFilePath);
            String versionLine = FreemarkerUtil.getInstance().process(getVersionLineTemplate().get(), Map.of("version",version));
            String updatedContents = currentContents.replaceFirst(getVersionLineRegex().get(), versionLine);
            Path updatedVersionFilePath = Paths.get(updatedVersionFile.get().toString());
            Files.write(updatedVersionFilePath, updatedContents.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
