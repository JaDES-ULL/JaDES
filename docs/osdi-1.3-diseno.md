# Diseño de OSDi 1.3 en jades-hta

Estos cambios llevan la copia de OSDi de `jades-hta` (`jades-hta/src/main/resources/OSDi.ttl`) hacia OSDi 1.3.
El número de versión de la copia no cambia hasta que los cambios se trasladen a OSDi.

## Decisiones acordadas

Las decisiones tal como quedaron al implementarlas, según el registro hasta la fase 7b. D8, D9 y D10 siguen pendientes de implementar, y D11 es nueva.

| Id | Decisión | Cambio en la ontología |
| --- | --- | --- |
| D1 | La intervención declara su objetivo: la enfermedad o un elemento de su progresión. Se aplica en cada aparición del objetivo (si es la enfermedad, desde el inicio). Las intervenciones comparadas tienen el mismo objetivo. Con el objetivo en un elemento, el árbol empieza en él con probabilidad 1. El objetivo de una intervención de detección es la enfermedad que detecta. No pueden ser objetivo las etapas ni las manifestaciones propias de un desarrollo. | `hasIntervention` / `isInterventionOf` entre `HealthCondition` e `Intervention`, exactamente un objetivo por intervención. |
| D2 | Las estrategias de una intervención son efectos de sustitución. En su brazo, cada estrategia nueva sustituye a las del mismo tipo (detección, seguimiento, tratamiento) de la condición afectada, o se asigna si no había; las de otros tipos se mantienen. Dos sustituciones sobre la misma condición en un brazo son un error. Los costes y utilidades de las estrategias se suman a los del elemento. | `StrategyReplacementEffect` (subclase de `Effect`, sin magnitud) y `hasReplacementStrategy`. `affects` admite `HealthCondition`. `hasStrategy` sale de `Intervention` (dominio: `HealthCondition` y `Strategy`). |
| D3 | La aplicación da la semántica según el `DataItemType` de la magnitud y el del parámetro afectado. Un factor multiplica. Un riesgo relativo o un hazard ratio sobre una probabilidad actúan sobre la tasa equivalente, 1 − (1 − p)^m. Un hazard ratio sobre un tiempo lo divide (si no hay distribución, se supone exponencial con aviso). Un odds ratio actúa sobre las odds, y una diferencia de medias se resta. Un tipo relativo siempre opera. Una magnitud del mismo tipo que el parámetro, o `DI_Continuous_Variable`, lo sustituye. Varios efectos se aplican en el orden sustituir, multiplicar, restar, y dos sustituciones son un error. | Comentarios de `Effect` y `hasEffectMagnitude`. Individuos `DI_HazardRatio` y `DI_OddsRatio`. |
| D4 | Una estrategia tiene resultados mutuamente excluyentes: de tratamiento (éxito o fracaso) o de detección (positivo, negativo o no concluyente). Cada resultado lleva su riesgo (en detección, uno por estado del paciente), si resuelve la condición, la estrategia siguiente, sus efectos, sus pagos (de una vez) y su momento de evaluación. `failsIf`, por un lado, y la sensibilidad y la especificidad, por otro, son formas abreviadas. Se rechazan los ciclos: una repetición se modela como otra estrategia. | Jerarquía `StrategyOutcome` (`TreatmentOutcome` con `SuccessOutcome` y `FailureOutcome`; `DetectionOutcome` con sus tres resultados). `hasStrategyOutcome`, `hasOutcomeRisk` (con `hasRiskIfDiseased` y `hasRiskIfNotDiseased`), `hasEvaluationTime`, `followedOnSuccessBy`, `followedOnFailureBy`, `resolvesHealthCondition` y `resolvesOnSuccess`. `followedByStrategy` es la secuencia programada. |
| D5 | Una estrategia puede fracasar cuando aparece una condición de salud. Hoy el fracaso ocurre al inicio de la estrategia; falta tener en cuenta la edad de inicio de la condición. | Rango de `failsIf`: `Parameter` o `HealthCondition`. |
| D6 | La eficacia la expresa el `DataItemType`: probabilidad por episodio (árbol) o tiempo hasta el evento (simulación). «Resuelve» detiene los pagos anuales de la condición. En árboles, el riesgo de tiempo hasta el evento T de una progresión se convierte en la probabilidad 1 − e^(−1/T), con T modificado por los efectos de la ruta; el de un resultado de estrategia se rechaza por ahora. | Comentarios de `failsIf`, `hasDuration` y `StrategyOutcome`. |
| D7 | Las utilidades por episodio son pérdidas de AVAC de una vez, que se suman al aplicar una estrategia o un resultado, o al aparecer una manifestación aguda. Los pagos anuales de una aguda se ignoran con aviso. | Comentarios de `appliesOneTime` e `isDisutility`. |
| D8 | Pendiente (fase 9). Efectos adversos. Si el problema forma parte de la evolución natural de la enfermedad y el tratamiento solo cambia su riesgo, se usa un Effect de la estrategia sobre ese riesgo. Si lo provoca el tratamiento, se declara un AdverseEvent: el acontecimiento (por ejemplo, «hipoglucemia por insulina»), causado por una estrategia, con su riesgo, sus pagos de una vez (D7) y sus estrategias. Apunta al problema de salud, que puede ser una manifestación de la enfermedad o un problema ajeno a ella. Los casos complejos (dosis, combinaciones) se expresan con hasConditionExpression. Puede provocar el fracaso de la estrategia (failsIf, D5) o la muerte. En la primera versión no activa la estructura de la enfermedad: el enlace solo identifica el problema. | Nueva clase AdverseEvent ⊑ HealthCondition, disjunta de Disease y de ClinicalProgressionElement. Nuevas propiedades hasAdverseEvent (de Strategy a AdverseEvent) e isOccurrenceOf (de AdverseEvent a HealthCondition). hasRiskCharacterization admite AdverseEvent. Nombres por confirmar en la fase 9. |
| D9 | *Pendiente (fase 10).* La recurrencia es una relación propia: el destino solo aparece cuando el origen se ha resuelto. La secuencia (`hasNext`) queda para la progresión. También modela los episodios repetidos de una aguda. | Nueva propiedad de `Manifestation` a `Manifestation` (nombre por decidir). |
| D10 | *Pendiente (fase 13).* Si la población no declara edad o sexo, la aplicación avisa al construir el modelo y supone 18 años y un 50 % de mujeres. Si ningún parámetro depende de ellos, el aviso basta. Sustituye el valor por defecto actual de JaDES (edad 0 con aviso). | Ninguno: las restricciones de `Population` se mantienen. |
| D11 | Las propiedades de detección (`detects`, sensibilidad, especificidad y umbral temporal) son de las estrategias de detección, no de las intervenciones. Las estrategias de diagnóstico son confirmaciones perfectas. | Dominio de `detects`, `hasSensitivity`, `hasSpecificity` y `hasTemporaryThreshold` en `DetectionStrategy` (umbral, en `ScreeningStrategy`); rango de `isDetectedBy` en `DetectionStrategy`. |

