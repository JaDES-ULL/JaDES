package es.ull.simulation.hta.config;

import java.util.ArrayList;
import java.util.List;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.output.OutputItem;

/**
 * Default implementation of the output configuration provider, which provides empty lists of output items for both base case and PSA.
 * Output items can be added to the provider using the addBaseCaseOutputItem and addPSAOutputItem methods.
 */
public class BaseHTAOutputConfigProvider implements IHTAOutputConfigProvider {
    /**
     * List of output items to be generated for the base case simulation (base case).
     */
    private final List<OutputItem<?>> baseCaseItems = new ArrayList<>();
    /**
     * List of output items to be generated for the PSA simulations (second-order).
     */
    private final List<OutputItem<?>> psaItems = new ArrayList<>();

    /**
     * Creates a new BaseHTAOutputConfigProvider with empty output item lists.
     */
    public BaseHTAOutputConfigProvider() {
    }

    @Override
    public List<OutputItem<?>> getBaseCaseOutputItems(HTAExperiment exp) {
        return baseCaseItems;
    }

    @Override
    public List<OutputItem<?>> getPSAOutputItems(HTAExperiment exp) {
        return psaItems;
    }

    /**
     * Adds an output item to the list of base case output items.
     * @param item The output item to be added.
     */
    public void addBaseCaseOutputItem(OutputItem<?> item) {
        baseCaseItems.add(item);
    }

    /**
     * Adds an output item to the list of PSA output items.
     * @param item The output item to be added.
     */
    public void addPSAOutputItem(OutputItem<?> item) {
        psaItems.add(item);
    }
}
