package io.github.configdoctor;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "config-doctor",
        mixinStandardHelpOptions = true,
        version = "spring-cloud-config-doctor 0.1.0",
        description = "Audit Spring Cloud Alibaba and Nacos service configuration files.")
public final class ConfigDoctorApplication implements Callable<Integer> {

    @Parameters(index = "0", defaultValue = ".", description = "Project root to scan.")
    private Path root;

    @Option(names = "--fail-on-warn", description = "Return a non-zero exit code when warnings are found.")
    private boolean failOnWarn;

    @Option(names = "--max-depth", defaultValue = "8", description = "Maximum directory depth to scan.")
    private int maxDepth;

    @Option(names = "--format", defaultValue = "text", description = "Output format: text, json, or sarif.")
    private String format;

    @Option(
            names = "--ignore-code",
            split = ",",
            description = "Finding code to ignore. Repeat or use commas, for example: --ignore-code NACOS_NAMESPACE_EMPTY,DUPLICATE_PORT.")
    private List<String> ignoredCodes = List.of();

    private final PrintWriter out;

    public ConfigDoctorApplication() {
        this(new PrintWriter(System.out, true));
    }

    ConfigDoctorApplication(PrintWriter out) {
        this.out = out;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ConfigDoctorApplication()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        ScanReport report = new ProjectScanner(maxDepth).scan(root)
                .withoutFindingsWithCodes(normalizedIgnoredCodes());
        switch (OutputFormat.from(format)) {
            case TEXT -> new ReportRenderer(out).render(report);
            case JSON -> new JsonReportRenderer(out).render(report);
            case SARIF -> new SarifReportRenderer(out).render(report);
        }

        if (report.hasErrors()) {
            return 1;
        }
        if (failOnWarn && report.hasWarnings()) {
            return 2;
        }
        return 0;
    }

    private Set<String> normalizedIgnoredCodes() {
        return ignoredCodes.stream()
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .map(code -> code.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}
