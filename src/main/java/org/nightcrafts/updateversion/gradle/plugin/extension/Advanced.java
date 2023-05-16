package org.nightcrafts.updateversion.gradle.plugin.extension;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Optional;

public interface Advanced {

    @Optional
    Property<String> getVersionLineRegex();
    @Optional
    Property<String> getVersionParseRegex();
    @Optional
    Property<String> getVersionFormatTemplate();
    @Optional
    Property<String> getVersionLineTemplate();
}
