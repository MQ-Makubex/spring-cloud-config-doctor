package io.github.configdoctor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import picocli.CommandLine;

class ConfigDoctorApplicationTest {

    @TempDir
    Path tempDir;

    @Test
    void writesSarifOutputFromCliFormatOption() throws IOException {
        writeYaml("gateway/src/main/resources/bootstrap.yml", """
                spring:
                  application:
                    name: gateway
                server:
                  port: 80
                """);
        StringWriter buffer = new StringWriter();

        int exitCode = new CommandLine(new ConfigDoctorApplication(new PrintWriter(buffer)))
                .execute("--format", "sarif", tempDir.toString());

        assertEquals(1, exitCode);
        assertTrue(buffer.toString().contains("\"version\": \"2.1.0\""));
        assertTrue(buffer.toString().contains("\"ruleId\": \"PORT_RANGE\""));
        assertTrue(buffer.toString().contains("\"uri\": \"gateway/src/main/resources/bootstrap.yml\""));
    }

    @Test
    void ignoresConfiguredFindingCodesBeforeRenderingAndExitCode() throws IOException {
        writeYaml("gateway/src/main/resources/application.yml", """
                spring:
                  application:
                    name: gateway
                server:
                  port: 80
                """);
        StringWriter buffer = new StringWriter();

        int exitCode = new CommandLine(new ConfigDoctorApplication(new PrintWriter(buffer)))
                .execute("--ignore-code", "port_range", tempDir.toString());

        assertEquals(0, exitCode);
        assertTrue(buffer.toString().contains("No findings. Configuration looks healthy."));
    }

    private void writeYaml(String relativePath, String content) throws IOException {
        Path path = tempDir.resolve(relativePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
