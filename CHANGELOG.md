# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Multi-module Maven structure (jades-core + jades-utils)
- Unified GitHub workflows (build, SonarCloud)
- Issue and PR templates
- Contributing guidelines
- Security policy
- Code of Conduct
- JaCoCo coverage enabled by default

### Changed
- Downgraded to Java 17 LTS (from Java 25)
- Consolidated repositories (JaDES-core + utils-library → JaDES)
- Centralized dependency management in parent POM
- Updated LICENSE metadata (Apache 2.0)

### Deprecated
- ClassPathHacker.java (Java 9+ incompatible, will be removed in 2.0)

### Removed
- Duplicate .github/ workflows from modules
- Duplicate .gitignore files from modules

### Fixed
- Build compatibility with Java 17
- Workflow Java version mismatches

### Security
- Added security policy and reporting guidelines

## [1.0-SNAPSHOT] - 2026-01-08

### Added
- Initial monorepo setup
- Parent POM with shared configuration
- jades-core module (simulation engine)
- jades-utils module (utility library)

---

## Release Guidelines

### Version Format
- **Major.Minor.Patch** (e.g., 1.2.3)
- **Major**: Breaking changes
- **Minor**: New features, backwards-compatible
- **Patch**: Bug fixes, backwards-compatible

### Categories
- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security vulnerabilities

[Unreleased]: https://github.com/JaDES-ULL/JaDES/compare/v1.0.0...HEAD
