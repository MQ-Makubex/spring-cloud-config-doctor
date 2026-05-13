package io.github.configdoctor;

import java.nio.file.Path;

record Finding(Severity severity, String code, String message, Path path) {
}
