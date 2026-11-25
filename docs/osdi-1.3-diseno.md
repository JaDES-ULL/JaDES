# Diseño de OSDi 1.3 en jades-hta

Estos cambios llevan la copia de OSDi de `jades-hta` (`jades-hta/src/main/resources/OSDi.ttl`) hacia OSDi 1.3.
El número de versión de la copia no cambia hasta que los cambios se trasladen a OSDi.

## Decisiones acordadas

Las decisiones tal como quedaron al implementarlas, según el registro hasta la fase 14. Todas están implementadas en los árboles. En el DES están implementadas D1 y D2 (fase 14), D3 (fases 2 y 8), D10 (fase 13) y, en parte, D11 (la sensibilidad y la especificidad se leen de las estrategias de detección; las pruebas quedan para la fase 23). El DES no implementa D4–D8: rechaza las estrategias con resultados o efectos adversos y los riesgos de tiempo hasta el evento (D6).

| Id | Decisión | Cambio en la ontología |
| --- | --- | --- |
| D1 | La intervención declara su objetivo: la enfermedad o un elemento de su progresión. El tiempo 0 es el momento en que se aplica: todos los pacientes presentan el objetivo y la intervención está activa toda la simulación. Con un objetivo agudo, todos empiezan con un episodio; el árbol evalúa ese episodio y el DES también los siguientes, tratando cada uno (divergencia documentada). Las intervenciones comparadas tienen el mismo objetivo. El objetivo de una intervención de detección es la enfermedad que detecta, y se aplica a la población en riesgo. No pueden ser objetivo las etapas, las manifestaciones propias de un desarrollo ni los objetivos con restricción temporal; en el DES, tampoco los desarrollos. | `hasIntervention` / `isInterventionOf` entre `HealthCondition` e `Intervention`, exactamente un objetivo por intervención. |
| D2 | Las estrategias de una intervención son efectos de sustitución. En su brazo, cada estrategia nueva sustituye a las del mismo tipo (detección, seguimiento, tratamiento) de la condición afectada, o se asigna si no había; las de otros tipos se mantienen. Dos sustituciones sobre la misma condición en un brazo son un error. Los costes y utilidades de las estrategias se suman a los del elemento. | `StrategyReplacementEffect` (subclase de `Effect`, sin magnitud) y `hasReplacementStrategy`. `affects` admite `HealthCondition`. `hasStrategy` sale de `Intervention` (dominio: `HealthCondition` y `Strategy`). |
| D3 | La aplicación da la semántica según el `DataItemType` de la magnitud y el del parámetro afectado. Un factor multiplica. Un riesgo relativo o un hazard ratio sobre una probabilidad actúan sobre la tasa equivalente, 1 − (1 − p)^m. Un hazard ratio sobre un tiempo lo divide (si no hay distribución, se supone exponencial con aviso). Un odds ratio actúa sobre las odds, y una diferencia de medias se resta. Un tipo relativo siempre opera. Una magnitud del mismo tipo que el parámetro, o `DI_Continuous_Variable`, lo sustituye. Varios efectos se aplican en el orden sustituir, multiplicar, restar, y dos sustituciones son un error. | Comentarios de `Effect` y `hasEffectMagnitude`. Individuos `DI_HazardRatio` y `DI_OddsRatio`. |
| D4 | Una estrategia tiene resultados mutuamente excluyentes: de tratamiento (éxito o fracaso) o de detección (positivo, negativo o no concluyente). Cada resultado lleva su riesgo (en detección, uno por estado del paciente), si resuelve la condición, la estrategia siguiente, sus efectos, sus pagos (de una vez) y su momento de evaluación. `failsIf`, por un lado, y la sensibilidad y la especificidad, por otro, son formas abreviadas. Se rechazan los ciclos: una repetición se modela como otra estrategia. | Jerarquía `StrategyOutcome` (`TreatmentOutcome` con `SuccessOutcome` y `FailureOutcome`; `DetectionOutcome` con sus tres resultados). `hasStrategyOutcome`, `hasOutcomeRisk` (con `hasRiskIfDiseased` y `hasRiskIfNotDiseased`), `hasEvaluationTime`, `followedOnSuccessBy`, `followedOnFailureBy`, `resolvesHealthCondition` y `resolvesOnSuccess`. `followedByStrategy` es la secuencia programada. |
| D5 | Una estrategia puede fracasar cuando aparece una condición de salud. En árboles, el fracaso ocurre en T = MAX(inicio de la estrategia, edad de inicio de la condición − edad inicial); sin edad de inicio, al inicio de la estrategia. La estrategia se paga hasta T y la siguiente empieza en T; si la condición aparece tras la muerte, el fracaso no llega a ocurrir. | Rango de `failsIf`: `Parameter` o `HealthCondition`. |
| D6 | La eficacia la expresa el `DataItemType`: probabilidad por episodio (árbol) o tiempo hasta el evento (simulación). «Resuelve» detiene los pagos anuales de la condición. En árboles, un riesgo de tiempo hasta el evento T se convierte en la probabilidad del evento dentro de un periodo, 1 − e^(−periodo/T), con T modificado por los efectos de la ruta: en una progresión, el horizonte del experimento (sin él, un año, con aviso); en un resultado de estrategia, su tiempo de evaluación o, si falta, el horizonte. El de un efecto adverso se rechaza. | Comentarios de `failsIf`, `hasDuration` y `StrategyOutcome`. |
| D7 | Las utilidades por episodio son pérdidas de AVAC de una vez, que se suman al aplicar una estrategia o un resultado, o al aparecer una manifestación aguda. Los pagos anuales de una aguda se ignoran con aviso. | Comentarios de `appliesOneTime` e `isDisutility`. |
| D8 | Efectos adversos. Si el problema forma parte de la evolución natural de la enfermedad y el tratamiento solo cambia su riesgo, se usa un Effect de la estrategia sobre ese riesgo. Si lo provoca el tratamiento, se declara un AdverseEvent: el acontecimiento (por ejemplo, «hipoglucemia por insulina»), causado por una estrategia, con su riesgo, sus pagos de una vez (D7) y sus estrategias. Apunta al problema de salud, que puede ser una manifestación de la enfermedad o un problema ajeno a ella. Los casos complejos (dosis, combinaciones) se expresan con hasConditionExpression. Puede provocar el fracaso de la estrategia (failsIf, D5) o la muerte. En la primera versión no activa la estructura de la enfermedad: el enlace solo identifica el problema. | Nueva clase AdverseEvent ⊑ HealthCondition, disjunta de Disease y de ClinicalProgressionElement. Nuevas propiedades hasAdverseEvent (de Strategy a AdverseEvent), con su inversa isAdverseEventOf, e isOccurrenceOf (de AdverseEvent a HealthCondition). hasRiskCharacterization admite AdverseEvent, con un riesgo obligatorio. |
| D9 | Recaídas y recurrencias, sin vocabulario nuevo. Una recaída tras la respuesta es el fracaso de una estrategia de seguimiento que sigue al éxito (`followedOnSuccessBy`), con su probabilidad entre quienes respondieron, seguido del tratamiento de la recaída. Los episodios repetidos de una aguda no son estructura del árbol: su frecuencia multiplica los pagos de una vez del episodio por los episodios esperados dentro del horizonte; el DES los simula uno a uno. La secuencia (`hasNext`) queda para la progresión. | Ninguno. Comentarios de `FollowUpStrategy`, `followedOnSuccessBy` y `hasFrequency`. |
| D10 | Si la población no declara edad o sexo, la aplicación avisa al construir el modelo y supone 18 años y un 50 % de mujeres. Si ningún parámetro depende de ellos, el aviso basta. Sustituye el valor por defecto anterior de JaDES (edad 0 con aviso). En el cribado neonatal se suponen 0 años. | Ninguno: las restricciones de `Population` se mantienen. |
| D11 | Las propiedades de detección (`detects`, sensibilidad, especificidad y umbral temporal) son de las estrategias de detección, no de las intervenciones. Las estrategias de diagnóstico son confirmaciones perfectas. | Dominio de `detects`, `hasSensitivity`, `hasSpecificity` y `hasTemporaryThreshold` en `DetectionStrategy` (umbral, en `ScreeningStrategy`); rango de `isDetectedBy` en `DetectionStrategy`. |

**Pendientes de decidir:**

- Qué ocurre con los falsos negativos del cribado: diagnóstico clínico tardío, muerte sin diagnóstico o falsa sensación de seguridad. Hoy no pagan diagnóstico. Bloquea la fase 23.
- Las siete decisiones de la detección en el DES (fase 23), descritas en «Fases pendientes».
- Si se añade otro caso de validación además de Evans (fase 18); el candidato natural es la otitis de Coco (2007), esbozada en P47.
- Los cribados de varias enfermedades (paneles), que no caben porque cada intervención tiene un solo objetivo.
- El objetivo en una manifestación propia de uno o varios desarrollos: qué desarrollos presenta el paciente.

Ya no están pendientes los nombres ni el momento de evaluación de los resultados, que entró en la fase 7a.

## Fases pendientes

Fases acordadas tras la fase 18, en orden: con la 19 se cierra el árbol y se publica OSDi 1.3, validada con árboles de decisión; de la 20 a la 23 se completa el DES. Cada fase termina con la suite de `jades-hta` pasando, sus cambios en commits y su entrada en el registro. Si una fase descubre trabajo no previsto, lo propone como fase nueva en el registro.

