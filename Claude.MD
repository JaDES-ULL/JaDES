# JaDES — guía para Claude Code
 
JaDES es una librería Java de simulación para evaluación económica de tecnologías sanitarias. El módulo `jades-hta` genera modelos a partir de la ontología **OSDi** (OWL, Turtle, versión 1.2, IRI `https://w3id.org/ontologies-ULL/OSDi`; copia local en `jades-hta/src/main/resources/OSDi.ttl`): árboles de decisión exportados a Excel y modelos de simulación de eventos discretos (DES). La misma ontología debe servir para ambos tipos de modelo.
 
Responde en español pero toda la codificación y comentarios deben estar en inglés.
 
## Comandos
 
```bash
# Suite completa de jades-hta
mvn -pl jades-hta -am test
 
# Solo los tests de árboles de decisión y la regresión del Excel
mvn -pl jades-hta -am test -Dtest='TestDecisionTreeOutcomes,TestDecisionTreeStructure,TestDecisionTreeMalformedModels,TestHTAExcelGeneration' -Dsurefire.failIfNoSpecifiedTests=false
 
# Excluir los defectos conocidos (cuando vuelva a haberlos)
mvn -pl jades-hta -am test -DexcludedGroups=known-issue

# Escribir a disco los Excel de los tests de árboles (por defecto se evalúan en memoria)
mvn -pl jades-hta -am test -Djades.tests.excel=true
```

Los tests de árboles cargan cada ontología una sola vez (`DecisionTreeTestSupport.loadModel`) y evalúan el Excel en memoria (`HTAExcelModelFactory.createWorkbook`), sin escribir ni releer el fichero. Por defecto usan una copia reducida de la plantilla, generada una vez a partir de la de producción sin las hojas que no intervienen en los resultados deterministas (`DecisionTreeTestSupport.SHEETS_NOT_IN_RESULTS`: EVPI, gráficos, sensibilidad, impacto presupuestario y tablas de población), porque leer la plantilla completa era lo más lento. Con `-Djades.tests.excel=true` se usa la plantilla completa y se escriben los ficheros. `TestHTAExcelGeneration` usa siempre la plantilla completa y escribe a disco dos casos (`TEST_Experiment1` y `PBD_ExperimentBase`; todos con la propiedad o con `-DkeepExcel=true`). Tras cambiar la plantilla (`model_template.xlsm`), ejecutar también los tests con `-Djades.tests.excel=true`.
 
## Cómo trabajar
 
1. **Para corregir un defecto, confirma primero que el test falla** y copia el mensaje real. Nunca cambies un valor esperado para que un test pase: si el valor esperado parece incorrecto, para y explícalo.
2. **Calcula los valores esperados a mano, antes de implementar**, a partir de la semántica (no de lo que devuelve el código). Muestra el cálculo. En casos delicados, usa un subagente que no haya visto la implementación.
3. **Muestra el cambio y espera mi aprobación antes de aplicarlo** cuando afecte a la semántica de OSDi, a la ontología o a más de un fichero de producción.
4. **Un defecto por commit.** No toques nada fuera del alcance pedido; si ves otro problema, anótalo y dímelo.
5. Tras cada cambio, ejecuta la suite completa de `jades-hta`. Si algo falla, para y explica por qué.
6. Si investigas sin cambiar nada, dilo explícitamente y no edites ficheros.
7. Documenta cada decisión de semántica en el Javadoc del método afectado, en el comentario de la propiedad en `OSDi.ttl` si procede (sin cambiar el número de versión) y en la nota de limitaciones de `docs/`.
8. No hagas push salvo que te lo pida.
## Evolución de OSDi 1.3

La copia de OSDi de `jades-hta` y el código que la lee evolucionan por fases hacia OSDi 1.3. Las decisiones, los puntos pendientes y el registro de cada fase están en [docs/osdi-1.3-diseno.md](docs/osdi-1.3-diseno.md). Las fases se aplican una a una: al terminar cada una, anota en su «Registro» qué se hizo, qué se decidió y qué quedó pendiente. El número de versión de `OSDi.ttl` no cambia hasta trasladar los cambios a OSDi.

