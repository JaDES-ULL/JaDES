package es.ull.simulation.hta.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;

@JsonIgnoreProperties(ignoreUnknown = false)
public class JsonExperimentConfig {

    private final int patients;
    private final Integer timeHorizon;
    private final Integer studyYear;
    private final Boolean baseCase;
    private final Integer PSARuns;
    private final Double defaultDiscountRateForCosts;
    private final Double defaultDiscountRateForEffects;
    private final DisutilityCombinationMethod disutilityCombinationMethod;

    public JsonExperimentConfig(
            @JsonProperty(value = "patients", required = true) int patients,
            @JsonProperty("timeHorizon") Integer timeHorizon,
            @JsonProperty("studyYear") Integer studyYear,
            @JsonProperty("baseCase") Boolean baseCase,
            @JsonProperty("PSARuns") Integer PSARuns,
            @JsonProperty("defaultDiscountRateForCosts") Double defaultDiscountRateForCosts,
            @JsonProperty("defaultDiscountRateForEffects") Double defaultDiscountRateForEffects,
            @JsonDeserialize(using = JsonDisutilityCombinationMethodDeserializer.class)
            @com.fasterxml.jackson.annotation.JsonProperty("disutilityCombinationMethod")
            DisutilityCombinationMethod disutilityCombinationMethod) {
        this.patients = patients;
        this.timeHorizon = timeHorizon;
        this.studyYear = studyYear;
        this.baseCase = baseCase;
        this.PSARuns = PSARuns;
        this.defaultDiscountRateForCosts = defaultDiscountRateForCosts;
        this.defaultDiscountRateForEffects = defaultDiscountRateForEffects;
        this.disutilityCombinationMethod = disutilityCombinationMethod;
    }

    public Integer getPatients() {
        return patients;
    }

    public Integer getTimeHorizon() {
        return timeHorizon;
    }

    public Integer getStudyYear() {
        return studyYear;
    }

    public Boolean getBaseCase() {
        return baseCase;
    }

    public Integer getPSARuns() {
        return PSARuns;
    }

    public Double getDefaultDiscountRateForCosts() {
        return defaultDiscountRateForCosts;
    }

    public Double getDefaultDiscountRateForEffects() {
        return defaultDiscountRateForEffects;
    }

    public DisutilityCombinationMethod getDisutilityCombinationMethod() {
        return disutilityCombinationMethod;
    }
}
