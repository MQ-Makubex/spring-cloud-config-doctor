package io.github.configdoctor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class SarifReportRendererTest {

    @Test
    void rendersSarifFindingsForCodeScanningTools() {
        ScanReport report = new ScanReport(
                Path.of("sample-project"),
                List.of(),
                List.of(
                        new Finding(
                                Severity.WARNING,
                                "SERVICE_NAME_MISSING",
                                "spring.application.name is missing.",
                                Path.of("sample-project/application.yml")),
                        new Finding(
                                Severity.ERROR,
                                "PORT_RANGE",
                                "server.port should be between 1024 and 65535.",
                                Path.of("sample-project/bootstrap.yml"))));
        StringWriter buffer = new StringWriter();

        new SarifReportRenderer(new PrintWriter(buffer)).render(report);

        String sarif = buffer.toString();
        assertTrue(sarif.contains("\"version\": \"2.1.0\""));
        assertTrue(sarif.contains("\"name\": \"Spring Cloud Config Doctor\""));
        assertTrue(sarif.contains("\"ruleId\": \"SERVICE_NAME_MISSING\""));
        assertTrue(sarif.contains("\"level\": \"warning\""));
        assertTrue(sarif.contains("\"ruleId\": \"PORT_RANGE\""));
        assertTrue(sarif.contains("\"level\": \"error\""));
        assertTrue(sarif.contains("\"uri\": \"sample-project/application.yml\""));
    }
}
