# JaDES Architecture Overview

## 🏗️ High-Level Architecture

JaDES is a discrete event simulation framework built on a modular architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                    JaDES Framework                           │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐              ┌──────────────┐            │
│  │  jades-core  │──────────────▶│ jades-utils  │            │
│  │              │   depends on  │              │            │
│  │  Simulation  │               │  Functions   │            │
│  │   Engine     │               │   Cycles     │            │
│  │   Flows      │               │   I/O        │            │
│  │   Resources  │               │  Ontology    │            │
│  └──────────────┘               └──────────────┘            │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## 📦 Module Breakdown

### jades-core
**Purpose**: Core simulation engine

**Key Components**:
- `model/` - Domain models (Simulation, Element, Resource)
- `model/engine/` - Execution engine (SimulationEngine, event scheduling)
- `model/flow/` - Workflow patterns (WFP implementation)
- `condition/` - Conditional logic system
- `info/` - Event notification system
- `inforeceiver/` - Listeners and observers

**Design Patterns**:
- **Observer Pattern**: Info/InfoReceiver system
- **Strategy Pattern**: Flow implementations
- **Command Pattern**: DiscreteEvent execution
- **Factory Pattern**: Element/Resource creation (deprecated)

### jades-utils
**Purpose**: Reusable utilities independent of simulation logic

**Key Components**:
- `functions/` - Time functions (constant, linear, polynomial, random)
- `utils/cycle/` - Temporal cycle definitions
- `utils/` - General utilities (Prioritizable, Statistics, ExcelTools)
- `utils/concurrent/` - Thread pool management
- `ontology/` - OWL ontology support (optional, heavy)
- `simkit/random/` - Statistical random variates (TO BE REMOVED - use dependency)

## 🔄 Simulation Flow

```
1. User defines Simulation
   ↓
2. Add Resources, ResourceTypes
   ↓
3. Add Elements (entities)
   ↓
4. Define ActivityManagers
   ↓
5. Attach Flows to Activities
   ↓
6. Register Listeners
   ↓
7. simulation.run()
   ↓
8. SimulationEngine schedules events
   ↓
9. Events execute (modify state)
   ↓
10. Listeners notified
    ↓
11. Simulation completes
    ↓
12. Results collected
```

## 🎯 Core Concepts

### Simulation
- Main orchestrator
- Manages time progression
- Coordinates all entities
- **Issue**: God Object (667 lines) - needs refactoring

### Element
- Entities moving through simulation
- Can request/release resources
- Follow flows (workflow patterns)

### Resource
- Limited capacity entities
- Can be seized/released by Elements
- Managed by ResourceType

### Flow
- Defines Element behavior
- Implements Workflow Patterns
- Chained to form complex processes

### ActivityManager
- Manages activities (work units)
- Coordinates Element-Resource interactions

### SimulationEngine
- Event scheduler
- Time advancement
- State consistency

## 🧩 Workflow Patterns Support

JaDES implements workflow patterns from [workflowpatterns.com](http://www.workflowpatterns.com):

| Pattern | Class | Status |
|---------|-------|--------|
| WFP-01: Sequence | BasicFlow | ✅ Implemented |
| WFP-02: Parallel Split | ParallelFlow | ✅ Implemented |
| WFP-03: Synchronization | SynchronizationFlow | ✅ Implemented |
| WFP-04: Exclusive Choice | ExclusiveChoiceFlow | ✅ Implemented |
| WFP-05: Simple Merge | SimpleMergeFlow | ✅ Implemented |
| ... | ... | ... |

## 🔌 Extension Points

### Custom Flows
```java
public class MyCustomFlow extends AbstractSingleSuccessorFlow {
    @Override
    public void request(ElementInstance ei) {
        // Your logic here
    }
}
```

### Custom Listeners
```java
public class MyListener extends BasicListener {
    @Override
    public void infoEmitted(SimulationInfo info) {
        // Handle simulation events
    }
}
```

### Custom Time Functions
```java
public class MyTimeFunction extends AbstractTimeFunction {
    @Override
    public double getValue(TimeUnit unit) {
        // Your calculation
    }
}
```

## 🚧 Known Architectural Issues

### 1. God Object: Simulation.java
- **Problem**: 667 lines, too many responsibilities
- **Impact**: Hard to test, maintain, extend
- **Solution**: Refactor into:
  - `SimulationCore` (logic)
  - `SimulationConfiguration` (setup)
  - `SimulationLifecycle` (start/stop)
  - `SimulationEventBus` (notifications)

### 2. Monolithic jades-utils
- **Problem**: Mixed responsibilities (I/O, math, ontology, threading)
- **Impact**: Heavy dependencies for all users
- **Solution**: Split into:
  - `jades-functions`
  - `jades-cycles`
  - `jades-io`
  - `jades-ontology` (optional)
  - `jades-concurrent`

### 3. Factory Package Underutilized
- **Problem**: Only 1 real usage (BarrelShipping)
- **Impact**: Maintenance burden
- **Solution**: Deprecate, remove in 2.0

### 4. Duplicate simkit/ Code
- **Problem**: Code copied despite Maven dependency
- **Impact**: Maintenance, security risk
- **Solution**: Remove, use dependency only

## 📚 Further Reading

- [Getting Started Guide](../guides/getting-started.md)
- [Advanced Usage](../guides/advanced-usage.md)
- [API Documentation](../api/) (Generated JavaDocs)
- [Examples](../examples/)
