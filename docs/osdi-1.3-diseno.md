# Diseño de OSDi 1.3 en jades-hta

Estos cambios llevan la copia de OSDi de `jades-hta` (`jades-hta/src/main/resources/OSDi.ttl`) hacia OSDi 1.3.
El número de versión de la copia no cambia hasta que los cambios se trasladen a OSDi.

## Decisiones

| Id | Decisión | Cambio en la ontología |
| --- | --- | --- |
| D1 | La intervención declara su objetivo: la enfermedad o un elemento de su progresión. Se aplica en cada aparición del objetivo (si es la enfermedad, desde el inicio). Las intervenciones comparadas tienen el mismo objetivo, y el árbol empieza en los pacientes que lo presentan. | `hasIntervention` / `isInterventionOf` entre `HealthCondition` e `Intervention`, exactamente un objetivo por intervención. |
| D2 | Las estrategias de una intervención son efectos: en su brazo sustituyen las estrategias del problema de salud afectado, o se le asignan si no tenía. Dos sustituciones sobre el mismo problema de salud en un brazo son un error. | Nueva subclase `StrategyReplacementEffect` de `Effect` (su `affects` apunta a la `HealthCondition`) y nueva propiedad `hasReplacementStrategy` (hacia `Strategy` o grupo). Se quita `hasStrategy` de `Intervention`. |
| D3 | La aplicación da la semántica según el `DataItemType` de la magnitud: un factor o una razón multiplican (un hazard ratio, sobre la tasa equivalente; un odds ratio, sobre las odds) y una diferencia de medias se resta. Una magnitud del mismo tipo que el parámetro afectado es un valor absoluto y lo sustituye; `DI_Continuous_Variable` también sustituye, y un tipo no relativo que no coincide con el del parámetro produce un aviso. Si un hazard ratio afecta a un tiempo sin distribución, se supone exponencial y se avisa al construir el modelo. Con varios efectos sobre un parámetro, el orden lo decide la aplicación; la ontología documenta uno sugerido (sustituir, multiplicar, restar). | Comentarios de `Effect` y `hasEffectMagnitude` (convenciones y orden sugerido). Nuevos individuos `DI_HazardRatio` y `DI_OddsRatio`. |
| D4 | Una estrategia tiene resultados mutuamente excluyentes. Cada uno lleva su probabilidad o su tiempo, si resuelve el problema de salud, la estrategia siguiente, sus efectos, sus costes y pérdidas de AVAC y, opcionalmente, el momento en que se evalúa. `failsIf` con su éxito y su fracaso es la forma abreviada de dos resultados. | Nueva clase `StrategyOutcome`; nuevas `followedOnFailureBy`, `followedOnSuccessBy` y `resolvesOnSuccess`; `followedByStrategy` pasa a ser la secuencia programada al terminar. |
| D5 | Una estrategia puede fracasar cuando aparece un problema de salud (la progresión, por ejemplo). | Rango de `failsIf` ampliado a `HealthCondition`. |
| D6 | La eficacia la expresa el `DataItemType`: probabilidad por episodio (árbol) o tiempo hasta el evento (simulación). En simulación, el éxito llega con la duración del problema de salud modificada por los efectos. El generador convierte cuando puede y avisa cuando no. | Comentarios de `failsIf`, `hasDuration` y `StrategyOutcome`. |
| D7 | Las utilidades por episodio son pérdidas de AVAC de una vez, que se suman al aplicar una estrategia o un resultado, o al aparecer una manifestación. | Comentarios de `appliesOneTime` e `isDisutility`. |
| D8 | Una ruta puede requerir una estrategia, con el significado «la estrategia se ha aplicado». | Rango de `requires` y restricción de `Pathway` ampliados a `Strategy`. |
| D9 | La recurrencia es una relación propia: el destino solo aparece cuando el origen se ha resuelto. La secuencia (`hasNext`) queda para la progresión. | Nueva propiedad de `Manifestation` a `Manifestation` (nombre por decidir). |
| D10 | Si la población no declara edad o sexo, la aplicación avisa al construir el modelo y supone 18 años y un 50 % de mujeres. Si ningún parámetro depende de ellos, el aviso basta. Sustituye el valor por defecto actual de JaDES (edad 0 con aviso). | Ninguno: las restricciones de Population se mantienen. |

## Puntos pendientes

Pendientes de decidir:

- Los nombres definitivos de las clases y propiedades nuevas.
- Si el momento de evaluación de un resultado (reglas de suspensión) entra en la primera versión.

## Registro

<!-- Cada fase añade una entrada: qué se hizo, qué se decidió y qué quedó pendiente. -->

### Fase 1: investigación (2026-11-13)

**Hecho:** inventario de cómo leen hoy el árbol y el DES los elementos afectados por D1–D10, de sus tests y de lo que se ignora o se rechaza. CLAUDE.md quedó actualizado (versión 1.2 de la copia, rechazos y elementos ignorados).

