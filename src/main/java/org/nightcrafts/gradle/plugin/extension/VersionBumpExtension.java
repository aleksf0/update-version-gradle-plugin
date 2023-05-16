package org.nightcrafts.gradle.plugin.extension;

import org.gradle.api.Action;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;

public interface VersionBumpExtension {

    String NAME = "versionBump";

    RegularFileProperty getCurrentVersionFile();
    @Optional
    Property<Boolean> getIsSnapshot();
    @Optional
    Property<String> getIncrementMode();
    @Optional
    MapProperty<String, String> getAdditionalVersionParts();
    @Optional
    Property<String> getOverrideVersion();
    @Optional
    @Nested
    Advanced getAdvanced();
    @Optional
    RegularFileProperty getUpdatedVersionFile();

    default void advanced(Action<? super Advanced> action) {
        action.execute(getAdvanced());
    }
}
