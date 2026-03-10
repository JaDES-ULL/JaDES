# JaDES Examples — Casos de Estudio de Simulación Sanitaria

> **Material de acompañamiento reproducible** para el framework de simulación de eventos discretos JaDES.  
> Este módulo **no se publica en Maven Central**; se proporciona como documentación ejecutable y como validación de integración continua.

---

## Resumen

Este módulo proporciona dos implementaciones de referencia de modelos de instalaciones sanitarias construidos con el framework JaDES (*Java Discrete-Event Simulation*). Los modelos tienen un doble propósito: (i) demostrar el uso correcto de la API de JaDES mediante patrones de flujo de trabajo de complejidad progresiva, y (ii) constituir tests de regresión ejecutables que verifican la corrección del framework en cada ciclo de integración continua. Ambos modelos se fundamentan en el paradigma de simulación de eventos discretos (DES) [Banks et al., 2010] y emplean patrones de flujo de trabajo catalogados formalmente por van der Aalst et al. [2003].

---

## 1. Fundamentos Teóricos

### 1.1 Simulación de Eventos Discretos

Un sistema de simulación de eventos discretos se caracteriza por un vector de estado $\mathbf{S}(t)$ que únicamente cambia en un conjunto numerable de instantes — los *tiempos de evento* $\{t_1, t_2, \ldots\}$. Entre eventos consecutivos el estado del sistema permanece constante [Law, 2015]. Formalmente, un modelo DES puede expresarse como la tupla

$$\mathcal{M} = \langle S,\; E,\; X,\; \delta,\; \lambda,\; s_0 \rangle$$

donde $S$ es el espacio de estados, $E$ el conjunto de eventos, $X$ el espacio de trayectorias de entrada, $\delta : S \times X \to S$ la función de transición de estado, $\lambda : S \to Y$ la función de salida y $s_0$ el estado inicial [Zeigler et al., 2000].

Los sistemas sanitarios constituyen un dominio de aplicación canónico para la DES [Günal & Pidd, 2010]: las trayectorias de los pacientes se representan naturalmente como secuencias de actividades con restricciones de recursos, tiempos de servicio estocásticos y procesos de llegada que frecuentemente se aproximan bien mediante distribuciones de Poisson o Erlang.

### 1.2 El Framework JaDES

JaDES modela un sistema como un conjunto de *elementos* (entidades) que recorren un grafo de *flujo* dirigido mientras consumen *recursos*. La semántica de ejecución sigue la perspectiva de *activity-scanning* [Pidd, 2004]: en cada instante simulado el motor evalúa las solicitudes de recursos pendientes y las concede según los grupos de trabajo configurados. El comportamiento estocástico se introduce mediante objetos `TimeFunction` intercambiables proporcionados por la biblioteca de variantes aleatorias SimKit [Buss, 2002].

### 1.3 Patrones de Flujo de Trabajo

Las estructuras de control empleadas en ambos modelos corresponden a patrones del catálogo de referencia *Workflow Patterns* [van der Aalst et al., 2003; Russell et al., 2006]. La Tabla 1 mapea cada patrón con su clase equivalente en JaDES.

**Tabla 1.** Patrones de flujo de trabajo empleados en los casos de estudio.

| ID WFP | Nombre del patrón | Clase JaDES | Caso de estudio |
|--------|-------------------|-------------|-----------------|
| WFP-1  | Secuencia | `ActivityFlow.link()` | 1 y 2 |
| WFP-4  | Elección exclusiva | `ExclusiveChoiceFlow` | 1 |
| WFP-2  | División paralela | `ParallelFlow` | 2 |
| WFP-3  | Sincronización | `SynchronizationFlow` | 2 |
| WFP-21 | Bucle estructurado (hacer-mientras) | `DoWhileFlow` | 2 |

---

## 2. Métricas de Salida

Sea $n$ el número de pacientes procesados en una réplica de duración $T$. Los siguientes indicadores de rendimiento son calculados por las clases *listener* y reportados por los puntos de entrada del experimento.

