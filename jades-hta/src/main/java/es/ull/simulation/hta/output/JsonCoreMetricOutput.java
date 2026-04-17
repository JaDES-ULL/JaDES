package es.ull.simulation.hta.output;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = false)
public final class JsonCoreMetricOutput implements JsonOutputSpec, JsonHasDiscount {

    private final String name;
    private final String description;
    private final Double discount;

    @JsonCreator
    public JsonCoreMetricOutput(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty("description") String description,
            @JsonProperty("discount") Double discount
    ) {
        this.name = name;
        this.description = description;
        this.discount = discount;
    }

    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }
    @Override public Double getDiscount() { return discount; }
}
