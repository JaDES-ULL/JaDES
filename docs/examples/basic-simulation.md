# Ejemplo: Simulación básica de servidor

## Escenario

Un servidor único atiende a clientes que llegan a intervalos regulares.  
Cada servicio tarda **5 minutos** y los clientes llegan cada **10 minutos**.

La simulación corre de $t = 0$ a $t = 100$ min, lo que produce **10 clientes**.  
Al ser $\rho = 5/10 = 0.5$, el servidor nunca queda saturado.

---

## Código completo

```java
package examples;

import es.ull.simulation.model.*;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.TimeDrivenElementGenerator;
import es.ull.simulation.inforeceiver.StdInfoListener;
import es.ull.simulation.functions.ConstantFunction;

/**
 * Simulación básica JaDES — un servidor, llegadas periódicas.
 *
 * Demuestra:
 *  - Construcción de Simulation con TimeUnit
 *  - Recurso con horario (PeriodicCycle)
 *  - ActivityFlow: solicitud + servicio + liberación en un solo objeto
 *  - TimeDrivenElementGenerator para llegadas
 *  - sim.registerListener() para observar eventos
 *  - sim.run(start, end) para acotar el horizonte
 */
public class BasicServerSimulation {

    public static void main(String[] args) {

        // ── 1. Simulación ────────────────────────────────────────────────
        Simulation sim = new Simulation(0, "Basic Server", TimeUnit.MINUTE);

        // ── 2. Tipo de recurso e instancia con horario ───────────────────
        ResourceType serverType = new ResourceType(sim, "Server Type");

        // El servidor está disponible desde t=0 durante 100 min
        PeriodicCycle available = new PeriodicCycle(TimeUnit.MINUTE, 0, 100, 0);
        new Resource(sim, "Server-1")
            .newTimeTableOrCancelEntriesAdder(serverType)
            .withDuration(available, 100)
            .addTimeTableEntry();

        // ── 3. Tipo de elemento ──────────────────────────────────────────
        ElementType customerType = new ElementType(sim, "Customer");

        // ── 4. Actividad de servicio (ActivityFlow encapsula
        //       solicitud + delay + liberación) ──────────────────────────
        ActivityFlow service = new ActivityFlow(sim, "Service");
        WorkGroup wg = new WorkGroup(sim, serverType, 1);   // 1 servidor necesario
        service.newWorkGroupAdder(wg)
               .withDelay(new ConstantFunction(5.0))        // 5 min de servicio
               .add();

        // ── 5. Generador: 1 cliente cada 10 min ─────────────────────────
        new TimeDrivenElementGenerator(sim, new ConstantFunction(10.0),
            customerType, service);

        // ── 6. Listener de consola ───────────────────────────────────────
        sim.registerListener(new StdInfoListener());

        // ── 7. Ejecutar t=[0, 100] ───────────────────────────────────────
        sim.run(0, 100);
    }
}
```

---

## Salida esperada (extracto)

```
[0.0] Customer[0] created
[0.0] Customer[0] requests Server-1
[0.0] Customer[0] seizes  Server-1
[5.0] Customer[0] releases Server-1
[5.0] Customer[0] finished
[10.0] Customer[1] created
[10.0] Customer[1] seizes  Server-1
[15.0] Customer[1] releases Server-1
...
[90.0] Customer[9] seizes  Server-1
[95.0] Customer[9] releases Server-1
[95.0] Customer[9] finished
```

---

## Conceptos clave

### 1. `ActivityFlow` — unidad de trabajo central

En versiones previas de JaDES se usaban `RequestResourcesFlow`, `DelayFlow` y
`ReleaseResourcesFlow` por separado, gestionados por un `ActivityManager`.
La API actual los unifica en **`ActivityFlow`**:

```java
ActivityFlow act = new ActivityFlow(sim, "Nombre");
WorkGroup wg = new WorkGroup(sim, resourceType, cantidad);
act.newWorkGroupAdder(wg)
   .withDelay(timeFunction)
   .add();
```

### 2. Horario de recursos

Los recursos exponen su disponibilidad mediante una tabla de tiempos
(`TimeTableEntry`) asociada a un `PeriodicCycle`:

```java
new Resource(sim, "Nombre")
    .newTimeTableOrCancelEntriesAdder(resourceType)
    .withDuration(cycle, duracionEnUnidades)
    .addTimeTableEntry();
```

### 3. Registro de listeners

```java
sim.registerListener(listener);   // ✅ API actual
// sim.addInfoReceiver(listener);  // ❌ API deprecada
```

### 4. Acotación temporal

```java
sim.run(double start, double end);
```

Los tiempos se expresan en la `TimeUnit` indicada al construir la `Simulation`.

---

## Variaciones

### Servidores múltiples

```java
for (int i = 1; i <= 3; i++) {
    new Resource(sim, "Server-" + i)
        .newTimeTableOrCancelEntriesAdder(serverType)
        .withDuration(available, 100)
        .addTimeTableEntry();
}
// Con 3 servidores ρ = 5/(10·3) ≈ 0.17 → sin cola prácticamente
```

### Tiempo de servicio aleatorio

```java
// Servicio exponencial con media 5 min
import simkit.random.ExponentialVariate;
// Usar RandomFunction de jades-random
service.newWorkGroupAdder(wg)
       .withDelay(new ExponentialFunction(5.0))
       .add();
```

### Llegadas aleatorias (proceso de Poisson)

```java
// Interarrival exponencial con media 10 min → λ = 6 clientes/hora
new TimeDrivenElementGenerator(sim, new ExponentialFunction(10.0),
    customerType, service);
```

---

## Próximos pasos

- [Workflow Patterns](workflow-patterns.md) — bifurcación, sincronización, bucles
- [Ejemplos sanitarios](../../jades-examples/README.md) — casos de estudio completos
- [Arquitectura](../architecture/overview.md) — visión global del framework
