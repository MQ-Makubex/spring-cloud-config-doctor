package io.github.configdoctor;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

record ConfigFile(
        Path path,
        Map<String, Object> document,
        Optional<String> serviceName,
        Optional<Integer> serverPort,
        Optional<String> nacosServer,
        Optional<String> nacosNamespace) {
}
