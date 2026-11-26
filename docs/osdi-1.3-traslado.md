# Traslado de OSDi 1.3 desde jades-hta

Fase 19 de `docs/osdi-1.3-diseno.md` (2026-11-25). Prepara el paso de la copia de OSDi de `jades-hta` (`jades-hta/src/main/resources/OSDi.ttl`) al repositorio de OSDi como versión 1.3, con sus ejemplos de validación. Durante la fase 19 solo cambiaron `docs/` y `jades-hta/examples/`; después, con tu aprobación, la ontología (comentarios en español y códigos de STATO) y la adopción de la 1.3 en JaDES (apartado 5).

Decidido el 2026-11-25: el repositorio de OSDi es la referencia y JaDES usa una copia de una versión publicada (apartado 5); los ejemplos se publican con el flujo de OSDi y reglas nuevas de w3id (apartado 2).

## 1. Cambios respecto a OSDi 1.2

**Método.** Comparación semántica (rdflib) entre `OSDi.ttl` del repositorio de OSDi en el commit de la publicación de la 1.2 (`1d424a5`) y la copia actual de `jades-hta`. Se compara cada entidad con nombre con todas sus tripletas, expandiendo los nodos anónimos (restricciones, uniones, listas). La copia de `jades-hta` justo antes de la fase 2 (`646cba0~1`) es idéntica a la 1.2 publicada, así que las diferencias son exactamente las de las fases 2 a 18.

**Resultado.** 65 entidades distintas por las decisiones de las fases 2 a 18: 27 nuevas y 38 cambiadas. No se ha eliminado ninguna entidad. Con la traducción de los comentarios y los parámetros de las distribuciones (apartado «Sin decisión numerada»), la comparación con la 1.2 da 28 entidades nuevas (se añade `hasThetaParameter`) y 199 cambiadas. Las 161 cambiadas de más solo cambian sus comentarios, salvo las correcciones que se indican allí. La cabecera (`owl:Ontology`) declara desde el 2026-11-25 `owl:versionIRI <…/OSDi/1.3>`, `owl:versionInfo "1.3"` y `owl:priorVersion <…/OSDi/1.2>` (apartado 5).

Cada entidad aparece en la decisión que la cambió; si la cambiaron varias, en la principal, con referencia a las demás.

### D1. Objetivo de la intervención (fases 4, 14 y 15)

- **Propiedades.** `hasIntervention`: dominio `Disease` → `HealthCondition`. `isInterventionOf`: rango `Disease` → `HealthCondition`.
- **Restricciones.** `Intervention`: nueva cardinalidad cualificada exacta 1 de `isInterventionOf` sobre `HealthCondition`. `hasIntervention only Intervention` pasa de `Disease` a `HealthCondition`.
- **Comentarios.** `hasIntervention` (inglés y español; tiempo 0 = aplicación de la intervención, todos presentan el objetivo, con objetivo agudo todos empiezan con un episodio, intervenciones de detección), `isInterventionOf` (inglés y español) e `Intervention`.

### D2. Estrategias de una intervención como efectos de sustitución (fases 5 y 6)

- **Clases.** Nueva `StrategyReplacementEffect` ⊑ `Effect`, con `affects` exactamente 1 `HealthCondition` y `hasReplacementStrategy` exactamente 1 (una estrategia o un grupo de estrategias).
- **Propiedades.** Nueva `hasReplacementStrategy`. `affects`: rango + `HealthCondition`; `isAffectedBy`: dominio + `HealthCondition`. `hasStrategy`: sale `Intervention` del dominio.
- **Restricciones.** `Effect`: `affects only (Attribute or Parameter or HealthCondition)`. Se quitan `Intervention: hasStrategy only Strategy` y `TherapeuticIntervention: hasStrategy only (FollowUpStrategy or LineOfTherapy)`.
- **Comentarios.** `StrategyReplacementEffect` y `hasReplacementStrategy` (inglés y español), `affects`, `isAffectedBy`, `hasStrategy`, `DiagnosisIntervention` y `ScreeningIntervention` (sus estrategias de detección, véase D11).

### D3. Semántica de la magnitud de un efecto (fase 2)

