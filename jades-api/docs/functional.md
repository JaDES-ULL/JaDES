# JaDES API - Funcional

Este módulo define los **contratos públicos** que describen el comportamiento de los elementos de simulación. No contiene lógica ejecutable, solo interfaces y tipos compartidos.

## ¿Qué aporta?

- **Modelo**: contratos de entidades, recursos, elementos, ciclos y contexto de simulación.
- **Flujos**: contratos de composición y control de flujo (`IFlowContract` y derivados).
- **Ubicación**: contratos de ubicación y movimiento (`ILocation`, `IMovable`, `IRouter`).
- **Información**: contratos para listeners y eventos de información.
- **Variables**: contratos para variables y almacenamiento.

## Uso esperado

Las implementaciones viven en `jades-core` y `jades-utils`. Este módulo se consume como dependencia para integrar o extender la simulación sin acoplarse a detalles internos.
