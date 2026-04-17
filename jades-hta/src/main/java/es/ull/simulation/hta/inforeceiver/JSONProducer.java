package es.ull.simulation.hta.inforeceiver;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A JSON producer for the HTA simulation. It is responsible for producing JSON files with the results of the simulation.
 */
public interface JSONProducer extends OutputProducer{

    /**
     * Produces a JSON object with results of all the experiments.
     * @return A JSON object with results of all the experiments
     */
    ObjectNode produceJSON();
}
