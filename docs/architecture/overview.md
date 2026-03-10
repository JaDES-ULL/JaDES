# JaDES Architecture Overview

## 🏗️ Arquitectura de alto nivel

JaDES es un framework de simulación de eventos discretos (DES) construido
sobre una arquitectura Maven multi-módulo de 5 componentes:

```
┌──────────────────────────────────────────────────────────────────────┐
│                         JaDES Framework v1.0                          │
├──────────────────────────────────────────────────────────────────────┤
│                                                                        │
│  ┌────────────────┐   depends on   ┌──────────────────┐              │
│  │  jades-core    │───────────────▶│   jades-utils    │              │
│  │                │                │                  │              │
│  │  Simulation    │   depends on   │  Functions       │              │
│  │  Engine        │───────────────▶│  Cycles          │              │
│  │  Flows (WFP)   │                │  I/O             │              │
│  │  Resources     │                │  Ontology (OWL)  │              │
│  │  Elements      │                └──────────────────┘              │
│  └────────────────┘                                                   │
│         │                                                              │
│         │ depends on                                                   │
│         ▼                                                              │
│  ┌────────────────┐                ┌──────────────────┐              │
│  │  jades-api     │                │  jades-random    │              │
│  │                │                │                  │              │
│  │  Interfaces    │                │  Distributions   │              │
│  │  Contracts     │                │  (Exp, Normal,   │              │
│  │  (ISimulation, │                │   Uniform, …)    │              │
│  │   IFlow, …)    │                └──────────────────┘              │
│  └────────────────┘                                                   │
│                                                                        │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │  jades-examples  (no publicado en Maven Central)               │  │
│  │  Caso 1: Urgencias (ExclusiveChoiceFlow, Poisson arrivals)     │  │
│  │  Caso 2: UCI (ParallelFlow + SynchronizationFlow + DoWhile)    │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                        │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 📦 Módulos

### jades-api
**Propósito**: Contratos e interfaces públicas del framework

**Componentes clave**:
- `ISimulation` — ciclo de vida de la simulación
- `IFlow`, `IElement`, `IResource` — abstracciones de dominio
- `ISimulationRegistry`, `IIdGenerator` — puntos de extensión (DIP)

**Por qué existe**: Separa el contrato (API estable) de la implementación,
permitiendo alternativas de motor sin cambiar el código de usuario.

---

### jades-core
**Propósito**: Motor principal de simulación

**Componentes clave**:
- `model/` — modelos de dominio (`Simulation`, `Element`, `Resource`)
- `model/engine/` — planificador de eventos (`SimulationEngine`, `DiscreteEvent`)
- `model/flow/` — patrones de flujo de trabajo (WFP)
  - `ActivityFlow` — unidad de trabajo con recursos
  - `ParallelFlow` / `SynchronizationFlow` — WFP-02/03
  - `ExclusiveChoiceFlow` / `SimpleMergeFlow` — WFP-04/05
  - `DoWhileFlow` — WFP-21
  - `TimeDrivenElementGenerator` — generación de entidades
- `condition/` — sistema de condiciones booleanas
- `info/` — sistema de notificación de eventos
- `inforeceiver/` — listeners y observadores

**Patrones de diseño**:
- **Observer**: sistema `Info`/`InfoReceiver` (`sim.registerListener(...)`)
- **Strategy**: implementaciones de `Flow`
- **Command**: ejecución de `DiscreteEvent`
- **DIP (Dependency Inversion)**: inyección de `ISimulationRegistry` e `IIdGenerator`

---

### jades-utils
**Propósito**: Utilidades reutilizables independientes de la lógica de simulación

**Componentes clave**:
- `functions/` — funciones temporales (constante, lineal, polinómica, aleatoria)
- `utils/cycle/` — definiciones de ciclos temporales (`PeriodicCycle`, etc.)
- `utils/` — utilidades generales (`Prioritizable`, `Statistics`, `ExcelTools`)
- `utils/concurrent/` — gestión de hilos
- `ontology/` — soporte OWL (opcional, dependencia pesada ~15 MB)
- `simkit/random/` — variates estadísticos **(pendiente de migración a `jades-random`)**

---

### jades-random
**Propósito**: Distribuciones de probabilidad reutilizables y sin estado

**Componentes clave**:
- `ExponentialFunction`, `NormalFunction`, `UniformFunction`, etc.
- Implementan la interfaz `TimeFunction` de `jades-utils`
- Sin dependencias de `jades-core` (pueden usarse en cualquier proyecto)

**Por qué existe**: Separa la generación de números aleatorios de la lógica
de simulación (ver ADR-009).

---

### jades-examples *(no publicado en Maven Central)*
**Propósito**: Casos de estudio de referencia que demuestran el uso del framework

**Escenarios**:
1. **Urgencias** (`EmergencyDeptModel`): llegadas Poisson + triaje con `ExclusiveChoiceFlow`,
   turno de 8h, réplica única
2. **UCI** (`ICUModel`): terapias paralelas con `ParallelFlow`/`SynchronizationFlow`,
   alta condicionada con `DoWhileFlow`, horizonte de 7 días, múltiples réplicas

**Cobertura**: 4 smoke tests que verifican comportamiento estadístico
(uso de camas, tiempos de espera) dentro de intervalos de confianza.

---

## 🔄 Flujo de ejecución de una simulación

```
1. Instanciar Simulation
   ↓
