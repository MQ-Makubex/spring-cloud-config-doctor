package io.github.configdoctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class YamlPath {

    private YamlPath() {
    }

    static Optional<String> stringAt(Map<String, Object> document, String dottedPath) {
        return valueAt(document, dottedPath)
                .map(Object::toString)
                .map(String::trim)
                .filter(value -> !value.isBlank());
    }

    static Optional<Integer> integerAt(Map<String, Object> document, String dottedPath) {
        return valueAt(document, dottedPath)
                .flatMap(YamlPath::toInteger);
    }

    static Optional<Boolean> booleanAt(Map<String, Object> document, String dottedPath) {
        return valueAt(document, dottedPath)
                .flatMap(YamlPath::toBoolean);
    }

    static Optional<List<Object>> listAt(Map<String, Object> document, String dottedPath) {
        Optional<Object> value = valueAt(document, dottedPath);
        if (value.isPresent() && value.orElseThrow() instanceof List<?> list) {
            return Optional.of(new ArrayList<>(list));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static Optional<Object> valueAt(Map<String, Object> document, String dottedPath) {
        Object cursor = document;
        for (String key : dottedPath.split("\\.")) {
            if (!(cursor instanceof Map<?, ?> map)) {
                return Optional.empty();
            }
            cursor = ((Map<String, Object>) map).get(key);
            if (cursor == null) {
                return Optional.empty();
            }
        }
        return Optional.of(cursor);
    }

    private static Optional<Integer> toInteger(Object value) {
        if (value instanceof Number number) {
            return Optional.of(number.intValue());
        }
        try {
            return Optional.of(Integer.parseInt(value.toString().trim()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> toBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return Optional.of(booleanValue);
        }
        String text = value.toString().trim();
        if (text.equalsIgnoreCase("true")) {
            return Optional.of(true);
        }
        if (text.equalsIgnoreCase("false")) {
            return Optional.of(false);
        }
        return Optional.empty();
    }
}