## Tests de árboles de decisión
 
Paquete `jades-hta/src/test/java/es/ull/simulation/hta/osdi/decisiontree/`:
 
- `DecisionTreeCatalog`: catálogo de casos (`TreeCase`, `MalformedCase`) y enum `KnownIssue`.
- `DecisionTreeTestSupport`: carga de modelos, lectura de hojas y lectura de resultados del Excel.
- `TestDecisionTreeStructure`, `TestDecisionTreeOutcomes`, `TestDecisionTreeMalformedModels`.
Ontologías de prueba en `jades-hta/src/test/resources/`:
 
| Fichero | Casos |
|---|---|
| `OSDi_test_tree_developments.ttl` | S01–S10, S20–S24 |
| `OSDi_test_tree_manifestations.ttl` | S11–S19 |
| `OSDi_test_tree_payoffs.ttl` | P01–P59 (P26: HR sobre un riesgo de tiempo hasta el evento; P28–P32: manifestaciones agudas; P33–P41: estrategias y pautas; P42–P46: resultados de estrategias; P47–P49: efectos adversos; P50: recaída tras la respuesta; P51–P52: horizonte temporal; P53–P54: tiempo hasta el evento con horizonte; P55–P56 y P30: frecuencia de una aguda; P57: costes en dólares canadienses de 1997; P58: grupo de estrategias alternativas como estrategia siguiente; P59: población sin edad ni sexo, D10) |
| `OSDi_test_tree_scenarios.ttl` | I01–I05 (I03–I04: objetivo de las intervenciones en una progresión; I05: Evans et al. (1997), sumatriptán frente a cafeína/ergotamina) |
| `OSDi_test_tree_nbs.ttl` | B01–B06 (cribado neonatal con estrategias; B04–B06: resultados de detección; se comprueban en `TestHTAExcelGeneration`) |
| `OSDi_test_tree_malformed.ttl` | N01–N05, N07–N12, N14–N34 (casos que deben lanzar excepción; N13 volvió a ser P26 y N06, P56) |

Otras ontologías de prueba (todas importan `<https://w3id.org/ontologies-ULL/OSDi/1.2>`):

| Fichero | Tests |
|---|---|
| `OSDi_test.ttl` | `TestOSDiCompliance`, `TestOntology`, `TestHTAExcelGeneration`, `TestSingleDiseaseAndPopulationDecisionTreeModelGeneration` |
| `PBD.ttl` | `TestOntology`, `TestHTAExcelGeneration` (árbol de cribado neonatal), `TestSingleDiseaseAndPopulationDecisionTreeModelGeneration` |
| `T1DM.ttl` | `TestOntology`, `TestLoadOntology` (DES; único modelo de prueba con `Pathway`, `requires` y `Group`) |
| `OSDi_test_des.ttl` | `TestDESRisksAndEffects` (DES: D01, riesgos de manifestaciones y valores de los efectos de una intervención sobre ellos) y `TestDESPopulationDefaults` (D02: población sin edad ni sexo, D10) |
| `BD.ttl` | Ningún test la carga; importa OSDi 1.0 y es la única que usa `hasStrategy`, `LineOfTherapy` y `followedByStrategy` |

`TestOSDiCompliance` exige que cada constante "core" de `OSDiClass`, `OSDiObjectProperty` y `OSDiDataProperty` exista en `OSDi.ttl`: renombrar o eliminar un término de la ontología obliga a actualizar esos enums.
 
Convenciones:
 