**Pendientes de decidir:**

- El nombre de la propiedad de recurrencia (se propone en la fase 10).
- Qué ocurre con los falsos negativos del cribado: diagnóstico clínico tardío, muerte sin diagnóstico o falsa sensación de seguridad. Hoy no pagan diagnóstico. Bloquea la fase 14.
- Los cribados de varias enfermedades (paneles), que no caben porque cada intervención tiene un solo objetivo.
- El objetivo en una manifestación propia de uno o varios desarrollos: qué desarrollos presenta el paciente.

Ya no están pendientes los nombres (salvo el de la recurrencia) ni el momento de evaluación de los resultados, que entró en la fase 7a.

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

### Fase 6: cribado neonatal con estrategias (D2) (2026-11-15)

**Hecho:**
- `OSDi.ttl` (sin cambiar la versión):
  - `detects`, `hasSensitivity` y `hasSpecificity` solo tienen dominio en `DetectionStrategy`, y `hasTemporaryThreshold` solo en `ScreeningStrategy`. El rango de `isDetectedBy` pasa a ser `DetectionStrategy`.
  - Se quitan las restricciones correspondientes de `DetectionIntervention` y `ScreeningIntervention`.
  - Comentarios nuevos en `ScreeningIntervention`, `DiagnosisIntervention`, `DetectionStrategy` y `detects`. El de `hasIntervention` precisa que el objetivo de una intervención de detección es la enfermedad que detecta y que se aplica a la población en riesgo.