- **Individuos.** Nuevos `DI_HazardRatio` (`hasRefToSTATO`: STATO_0000677, *hazard ratio*) y `DI_OddsRatio` (STATO_0000182, *odds ratio*), de tipo `DataItemType`. Códigos comprobados en OLS (no obsoletos) el 2026-11-25.
- **Comentarios.** `Effect` (cómo se aplica la magnitud según los tipos de dato) y `hasEffectMagnitude` (tabla de operaciones).

### D4. Resultados de las estrategias (fases 7a y 7b)

- **Clases nuevas.** `StrategyOutcome` ⊑ `ModelItem` (como mucho un `hasEvaluationTime` y un `followedByStrategy`); `TreatmentOutcome` (como mucho un `hasOutcomeRisk`), con `SuccessOutcome` y `FailureOutcome` (disjuntas); `DetectionOutcome` (como mucho un `hasRiskIfDiseased` y un `hasRiskIfNotDiseased`), unión disjunta de `PositiveDetectionOutcome`, `NegativeDetectionOutcome` e `InconclusiveDetectionOutcome` (esta última, con `followedByStrategy some Strategy`). `TreatmentOutcome` y `DetectionOutcome` son disjuntas.
- **Propiedades nuevas.** `hasStrategyOutcome` / `isStrategyOutcomeOf`; `hasOutcomeRisk` (⊑ `hasParameter`) con sus subpropiedades `hasRiskIfDiseased` y `hasRiskIfNotDiseased`; `hasEvaluationTime` (⊑ `hasParameter`); `followedOnSuccessBy` y `followedOnFailureBy` (forma abreviada); `resolvesHealthCondition` y `resolvesOnSuccess` (funcionales, booleanas).
- **Propiedades cambiadas.** `followedByStrategy`: dominio + `StrategyOutcome`; `followsStrategy`: rango + `StrategyOutcome`. `hasCost`, `hasUtility` y `hasEffect`: dominio + `StrategyOutcome`. `failsIf`: dominio `LineOfTherapy` → `Strategy` (el rango, en D5).
- **Restricciones.** `LineOfTherapy` y `FollowUpStrategy`: `hasStrategyOutcome only TreatmentOutcome`; `DetectionStrategy`: `hasStrategyOutcome only DetectionOutcome`. Se quita `LineOfTherapy: failsIf only Parameter`.
- **Comentarios.** Todas las clases y propiedades nuevas; `followedByStrategy`, `failsIf` y `DetectionStrategy`.

### D5. Fracaso cuando aparece una condición de salud (fases 13 y 17)

- **Propiedades.** `failsIf`: rango `Parameter` → `HealthCondition or Parameter`. `causesFailOf` (su inversa; corregida en la fase 17, porque restringía el rango efectivo de `failsIf` a `Parameter`): dominio `Parameter` → `HealthCondition or Parameter`, rango `LineOfTherapy` → `Strategy`.
- **Comentarios.** `failsIf` (momento del fracaso) y `causesFailOf` (inglés y español, remite a `failsIf`).

### D6. Eficacia, tiempos hasta el evento y resolución (fases 7a y 11)

Solo comentarios: `hasDuration` (resolución en una simulación), `hasOutcomeRisk`, `StrategyOutcome`, `SuccessOutcome` y `failsIf` (probabilidad por episodio o tiempo hasta el evento).

### D7. Utilidades por episodio como pérdidas de AVAC (fases 3, 7a y 12)

Solo comentarios: `appliesOneTime`, `isDisutility` (pérdida de una vez; una pérdida negativa es una ganancia) y `AcuteManifestation` (pagos por episodio).

### D8. Efectos adversos (fase 9)

- **Clases.** Nueva `AdverseEvent` ⊑ `HealthCondition`, disjunta de `Disease` y `ClinicalProgressionElement`, con exactamente un `hasRiskCharacterization` sobre `Parameter` y como mucho un `isOccurrenceOf`.
- **Propiedades.** Nuevas `hasAdverseEvent` / `isAdverseEventOf` e `isOccurrenceOf`. `hasRiskCharacterization`: dominio + `AdverseEvent`; `isRiskCharacterizationOf`: rango + `AdverseEvent`.
- **Comentarios.** Las nuevas (inglés y español), `hasRiskCharacterization` (también describe las progresiones sin riesgo) e `isRiskCharacterizationOf`.

### D9. Recaídas y recurrencias (fases 10 y 11)

Solo comentarios: `FollowUpStrategy` (inglés y español; corrige además su comentario anterior, que era inglés etiquetado como `@es`), `followedOnSuccessBy` y `hasFrequency` (frecuencia de una aguda en un árbol).

