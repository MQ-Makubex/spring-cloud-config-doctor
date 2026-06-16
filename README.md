# Spring Cloud Config Doctor

Spring Cloud Config Doctor is a small Java 21 command-line tool that audits Spring Cloud Alibaba and Nacos configuration files before a microservice project is started or deployed.

It scans `application*.yml`, `application*.yaml`, `bootstrap*.yml`, and `bootstrap*.yaml` files and reports common configuration risks such as missing service names, invalid ports, duplicate ports, missing Nacos server addresses, and default Nacos namespaces.

## Why this exists

Large Spring Cloud repositories often contain many services with similar configuration files. Small configuration drift can cause startup failures, service registration mistakes, or hard-to-debug local development issues. This project gives maintainers a fast, local check that can run in a terminal or CI pipeline.

## Features

- Finds Spring Boot and Spring Cloud YAML configuration files.
- Parses YAML with SnakeYAML Engine instead of line-based string matching.
- Understands common Maven resource-filtering placeholders such as `@nacos.group@`.
- Checks `spring.application.name`, `server.port`, and common Nacos config/discovery settings.
- Checks configured Spring Cloud Gateway routes for missing `id`, `uri`, or predicates.
- Warns when Seata is enabled without a `tx-service-group`.
- Detects duplicate ports across services.
- Allows specific finding codes to be ignored during gradual CI rollout.
- Skips generated and documentation folders such as `target/`, `build/`, and `docs/`.
- Supports text, JSON, and SARIF report output.
- Provides CI-friendly exit codes.
- Runs on Java 21 or newer.

## Quick start

```bash
mvn clean package
java -jar target/spring-cloud-config-doctor-0.1.0-SNAPSHOT.jar /path/to/your/project
```

Use `--fail-on-warn` when warnings should fail CI:

```bash
java -jar target/spring-cloud-config-doctor-0.1.0-SNAPSHOT.jar --fail-on-warn /path/to/your/project
```

Use JSON output when another tool or CI step needs to consume the report:

```bash
java -jar target/spring-cloud-config-doctor-0.1.0-SNAPSHOT.jar --format json /path/to/your/project
```

Use SARIF output when a code scanning workflow needs standard static-analysis results:

```bash
java -jar target/spring-cloud-config-doctor-0.1.0-SNAPSHOT.jar --format sarif /path/to/your/project
```

Ignore a known finding code when a repository needs a gradual rollout:

```bash
java -jar target/spring-cloud-config-doctor-0.1.0-SNAPSHOT.jar --ignore-code NACOS_NAMESPACE_EMPTY /path/to/your/project
```

Try the bundled Spring Cloud Alibaba sample to verify the scanner locally:

```bash
java -jar target/spring-cloud-config-doctor-0.1.0-SNAPSHOT.jar examples/spring-cloud-alibaba-sample
```

## CLI reference

```text
config-doctor [--fail-on-warn] [--format=<format>] [--ignore-code=<code>[,<code>...]] [--max-depth=<depth>] [ROOT]
```

| Option | Default | Description |
| --- | --- | --- |
| `ROOT` | `.` | Project root to scan. |
| `--format` | `text` | Report format: `text`, `json`, or `sarif`. |
| `--ignore-code` | none | Finding code to ignore before rendering and exit-code calculation. Can be repeated or comma-separated. |
| `--max-depth` | `8` | Maximum directory depth to scan. |
| `--fail-on-warn` | disabled | Return exit code `2` when warnings are found. |
| `-h`, `--help` | | Show command help. |
| `-V`, `--version` | | Show the application version. |

Exit codes are stable for CI use:

| Exit code | Meaning |
| --- | --- |
| `0` | No errors were found, and warnings are allowed. |
| `1` | At least one error was found. |
| `2` | Warnings were found while `--fail-on-warn` was enabled. |

## GitHub Actions with SARIF

The SARIF report can be uploaded to GitHub code scanning:

```yaml
name: Config Doctor

on:
  pull_request:
  push:
    branches: [main]

permissions:
  contents: read
  security-events: write

jobs:
  scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven
      - run: mvn -B package
      - name: Scan configuration
        run: java -jar target/spring-cloud-config-doctor-0.1.0-SNAPSHOT.jar --format sarif . > config-doctor.sarif
      - name: Upload SARIF
        if: always()
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: config-doctor.sarif
```

The scan step returns a non-zero exit code when errors are found. Add `continue-on-error: true` to that step when code scanning should upload findings without failing the job immediately.

## Example output

```text
Spring Cloud Config Doctor
Root: /workspace/lilishop-saas
Config files: 15

[WARNING] DUPLICATE_PORT server.port 11115 is used by: order-service, payment-service (/workspace/lilishop-saas/service/order-service/src/main/resources/bootstrap.yml)
[INFO] NACOS_NAMESPACE_EMPTY Nacos namespace is not set; the default namespace will be used. (/workspace/lilishop-saas/gateway/src/main/resources/bootstrap.yml)
```

## Project status

This repository is intentionally small and early-stage. The first stable milestone is focused on reliable local checks for Spring Cloud Alibaba repositories. Future work may include:

- Configurable rules.
- Markdown reports for pull requests.
- Spring profile awareness.
- More checks for Seata, Redis, RabbitMQ, and advanced Gateway route risks.

## JetBrains Open Source license note

This project is released as a non-commercial open source project under the Apache License 2.0. JetBrains says open-source software projects can qualify for one-year complimentary subscriptions for non-commercial open-source development, subject to approval. See the official JetBrains pages for current terms:

- [Community programs for open-source projects](https://www.jetbrains.com/community/opensource/)
- [Toolbox subscription options comparison](https://www.jetbrains.com/store/comparison.html)
- [Toolbox Subscription Agreement for Open Source Projects](https://www.jetbrains.com/legal/docs/toolbox/license_opensource/)

When applying, use accurate information about the public repository, contributors, license, and project activity. Do not describe the project as mature or widely used until that is true.

## Development

```bash
mvn test
```

## Contributing

Issues and pull requests are welcome. Before proposing a large feature, open an issue so the design can be discussed.

## License

Apache License 2.0. See [LICENSE](LICENSE).
