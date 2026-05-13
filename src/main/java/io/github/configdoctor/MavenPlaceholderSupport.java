package io.github.configdoctor;

import java.util.regex.Pattern;

final class MavenPlaceholderSupport {

    private static final Pattern MAPPING_VALUE = Pattern.compile("(?m)(:\\s*)(@[^\\s#]+)(\\s*(#.*)?$)");
    private static final Pattern LIST_VALUE = Pattern.compile("(?m)(^\\s*-\\s*)(@[^\\s#]+)(\\s*(#.*)?$)");

    private MavenPlaceholderSupport() {
    }

    static String quoteBarePlaceholders(String yaml) {
        String mappingSafe = MAPPING_VALUE.matcher(yaml).replaceAll("$1\"$2\"$3");
        return LIST_VALUE.matcher(mappingSafe).replaceAll("$1\"$2\"$3");
    }
}
