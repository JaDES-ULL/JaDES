package es.ull.simulation.hta.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.output.JsonConfigLoader;
import es.ull.simulation.hta.output.JsonOutputGroup;
import es.ull.simulation.hta.output.JsonOutputGroup.Phase;
import es.ull.simulation.hta.output.JsonOutputGroupBuilder;
import es.ull.simulation.hta.output.OutputItem;

/**
 * JSON-based implementation of the HTA output configuration provider.
 * @author Iván Castilla Rodríguez
 */
public class JsonOutputConfigProvider implements IHTAOutputConfigProvider {
    /**
     * The schema resource for validating the JSON configuration file.
     */
    private static String SCHEMA_RESOURCE = "output_config.schema.json";
    /**
     * The list of output groups defined in the configuration file.
     */
    private final List<JsonOutputGroup> outputGroups;

    /**
     * Creates a new JSON output configuration provider.
     * @param jsonConfigFilePath The path to the JSON configuration file.
     */
    public JsonOutputConfigProvider(String jsonConfigFilePath) {
        JsonConfigLoader<JsonOutputGroup[]> loader = new JsonConfigLoader<>(SCHEMA_RESOURCE, JsonOutputGroup[].class);
        try {
            JsonOutputGroup[] groups = loader.load(jsonConfigFilePath);
            this.outputGroups = List.of(groups);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load outputs configuration from: " + jsonConfigFilePath, e);
        }
    }
    @Override
    public List<OutputItem<?>> getBaseCaseOutputItems(HTAExperiment exp) {
        List<OutputItem<?>> items = new ArrayList<>();
        for (JsonOutputGroup group : outputGroups) {
            if (!Phase.PSA_RUNS.equals(group.getPhase())) {
                JsonOutputGroupBuilder builder = new JsonOutputGroupBuilder(group);
                items.add(builder.buildOutputItem(exp, true));
            }
        }
        return items;
    }

    @Override
    public List<OutputItem<?>> getPSAOutputItems(HTAExperiment exp) {
        List<OutputItem<?>> items = new ArrayList<>();
        for (JsonOutputGroup group : outputGroups) {
            if (!Phase.BASE_CASE.equals(group.getPhase())) {
                JsonOutputGroupBuilder builder = new JsonOutputGroupBuilder(group);
                items.add(builder.buildOutputItem(exp, false));
            }
        }
        return items;
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
