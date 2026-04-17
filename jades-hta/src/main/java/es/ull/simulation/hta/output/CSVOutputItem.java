package es.ull.simulation.hta.output;

import java.io.PrintWriter;
import es.ull.simulation.hta.inforeceiver.CSVProducer;
import es.ull.simulation.hta.inforeceiver.ListenerCollector;
import es.ull.simulation.hta.inforeceiver.TxtProducer;

public class CSVOutputItem extends OutputItem<CSVProducer> {
    public CSVOutputItem(PrintWriter out) {
        super(out);
    }

    @Override
    public void processAfterAllExperiments() {
        // Do Nothing
        
    }

    @Override
    public void processAfterSingleExperiment(int simulationId) {
        if (simulationId == 0) {
            print("ID" + TxtProducer.DEFAULT_SEP);
            for (ListenerCollector listenerCollector : getListenerCollectors()) {
                if (listenerCollector instanceof CSVProducer) {
                    CSVProducer producer = (CSVProducer) listenerCollector;
                    print(producer.getStrHeader());
                }
            }
            println("");
        }
        print(simulationId + TxtProducer.DEFAULT_SEP);
        for (ListenerCollector listenerCollector : getListenerCollectors()) {
            if (listenerCollector instanceof CSVProducer) {
                CSVProducer producer = (CSVProducer) listenerCollector;
                print(producer.getPartialFormattedResult(simulationId));
            }
        }
        println("");
        
    }    
}
