package io.github.configdoctor;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

record ScanReport(Path root, List<ConfigFile> configFiles, List<Finding> findings) {

    ScanReport withoutFindingsWithCodes(Set<String> ignoredCodes) {
        if (ignoredCodes.isEmpty()) {
            return this;
        }
        return new ScanReport(
                root,
                configFiles,
                findings.stream()
                        .filter(finding -> !ignoredCodes.contains(finding.code()))
                        .toList());
    }

    boolean hasErrors() {
        return findings.stream().anyMatch(finding -> finding.severity() == Severity.ERROR);
    }

    boolean hasWarnings() {
        return findings.stream().anyMatch(finding -> finding.severity() == Severity.WARNING);
    }
}