Quedan fuera, como trabajo futuro: pautas con subpautas, condiciones o edades de aplicación; pautas en estrategias con resultados; la confirmación imperfecta (estrategias de diagnóstico con resultados explícitos); `hasConditionExpression` en las estrategias; grupos en las cadenas de detección del cribado neonatal; actualizar costes fuera del IPC español de 2002 a 2026; y el análisis de impacto presupuestario.

### Fase 19: traslado a OSDi 1.3

Preparar en `docs/osdi-1.3-traslado.md`: los cambios de la copia de `OSDi.ttl` respecto a OSDi 1.2, agrupados por decisión; `examples/` con las importaciones de la versión 1.3 y un catálogo local; lo pendiente (como los códigos STATO de `DI_HazardRatio` y `DI_OddsRatio`); las notas de la versión 1.3 (validada con árboles; lo que el DES aún no implementa y las divergencias documentadas, como los episodios de un objetivo agudo); y qué copia de la ontología será la de referencia después. Requiere decisión.

### Fase 20: DES, defectos y limitaciones menores

1. En la combinación probabilidad + RR, el RR se ignora si la probabilidad aparece primero (`TimeToEventCalculatorFactory`, rama `PROBABILITY_RR`).
2. El horizonte se redondea a años enteros: 24 horas quedan en 0. Proponer horizontes fraccionarios.
3. Solo se aceptan riesgos `DI_Probability`, `DI_Proportion` y probabilidad con `DI_RelativeRisk`: añadir `DI_Incidence` y `DI_TimeToEvent`, con los efectos de D3.
4. La edad de la población no distingue la heterogeneidad de la incertidumbre paramétrica (fase 8), como ya se hizo con el sexo.
5. Tests que faltan: costes de las tecnologías de una estrategia y sustituciones sobre un desarrollo.

### Fase 21: DES, estrategias con resultados, efectos adversos y recaídas (D4–D9)

Hoy el DES rechaza las estrategias con resultados (`hasStrategyOutcome`, `failsIf`), efectos adversos o pautas. Proponer cómo simularlos (resultados como eventos que compiten, pagos anuales hasta el resultado, estrategia siguiente con grupos, efectos adversos, fracaso por una condición de salud y recaídas como fracaso del seguimiento), de forma que coincida con el árbol en valor esperado cuando el árbol pueda representarlo, e implementarlo por partes. Evans se añade a la validación en el DES.

### Fase 22: DES, desarrollos, muerte de las progresiones y varios pagos

1. Desarrollos y reglas de combinación (propuesta en `docs/known-limitations.md`, «Future work: developments in the DES»), incluido un desarrollo como objetivo, que hoy se rechaza.
2. Probabilidad de muerte de las progresiones (comentada en `OSDiDiseaseProgression`), también la del objetivo en su aparición.
3. Varios costes o utilidades por tipo en condiciones e intervenciones: hoy se usa el primero (con aviso en `OSDiDiseaseProgression` y `OSDiIntervention`, sin aviso en `OSDiDisease`); deben sumarse, como en el árbol y en las estrategias.

### Fase 23: DES, detección y falsos negativos (D11)

Bloqueada hasta decidir qué les ocurre a los falsos negativos (véase «Pendientes de decidir»).

**Situación:**
- `OSDiDetectionIntervention` extiende `OSDiIntervention` y no programa pruebas: en el DES de OSDi no hay eventos `SCREEN`, así que la sensibilidad, la especificidad y los pagos de una vez de las intervenciones de detección no se usan nunca. Tampoco hay diagnóstico clínico, porque el DES no lee la probabilidad de diagnóstico de las progresiones.
- Ya están hechos (fase 14): la estrategia de detección de cada brazo tras la sustitución por tipo, el rechazo de más de una del mismo tipo por brazo y la sensibilidad o especificidad perfectas cuando la estrategia no las define.
- El núcleo de JaDES ya tiene casi todo lo necesario:
  - `ScreeningIntervention` y `DiagnosisIntervention` programan una prueba en el tiempo 0 por paciente no diagnosticado (VP, FP, FN o VN según la sensibilidad y la especificidad de la intervención, `DefinesSensitivityAndSpecificity`), emiten `SCREEN` y, para VP y FP, `DIAGNOSIS`, y marcan al VP como diagnosticado. Las usan `PBDNewbornScreening` y `BasicNewbornScreening`.
  - `ScreeningTestPerformanceListener` cuenta los resultados por intervención.
  - Las estrategias del núcleo (`Strategy`, `ScreeningStrategy`, `DiagnosisStrategy`, `HealthTechnology`) están a medias: la sensibilidad es 0 fuera de la estructura de un nivel y un hijo, `getCostForPeriod` suma el coste de una vez en cada periodo, y ningún listener cobra sus costes.
  - `Disease.getCostWithinPeriod` solo cobra `TREATMENT_COST` y `FOLLOW_UP_COST` a los pacientes diagnosticados.
- Defecto del núcleo: el coste de diagnóstico de un falso positivo es el de `HEALTHY` (0), porque `CostListener` cobra `pat.getDisease().getStartingCost` en `DIAGNOSIS`. Corregirlo afecta también al modelo PBD del núcleo.

**Propuesta (sin aprobar):**
- Reutilizar las intervenciones del núcleo: `OSDiScreeningIntervention extends ScreeningIntervention` y `OSDiDiagnosisIntervention extends DiagnosisIntervention`, con la lógica común de `OSDiIntervention` (efectos, pagos, D1) en una clase auxiliar usada por composición. Sin estrategias del núcleo (`getStrategy() == null`): la sensibilidad y la especificidad siguen siendo parámetros de la intervención, leídos de la estrategia OSDi del brazo.
- Costes, como en el árbol NBS: en `SCREEN`, todos los costes de una vez de la intervención con `ScreeningCat` más los de la estrategia de cribado del brazo; en `DIAGNOSIS`, todos los costes de una vez de la enfermedad con `DiagnosisCat` más los de la estrategia de diagnóstico del brazo. Costes anuales o pautas de una estrategia de detección: `Unsupported`.
- Tests propuestos: D16 (prevalencia 0,1; sensibilidad 0,9 y especificidad 0,95; costes 5 + 10 por prueba y 500 + 100 por diagnóstico: 15 + 600 × [VP o FP] por paciente con cribado, media 96; 600 × [enfermo] sin cribado, media 60), D17 (efectos solo en VP: proporción con ManX 0,9 × 0,427567 + 0,1 × 0,67232 = 0,4520, frente a 0,4276 con efectos para todo el brazo), D18 y D19 (coste anual y pauta en una estrategia de cribado: `Unsupported`).

**Decisiones pendientes:**
1. Reutilizar `ScreeningIntervention` y `DiagnosisIntervention` del núcleo (recomendado) o programar las pruebas en `OSDiDetectionIntervention`.
2. Brazo sin cribado: `DiagnosisIntervention` tal cual, con la sensibilidad y especificidad de la estrategia de diagnóstico (prueba a todos, sanos incluidos; divergencia con el árbol), o diagnóstico perfecto de los enfermos en el tiempo 0, como el árbol NBS. Una tercera vía, la detección clínica a lo largo del tiempo, requiere leer la probabilidad de diagnóstico de las progresiones.
3. Pagos de las estrategias de tratamiento y seguimiento de la enfermedad: hoy (fase 14, D09) se cobran a todos los enfermos desde el tiempo 0; en el núcleo, el tratamiento depende del diagnóstico. Si se ligan al diagnóstico, hay que decidir qué ocurre en un modelo sin intervenciones de detección (p. ej., todos los enfermos diagnosticados en el tiempo 0), y D09 cambiaría.
4. Efectos de una intervención de cribado: hoy se aplican a todo el brazo; en el árbol NBS, solo a los verdaderos positivos. Si se restringen, el resultado de la prueba debe conocerse al planificar los riesgos en el tiempo 0 (la prueba se ejecuta después de `startAction`); se propuso sortearlo una vez por paciente y memorizarlo.
5. Coste de diagnóstico de los falsos positivos (defecto del núcleo): corregirlo en esta fase o documentarlo.
6. Costes de la intervención de detección que no son de `ScreeningCat`: una vez en el tiempo 0, como los de una intervención terapéutica, o ignorados con aviso.
7. Qué ocurre con los falsos negativos (véase «Pendientes de decidir»).

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
- Episodios repetidos en el árbol: se modelarán como recurrencias a largo plazo cuando haya horizonte (D9, fase 11). Hasta entonces, el árbol cuenta un episodio y el DES todos.
- Divergencia: con varios pagos de una vez en una aguda, el DES solo usa el primero (`OSDiDiseaseProgression`), mientras que el árbol los suma.
- El DES no lee `hasFrequency`: el tiempo hasta el siguiente episodio sale solo del riesgo.
- (Resuelto en la fase 13.) N06 comprueba el tipo de excepción, no el mensaje (como todos los casos N).

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
- (Resuelto en la fase 13: P62.) No hay un test del aviso de una intervención sin objetivo (todas las ontologías de prueba lo declaran).
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
- (Resuelto en la fase 13, D5.) El fracaso por una condición de salud ocurre al inicio de la estrategia: no se tiene en cuenta la edad de inicio de la condición.
- Efectos adversos: resuelto en la fase 9 con `AdverseEvent` (D8), que son independientes del resultado de la estrategia salvo que esta tenga `failsIf` hacia el efecto adverso.
- El DES no simula estrategias ni sus resultados.
- (Resuelto en la fase 13.) El aviso de `StrategyWrapper` sobre las estrategias con pauta («Guideline-based strategies are not supported yet») ha quedado desfasado desde la Fase 5. No lo he cambiado porque queda fuera del alcance de esta fase.

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