- Individuos con prefijo `TT_<caso>_…`; un experimento por caso, `TT_<caso>_Experiment`.
- Base común: `TT_Population` (edad 20, esperanza de vida 65), `TT_NewbornPopulation` (edad 0, esperanza de vida 80), utilidad basal 0,85, coste anual de la enfermedad 1000, `TT_InterventionTreat` (solo añade un coste anual de 100) y `TT_InterventionNone`.
- Valores habituales de las progresiones (riesgo, coste anual, desutilidad anual, reducción de esperanza de vida): DevA (0,3; 500; 0,10; 10), DevB (0,5; 300; 0,05; 4), DevC (0,2; 200; 0,02; —), DevD (0,7; 100; 0,01; —), DevE (0,4; 400; 0,08; 6), ManX (0,2; 150; 0,03; —), ManY (0,5; 250; 0,06; 2), ManZ (0,4; 350; 0,04; —).
- Clave semántica de una hoja: nombres de las progresiones activas, ordenados y unidos con `+`; `""` si no hay ninguna; `DEATH` para la muerte.
- Las probabilidades de las hojas se comprueban en el brazo comparador. Si los brazos tienen hojas distintas (p. ej., una intervención sustituye una estrategia con resultados o efectos adversos), el caso declara las del brazo evaluado con `withAssessedLeaves`.
- Resultados del Excel: hoja `Process`, fila 10, columnas 6–11 (coste, esperanza de vida y AVAC del brazo evaluado y del comparador). Tolerancia relativa 1e-8.
- Combinaciones por defecto: costes ADD, desutilidades ADD, reducción de esperanza de vida MAX, tasa de mortalidad MAX.
- Defectos conocidos: se marcan en el catálogo con `knownIssue(...)` o `knownOutcomesIssue(...)` y un valor del enum `KnownIssue`; los tests correspondientes llevan `@Tag("known-issue")`. Al corregir el defecto, se quita la marca y el valor del enum.
- Los tests que cargan OSDi llevan `@OSDiTest` (tag `osdi`).
## Semántica de OSDi decidida
 
**Combinación de pagos y efectos** (a nivel de `Experiment`, válida para árboles y DES):
- `hasCostCombinationMethod`: ADD, ADD_DISTINCT.
- `hasDisutilityCombinationMethod`: ADD, ADD_DISTINCT, MAX.
- `hasLifeExpectancyReductionCombinationMethod`: ADD, ADD_DISTINCT, MAX (por defecto MAX).
- `hasMortalityRateCombinationMethod`: MAX, MULT (por defecto MAX).
- Los pagos de una vez se suman siempre; MAX solo se aplica a los pagos anuales.
- Esperanza de vida de una hoja: MAX(0, LE / IMR − LER).
**Magnitud de los efectos (D3):** en un `Effect` genérico la operación depende del `DataItemType` de la magnitud y del parámetro afectado (`EffectWrapper.resolveOperation`; tabla completa en `docs/known-limitations.md`). Factor y Ratio multiplican; RelativeRisk y HazardRatio sobre una probabilidad actúan sobre la tasa equivalente, `1 − (1 − p)^m`; HazardRatio sobre un tiempo hasta el evento divide la media (exponencial, con aviso); OddsRatio multiplica las odds; MeanDifference resta; Continuous_Variable y cualquier tipo absoluto sustituyen (con aviso si el tipo no coincide). Un tipo relativo siempre opera, aunque coincida con el del parámetro. Se rechazan: OR fuera de probabilidades, HR fuera de probabilidades y tiempos, estadísticos de incertidumbre como magnitud (Malformed) y RR sobre un tiempo (Unsupported). Para multiplicar una probabilidad linealmente se usa `DI_Factor`. Varios efectos sobre un parámetro se aplican en el orden sustituir → multiplicar (factores, tasas, odds) → restar; dos sustituciones son un error. Las subclases de `Effect` mantienen su operación.

**Utilidades:** una utilidad con `isDisutility = false` es un valor absoluto y se convierte en desutilidad: utilidad basal − u, igual que en el DES. Limitación documentada: se asume que la utilidad absoluta y la basal tienen la misma referencia.
 
**Horizonte temporal (árbol):** `hasTimeHorizon` del experimento, en años y sin redondear (`ExperimentWrapper.getDeclaredTimeHorizon`), se escribe en el rango `timeHorizon` del Excel (`ControlSheet`). Los años de vida de cada hoja son MIN(LE de la hoja, horizonte), y con ellos se limitan los pagos anuales, los AVAC y los pagos de una vez posteriores (P51; P52: 24 horas). Sin horizonte, la vida entera (la plantilla mantiene 150). Un horizonte no positivo es `Malformed`. El DES lo redondea a años enteros (con aviso).