### D10. Población sin edad, sexo, esperanza de vida o utilidad basal (fase 13)

Ningún cambio en la ontología.

### D11. Propiedades de detección en las estrategias (fase 6)

- **Propiedades.** `detects`, `hasSensitivity` y `hasSpecificity`: dominio `DetectionIntervention or DetectionStrategy` → `DetectionStrategy`. `hasTemporaryThreshold`: dominio → `ScreeningStrategy`. `isDetectedBy`: rango → `DetectionStrategy`.
- **Restricciones.** Se quitan de `DetectionIntervention` las restricciones sobre `hasSensitivity`, `hasSpecificity` y `detects`, y de `ScreeningIntervention`, la de `hasTemporaryThreshold`.
- **Comentarios.** `detects`, `hasSensitivity`, `hasSpecificity`, `isDetectedBy`, `DiagnosisIntervention` y `ScreeningIntervention`.

### Sin decisión numerada

- **Parámetros de las distribuciones (2026-11-25).**
  - **Escala de la gamma.** Se llamaba `hasLambdaParameter`, aunque lambda suele ser la tasa (1/θ). La 1.3 la renombra:
    - Nueva propiedad `hasThetaParameter` (dominio `GammaDistributionExpression`), que es la escala θ: media = k·θ, con k la forma (`hasAlfaParameter`).
    - `GammaDistributionExpression` la restringe en lugar de `hasLambdaParameter`.
    - `hasLambdaParameter` sale del dominio de la gamma y queda para la exponencial (tasa, media = 1/λ) y la de Poisson (número medio de eventos).
    - El significado del valor no cambia: JaDES y todos los datos ya lo usaban como escala (p. ej., PBD: k = 25, θ = 3,29, media 82,25 frente al valor esperado de 82,27). Solo cambia el nombre.
  - **Comentarios.** Se corrigen los de `GammaDistributionExpression` y `hasLambdaParameter`, que se contradecían (forma frente a escala). `hasScaleParameter` explica que es un factor que multiplica la variable (antes del desplazamiento), no la escala de la gamma.
  - **Errata.** La etiqueta en inglés de `ExponentialDistributionExpression` decía «Expnential».
  - **Cambio incompatible.** Una gamma con `hasLambdaParameter` deja de ser válida, y HermiT no lo detecta, porque las distribuciones no son disjuntas. JaDES la rechaza con un mensaje que explica el cambio. Las copias de `PBD.ttl`, `T1DM.ttl` y `BD.ttl` de `jades-hta` ya usan `hasThetaParameter`; las publicadas en OSDi (1.0 y 1.1) usan `hasLambdaParameter` y se sustituyen al publicar (paso 2).
- **Horizonte temporal (fase 11).** Comentario de `hasTimeHorizon` (inglés y español).
- **Comentarios en español (2026-11-25).** Los 37 términos nuevos o con el comentario cambiado que solo lo tenían en inglés tienen ahora también el español. `hasSensitivity` y `hasSpecificity` tenían el comentario sin etiqueta de idioma; ahora lleva `@en`. Después, todas las clases y propiedades de OSDi tienen comentario en inglés y en español (2026-11-25):
  - 165 comentarios traducidos.
  - `DiagnosisStrategy`, `LineOfTherapy` y `Strategy` tenían el comentario en inglés etiquetado como `@es`; ahora llevan `@en`, más su traducción.
  - `AlternativeCombinationRule`, `CoexistentCombinationRule`, `hasTemporaryThreshold` y `hasYear` tenían el comentario sin etiqueta de idioma; ahora llevan `@en`.
  - Nuevos comentarios de `isAssessedInterventionOf` e `isComparatorInterventionOf` (inversas de `hasAssessedIntervention` y `hasComparatorIntervention`).
  - Erratas del inglés: `AgentBasedModel`, `AcuteManifestation`, `ScreeningIntervention`, `GammaDistributionExpression`, `MeasuredDataItemType`, `ProbabilityDistributionExpression`, `RareDisease`, `UniformDistributionExpression`, `usesSameModelItemsAs` y «which value» → «whose value» en los parámetros.
  - Errores de copia y pega: `GroupableModelItemWithProportion` tenía la etiqueta y el comentario de `GroupableModelItem` (ahora «Groupable model item with proportion» / «Elemento del modelo agrupable con proporción»); el comentario de `isGroupItemOf` hablaba de estrategias (ahora: pertenece a un grupo, inversa de `hasGroupItem`); el de `isStrategyOf`, inversa de `hasStrategy`, hablaba de una ruta (ahora remite a `hasStrategy`).
  - Quedan sin comentario ni etiqueta las tres propiedades de anotación del espacio de nombres de OSDi (`osdi:TODO`, `osdi:creator`, `osdi:contributor`) y, entre los individuos, 51 sin comentario y 20 sin etiquetas.