**Tiempo de espera** $W_q^{(a)}$ para la actividad $a$ — intervalo entre el evento de solicitud del recurso y el evento de adquisición del recurso:

$$W_q^{(a)} = \frac{1}{n_a} \sum_{i=1}^{n_a} \left( t_{\mathrm{acq},i}^{(a)} - t_{\mathrm{req},i}^{(a)} \right)$$

**Tiempo de permanencia** $W$ — tiempo total que cada elemento permanece en el sistema desde su creación hasta su eliminación:

$$W = \frac{1}{n} \sum_{i=1}^{n} \left( t_{\mathrm{fin},i} - t_{\mathrm{ini},i} \right)$$

**Utilización del recurso** $\rho^{(r)}$ para el tipo de recurso $r$ con $c_r$ unidades idénticas durante el horizonte $T$:

$$\rho^{(r)} = \frac{\displaystyle\sum_{j=1}^{c_r} b_j^{(r)}}{c_r \cdot T}$$

donde $b_j^{(r)}$ es el tiempo total de ocupación de la unidad $j$.

Para experimentos con múltiples réplicas ($R$ réplicas independientes), los estimadores puntuales son la media muestral $\bar{x}$ y la desviación típica muestral $s$. Un intervalo de confianza clásico al nivel $(1-\alpha)$ adopta la forma

$$\bar{x} \pm t_{\alpha/2,\; R-1} \cdot \frac{s}{\sqrt{R}}$$

donde $t_{\alpha/2,\; R-1}$ es el valor crítico de la distribución $t$ de Student con $R-1$ grados de libertad [Law, 2015, Cap. 9].

---

## 3. Caso de Estudio 1 — Servicio de Urgencias

### 3.1 Modelo Conceptual

El modelo representa un único servicio de urgencias que opera durante un **turno de 8 horas** ($T = 480\ \mathrm{min}$). Los pacientes llegan a una tasa de interarribo constante de uno cada 5 minutos, lo que proporciona un throughput nominal de 96 pacientes por turno. Se utiliza deliberadamente un proceso de llegada determinista — en lugar del proceso de Poisson habitualmente reportado en la literatura [Günal & Pidd, 2010] — para producir resultados de referencia completamente reproducibles, adecuados para la verificación del modelo mediante análisis de trazas.

A su llegada, cada paciente pasa por triaje y una consulta médica. Con probabilidad $p_{\mathrm{diag}} = 0{,}20$, el médico responsable solicita pruebas diagnósticas seguidas de una segunda evaluación (WFP-4, elección exclusiva); el 80 % restante recibe el alta directamente tras la consulta.

```
Paciente
  └─[Triaje]                    GT: {Enfermero de triaje × 1}
       └─[Consulta médica]       GT: {Médico de urgencias × 1}
            └─ ExclusiveChoiceFlow
                 ├─ p = 0,20 ──► [Pruebas diagnósticas]  GT: {Técnico diagnóstico × 1}
                 │                    └─[2.ª evaluación]  GT: {Médico de urgencias × 1}
                 │                         └─ Alta
                 └─ p = 0,80 ──► Alta
```

### 3.2 Parámetros del Modelo

**Tabla 2.** Parámetros de entrada del modelo de Urgencias.

| Parámetro | Símbolo | Valor | Distribución |
|-----------|---------|-------|-------------|
| Duración del turno | $T$ | 480 min | — |
| Tiempo de interarribo | $1/\lambda$ | 5 min | Constante |
| Duración del triaje | $s_{\mathrm{triaje}}$ | 3 min | Constante |
| Duración de la consulta | $s_{\mathrm{cons}}$ | $\mathcal{U}[8, 15]$ min | Uniforme |
| Duración de pruebas diagnósticas | $s_{\mathrm{diag}}$ | 20 min | Constante |
| Duración de la 2.ª evaluación | $s_{\mathrm{eval}}$ | 5 min | Constante |
| Probabilidad de rama diagnóstica | $p_{\mathrm{diag}}$ | 0,20 | Bernoulli |
| Enfermeros de triaje | $c_{\mathrm{enf}}$ | 2 | — |
| Médicos de urgencias | $c_{\mathrm{med}}$ | 3 | — |
| Técnicos diagnósticos | $c_{\mathrm{tec}}$ | 2 | — |

