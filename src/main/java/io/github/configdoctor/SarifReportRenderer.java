package io.github.configdoctor;

import java.io.PrintWriter;
import java.nio.file.Path;

final class SarifReportRenderer {

    private final PrintWriter out;

    SarifReportRenderer(PrintWriter out) {
        this.out = out;
    }

    void render(ScanReport report) {
        out.println("{");
        out.println("  \"$schema\": \"https://json.schemastore.org/sarif-2.1.0.json\",");
        out.println("  \"version\": \"2.1.0\",");
        out.println("  \"runs\": [");
        out.println("    {");
        out.println("      \"tool\": {");
        out.println("        \"driver\": {");
        out.println("          \"name\": \"Spring Cloud Config Doctor\",");
        out.println("          \"informationUri\": \"https://github.com/MQ-Makubex/spring-cloud-config-doctor\"");
        out.println("        }");
        out.println("      },");
        out.println("      \"results\": [");
        for (int index = 0; index < report.findings().size(); index++) {
            Finding finding = report.findings().get(index);
            out.println("        {");
            out.println("          \"ruleId\": \"" + escape(finding.code()) + "\",");
            out.println("          \"level\": \"" + level(finding.severity()) + "\",");
            out.println("          \"message\": {");
            out.println("            \"text\": \"" + escape(finding.message()) + "\"");
            out.println("          },");
            out.println("          \"locations\": [");
            out.println("            {");
            out.println("              \"physicalLocation\": {");
            out.println("                \"artifactLocation\": {");
            out.println("                  \"uri\": \"" + escape(uri(report.root(), finding.path())) + "\"");
            out.println("                }");
            out.println("              }");
            out.println("            }");
            out.println("          ]");
            out.print("        }");
            if (index < report.findings().size() - 1) {
                out.println(",");
            } else {
                out.println();
            }
        }
        out.println("      ]");
        out.println("    }");
        out.println("  ]");
        out.println("}");
    }

    private static String level(Severity severity) {
        return switch (severity) {
            case ERROR -> "error";
            case WARNING -> "warning";
            case INFO -> "note";
        };
    }

    private static String uri(Path root, Path path) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path normalizedPath = path.normalize();
        if (normalizedPath.isAbsolute() && normalizedPath.startsWith(normalizedRoot)) {
            normalizedPath = normalizedRoot.relativize(normalizedPath);
        }
        return normalizedPath.toString().replace('\\', '/');
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
