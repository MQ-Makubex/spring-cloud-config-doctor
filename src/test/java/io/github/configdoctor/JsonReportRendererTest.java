package io.github.configdoctor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class JsonReportRendererTest {

    @Test
    void rendersMachineReadableFindings() {
        ScanReport report = new ScanReport(
                Path.of("sample-project"),
                List.of(),
                List.of(new Finding(
                        Severity.WARNING,
                        "SERVICE_NAME_MISSING",
                        "spring.application.name is missing.",
                        Path.of("sample-project/application.yml"))));
        StringWriter buffer = new StringWriter();

        new JsonReportRenderer(new PrintWriter(buffer)).render(report);

        String json = buffer.toString();
        assertTrue(json.contains("\"configFileCount\": 0"));
        assertTrue(json.contains("\"hasWarnings\": true"));
        assertTrue(json.contains("\"severity\": \"WARNING\""));
        assertTrue(json.contains("\"code\": \"SERVICE_NAME_MISSING\""));
    }
}
