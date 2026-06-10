package io.github.configdoctor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void reportsDuplicatePortsAcrossServices() throws IOException {
        writeYaml("order-service/src/main/resources/bootstrap.yml", """
                spring:
                  application:
                    name: order-service
                  cloud:
                    nacos:
                      config:
                        server-addr: localhost:8848
                        namespace: saas
                server:
                  port: 11115
                """);
        writeYaml("payment-service/src/main/resources/bootstrap.yml", """
                spring:
                  application:
                    name: payment-service
                  cloud:
                    nacos:
                      config:
                        server-addr: localhost:8848
                        namespace: saas
                server:
                  port: 11115
                """);

        ScanReport report = new ProjectScanner(8).scan(tempDir);

        assertTrue(report.findings().stream().anyMatch(finding -> finding.code().equals("DUPLICATE_PORT")));
    }

    @Test
    void validatesPortRange() throws IOException {
        writeYaml("gateway/src/main/resources/application.yml", """
                spring:
                  application:
                    name: gateway
                server:
                  port: 80
                """);

        ScanReport report = new ProjectScanner(8).scan(tempDir);

        assertTrue(report.hasErrors());
        assertEquals("PORT_RANGE", report.findings().stream()
                .filter(finding -> finding.severity() == Severity.ERROR)
                .findFirst()
                .orElseThrow()
                .code());
    }

    @Test
    void warnsWhenBootstrapHasNoNacosServer() throws IOException {
        writeYaml("user-service/src/main/resources/bootstrap.yml", """
                spring:
                  application:
                    name: user-service
                server:
                  port: 11114
                """);

        ScanReport report = new ProjectScanner(8).scan(tempDir);

        assertTrue(report.findings().stream().anyMatch(finding -> finding.code().equals("NACOS_SERVER_MISSING")));
    }

    @Test
    void warnsWhenGatewayRouteHasNoUri() throws IOException {
        writeYaml("gateway/src/main/resources/bootstrap.yml", """
                spring:
                  application:
                    name: gateway
                  cloud:
                    nacos:
                      config:
                        server-addr: localhost:8848
                        namespace: saas
                    gateway:
                      routes:
                        - id: order-service
                          predicates:
                            - Path=/orders/**
                server:
                  port: 18080
                """);

        ScanReport report = new ProjectScanner(8).scan(tempDir);

        assertTrue(report.findings().stream().anyMatch(finding -> finding.code().equals("GATEWAY_ROUTE_URI_MISSING")));
    }

    @Test
    void warnsWhenGatewayRouteHasNoIdOrPredicates() throws IOException {
        writeYaml("gateway/src/main/resources/bootstrap.yml", """
                spring:
                  application:
                    name: gateway
                  cloud:
                    nacos:
                      config:
                        server-addr: localhost:8848
                        namespace: saas
                    gateway:
                      routes:
                        - uri: lb://order-service
                server:
                  port: 18080
                """);

        ScanReport report = new ProjectScanner(8).scan(tempDir);

        assertTrue(report.findings().stream().anyMatch(finding -> finding.code().equals("GATEWAY_ROUTE_ID_MISSING")));
        assertTrue(report.findings().stream().anyMatch(finding -> finding.code().equals("GATEWAY_ROUTE_PREDICATES_MISSING")));
    }

    @Test
    void acceptsMavenFilteredYamlPlaceholders() throws IOException {
        writeYaml("goods-service/src/main/resources/bootstrap.yml", """
                spring:
                  application:
                    name: goods-service
                  cloud:
                    nacos:
                      config:
                        server-addr: @nacos.server@
                        namespace: @nacos.namespace@
                        group: @nacos.group@
                seata:
                  tx-service-group: @artifactId@-group
                server:
                  port: 11113
                """);

        ScanReport report = new ProjectScanner(8).scan(tempDir);

        assertTrue(report.findings().stream().noneMatch(finding -> finding.code().equals("YAML_PARSE_FAILED")));
    }

    @Test
    void warnsWhenEnabledSeataHasNoTransactionServiceGroup() throws IOException {
        writeYaml("order-service/src/main/resources/application.yml", """
                spring:
                  application:
                    name: order-service
                seata:
                  enabled: true
                server:
                  port: 11115
                """);

        ScanReport report = new ProjectScanner(8).scan(tempDir);

        assertTrue(report.findings().stream()
                .anyMatch(finding -> finding.code().equals("SEATA_TX_SERVICE_GROUP_MISSING")));
    }

    @Test
    void acceptsEnabledSeataWithTransactionServiceGroup() throws IOException {
        writeYaml("order-service/src/main/resources/application.yml", """
                spring:
                  application:
                    name: order-service
                seata:
                  enabled: true
                  tx-service-group: order-service-group
                server:
                  port: 11115
                """);

        ScanReport report = new ProjectScanner(8).scan(tempDir);

        assertTrue(report.findings().stream()
                .noneMatch(finding -> finding.code().equals("SEATA_TX_SERVICE_GROUP_MISSING")));
    }

    @Test
    void bundledExampleProjectScansCleanly() {
        ScanReport report = new ProjectScanner(8).scan(Path.of("examples/spring-cloud-alibaba-sample"));

        assertEquals(2, report.configFiles().size());
        assertTrue(report.findings().isEmpty());
    }

    private void writeYaml(String relativePath, String content) throws IOException {
        Path path = tempDir.resolve(relativePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }
}