**Población sin edad o sexo (D10):** se avisa y se suponen 18 años y un 50 % de mujeres (`PopulationWrapper.DEFAULT_AGE` y `DEFAULT_PROPORTION_OF_WOMEN`). En el árbol, la edad solo interpreta las edades de inicio y fin (P59) y el sexo no se usa (solo el aviso); el cribado neonatal supone 0 años (`NBSModel.getDefaultInitialAge`). En el DES, edad constante de 18 y sexo Bernoulli(0,5) (D02).

**Ventanas temporales:**
- `hasOnsetAge` y `hasEndAge` son edades, no tiempos desde el inicio. Con Age0 = edad inicial de la población (si no la define, 18 con aviso, D10; 0 en el cribado neonatal) y LY = años de vida de la hoja:
  - Inicio = MAX(0, Onset − Age0);
  - Fin = MIN(LY, End − Age0);
  - Duración = MAX(0, Fin − Inicio).
- Solo se cuenta lo que ocurre entre la edad inicial y la muerte.
- Pagos anuales: `PV(r, Duración, -x) / (1 + r) ^ Inicio`.
- Pagos de una vez: en el momento 0 si no hay edad de inicio; en `Onset − Age0` si Age0 ≤ Onset ≤ Age0 + LY; si no, no se cobran. Una edad de inicio anterior a la edad inicial significa que el evento ocurrió antes del modelo.
**Progresiones sin riesgo:**
- Si no pertenecen a un grupo de alternativas de dos o más miembros, están siempre activas en su contexto y no generan nodo. Una manifestación propia de un desarrollo solo está activa donde ese desarrollo lo está.
- En un grupo de alternativas, el miembro sin riesgo es la rama por defecto (1 − suma del resto). Solo se admite uno por grupo, y no es compatible con `hasNullProgression = true`.
**Árbol trivial:** una hoja que cuelga directamente de una intervención tiene probabilidad 1.

**Manifestaciones agudas (árbol):** un único episodio entre la edad inicial y la muerte, con el mismo nodo que una crónica (reglas, dependencia de un desarrollo y efectos sobre la hoja).
- El riesgo es la probabilidad de que ocurra el episodio; una aguda sin riesgo ocurre siempre en su contexto (D1: el árbol empieza en la crisis).
- `hasFrequency` f (episodios por año, fase 11): no cambia la estructura del árbol. El riesgo es la probabilidad de tener episodios (sin riesgo, siempre en su contexto: P56). Los pagos de una vez cuyo dueño es el nodo de la aguda (los suyos, los de sus estrategias y los de sus resultados y efectos adversos) se multiplican por los episodios esperados: pago anual f·x dentro de la ventana del nodo (`BranchDestinationNode.getEpisodeFrequency`, `PayoffWrapper.applyDiscount`), que siguen sumándose siempre como pagos de una vez (P30, P55). Sus efectos (muerte, LER) no se multiplican. Si la aguda es el objetivo de la intervención, la frecuencia se ignora con aviso (pendiente de reanalizar).
- Pagos de una vez: una vez por episodio, en el momento 0 o en la edad de inicio (reglas de las ventanas temporales); una utilidad de una vez es una pérdida de AVAC. Pagos anuales: se ignoran con aviso, como en el DES.

