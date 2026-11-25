# Traslado de OSDi 1.3 desde jades-hta

Fase 19 de `docs/osdi-1.3-diseno.md` (2026-11-25). Prepara el paso de la copia de OSDi de `jades-hta` (`jades-hta/src/main/resources/OSDi.ttl`) al repositorio de OSDi como versión 1.3, con sus ejemplos de validación. Nada fuera de `docs/` y `jades-hta/examples/` ha cambiado.

Queda una decisión tuya antes de publicar: qué copia de la ontología será la de referencia después (apartado 5).

## 1. Cambios respecto a OSDi 1.2

**Método.** Comparación semántica (rdflib) entre `OSDi.ttl` del repositorio de OSDi en el commit de la publicación de la 1.2 (`1d424a5`) y la copia actual de `jades-hta`. Se compara cada entidad con nombre con todas sus tripletas, expandiendo los nodos anónimos (restricciones, uniones, listas). La copia de `jades-hta` justo antes de la fase 2 (`646cba0~1`) es idéntica a la 1.2 publicada, así que las diferencias son exactamente las de las fases 2 a 18.

**Resultado.** 65 entidades distintas: 27 nuevas y 38 cambiadas. No se ha eliminado ninguna entidad. La cabecera (`owl:Ontology`) no ha cambiado: sigue declarando `owl:versionIRI <…/OSDi/1.2>` y `owl:versionInfo "1.2"`.

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

- **Individuos.** Nuevos `DI_HazardRatio` y `DI_OddsRatio` (`DataItemType`), sin `hasRefToSTATO` (véase el apartado 3).
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

- **Horizonte temporal (fase 11).** Comentario de `hasTimeHorizon` (inglés y español).
- **Evans et al. (fase 12).** Nuevo individuo `Currency_CanadianDollar`; comentario de `hasProportionWithinGroup` (proporción de una estrategia dentro de un grupo de alternativas; añade el español).

## 2. Ejemplos para el repositorio de OSDi

**Hecho.** `jades-hta/examples/evans1997-migraine/catalog-v001.xml` resuelve `https://w3id.org/ontologies-ULL/OSDi/1.3` con `../../OSDi.ttl`, es decir, con el `OSDi.ttl` de la raíz del repositorio de OSDi si el ejemplo queda en `examples/evans1997-migraine/`. Así, Protégé abre el ejemplo sin descargar nada.

**No hecho, a propósito: la importación de `evans1997-migraine.ttl` sigue siendo `<https://w3id.org/ontologies-ULL/OSDi/1.2>`.** JaDES solo resuelve en local la `owl:versionIRI` de su copia de `OSDi.ttl`, que sigue siendo la 1.2. Con la importación de la 1.3, `TestOntologyConsistency` y la suite de validación intentarían descargar la 1.3, que aún no está publicada, y fallarían. Para cambiar la versión de la copia habría que editar fuera de `docs/` y `examples/`. El cambio es una línea (`owl:imports <https://w3id.org/ontologies-ULL/OSDi/1.3>`) y conviene hacerlo a la vez que el de la copia, según lo que decidas en el apartado 5.

**Al mover `examples/` al repositorio de OSDi:**

1. Copiar `jades-hta/examples/` a `examples/` en la raíz del repositorio de OSDi, con su `README.md` de índice.
2. Cambiar la importación de cada ejemplo a la 1.3 (arriba).
3. El IRI del ejemplo, `https://w3id.org/ontologies-ULL/OSDi/examples/evans1997-migraine`, no se resuelve hoy: w3id solo tiene reglas para `OSDi`, `OSDi/X.Y` y `OSDi/X.Y/individuals/…`, y el flujo `generate_doc.yml` solo copia `individuals/` a `docs/X.Y/`. Hay dos opciones:
   - **Recomendada:** un paso en `generate_doc.yml` que copie `examples/` a `docs/latest/examples/` y `docs/X.Y/examples/`, y una regla en `w3id.org/ontologies-ULL/OSDi/.htaccess` para `X.Y/examples/(.*)`. El IRI pasaría a ser `…/OSDi/1.3/examples/evans1997-migraine`.
   - Ponerlos en `individuals/examples/`, que ya se publica y resuelve, pero mezcla ejemplos de validación con individuos.