- Árbol:
  - `DetectionInterventionWrapper` rechaza las propiedades de detección en intervenciones.
  - `NBSDetectionArm` reúne las estrategias de detección de cada brazo (tras la sustitución por tipo, con `DiseaseSubTreeGenerator.replaceByType`) y sus costes.
  - `NBSModel` y los dos constructores del árbol NBS leen de ahí la sensibilidad, la especificidad y los costes, y pasan las sustituciones del brazo al subárbol de la enfermedad.
- DES: `OSDiDetectionIntervention` lee la sensibilidad y la especificidad de la estrategia de detección del brazo (de sus sustituciones o, si no, de la enfermedad).
- Migración de `PBD.ttl`:
  - Estrategias nuevas `PBD_ClinicalDiagnosis` y `PBD_ScreeningStrategy`.
  - El cribado sustituye las estrategias de detección de la enfermedad por el grupo {cribado, diagnóstico}.
  - Las intervenciones pierden costes, sensibilidad, especificidad y `detects`, y `PBD_CostDiagnosis` sale de los costes propios de la enfermedad.
  - Los valores de regresión no cambian (1,5512269701985133 y 0,7636187637821464). `BD.ttl` ya lo declaraba en estrategias.
- Tests:
  - Nueva ontología `OSDi_test_tree_nbs.ttl` con B01–B03, que se comprueban en `TestHTAExcelGeneration` con valores calculados a mano.
  - N20 (intervención con `hasSensitivity`), N21 (cribado en el brazo sin cribado) y N22 (intervención con `detects`). Sus mensajes se comprobaron a mano con un test temporal.
- Documentación: Javadoc, `docs/known-limitations.md` (sección «Newborn screening» y fila de la tabla de conformidad) y Claude.MD.

**Decidido:**
- Una sola forma de modelar la detección: las propiedades de detección son de las estrategias, y la enfermedad detectada por una intervención es su objetivo (D1). `detects` sobraba en la intervención y podía contradecir su objetivo.
- Costes: el cribado lo paga toda la población del brazo (costes propios de la intervención con `ScreeningCat` más la estrategia de cribado). El diagnóstico lo pagan los verdaderos y falsos positivos, o los enfermos sin cribado (costes propios de la enfermedad con `DiagnosisCat` más la estrategia de diagnóstico del brazo).
- La sensibilidad y especificidad de la estrategia de diagnóstico no se usan: el diagnóstico se supone perfecto.

**Pendiente:**
- Falsos negativos del cribado: hoy no pagan diagnóstico. Lo que les ocurre depende de la enfermedad (diagnóstico clínico tardío o muerte sin diagnóstico, en parte por una falsa sensación de seguridad tras el cribado). Equipararlos siempre a la detección clínica podría ser engañoso, así que se revisará en una fase posterior.
- Cribados de varias enfermedades (paneles): D1 limita cada intervención a un objetivo y cada sustitución a una condición, así que no caben. El NBS solo admite una enfermedad.
- `followedByStrategy` (de cribado a confirmación) sigue ignorándose, a la espera de D4.
- El DES no aplica los costes de las estrategias de detección.

### Fase 7a: resultados de las estrategias de tratamiento (D4–D7) (2026-11-15)

