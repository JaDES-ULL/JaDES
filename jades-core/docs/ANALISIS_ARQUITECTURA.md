# Análisis de Arquitectura y Violaciones SOLID

## Fecha: 9 de enero de 2026

## Resumen Ejecutivo

Este documento analiza las clases más complejas del proyecto JaDES Core, identificando violaciones de principios SOLID, problemas de arquitectura y barreras para la testabilidad.

---

## 1. Clase `Resource` (907 líneas, 17% cobertura)

### Métricas
- **Cobertura**: 17% (455 de 551 instrucciones sin cubrir)
- **Complejidad**: 49 métodos, múltiples responsabilidades
- **Líneas de código**: 907 líneas

### Violaciones Identificadas

#### 🔴 **Violación Crítica: Single Responsibility Principle (SRP)**

La clase `Resource` tiene **al menos 7 responsabilidades distintas**:

1. **Gestión de estado del recurso** (seized, available, timeout)
2. **Gestión de ubicación/movimiento** (IMovable, Location)
3. **Gestión de eventos del ciclo de vida** (onCreate, onDestroy, notifyEnd)
4. **Gestión de tablas de horario** (TimeTable, CancelPeriodTable)
5. **Gestión de roles** (ResourceType, CurrentResourceType)
6. **Coordinación con motor de ejecución** (ResourceEngine delegation)
7. **Notificación de información** (ResourceInfo, EntityLocationInfo)

```java
// Evidencia del problema:
public class Resource extends VariableStoreSimulationObject 
    implements IDescribable, IEventSource, IMovable {
    
    // Estado del recurso
    private boolean timeOut = false;
    protected ResourceType currentResourceType = null;
    
    // Ubicación
    private Location currentLocation;
    private Location initLocation;
    
    // Movimiento
    private ElementInstance movingInstance = null;
    
    // Tablas de horario
    protected final ArrayList<TimeTableEntry> timeTable;
    protected final ArrayList<TimeTableEntry> cancelPeriodTable;
    
    // Motor de ejecución
    private ResourceEngine engine;
}
```

**Impacto**:
- Testear un recurso requiere mockear: Location, ResourceEngine, ElementInstance, ResourceType, TimeTableEntry
- Cambios en movimiento afectan la gestión de disponibilidad
- Imposible testear funcionalidades de forma aislada

#### 🟡 **Violación Media: Open/Closed Principle (OCP)**

La clase contiene **5 clases internas de eventos** hardcodeadas:
- `CreateResourceEvent`
- `RoleOnEvent`
- `RoleOffEvent`
- `CancelPeriodOnEvent`
- `CancelPeriodOffEvent`
- `MoveEvent`
- `TransportEvent`

```java
// Código acoplado a implementaciones concretas:
public DiscreteEvent onCreate(final long ts) {
    // ...
    return new CreateResourceEvent(ts); // Hardcoded!
}

protected class RoleOnEvent extends DiscreteEvent {
    // Lógica compleja dentro de clase interna (67 líneas)
}
```

**Problema**: No se pueden extender o personalizar los eventos sin modificar la clase Resource.

#### 🟡 **Violación Media: Dependency Inversion Principle (DIP)**

La clase depende directamente de implementaciones concretas:

```java
// Dependencias concretas:
private ResourceEngine engine;              // Clase concreta
private Location currentLocation;           // Clase concreta (puede ser abstracta)
private ElementInstance movingInstance;     // Clase concreta
protected final ArrayList<TimeTableEntry>   // Implementación específica
```

**Debería depender de abstracciones** (interfaces) para facilitar testing y extensibilidad.

#### 🔴 **God Class Anti-Pattern**

Con 907 líneas y múltiples responsabilidades, Resource es una **God Class**:
- Gestiona su propio estado
- Gestiona ubicación y movimiento
- Gestiona eventos y ciclo de vida
- Actúa como coordinador de otros objetos

### Problemas de Testabilidad

1. **Inicialización compleja**: Requiere `SimulationEngine` para crear `ResourceEngine`
2. **Múltiples dependencias**: Location, ResourceType, ElementInstance, TimeTableEntry
3. **Lógica en clases internas**: 7 clases internas con lógica de negocio (>500 líneas combinadas)
4. **Estado mutable compartido**: `currentLocation`, `movingInstance`, `currentResourceType`
5. **Métodos protected**: Dificultan la verificación en tests (`catchResource`, `releaseResource`)

