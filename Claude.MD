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
```
 
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
| `OSDi_test_tree_payoffs.ttl` | P01–P23 |
| `OSDi_test_tree_scenarios.ttl` | I01, I02 |
| `OSDi_test_tree_malformed.ttl` | N01–N07 (casos que deben lanzar excepción) |

Otras ontologías de prueba (todas importan `<https://w3id.org/ontologies-ULL/OSDi/1.2>`):

| Fichero | Tests |
|---|---|
| `OSDi_test.ttl` | `TestOSDiCompliance`, `TestOntology`, `TestHTAExcelGeneration`, `TestSingleDiseaseAndPopulationDecisionTreeModelGeneration` |
| `PBD.ttl` | `TestOntology`, `TestHTAExcelGeneration` (árbol de cribado neonatal), `TestSingleDiseaseAndPopulationDecisionTreeModelGeneration` |
| `T1DM.ttl` | `TestOntology`, `TestLoadOntology` (DES; único modelo de prueba con `Pathway`, `requires` y `Group`) |
| `BD.ttl` | Ningún test la carga; importa OSDi 1.0 y es la única que usa `hasStrategy`, `LineOfTherapy` y `followedByStrategy` |

`TestOSDiCompliance` exige que cada constante "core" de `OSDiClass`, `OSDiObjectProperty` y `OSDiDataProperty` exista en `OSDi.ttl`: renombrar o eliminar un término de la ontología obliga a actualizar esos enums.
 
Convenciones:
 
- Individuos con prefijo `TT_<caso>_…`; un experimento por caso, `TT_<caso>_Experiment`.
- Base común: `TT_Population` (edad 20, esperanza de vida 65), `TT_NewbornPopulation` (edad 0, esperanza de vida 80), utilidad basal 0,85, coste anual de la enfermedad 1000, `TT_InterventionTreat` (solo añade un coste anual de 100) y `TT_InterventionNone`.
- Valores habituales de las progresiones (riesgo, coste anual, desutilidad anual, reducción de esperanza de vida): DevA (0,3; 500; 0,10; 10), DevB (0,5; 300; 0,05; 4), DevC (0,2; 200; 0,02; —), DevD (0,7; 100; 0,01; —), DevE (0,4; 400; 0,08; 6), ManX (0,2; 150; 0,03; —), ManY (0,5; 250; 0,06; 2), ManZ (0,4; 350; 0,04; —).
- Clave semántica de una hoja: nombres de las progresiones activas, ordenados y unidos con `+`; `""` si no hay ninguna; `DEATH` para la muerte.
- Las probabilidades de las hojas se comprueban en el brazo comparador.
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
**Utilidades:** una utilidad con `isDisutility = false` es un valor absoluto y se convierte en desutilidad: utilidad basal − u, igual que en el DES. Limitación documentada: se asume que la utilidad absoluta y la basal tienen la misma referencia.
 
**Ventanas temporales:**
- `hasOnsetAge` y `hasEndAge` son edades, no tiempos desde el inicio. Con Age0 = edad inicial de la población (0 con aviso si no la define) y LY = años de vida de la hoja:
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
 
## Divergencias conocidas entre árbol y DES
 
- El DES no simula desarrollos (sin riesgo, sin pagos, sin estado) e ignora las reglas de combinación de progresiones; las manifestaciones solo se excluyen entre sí mediante `supersedesClinicalProgression`.
- En el DES, una manifestación sin riesgo solo ocurre a través de `hasInitialProportion`; en el árbol está siempre activa.
- El árbol rechaza `hasInitialProportion` con `UnsupportedOSDiFeatureException` (N07).
- El DES ignora las estrategias y los efectos cuya causa no es una intervención; en el árbol se aplican los efectos de intervenciones, enfermedad y progresiones.
- `Pathway` (con `requires`) solo se admite en el DES; el árbol lo rechaza.
- Trabajo futuro (DES con desarrollos): grupo de alternativas = sorteo categórico al inicio; coexistente con riesgo p = sorteo independiente; desarrollo sin riesgo = siempre presente; pagos del desarrollo = anuales mientras lo tenga; manifestación propia = riesgo solo para pacientes con ese desarrollo; manifestación sin riesgo = proporción inicial 1 en su contexto.
## Limitaciones y TODO pendientes
 
- Manifestaciones agudas en árboles: TODO; hoy se rechazan con `UnsupportedOSDiFeatureException` (N06).
- El árbol rechaza un `Pathway` como caracterización del riesgo (sin test) y un riesgo cuyo `DataItemType` no sea una probabilidad (N04); `DI_TimeToEvent` se convierte en un parámetro sintético `P_…`.
- El árbol ignora `supersedesClinicalProgression`, `hasDuration` y `hasFrequency`.
- `SequentialCombinationRule` se trata como coexistente sin aviso en el árbol; no se leen `hasFirst` ni `hasNext`. El DES ignora todas las reglas con aviso.
- Estrategias (`hasStrategy` en intervenciones, enfermedad y progresiones): el árbol solo usa los costes y utilidades de estrategias FOLLOW_UP o TREATMENT, y solo si el elemento no tiene costes o utilidades propios. Se ignoran `followedByStrategy` (con aviso), `hasConditionExpression`, `hasGuideline` (con aviso) y `failsIf`.
- No se leen `hasIntervention` ni `isInterventionOf`: las intervenciones se obtienen por pertenencia al modelo y `hasAssessedIntervention`.
- `Group` no tiene wrapper; no se leen `hasGroupItem` ni `hasProportionWithinGroup`.
- Efectos: en el `Effect` genérico la operación depende del `DataItemType` de la magnitud (ContinuousVariable → SET; Factor o RelativeRisk → FACTOR; MeanDifference → DIFF; otro → SET con aviso). Las subclases fijan la operación (InstantDeath SET, LifeExpectancyReduction DIFF, IncreasedMortalityRate FACTOR).
- Varios efectos sobre el mismo parámetro: gana el último por IRI (sin test).
- MAX aplicado a totales de toda la vida: no es correcto si los elementos tienen ventanas temporales distintas.
- El horizonte temporal del experimento no se transfiere al Excel (la plantilla usa 150).
- Huecos de cobertura: estrategias (ningún test las ejercita), guías, etapas y reglas secuenciales, rechazo de `Pathway` en árboles, aserciones sobre vías y manifestaciones agudas en el DES, parámetros probabilísticos o calculados.
- Los recursos `/OSDi.ttl`, `/skos.rdf` y `/time.owl` están en la raíz del jar; conviene moverlos a una ruta de paquete para evitar colisiones en el classpath.
## Notas técnicas
 
- OWL API 5.5.0 con `StructuralReasoner`; Apache POI 5.4.1 para el Excel.
- `OSDiWrapper` resuelve las importaciones de SKOS y OWL-Time con copias locales (`SimpleIRIMapper`) para no depender de w3.org (error HTTP 429).
- `OSDiWrapper.Builder.build` mapea la `owl:versionIRI` declarada en la copia local de `OSDi.ttl` al núcleo ya cargado. Si se cambia esa versión, hay que actualizar a la vez los `owl:imports` de las ontologías de prueba, o se intentará descargar la versión antigua.
- Convención de descuento del Excel: anualidad vencida; actualización de costes con IPC: `ROUND(1000*CPI(estudio)/CPI(año), 0)/1000`.