**Objetivo de las intervenciones (D1):** cada intervención declara su objetivo, la enfermedad o un elemento de su progresión (`isInterventionOf` o su inversa `hasIntervention`, que se leen en las dos direcciones). Se aplica en cada aparición del objetivo; si es la enfermedad, desde el inicio.
- Solo cuentan los objetivos incluidos en el modelo (las ontologías de prueba comparten intervenciones entre modelos). Sin objetivo: la enfermedad, con aviso. Más de un objetivo (N15), un objetivo ajeno a la enfermedad o intervenciones comparadas con objetivos distintos (N14): `MalformedOSDiModelException`. Etapas y manifestaciones propias de un desarrollo como objetivo (N16): `UnsupportedOSDiFeatureException`.
- Objetivo en la enfermedad: el árbol no cambia. Objetivo en un elemento T: el árbol empieza en los pacientes que presentan T (rama única con probabilidad 1; se ignoran su riesgo y sus reglas). Se mantienen los pagos y efectos de la enfermedad salvo su muerte inmediata (ocurre al inicio de la enfermedad). Solo se incluye lo que depende de T: las manifestaciones propias de un desarrollo (I04); nada más si T es una manifestación (I03).
- Si T es aguda, los pagos anuales de la intervención se ignoran con aviso. El modelo de cribado neonatal solo admite la enfermedad como objetivo.

**Estrategias (D2):** las intervenciones no declaran estrategias (`hasStrategy` en una intervención: `MalformedOSDiModelException`); las cambian con un `StrategyReplacementEffect` (`affects`: una condición de salud; `hasReplacementStrategy`: una estrategia o un grupo; sin magnitud, que se ignora con aviso).
- La sustitución se aplica en el brazo de la intervención y por tipo (detección = cribado o diagnóstico; seguimiento; tratamiento): cada estrategia nueva sustituye a las de su tipo de la condición de salud, o se le asigna si no tenía; las de otros tipos se mantienen (P34, P35, P41). Dos sustituciones sobre la misma condición en un brazo (N17), un efecto de otra clase que afecte a una condición de salud (N18) o una sustitución declarada por la enfermedad o una progresión: `MalformedOSDiModelException`.
- Costes y utilidades de las estrategias (seguimiento y tratamiento) se suman a los propios del elemento (P33): el modelador elige qué incluye en cada modelo, porque solo se lee lo que el modelo incluye.
- Coste de una estrategia: sus costes propios más los de las tecnologías que usa. La pauta de la tecnología (o, si no tiene, la de la estrategia) hace que su coste sea por aplicación (c): con r = `hasFrequency` × 8760 / `hasPeriod` (sin periodo, la frecuencia es anual), inicio = inicio del nodo + `hasStartOffset` y L = MIN(`hasDuration`, `hasCount` / r), pago anual r·c durante [inicio, inicio + L] recortado al nodo y a la vida (P36, P38, P39); sin frecuencia, N·c de una vez en el inicio, con N = `hasCount` o 1 (P37, P40). Sin pauta, los costes de la tecnología son pagos normales.

**Resultados de estrategias (D4–D7, árbol):** jerarquía `StrategyOutcome` → `TreatmentOutcome` (`SuccessOutcome`, `FailureOutcome`) y `DetectionOutcome` (positivo, negativo, no concluyente; solo en el NBS, ver abajo). `failsIf` (parámetro o condición de salud, D5) es la forma abreviada de fracaso y éxito.
- Los resultados se expanden bajo cada hoja (`StrategyOutcomeExpander`): una rama por resultado con su riesgo (el que no tiene riesgo se lleva el resto). Con `failsIf` hacia una condición, fracasa donde la condición está activa y tiene éxito donde no.
- Momento T = inicio de la estrategia + `hasEvaluationTime` (inicio, con aviso, si falta). Los pagos anuales de la estrategia van hasta T si el resultado tiene estrategia siguiente o resuelve; si no, hasta el final del nodo. La siguiente empieza en T. Pagos del resultado: una vez en T (anuales ignorados con aviso). «Resuelve»: los pagos anuales de la condición paran en T; sus efectos sobre la LE se mantienen, con aviso.
- Riesgo de tiempo hasta el evento T de un resultado: 1 − e^(−τ/T), con τ su `hasEvaluationTime` o, si falta, el horizonte (P54).
- Rechazos: ciclos (N23) y riesgo de tiempo hasta el evento sin tiempo de evaluación ni horizonte (N24), `Unsupported`; dos resultados sin riesgo (N25) y `failsIf` con resultados explícitos (N26), `Malformed`. Estrategias con resultados cuyos costes siguen una pauta: `Unsupported`.
- Grupo de estrategias alternativas como estrategia siguiente (fase 12, `NextStrategy`): de un resultado (`followedByStrategy`), de `followedOnFailureBy`/`followedOnSuccessBy` o de la estrategia. Cada paciente recibe un miembro con su `hasProportionWithinGroup` (como mucho uno sin proporción, que se lleva el resto); una rama `STRATEGY: …` por miembro (P58). Grupo sin proporciones (N33, `Unsupported`; en una sustitución, un grupo significa «todas a la vez»), varios miembros sin proporción o proporciones deterministas que no suman 1 sin miembro por defecto (N34, `Malformed`). En las cadenas de detección del NBS, `Unsupported`.
- Recaída tras la respuesta (D9, sin vocabulario nuevo): el éxito va seguido (`followedOnSuccessBy` o el `followedByStrategy` de su `SuccessOutcome`) de una `FollowUpStrategy` cuyo fracaso es la recaída (probabilidad entre quienes respondieron), seguido del tratamiento de la recaída (P50). Una recurrencia a largo plazo es un nuevo episodio de la misma aguda (fase 11).