- **Evans et al. (fase 12).** Nuevo individuo `Currency_CanadianDollar`; comentario de `hasProportionWithinGroup` (proporción de una estrategia dentro de un grupo de alternativas; añade el español).

## 2. Ejemplos para el repositorio de OSDi

Propuesta aprobada el 2026-11-25. Además de copiar `examples/`, hay que publicar los ficheros con el flujo del repositorio de OSDi y añadir reglas en w3id. Los cambios en `generate_doc.yml` y en el `.htaccess` del fork de w3id.org los haces tú; aquí solo cambian `docs/` y `jades-hta/examples/`.

### Situación de partida (comprobada con `curl` el 2026-11-25)

Las reglas de `ontologies-ULL/OSDi/.htaccess` del fork están activas en w3id.

| Dirección | Respuesta |
|---|---|
| `…/OSDi` y `…/OSDi/1.2` | 303 al formato pedido (`ontology.ttl` con Turtle, `ontology.owl` con RDF/XML, que es lo que pide OWL API por defecto). Los ficheros existen. |
| `…/OSDi/1.2/individuals/PBD.ttl` | 303 a `github.io/OSDi/1.2/individuals/PBD.ttl`, que existe. |
| `…/OSDi/individuals/PBD` (el IRI que declara `PBD.ttl`) | 303 a `…/latest/individuals/PBD`, sin extensión: **404**. |
| `…/OSDi/examples/evans1997-migraine` | Ninguna regla: **404**. |

GitHub Pages sirve los `.md` sin convertirlos a HTML, y el repositorio de OSDi no tiene etiquetas por versión.

### Hecho en `jades-hta/examples/`

- `evans1997-migraine/catalog-v001.xml` resuelve `https://w3id.org/ontologies-ULL/OSDi/1.3` con `../../OSDi.ttl` (la raíz del repositorio de OSDi).
- La ontología del ejemplo tiene como IRI `https://w3id.org/ontologies-ULL/OSDi/examples/evans1997-migraine` (la última versión) y como `owl:versionIRI` `https://w3id.org/ontologies-ULL/OSDi/1.3/examples/evans1997-migraine`.
- Convención, documentada en `examples/README.md`: el `.ttl` de cada ejemplo se llama como su carpeta (`<caso>/<caso>.ttl`). Las reglas de w3id dependen de ella.
- La ontología del ejemplo importa `<https://w3id.org/ontologies-ULL/OSDi/1.3>`, la versión de la copia de `jades-hta` (apartado 5).

### Publicación en el repositorio de OSDi (`.github/workflows/generate_doc.yml`)

Un paso como el de `individuals/` (paso 7). El paso 8 ya copia `docs/latest/*` a `docs/$VERSION/`, así que los ejemplos llegan también a la carpeta de la versión:

```yaml
- name: Copy examples folder
  if: ${{ hashFiles('examples/**') != '' }}
  run: |
    mkdir -p docs/latest/examples
    cp -R examples/* docs/latest/examples/
```

### Reglas en w3id (`ontologies-ULL/OSDi/.htaccess` del fork)

- Solo existe el `.ttl` de cada ejemplo, así que a cualquier cliente RDF se le sirve el Turtle. OWL API lo lee bien, porque Pages lo sirve como `text/turtle`.
- Los navegadores (`text/html` en `Accept`, que OWL API no envía) van a la carpeta del ejemplo en GitHub, que muestra su `README.md`.