4. El índice de `examples/README.md` explica cómo ejecutar la validación con JaDES (`mvn -pl jades-hta -am test -Pvalidation`). En el repositorio de OSDi habrá que decir de qué versión de JaDES se trata, o enlazar el informe.

## 3. Pendiente

**En la ontología**
- `DI_HazardRatio` y `DI_OddsRatio` no tienen `hasRefToSTATO`; los demás tipos de dato sí (p. ej., `DI_RelativeRisk`: STATO_0000245). Hay que confirmar los códigos de STATO antes de añadirlos.
- 37 términos nuevos o con el comentario cambiado tienen comentario en inglés pero no en español: `AcuteManifestation`, `DI_HazardRatio`, `DI_OddsRatio`, `DetectionOutcome`, `DiagnosisIntervention`, `Effect`, `FailureOutcome`, `InconclusiveDetectionOutcome`, `Intervention`, `NegativeDetectionOutcome`, `PositiveDetectionOutcome`, `ScreeningIntervention`, `SuccessOutcome`, `TreatmentOutcome`, `affects`, `appliesOneTime`, `detects`, `failsIf`, `followedByStrategy`, `followedOnFailureBy`, `hasEffectMagnitude`, `hasEvaluationTime`, `hasOutcomeRisk`, `hasRiskCharacterization`, `hasRiskIfDiseased`, `hasRiskIfNotDiseased`, `hasSensitivity`, `hasSpecificity`, `hasStrategy`, `hasStrategyOutcome`, `isAffectedBy`, `isDetectedBy`, `isDisutility`, `isRiskCharacterizationOf`, `isStrategyOutcomeOf`, `resolvesHealthCondition` y `resolvesOnSuccess`. Algunos ya no tenían español en la 1.2.
- La cabecera: `owl:versionIRI` y `owl:versionInfo` a 1.3, `owl:priorVersion <…/OSDi/1.2>` y, si se quiere, una nota de cambios en el comentario.

**En el repositorio de OSDi**
- `individuals/PBD.ttl` importa la 1.1 y `individuals/T1DM.ttl` la 1.0. La `PBD.ttl` publicada declara `detects`, `hasSensitivity` y `hasSpecificity` en las intervenciones, lo que es incoherente con la 1.3 (D11; el mismo caso que N20). Las copias de `jades-hta/src/test/resources` (`PBD.ttl`, `T1DM.ttl`) ya están migradas a D2 y D11 y son coherentes según HermiT (`TestOntologyConsistency`): conviene publicarlas en su lugar, importando la 1.3.
- `individuals/catalog-v001.xml` resuelve la 1.0 con una ruta absoluta de un equipo (`file:/C:/Users/icasrod/git/JaDES-HTA/resources/OSDi.owl`), y el catálogo de la raíz apunta a `docs/latest/ontology.owl`.

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
> - **Effect magnitudes**: the data item types of the magnitude and of the affected parameter define how an effect applies (relative risks and hazard ratios on the equivalent rate, odds ratios on the odds, differences, replacements). New `DI_HazardRatio` and `DI_OddsRatio`.
> - **Strategy outcomes** (`StrategyOutcome`, with `TreatmentOutcome` — `SuccessOutcome`, `FailureOutcome` — and `DetectionOutcome` — positive, negative, inconclusive): mutually exclusive outcomes with their risk, evaluation time, next strategy, payoffs, effects and resolution of the health condition. `failsIf` (now also towards a health condition), `followedOnSuccessBy`, `followedOnFailureBy` and `resolvesOnSuccess` are short forms. Relapses after the response are the failure of a follow-up strategy.
> - **Adverse events** (`AdverseEvent`, `hasAdverseEvent`, `isOccurrenceOf`): problems caused by a strategy, with a mandatory risk per application, one-time payoffs, and effects such as death or the failure of the strategy.
> - **Detection**: `detects`, `hasSensitivity`, `hasSpecificity` and `hasTemporaryThreshold` belong to detection strategies, not to interventions. The disease detected by a detection intervention is its target.
> - **Payoffs per episode**: one-time utilities are QALY losses (a negative loss is a gain); the costs and utilities of an acute manifestation apply per episode.
> - New currency `Currency_CanadianDollar`, and clarified comments of `hasTimeHorizon`, `hasFrequency`, `hasDuration`, `appliesOneTime`, `isDisutility` and `hasProportionWithinGroup`.
>
> ### Breaking changes
>
> - `hasStrategy` on an intervention: use a `StrategyReplacementEffect`.
> - `detects`, `hasSensitivity`, `hasSpecificity` and `hasTemporaryThreshold` on an intervention: move them to its detection strategy.
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