**Efectos adversos (D8, árbol):** `AdverseEvent` (subclase de `HealthCondition`, disjunta de `Disease` y `ClinicalProgressionElement`), `hasAdverseEvent` / `isAdverseEventOf` (de `Strategy`) e `isOccurrenceOf` (solo identifica la condición; no activa su estructura). Regla para quien modela: problema de la evolución natural cuyo riesgo cambia la estrategia → `Effect`; problema provocado por la estrategia → `AdverseEvent`.
- Se expanden bajo las hojas (`StrategyOutcomeExpander`) cada vez que una rama aplica una estrategia (con o sin resultados, tras un resultado o como estrategia de otro efecto adverso), antes de sus resultados: una rama independiente por efecto adverso con su probabilidad por aplicación (riesgo obligatorio) y otra sin él.
- Son independientes del resultado, salvo `failsIf` hacia el efecto adverso: entonces el fracaso ocurre donde el efecto adverso ocurrió (P48). Pagos de una vez al inicio de la estrategia (anuales ignorados con aviso); sus efectos se aplican en su rama; un `InstantDeathEffect` abre una rama de muerte (esperanza de vida 0, aunque la estrategia empiece más tarde, con aviso) en la que no se expande nada más (P49); sus estrategias se aplican en su rama.
- Rechazos: sin riesgo, con varios o con un riesgo que no es un parámetro (N31, `Malformed`); riesgo de tiempo hasta el evento (N32, `Unsupported`); efectos adversos en las estrategias de detección del NBS (`Unsupported`). El DES los ignora (no simula estrategias).

