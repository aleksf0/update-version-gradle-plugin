package org.nightcrafts.updateversion.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.impldep.org.apache.commons.lang.BooleanUtils;
import org.nightcrafts.updateversion.gradle.plugin.extension.UpdateVersionExtension;
import org.nightcrafts.updateversion.gradle.plugin.model.Version;
import org.nightcrafts.updateversion.gradle.plugin.task.UpdateVersionTask;

import java.util.HashMap;
import java.util.Map;

public class UpdateVersionPlugin implements Plugin<Project> {

    public static String NAME = "updateVersion";

    @Override
    public void apply(Project project) {

        UpdateVersionExtension updateVersionExtension = project.getExtensions()
                .create(UpdateVersionExtension.NAME, UpdateVersionExtension.class);

        project.getTasks().register(NAME, UpdateVersionTask.class, task -> {
            task.getCurrentVersionFile().set(project.file(resolveCurrentVersionFile(project, updateVersionExtension)));
            task.getIsSnapshot().set(resolveIsSnapshot(project, updateVersionExtension));
            task.getIncrementMode().set(resolveIncrementMode(project, updateVersionExtension));
            task.getAdditionalVersionParts().set(resolveAdditionalVersionParts(project, updateVersionExtension));
            task.getOverrideVersion().set(resolveOverrideVersion(project, updateVersionExtension));
            task.getVersionLineRegex().set(updateVersionExtension.getAdvanced().getVersionLineRegex()
                    .getOrElse(UpdateVersionTask.DEFAULT_VERSION_LINE_REGEX));
            task.getVersionParseRegex().set(updateVersionExtension.getAdvanced().getVersionParseRegex()
                    .getOrElse(UpdateVersionTask.DEFAULT_VERSION_PARSE_REGEX));
            task.getVersionFormatTemplate().set(updateVersionExtension.getAdvanced().getVersionFormatTemplate()
                    .getOrElse(UpdateVersionTask.DEFAULT_VERSION_FORMAT_TEMPLATE));
            task.getVersionLineTemplate().set(updateVersionExtension.getAdvanced().getVersionLineTemplate()
                    .getOrElse(UpdateVersionTask.DEFAULT_VERSION_LINE_TEMPLATE));
            task.getUpdatedVersionFile().set(project.file(resolveUpdatedVersionFile(project, updateVersionExtension)));
        });
    }

    private String resolveCurrentVersionFile(Project project, UpdateVersionExtension updateVersionExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("currentVersionFile")) {
            return properties.get("currentVersionFile").toString();
        }
        return updateVersionExtension.getCurrentVersionFile().get().toString();
    }

    private Boolean resolveIsSnapshot(Project project, UpdateVersionExtension updateVersionExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("isSnapshot")) {
            return BooleanUtils.toBoolean(properties.get("isSnapshot").toString());
        }
        return updateVersionExtension.getIsSnapshot().getOrElse(Boolean.FALSE);
    }

    private Version.IncrementMode resolveIncrementMode(Project project, UpdateVersionExtension updateVersionExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("incrementMode")) {
            return Version.IncrementMode.fromValue(properties.get("incrementMode").toString());
        }
        return updateVersionExtension.getIncrementMode().isPresent()
                ? Version.IncrementMode.fromValue(updateVersionExtension.getIncrementMode().get())
                : Version.IncrementMode.UNDEFINED;
    }

    private Map<String, String> resolveAdditionalVersionParts(Project project, UpdateVersionExtension updateVersionExtension) {
        Map<String, ?> properties = project.getProperties();
        Map<String, String> map = new HashMap<>();
        if (properties.containsKey("additionalVersionParts")) {
            map.putAll(convertToMap(properties.get("additionalVersionParts").toString()));
        }
        if (updateVersionExtension.getAdditionalVersionParts().isPresent()) {
            map.putAll(updateVersionExtension.getAdditionalVersionParts().get());
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

    private String resolveOverrideVersion(Project project, UpdateVersionExtension updateVersionExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("overrideVersion")) {
            return properties.get("overrideVersion").toString();
        }
        return updateVersionExtension.getOverrideVersion().getOrNull();
    }

    private String resolveUpdatedVersionFile(Project project, UpdateVersionExtension updateVersionExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("updatedVersionFile")) {
            return properties.get("updatedVersionFile").toString();
        }
        return updateVersionExtension.getUpdatedVersionFile().isPresent()
                ? updateVersionExtension.getUpdatedVersionFile().get().toString()
                : resolveCurrentVersionFile(project, updateVersionExtension);
    }
}
