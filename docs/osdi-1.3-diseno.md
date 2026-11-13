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
