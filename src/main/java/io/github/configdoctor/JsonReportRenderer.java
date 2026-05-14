package io.github.configdoctor;

import java.io.PrintWriter;
import java.nio.file.Path;

final class JsonReportRenderer {

    private final PrintWriter out;

    JsonReportRenderer(PrintWriter out) {
        this.out = out;
    }

    void render(ScanReport report) {
        out.println("{");
        out.println("  \"root\": \"" + escape(report.root().toAbsolutePath().normalize().toString()) + "\",");
        out.println("  \"configFileCount\": " + report.configFiles().size() + ",");
        out.println("  \"hasErrors\": " + report.hasErrors() + ",");
        out.println("  \"hasWarnings\": " + report.hasWarnings() + ",");
        out.println("  \"findings\": [");
        for (int index = 0; index < report.findings().size(); index++) {
            Finding finding = report.findings().get(index);
            out.println("    {");
            out.println("      \"severity\": \"" + finding.severity() + "\",");
            out.println("      \"code\": \"" + escape(finding.code()) + "\",");
            out.println("      \"message\": \"" + escape(finding.message()) + "\",");
            out.println("      \"path\": \"" + escape(normalize(finding.path())) + "\"");
            out.print("    }");
            if (index < report.findings().size() - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }
        out.println("  ]");
        out.println("}");
    }

    private static String normalize(Path path) {
        return path.toAbsolutePath().normalize().toString();
    }

    private static String escape(String value) {
        StringBuilder result = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            switch (ch) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        result.append(String.format("\\u%04x", (int) ch));
                    } else {
                        result.append(ch);
                    }
                }
            }
        }
        return result.toString();
    }
}