### Fase 9: efectos adversos (D8) (2026-11-16)

**Hecho:**
- `OSDi.ttl` (sin cambiar la versión):
  - Nueva clase `AdverseEvent`, subclase de `HealthCondition` y disjunta de `Disease` y `ClinicalProgressionElement`. Tiene exactamente un riesgo (`hasRiskCharacterization` hacia un `Parameter`) y como mucho un `isOccurrenceOf`.
  - Propiedades nuevas: `hasAdverseEvent` (de `Strategy` a `AdverseEvent`), con su inversa `isAdverseEventOf`, e `isOccurrenceOf` (de `AdverseEvent` a `HealthCondition`).
  - El dominio de `hasRiskCharacterization` y el rango de `isRiskCharacterizationOf` admiten `AdverseEvent`.
  - El comentario de `failsIf` menciona el abandono por un efecto adverso.
  - Comentarios en inglés y en español, con la regla para quien modela: si el problema forma parte de la evolución natural y la estrategia solo cambia su riesgo, se usa un `Effect`; si lo provoca la estrategia, un `AdverseEvent`.
  - Constantes nuevas en `OSDiClass` y `OSDiObjectProperty`.
- Código:
  - `AdverseEventWrapper` lee y valida el riesgo y la condición de salud.
  - `StrategyWrapper.getAdverseEvents` y `hasAdverseEvents`.
  - `BranchDestinationNode` reúne las estrategias sin resultados que tienen efectos adversos.
  - `StrategyOutcomeExpander` expande los efectos adversos bajo las hojas cada vez que una rama aplica una estrategia, antes de sus resultados, y también tras un resultado y en las estrategias de un efecto adverso.
  - `NBSDetectionArm` rechaza los efectos adversos de las estrategias de detección.
- Tests, con valores calculados a mano:
  - P47: otitis, como en Coco (2007). El antibiótico tiene dos efectos adversos independientes y se da de entrada (sustituye a la observación) o tras su fracaso.
  - P48: abandono por efecto adverso (`failsIf` hacia el efecto adverso), seguido de otro antibiótico.
  - P49: efecto adverso mortal (`InstantDeathEffect`).
  - N31: efecto adverso sin riesgo (`Malformed`).
  - N32: riesgo de tiempo hasta el evento (`Unsupported`).
  - Antes de implementar, P47–P49 fallaron en estructura y resultados (por ejemplo, en P48 `expected: <[TT_P48_A_Failure+TT_P48_Diarrhoea+TT_P48_ManA, TT_P48_A_Success+TT_P48_ManA]> but was: <[TT_P48_A_Success+TT_P48_ManA]>`), y N31 y N32 con `Expected ... to be thrown, but nothing was thrown`.
- Soporte de tests:
  - Prefijo `ADVERSE EVENT: ` en la clave de las hojas.
  - `TreeCase.withAssessedLeaves` declara las hojas del brazo evaluado cuando difieren de las del comparador, como en P47.
- Documentación: Javadoc, `docs/known-limitations.md` (sección «Adverse events in decision trees» y fila de la tabla de conformidad), CLAUDE.md y la fila D8, que pasa de pendiente a implementada.

**Decidido:**
- Nombres: `AdverseEvent`, `hasAdverseEvent` / `isAdverseEventOf` e `isOccurrenceOf`. El riesgo es obligatorio.
- Árbol:
  - Cada efecto adverso es una rama independiente con su probabilidad por aplicación, y otra sin él.
  - Se expanden antes de los resultados de la estrategia. El efecto adverso queda activo en su rama, así que un `failsIf` hacia él decide el fracaso con el mismo mecanismo que D5.
  - Pagos de una vez al inicio de la estrategia; los anuales se ignoran con aviso (D7). Sus efectos se aplican en su rama.
  - Un `InstantDeathEffect` abre una rama de muerte, con esperanza de vida 0 y sin nada expandido después. Si la estrategia empieza más tarde, se avisa.
  - Sus estrategias se aplican en su rama. `isOccurrenceOf` solo identifica la condición.
- Los tests de estructura comprueban que los dos brazos tengan las mismas hojas. En P47 no es así por diseño (la intervención sustituye la estrategia), así que el caso declara las hojas de cada brazo. Esto no cambia ningún valor esperado: en una primera versión, mientras entendía qué brazo se comprueba, llegué a cambiar las hojas esperadas de P47 a las del brazo evaluado, y lo deshice.
- D8 deja de estar pendiente y cubre el uso que se preveía para «una ruta que requiere una estrategia».

**Pendiente:**
- D8 prevé expresar los casos complejos (dosis, combinaciones) con `hasConditionExpression`, pero el árbol no lee esa propiedad en las estrategias (se ignora con aviso).
- La muerte por un efecto adverso ocurre al inicio del modelo aunque la estrategia empiece más tarde (por ejemplo, el rescate tras la observación), igual que la muerte inmediata de la enfermedad.
- DES: no simula estrategias, así que ignora sus efectos adversos. Riesgos de tiempo hasta el evento de los efectos adversos (simulación).
- Efectos adversos de las estrategias de detección en el NBS (por ejemplo, complicaciones de una prueba invasiva).
- Los brazos comparados solo se comprueban en estructura con hojas declaradas para cada uno cuando difieren. El resto de casos sigue suponiendo que las hojas son iguales.

### Fase 10: recaídas tras la respuesta (D9) (2026-11-17)

**Hecho:**
- D9 se sustituyó por la decisión «Recaídas y recurrencias, sin vocabulario nuevo» (commit aparte). La propuesta anterior de una propiedad de recurrencia entre manifestaciones se descartó.
- Comprobación: el árbol ya encadena el éxito de una estrategia (`followedOnSuccessBy`) con una `FollowUpStrategy` con resultados de tratamiento propios, y aplica en su rama los pagos y la estrategia siguiente de esos resultados. La ontología ya lo permitía (`FollowUpStrategy` restringe `hasStrategyOutcome` a `TreatmentOutcome`) y el código no distingue el tipo de estrategia al expandir los resultados. No hizo falta cambiar el código.
- Test P50, con valores calculados a mano: episodio agudo tratado con A (fracaso 0,3 por `failsIf`), cuyo fracaso va seguido de un rescate sin seguimiento y cuyo éxito va seguido del seguimiento F. El fracaso de F es la recaída (0,4; pérdida de 0,1 AVAC de una vez) y va seguido de una segunda dosis sin resultados. Comparador: 67247,4; 65; 54,722. El test pasó a la primera, porque no había defecto que corregir.
- `OSDi.ttl` (sin cambiar la versión): comentarios de `FollowUpStrategy` y `followedOnSuccessBy`, en inglés y en español, con el patrón de recaída. El comentario anterior de `FollowUpStrategy` estaba en inglés pero etiquetado como `@es`.
- Documentación: `docs/known-limitations.md` (sección «Strategy outcomes in decision trees») y CLAUDE.md.

**Decidido:**
- Una recaída tras la respuesta no necesita vocabulario: es el fracaso de un periodo de seguimiento tras el éxito, con su probabilidad entre quienes respondieron.
- Una recurrencia a largo plazo es un nuevo episodio de la misma manifestación aguda, y se modelará en el árbol cuando haya horizonte (fase 11).

**Pendiente:**
- Recurrencias a largo plazo y episodios repetidos de una aguda en el árbol (fase 11).
- Si una recaída necesitara ser un elemento clínico con evolución propia, valorar que una ruta pueda requerir un resultado de estrategia.
- Sin tiempo de evaluación, los resultados de F se evalúan al inicio de la estrategia (con aviso). Para que los pagos anuales del seguimiento duren hasta la recaída, hay que declarar `hasEvaluationTime` en el resultado.

### Fase 11: horizonte temporal del experimento (2026-11-17)

**Hecho:**
- `ExperimentWrapper.getDeclaredTimeHorizon` conserva `hasTimeHorizon` sin redondear; el redondeo a años enteros (y su aviso) queda para el DES. `ControlSheet` escribe el horizonte en el rango `timeHorizon` del Excel, que ya limitaba los años de vida sin descontar de cada hoja (`MIN(LE, timeHorizon)`) y, a través de ellos, los pagos anuales, los AVAC y los pagos de una vez posteriores. Un horizonte no positivo es `Malformed`. Sin horizonte se mantiene la vida entera (150 en la plantilla), así que los casos existentes no cambian.
- Tests P51 (10 años: los años de vida quedan en 10 aunque una manifestación reduzca la esperanza de vida a 55, y una manifestación que empieza a los 15 años no se cobra) y P52 (24 horas, 1/365 años, como en Evans), con valores calculados a mano. Antes del cambio fallaron, por ejemplo, con `Unexpected cost (comparator). Expected 11000.0 but found 75500.0` y `Expected 2.73972602739726 but found 65000.0`.
- Documentación: CLAUDE.md (se quita la limitación del horizonte), `docs/known-limitations.md` (tabla de conformidad) y Javadoc.