**Hecho:**
- `OSDi.ttl` (sin cambiar la versión):
  - Jerarquía `StrategyOutcome`: `TreatmentOutcome` (con `SuccessOutcome` y `FailureOutcome`) y `DetectionOutcome` (con los resultados positivo, negativo y no concluyente; este último exige una estrategia siguiente).
  - Propiedades nuevas: `hasStrategyOutcome`/`isStrategyOutcomeOf`, `hasOutcomeRisk` con sus subpropiedades `hasRiskIfDiseased` y `hasRiskIfNotDiseased`, `hasEvaluationTime`, `followedOnSuccessBy`, `followedOnFailureBy`, `resolvesHealthCondition` y `resolvesOnSuccess`.
  - `failsIf` pasa a admitir cualquier estrategia y apunta a un parámetro o a una condición de salud (D5). `followedByStrategy` también vale en resultados. `hasCost`, `hasUtility` y `hasEffect` admiten resultados.
  - Comentarios de D6 y D7. La sensibilidad y la especificidad pasan a ser la forma abreviada de dos resultados de detección.
  - Restricciones `hasStrategyOutcome only …` en las estrategias de detección, seguimiento y tratamiento.
- Código:
  - `StrategyOutcomeWrapper` y `StrategyWrapper.getOutcomes` (expande la forma abreviada y valida).
  - `StrategyOutcomeExpander` expande los resultados bajo las hojas.
  - `BranchDestinationNode` guarda los pagos con ventana y las resoluciones, y `PayoffWrapper.applyDiscount` los aplica en el Excel.
  - El soporte de tests añade los resultados a la clave de las hojas (prefijo `OUTCOME: `).
- Tests: P42–P46 y N23–N26, con valores calculados a mano. Los mensajes de N23–N26 se comprobaron a mano.
- Documentación: `docs/known-limitations.md` (sección «Strategy outcomes in decision trees» y fila de la tabla de conformidad) y Claude.MD.

**Decidido:**
- Se usa una jerarquía de clases para los resultados. El riesgo es un parámetro, unido por `hasOutcomeRisk` (o, en detección, uno por estado del paciente). Las distinciones sin efecto en el cálculo van como categorías SKOS.
- Los resultados se expanden bajo las hojas, porque son independientes de las demás ramas. El momento de cada resultado es el inicio de la estrategia más `hasEvaluationTime`; sin él, el inicio, con aviso.
- Los pagos anuales de la estrategia van hasta el resultado si este tiene estrategia siguiente o resuelve la condición. Los pagos de un resultado son de una vez.
- «Resuelve» detiene los pagos anuales de la condición en ese momento; sus efectos sobre la LE se mantienen, con aviso.
- Se rechazan los ciclos: una repetición se modela como otra estrategia. Se rechazan los riesgos de tiempo hasta el evento en árboles.
- Se dividió la fase en 7a (tratamiento) y 7b (detección en el NBS).

**Pendiente:**
- Fase 7b: resultados de detección en el NBS (riesgos por estado, resultados no concluyentes encadenados a otra prueba, confirmación con el `followedByStrategy` de los positivos). Hecho en la Fase 7b.
- Pautas en estrategias con resultados (hoy `Unsupported`).
- El fracaso por una condición de salud ocurre al inicio de la estrategia: no se tiene en cuenta la edad de inicio de la condición.
- Efectos adversos que pueden coincidir con el éxito o el fracaso: habría que modelarlos como manifestaciones agudas cuyo riesgo depende de la estrategia.
- El DES no simula estrategias ni sus resultados.
- El aviso de `StrategyWrapper` sobre las estrategias con pauta («Guideline-based strategies are not supported yet») ha quedado desfasado desde la Fase 5. No lo he cambiado porque queda fuera del alcance de esta fase.

### Fase 7b: resultados de detección en el cribado neonatal (D6) (2026-11-15)

**Hecho:**
- `OSDi.ttl` (sin cambiar la versión): el comentario de `PositiveDetectionOutcome` explica la confirmación con una estrategia de diagnóstico, la prueba de segundo nivel y el uso por defecto del `followedByStrategy` de la estrategia.
- Código:
  - `StrategyOutcomeWrapper` lee los riesgos por estado (`hasRiskIfDiseased`, `hasRiskIfNotDiseased`) y el tipo de resultado de detección.
  - `DetectionStrategyWrapper.getDetectionOutcomes` valida los resultados explícitos y los ordena (positivo, no concluyente, negativo). Un positivo sin estrategia propia toma el `followedByStrategy` de la estrategia.
  - `NBSScreeningInterventionBuilder` ramifica enfermos y sanos en los resultados de la estrategia de cribado. La forma abreviada se expande a positivo y negativo, con el mismo árbol que antes. Un resultado seguido de una estrategia de diagnóstico es una confirmación perfecta; seguido de otra estrategia de detección, paga su coste y se vuelve a ramificar en el mismo estado.
  - `NBSDetectionArm.addConfirmationCosts` compone el coste de la confirmación con otra estrategia de diagnóstico, y el brazo rechaza una estrategia de diagnóstico con resultados explícitos.
  - `StrategyWrapper.getOutcomes` sigue rechazando los resultados de detección fuera del NBS, con un mensaje nuevo. El aviso de `followedByStrategy` ya no salta en las estrategias de detección.
  - `OSDiDetectionIntervention` (DES) rechaza las estrategias de detección con resultados explícitos.
