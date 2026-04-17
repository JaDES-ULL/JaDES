package es.ull.simulation.hta.output;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;

/**
 * Deserializer for DisutilityCombinationMethod enum
 * @author Iván Castilla Rodríguez
 */
public final class JsonDisutilityCombinationMethodDeserializer
        extends JsonDeserializer<DisutilityCombinationMethod> {

    @Override
    public DisutilityCombinationMethod deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        String v = p.getValueAsString();
        if (v == null) {
            return null; // Letting the caller handle null values
        }

        return switch (v) {
            case "additive" -> DisutilityCombinationMethod.ADD;
            case "maximum"  -> DisutilityCombinationMethod.MAX;
            default -> throw JsonMappingException.from(
                    p,
                    "Unknown disutilityCombinationMethod: '" + v + "'. Expected: additive|maximum"
            );
        };
    }
}