**Decidido:**
- El horizonte del árbol es el declarado en el experimento, sin redondear; sin él, la vida entera.

**Pendiente:**
- `hasFrequency` de una aguda en árboles: servirá para sumar costes o desutilidades (episodios esperados), no para cambiar la estructura del árbol. Falta acordar la semántica concreta antes de implementarla.
- El DES redondea el horizonte a años enteros: un horizonte de 24 horas queda en 0.
- Una primera ejecución de los tests previa al cambio falló con errores de clases y recursos no encontrados, porque edité las fuentes mientras se ejecutaban (el IDE recompila `target`). Se repitió sin editar.

**Revisión tras las propuestas (2026-11-17):**
- `OSDi.ttl` (sin cambiar la versión): el comentario de `hasTimeHorizon`, en inglés y en español, explica su efecto en los árboles.
- Decidido: la frecuencia de una aguda en un árbol sirve para sumar costes o desutilidades, no para cambiar la estructura del árbol.
- Decidido: no se modelan episodios repetidos como estructura del árbol con un tope fijado por el generador. Si hiciera falta declarar un máximo de repeticiones, habría que incorporar esa semántica a la ontología; no se considera un caso habitual.
- Riesgos de tiempo hasta el evento con horizonte (revisión de D6, aprobada):
  - Progresiones: 1 − e^(−H/T) con horizonte; sin él, 1 − e^(−1/T) con aviso (P26 no cambia).
  - Resultados: 1 − e^(−τ/T), con τ el tiempo de evaluación o, si falta, H; sin ambos, se rechaza (N24).
  - Efectos adversos: siguen rechazados (N32).
  - `AbstractModelFactory.getProbabilityParameterFromTimeToEvent` y `getTimeToEventProbability` sustituyen a `getSourceTimeToEvent`.
  - Tests P53 (P26 con horizonte de 10 años) y P54 (fracaso con T = 5, evaluado a los 2 años), con valores calculados a mano. Antes del cambio fallaron con `Probability of the leaf 'TT_P53_Normal' in the comparator arm. Expected 0.006737946999085476 but found 0.6065306597126334` y con `The risk of the outcome TT_P54_L1_Failure of the strategy TT_P54_L1 is a time to event, which is not supported yet in decision trees`.
- Pendiente de reanalizar: una aguda que es objetivo de la intervención (el árbol empieza en la crisis) ignorará su frecuencia, con aviso.
- Frecuencia de una aguda (aprobada):
  - No cambia la estructura del árbol. El riesgo es la probabilidad de tener episodios; sin riesgo, la aguda está siempre presente en su contexto (N06 pasa a ser P56).
  - Los pagos de una vez cuyo dueño es el nodo de la aguda (los suyos, los de sus estrategias y los de sus resultados y efectos adversos) se multiplican por los episodios esperados: cada pago de una vez x pasa a ser un pago anual f·x dentro de la ventana del nodo, limitada por los años de vida de la hoja (y, por tanto, por el horizonte), y sigue sumándose siempre como pago de una vez.
  - Si la aguda es el objetivo de la intervención, la frecuencia se ignora con aviso (marcado para reanalizar).
  - `BranchDestinationNode.getEpisodeFrequency`, `DiseaseSubTreeGenerator.getEpisodeFrequency` (sustituye a `checkAcuteManifestation`) y `PayoffWrapper.applyDiscount`.
  - Tests, con valores calculados a mano: P55 (riesgo y frecuencia, con estrategia, efecto adverso y horizonte de 10 años), P56 (solo frecuencia, horizonte de 5 años; antes N06) y P30. P30 comprobaba que la frecuencia se ignoraba; con la nueva semántica sus datos (12 episodios al año de 0,5 AVAC durante 65 años) darían AVAC negativos, así que se rediseñó antes de implementar: frecuencia 0,1 al año y nuevas descripciones (comparador 67600; 65; 54,6).
  - Antes del cambio fallaron con `Unexpected cost (comparator). Expected 67600.0 but found 65400.0` (P30), `Expected 11326.0 but found 10044.2` (P55) y el rechazo «defines a frequency … but no risk» (P56).
- `OSDi.ttl` (sin cambiar la versión): el comentario de `hasFrequency`, en inglés y en español, describe su uso en los árboles.
- Pendiente: los efectos de una aguda (muerte, LER) no se multiplican por los episodios.

### Fase 12: Evans et al. (1997), sumatriptán frente a cafeína/ergotamina (2026-11-19)

**Hecho:**
- Comprobaciones previas: una pérdida de AVAC negativa en un resultado se acepta sin recortes (clon temporal de P43 con −0,5: 55,45 AVAC); el árbol no admitía un grupo como estrategia siguiente (no hay wrapper de `Group` ni se lee `hasProportionWithinGroup`); la tabla CPI de la plantilla solo cubre 2002–2026 (IPC español), así que un coste de 1997 daba `#N/A`; OSDi no tenía dólar canadiense.
- Costes de 1997 en dólares canadienses: `Currency_CanadianDollar` en `OSDi.ttl` (sin cambiar la versión) y en `OSDiDataItemType`; un coste del año del estudio no se actualiza con el IPC (`ParamSheet.updateCost`; su factor ya era 1, así que los casos existentes no cambian). Test P57 (costes en CAD de 1997), que antes fallaba con `The cell with the cost (assessed) does not contain a number: #N/A`.
- Grupo de estrategias alternativas como estrategia siguiente (aprobado): `NextStrategy` lee una estrategia o un grupo (`Group` con `hasGroupItem`) cuyos miembros declaran `hasProportionWithinGroup`; como mucho un miembro sin proporción, que se lleva el resto. Lo usan `StrategyWrapper.getOutcomes` (resultados, forma abreviada y `followedByStrategy` de la estrategia), `StrategyOutcomeWrapper` y `StrategyOutcomeExpander` (una rama `STRATEGY: …` por miembro). No se registra un wrapper para `Group`, porque las sustituciones y el DES identifican los grupos con el wrapper genérico. En las cadenas de detección del NBS se rechaza (`DetectionStrategyWrapper.requireSingle`). Comentario de `hasProportionWithinGroup` en `OSDi.ttl` (sin cambiar la versión).
  - Tests P58 (grupo {R1: 0,7; R2: el resto, con sus resultados} tras el fracaso de un tratamiento; comparador 67082; 65; 55,21), N33 (grupo sin proporciones, `Unsupported`) y N34 (proporciones que suman 0,9 sin miembro por defecto, `Malformed`). Antes del cambio fallaron con `…TT_P58_Rescue should belong to the working model and have a wrapper of type StrategyWrapper, but its wrapper is of type GenericWrapper` y con `IllegalStateException` en N33 y N34. En la primera ejecución tras implementarlo, N34 no fallaba porque leía la proporción sin inicializar su parámetro; se corrigió.
  - La proporción es una propiedad de la estrategia: una estrategia que pertenece a varios grupos tiene la misma proporción en todos.
- Caso de integración I05 (`OSDi_test_tree_scenarios.ttl`): Evans et al. (1997), sumatriptán frente a cafeína/ergotamina para la crisis de migraña.
  - Representa el conocimiento: migraña (`Disease`) y crisis (`AcuteManifestation`), objetivo de las dos intervenciones (`isInterventionOf`; la migraña no declara `hasIntervention`, que le daría un segundo objetivo). Cada intervención sustituye la estrategia de la crisis por su fármaco (`StrategyReplacementEffect`). El fracaso del fármaco (pérdida de 1,3/365) va seguido del grupo de rescate {casa: 0,92; urgencias: 0,08, coste 63,16}; el alivio en urgencias (pérdida de −0,4/365) resuelve la crisis y su fracaso (0,002) va seguido del ingreso (1093). El alivio del fármaco resuelve la crisis y va seguido de un seguimiento sin coste cuyo fracaso es la recaída (0,406 o 0,297; pérdida de 0,1/365), seguida de otra dosis.
  - Horizonte de 1/365 años, sin descuento, utilidad basal 1, costes en dólares canadienses de 1997 (año del estudio).
  - Valores calculados a mano: coste 4,71497244 (cafeína/ergotamina) y 22,05805736 (sumatriptán); AVAC 0,201275956/365 y 0,416860912/365; RCEI 29 363 CAD/AVAC (29 366 en Evans et al., por redondeo). Las hojas de cada brazo se comprueban por separado (`withAssessedLeaves`).
  - Supuestos de Evans que quedan explícitos (en las descripciones de los individuos): tras el alivio en urgencias no hay seguimiento, así que no hay recaída; la dosis de la recaída siempre alivia (estrategia sin resultados); quedarse en casa, urgencias, el ingreso y la segunda dosis no tienen pérdida de AVAC propia; la utilidad basal es 1.
  - Decisiones de modelado: la segunda dosis es otra estrategia (`SumatriptanSecondDose`, `CafergotSecondDose`) con el mismo parámetro de coste que el fármaco, porque los árboles rechazan los ciclos; los resultados no declaran tiempo de evaluación (se evalúan al inicio de la estrategia, con aviso), lo que no cambia el resultado con un horizonte de 24 horas.
  - La población no declara edad ni sexo (el árbol supone edad 0 con aviso), pero sí una esperanza de vida (50 años), que no influye en el resultado: sin ella, el Excel no se podía evaluar (véase el defecto pendiente).

