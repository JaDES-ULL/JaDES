# JaDES - Java Discrete Event Simulation

[![Build Status](https://github.com/JaDES-ULL/JaDES/workflows/build/badge.svg)](https://github.com/JaDES-ULL/JaDES/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=JaDES-ULL_JaDES&metric=alert_status)](https://sonarcloud.io/dashboard?id=JaDES-ULL_JaDES)

JaDES is a comprehensive Java framework for discrete event simulation, developed at Universidad de La Laguna.

## Modules

This is a multi-module Maven project with the following structure:

- **jades-core**: Core simulation engine with workflow patterns and execution logic
- **jades-utils**: Utility library with time functions, cycles, Excel I/O, ontology support, and more

## Requirements

- Java 17+ (LTS recommended)
- Maven 3.8+

## Building

```bash
mvn clean install
```

## Quick Start

```java
import es.ull.simulation.model.Simulation;
import es.ull.simulation.model.TimeUnit;

public class Example {
    public static void main(String[] args) {
        Simulation sim = new Simulation(0, "My Simulation", TimeUnit.MINUTE, 0, 100);
        // Configure your simulation...
        sim.run();
    }
}
```

## Dependencies

To use JaDES in your project:

```xml
<dependency>
    <groupId>es.ull.simulation</groupId>
    <artifactId>jades-core</artifactId>
    <version>version</version>
</dependency>
```

For utilities only:

```xml
<dependency>
    <groupId>es.ull.simulation</groupId>
    <artifactId>jades-utils</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Authors

JaDES Development Team - Universidad de La Laguna