**Cribado neonatal (NBS):** las intervenciones de detección no declaran `hasSensitivity`, `hasSpecificity`, `detects` ni `hasTemporaryThreshold` (`MalformedOSDiModelException`, N20 y N22): la enfermedad que detectan es su objetivo, y lo demás es de sus estrategias de detección.
- Estrategias de detección de un brazo: las de la enfermedad tras la sustitución de la intervención (por tipo). La intervención de cribado suele sustituirlas por un grupo {cribado, diagnóstico}. Como mucho una de cribado y una de diagnóstico por brazo (`Unsupported`); cribado en el brazo sin cribado (N21) o `detects` hacia otra enfermedad: `Malformed`.
- Brazo con cribado: toda la población paga los costes propios de la intervención con `ScreeningCat` más los de la estrategia de cribado; sensibilidad y especificidad de la estrategia de cribado (1 con aviso si faltan); verdaderos y falsos positivos pagan el diagnóstico (costes propios de la enfermedad con `DiagnosisCat` más la estrategia de diagnóstico del brazo). Brazo sin cribado: los enfermos pagan el diagnóstico. La sensibilidad y especificidad de la estrategia de diagnóstico no se usan. Las sustituciones del brazo llegan también al subárbol de la enfermedad (B03).
- Resultados de detección (D6, `NBSScreeningInterventionBuilder`): enfermos y sanos se ramifican en los resultados de la estrategia de cribado con `hasRiskIfDiseased` / `hasRiskIfNotDiseased` (el que no tiene riesgo para un estado se lleva el resto). Sensibilidad y especificidad se expanden a positivo y negativo (mismo árbol que antes; B04 ≡ B01). Un positivo sin estrategia propia sigue el `followedByStrategy` de la estrategia de cribado o, si no, el diagnóstico del brazo. Resultado seguido de una estrategia de diagnóstico: confirmación perfecta (costes propios con `DiagnosisCat` + coste de esa estrategia; enfermos = verdaderos positivos con los efectos de la intervención, sanos descartados; B06). Seguido de otra estrategia de detección (prueba repetida o de segundo nivel): paga su coste y se vuelve a ramificar en el mismo estado (B05). Negativo: fin (su estrategia siguiente se ignora con aviso). Pagos del resultado una vez y sus efectos en su rama; anuales y `hasEvaluationTime` ignorados con aviso.
- Rechazos: dos resultados sin riesgo para un estado (N27), positivo o no concluyente seguido de una estrategia que no es de detección (N28), no concluyente sin estrategia siguiente, forma abreviada con resultados explícitos: `Malformed`. Ciclos en la cadena de pruebas (N29) y estrategia de diagnóstico con resultados explícitos, en el brazo (N30) o como confirmación: `Unsupported`. Fuera del NBS, una estrategia de detección con resultados explícitos: `Unsupported`.
 
**Sexo de la población (DES):** el parámetro `hasSex` es la heterogeneidad del sexo (primer orden; 0 = hombre, 1 = mujer). Si es determinista, de segundo orden o calculado, es la proporción de mujeres p y cada paciente se sortea con un Bernoulli(p); el segundo orden se muestrea una vez por simulación (valor esperado en el caso base). Si es de primer orden, debe ser una distribución discreta (Bernoulli). La edad todavía no distingue los dos niveles.

## Divergencias conocidas entre árbol y DES
 
- El DES no simula desarrollos (sin riesgo, sin pagos, sin estado) e ignora las reglas de combinación de progresiones; las manifestaciones solo se excluyen entre sí mediante `supersedesClinicalProgression`.
- En el DES, una manifestación sin riesgo solo ocurre a través de `hasInitialProportion`; en el árbol está siempre activa.
- El árbol rechaza `hasInitialProportion` con `UnsupportedOSDiFeatureException` (N07).
- El DES ignora las estrategias (y las sustituciones de estrategia, con aviso) y los efectos cuya causa no es una intervención; en el árbol se aplican los efectos de intervenciones, enfermedad y progresiones.
- Detección en el DES: la sensibilidad y la especificidad de una intervención de detección salen de la estrategia de detección de su brazo (sus sustituciones o, si no, la de la enfermedad); sus costes no se aplican. Una estrategia de detección con resultados explícitos se rechaza (`Unsupported`).
- `Pathway` (con `requires`) solo se admite en el DES; el árbol lo rechaza.
- Manifestaciones agudas: el árbol cuenta un episodio y el DES todos los que ocurran; con varios pagos de una vez, el árbol los suma y el DES usa el primero (con aviso).
- El DES no lee el objetivo de las intervenciones: siempre se aplican desde el inicio de la simulación.
- Trabajo futuro (DES con desarrollos): grupo de alternativas = sorteo categórico al inicio; coexistente con riesgo p = sorteo independiente; desarrollo sin riesgo = siempre presente; pagos del desarrollo = anuales mientras lo tenga; manifestación propia = riesgo solo para pacientes con ese desarrollo; manifestación sin riesgo = proporción inicial 1 en su contexto.
## Limitaciones y TODO pendientes
 
