package es.ull.simulation.hta.output;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public final class JsonConfigLoader<T> {


    private final ObjectMapper objectMapper;
    private final JsonSchema schema;
    private final Class<T> configClass;

    public JsonConfigLoader(String schemaResource, Class<T> configClass) {
        this.objectMapper = new ObjectMapper();

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        try (InputStream schemaStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaResource)) {
            if (schemaStream == null) {
                throw new IllegalStateException("Schema resource not found: " + schemaResource);
            }
            this.schema = schemaFactory.getSchema(schemaStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load schema: " + schemaResource, e);
        }
        this.configClass = configClass;
    }

	/**
	 * Creates a wrapper class from the specified configuration file and validates it against the schema
	 * @param fileName The configuration file name
	 * @return A wrapper class from the specified configuration file
	 * @throws IOException If there is an error reading the configuration file
	 */

    public T load(String fileName) throws IOException {
        try (InputStream jsonStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {

            if (jsonStream == null) {
                throw new IOException("Config resource not found: " + fileName);
            }

            JsonNode json = objectMapper.readTree(jsonStream);

            Set<ValidationMessage> validationResult = schema.validate(json);
            if (!validationResult.isEmpty()) {
                StringBuilder sb = new StringBuilder("Config validation failed for ")
                        .append(fileName).append(":\n");
                for (ValidationMessage vm : validationResult) {
                    sb.append(" - ").append(vm.getMessage()).append('\n');
                }
                throw new IllegalArgumentException(sb.toString());
            }

            // Conversión JsonNode -> POJO (inmutable con @JsonCreator)
            return objectMapper.treeToValue(json, configClass);
        }

    }
}
