package es.ull.simulation.hta.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import es.ull.simulation.hta.output.JsonExperimentConfig;
import es.ull.simulation.hta.HTAArguments;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;
import es.ull.simulation.hta.output.JsonConfigLoader;

public class BaseHTAExperimentConfigProvider implements IHTAExperimentConfigProvider {
    private static String SCHEMA_RESOURCE = "HTA_config.schema.json";

    private final HTAArguments args;
    private final JsonExperimentConfig jsonConfig;

    public BaseHTAExperimentConfigProvider(HTAArguments args) throws IOException {
        this.args = args;
        if (args.configFile != null) {
            JsonConfigLoader<JsonExperimentConfig> loader = new JsonConfigLoader<>(SCHEMA_RESOURCE, JsonExperimentConfig.class);
            this.jsonConfig = loader.load(args.configFile);
        } else {
            this.jsonConfig = null;
        }
    }

    @Override
    /**
     * Returns the number of second-order simulations to run (base case is considered a separate run)
     * @return the number of second-order simulations to run
     */
    public OptionalInt getNRuns() {
        if (args.commonArgs.getNRuns().isPresent()) {
            return args.commonArgs.getNRuns();
        }
        if (jsonConfig != null && jsonConfig.getPSARuns() != null) {
            return OptionalInt.of(jsonConfig.getPSARuns());
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalLong getSeed() {
        return args.commonArgs.getSeed();
    }

    @Override
    public OptionalInt getTimeHorizon() {
        if (args.commonArgs.getTimeHorizon().isPresent()) {
            return args.commonArgs.getTimeHorizon();
        }
        if (jsonConfig != null && jsonConfig.getTimeHorizon() != null) {
            return OptionalInt.of(jsonConfig.getTimeHorizon());
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalInt getNThreads() {
        return args.commonArgs.getNThreads();
    }

    @Override
    public Optional<Boolean> isParallel() {
        return args.commonArgs.isParallel();
    }

    @Override
    public OptionalInt getNPatients() {
        if (jsonConfig != null && jsonConfig.getPatients() != null) {
            return OptionalInt.of(jsonConfig.getPatients());
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalInt getStudyYear() {
        if (jsonConfig != null && jsonConfig.getStudyYear() != null) {
            return OptionalInt.of(jsonConfig.getStudyYear());
        }
        return OptionalInt.empty();
    }

    @Override
    public OptionalDouble getDefaultDiscountRateForCosts() {
        if (jsonConfig != null && jsonConfig.getDefaultDiscountRateForCosts() != null) {
            return OptionalDouble.of(jsonConfig.getDefaultDiscountRateForCosts());
        }
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble getDefaultDiscountRateForEffects() {
        if (jsonConfig != null && jsonConfig.getDefaultDiscountRateForEffects() != null) {
            return OptionalDouble.of(jsonConfig.getDefaultDiscountRateForEffects());
        }
        return OptionalDouble.empty();
    }

    @Override
    public Optional<DisutilityCombinationMethod> getDisutilityCombinationMethod() {
        if (jsonConfig != null && jsonConfig.getDisutilityCombinationMethod() != null) {
            return Optional.of(jsonConfig.getDisutilityCombinationMethod());
        }
        return Optional.empty();
    }
    
    @Override
    public List<Integer> getDebugPatients() {
        List<Integer> patients = args.debugPatients;
        if (patients == null || patients.isEmpty()) {
            return new ArrayList<>();
        }
        return List.copyOf(patients);
    }

    @Override
    public Optional<Boolean> isBaseCaseEnabled() {
        if (jsonConfig != null && jsonConfig.getBaseCase() != null) {
            return Optional.of(jsonConfig.getBaseCase());
        }
        return Optional.empty();
    }

    /**
     * Returns the schema resource for validating the JSON configuration file.
     * @return The schema resource path
     */
    public static String getSchemaResource() {
        return SCHEMA_RESOURCE;
    }

    /**
     * Sets the schema resource for validating the JSON configuration file.
     * @param schemaResource The schema resource path
     */
    public static void setSchemaResource(String schemaResource) {
        SCHEMA_RESOURCE = schemaResource;
    }

}
