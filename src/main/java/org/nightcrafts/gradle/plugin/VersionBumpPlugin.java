package org.nightcrafts.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.impldep.org.apache.commons.lang.BooleanUtils;
import org.nightcrafts.gradle.plugin.extension.VersionBumpExtension;
import org.nightcrafts.gradle.plugin.model.Version;
import org.nightcrafts.gradle.plugin.task.VersionBumpTask;

import java.util.HashMap;
import java.util.Map;

public class VersionBumpPlugin implements Plugin<Project> {

    public static String NAME = "bump";

    @Override
    public void apply(Project project) {

        VersionBumpExtension versionBumpExtension = project.getExtensions()
                .create(VersionBumpExtension.NAME, VersionBumpExtension.class);

        project.getTasks().register(NAME, VersionBumpTask.class, task -> {
            task.getCurrentVersionFile().set(project.file(resolveCurrentVersionFile(project, versionBumpExtension)));
            task.getIsSnapshot().set(resolveIsSnapshot(project, versionBumpExtension));
            task.getIncrementMode().set(resolveIncrementMode(project, versionBumpExtension));
            task.getAdditionalVersionParts().set(resolveAdditionalVersionParts(project, versionBumpExtension));
            task.getOverrideVersion().set(resolveOverrideVersion(project, versionBumpExtension));
            task.getVersionLineRegex().set(versionBumpExtension.getAdvanced().getVersionLineRegex()
                    .getOrElse(VersionBumpTask.DEFAULT_VERSION_LINE_REGEX));
            task.getVersionParseRegex().set(versionBumpExtension.getAdvanced().getVersionParseRegex()
                    .getOrElse(VersionBumpTask.DEFAULT_VERSION_PARSE_REGEX));
            task.getVersionFormatTemplate().set(versionBumpExtension.getAdvanced().getVersionFormatTemplate()
                    .getOrElse(VersionBumpTask.DEFAULT_VERSION_FORMAT_TEMPLATE));
            task.getVersionLineTemplate().set(versionBumpExtension.getAdvanced().getVersionLineTemplate()
                    .getOrElse(VersionBumpTask.DEFAULT_VERSION_LINE_TEMPLATE));
            task.getUpdatedVersionFile().set(project.file(resolveUpdatedVersionFile(project, versionBumpExtension)));
        });
    }

    private String resolveCurrentVersionFile(Project project, VersionBumpExtension versionBumpExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("currentVersionFile")) {
            return properties.get("currentVersionFile").toString();
        }
        return versionBumpExtension.getCurrentVersionFile().get().toString();
    }

    private Boolean resolveIsSnapshot(Project project, VersionBumpExtension versionBumpExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("isSnapshot")) {
            return BooleanUtils.toBoolean(properties.get("isSnapshot").toString());
        }
        return versionBumpExtension.getIsSnapshot().getOrElse(Boolean.FALSE);
    }

    private Version.IncrementMode resolveIncrementMode(Project project, VersionBumpExtension versionBumpExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("incrementMode")) {
            return Version.IncrementMode.fromValue(properties.get("incrementMode").toString());
        }
        return versionBumpExtension.getIncrementMode().isPresent()
                ? Version.IncrementMode.fromValue(versionBumpExtension.getIncrementMode().get())
                : Version.IncrementMode.UNDEFINED;
    }

    private Map<String, String> resolveAdditionalVersionParts(Project project, VersionBumpExtension versionBumpExtension) {
        Map<String, ?> properties = project.getProperties();
        Map<String, String> map = new HashMap<>();
        if (properties.containsKey("additionalVersionParts")) {
            map.putAll(convertToMap(properties.get("additionalVersionParts").toString()));
        }
        if (versionBumpExtension.getAdditionalVersionParts().isPresent()) {
            map.putAll(versionBumpExtension.getAdditionalVersionParts().get());
        }
        return map;
    }

    private Map<String, String> convertToMap(String commaSeparatedKeyValuePairs) {
        Map<String, String> map = new HashMap<>();
        for (String pair : commaSeparatedKeyValuePairs.split(",")) {
            String[] keyValue = pair.split("=");
            map.put(keyValue[0], keyValue[1]);
        }
        return map;
    }

    private String resolveOverrideVersion(Project project, VersionBumpExtension versionBumpExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("overrideVersion")) {
            return properties.get("overrideVersion").toString();
        }
        return versionBumpExtension.getOverrideVersion().getOrNull();
    }

    private String resolveUpdatedVersionFile(Project project, VersionBumpExtension versionBumpExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("updatedVersionFile")) {
            return properties.get("updatedVersionFile").toString();
        }
        return versionBumpExtension.getUpdatedVersionFile().isPresent()
                ? versionBumpExtension.getUpdatedVersionFile().get().toString()
                : resolveCurrentVersionFile(project, versionBumpExtension);
    }
}
