package io.github.configdoctor;

enum OutputFormat {
    TEXT,
    JSON,
    SARIF;

    static OutputFormat from(String value) {
        for (OutputFormat format : values()) {
            if (format.name().equalsIgnoreCase(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unsupported output format: " + value + ". Expected text, json, or sarif.");
    }
}
