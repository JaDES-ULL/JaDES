# Getting Started with JaDES

## 📋 Requisitos previos

- **Java**: 17 o superior (LTS recomendado)
- **Maven**: 3.8 o superior
- **IDE**: IntelliJ IDEA, Eclipse, o VS Code (opcional)

## 🚀 Instalación

### Opción 1: Dependencia Maven (instalación local)

Clona el repositorio e instálalo en tu repositorio local Maven:

```bash
git clone https://github.com/JaDES-ULL/JaDES.git
cd JaDES
mvn clean install -DskipTests
```

Luego añade la dependencia en tu `pom.xml`:

```xml
<dependency>
    <groupId>io.github.jades-ull.jades</groupId>
    <artifactId>jades-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

Si necesitas distribuciones de probabilidad integradas, añade también:

```xml
<dependency>
    <groupId>io.github.jades-ull.jades</groupId>
    <artifactId>jades-random</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Opción 2: Build desde el código fuente (todo en uno)

```bash
# Clona el repositorio
git clone https://github.com/JaDES-ULL/JaDES.git
cd JaDES

# Compila e instala localmente (con tests)
mvn clean install

# Sin tests si solo quieres instalarlo rápido
mvn clean install -DskipTests
```

## 📝 Tu primera simulación

### Ejemplo: cola de servicio simple

La unidad de trabajo en JaDES es el `ActivityFlow`, que encapsula la solicitud de
recursos, el tiempo de servicio y la liberación en un solo objeto configurador.

```java
import es.ull.simulation.model.*;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.TimeDrivenElementGenerator;
import es.ull.simulation.inforeceiver.StdInfoListener;
import es.ull.simulation.functions.ConstantFunction;

public class SimpleQueueExample {
    public static void main(String[] args) {
        // 1. Crear la simulación (unidad de tiempo: minutos)
        Simulation sim = new Simulation(0, "Queue Example", TimeUnit.MINUTE);

        // 2. Definir el tipo de recurso (p.ej., "Servidor")
        ResourceType serverType = new ResourceType(sim, "Server Type");

        // 3. Crear el recurso y su horario de disponibilidad
        //    Cycle de 100 min, disponible 100 min desde el inicio
        PeriodicCycle cycle = new PeriodicCycle(TimeUnit.MINUTE, 0, 100, 0);
        new Resource(sim, "Server 1")
            .newTimeTableOrCancelEntriesAdder(serverType)
            .withDuration(cycle, 100)
            .addTimeTableEntry();

        // 4. Definir el tipo de elemento (p.ej., "Cliente")
        ElementType customerType = new ElementType(sim, "Customer");

        // 5. Crear la actividad con su grupo de trabajo
        //    ActivityFlow gestiona solicitud + servicio + liberación
        ActivityFlow service = new ActivityFlow(sim, "Service");
        WorkGroup wg = new WorkGroup(sim, serverType, 1); // 1 servidor
        service.newWorkGroupAdder(wg)
               .withDelay(new ConstantFunction(5.0))   // 5 minutos
               .add();

        // 6. Generar un cliente cada 10 minutos durante 100 minutos
        new TimeDrivenElementGenerator(sim, new ConstantFunction(10.0),
            customerType, service);

        // 7. Registrar un listener para ver los eventos
        sim.registerListener(new StdInfoListener());

        // 8. Ejecutar la simulación (t = 0 a 100 minutos)
        sim.run(0, 100);

        System.out.println("Simulación completada.");
    }
}
```

## 🔍 Conceptos clave

### 1. Objeto Simulation
```java
// Con unidad de tiempo explícita
Simulation sim = new Simulation(int id, String description, TimeUnit unit);

// Con unidad de tiempo por defecto (minutos)
Simulation sim = new Simulation(int id, String description);
```
- **id**: Identificador único (normalmente 0)
- **description**: Nombre descriptivo
- **unit**: `TimeUnit.SECOND`, `MINUTE`, `HOUR`, `DAY`, …

La simulación se ejecuta con:
```java
sim.run(double startTime, double endTime);
```

### 2. Recursos
```java
ResourceType rt = new ResourceType(sim, "Doctor");
new Resource(sim, "Dr. Smith")
    .newTimeTableOrCancelEntriesAdder(rt)
    .withDuration(cycle, durationInTimeUnits)
    .addTimeTableEntry();
```
- `ResourceType`: categoría (p.ej., "Médico")
- `Resource`: instancia concreta (p.ej., "Dr. Smith")
- El horario de disponibilidad se define con `PeriodicCycle` + `addTimeTableEntry()`