- Tests: B04–B06 en `OSDi_test_tree_nbs.ttl` (comprobados en `TestHTAExcelGeneration`) y N27–N30, con valores calculados a mano. Los mensajes de N27–N30 se comprobaron a mano. `PBD.ttl` y B01–B03 dan los mismos resultados.
- Documentación: `docs/known-limitations.md` (secciones «Newborn screening» y «Strategy outcomes in decision trees», y tabla de conformidad) y Claude.MD.

**Decidido:**
- Las estrategias de diagnóstico son confirmaciones perfectas. Las demás estrategias de detección (cribado o pruebas de segundo nivel) son pruebas que se ramifican en sus resultados. Esto vale igual para un positivo que para un no concluyente.
- Un no concluyente sin estrategia siguiente, o un positivo o no concluyente seguido de una estrategia que no es de detección, es `Malformed`. Un negativo termina la detección: su estrategia siguiente se ignora con aviso.
- Los pagos de un resultado de detección son de una vez y sus efectos se aplican en su rama. `hasEvaluationTime` y los pagos anuales se ignoran con aviso, porque el cribado ocurre en el momento 0.
- `hasOutcomeRisk` no se usa en los resultados de detección.
- En la propuesta, el AVAC del brazo con cribado de B06 era 68 por un error de cálculo. Como S1 es el de B05, también tiene la pérdida de 0,01 AVAC del no concluyente, así que el valor correcto es 67,999495. Se corrigió antes de implementar.

**Pendiente:**
- Estrategias de diagnóstico con resultados explícitos, es decir, una confirmación imperfecta (hoy `Unsupported`, N30).
- Resultados de detección explícitos en el DES (hoy `Unsupported`).
- Los falsos negativos (ya pendiente desde la Fase 6).
- Los demás puntos pendientes de la Fase 7a.

### Fase 8: sincronización del diseño y defectos de fases anteriores (2026-11-16)

