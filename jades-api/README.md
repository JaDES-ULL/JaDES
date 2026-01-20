# JaDES API Module

Contratos públicos y tipos compartidos de JaDES. Este módulo define las interfaces que implementan los módulos de runtime.

## 📦 Propósito

- Definir contratos de simulación (`model`, `flow`, `location`, `variable`, `info`).
- Evitar dependencias circulares entre módulos de runtime.
- Servir como superficie estable para integraciones externas.

## 📚 Documentación

- [Funcional](docs/functional.md)
- [Técnica](docs/technical.md)
- [Arquitectura](docs/architecture.md)
- [Diagramas](docs/diagrams/overview.md)

## 🔗 Dependencias

- Log4j API (interfaces de logging)
- Simkit (contratos de experimentación)
- Apache POI (contratos de fórmulas Excel)

## 📄 Licencia

Apache License 2.0 - ver [LICENSE](../LICENSE).