- El árbol rechaza un `Pathway` como caracterización del riesgo (sin test) y un riesgo cuyo `DataItemType` no sea una probabilidad (N04); `DI_TimeToEvent` se convierte en una probabilidad (véase más abajo).
- El árbol ignora `supersedesClinicalProgression` y `hasDuration`; `hasFrequency` solo se lee en las manifestaciones agudas (véase «Manifestaciones agudas»).
- `SequentialCombinationRule` se trata como coexistente sin aviso en el árbol; no se leen `hasFirst` ni `hasNext`. El DES ignora todas las reglas con aviso.
- Estrategias en el árbol: no se leen `followedByStrategy` de estrategias sin resultados (con aviso), `hasConditionExpression` de la estrategia ni `failsIf`. Las pautas con subpautas, `hasConditionExpression` o edades de aplicación se rechazan (`UnsupportedOSDiFeatureException`, N19); TODO: soportarlas. `hasDose` no se usa. Falsos negativos del cribado: no pagan diagnóstico; pendiente de decidir (según la enfermedad, diagnóstico clínico tardío o muerte sin diagnóstico).
- `Group` no tiene wrapper. Los grupos de estrategias se leen en las sustituciones (todas a la vez) y como estrategia siguiente (alternativas con `hasProportionWithinGroup`, `NextStrategy`); los demás grupos no se leen en el árbol.
- Riesgo `DI_TimeToEvent` T de una progresión en el árbol (D6): probabilidad del evento dentro del horizonte, 1 − e^(−H/T) (exponencial; P53); sin horizonte, la anual, 1 − e^(−1/T), con aviso (P26). Se calcula en la fórmula de cada rama con T modificado por los efectos de la ruta (`AbstractModelFactory.getTimeToEventProbability`); el parámetro sintético `P_…` no se escribe en el Excel. El RR sobre un tiempo sigue rechazado (N12); el riesgo de un efecto adverso no puede ser un tiempo (N32).
- Las operaciones lineales (Factor, Ratio, MeanDifference) sobre una probabilidad no se recortan a [0, 1]; solo se avisa si los dos valores son deterministas.
- MAX aplicado a totales de toda la vida: no es correcto si los elementos tienen ventanas temporales distintas.
- Huecos de cobertura: estrategias con utilidades, pautas con descuento o con ventanas temporales del nodo, etapas y reglas secuenciales, rechazo de `Pathway` en árboles, aserciones sobre vías y manifestaciones agudas en el DES, parámetros probabilísticos o calculados.
- Los recursos `/OSDi.ttl`, `/skos.rdf` y `/time.owl` están en la raíz del jar; conviene moverlos a una ruta de paquete para evitar colisiones en el classpath.
## Notas técnicas
 
- El proyecto compila con `release 25`: si Maven usa otro JDK, ejecutar con `JAVA_HOME` apuntando a un JDK 25.
- El IDE también compila en `target/`: no editar las fuentes mientras se ejecutan los tests, y si aparecen errores `Unresolved compilation problem` o clases no encontradas, ejecutar `mvn -pl jades-hta -am clean test`.
- OWL API 5.5.0 con `StructuralReasoner`; Apache POI 5.4.1 para el Excel.
- `OSDiWrapper` resuelve las importaciones de SKOS y OWL-Time con copias locales (`SimpleIRIMapper`) para no depender de w3.org (error HTTP 429).
- `OSDiWrapper.Builder.build` mapea la `owl:versionIRI` declarada en la copia local de `OSDi.ttl` al núcleo ya cargado. Si se cambia esa versión, hay que actualizar a la vez los `owl:imports` de las ontologías de prueba, o se intentará descargar la versión antigua.
- Convención de descuento del Excel: anualidad vencida; actualización de costes con IPC: `ROUND(1000*CPI(estudio)/CPI(año), 0)/1000`, salvo si el año del coste es el del estudio (no se actualiza; `ParamSheet.updateCost`). La tabla CPI de la plantilla es el IPC español de 2002 a 2026: un estudio de otro año solo funciona si todos sus costes son de ese año (P57), y actualizar costes en otra moneda es aproximado.