```apache
# ===============================================================
# 4) EXAMPLES: /OSDi/X.Y/examples/<case> and /OSDi/examples/<case>
# ===============================================================

# Files of an example (e.g., expected-results.json, README.md)
RewriteRule ^([0-9]+\.[0-9]+)/examples/([^/]+/.+)$ \
  https://ontologies-ULL.github.io/OSDi/$1/examples/$2 [R=303,L]
RewriteRule ^examples/([^/]+/.+)$ \
  https://ontologies-ULL.github.io/OSDi/latest/examples/$1 [R=303,L]

# Browsers: the folder of the example in GitHub, which renders its README
RewriteCond %{HTTP_ACCEPT} text/html
RewriteRule ^([0-9]+\.[0-9]+/)?examples/([^/]+)/?$ \
  https://github.com/ontologies-ULL/OSDi/tree/main/examples/$2 [R=303,L]

# RDF clients (any format): the Turtle file of the example
RewriteRule ^([0-9]+\.[0-9]+)/examples/([^/]+)/?$ \
  https://ontologies-ULL.github.io/OSDi/$1/examples/$2/$2.ttl [R=303,L]
RewriteRule ^examples/([^/]+)/?$ \
  https://ontologies-ULL.github.io/OSDi/latest/examples/$1/$1.ttl [R=303,L]
```

Los navegadores van siempre a `main`. Si se crean etiquetas por versión en el repositorio de OSDi (`v1.3`), la regla de HTML puede apuntar a `tree/v$1/examples/$2`.

En el mismo PR conviene arreglar los individuos, cuyo IRI no lleva la extensión del fichero:

```apache
# Individuals without extension: their Turtle file
RewriteRule ^([0-9]+\.[0-9]+)/individuals/([^/.]+)$ \
  https://ontologies-ULL.github.io/OSDi/$1/individuals/$2.ttl [R=303,L]
RewriteRule ^individuals/([^/.]+)$ \
  https://ontologies-ULL.github.io/OSDi/latest/individuals/$1.ttl [R=303,L]
```

Estas dos reglas deben ir **antes** de las de la sección 3 del `.htaccess`, que copian la ruta tal cual. El IRI de `T1DM.ttl` tiene además una doble barra (`ontologies-ULL//individuals/T1DM`) que hay que corregir en el fichero. Conviene actualizar también la sección «Access» del `README.md` de `ontologies-ULL/OSDi` con las versiones, los individuos y los ejemplos.

### Activación y comprobación

- Las reglas funcionan cuando perma-id/w3id.org acepta el PR desde el fork. No importa que lleguen antes que los ficheros: devolverán 404 hasta que se publique la 1.3.
- Comprobación:

```bash
curl -sI -H "Accept: text/turtle" https://w3id.org/ontologies-ULL/OSDi/1.3/examples/evans1997-migraine
curl -sI -H "Accept: application/rdf+xml" https://w3id.org/ontologies-ULL/OSDi/examples/evans1997-migraine
curl -sI -H "Accept: text/html" https://w3id.org/ontologies-ULL/OSDi/examples/evans1997-migraine
curl -sI -H "Accept: text/turtle" https://w3id.org/ontologies-ULL/OSDi/individuals/PBD
```

### Espacio de nombres de los individuos (pendiente)

Los individuos del ejemplo son `…/OSDi#EVANS_…`. El fragmento no llega al servidor, así que consultar `EVANS_Experiment` devuelve la ontología OSDi, que no lo define. Con un espacio de nombres propio (`…/OSDi/examples/evans1997-migraine#EVANS_…`), las reglas anteriores devolverían el ejemplo. Requiere que JaDES busque el experimento en el espacio de nombres de la ontología cargada: `OntologyResolution.toIRI` ya usa el prefijo por defecto, pero hay que comprobar el resto del wrapper. No bloquea la publicación.

## 3. Pendiente

**En el repositorio de OSDi**
- `individuals/PBD.ttl` importa la 1.1 y `individuals/T1DM.ttl` la 1.0. La `PBD.ttl` publicada declara `detects`, `hasSensitivity` y `hasSpecificity` en las intervenciones, lo que es incoherente con la 1.3 (D11; el mismo caso que N20). Las copias de `jades-hta/src/test/resources` (`PBD.ttl`, `T1DM.ttl`) ya están migradas a D2 y D11 y son coherentes según HermiT (`TestOntologyConsistency`): conviene publicarlas en su lugar, importando la 1.3.
- Corrección (2026-11-26): los catálogos no están publicados. El `.gitignore` de OSDi ignora `catalog-v*.xml`, así que el `individuals/catalog-v001.xml` con ruta absoluta era solo una copia local. En la rama `release-1.3` se añade a la fuerza solo el catálogo del ejemplo, que `examples/README.md` documenta.

