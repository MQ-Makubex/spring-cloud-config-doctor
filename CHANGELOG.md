# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

### Added

- Added `--format json` for machine-readable scan reports.
- Added a bundled Spring Cloud Alibaba sample project for local scanner smoke tests.
- Added basic Spring Cloud Gateway route checks for missing `id`, `uri`, and predicates.

## 0.1.0 - 2026-05-13

### Added

- Initial Java 21 CLI for scanning Spring Cloud Alibaba and Nacos YAML configuration.
- Text report output with CI-friendly exit codes.
- Checks for service names, server ports, duplicate ports, and common Nacos settings.
- Support for Maven resource-filtering placeholders in YAML files.
