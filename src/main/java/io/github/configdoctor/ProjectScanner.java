package io.github.configdoctor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

final class ProjectScanner {

    private static final Pattern SERVICE_NAME = Pattern.compile("[a-z][a-z0-9-]*");

    private final int maxDepth;

    ProjectScanner(int maxDepth) {
        this.maxDepth = Math.max(1, maxDepth);
    }

    ScanReport scan(Path root) {
        List<Finding> findings = new ArrayList<>();
        List<ConfigFile> files = new ArrayList<>();

        if (!Files.isDirectory(root)) {
            findings.add(new Finding(Severity.ERROR, "ROOT_NOT_FOUND", "Scan root does not exist or is not a directory.", root));
            return new ScanReport(root, List.of(), findings);
        }

        for (Path candidate : discoverConfigFiles(root, findings)) {
            parseConfig(candidate, findings).ifPresent(files::add);
        }

        if (files.isEmpty() && findings.stream().noneMatch(finding -> finding.severity() == Severity.ERROR)) {
            findings.add(new Finding(Severity.WARNING, "NO_CONFIG_FILES", "No application*.yml or bootstrap*.yml files were found.", root));
        }

        validateConfigFiles(files, findings);
        return new ScanReport(root, files, findings);
    }

    private List<Path> discoverConfigFiles(Path root, List<Finding> findings) {
        try (Stream<Path> paths = Files.walk(root, maxDepth)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(ProjectScanner::isConfigFile)
                    .filter(ProjectScanner::isNotGeneratedPath)
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            findings.add(new Finding(Severity.ERROR, "SCAN_FAILED", "Failed to scan project: " + ex.getMessage(), root));
            return List.of();
        }
    }

    private static boolean isConfigFile(Path path) {
        String fileName = path.getFileName().toString();
        return (fileName.startsWith("application") || fileName.startsWith("bootstrap"))
                && (fileName.endsWith(".yml") || fileName.endsWith(".yaml"));
    }

    private static boolean isNotGeneratedPath(Path path) {
        for (Path part : path) {
            String name = part.toString();
            if (name.equals(".git")
                    || name.equals("target")
                    || name.equals("build")
                    || name.equals("node_modules")
                    || name.equals("docs")) {
                return false;
            }
        }
        return true;
    }