2. Definir ResourceType + Resource (con horario)
   ↓
3. Definir ElementType
   ↓
4. Construir grafo de ActivityFlows
   ↓
5. Conectar TimeDrivenElementGenerator al flujo inicial
   ↓
6. sim.registerListener(...)  ← observadores
   ↓
7. sim.run(startTime, endTime)
   ↓
8. SimulationEngine planifica DiscreteEvents
   ↓
9. Los eventos modifican el estado (seize / release / delay)
   ↓
10. Los listeners reciben notificaciones
    ↓
11. La simulación completa y devuelve resultados
```

---

## 🎯 Conceptos del dominio

| Concepto | Clase | Descripción |
|----------|-------|-------------|
| **Simulation** | `Simulation` | Orquestador principal; gestiona tiempo y entidades |
| **Element** | `Element` + `ElementType` | Entidades que atraviesan el sistema |
| **Resource** | `Resource` + `ResourceType` | Recursos con capacidad limitada |
| **Flow** | `ActivityFlow`, `ParallelFlow`, … | Comportamiento de los elementos (WFP) |
| **WorkGroup** | `WorkGroup` | Requisitos de recursos para una actividad |
| **SimulationEngine** | `SimulationEngine` | Planificador de eventos; avance del tiempo |

---

## 🧩 Soporte de Workflow Patterns

JaDES implementa los patrones del [workflowpatterns.com](http://www.workflowpatterns.com):

| Patrón | Clase | Estado |
|--------|-------|--------|
| WFP-01 Sequence | Encadenamiento `link()` | ✅ Implementado |
| WFP-02 Parallel Split | `ParallelFlow` | ✅ Implementado |
| WFP-03 Synchronization | `SynchronizationFlow` | ✅ Implementado |
| WFP-04 Exclusive Choice | `ExclusiveChoiceFlow` | ✅ Implementado |
| WFP-05 Simple Merge | `SimpleMergeFlow` | ✅ Implementado |
| WFP-21 Structured Loop | `DoWhileFlow` | ✅ Implementado |

---

## 🔌 Puntos de extensión

### Flow personalizado
```java
public class MyCustomFlow extends AbstractSingleSuccessorFlow {
    @Override
    public void request(ElementInstance ei) {
        // lógica personalizada
        super.request(ei);
    }
}
```

### Listener personalizado
```java
public class MyListener extends BasicListener {
    @Override
    public void infoEmitted(SimulationInfo info) {
        // manejar eventos de la simulación
    }
}
sim.registerListener(new MyListener());
```

### Función temporal personalizada
```java
public class MyTimeFunction extends AbstractTimeFunction {
    @Override
    public double getValue(TimeUnit unit) {
        return /* tu cálculo */;
    }
}
```

---

## 🚧 Problemas arquitecturales conocidos

### 1. God Object: `Simulation.java`
- **Problema**: 667 líneas, demasiadas responsabilidades
- **Impacto**: difícil de testear, mantener y extender
- **Solución propuesta**: refactorizar en `SimulationCore`, `SimulationConfiguration`,
  `SimulationLifecycle`, `SimulationEventBus` (planificado para v2.0)

### 2. `jades-utils` monolítico
- **Problema**: I/O, matemáticas, ontología y threading mezclados en un módulo
- **Impacto**: dependencias pesadas (~25 MB) para todos los usuarios
- **Solución**: dividir en `jades-functions`, `jades-io`, `jades-ontology` (ver ADR-008)

### 3. `simkit/` duplicado en `jades-utils`
- **Problema**: código copiado de la dependencia `simkit` a pesar de existir `jades-random`
- **Impacto**: mantenimiento duplicado, riesgo de CVEs
- **Solución**: migrar y eliminar el paquete `simkit/random` de `jades-utils`

---

## 📚 Lectura adicional

- [Guía de inicio rápido](../guides/getting-started.md)
- [Ejemplos de Workflow Patterns](../examples/workflow-patterns.md)
- [Casos de estudio sanitarios](../../jades-examples/README.md)
- [Decisiones de arquitectura (ADR)](../development/architecture-decisions.md)
- [Documentación de API](../api/) (JavaDocs generados)