### 3. Elementos
```java
ElementType et = new ElementType(sim, "Patient");
```
- Entidades que circulan por la simulación
- Pueden ser: clientes, pacientes, trabajos, pedidos, etc.

### 4. ActivityFlow — unidad de trabajo central
```java
ActivityFlow act = new ActivityFlow(sim, "Consultation");
WorkGroup wg = new WorkGroup(sim, doctorType, 1);
act.newWorkGroupAdder(wg)
   .withDelay(new ConstantFunction(15.0))  // 15 min de servicio
   .add();
act.link(nextFlow);  // encadenar con la siguiente actividad
```
Un `ActivityFlow` realiza internamente:
1. Solicitar los recursos del `WorkGroup`
2. Esperar el tiempo de servicio
3. Liberar los recursos

### 5. Generadores de elementos
```java
// Llegadas cada 8 minutos (constante)
new TimeDrivenElementGenerator(sim, new ConstantFunction(8.0), et, firstFlow);

// Llegadas exponenciales (media 10 min)
new TimeDrivenElementGenerator(sim, new ExponentialFunction(10.0), et, firstFlow);
```

### 6. Listeners / observadores
```java
sim.registerListener(new StdInfoListener());  // salida por consola
```
Reciben notificaciones de eventos durante la simulación.

## 🔀 Patrones de flujo de trabajo

JaDES implementa los Workflow Patterns de van der Aalst:

| Patrón | Clase JaDES | Descripción |
|--------|-------------|-------------|
| WFP-01 Secuencia | `flow1.link(flow2)` | Ejecución en orden |
| WFP-02 Parallel Split | `ParallelFlow` | Bifurcación paralela |
| WFP-03 Synchronization | `SynchronizationFlow` | Espera a todas las ramas |
| WFP-04 Exclusive Choice | `ExclusiveChoiceFlow` | Rama única por condición |
| WFP-05 Simple Merge | `SimpleMergeFlow` | Convergencia sin espera |
| WFP-21 Structured Loop | `DoWhileFlow` | Bucle con condición de salida |

Consulta [docs/examples/workflow-patterns.md](../examples/workflow-patterns.md) para código completo.

## 🏥 Ejemplos de referencia

El módulo `jades-examples` contiene dos simulaciones sanitarias completas y verificadas:

- **Urgencias** (`EmergencyDeptModel`): llegadas Poisson + `ExclusiveChoiceFlow`
- **UCI** (`ICUModel`): `ParallelFlow` + `SynchronizationFlow` + `DoWhileFlow` + multi-réplica

```bash
# Ejecutar ejemplos
mvn exec:java -pl jades-examples \
  -Dexec.mainClass="es.ull.simulation.examples.healthcare.emergencydept.EmergencyDeptSimulation"
```

Consulta [`jades-examples/README.md`](../../jades-examples/README.md) para la documentación completa.

## 🧪 Ejecutar los tests

```bash
# Todos los tests
mvn test

# Test específico
mvn test -Dtest=NombreDeTest

# Con cobertura (reporte en target/site/jacoco/index.html)
mvn verify
```

## 📊 Visualizar resultados

### Listeners incluidos
- `StdInfoListener`: imprime eventos por consola
- `ProgressListener`: barra de progreso
- `CpuTimeView`: tiempo de CPU utilizado

### Listener personalizado
```java
public class MyListener extends BasicListener {
    @Override
    public void infoEmitted(SimulationInfo info) {
        if (info instanceof ElementInfo ei) {
            System.out.println("Elemento " + ei.getElement().getIdentifier()
                + " evento: " + ei.getType());
        }
    }
}

sim.registerListener(new MyListener());
```

## 🎯 Próximos pasos

1. Explorar los [Ejemplos](../examples/)
2. Leer el [Resumen de arquitectura](../architecture/overview.md)
3. Estudiar los [Workflow Patterns](../examples/workflow-patterns.md)
4. Consultar los ejemplos sanitarios en [`jades-examples/README.md`](../../jades-examples/README.md)

## ❓ Problemas frecuentes

### Error de versión de Java
```
error: release version 17 not supported
```
**Solución**: Instala Java 17+ o actualiza `JAVA_HOME`

### Build falla
```bash
mvn clean install -U
```

### Los tests fallan
```bash
# Omite tests temporalmente para instalar
mvn install -DskipTests
# Luego investiga el fallo
```

## 💬 Obtener ayuda

- [GitHub Issues](https://github.com/JaDES-ULL/JaDES/issues)
- [Discussions](https://github.com/JaDES-ULL/JaDES/discussions)
- Consulta [CONTRIBUTING.md](../../CONTRIBUTING.md)