    private Optional<ConfigFile> parseConfig(Path path, List<Finding> findings) {
        try {
            String yaml = MavenPlaceholderSupport.quoteBarePlaceholders(Files.readString(path));
            Object loaded = new Load(LoadSettings.builder().build()).loadFromString(yaml);
            Map<String, Object> document = toStringMap(loaded);
            return Optional.of(new ConfigFile(
                    path,
                    document,
                    YamlPath.stringAt(document, "spring.application.name"),
                    YamlPath.integerAt(document, "server.port"),
                    firstPresent(
                            YamlPath.stringAt(document, "spring.cloud.nacos.config.server-addr"),
                            YamlPath.stringAt(document, "spring.cloud.nacos.discovery.server-addr")),
                    firstPresent(
                            YamlPath.stringAt(document, "spring.cloud.nacos.config.namespace"),
                            YamlPath.stringAt(document, "spring.cloud.nacos.discovery.namespace"))));
        } catch (RuntimeException | IOException ex) {
            findings.add(new Finding(Severity.ERROR, "YAML_PARSE_FAILED", "Cannot parse YAML: " + ex.getMessage(), path));
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toStringMap(Object loaded) {
        if (!(loaded instanceof Map<?, ?> source)) {
            return Map.of();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(Objects.toString(entry.getKey()), entry.getValue());
        }
        return result;
    }

    @SafeVarargs
    private static <T> Optional<T> firstPresent(Optional<T>... values) {
        for (Optional<T> value : values) {
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    private void validateConfigFiles(List<ConfigFile> files, List<Finding> findings) {
        for (ConfigFile file : files) {
            validateServiceName(file, findings);
            validatePort(file, findings);
            validateNacos(file, findings);
            validateGatewayRoutes(file, findings);
        }
        validateDuplicatePorts(files, findings);
    }

    private void validateServiceName(ConfigFile file, List<Finding> findings) {
        if (file.serviceName().isEmpty()) {
            findings.add(new Finding(Severity.WARNING, "SERVICE_NAME_MISSING", "spring.application.name is missing.", file.path()));
            return;
        }

        String serviceName = file.serviceName().orElseThrow();
        if (serviceName.contains("@")) {
            return;
        }
        if (!SERVICE_NAME.matcher(serviceName).matches()) {
            findings.add(new Finding(Severity.WARNING, "SERVICE_NAME_FORMAT", "spring.application.name should use lower-kebab-case.", file.path()));
        }
    }

    private void validatePort(ConfigFile file, List<Finding> findings) {
        file.serverPort().ifPresent(port -> {
            if (port < 1024 || port > 65535) {
                findings.add(new Finding(Severity.ERROR, "PORT_RANGE", "server.port must be between 1024 and 65535.", file.path()));
            }
        });
    }

    private void validateNacos(ConfigFile file, List<Finding> findings) {
        if (file.path().getFileName().toString().startsWith("bootstrap") && file.nacosServer().isEmpty()) {
            findings.add(new Finding(Severity.WARNING, "NACOS_SERVER_MISSING", "Nacos server address is missing in bootstrap config.", file.path()));
        }
        if (file.nacosServer().isPresent() && file.nacosNamespace().isEmpty()) {
            findings.add(new Finding(Severity.INFO, "NACOS_NAMESPACE_EMPTY", "Nacos namespace is not set; the default namespace will be used.", file.path()));
        }
    }

    private void validateGatewayRoutes(ConfigFile file, List<Finding> findings) {
        Optional<List<Object>> routes = YamlPath.listAt(file.document(), "spring.cloud.gateway.routes");
        if (routes.isEmpty()) {
            return;
        }

        for (Object route : routes.orElseThrow()) {
            if (!(route instanceof Map<?, ?> routeMap)) {
                findings.add(new Finding(Severity.WARNING, "GATEWAY_ROUTE_INVALID", "Gateway route entry should be a YAML object.", file.path()));
                continue;
            }

            Object id = routeMap.get("id");
            Object uri = routeMap.get("uri");
            Object predicates = routeMap.get("predicates");
            String routeName = id == null || id.toString().isBlank() ? "<unnamed>" : id.toString().trim();
            if (id == null || id.toString().isBlank()) {
                findings.add(new Finding(Severity.WARNING, "GATEWAY_ROUTE_ID_MISSING", "Gateway route is missing id.", file.path()));
            }
            if (uri == null || uri.toString().isBlank()) {
                findings.add(new Finding(Severity.WARNING, "GATEWAY_ROUTE_URI_MISSING", "Gateway route " + routeName + " is missing uri.", file.path()));
            }
            if (!hasPredicates(predicates)) {
                findings.add(new Finding(Severity.WARNING, "GATEWAY_ROUTE_PREDICATES_MISSING", "Gateway route " + routeName + " has no predicates.", file.path()));
            }
        }
    }

    private static boolean hasPredicates(Object predicates) {
        if (predicates instanceof List<?> list) {
            return !list.isEmpty();
        }
        return predicates != null && !predicates.toString().isBlank();
    }

    private void validateDuplicatePorts(List<ConfigFile> files, List<Finding> findings) {
        Map<Integer, List<ConfigFile>> byPort = files.stream()
                .filter(file -> file.serverPort().isPresent())
                .collect(Collectors.groupingBy(file -> file.serverPort().orElseThrow(), LinkedHashMap::new, Collectors.toList()));

        byPort.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> {
                    String services = entry.getValue().stream()
                            .map(file -> file.serviceName().orElse(file.path().getFileName().toString()))
                            .distinct()
                            .collect(Collectors.joining(", "));
                    findings.add(new Finding(Severity.WARNING, "DUPLICATE_PORT", "server.port " + entry.getKey() + " is used by: " + services, entry.getValue().get(0).path()));
                });
    }
}
