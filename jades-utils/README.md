# JaDES Utils Module

Utility library providing support functions, I/O, and advanced features for JaDES simulations.

## 📦 Purpose

Provides reusable components and utilities:
- **Functions**: Time-based, random, mathematical functions for parameters
- **I/O**: Excel integration (Apache POI), XML parsing, data export
- **Ontology**: OWL/RDF support for semantic modeling (OWL API + HermiT)
- **Concurrency**: Thread-safe utilities for parallel simulation
- **Cycle detection**: Graph algorithms for workflow validation
- **Random variates**: Extended probability distributions (via Simkit)

## 🎯 Key Features

- **Excel I/O**: Read/write simulation data from/to Excel files
- **Ontology reasoning**: Integrate semantic models (OWL, RDF/XML)
- **Function library**: 15+ parametric functions (constant, linear, exponential, etc.)
- **Cycle utilities**: Detect circular dependencies in workflow graphs
- **Thread-safe collections**: Concurrent access for multi-threaded simulations

## ⚠️ Note

This module contains heterogeneous functionality and is planned for modularization in v2.0:
- `jades-functions`: Core function library
- `jades-io`: Excel and XML I/O
- `jades-ontology`: OWL/RDF support
- `jades-concurrent`: Thread utilities
- `jades-algorithms`: Graph algorithms

See [Architecture Issues](../docs/architecture/overview.md#known-architectural-issues) for details.

## 📚 Documentation

- [Architecture Overview](../docs/architecture/overview.md)
- [Custom Functions Guide](../docs/guides/custom-functions.md)
- [Excel I/O Guide](../docs/guides/excel-io.md)

Module docs:
- [Functional](docs/functional.md)
- [Technical](docs/technical.md)
- [Architecture](docs/architecture.md)
- [Diagrams](docs/diagrams/utils-architecture.md)

## 🔗 Dependencies

- **Apache POI**: Excel file manipulation (~8MB)
- **OWL API**: Ontology processing (~15MB)
- **HermiT**: OWL reasoning engine
- **Simkit**: Random number generation
- **Log4j2**: Logging

## 📄 License

Apache License 2.0 - See [LICENSE](../LICENSE) in root directory
