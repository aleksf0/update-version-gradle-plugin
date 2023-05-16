package org.nightcrafts.updateversionstring.gradle.plugin.model;

import lombok.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Version {

    private Integer major;
    private Integer minor;
    private Integer patch;
    private Boolean isSnapshot;
    private Map<String, String> additional;

    public Version(Map<String, String> versionParts) {
        this.additional = new HashMap<>();
        versionParts.forEach((key, value) -> {
            if (key.equals(IncrementMode.MAJOR.label)) {
                this.major = Integer.parseInt(value);
            } else if (key.equals(IncrementMode.MINOR.label)) {
                this.minor = Integer.parseInt(value);
            } else if (key.equals(IncrementMode.PATCH.label)) {
                this.patch = Integer.parseInt(value);
            } else if (key.equals("snapshot")) {
                this.isSnapshot = "-SNAPSHOT".equals(value);
            } else {
                additional.put(key, value);
            }
        });
    }

    public Map<String, String> getAsMap() {
        Map<String, String> map = new HashMap<>(additional);
        map.put(IncrementMode.MAJOR.label, getMajor().toString());
        map.put(IncrementMode.MINOR.label, getMinor().toString());
        map.put(IncrementMode.PATCH.label, getPatch().toString());
        map.put("isSnapshot", getIsSnapshot().toString());
        return map;
    }

    public void increment(Version.IncrementMode incrementMode) {
        switch (incrementMode) {
            case MAJOR -> {
                this.major += 1;
                this.minor = 0;
                this.patch = 0;
            }
            case MINOR -> {
                this.minor += 1;
                this.patch = 0;
            }
            case PATCH -> this.patch += 1;
        }
    }

    public enum IncrementMode {
        MAJOR("major"),
        MINOR("minor"),
        PATCH("patch"),
        UNDEFINED("undefined");

        private final String label;
        private IncrementMode(String label) {
            this.label = label;
        }

        public static IncrementMode fromValue(String input) {
            return Arrays.stream(values())
                    .filter(v -> v.name().equalsIgnoreCase(input))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown increment mode supplied: " + input));
        }
    }
}
