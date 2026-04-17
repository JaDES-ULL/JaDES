package es.ull.simulation.hta.output;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = JsonOutputSpecDeserializer.class)
public sealed interface JsonOutputSpec permits
        JsonCoreMetricOutput,
        JsonEpiOutput,
        JsonBudgetImpactOutput {

    String getName();
    String getDescription();
}
