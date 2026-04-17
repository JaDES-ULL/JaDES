package es.ull.simulation.hta.output;

import java.io.PrintWriter;
import es.ull.simulation.hta.inforeceiver.ListenerCollector;
import es.ull.simulation.hta.inforeceiver.TxtProducer;

/**
 * An output item that writes the results in a text file.
 */
public class TxtOutputItem extends OutputItem<TxtProducer> {
    /**
     * Creates a new output item that writes the results in a text file.
     * @param out The output stream
     */
    public TxtOutputItem(PrintWriter out) {
        super(out);
    }

    @Override
    public void processAfterAllExperiments() {
        for (ListenerCollector listenerCollector : getListenerCollectors()) {
            if (listenerCollector instanceof TxtProducer) {
                TxtProducer producer = (TxtProducer) listenerCollector;
                println(producer.getStrHeader());
                println(producer.getFormattedResult());
            }        
        }
    }

    @Override
    public void processAfterSingleExperiment(int simulationId) {
        // Do Nothing        
    }
}
