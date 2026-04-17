package es.ull.simulation.hta.output;

import java.io.PrintWriter;
import java.util.ArrayList;

import es.ull.simulation.hta.HTAExperiment;
import es.ull.simulation.hta.inforeceiver.ListenerCollector;
import es.ull.simulation.hta.inforeceiver.BasicHTAListener;
import es.ull.simulation.hta.inforeceiver.OutputProducer;

public abstract class OutputItem<T extends OutputProducer> implements OutputProcessor<T> {
    private final PrintWriter out;
    private final ArrayList<ListenerCollector> listenerCollectors;

    public OutputItem(PrintWriter out) {
        this.out = out;
        this.listenerCollectors = new ArrayList<>();
    }

    public void addListenerCollector(ListenerCollector listenerCollector) {
        listenerCollectors.add(listenerCollector);
    }

    public ArrayList<ListenerCollector> getListenerCollectors() {
        return listenerCollectors;
    }
    
    public void print(String s) {
        out.print(s);
    }

    public void println(String s) {
        out.println(s);
    }

    public void close() {
        out.close();
    }
    
    public void flush() {
        out.flush();
    }  
    
    @Override
    public ArrayList<BasicHTAListener> getListeners(HTAExperiment exp) {
        final ArrayList<BasicHTAListener> listeners = new ArrayList<>();
        for (ListenerCollector listenerCollector : listenerCollectors) {
            listeners.add(listenerCollector.createListener(exp));
        }
        return listeners;
    }
}