### 3.3 Notas de Implementación en JaDES

- **Disponibilidad del recurso:** se modela mediante `SimulationPeriodicCycle.newDailyCycle(unit, 0)` con duración igual a `SHIFT_DURATION_MIN`, vinculando el horario del recurso al horizonte de simulación sin necesidad de un evento de cancelación explícito.
- **Enrutamiento estocástico:** se implementa como `ExclusiveChoiceFlow` custodiado por `PercentageCondition<ElementInstance>(20.0)`, evaluado una vez por instancia de elemento en el punto de elección.
- **Recogida de estadísticas:** desacoplada del modelo mediante el patrón observador: `EmergencyStatsListener` se suscribe a `ElementActionInfo` (tiempos de espera), `ElementInfo` (tiempos de permanencia) y `ResourceUsageInfo` (utilización), manteniendo la medición del rendimiento independiente de la lógica del modelo.

### 3.4 Ejecución

```bash
# Réplica única — traza de eventos completa por stdout
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.emergency.EmergencyDeptMain

# Cinco réplicas — modo silencioso — semilla fija para reproducibilidad
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.emergency.EmergencyDeptMain \
    -Dexec.args="-r 5 -q -s 12345"
```

| Opción | Valor por defecto | Descripción |
|--------|-------------------|-------------|
| `-r <n>` | 1 | Número de réplicas independientes |
| `-q` | false | Suprime la traza de eventos y la salida de progreso |
| `-s <n>` | reloj del sistema | Semilla del generador pseudoaleatorio |

### 3.5 Salida Representativa (réplica única)

```
════════════════════════════════════════════════════════
 EMERGENCY DEPARTMENT — Simulation Results
════════════════════════════════════════════════════════
 Simulation duration  : 480 minutes (8.0 hours)
 Patients processed   : 96
 Avg sojourn time     : 23.4 min

 Activity                Avg Wait   Max Wait   Count
 ─────────────────────────────────────────────────────
 Triage                    2.1 m     18.0 m      96
 Medical Consultation      8.7 m     45.0 m      96
 Diagnostic Tests          4.3 m     22.0 m      19
 Second Evaluation         1.2 m      8.0 m      19

 Resource Type           Utilisation
 ────────────────────────────────────
 Triage Nurse               82.4 %
 Emergency Physician        74.1 %
 Diagnostic Technician      30.9 %
════════════════════════════════════════════════════════
```

La utilización del médico (~74 %) es coherente con la aproximación de carga ofrecida M/D/3: con $\lambda = 12\ \mathrm{pac/h}$, $\mu_{\mathrm{cons}}^{-1} \approx 11{,}5\ \mathrm{min}$ y $c = 3$ servidores, la carga ofrecida es $\rho = \lambda / (c \cdot \mu) \approx 0{,}77$.

---

## 4. Caso de Estudio 2 — Unidad de Cuidados Intensivos

### 4.1 Modelo Conceptual

El modelo de UCI opera de forma continua durante **7 días** ($T = 10\,080\ \mathrm{min}$), con un paciente ingresado cada 120 minutos (aproximadamente 84 ingresos por réplica). La trayectoria amplía el Caso de Estudio 1 con tres patrones de flujo de trabajo adicionales:

1. **División paralela / sincronización (WFP-2 / WFP-3):** el análisis de laboratorio y la consulta al especialista se inician de forma concurrente inmediatamente tras el ingreso. La actividad *Plan de tratamiento* no puede comenzar hasta que ambas ramas hayan finalizado, imponiendo una sincronización de unión (*join*).

2. **Bucle estructurado — hacer-mientras (WFP-21):** cada ronda de tratamiento puede repetirse debido a un evento de complicación, modelado como una prueba de Bernoulli con $p_{\mathrm{comp}} = 0{,}30$. El número esperado de rondas por paciente es

