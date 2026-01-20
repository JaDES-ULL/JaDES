# JaDES API - Arquitectura

## Rol en la arquitectura

`jades-api` es la **capa de contratos**. Define las interfaces que permiten a `jades-core` y `jades-utils` interoperar sin dependencias circulares.

## Dependencias

- **Inbound**: `jades-core`, `jades-utils`, integraciones externas.
- **Outbound**: solo librerías necesarias para contratos (p. ej. Log4j API).

## Diagramas

Consulta [docs/diagrams/overview.md](diagrams/overview.md).
