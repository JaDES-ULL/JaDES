# JaDES Core - Diagramas

## Flujo de ejecución

```mermaid
flowchart TD
  Simulation --> Engine
  Engine --> Events
  Events --> Flows
  Flows --> Elements
  Elements --> Resources
  Resources --> Locations
```