**Decidido:**
- Una pérdida de AVAC negativa en un resultado es una ganancia y se acepta tal cual.
- Un grupo de estrategias como estrategia siguiente es un reparto entre alternativas, con las proporciones de sus estrategias; un grupo sin proporciones sigue significando «todas a la vez» y solo vale en las sustituciones.
- Un coste del año del estudio no se actualiza con el IPC.

**Pendiente:**
- Defecto encontrado (sin corregir, fuera del alcance): si la población no declara esperanza de vida, `SimpleModel` crea un parámetro sintético (`FakePopulationAge`, 100 años) que no se escribe en el Excel, y la evaluación falla con `Specified named range 'FakePopulationAge' does not exist in the current workbook`. Encaja con la fase 13 (D10, valores por defecto de la población).
- La tabla CPI de la plantilla es el IPC español de 2002 a 2026: no sirve para actualizar costes de otros años ni de otras monedas.
- Grupos de estrategias en las cadenas de detección del NBS.

### Fase 13: pendientes menores del árbol (2026-11-19)

**Hecho:**
- D10 (aprobado): si la población no declara edad o sexo, se avisa y se suponen 18 años y un 50 % de mujeres (`PopulationWrapper.DEFAULT_AGE` y `DEFAULT_PROPORTION_OF_WOMEN`).
  - Árbol: la edad interpreta las edades de inicio y fin (`ModelSheet`, `Model.getDefaultInitialAge`); el sexo no se usa, así que basta el aviso. Excepción aprobada: el cribado neonatal supone 0 años (`NBSModel`).
  - DES: `OSDiPopulation` supone una edad constante de 18 años y un sexo Bernoulli(0,5), en lugar de rechazar el modelo.
  - Ningún test existente cambia de valor esperado: la única población de prueba sin edad ni sexo es la de I05, donde nada depende de la edad.
  - Tests nuevos: P59 (ManY desde los 30 años con Age0 = 18: comparador 72625; con la edad 0 anterior daba 70375, valor con el que falló antes del cambio) y D02 en el DES (`TestDESPopulationDefaults`: edad inicial 18 y 0,5 ± 0,02 de mujeres; antes fallaba con `Population TD_D02_Population does not have a valid age parameter`).
- Defecto de la esperanza de vida (fase 12; commit aparte): si la población no la declara, el árbol crea un parámetro sintético de 100 años que no se escribía en el Excel, porque `ModelWrapper` construía su lista de parámetros una sola vez. `ModelWrapper.registerSyntheticModelItem` añade ahora los parámetros sintéticos a esa lista, y el parámetro se llama `DefaultLifeExpectancy` (antes `FakePopulationAge`). Test P60 (comparador 100000; 100; 85), que antes fallaba con `Specified named range 'FakePopulationAge' does not exist in the current workbook`.
- (Resuelto en la fase 13.) Pendiente (encontrado al corregir el defecto anterior, sin corregir): si la población no declara utilidad basal, `SimpleModel.getBaseUtility` y `NBSModel.getBaseUtility` crean un parámetro sintético y después toman el primer elemento del conjunto vacío (`NoSuchElementException`); además, una segunda llamada intentaría registrarlo de nuevo.
- D5 (aprobado): el fracaso por una condición de salud (`failsIf`) ocurre cuando aparece la condición, en T = MAX(inicio de la estrategia, edad de inicio de la condición − edad inicial). La estrategia se paga hasta T, los pagos del fracaso se cobran en T y la siguiente estrategia empieza en T; si la condición aparece tras la muerte, las ventanas lo recortan y el fracaso no llega a ocurrir. Sin edad de inicio (o con un efecto adverso como condición), el fracaso ocurre al inicio de la estrategia, como antes. Se implementa con una marca «no antes de la edad X» en las listas de tiempos (`BranchDestinationNode.notBeforeAge`), que `PayoffWrapper.addTimes` traduce en MAX(tiempo, X − Age0). Ningún test existente cambia (P45 usa una condición sin edad de inicio). Test P61 (P45 con DevA desde los 40 años: comparador 87950; 62; 51,65), que antes daba 89150 (fracaso al inicio).
- Aviso desfasado de `StrategyWrapper` («Guideline-based strategies are not supported yet. They will be ignored»): se quita, igual que la frase del Javadoc de la clase, porque las pautas están soportadas en los árboles desde la fase 5 y sus casos no soportados se rechazan con su propia excepción. Nueva utilidad de tests `LogCapture` (captura los mensajes de JaDES con un appender de Log4j 2) y nueva clase `TestDecisionTreeWarnings`; su test comprueba que P40 (la única estrategia con pauta propia) no produce el aviso, y antes del cambio fallaba con él.
- Test del aviso de una intervención sin objetivo: P62 (la intervención evaluada no declara objetivo y se supone que es la enfermedad; comparador 65000, evaluado 71500) y `TestDecisionTreeWarnings.warnsAboutInterventionWithoutTarget`, que comprueba el texto del aviso. Pasó a la primera, porque el aviso ya existía: el punto añade cobertura.
- Los casos N comprueban también el mensaje de la excepción (`MalformedCase.expectedMessage`, un fragmento estable del mensaje). N06 ya no existía (pasó a ser P56 en la fase 11), así que se aplica a los 32 casos actuales. Antes de fijar los fragmentos se extrajo el mensaje real de cada caso y se revisó que corresponde al motivo que el caso quiere probar: todos lo hacen. Se comprobó que un fragmento falso hace fallar el test (N01).
- (Resuelto en la fase 13.) Pendiente (menor, sin corregir): el mensaje de N08 muestra los efectos como `EffectWrapper@…`, porque `EffectWrapper` no tiene un `toString` legible.
- `BD.ttl` importa OSDi 1.2 en lugar de 1.0, y se quitan sus redeclaraciones locales de `affects`, `hasEffect`, `hasEffectMagnitude` (como propiedades de anotación, que chocaban con el núcleo) y de las clases `Effect`, `InstantDeathEffect` y `LifeExpectancyReductionEffect`. No tiene experimentos (está a medias), así que no había resultados que conservar ni se añaden tests: con experimentos creados en memoria, sus tres modelos (`PBD_ModelTree`, `PBD_ModelTreeDebug`, `PBD_ModelTreeFake`) se construyen y dan resultados.
- Utilidad basal por defecto: si la población no la declara, se supone 1, con aviso. `SimpleModel` y `NBSModel` crean una sola vez el parámetro sintético `DefaultBaseUtility` (utilidad anual, no desutilidad) al construir el modelo, para que se escriba en el Excel. Antes, `getBaseUtility` fallaba con `NoSuchElementException`. Test P63 (comparador 65000; 65 años de vida; 65 AVAC).
- `EffectWrapper.toString` devuelve el nombre del efecto: los mensajes que listan efectos (p. ej., N08) son legibles.

### Fase 14: el DES y el objetivo y las estrategias de una intervención (2026-11-19 a 2026-11-23)

**Hecho:**
- Refactorización sin cambios de comportamiento (commit 0): `InterventionTargets.resolve` (objetivo común de las intervenciones, con los mismos rechazos y aviso) y `StrategyReplacements` (`of` y `replaceByType`) pasan al paquete `osdi/ontology`, para que el DES use las mismas reglas que el árbol. El rechazo de etapas y manifestaciones propias de un desarrollo se queda en el generador del árbol.
- Pagos de una vez de las intervenciones en el DES (commit 1, defecto previo): el DES nunca aplicaba el coste ni la utilidad de una vez de una intervención que no fuera de detección (los listeners solo los usaban en `SCREEN`). Ahora se aplican una vez, al inicio de la simulación (`Intervention.appliesOneTimePayoffsAtStart`); las de detección los siguen aplicando con cada prueba. Los listeners de costes tenían `START_MANIF` sin `break` hasta `START`; se separan los casos. Test D03 (`TestDESInterventionPayoffs`): 500 por paciente y −0,05 AVAC por paciente; antes fallaba con `expected: <500.0> but was: <0.0>` y `expected: <-0.05> but was: <0.0>`.
- D1 en el DES (commit 2; la primera propuesta, que activaba la intervención en la aparición del objetivo, se rechazó: eso corresponde a un análisis de impacto presupuestario). `DESInterventionTarget` resuelve el objetivo y comprueba la población; `OSDiDESModel` lo conoce antes de construir la enfermedad.
  - T crónica: proporción inicial 1 (sintética, con aviso, si la población no la declara; `OSDiDiseaseProgression`), así que `startAction` la pone en el estado en el tiempo 0 y emite `START_MANIF` (pagos de una vez de T en el tiempo 0, como el árbol, que los aplica en el tiempo 0 si T no tiene edad de inicio: I03).
  - T aguda: rutas sintéticas (`TargetEpisodePathways`): un episodio en el tiempo 0 para quien aún no ha tenido ninguno y los siguientes de la frecuencia (tasa exponencial) o, si no hay, del riesgo. Pagos de una vez de la intervención en cada episodio (`Intervention.appliesOneTimePayoffsOn`, en los cuatro listeners); anuales ignorados con aviso (`OSDiIntervention`).
  - Tests D04–D08 (`TestDESInterventionTarget`), con valores a mano: D04 (todos con ManX en el tiempo 0; coste por paciente 200 + 1000 o 1100 × años vividos; ManY desde ManX: 1 − 0,8^5 = 0,67232; aviso de la proporción supuesta), D05 (proporción inicial 0,5: `Malformed`), D06 (episodio en el tiempo 0; episodios medios 1 + 5 ln 2 = 4,4657 y, con HR 0,5, 1 + 2,5 ln 2 = 2,7329; coste 2000 o 2300 por episodio; aviso de los pagos anuales), D07 (frecuencia 2/año: 11 episodios) y D08 (desarrollo: `Unsupported`). Antes del cambio fallaron los 8 (p. ej., `expected: <5200.0> but was: <3079.45…>`, `Mean episodes ==> expected: <11.0> but was: <0.0>`, y D05 y D08 sin excepción).