**En JaDES (distribuciones; vistos al renombrar la escala de la gamma)**
- Excel: `ParamSheet` lee siempre dos parámetros de la distribución, así que falla con una distribución de un solo parámetro (exponencial, Poisson, Bernoulli).
- Excel: la fórmula de la exponencial (`-LN(RAND() / λ)`) no es la inversa de la exponencial (`-LN(RAND()) / λ`).
- Excel: `POISSON.INV` no existe en Excel.
- DES: no hay generador de Poisson (`PoissonVariate`).
- Ningún modelo de prueba usa estas distribuciones. La exponencial del DES ya se corrigió (pasa la media 1/λ).

**En los ejemplos**
- Evans: comprobar en el artículo la tabla de la RCEI publicada (29 366 CAD/AVAC) y, si se quiere, añadir otros valores publicados.
- Los individuos de los ejemplos están en el espacio de nombres de OSDi (`EVANS_…`), porque JaDES busca el experimento por su nombre corto en ese espacio. Un espacio de nombres propio por ejemplo sería más limpio, pero exige que JaDES resuelva nombres en el espacio de la ontología cargada.

**Decisiones abiertas** (véase «Pendientes de decidir» en `docs/osdi-1.3-diseno.md`)
- Qué ocurre con los falsos negativos del cribado; bloquea la fase 23.
- Las siete decisiones de la detección en el DES (fase 23).
- Los cribados de varias enfermedades (paneles), que no caben con un solo objetivo por intervención.
- El objetivo en una manifestación propia de uno o varios desarrollos.

**Fuera de la 1.3** (trabajo futuro): pautas con subpautas, condiciones o edades de aplicación; pautas en estrategias con resultados; confirmación imperfecta; `hasConditionExpression` en las estrategias; grupos en las cadenas de detección del cribado neonatal; actualización de costes fuera del IPC español de 2002 a 2026; análisis de impacto presupuestario.

## 4. Notas de la versión 1.3 (borrador)

