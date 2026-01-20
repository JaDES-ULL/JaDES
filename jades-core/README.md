# JaDES Core Module

Core simulation engine for the JaDES (Java Discrete Event Simulation) framework.

## 📦 Purpose

Provides the fundamental discrete event simulation infrastructure:
- Simulation engine and scheduling
- Resource management (types, instances, work groups)
- Element lifecycle and flow execution
- Activity management and coordination
- Event notification system
- Workflow pattern implementations (WFP-01 through WFP-17)

## 🎯 Key Features

- **Event-driven architecture**: Efficient priority queue scheduling
- **Resource modeling**: Flexible resource types and work groups
- **Flow-based composition**: Chain activities using flows
- **Extensible design**: Custom flows, activities, and listeners
- **Workflow patterns**: Implements van der Aalst's WFP taxonomy

## 📚 Documentation

See the main project documentation:
- [Getting Started](../docs/guides/getting-started.md)
- [Architecture Overview](../docs/architecture/overview.md)
- [Examples](../docs/examples/)

Module docs:
- [Functional](docs/functional.md)
- [Technical](docs/technical.md)
- [Architecture](docs/architecture.md)
- [Diagrams](docs/diagrams/core-architecture.md)

## 🔗 Dependencies

- **jades-utils**: Utility library (functions, I/O, ontology support)
- **JFlex**: Lexical analysis for model definition language
- **Simkit**: Random variate generation
- **JCommander**: Command-line parsing
- **Log4j2**: Logging framework

## 📄 License

Apache License 2.0 - See [LICENSE](../LICENSE) in root directory