**Decidido:**
- En un análisis de coste-efectividad o coste-utilidad, el tiempo 0 es el momento en que se aplica la intervención: no se activa a mitad de la simulación, y no hace falta replanificar eventos. La población describe a los pacientes en ese momento.
- Con T aguda, los pagos de una vez de la intervención se aplican en cada episodio (cada episodio se trata). El árbol, que representa un episodio, los aplica una vez.
- `hasFrequency` solo se lee en el DES para una T aguda; si T declara también un riesgo, el riesgo se ignora con aviso.
- En el DES se rechazan como objetivo los desarrollos, las etapas, las manifestaciones propias de un desarrollo y los objetivos con restricción temporal.

**Pendiente:**
- El árbol mantiene la restricción temporal (edad de inicio o fin) del nodo objetivo, lo que contradice que todos presenten T en el tiempo 0.
- (Pasa a la fase 15.) `OSDiDetectionIntervention` no programa pruebas: en el DES de OSDi no hay eventos `SCREEN`, así que la sensibilidad, la especificidad y los pagos de una vez de las intervenciones de detección no se usan nunca.
- (Resuelto en la fase 14, D2: D10.) El DES no simula las estrategias de tratamiento: la estrategia sustituida no se aplica en cada episodio de una T aguda.
- El DES no lee la probabilidad de muerte de las progresiones (tampoco la de T en su aparición).
- (Hecho en la fase 14.) El texto de D1 («se aplica en cada aparición del objetivo») debería precisarse con la semántica del tiempo 0. Se cambió por «la intervención se aplica en el tiempo 0, cuando los pacientes presentan el objetivo; si es una manifestación aguda, en cada episodio», pero esta semántica habrá que revisarla más adelante.
- (Hecho en la fase 14.) Punto 2 de la fase (D2: sustituciones en el DES).
- (Pasa a la fase 15.) Punto 3 de la fase (costes de las estrategias de detección), que necesita que `OSDiDetectionIntervention` programe pruebas.

**Hecho (continuación):**
- Defecto previo (commit aparte): `OSDiDetectionIntervention` suponía sensibilidad o especificidad perfectas con `addUsedParameter(plantilla, descripción, fuente, 1.0)`, que solo añade el parámetro si estaba registrado; no lo estaba, así que el valor supuesto se perdía y quedaba `NaN`. Ahora se registran en el constructor. Test D12 (`TestDESDetection`): la especificidad del brazo sin cribado, cuya estrategia de diagnóstico no la define, es 1; antes fallaba con `expected: <1.0> but was: <NaN>`.
- D2 en el DES (aprobado con las recomendaciones):
  - `DESStrategyPayoffs` calcula, al construir el modelo, las estrategias de cada condición en cada brazo con `StrategyReplacements.replaceByType` y reúne los pagos de las de tratamiento y seguimiento (categorías `TreatmentCat` o `FollowUpCat`, como el árbol): costes propios, costes de sus tecnologías y utilidades, todos.
  - `OSDiDisease` y `OSDiDiseaseProgression` suman esos pagos a los suyos con el calendario de la condición. Para los pagos de una vez de la enfermedad en el tiempo 0 se añaden `Disease.getStartOfSimulationCost` y `getStartOfSimulationDisutility` (0 por defecto), que los cuatro listeners usan en `START`.
  - `OSDiDetectionIntervention` toma la estrategia de detección del brazo tras la sustitución por tipo (antes cogía la primera de las sustituciones o de cualquier enfermedad); más de una del mismo tipo es `Unsupported`.
  - `OSDiIntervention` ya no avisa de que ignora las sustituciones.
  - Se rechazan (`Unsupported`) las estrategias de tratamiento o seguimiento con resultados (`hasStrategyOutcome`, `failsIf`), efectos adversos o pautas (de la estrategia o de sus tecnologías). Las sustituciones de un desarrollo se ignoran con aviso.
  - Tests nuevos, con valores a mano: D09 (coste por paciente 100 + 350 × años vividos y 400 + 850 × años vividos; diferencia de AVAC −0,1 × años vividos), D10 (2100 y 2400 por episodio; aviso de los pagos anuales de la estrategia), D11 (100 + 20 × (años vividos − aparición de ManX)), D12 (sensibilidad 0,9 y especificidad 0,95 en el brazo con cribado; 0,7 y 1 en el otro), D13 (dos estrategias de cribado: `Unsupported`), D14 (resultados: `Unsupported`) y D15 (pauta: `Unsupported`). Antes del cambio fallaron todos menos las sensibilidades de D12, que ya se leían bien (p. ej., `expected: <1850.0> but was: <0.0>`, `expected: <-0.5> but was: <0.0>`).
  - Nueva utilidad de tests `DESTestSupport` (simula los dos brazos con listeners de costes y AVAC), que también usa `TestDESInterventionTarget`.

**Decidido (continuación):**
- Las estrategias con resultados, efectos adversos o pautas se rechazan en el DES en lugar de aplicar solo sus pagos propios: no se dan resultados engañosos.
- Los pagos de las estrategias siguen el calendario de su condición, sin ventanas propias.

**Pendiente (continuación):**
- Sin test: costes de las tecnologías de una estrategia en el DES y sustituciones sobre un desarrollo.

**Cierre (2026-11-23):**
- Commits: refactorización del objetivo y las sustituciones (commit 0), pagos de una vez de las intervenciones, D1 en el DES, texto de D1, registro de la sensibilidad y especificidad, y D2 en el DES. Suite final: 340 tests, 0 fallos, 1 omitido.
- El punto 3 (costes de las estrategias de detección) no se implementó: al plantearlo se vio que el núcleo de JaDES ya tiene intervenciones de cribado y diagnóstico con pruebas, y que hay decisiones previas. Pasa a la fase 15 (véase «Fases pendientes»), con el resto de lo que quedó abierto.

### Fase 15: semántica del objetivo en el árbol y sincronización (D1, D9) (2026-11-24)

**Hecho:**
- Sincronización del diseño: los textos de D1 y D9 y la sección «Fases pendientes» se sustituyen por los del plan de fases 15 a 23 (la detección en el DES pasa a ser la fase 23, con su situación, su propuesta y sus siete decisiones pendientes). Al aprobarse el punto 2, D1 dice que tampoco pueden ser objetivo los que tienen restricción temporal, en el árbol y en el DES.
- Objetivo agudo (commit `db77c84`): el árbol evalúa un único episodio, el del tiempo 0, e ignora la frecuencia del objetivo con un aviso que lo dice («…which is ignored: the decision tree evaluates a single episode, the one that every patient suffers at time 0 (a discrete event simulation also simulates the next episodes).»). Divergencia aceptada con el DES, que simula también los episodios siguientes y trata cada uno; documentada en CLAUDE.md y `docs/known-limitations.md`. Se quita el TODO de `DiseaseSubTreeGenerator.getEpisodeFrequency`.
  - Test P64 (P56 con ManA como objetivo de dos intervenciones propias; horizonte de 5 años): comparador 5 × 1000 + 300 = 5300, evaluado + 50 de una vez = 5350; 5 años de vida y 4,25 AVAC. Con la frecuencia serían 8000 y 8050. Test del aviso en `TestDecisionTreeWarnings`, que antes del cambio falló con el texto anterior («…which is ignored, since it is the target of the interventions: the tree starts with an episode.»). Los resultados de P64 ya pasaban: la frecuencia ya se ignoraba.
- Objetivo con restricción temporal (aprobado; commit `2027dd4`): el árbol lo rechaza (`Unsupported`, `DiseaseSubTreeGenerator.resolveInterventionTarget`), como el DES, porque todos presentan el objetivo en el tiempo 0. Ningún test existente cambia (ningún objetivo de las ontologías de prueba tiene edad de inicio o fin). Test N35 (ManX desde los 30 años como objetivo), que antes del cambio falló con «Expected UnsupportedOSDiFeatureException to be thrown, but nothing was thrown». Se quita la divergencia de CLAUDE.md y el TODO de `docs/known-limitations.md`.
- `OSDi.ttl` (aprobado; commit `feffb5b`, sin cambiar la versión): el comentario de `hasIntervention`, en inglés y en español, dice lo mismo que D1: el tiempo 0 es el momento en que se aplica la intervención; todos los pacientes presentan el objetivo y la intervención está activa toda la simulación; con un objetivo agudo, todos empiezan con un episodio. Se quitan «se aplica en cada aparición», las etapas como objetivo y la frase sobre el árbol. Lo propio de JaDES (episodios en árbol y DES, rechazos) queda en el Javadoc y en `docs/`. El de `isInterventionOf` no cambia: remite a `hasIntervention`.
- Suite final: 345 tests, 0 fallos, 1 omitido.