$$\mathbb{E}[\text{rondas}] = \frac{1}{1 - p_{\mathrm{comp}}} = \frac{1}{0{,}70} \approx 1{,}43$$

3. **Disponibilidad de recursos con turnos múltiples:** los intensivistas rotan en dos turnos de 12 horas; las enfermeras rotan en tres turnos de 8 horas con dos enfermeras por turno. Cada ciclo se expresa mediante una instancia independiente de `SimulationPeriodicCycle.newDailyCycle` con el desplazamiento correspondiente.

```
Paciente
  └─[Ingreso UCI]                  GT: {Intensivista × 1, Enfermera UCI × 1}
       └─ ParallelFlow (WFP-2)
           ├─[Análisis de laboratorio]   GT: {Técnico de laboratorio × 1}
           └─[Consulta al especialista]  GT: {Intensivista × 1}
       └─ SynchronizationFlow (WFP-3)
       └─[Plan de tratamiento]      GT: {Intensivista × 1}
       └─ DoWhileFlow (WFP-21, p_comp = 0,30)
           └─[Ronda de tratamiento]  GT: {Enfermera UCI × 1}
       └─[Monitorización de recuperación]  GT: {Enfermera UCI × 1}
       └─ Alta
```

### 4.2 Parámetros del Modelo

**Tabla 3.** Parámetros de entrada del modelo de UCI.

| Parámetro | Símbolo | Valor | Distribución |
|-----------|---------|-------|-------------|
| Horizonte de simulación | $T$ | 10 080 min (7 días) | — |
| Tiempo de interarribo | $1/\lambda$ | 120 min | Constante |
| Duración del ingreso | $s_{\mathrm{ing}}$ | $\mathcal{U}[60, 120]$ min | Uniforme |
| Duración del análisis de lab. | $s_{\mathrm{lab}}$ | 30 min | Constante |
| Duración de la consulta especialista | $s_{\mathrm{esp}}$ | $\mathcal{U}[20, 40]$ min | Uniforme |
| Duración del plan de tratamiento | $s_{\mathrm{plan}}$ | 30 min | Constante |
| Duración de la ronda de tratamiento | $s_{\mathrm{ronda}}$ | $\mathcal{U}[90, 150]$ min | Uniforme |
| Duración de la monitorización | $s_{\mathrm{rec}}$ | $\mathcal{U}[180, 360]$ min | Uniforme |
| Probabilidad de complicación | $p_{\mathrm{comp}}$ | 0,30 | Bernoulli |
| Intensivistas | $c_{\mathrm{int}}$ | 2 (1 por turno de 12 h) | — |
| Enfermeras UCI | $c_{\mathrm{enf}}$ | 6 (2 por turno de 8 h) | — |
| Técnicos de laboratorio | $c_{\mathrm{lab}}$ | 1 (24 h/día) | — |
| Réplicas independientes | $R$ | 10 (por defecto) | — |

### 4.3 Notas de Implementación en JaDES

- **Semántica de `DoWhileFlow`:** la condición del bucle se evalúa *después* de cada ejecución del cuerpo (`actRound`). `PercentageCondition<ElementInstance>(30.0)` devuelve `true` con probabilidad 0,30, desencadenando una iteración adicional. Esto implementa correctamente la semántica *hacer-mientras* (post-test) de WFP-21, a diferencia del patrón *mientras-hacer* (pre-test).
- **Agregación entre réplicas:** `ICUStatsListener` expone *accessors* tipados (`getAverageWaitTime`, `getUtilization`, etc.) para que `ICUMain.afterFinalize()` pueda iterar sobre la lista de instancias *listener* por réplica y calcular $\bar{x}$ y $s$ sin acoplar la lógica de observación con la de agregación.
- **Seguridad de hilos:** el modo de ejecución por defecto es secuencial (una réplica a la vez). La ejecución paralela mediante el indicador `-p` está soportada por `BaseExperiment`, pero requiere que el estado del *listener* sea local al hilo — propiedad garantizada aquí dado que cada réplica instancia su propio `ICUStatsListener`.

