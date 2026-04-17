package es.ull.simulation.hta.output;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = false)
public final class JsonEpiOutput implements JsonOutputSpec {

    private final String name;
    private final String description;
    private final Boolean relative;
    private final Integer intervalSize;
    private final Boolean basedOnAge;

    @JsonCreator
    public JsonEpiOutput(
            @JsonProperty(value = "name", required = true) String name,
            @JsonProperty("description") String description,
            @JsonProperty("relative") Boolean relative,
            @JsonProperty("intervalSize") Integer intervalSize,
            @JsonProperty("basedOnAge") Boolean basedOnAge
    ) {
        this.name = name;
        this.description = description;
        this.relative = relative;
        this.intervalSize = intervalSize;
        this.basedOnAge = basedOnAge;
    }

    @Override public String getName() { return name; }
    @Override public String getDescription() { return description; }

    public Boolean getRelative() { return relative; }
    public Integer getIntervalSize() { return intervalSize; }
    public Boolean getBasedOnAge() { return basedOnAge; }
}
