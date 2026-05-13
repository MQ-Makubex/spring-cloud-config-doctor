package io.github.configdoctor;

import java.nio.file.Path;
import java.util.List;

record ScanReport(Path root, List<ConfigFile> configFiles, List<Finding> findings) {

    boolean hasErrors() {
        return findings.stream().anyMatch(finding -> finding.severity() == Severity.ERROR);
    }

    boolean hasWarnings() {
        return findings.stream().anyMatch(finding -> finding.severity() == Severity.WARNING);
    }
}