### Propuesta de Refactorización

```
Resource (Simplified)
├── ResourceState          // Estado básico
├── ResourceLifecycle      // onCreate, onDestroy
├── ResourceAvailability   // TimeTable, roles
├── ResourceLocation       // IMovable, Location
└── ResourceEventFactory   // Crea eventos (Strategy)
```

---

## 2. Clase `ElementInstance` (461 líneas, 38% cobertura)

### Métricas
- **Cobertura**: 38% (423 de 691 instrucciones sin cubrir)
- **Complejidad**: 61 métodos, gestión de árbol de instancias
- **Líneas de código**: 461 líneas

### Violaciones Identificadas

#### 🟡 **Violación Media: Single Responsibility Principle (SRP)**

La clase tiene **5 responsabilidades**:

1. **Gestión de jerarquía** (parent, descendants)
2. **Gestión de flujos** (currentFlow, initialFlow, lastFlow)
3. **Gestión de recursos** (executionWG, seizedResources)
4. **Gestión de estado** (token, arrivalOrder, remainingTask)
5. **Notificación de eventos** (arrivalTs, engine delegation)

```java
public class ElementInstance implements Prioritizable, Comparable<ElementInstance> {
    // Jerarquía
    protected final ElementInstance parent;
    protected final ArrayList<ElementInstance> descendants;
    
    // Flujos
    protected final IFlow initialFlow;
    protected IFlow currentFlow = null;
    protected IFlow lastFlow = null;
    
    // Recursos
    protected ActivityWorkGroup executionWG = null;
    protected int arrivalOrder;
    protected long arrivalTs = -1;
    
    // Estado
    protected WorkToken token;
    protected double remainingTask = 0.0;
    
    // Motor
    final private ElementInstanceEngine engine;
}
```

#### 🔴 **Violación Crítica: Composite Pattern mal implementado**

La gestión del árbol de instancias (parent/descendants) está **entrelazada con lógica de negocio**:

```java
private void removeDescendant(final ElementInstance wThread) {
    descendants.remove(wThread);
    if (parent == null && descendants.size() == 0)
        elem.notifyEnd(); // ¡Lógica de negocio mezclada!
}

public void notifyEnd() {
    if (parent != null) {
        parent.removeDescendant(this);
        if ((parent.descendants.size() == 0) && (parent.currentFlow != null))
            ((ITaskFlow)parent.currentFlow).finish(parent); // ¡Cast peligroso!
    }
}
```

**Problemas**:
- Difícil testear la jerarquía sin activar toda la lógica de finalización
- Acoplamiento con `Element`, `ITaskFlow`
- Lógica de coordinación distribuida entre métodos

#### 🟡 **Falta de encapsulación**

Muchos campos son `protected`, expuestos a subclases y mismo paquete:

```java
protected WorkToken token;
protected IFlow currentFlow = null;
protected ActivityWorkGroup executionWG = null;
protected int arrivalOrder;
protected double remainingTask = 0.0;
```

**Riesgo**: Cualquier clase del paquete puede modificar estado crítico.

### Problemas de Testabilidad

1. **Constructor privado**: Solo accesible via métodos estáticos `getInstance...`
2. **Jerarquía obligatoria**: Imposible crear instancia aislada sin parent
3. **Dependencia de Element**: Requiere `Element` con `ElementEngine` inicializado
4. **Estado mutable complejo**: `currentFlow`, `token`, `descendants` cambian durante ejecución
5. **Lógica en métodos de gestión**: `removeDescendant` ejecuta `notifyEnd` como efecto secundario

### Propuesta de Refactorización

```
ElementInstance (Simplified)
├── ElementInstanceState     // token, currentFlow, arrivalOrder
├── ElementInstanceHierarchy // parent, descendants (Composite puro)
├── ElementInstanceFlow      // Gestión de flujos
└── ElementInstanceResources // executionWG, arrivalTs
```

---

## 3. Clase `Element` (615 líneas, 53% cobertura)

### Métricas
- **Cobertura**: 53% (224 de 479 instrucciones sin cubrir)
- **Complejidad**: 44 métodos, coordinador de múltiples responsabilidades
- **Líneas de código**: 615 líneas

### Violaciones Identificadas

#### 🟡 **Violación Media: Single Responsibility Principle (SRP)**

La clase tiene **6 responsabilidades**:

1. **Gestión de tipo** (ElementType)
2. **Gestión de flujo** (IInitializerFlow, mainInstance)
3. **Gestión de recursos** (SeizedResourcesCollection)
4. **Gestión de ubicación** (IMovable, Location)
5. **Gestión de exclusividad** (exclusive flag)
6. **Gestión de variables** (VariableStoreSimulationObject)

```java
public class Element extends VariableStoreSimulationObject 
    implements Prioritizable, IEventSource, IMovable {
    
    // Tipo
    protected ElementType elementType;
    
    // Flujo
    protected final IInitializerFlow initialFlow;
    protected ElementInstance mainInstance = null;
    
    // Recursos
    final protected SeizedResourcesCollection seizedResources;
    
    // Ubicación
    private Location currentLocation;
    private ElementInstance movingInstance = null;
    
    // Exclusividad
    protected boolean exclusive = false;
    
    // Motor
    private ElementEngine engine;
}
```

#### 🟡 **SeizedResourcesCollection: Clase interna compleja**

La clase contiene una **clase interna de 337 líneas** (`SeizedResourcesCollection`) que:
- Gestiona TreeMap de recursos
- Tiene 12 métodos propios
- Mezcla lógica de colecciones con notificaciones de eventos

```java
protected final class SeizedResourcesCollection {
    // 337 líneas de código con lógica compleja
    private final TreeMap<Integer, ArrayDeque<Resource>> resources;
    
    public void addResources(int id, ArrayDeque<Resource> newRes) {
        // Lógica + notificaciones mezcladas
        simul.notifyInfo(...); // ¡Acceso directo a simulación!
    }
}
```

**Problema**: Imposible testear `SeizedResourcesCollection` de forma aislada.

#### 🟡 **Tight Coupling con Location**

Los métodos de movimiento están acoplados a implementaciones específicas:

```java
// En varios métodos:
public void notifyLocationAvailable(final Location location) {
    location.enter(this);
    final MoveFlow flow = (MoveFlow)movingInstance.getCurrentFlow(); // Cast!
    final Location destination = flow.getDestination();
    // ... más lógica de coordinación
}
```

**Problema**: Difícil testear movimiento sin infraestructura completa de Location.

### Problemas de Testabilidad

1. **4 constructores sobrecargados**: Complejidad en inicialización
2. **Dependencia de ElementEngine**: Requiere `SimulationEngine` para `assignSimulation`
3. **Clase interna acoplada**: `SeizedResourcesCollection` no es testeable aisladamente
4. **Estado mutable distribuido**: `exclusive`, `movingInstance`, `currentLocation`
5. **Métodos protected**: `seizeResources`, `releaseResources` dificultan verificación

### Propuesta de Refactorización

```
Element (Simplified)
├── ElementCore          // id, type, priority
├── ElementFlow          // initialFlow, mainInstance
├── ElementResources     // Usar SeizedResourcesManager (clase externa)
└── ElementMovement      // IMovable, Location (puede ser Strategy)
```

---

## 4. Síntesis de Problemas Comunes

### 🔴 **Problema 1: God Classes**

**Resource**, **Element** y **ElementInstance** son "God Classes":
- Múltiples responsabilidades (5-7 cada una)
- Cientos de líneas de código (461-907)
- Múltiples dependencias (5-10 clases)

**Consecuencia**: Baja testabilidad (17%-53% cobertura)

### 🔴 **Problema 2: Tight Coupling con Engine**

Todas las clases dependen de sus respectivos "Engine":
- `ResourceEngine`
- `ElementEngine`
- `ElementInstanceEngine`
- `SimulationEngine`

```java
// Patrón repetido:
@Override
protected void assignSimulation(SimulationEngine simul) {
    engine = simul.getXXXEngineInstance(this);
}
```

**Problema**: No se puede testear sin infraestructura completa de simulación.

### 🟡 **Problema 3: Clases Internas con Lógica**

- `Resource`: 7 clases internas de eventos (>500 líneas)
- `Element`: `SeizedResourcesCollection` (337 líneas)
- `Resource`: `TimeTableOrCancelEntriesAdder` (builder de 93 líneas)

**Problema**: Lógica de negocio oculta, difícil de testear y extender.

### 🟡 **Problema 4: Falta de Interfaces**

Las clases usan implementaciones concretas en lugar de interfaces:
- `ResourceEngine`, `ElementEngine` (clases concretas)
- `ArrayList<TimeTableEntry>` (implementación específica)
- `TreeMap<Integer, ArrayDeque<Resource>>` (implementación específica)