## 5. Copia de referencia después de la 1.3 (necesito tu decisión)

Hoy hay dos copias: la del repositorio de OSDi (la 1.2 publicada) y la de `jades-hta`, que ha evolucionado durante las fases con la misma `owl:versionIRI` 1.2. Durante el desarrollo fue útil, pero tiene un coste: un fichero que dice ser la 1.2 sin serlo, y ontologías de prueba que importan la 1.2 y dependen de cambios de la 1.3.

**Opción A: OSDi fijada a una versión publicada (recomendada).** El repositorio de OSDi es la referencia; `jades-hta` usa una copia idéntica a una versión publicada.
- **Sincronización:** al publicar una versión, se copia su `OSDi.ttl` a `jades-hta`. Un test compararía la copia con un hash, o con la versión que declara, para detectar cualquier edición local.
- **Cambios futuros:** se proponen en el repositorio de OSDi como 1.3.x si son aditivos, o 1.4 si cambian el significado de algo publicado (es la regla que ya prevé el plan para las fases 20 a 23). Durante el desarrollo, JaDES puede usar una copia de borrador con su propia `owl:versionIRI` (p. ej., `…/OSDi/1.4`, `versionInfo "1.4-dev"`), nunca con la de una versión publicada.
- **Ventajas:** un único significado para cada IRI de versión; los ejemplos y las ontologías de prueba importan una versión que existe; quien use OSDi sin JaDES ve lo mismo que JaDES.
- **Coste:** cada cambio de ontología que necesite el DES pasa por una publicación, o por un borrador explícito.

**Opción B: la copia de JaDES como desarrollo de la siguiente versión.** `jades-hta` sigue siendo donde se cambia la ontología, y el repositorio de OSDi recibe cada versión cuando se publica, como ahora.
- **Sincronización:** al publicar, se traslada la copia (como en esta fase) y se sube la `owl:versionIRI` de la copia de JaDES a la siguiente versión.
- **Ventajas:** cambiar la ontología y el código que la lee va en el mismo commit, como en las fases 2 a 18.
- **Coste:** la copia de JaDES es siempre una versión no publicada. Las ontologías de prueba y los ejemplos importan una versión que no existe fuera de JaDES, y hay que acordarse de trasladar.

**En los dos casos, al publicar la 1.3** (fuera de `docs/` y `examples/`, así que necesita tu aprobación):
- Cambiar la `owl:versionIRI` y la `owl:versionInfo` de la copia de `jades-hta` a 1.3.
- Actualizar a la vez los `owl:imports` de todas las ontologías de prueba y de los ejemplos, porque `OSDiWrapper.Builder.build` solo resuelve en local la versión de su copia (CLAUDE.md, notas técnicas).
- Pasar la suite completa y la de validación.

## Pasos en el repositorio de OSDi, una vez decidido

1. Copiar la `OSDi.ttl` de `jades-hta` a la raíz, con la cabecera de la 1.3 (apartado 3).
2. Sustituir `individuals/PBD.ttl` y `individuals/T1DM.ttl` por las copias de `jades-hta`, importando la 1.3, y corregir `individuals/catalog-v001.xml`.
3. Añadir `examples/` (apartado 2), con su paso de publicación y su regla de w3id.
4. Añadir las notas de la versión (apartado 4) al `README.md` o a un `CHANGELOG`.
5. Publicar. `generate_doc.yml` genera `docs/1.3` a partir de la `owl:versionIRI`.
