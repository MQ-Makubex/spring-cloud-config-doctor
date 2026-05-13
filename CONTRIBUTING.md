# Contributing

Thanks for helping improve Spring Cloud Config Doctor.

## Local setup

Requirements:

- Java 21 or newer
- Maven 3.9 or newer

Run tests:

```bash
mvn test
```

Build the executable jar:

```bash
mvn clean package
```

## Pull request guidelines

- Keep changes focused on one problem.
- Add tests for new rules or changed behavior.
- Prefer structured parsers and Java APIs over ad-hoc text matching.
- Update the README when user-visible behavior changes.

## Rule design

Rules should be practical and low-noise. A finding should help a maintainer prevent a real startup, deployment, or operations problem.
