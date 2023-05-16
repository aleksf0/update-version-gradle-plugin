package org.nightcrafts.updateversionstring.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.impldep.org.apache.commons.lang.BooleanUtils;
import org.nightcrafts.updateversionstring.gradle.plugin.extension.UpdateVersionStringExtension;
import org.nightcrafts.updateversionstring.gradle.plugin.model.Version;
import org.nightcrafts.updateversionstring.gradle.plugin.task.UpdateVersionStringTask;

import java.util.HashMap;
import java.util.Map;

public class UpdateVersionStringPlugin implements Plugin<Project> {

    public static String NAME = "updateVersionString";

    @Override
    public void apply(Project project) {

        UpdateVersionStringExtension updateVersionStringExtension = project.getExtensions()
                .create(UpdateVersionStringExtension.NAME, UpdateVersionStringExtension.class);

        project.getTasks().register(NAME, UpdateVersionStringTask.class, task -> {
            task.getCurrentVersionFile().set(project.file(resolveCurrentVersionFile(project, updateVersionStringExtension)));
            task.getIsSnapshot().set(resolveIsSnapshot(project, updateVersionStringExtension));
            task.getIncrementMode().set(resolveIncrementMode(project, updateVersionStringExtension));
            task.getAdditionalVersionParts().set(resolveAdditionalVersionParts(project, updateVersionStringExtension));
            task.getOverrideVersion().set(resolveOverrideVersion(project, updateVersionStringExtension));
            task.getVersionLineRegex().set(updateVersionStringExtension.getAdvanced().getVersionLineRegex()
                    .getOrElse(UpdateVersionStringTask.DEFAULT_VERSION_LINE_REGEX));
            task.getVersionParseRegex().set(updateVersionStringExtension.getAdvanced().getVersionParseRegex()
                    .getOrElse(UpdateVersionStringTask.DEFAULT_VERSION_PARSE_REGEX));
            task.getVersionFormatTemplate().set(updateVersionStringExtension.getAdvanced().getVersionFormatTemplate()
                    .getOrElse(UpdateVersionStringTask.DEFAULT_VERSION_FORMAT_TEMPLATE));
            task.getVersionLineTemplate().set(updateVersionStringExtension.getAdvanced().getVersionLineTemplate()
                    .getOrElse(UpdateVersionStringTask.DEFAULT_VERSION_LINE_TEMPLATE));
            task.getUpdatedVersionFile().set(project.file(resolveUpdatedVersionFile(project, updateVersionStringExtension)));
        });
    }

    private String resolveCurrentVersionFile(Project project, UpdateVersionStringExtension updateVersionStringExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("currentVersionFile")) {
            return properties.get("currentVersionFile").toString();
        }
        return updateVersionStringExtension.getCurrentVersionFile().get().toString();
    }

    private Boolean resolveIsSnapshot(Project project, UpdateVersionStringExtension updateVersionStringExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("isSnapshot")) {
            return BooleanUtils.toBoolean(properties.get("isSnapshot").toString());
        }
        return updateVersionStringExtension.getIsSnapshot().getOrElse(Boolean.FALSE);
    }

    private Version.IncrementMode resolveIncrementMode(Project project, UpdateVersionStringExtension updateVersionStringExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("incrementMode")) {
            return Version.IncrementMode.fromValue(properties.get("incrementMode").toString());
        }
        return updateVersionStringExtension.getIncrementMode().isPresent()
                ? Version.IncrementMode.fromValue(updateVersionStringExtension.getIncrementMode().get())
                : Version.IncrementMode.UNDEFINED;
    }

    private Map<String, String> resolveAdditionalVersionParts(Project project, UpdateVersionStringExtension updateVersionStringExtension) {
        Map<String, ?> properties = project.getProperties();
        Map<String, String> map = new HashMap<>();
        if (properties.containsKey("additionalVersionParts")) {
            map.putAll(convertToMap(properties.get("additionalVersionParts").toString()));
        }
        if (updateVersionStringExtension.getAdditionalVersionParts().isPresent()) {
            map.putAll(updateVersionStringExtension.getAdditionalVersionParts().get());
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

    private String resolveOverrideVersion(Project project, UpdateVersionStringExtension updateVersionStringExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("overrideVersion")) {
            return properties.get("overrideVersion").toString();
        }
        return updateVersionStringExtension.getOverrideVersion().getOrNull();
    }

    private String resolveUpdatedVersionFile(Project project, UpdateVersionStringExtension updateVersionStringExtension) {
        Map<String, ?> properties = project.getProperties();
        if (properties.containsKey("updatedVersionFile")) {
            return properties.get("updatedVersionFile").toString();
        }
        return updateVersionStringExtension.getUpdatedVersionFile().isPresent()
                ? updateVersionStringExtension.getUpdatedVersionFile().get().toString()
                : resolveCurrentVersionFile(project, updateVersionStringExtension);
    }
}