**Debería**: Usar `IResourceEngine`, `List<TimeTableEntry>`, `Map<Integer, Collection<Resource>>`

### 🟡 **Problema 5: Estado Mutable Compartido**

Múltiples flags booleanos y referencias compartidas:
- `Resource`: `timeOut`, `currentResourceType`
- `Element`: `exclusive`, `movingInstance`
- `ElementInstance`: `token`, `currentFlow`, `executionWG`

**Riesgo**: Condiciones de carrera, estados inconsistentes, difícil depuración.

---

## 5. Impacto en Cobertura

### Correlación Complejidad vs Cobertura

| Clase | Líneas | Responsabilidades | Cobertura | Instrucciones sin cubrir |
|-------|--------|------------------|-----------|-------------------------|
| Resource | 907 | 7 | **17%** | 455 |
| ElementInstance | 461 | 5 | **38%** | 423 |
| Element | 615 | 6 | **53%** | 224 |
| Simulation | ~600 | 8+ | **64%** | 325 |

**Observación**: A mayor número de responsabilidades, menor cobertura.

### Barreras para Testing

1. **Inicialización compleja**: Requiere SimulationEngine
2. **Múltiples mocks requeridos**: 5-10 dependencias por clase
3. **Lógica en clases internas**: No se puede testear aisladamente
4. **Estado mutable**: Difícil setup de precondiciones
5. **Métodos protected**: No accesibles desde tests externos

---

## 6. Recomendaciones Prioritarias

### 🔴 **Alta Prioridad**

1. **Extraer responsabilidades de Resource**
   - Crear `ResourceLocation` para IMovable
   - Crear `ResourceAvailability` para TimeTable/roles
   - Crear `ResourceEventFactory` para eventos
   - **Impacto esperado**: Resource 17% → 40%+

2. **Refactorizar ElementInstance**
   - Separar jerarquía (Composite puro) de lógica de negocio
   - Extraer `ElementInstanceFlow` y `ElementInstanceResources`
   - **Impacto esperado**: ElementInstance 38% → 60%+

3. **Introducir interfaces para Engines**
   - `IResourceEngine`, `IElementEngine`, `IElementInstanceEngine`
   - Permitir mocking sin SimulationEngine completo
   - **Impacto esperado**: +15-20% en todas las clases

### 🟡 **Media Prioridad**

4. **Extraer SeizedResourcesCollection como clase top-level**
   - Hacer testeable independientemente
   - Eliminar acceso directo a `simul` (Dependency Injection)
   - **Impacto esperado**: Element 53% → 65%+

5. **Aplicar Strategy Pattern para eventos de Resource**
   - Reemplazar clases internas por estrategias inyectables
   - **Beneficio**: Extensibilidad + testabilidad

6. **Reducir estado mutable**
   - Usar objetos inmutables para configuración
   - Separar estado de comportamiento
   - **Beneficio**: Tests más predecibles

### 🟢 **Baja Prioridad (Mejora Continua)**

7. **Documentar invariantes**
   - Documentar precondiciones y postcondiciones
   - Agregar assertions en código

8. **Introducir Value Objects**
   - `ResourceState`, `ElementState`, `InstanceState`
   - Encapsular validaciones

---

## 7. Conclusiones

### Estado Actual
- **3 God Classes** con 5-7 responsabilidades cada una
- **17-53% cobertura** en clases críticas
- **1,154 instrucciones sin cubrir** solo en estas 3 clases
- **Múltiples violaciones SOLID**: SRP (crítico), DIP (medio), OCP (medio)

### Riesgo
- **Alto**: Cambios en estas clases afectan múltiples funcionalidades
- **Medio**: Baja testabilidad impide detectar regresiones
- **Medio**: Difícil agregar nuevas funcionalidades

### Objetivo
Refactorizar estas 3 clases puede:
- **Aumentar cobertura global**: +10-15% (de 61% a 71-76%)
- **Reducir complejidad**: -40% líneas por clase
- **Mejorar mantenibilidad**: -60% acoplamiento

### Próximos Pasos
1. Crear issues para cada refactorización propuesta
2. Priorizar Resource (mayor impacto en cobertura)
3. Establecer tests de integración antes de refactorizar
4. Refactorizar incrementalmente (1-2 responsabilidades por iteración)

---

**Documento generado automáticamente el 9 de enero de 2026**
