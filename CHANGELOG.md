# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

### Added

- Added `--format json` for machine-readable scan reports.
- Added `--format sarif` for code scanning compatible scan reports.
- Added a bundled Spring Cloud Alibaba sample project for local scanner smoke tests.
- Added basic Spring Cloud Gateway route checks for missing `id`, `uri`, and predicates.
- Added a Seata check for enabled services missing `tx-service-group`.
- Added `--ignore-code` for suppressing selected finding codes during gradual CI rollout.
- Added `--min-port` and `--max-port` for configuring the accepted `server.port` range.
- Documented CLI options, exit codes, and GitHub Actions SARIF upload setup.
- Documented supported finding codes and severities.

## 0.1.0 - 2026-05-13

### Added

- Initial Java 21 CLI for scanning Spring Cloud Alibaba and Nacos YAML configuration.
- Text report output with CI-friendly exit codes.
- Checks for service names, server ports, duplicate ports, and common Nacos settings.
- Support for Maven resource-filtering placeholders in YAML files.