**Decidido:**
- Con un objetivo agudo, el árbol evalúa un único episodio y el DES todos: divergencia aceptada.
- Un objetivo con restricción temporal se rechaza en el árbol y en el DES.

**Pendiente:**
- El aviso de la frecuencia de un objetivo agudo se emite dos veces por modelo (ya ocurría antes).
- El objetivo en una manifestación propia de uno o varios desarrollos sigue en «Pendientes de decidir».
- Maven necesita `JAVA_HOME` apuntando a un JDK 25 (el del entorno es el 21), como dice CLAUDE.md.

### Fase 16: defectos del árbol con resultados erróneos (D7, D8) (2026-11-24)

**Hecho:**
- Muerte por un efecto adverso (commit `f67607f`): ocurría al inicio del modelo aunque la estrategia empezara más tarde. El nodo de muerte guarda ahora el inicio T de la estrategia (`BranchDestinationNode.getDeathTime`) y la esperanza de vida de su hoja es MIN(T, la de la hoja sin la muerte) (`PayoffWrapper.getUndiscountedLEFormula`): los años de vida y los pagos se recortan en T. Se quita el aviso. P49 (T = 0) no cambia.
  - Test P65: L1 (20) fracasa con 0,4, evaluado a los 10 años, seguido de R (200), cuya anafilaxia (0,05; 500) es mortal con 0,2. Muerte (0,004): 10 años de vida, 10 × 1000 + 20 + 200 + 500 = 10720, 8,5 AVAC. Comparador 64890; 64,78; 55,063; evaluado 71368. Antes falló con `Unexpected cost (comparator). Expected 64890.0 but found 64847.2` y `Expected 64.78 but found 64.74`.
- Agudas con frecuencia y efectos sobre la esperanza de vida (aprobado; commit `4fd0425`): se rechazan (`Unsupported`, `DiseaseSubTreeGenerator.getEpisodeFrequency`) si la frecuencia se usa y la aguda tiene una muerte inmediata, una reducción de la esperanza de vida o una tasa de mortalidad aumentada. Si la aguda es el objetivo, la frecuencia se ignora y no se rechaza. Ningún test existente cambia. Test N36 (frecuencia 2 y LER 1), que antes falló con «Expected UnsupportedOSDiFeatureException to be thrown, but nothing was thrown».
- Defecto encontrado y corregido (aprobado; commit `cde76d4`): la muerte inmediata de una progresión se añadía como efecto de su nodo, así que toda hoja con la progresión tenía esperanza de vida 0, sin tener en cuenta la probabilidad ni la edad de inicio. Ahora el nodo la guarda aparte (`BranchDestinationNode.updateEffectsOfProgression`) y `ProgressionDeathExpander` abre bajo cada hoja donde está activo una rama de muerte con la probabilidad de la magnitud, en la aparición de la progresión (MAX(0, edad de inicio − Age0), o 0), y otra de supervivencia. `StrategyOutcomeExpander` no expande nada bajo esas muertes. La muerte de la enfermedad no cambia (S10, I02).
  - Test P66: ManX (0,2) desde los 40 años, 150 al año, muerte con 0,1 en su aparición (T = 20). Comparador 65315; 64,1; 54,485; evaluado 71725. Antes falló con `Unexpected life years (comparator). Expected 64.1 but found 52.0`.
- Documentación: Javadoc, CLAUDE.md y `docs/known-limitations.md` (sección nueva «Instant death of a progression in decision trees»).
- Suite tras los tres commits: 349 tests, 0 fallos, 1 omitido.

**Decidido:**
- La muerte por un efecto adverso ocurre al inicio de su estrategia, y la de una progresión, en su aparición; si el paciente muere antes, no llega a ocurrir.
- No se multiplican por los episodios los efectos de una aguda sobre la esperanza de vida: la combinación con la frecuencia se rechaza. Se descartó la probabilidad de morir en alguno de los episodios esperados, 1 − e^(−λD) con λ = −f·ln(1 − p), porque la probabilidad de la rama dependería de los años de vida de la hoja.

**Pendiente:**
- (Resuelto en la fase 16: véase «Revisión».) Muerte inmediata del objetivo.
- El DES no lee la muerte inmediata de las progresiones (fase 22).

**Revisión (2026-11-24):**
- Decidido: con un objetivo que es un desarrollo o una manifestación crónica, su muerte inmediata se ignora con aviso, como la de la enfermedad: todos presentan el objetivo en el tiempo 0, así que ya sobrevivieron a su aparición. Con un objetivo agudo, el episodio ocurre en el tiempo 0, dentro del modelo, y su muerte se aplica entonces.
- `DiseaseSubTreeGenerator.generateTargetSubTree` descarta las muertes del nodo objetivo con aviso. Test P67 (ManX crónica con muerte 0,1 como objetivo: comparador 74750; 65; 55,25; evaluado 81250) y su aviso en `TestDecisionTreeWarnings`. Antes falló con `Unexpected life years (comparator). Expected 65.0 but found 58.5` y una hoja `DEATH` de más.
- Test P68 (ManA aguda con muerte 0,1 como objetivo, 300 por episodio y 50 de una vez de la intervención): muerte en el tiempo 0 con 0 años de vida y solo los pagos de una vez; comparador 58800; 58,5; 49,725; evaluado 58850. Pasó a la primera: añade cobertura.
- Suite final: 354 tests, 0 fallos, 1 omitido.

### Fase 17: consistencia con un razonador completo (2026-11-24)

**Hecho:**
- Test `TestOntologyConsistency` (commit `0547d5d`): carga con HermiT 1.4.5.519 (ya era dependencia; `ignoreUnsupportedDatatypes` por OWL-Time) `OSDi.ttl` y todas las ontologías de prueba, `BD.ttl` y `PBD.ttl` incluidas, tal como están escritas (sin el `OSDiWrapper`). Comprueba que son coherentes y que el razonador no deduce que dos enfermedades distintas sean el mismo individuo. `OSDi_test_tree_malformed.ttl` se comprueba caso a caso.
- Primera ejecución: `OSDi.ttl` y ocho ontologías eran coherentes; `OSDi_test_tree_malformed.ttl`, `OSDi_test_tree_payoffs.ttl` y `BD.ttl`, no. owlexplanation 5.0.0 falló con un `NullPointerException` interno tras 5 a 12 minutos por ontología, así que las causas se localizaron comprobando cada modelo por separado y reduciendo sus asertos a un conjunto mínimo incoherente. Las intervenciones compartidas no eran la causa de ninguna incoherencia: sin sus objetivos, las tres seguían siendo incoherentes.
- A (aprobado; commit `11ff4f7`): defecto de `OSDi.ttl` de la fase 13. `causesFailOf`, inversa de `failsIf`, seguía con dominio `Parameter` y rango `LineOfTherapy`, así que el rango efectivo de `failsIf` era `Parameter` y un `failsIf` hacia una condición de salud (P45, P61) o un efecto adverso (P48) hacía la ontología incoherente. Ahora su dominio es `HealthCondition` o `Parameter`, su rango `Strategy` y su comentario, en inglés y en español, remite a `failsIf`. Sin cambiar la versión.
- B (aprobado): N15 (una intervención con dos objetivos disjuntos), N20 y N22 (una intervención de cribado con `hasSensitivity` o `detects`, cuyo dominio es `DetectionStrategy`, disjunta de `Intervention`) son incoherentes a propósito: el razonador detecta el error que JaDES rechaza. El test exige que sean exactamente esos tres.
- C (aprobado; commit `a3b8490`): los modelos de `BD.ttl` declaraban `hasDisutilityCombinationMethod` y `PBD_ModelTree` también `hasYear`, cuyos dominios no incluyen `Model`. Se quitan; `hasYear` apareció al volver a comprobar tras quitar el primero.
- Punto 1 (aprobado; commit `0702c6c`): `TT_InterventionTreat` y `TT_InterventionNone` se compartían en 114 casos de cinco ontologías de árbol; una intervención tiene exactamente un objetivo, así que HermiT deducía que todas las enfermedades de cada fichero eran el mismo individuo. Un script crea en cada caso `TT_<caso>_InterventionTreat` (con `TT_TreatAnnualCost`) y `TT_<caso>_InterventionNone`; las intervenciones que ya eran propias se conservan. Todas las enfermedades ya declaraban su objetivo con `hasIntervention`, así que no cambia ningún aviso. Ningún valor esperado cambia.
- La comprobación de enfermedades iguales encontró el mismo error en `OSDi_test.ttl` (aprobado; commit `d458d7f`): sus cuatro modelos compartían `TEST_InterventionEffective` y `TEST_InterventionIneffective`. El modelo 1 conserva los nombres; los modelos 2 a 4 tienen `TEST_InterventionEffective<n>` y `TEST_InterventionIneffective<n>`, con el mismo coste y efecto. Solo cambian los nombres esperados de `TestSingleDiseaseAndPopulationDecisionTreeModelGeneration`; los valores, incluida la regresión del Excel, no.
- Antes de los cambios, la comprobación de enfermedades iguales falló en `OSDi_test.ttl` y en las cinco ontologías de árbol (p. ej., `OSDi_test_tree_scenarios.ttl: the reasoner infers that different diseases are the same individual: [TT_I01_Disease = TT_I02_Disease]`).
- Documentación: CLAUDE.md (convenciones de los casos, test nuevo, notas técnicas) y `docs/known-limitations.md`.
- Suite final: 366 tests, 0 fallos, 1 omitido.