> ## OSDi 1.3
>
> OSDi 1.3 makes the ontology able to describe the strategies, outcomes and side effects of the interventions of a health economic evaluation, and fixes the semantics of what an intervention targets. All the changes have been implemented and validated with decision trees in JaDES (jades-hta): 104 test models whose expected results were computed by hand, 34 malformed models that must be rejected for the right reason, and a first validation example of a published study, Evans et al. (1997), whose cost-effectiveness ratio JaDES reproduces (CAD 29 363 per QALY, against 29 366 in the article). A complete reasoner (HermiT) finds OSDi 1.3 and all the test and example ontologies consistent.
>
> ### What is new
>
> - **Target of an intervention** (`hasIntervention` / `isInterventionOf`, now between `HealthCondition` and `Intervention`): every intervention has exactly one target, the disease or an element of its progression. Time 0 is the moment when the intervention is applied: every patient presents the target, and the intervention is active during the whole simulation. With an acute target, every patient starts with an episode. Compared interventions have the same target.
> - **Strategies of an intervention** are declared as effects that replace the strategies of a health condition (`StrategyReplacementEffect`, `hasReplacementStrategy`). `Intervention` no longer declares `hasStrategy`.
> - **Effect magnitudes**: the data item types of the magnitude and of the affected parameter define how an effect applies (relative risks and hazard ratios on the equivalent rate, odds ratios on the odds, differences, replacements). New `DI_HazardRatio` and `DI_OddsRatio`, with their STATO references.
> - **Strategy outcomes** (`StrategyOutcome`, with `TreatmentOutcome` — `SuccessOutcome`, `FailureOutcome` — and `DetectionOutcome` — positive, negative, inconclusive): mutually exclusive outcomes with their risk, evaluation time, next strategy, payoffs, effects and resolution of the health condition. `failsIf` (now also towards a health condition), `followedOnSuccessBy`, `followedOnFailureBy` and `resolvesOnSuccess` are short forms. Relapses after the response are the failure of a follow-up strategy.
> - **Adverse events** (`AdverseEvent`, `hasAdverseEvent`, `isOccurrenceOf`): problems caused by a strategy, with a mandatory risk per application, one-time payoffs, and effects such as death or the failure of the strategy.
> - **Detection**: `detects`, `hasSensitivity`, `hasSpecificity` and `hasTemporaryThreshold` belong to detection strategies, not to interventions. The disease detected by a detection intervention is its target.
> - **Payoffs per episode**: one-time utilities are QALY losses (a negative loss is a gain); the costs and utilities of an acute manifestation apply per episode.
> - New currency `Currency_CanadianDollar`, and clarified comments of `hasTimeHorizon`, `hasFrequency`, `hasDuration`, `appliesOneTime`, `isDisutility` and `hasProportionWithinGroup`.
> - Every class and property has its comment in English and Spanish, with some spelling fixes; `GroupableModelItemWithProportion` gets its own label and comment.
>
> ### Breaking changes
>
> - `hasStrategy` on an intervention: use a `StrategyReplacementEffect`.
> - `detects`, `hasSensitivity`, `hasSpecificity` and `hasTemporaryThreshold` on an intervention: move them to its detection strategy.
> - The scale of a gamma distribution is now `hasThetaParameter` (mean = shape × scale); OSDi 1.2 called it `hasLambdaParameter`, although lambda usually denotes the rate. Rename the property in your gamma distributions; the value does not change. `hasLambdaParameter` remains the rate of an exponential distribution and the mean of a Poisson distribution.
> - An intervention shared by the models of different diseases: since it has exactly one target, a reasoner infers that the diseases are the same individual. Give each model its own interventions.
>
> ### What the discrete event simulation of JaDES does not implement yet
>
> JaDES rejects (as unsupported) or ignores some features of OSDi 1.3 in discrete event simulations; decision trees support them:
>
> - strategy outcomes (`hasStrategyOutcome`, `failsIf`), adverse events and guidelines in the strategies of a model (rejected), and therefore relapses after the response;
> - detection: the sensitivity and specificity of the detection strategy of each arm are read, but no tests are scheduled, so detection costs are not applied and there is no clinical diagnosis;
> - developments and combination rules of progressions (developments are not simulated; a development as a target is rejected);
> - the instant death of the progressions, and more than one cost or utility of the same kind in a health condition or an intervention (only the first one is used).
>
> ### Documented divergences between decision trees and discrete event simulations
>
> - **Acute target**: a decision tree evaluates a single episode, the one at time 0, and ignores the frequency of the episodes; a simulation also simulates the next episodes, and applies the one-time payoffs of the intervention with each of them.
> - **Acute manifestations**: a tree counts one episode or, with a frequency, multiplies the one-time payoffs by the expected number of episodes; a simulation counts every episode.
> - **Progressions without risk** are always present in their context in a tree; in a simulation, only through an initial proportion. Trees do not support initial proportions.
> - **Effects**: a simulation only applies the effects caused by an intervention; a tree also applies those of the disease and its progressions.
> - **Time horizon**: a tree uses it as declared; a simulation rounds it to whole years.
> - `Pathway` is only supported in simulations.

## 5. Copia de referencia después de la 1.3

**Decidido (2026-11-25): opción A, OSDi fijada a una versión publicada.** El repositorio de OSDi es la referencia. `jades-hta` usa una copia idéntica a una versión publicada.

- **Sincronización.** Al publicar una versión, su `OSDi.ttl` se copia sin cambios a `jades-hta/src/main/resources/OSDi.ttl`. Un test de JaDES compara la copia con la versión que declara (p. ej., con la huella del fichero publicado), para detectar cualquier edición local.
- **Cambios futuros de la ontología.** Se proponen en el repositorio de OSDi: 1.3.x si son aditivos, 1.4 si cambian el significado de algo publicado (la regla que el plan ya prevé para las fases 20 a 23). Si una fase de JaDES necesita la ontología antes de publicarla, usa un borrador con su propia versión (`owl:versionIRI <…/OSDi/1.4>`, `owl:versionInfo "1.4-dev"`), nunca con la de una versión publicada, y el test anterior lo señala como borrador.
- **Lo que se evita.** Durante las fases 2 a 18, la copia de JaDES dijo ser la 1.2 sin serlo. Con la opción A, cada IRI de versión significa una sola cosa, y quien use OSDi sin JaDES ve lo mismo que JaDES.

### Adopción de la 1.3 en JaDES (hecho el 2026-11-25)

En lugar de copiar a JaDES la versión ya publicada, se hace al revés: JaDES declara la 1.3 primero y su `OSDi.ttl` se copia sin cambios al repositorio de OSDi. Así las dos copias son idénticas desde el principio. No hay que esperar a publicar, porque `OSDiWrapper.Builder.build` resuelve en local la versión que declara su copia, sea cual sea.

