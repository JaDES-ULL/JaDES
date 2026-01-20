# JaDES API - Técnica

## Build

```bash
mvn -pl jades-api -am test
```

## Compatibilidad

- **API estable**: cambios en interfaces deben ser compatibles hacia atrás.
- **Sin dependencias de runtime**: solo dependencias necesarias para tipos compartidos.

## Paquetes principales

- `es.ull.simulation.model`
- `es.ull.simulation.model.flow`
- `es.ull.simulation.model.location`
- `es.ull.simulation.info` / `es.ull.simulation.inforeceiver`
- `es.ull.simulation.variable`

## Reglas

- No incluir lógica de negocio.
- No depender de `jades-core` ni `jades-utils`.
