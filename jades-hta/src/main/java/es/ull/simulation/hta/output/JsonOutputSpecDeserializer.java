package es.ull.simulation.hta.output;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Set;

public final class JsonOutputSpecDeserializer extends JsonDeserializer<JsonOutputSpec> {

    // Según el esquema: outputs “core metrics”
    private static final Set<String> CORE_METRICS = Set.of(
            "cost",
            "life_expectancy",
            "quality_adjusted_life_expectancy",
            "time_to_event",
            "manifestation_prevalence",
            "manifestation_incidence",
            "breakdown_costs",
            "individual_outcomes"
    );

    // Según el esquema: outputs epi
    private static final Set<String> EPI = Set.of(
            "incidence",
            "prevalence",
            "cumulative_incidence"
    );

    @Override
    public JsonOutputSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        JsonNode nameNode = node.get("name");
        if (nameNode == null || !nameNode.isTextual()) {
            throw JsonMappingException.from(p, "OutputSpec missing textual field 'name': " + node);
        }

        String name = nameNode.asText();

        if (CORE_METRICS.contains(name)) {
            return codec.treeToValue(node, JsonCoreMetricOutput.class);
        }
        if (EPI.contains(name)) {
            return codec.treeToValue(node, JsonEpiOutput.class);
        }
        if (JsonBudgetImpactOutput.NAME.equals(name)) {
            return codec.treeToValue(node, JsonBudgetImpactOutput.class);
        }

        throw JsonMappingException.from(p, "Unknown OutputSpec name='" + name + "'. Node: " + node);
    }
}
