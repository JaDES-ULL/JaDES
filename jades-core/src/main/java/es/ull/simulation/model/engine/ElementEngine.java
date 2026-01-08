package es.ull.simulation.model.engine;

import java.util.ArrayList;

import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.RequestResourcesFlow;

/**
 * Represents case instances that make use of activity flows in order to carry out
 * their events.
 * 
 * @author Iván Castilla Rodríguez
 */
public class ElementEngine extends AbstractEngineObject {
	/** Activity queues in which this element is. This list is used to notify the activities
	 * when the element becomes available. */
	protected final ArrayList<ElementInstance> inQueue = new ArrayList<ElementInstance>();
	/** The associated {@link Element} */
	private final Element modelElem;
	
	/**
	 * Creates a new element.
	 * @param simul imulationEngine object
	 * @param modelElem Element type this element belongs to
	 */
	public ElementEngine(SimulationEngine simul, Element modelElem) {
		super(modelElem.getIdentifier(), simul, "E");
		this.modelElem = modelElem;
	}

	/**
	 * Returns the associated {@link Element}
	 * @return the associated {@link Element}
	 */
	public Element getModelElem() {
		return modelElem;
	}

	/**
	 * Notifies a new IFlow executor is waiting in an activity queue.
	 * @param fe IFlow executor waiting in queue.
	 */
	public void incInQueue(ElementInstance fe) {
		inQueue.add(fe);
	}

	/**
	 * Notifies a IFlow executor has finished waiting in an activity queue.
	 * @param fe IFlow executor that was waiting in a queue.
	 */
	public void decInQueue(ElementInstance fe) {
		inQueue.remove(fe);
	}

	public void notifyAvailableElement() {
		for (final ElementInstance fe : inQueue) {
            final RequestResourcesFlow act = (RequestResourcesFlow) fe.getCurrentFlow();
			if (act.isInExclusiveActivity()) {
	            act.getManager().notifyAvailableElement(fe);
			}
		}
	}

    public void notifyEnd() {
        simul.addEvent(modelElem.onDestroy(simul.getTs()));
    }

	/**
	 * Acquires a semaphore associated to a specific IFlow. 
	 * Useful only for parallel implementations
	 * @param IFlow The IFlow to be requested
	 */
	public void waitProtectedFlow(IFlow IFlow) {
		// Nothing to do		
	}

	/**
	 * Releases a semaphore associated to a specific IFlow
	 * Useful only for parallel implementations
	 * @param IFlow The IFlow to be requested
	 */
	public void signalProtectedFlow(IFlow IFlow) {
		// Nothing to do		
	}

	public ElementInstanceEngine getElementInstance(ElementInstance ei) {
		return new ElementInstanceEngine(simul, ei);
	}
    
}