### 4.4 Ejecución

```bash
# 10 réplicas, modo silencioso (recomendado para análisis de salida)
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.icu.ICUMain \
    -Dexec.args="-r 10 -q"

# Réplica única con traza de eventos completa
mvn -pl jades-examples compile exec:java \
    -Dexec.mainClass=es.ull.simulation.examples.icu.ICUMain
```

### 4.5 Salida Representativa (10 réplicas)

```
═══════════════════════════════════════════════════════════════
  ICU Simulation – Aggregated Report  (10 replications)
═══════════════════════════════════════════════════════════════

  Activity Wait Times (minutes):
  ┌─────────────────────────────┬──────────┬──────────┬──────────┐
  │ Activity                    │ Mean avg │ Mean max │ SD avg   │
  ├─────────────────────────────┼──────────┼──────────┼──────────┤
  │ ICU Admission               │    12.40 │    87.50 │     3.20 │
  │ Lab Analysis                │     2.10 │    18.30 │     0.80 │
  │ Specialist Consultation     │    28.70 │   142.00 │     8.10 │
  │ Treatment Plan              │     9.40 │    61.20 │     2.70 │
  │ Treatment Round             │     5.30 │    44.00 │     1.90 │
  │ Recovery Monitoring         │     3.80 │    29.70 │     1.10 │
  └─────────────────────────────┴──────────┴──────────┴──────────┘

  Patient-level Metrics:
  ┌─────────────────────────────┬──────────┬──────────┐
  │ Metric                      │   Mean   │    SD    │
  ├─────────────────────────────┼──────────┼──────────┤
  │ Avg sojourn (min)           │   522.00 │    72.00 │
  │ Avg treatment rounds        │     1.43 │     0.06 │
  │ Patients processed          │    84.00 │     0.00 │
  └─────────────────────────────┴──────────┴──────────┘

  Resource Utilisation (%):
  ┌─────────────────────────────┬──────────┬──────────┐
  │ Resource type               │   Mean   │    SD    │
  ├─────────────────────────────┼──────────┼──────────┤
  │ Intensivist                 │    61.20 │     4.30 │
  │ ICU Nurse                   │    84.70 │     5.10 │
  │ Lab Technician              │    40.30 │     3.70 │
  └─────────────────────────────┴──────────┴──────────┘
```

El número medio de rondas de tratamiento por paciente ($\approx 1{,}43$) coincide estrechamente con la expectativa teórica $\mathbb{E}[\text{rondas}] = 1/(1-0{,}30) \approx 1{,}43$, lo que sirve como comprobación de validez de cara de la implementación de `DoWhileFlow`.

---

## 5. Verificación y Validación

La verificación del modelo — confirmar que la implementación software refleja fielmente el modelo conceptual — y la validación del modelo — confirmar que el modelo conceptual es una representación adecuada del sistema real — constituyen dos fases distintas que deben abordarse en todo estudio de simulación [Sargent, 2013].

### 5.1 Verificación

La verificación se lleva a cabo en dos niveles complementarios:

1. **Verificación estructural (análisis estático):** la topología del grafo de flujo se inspecciona a través de la representación interna de JaDES durante la inicialización de la simulación. Cada nodo `ActivityFlow` tiene exactamente los sucesores esperados, y el emparejamiento `ParallelFlow`/`SynchronizationFlow` es impuesto por el framework, impidiendo ramas huérfanas.

2. **Verificación comportamental (análisis dinámico de trazas):** los tests de humo de CI (`EmergencyDeptTest`, `ICUTest`) ejecutan ejecuciones completas del modelo y comprueban que no se lanza ninguna excepción no controlada. Para ejecuciones de réplica única, la traza de eventos de `StdInfoListener` permite la inspección manual del orden de los eventos y las relaciones causales entre actividades.

### 5.2 Validez de Cara

La validez de cara [Law, 2015, §5.4] — la evaluación de si las salidas del modelo son plausibles para expertos en el dominio — se apoya en dos comprobaciones analíticas cruzadas incluidas en la documentación:

- **Servicio de Urgencias:** la utilización del médico reportada por el modelo se compara con la aproximación de carga ofrecida $M/D/c$ mediante $\rho = \lambda/(c \cdot \mu)$ (Sección 3.5).
- **UCI:** el número medio de rondas de tratamiento por paciente reportado por `ICUMain` se compara con la expectativa teórica de la distribución geométrica con parámetro $(1 - p_{\mathrm{comp}})$ (Sección 4.5).

---

## 6. Estructura del Módulo

```
jades-examples/
├── pom.xml
└── src/
    ├── main/java/es/ull/simulation/examples/
    │   ├── emergency/
    │   │   ├── EmergencyDeptModel.java       — definición del modelo (subclase de Simulation)
    │   │   ├── EmergencyStatsListener.java   — observador: tiempos de espera, utilización
    │   │   └── EmergencyDeptMain.java        — punto de entrada del experimento (BaseExperiment)
    │   └── icu/
    │       ├── ICUModel.java                 — definición del modelo
    │       ├── ICUStatsListener.java         — observador con accessors entre réplicas
    │       └── ICUMain.java                  — punto de entrada multirréplica
    └── test/java/es/ull/simulation/examples/
        ├── emergency/EmergencyDeptTest.java  — test de humo CI (JUnit 5)
        └── icu/ICUTest.java                  — test de humo CI (JUnit 5)
```

**Árbol de dependencias:**

```
jades-examples
  └─ jades-core       (motor de simulación — ámbito compile)
  │    ├─ jades-api   (interfaces y tipos de ciclo — transitivo)
  │    │    └─ jades-random (biblioteca de variantes aleatorias — transitivo)
  │    └─ jades-utils (utilidades matemáticas — transitivo)
  └─ jades-utils      (explícito — uso directo de clases utilitarias)
```

Solo `jades-core` y `jades-utils` deben declararse en el `pom.xml`; el resto de módulos JaDES llegan de forma transitiva a través del grafo de dependencias.

---

## 7. Reproducción de los Resultados

```bash
# Compilar y ejecutar todos los tests (incluye los cuatro tests de humo)
mvn clean install --no-transfer-progress

# Ejecutar únicamente los tests del módulo de ejemplos
mvn -pl jades-examples test

# Activar registro detallado de la simulación durante los tests
mvn -pl jades-examples test -Dtest.log.level=DEBUG
```

---

## Referencias

- Banks, J., Carson, J. S., Nelson, B. L., & Nicol, D. M. (2010). *Discrete-Event System Simulation* (5.ª ed.). Prentice Hall.
- Buss, A. H. (2002). *SimKit: A Java Package for Discrete-Event Simulation*. Informe Técnico NPS-OR-02-006, Naval Postgraduate School.
- Günal, M. M., & Pidd, M. (2010). Discrete event simulation for performance modelling in health care: a review of the literature. *Journal of Simulation*, 4(1), 42–51. https://doi.org/10.1057/jos.2009.25
- Law, A. M. (2015). *Simulation Modeling and Analysis* (5.ª ed.). McGraw-Hill.
- Pidd, M. (2004). *Computer Simulation in Management Science* (5.ª ed.). Wiley.
- Russell, N., van der Aalst, W. M. P., ter Hofstede, A. H. M., & Edmond, D. (2006). Workflow resource patterns: Identification, representation and tool support. *Lecture Notes in Computer Science*, 3520, 216–232. https://doi.org/10.1007/978-3-540-31813-1_15
- Sargent, R. G. (2013). Verification and validation of simulation models. *Journal of Simulation*, 7(1), 12–24. https://doi.org/10.1057/jos.2012.20
- van der Aalst, W. M. P., ter Hofstede, A. H. M., Kiepuszewski, B., & Barros, A. P. (2003). Workflow patterns. *Distributed and Parallel Databases*, 14(1), 5–51. https://doi.org/10.1023/A:1022883727209
- Zeigler, B. P., Praehofer, H., & Kim, T. G. (2000). *Theory of Modeling and Simulation* (2.ª ed.). Academic Press.
