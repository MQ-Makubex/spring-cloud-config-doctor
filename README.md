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
- Detects duplicate ports across services.
- Skips generated and documentation folders such as `target/`, `build/`, and `docs/`.
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

- JSON and SARIF output.
- Configurable rules.
- Markdown reports for pull requests.
- Spring profile awareness.
- More checks for Seata, Redis, RabbitMQ, and Gateway routes.

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
