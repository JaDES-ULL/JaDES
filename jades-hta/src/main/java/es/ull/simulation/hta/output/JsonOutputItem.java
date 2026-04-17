package es.ull.simulation.hta.output;

import java.io.PrintWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.ull.simulation.hta.inforeceiver.JSONProducer;
import es.ull.simulation.hta.inforeceiver.ListenerCollector;

public class JsonOutputItem extends OutputItem<JSONProducer> {
    public JsonOutputItem(PrintWriter out) {
        super(out);
    }

    @Override
    public void processAfterAllExperiments() {
        final ObjectMapper objectMapper = new ObjectMapper();
        final ObjectNode json = objectMapper.createObjectNode();
        final ArrayNode jsonItems = objectMapper.createArrayNode();
        for (ListenerCollector listenerCollector : getListenerCollectors()) {
            if (listenerCollector instanceof JSONProducer) {
                jsonItems.add(((JSONProducer)listenerCollector).produceJSON());
            }
        }
        json.set("items", jsonItems);
        try {
            final String prettyJsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            println(prettyJsonString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processAfterSingleExperiment(int simulationId) {
        // Do nothing        
    }
    
}