### Fase 2: semántica de la magnitud de un `Effect` (D3) (2026-11-13)

**Hecho:**
- La operación de un `Effect` genérico depende del `DataItemType` de la magnitud **y** del parámetro afectado (`EffectWrapper.resolveOperation`). Ya no se fija una única operación por efecto.
- Operaciones nuevas, compartidas por el árbol y el DES (`ModifierOperation`): sustituir, multiplicar, razón de tasas `1 − (1 − p)^m`, HR sobre un tiempo `T/m`, odds ratio `p·m/(1 − p + p·m)` y restar.
- Varios efectos sobre un parámetro se aplican en el orden sustituir → multiplicar (factores, tasas, odds) → restar. Dos sustituciones son un error. En el árbol, una sola fórmula (`HTAExcelModelFactory.getParameterTextFormula`) sustituye a las dos copias anteriores. En el DES, un `CompositeParameterModifier` por parámetro e intervención sustituye al modificador único, que se pisaba.
- Nuevos individuos `DI_HazardRatio` y `DI_OddsRatio` en `OSDi.ttl` y en `OSDiDataItemType`. Comentarios de `Effect` y `hasEffectMagnitude`. La versión no cambia.
- Tests: P24 (HR), P25 (OR) y P27 (orden de tres efectos); N08–N13 (dos sustituciones, OR y HR sobre un coste, desviación estándar como magnitud, RR y HR sobre un tiempo); tests unitarios `TestModifierOperation` y `TestEffectOperationResolution`. Nuevos valores esperados, calculados a mano, de P01, I01 e I02.
- Documentación: Javadoc, `docs/known-limitations.md` («Effect magnitudes» y fila de la tabla de conformidad) y CLAUDE.md.

**Decidido:**
- Un tipo relativo siempre opera, aunque coincida con el del parámetro (el RR sobre el RR de P09 multiplica).
- HR y OR tratan como probabilidad los tipos de probabilidad del árbol: probabilidad, proporción, incidencia, prevalencia, prevalencia al nacer, sensibilidad y especificidad.
- RR y HR sobre una probabilidad se aplican sobre la tasa equivalente: `1 − (1 − p)^m`. Coincide con lo que ya hacía el DES en `Statistics.getAnnualBasedTimeToEvent`. Para multiplicar linealmente se declara `DI_Factor`. No se compara la fuente de los parámetros para elegir la fórmula.
- RR sobre algo que no es un riesgo multiplica, con un aviso que recomienda `DI_Factor`. RR sobre un tiempo se rechaza (Unsupported).
- Las operaciones lineales sobre una probabilidad no se recortan a [0, 1]; se avisa si los valores son deterministas.
- Un tipo indefinido (`DI_Undefined`, `DI_Other`) sustituye, con aviso.
- El HR sobre un tiempo se rechaza en el árbol hasta que se soporten los tiempos hasta el evento (el caso P26 pasó a ser N13).