**Hecho:**
- Las secciones «Decisiones acordadas» y «Pendientes de decidir» recogen las decisiones tal como quedaron hasta la fase 7b, con la nueva D11 (propiedades de detección en las estrategias).
- Árbol, riesgo `DI_TimeToEvent` de una progresión: la probabilidad 1 − e^(−1/T) ya no depende del parámetro sintético `P_…`, que no se escribía en el Excel. La fórmula de cada rama la calcula con el tiempo modificado por los efectos de la ruta (`AbstractModelFactory.getSourceTimeToEvent` y `HTAExcelModelFactory.getParameterTextFormula`). Se quita el rechazo de `DiseaseSubTreeGenerator.getAdaptedRiskParameter`, que conserva la validación de las operaciones (N12 sigue rechazado). N13 vuelve a ser P26 (HR 0,5 sobre T = 2: p = 1 − e^(−1/2) = 0,393469; p' = 1 − e^(−1/4) = 0,221199). Antes del cambio, P26 fallaba con el rechazo y, sin él, con `The value of the parameter P_TT_P26_Severe_Risk cannot be computed in the tests: CALCULATED`.
- DES, nombres de los riesgos: `TimeToEventCalculatorFactory` buscaba `P_<nombre>` (y `RR_`), pero `OSDiManifestationPathway` registra el riesgo con su nombre corto, y `EffectFactory` hace lo mismo con los modificadores. El riesgo valía `NaN` y las manifestaciones con riesgo no ocurrían nunca. El nuevo test `TestDESRisksAndEffects` (ontología `OSDi_test_des.ttl`, caso D01) falló con `No patient suffered ManX without intervention ==> expected: <true> but was: <false>`: proporción 0,0 en las dos manifestaciones y los dos brazos. Los calculadores usan ahora el nombre corto. El mismo test comprueba los valores de los efectos en el DES: HR 0,5 sobre p = 0,2 da 1 − 0,8^0,5 = 0,105573, y el factor 0,5 sobre 0,3 da 0,15. Ya funcionaban, porque parámetro y modificador comparten el nombre corto.
- DES, conversión de la probabilidad anual: `Statistics.getAnnualBasedTimeToEvent` (en `jades-utils`) usaba la probabilidad modificada como tasa (`−ln(U)/p'`). Ahora usa la tasa exacta, `−rr · ln(1 − p)`. Tests nuevos: `StatisticsTest.shouldComputeAnnualBasedTimeToEventWithTheExactRate` (p = 0,2, rr = 0,5, U = 0,5: T = 6,212567; antes daba 6,565584) y `TestDESRisksAndEffects.convertsAnnualProbabilityIntoExactRate` (proporción a 5 años, 1 − (1 − p)^5). Antes del cambio fallaron con `expected: <6.212567439010781> but was: <6.565584331088442>` y con `ManX without intervention ==> expected: <0.67232> but was: <0.6318>` (también ManY: 0,7838 frente a 0,831930, y ManY con el factor: 0,5338 frente a 0,556295). El cambio afecta también a `AgeBasedTimeToEventCalculator`, que usa la misma función.

**Decidido:**
- En el árbol, la probabilidad derivada de un tiempo hasta el evento no se escribe como parámetro en el Excel: la calcula la fórmula de cada rama, porque los efectos de la ruta modifican el tiempo.
- En el DES, el nombre de un riesgo es su nombre corto, el mismo con el que se registran el parámetro y los modificadores de los efectos.

**Pendiente:**
- D6 decía que los riesgos de tiempo hasta el evento se rechazan en árboles. Ahora solo se rechazan en los resultados de estrategias (N24). Hecho: el texto de D6 ya está precisado.
- DES: solo acepta riesgos `DI_Probability`, `DI_Proportion` y probabilidad con `DI_RelativeRisk`. `DI_Incidence`, que usan las ontologías de árbol, y `DI_TimeToEvent` se rechazan.
- DES: en la combinación probabilidad + RR, si la probabilidad aparece primero en la lista, el RR se ignora (`TimeToEventCalculatorFactory`, rama `PROBABILITY_RR`).
- DES: un sexo de la población declarado como parámetro determinista provocaba un `ClassCastException` en `OSDiPopulation`. Hecho: véase la entrada siguiente.
- DES: la edad de la población no distingue la heterogeneidad (primer orden) de la incertidumbre paramétrica (segundo orden): un parámetro de edad de segundo orden se muestrea para cada paciente.
- DES, sexo de la población: `hasSex` es la heterogeneidad del sexo (primer orden; 0 = hombre, 1 = mujer). Un parámetro determinista, de segundo orden o calculado es la proporción de mujeres p, que se registra como parámetro del modelo; cada paciente se sortea con un Bernoulli(p). El segundo orden se muestrea una vez por simulación (valor esperado en el caso base) y ese valor es la p del Bernoulli. Un parámetro de primer orden sigue usando su distribución, que debe ser discreta (T1DM no cambia). Se rechazan, con `MalformedOSDiModelException`, un valor determinista fuera de [0, 1] y una distribución de primer orden no discreta (`OSDiPopulation.initializeSexVariate` y `getSexVariate`). `OSDi_test_des.ttl` vuelve a declarar el sexo como determinista (0,5). Antes del cambio, `TestDESRisksAndEffects` falló en `setUp` con `ClassCast class simkit.random.ConstantVariate cannot be cast to class simkit.random.DiscreteRandomVariate`. El test nuevo `assignsSexFromProportionOfWomen` comprueba una proporción de mujeres de 0,5 ± 0,02.
