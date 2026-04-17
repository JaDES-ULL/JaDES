package es.ull.simulation.hta.output;

import java.util.ArrayList;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.inforeceiver.BasicHTAListener;
import es.ull.simulation.hta.inforeceiver.OutputProducer;

/**
 * Classes implementing this interface are responsible for processing the outputs collected in a set of listeners
 */
public interface OutputProcessor<T extends OutputProducer> {
    public abstract void processAfterSingleExperiment(int simulationId);
    public abstract void processAfterAllExperiments();
    public ArrayList<BasicHTAListener> getListeners(HTAExperiment exp);
}
