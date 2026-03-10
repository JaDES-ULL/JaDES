# Workflow Patterns en JaDES

JaDES implementa el catálogo de [Workflow Patterns](http://www.workflowpatterns.com)
de van der Aalst & ter Hofstede (WFP).  
Todos los ejemplos usan la API actual del framework.

---

## WFP-01: Sequence (Secuencia)

Actividades en orden estricto: A → B → C

```java
Simulation sim = new Simulation(0, "WFP-01 Sequence", TimeUnit.MINUTE);

ElementType et = new ElementType(sim, "Element");
ResourceType rt = new ResourceType(sim, "Worker");

PeriodicCycle c = new PeriodicCycle(TimeUnit.MINUTE, 0, 100, 0);
new Resource(sim, "W-1")
    .newTimeTableOrCancelEntriesAdder(rt).withDuration(c, 100).addTimeTableEntry();

WorkGroup wg = new WorkGroup(sim, rt, 1);

ActivityFlow actA = new ActivityFlow(sim, "Activity A");
actA.newWorkGroupAdder(wg).withDelay(new ConstantFunction(5.0)).add();

ActivityFlow actB = new ActivityFlow(sim, "Activity B");
actB.newWorkGroupAdder(wg).withDelay(new ConstantFunction(3.0)).add();

ActivityFlow actC = new ActivityFlow(sim, "Activity C");
actC.newWorkGroupAdder(wg).withDelay(new ConstantFunction(4.0)).add();

// WFP-01: encadenamiento simple
actA.link(actB);
actB.link(actC);

new TimeDrivenElementGenerator(sim, new ConstantFunction(20.0), et, actA);
sim.registerListener(new StdInfoListener());
sim.run(0, 100);
// Tiempo total por elemento: A(5) + B(3) + C(4) = 12 min
```

---

## WFP-02: Parallel Split + WFP-03: Synchronization

Bifurcación paralela seguida de una barrera de sincronización.

```java
Simulation sim = new Simulation(0, "WFP-02/03 Parallel", TimeUnit.MINUTE);

ElementType et = new ElementType(sim, "Element");
ResourceType rt = new ResourceType(sim, "Worker");

PeriodicCycle c = new PeriodicCycle(TimeUnit.MINUTE, 0, 200, 0);
// 2 workers para que B y C puedan ejecutarse en paralelo
for (int i = 1; i <= 2; i++) {
    new Resource(sim, "W-" + i)
        .newTimeTableOrCancelEntriesAdder(rt).withDuration(c, 200).addTimeTableEntry();
}
WorkGroup wg = new WorkGroup(sim, rt, 1);

ActivityFlow actB = new ActivityFlow(sim, "Branch B");
actB.newWorkGroupAdder(wg).withDelay(new ConstantFunction(5.0)).add();

ActivityFlow actC = new ActivityFlow(sim, "Branch C");
actC.newWorkGroupAdder(wg).withDelay(new ConstantFunction(8.0)).add();

ActivityFlow actD = new ActivityFlow(sim, "After Sync");
actD.newWorkGroupAdder(wg).withDelay(new ConstantFunction(2.0)).add();

// WFP-02: ParallelFlow bifurca a B y C simultáneamente
ParallelFlow parallel = new ParallelFlow(sim);
parallel.link(actB);
parallel.link(actC);

// WFP-03: SynchronizationFlow espera a que AMBAS ramas terminen
SynchronizationFlow sync = new SynchronizationFlow(sim);
actB.link(sync);
actC.link(sync);
sync.link(actD);

new TimeDrivenElementGenerator(sim, new ConstantFunction(30.0), et, parallel);
sim.registerListener(new StdInfoListener());
sim.run(0, 200);
// Tiempo de paralelo: max(B=5, C=8) = 8 min; total = 8+2 = 10 min
```

> **Nota**: `ParallelFlow` sustituye al antiguo `ForkFlow` (eliminado).  
> `SynchronizationFlow` sustituye a `JoinFlow`.

---

## WFP-04: Exclusive Choice (Decisión exclusiva)

Exactamente una rama se activa según una condición.

```java
Simulation sim = new Simulation(0, "WFP-04 Exclusive Choice", TimeUnit.MINUTE);

ElementType urgentType   = new ElementType(sim, "Urgent");
ElementType regularType  = new ElementType(sim, "Regular");

ResourceType rt = new ResourceType(sim, "Doctor");
PeriodicCycle c = new PeriodicCycle(TimeUnit.MINUTE, 0, 480, 0);
new Resource(sim, "Dr. Smith")
    .newTimeTableOrCancelEntriesAdder(rt).withDuration(c, 480).addTimeTableEntry();
WorkGroup wg = new WorkGroup(sim, rt, 1);

ActivityFlow urgentService  = new ActivityFlow(sim, "Urgent Service");
urgentService.newWorkGroupAdder(wg).withDelay(new ConstantFunction(10.0)).add();

ActivityFlow regularService = new ActivityFlow(sim, "Regular Service");
regularService.newWorkGroupAdder(wg).withDelay(new ConstantFunction(20.0)).add();

// WFP-04: ExclusiveChoiceFlow selecciona la rama "urgente" si se cumple la condición
ExclusiveChoiceFlow choice = new ExclusiveChoiceFlow(sim,
    ei -> ei.getElement().getType() == urgentType,   // condición
    urgentService,    // rama "verdadero"
    regularService);  // rama "falso"

new TimeDrivenElementGenerator(sim, new ConstantFunction(15.0), urgentType,  choice);
new TimeDrivenElementGenerator(sim, new ConstantFunction(12.0), regularType, choice);

sim.registerListener(new StdInfoListener());
sim.run(0, 480);
```

---

## WFP-05: Simple Merge (Convergencia sin espera)

Varias ramas convergen en un punto común sin sincronización.

```java
Simulation sim = new Simulation(0, "WFP-05 Simple Merge", TimeUnit.MINUTE);

ElementType typeA = new ElementType(sim, "Type-A");
ElementType typeB = new ElementType(sim, "Type-B");

ResourceType rt = new ResourceType(sim, "Processor");
PeriodicCycle c = new PeriodicCycle(TimeUnit.MINUTE, 0, 200, 0);
new Resource(sim, "CPU-1")
    .newTimeTableOrCancelEntriesAdder(rt).withDuration(c, 200).addTimeTableEntry();
WorkGroup wg = new WorkGroup(sim, rt, 1);

ActivityFlow pathA = new ActivityFlow(sim, "Path A");
pathA.newWorkGroupAdder(wg).withDelay(new ConstantFunction(3.0)).add();

ActivityFlow pathB = new ActivityFlow(sim, "Path B");
pathB.newWorkGroupAdder(wg).withDelay(new ConstantFunction(7.0)).add();

// WFP-05: cualquier rama puede continuar independientemente
ActivityFlow shared = new ActivityFlow(sim, "Shared Step");
shared.newWorkGroupAdder(wg).withDelay(new ConstantFunction(2.0)).add();

pathA.link(shared);
pathB.link(shared);

new TimeDrivenElementGenerator(sim, new ConstantFunction(20.0), typeA, pathA);
new TimeDrivenElementGenerator(sim, new ConstantFunction(25.0), typeB, pathB);

sim.registerListener(new StdInfoListener());
sim.run(0, 200);
```

---

## WFP-21: Structured Loop (Bucle estructurado)

Un bloque se repite hasta que se cumple una condición de salida.

```java
Simulation sim = new Simulation(0, "WFP-21 Do-While", TimeUnit.MINUTE);

ElementType et = new ElementType(sim, "Patient");
ResourceType rt = new ResourceType(sim, "Nurse");
PeriodicCycle c = new PeriodicCycle(TimeUnit.MINUTE, 0, 1440, 0);
new Resource(sim, "Nurse-1")
    .newTimeTableOrCancelEntriesAdder(rt).withDuration(c, 1440).addTimeTableEntry();
WorkGroup wg = new WorkGroup(sim, rt, 1);

// Tratamiento que puede repetirse varias veces
ActivityFlow treatment = new ActivityFlow(sim, "Treatment Round");
treatment.newWorkGroupAdder(wg).withDelay(new ConstantFunction(60.0)).add();

// Evaluación: el paciente se da de alta con p=0.40 tras cada ronda
ActivityFlow evaluation = new ActivityFlow(sim, "Evaluation");
evaluation.newWorkGroupAdder(wg).withDelay(new ConstantFunction(10.0)).add();

// WFP-21: DoWhileFlow repite { treatment → evaluation } mientras condición = true
DoWhileFlow loop = new DoWhileFlow(sim,
    ei -> Math.random() > 0.40);  // continuar si random > 0.40 (~media 2.5 rondas)

loop.link(treatment);
treatment.link(evaluation);
evaluation.link(loop);  // cierre del bucle

new TimeDrivenElementGenerator(sim, new ConstantFunction(120.0), et, loop);
sim.registerListener(new StdInfoListener());
sim.run(0, 1440);
```

---

## Tabla resumen

| Patrón WFP | Descripción | Clase JaDES | Notas |
|-----------|-------------|-------------|-------|
| WFP-01 | Sequence | `flow.link(next)` | Encadenamiento directo |
| WFP-02 | Parallel Split | `ParallelFlow` | Sustituye a `ForkFlow` |
| WFP-03 | Synchronization | `SynchronizationFlow` | Sustituye a `JoinFlow` |
| WFP-04 | Exclusive Choice | `ExclusiveChoiceFlow` | Condición lambda |
| WFP-05 | Simple Merge | `flow.link(shared)` desde varias ramas | Sin barrera |
| WFP-21 | Structured Loop | `DoWhileFlow` | Condición de salida |

---

## Casos de estudio completos

Los patrones anteriores se combinan en escenarios reales en el módulo `jades-examples`:

- **Urgencias** → WFP-04 (`ExclusiveChoiceFlow` para triaje)  
- **UCI** → WFP-02 + WFP-03 + WFP-21 (terapias paralelas con alta condicionada)

Consulta [`jades-examples/README.md`](../../jades-examples/README.md) para los modelos completos
con métricas formales y verificación.

---

## Próximos pasos

- [Simulación básica](basic-simulation.md) — servidor único paso a paso
- [Getting Started](../guides/getting-started.md) — instalación y primeros pasos
- [Arquitectura](../architecture/overview.md) — visión global del framework