1. Cabecera de `jades-hta/src/main/resources/OSDi.ttl`: `owl:versionIRI <…/OSDi/1.3>`, `owl:priorVersion <…/OSDi/1.2>` y `owl:versionInfo "1.3"`.
2. Todas las ontologías de prueba (`jades-hta/src/test/resources/*.ttl`) y el ejemplo importan `<https://w3id.org/ontologies-ULL/OSDi/1.3>`.
3. `TestOSDiPublishedCopy` comprueba:
   - la cabecera: la versión coincide con `owl:versionInfo` y hay un único `owl:priorVersion`;
   - la huella SHA-256 de la copia, con finales de línea LF, contra la registrada para su versión (1.3: `bc1295056d702387c2f9bb7f02bde2c0d31c596b4e4e0942bd4e8a9faaf5e60b`); un borrador (`-dev`) omite esta comprobación;
   - que todas las ontologías de prueba y los ejemplos importan la versión de la copia.
4. CLAUDE.md recoge la versión 1.3 y la regla de la opción A.

**La huella es la del fichero que se publica.** Si la copia cambia antes de publicarla, hay que actualizar la huella del test, o publicar el fichero que tiene la huella registrada.

## Pasos para publicar la 1.3

**En el repositorio de OSDi**

**Publicada el 2026-11-26.** La rama `release-1.3` se fusionó en `main` del repositorio de OSDi (`dc09f91`), y los flujos generaron `docs/1.3`, `docs/latest` (con los individuos y los ejemplos) y el tutorial.
- `https://w3id.org/ontologies-ULL/OSDi/1.3` resuelve a `docs/1.3/ontology.ttl`.
- La huella de `TestOSDiPublishedCopy` es la del `OSDi.ttl` de la raíz del repositorio (comprobada en `main`). El `ontology.ttl` que sirve w3id lo vuelve a serializar Widoco y tiene otra huella.
- Queda el paso 6.

1. Copiar a la raíz, sin cambios, la `OSDi.ttl` de `jades-hta`, que ya declara la 1.3. Su huella SHA-256 (con finales de línea LF) debe ser la que registra `TestOSDiPublishedCopy`.
2. Sustituir `individuals/PBD.ttl` e `individuals/T1DM.ttl` por las copias de `jades-hta`, importando la 1.3. Corregir el IRI de `T1DM.ttl` (doble barra; corregido también en `jades-hta`).
3. Añadir `examples/` (ya importan la 1.3) y su paso en `generate_doc.yml` (apartado 2).
4. Añadir las notas de la versión (apartado 4) a `CHANGELOG.md`, enlazado desde el `README.md`.
5. Publicar: fusionar `release-1.3` en `main` y hacer push. `generate_doc.yml` genera `docs/1.3` a partir de la `owl:versionIRI` (solo se dispara si cambia `OSDi.ttl`, como en este caso).
   - Etiqueta `v1.3`, creada a mano el 2026-11-26 en `6540e67`. Las reglas de w3id del PR #7317 llevan a los navegadores a los ejemplos de la etiqueta de cada versión (`blob/vX.Y/...`). Desde entonces, `generate_doc.yml` crea la etiqueta `vX.Y` de cada versión nueva al publicarla, si no existe y si la versión es un número de versión (paso 10, commit `eb045fb` en OSDi). Los borradores no deben publicarse en el repositorio de OSDi: la etiqueta se toma de la `owl:versionIRI` (en un borrador, la de la versión siguiente, p. ej. 1.4).
   - Tutorial (`docs-src/`) actualizado a la 1.3 en la misma rama (commit `dc8c726`). Se construye con `mkdocs build --strict` y todos sus ejemplos Turtle se analizan sin errores. Las figuras (PNG) las cambias tú; la lista de cambios está en el registro de `docs/osdi-1.3-diseno.md`.

**En el fork de w3id.org**

6. Añadir las reglas de los ejemplos y de los individuos sin extensión (apartado 2), actualizar el `README.md` de `ontologies-ULL/OSDi` y abrir el PR contra perma-id/w3id.org. Comprobar con `curl` cuando lo acepten.

**En JaDES**

7. Nada: la 1.3 ya está adoptada (apartado 5). Si el fichero publicado acaba siendo distinto, actualizar la huella de `TestOSDiPublishedCopy` con la del fichero publicado.
