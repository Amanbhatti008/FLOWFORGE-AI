# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Final Production Readiness**: Integration tests for API endpoints.
- **Monitoring & Observability**: SLF4J/Logback for structured logging, and Micrometer for Prometheus metrics.
- **Caching**: Redis Cache integration using Spring Boot Cache and Redisson for template and workflow caching.
- **Resilience**: Redis-backed distributed rate limiting via Bucket4j, and Kafka retry/dead-letter mechanisms.
- **DevOps**: Complete GitHub Actions CI/CD with Docker build, Trivy security scanning, Checkstyle, SpotBugs, and Jacoco.
- **Documentation**: Extensive architectural documentation including scalability strategies, Kafka flows, and DB design.

### Changed
- Converted in-memory rate limiting to distributed Redis limits.
- Optimized database indexing.
- Improved JWT security with role-based access control and input validation.

### Fixed
- Addressed multiple IDE warnings and nullable annotations.
- Corrected GitHub Actions pipeline syntax errors.
