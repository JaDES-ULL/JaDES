package es.ull.simulation.hta.inforeceiver;

import es.ull.simulation.inforeceiver.IListener;

/**
 * 
 */
public interface AttachableToCollector extends IListener {
    ListenerCollector getCollector();
}