**Decidido:**
- Cada modelo de prueba tiene sus propias intervenciones; los demás individuos (población, costes, efectos) se pueden compartir.
- Los casos malformados incoherentes a propósito se mantienen, y el test los enumera.

**Pendiente:**
- El test no cubre las ontologías que se creen fuera de `src/test/resources` (p. ej., los ejemplos de la fase 18): convendrá añadirlas.
- Algunas progresiones se comparten entre enfermedades de `OSDi_test.ttl` (p. ej., `TEST_CoexistentDevelopment1`); no producen incoherencia, pero si OSDi restringiera algún día `hasProgressionElement`, habría que separarlas.

### Fase 18: ejemplos de validación (2026-11-24)

**Hecho:**
- `genExcel` de `OSDiMain` (punto 4; commit `81eebb3`): no registraba las factorías de árboles, y `OntologyLoader.getOntologyDocumentSourceFromString` trataba como IRI cualquier cadena que `java.net.URI` aceptara, incluidas las rutas relativas y `C:/…`. Ahora registra las factorías (`CentralModelFactory.registerDefaultFactories`; `register` ignora una factoría de una clase ya registrada) y carga primero un fichero existente; si no, exige un IRI absoluto con esquema de más de una letra. Test `TestOSDiMainGenExcel` (ruta absoluta con barras y ruta relativa, sin `file:///`), que antes falló con `OWLOntologyFactoryNotFoundException` y con `Could not find a suitable decision tree factory`. El cambio toca `jades-utils`.
- Estructura (punto 1, aprobada): `jades-hta/examples/`, con un `README.md` de índice y una carpeta por caso con su `.ttl`, su `README.md` y `expected-results.json` (valores `published`, con tolerancia y dónde aparecen, o `hand`, con tolerancia relativa 1e-8; medidas por brazo `cost`, `lifeYears`, `qalys` y comparativas `incrementalCost`, `incrementalQalys`, `icer`).
- Evans et al. (1997) (punto 2; commits `8c72da0` y `e7933ef`): `examples/evans1997-migraine`, a partir de I05, con individuos `EVANS_…`. Se quitó la esperanza de vida de la población: los resultados no cambian (horizonte de 24 horas; se suponen 100 años con aviso). I05 sale de `OSDi_test_tree_scenarios.ttl` y del catálogo. Lo que cubría sigue cubierto: grupo como estrategia siguiente (P58), recaída tras el seguimiento (P50) y costes en CAD del año del estudio (P57); la pérdida de AVAC negativa en un resultado solo la cubría I05, así que se añadió P69 (P43 con −0,5: AVAC 55,45; commit `27ba75b`), que pasó a la primera.
- Suite de validación (punto 3, aprobada: etiqueta y perfil): `TestValidationExamples` (etiqueta `validation`; la propiedad `excludedGroups` del pom de `jades-hta` la excluye por defecto y el perfil `validation` solo ejecuta esa etiqueta) carga cada ejemplo, genera su Excel en memoria, compara cada valor y escribe `target/validation/report.md`. Evans: los 10 valores pasan; RCEI 29 363,02 frente a 29 366 publicada (diferencia −2,98; tolerancia relativa 0,001). La clase se llamó primero `ValidationSuite`, pero surefire solo recoge `Test*`, `*Test`, `*Tests` y `*TestCase`.
- `TestOntologyConsistency` comprueba también los ejemplos (pendiente de la fase 17).
- Documentación: CLAUDE.md (comandos, sección de ejemplos, `genExcel`, tabla de scenarios) y los README de `examples/`.
- Suite final: 369 tests en `jades-hta`, 0 fallos, 1 omitido; validación: 1 test, 10 valores, 0 fallos.

**Decidido:**
- Los ejemplos van en `jades-hta/examples/` hasta el traslado; los resultados, en JSON; la validación, con etiqueta y perfil.
- Con `-DexcludedGroups=…` en la línea de órdenes hay que mantener `validation` en la lista (p. ej., `known-issue,validation`), porque sustituye a la propiedad del pom.

**Pendiente:**
- Comprobar en el artículo la tabla de la RCEI publicada y, si se quiere, añadir otros valores publicados (costes y AVAC por brazo).
- Los individuos de los ejemplos están en el espacio de nombres de OSDi porque JaDES busca el experimento por su nombre corto en él; para publicarlos en el repositorio de OSDi convendría un espacio de nombres propio (fase 19).
- Si se añade otro caso (el candidato es la otitis de Coco, 2007, esbozada en P47), hace falta tu aprobación.

### Fase 19: preparación del traslado a OSDi 1.3 (2026-11-25)

**Hecho:**
- `docs/osdi-1.3-traslado.md`: cambios de la copia de `OSDi.ttl` respecto a la 1.2 publicada, agrupados por decisión (comparación semántica con rdflib contra el commit `1d424a5` del repositorio de OSDi; la copia era idéntica a la 1.2 antes de la fase 2; 65 entidades: 27 nuevas y 38 cambiadas, ninguna eliminada); preparación de los ejemplos; pendientes; borrador de las notas de la versión; y propuesta sobre la copia de referencia.
- `jades-hta/examples/evans1997-migraine/catalog-v001.xml`: resuelve la 1.3 con `../../OSDi.ttl` (la raíz del repositorio de OSDi).
- La importación del ejemplo sigue siendo la 1.2: con la 1.3, JaDES intentaría descargarla (solo resuelve en local la versión de su copia) y fallarían la coherencia y la validación. Se cambiará a la vez que la versión de la copia.
- Encontrado: la `individuals/PBD.ttl` publicada en OSDi declara `detects`, `hasSensitivity` y `hasSpecificity` en las intervenciones, incoherente con la 1.3 (D11); las copias de `jades-hta` ya están migradas.
- Suite: 369 tests en `jades-hta`, 0 fallos, 1 omitido.

**Pendiente:**
- (Decidido: véase «Revisión».) Copia de referencia.

**Revisión (2026-11-25):**
- Decidido (apartado 5 de `docs/osdi-1.3-traslado.md`): opción A. El repositorio de OSDi es la referencia; `jades-hta` usa una copia idéntica a una versión publicada, comprobada por un test. Los cambios futuros se publican como 1.3.x (aditivos) o 1.4; un borrador lleva su propia versión (p. ej., 1.4-dev), nunca la de una versión publicada.
- Revisadas las reglas de w3id del fork (`ontologies-ULL/OSDi/.htaccess`), activas en w3id: la ontología y sus versiones se resuelven; los individuos solo con la extensión del fichero (su IRI declarado, sin extensión, da 404); los ejemplos no tienen regla. Aprobado el plan del apartado 2: paso de copia de `examples/` en `generate_doc.yml`, reglas de w3id para `X.Y/examples/<caso>` y `examples/<caso>` (Turtle para clientes RDF, carpeta de GitHub para navegadores) y para los individuos sin extensión. Los aplicas tú en el repositorio de OSDi y en el fork.
- En `jades-hta/examples/`: `owl:versionIRI` del ejemplo (`…/OSDi/1.3/examples/evans1997-migraine`) y la convención `<caso>/<caso>.ttl` en `examples/README.md`. La importación sigue en la 1.2 hasta adoptar la 1.3 publicada.
- Suite: 369 tests en `jades-hta`, 0 fallos, 1 omitido; validación: 10 valores, 0 fallos.

**Pendiente:**
- Publicar la 1.3 en el repositorio de OSDi y el PR de w3id (pasos 1 a 6 de `docs/osdi-1.3-traslado.md`).
- Después, adoptar la 1.3 en JaDES (apartado 5): copia publicada, importaciones de las ontologías de prueba y de los ejemplos, test de la copia y CLAUDE.md. Necesita tu aprobación, porque queda fuera de `docs/` y `examples/`.

**Pendientes de la ontología (2026-11-25):**
- `OSDi.ttl` (aprobado; sin cambiar la versión): `hasRefToSTATO` de `DI_HazardRatio` (STATO_0000677, *hazard ratio*) y `DI_OddsRatio` (STATO_0000182, *odds ratio*), comprobados en OLS y no obsoletos. Resuelve el pendiente de la fase 2.
- `OSDi.ttl`: comentario en español para los 37 términos nuevos o cambiados respecto a la 1.2 que solo lo tenían en inglés, con la terminología de los comentarios en español existentes (AVAC, forma abreviada, se lleva la probabilidad restante…). Los comentarios sin etiqueta de idioma de `hasSensitivity` y `hasSpecificity` pasan a `@en`. Comprobado con rdflib: ninguno de los 37 queda sin `@es`.
- `docs/osdi-1.3-traslado.md`: los dos puntos salen de «Pendiente»; el apartado 1 y las notas de la versión los recogen.