**Pendiente:**
- En el árbol, un riesgo `DI_TimeToEvent` no funciona, con o sin efectos: el parámetro sintético `P_…` no se escribe en el Excel y los tests no pueden calcular su valor. Al corregirlo, quitar el rechazo de `DiseaseSubTreeGenerator.getAdaptedRiskParameter` y recuperar P26 (HR 0,5 sobre T = 2: p = 1 − e^(−1/2), p' = 1 − e^(−1/4)).
- DES: los riesgos se registran con su nombre corto, pero el calculador los busca como `P_<nombre>` (`OSDiManifestationPathway`, `TimeToEventCalculatorFactory`). Hay que confirmar si las manifestaciones con riesgo llegan a ocurrir y si los efectos sobre riesgos se aplican.
- DES: `Statistics.getAnnualBasedTimeToEvent` usa la probabilidad modificada como si fuera la tasa (`−ln(U)/p'`); la tasa exacta es `−ln(1 − p')`.
- DES: solo se aplican los efectos causados por una intervención; no hay ningún test que compruebe valores de efectos en el DES.
- Añadir `hasRefToSTATO` a `DI_HazardRatio` y `DI_OddsRatio` cuando se confirmen sus códigos.

### Fase 3: manifestaciones agudas en árboles (2026-11-15)

**Hecho:**
- El árbol ya no rechaza las manifestaciones agudas, porque serán objetivo de intervenciones (D1), como la crisis de migraña. `DiseaseSubTreeGenerator` construye su nodo igual que el de una crónica (reglas de combinación, dependencia de un desarrollo y efectos sobre la hoja). Además, comprueba la frecuencia (`checkAcuteManifestation`) y quita los pagos anuales (`removeAnnualPayoffs`).
- Tests nuevos: P28 (riesgo y pagos de una vez), P29 (pagos anuales ignorados), P30 (frecuencia ignorada), P31 (sin riesgo), P32 (`DI_Factor` de la intervención sobre el riesgo). N06 pasa a ser «aguda con solo `hasFrequency`». Los valores esperados se calcularon a mano antes de implementar.
- Documentación: Javadoc de la clase y de `generateNode`, comentario de `AcuteManifestation` en `OSDi.ttl` (sin cambiar la versión), `docs/known-limitations.md` (sección y tabla de conformidad) y Claude.MD (semántica, divergencias; se quita la limitación).

**Decidido:**
- Una manifestación aguda es un único episodio entre la edad inicial y la muerte de la hoja. Su riesgo es la probabilidad de que ocurra. Sin riesgo, ocurre siempre en su contexto: es el árbol que empieza en la crisis.
- `hasFrequency` se ignora con aviso si hay riesgo; si solo hay frecuencia, se rechaza (`UnsupportedOSDiFeatureException`). No se convierte en 1 − e^(−f·H): el árbol no tiene horizonte (cada hoja cuenta sus años de vida y `hasTimeHorizon` no llega al Excel), y contar un episodio perdería el número esperado de episodios, f·H.
- Pagos de una vez: una vez por episodio, en el momento 0 o en la edad de inicio, con las reglas de las ventanas temporales; una utilidad de una vez es una pérdida de AVAC (D7); varios se suman. Pagos anuales: se ignoran con aviso, como en el DES.

**Pendiente:**
- Episodios repetidos en el árbol: se modelarán con la recurrencia (D9). Hasta entonces, el árbol cuenta un episodio y el DES todos.
- Divergencia: con varios pagos de una vez en una aguda, el DES solo usa el primero (`OSDiDiseaseProgression`), mientras que el árbol los suma.
- El DES no lee `hasFrequency`: el tiempo hasta el siguiente episodio sale solo del riesgo.
- N06 comprueba el tipo de excepción, no el mensaje (como todos los casos N).

### Fase 4: objetivo de las intervenciones (D1) (2026-11-15)

**Hecho:**
- `OSDi.ttl` (sin cambiar la versión):
  - `hasIntervention` va de `HealthCondition` a `Intervention`, e `isInterventionOf`, de `Intervention` a `HealthCondition`. Las dos tienen comentarios en inglés y en español con la semántica.
  - `Intervention` tiene un objetivo exacto (`isInterventionOf`, cardinalidad cualificada 1 sobre `HealthCondition`).
  - La restricción `hasIntervention allValuesFrom Intervention` pasa de `Disease` a `HealthCondition`.
- Árbol:
  - `DiseaseSubTreeGenerator.resolveInterventionTarget` obtiene el objetivo común de las intervenciones del modelo.
  - `generateTargetSubTree` construye el árbol que empieza en un elemento de progresión.
  - `SimpleInterventionGenerator` reúne aparte los pagos de la intervención, para poder quitar los anuales cuando el objetivo es agudo.
  - `NBSModel` rechaza cualquier objetivo que no sea la enfermedad.
- Tests:
  - I03 (objetivo en una aguda, declarado con `isInterventionOf`) e I04 (objetivo en un desarrollo, declarado con `hasIntervention`), con valores calculados a mano.
  - N14 (objetivos distintos), N15 (dos objetivos) y N16 (manifestación propia de un desarrollo). Sus mensajes se comprobaron a mano con un test temporal, igual que el de N06.
  - Los resultados de los modelos existentes no cambian.
- Documentación: Javadoc, `docs/known-limitations.md` (sección «Target of the interventions» y fila de la tabla de conformidad) y Claude.MD (semántica, tablas de tests y divergencia; se quita la limitación de que `hasIntervention` e `isInterventionOf` no se leían).

**Decidido:**
- La cardinalidad «exactamente un objetivo» se comprueba dentro de cada modelo, porque las ontologías de prueba comparten intervenciones entre modelos. Una intervención sin objetivo en el modelo apunta a la enfermedad, con aviso.
- El objetivo se lee en las dos direcciones, porque `StructuralReasoner` no infiere inversas.
- Con objetivo en un elemento T: el árbol empieza en T con probabilidad 1 (se ignoran su riesgo y sus reglas).
  - Los pagos y efectos de la enfermedad se mantienen, salvo su muerte inmediata.
  - Solo se incluye lo que depende de T.
  - Si T es agudo, los pagos anuales de la intervención se ignoran con aviso.
- Se rechazan como objetivo las etapas y las manifestaciones propias de un desarrollo (`Unsupported`).

**Pendiente:**
- Objetivo en una manifestación propia de uno o varios desarrollos: decidir qué desarrollos presenta el paciente.
- DES: no lee el objetivo; las intervenciones se aplican desde el inicio. Habría que aplicarlas en cada aparición del objetivo.
- Las intervenciones compartidas de las ontologías de prueba (`TT_InterventionTreat`, `TT_InterventionNone`) incumplen la cardinalidad global de la ontología. No falla porque `StructuralReasoner` no la comprueba, pero un razonador completo inferiría que las enfermedades son el mismo individuo.
- No hay un test del aviso de una intervención sin objetivo (todas las ontologías de prueba lo declaran).
- D2 (estrategias de una intervención como efectos) quitará `hasStrategy` de `Intervention`.

### Fase 5: estrategias de una intervención como efectos (D2) (2026-11-15)

**Hecho:**
- `OSDi.ttl` (sin cambiar la versión):
  - Nueva clase `StrategyReplacementEffect`, subclase de `Effect`: una condición de salud en `affects`, una estrategia o grupo en `hasReplacementStrategy` y sin magnitud.
  - Nueva propiedad `hasReplacementStrategy`.
  - `affects`, `isAffectedBy` y la restricción de `Effect` admiten `HealthCondition`.
  - `hasStrategy` sale de `Intervention` y de `TherapeuticIntervention` (dominio: `HealthCondition ∪ Strategy`).
  - Constantes nuevas en `OSDiClass`, `OSDiObjectProperty` y `EffectType`.
- Migración: `BD.ttl` era la única ontología de prueba con `hasStrategy` en intervenciones. El brazo con cribado sustituye las estrategias de detección de la enfermedad por un grupo (cribado más diagnóstico clínico). Ningún test la carga, así que no cambia ningún valor esperado.
- Árbol:
  - `EffectWrapper` inicializa las sustituciones y rechaza los demás efectos sobre condiciones de salud.
  - `SimpleInterventionGenerator.getStrategyReplacements` reúne las sustituciones de cada brazo.
  - `DiseaseSubTreeGenerator.getArmStrategies` aplica la sustitución por tipo.
  - `BranchDestinationNode` suma los costes y utilidades de las estrategias a los propios.
  - `StrategyCosts` reúne los costes de estrategias, tecnologías y pautas, y `PayoffWrapper.getGuidelineCostFormula` escribe sus fórmulas en el Excel.
  - El NBS rechaza las sustituciones y las estrategias declaradas en intervenciones.
  - El DES (`OSDiIntervention`) ignora las sustituciones con aviso.
- Tests: P33–P41 y N17–N19, con valores calculados a mano. Los mensajes de N17–N19 se comprobaron a mano con un test temporal.
- Documentación: Javadoc, `docs/known-limitations.md` (sección «Strategies and guidelines in decision trees» y fila de la tabla de conformidad) y Claude.MD.

**Decidido:**
- La sustitución actúa por tipo de estrategia (detección, es decir, cribado o diagnóstico; seguimiento; tratamiento). Cada estrategia nueva sustituye a las de su tipo o se asigna si no había ninguna; las de otros tipos se mantienen. Se adoptó en lugar de «sustituye todas» porque el cribado del NBS borraría el seguimiento y el tratamiento.
- Los costes y utilidades de las estrategias se suman a los propios del elemento. Antes solo se usaban si el elemento no tenía propios. No hay ambigüedad, porque solo se lee lo que el modelo incluye: el modelador elige el coste global, el detallado o ambos.
- Con pauta, el coste de una tecnología es por aplicación:
  - r = frecuencia × 8760 / periodo (en horas);
  - inicio = inicio del nodo + desfase;
  - L = MIN(duración, número / r);
  - con frecuencia, pago anual r·c durante [inicio, inicio + L] recortado al nodo y a la vida;
  - sin frecuencia, N·c de una vez en el inicio, con N = número de aplicaciones o 1.
  - Si la tecnología no tiene pauta, se usa la de la estrategia; si ninguna tiene, sus costes son pagos normales.
- Se rechazan dos sustituciones de la misma condición en un brazo, otros efectos sobre condiciones de salud y sustituciones declaradas por la enfermedad o una progresión.

**Pendiente:**
- NBS con estrategias (fase siguiente): el brazo con cribado sustituye la estrategia de detección de la enfermedad por la de cribado; la sensibilidad, la especificidad y los costes se leen de la estrategia de detección de cada brazo; la confirmación depende de `followedByStrategy` (D4); hay que migrar `PBD.ttl` sin cambiar resultados.
- TODO: pautas con subpautas, `hasConditionExpression` o edades de aplicación (hoy se rechazan, N19). JaDES no las soporta: ni el árbol ni el DES leen pautas de OSDi.
- Sin tests: utilidades de estrategias, pautas con descuento, ventanas temporales del nodo combinadas con pautas y `ADD_DISTINCT` con costes de pauta compartidos.
- `BD.ttl` sigue importando OSDi 1.0, que no tiene `StrategyReplacementEffect`.
- El DES no simula estrategias.
