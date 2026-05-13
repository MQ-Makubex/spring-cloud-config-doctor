package io.github.configdoctor;

import java.io.PrintWriter;
import java.util.Comparator;

final class ReportRenderer {

    private final PrintWriter out;

    ReportRenderer(PrintWriter out) {
        this.out = out;
    }

    void render(ScanReport report) {
        out.println("Spring Cloud Config Doctor");
        out.println("Root: " + report.root().toAbsolutePath().normalize());
        out.println("Config files: " + report.configFiles().size());
        out.println();

        if (report.findings().isEmpty()) {
            out.println("No findings. Configuration looks healthy.");
            return;
        }

        report.findings().stream()
                .sorted(Comparator.comparing(Finding::severity).thenComparing(Finding::code))
                .forEach(finding -> out.printf(
                        "[%s] %s %s (%s)%n",
                        finding.severity(),
                        finding.code(),
                        finding.message(),
                        finding.path().toAbsolutePath().normalize()));
    }
}
