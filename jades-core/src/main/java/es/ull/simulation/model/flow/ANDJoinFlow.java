/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;


/**
 * A merge IFlow which allows only one of the incoming branches to pass. Which one
 * passes depends on the <code>acceptValue</code>.
 * @author Iván Castilla Rodríguez
 */
public abstract class ANDJoinFlow extends AbstractMergeFlow {
	/** The number of branches which have to arrive to pass the control thread */
	protected int acceptValue;

	/**
	 * Creates a new AND IFlow.
	 */
	public ANDJoinFlow(Simulation model) {
		this(model, true, 0);
	}
	
	/**
	 * Creates a new AND IFlow
	 * @param acceptValue The number of branches which have to arrive to pass the control thread
	 */
	public ANDJoinFlow(Simulation model, int acceptValue) {
		this(model, true, acceptValue);
	}
	
	/**
	 * Create a new AND IFlow which can be used in a safe context or a general one.
	 * @param safe True for safe context; false in other case
	 */
	public ANDJoinFlow(Simulation model, boolean safe) {
		this(model, safe, 0);
	}
	
	/**
	 * Create a new AND IFlow which can be used in a safe context or a general one.
	 * @param safe True for safe context; false in other case
	 * @param acceptValue The number of branches which have to arrive to pass the control thread
	 */
	public ANDJoinFlow(Simulation model, boolean safe, int acceptValue) {
		super(model, safe);
		this.acceptValue = acceptValue;
	}
	
	/* (non-Javadoc)
	 * @see com.ull.simulation.AbstractMergeFlow#canPass(com.ull.simulation.FlowExecutor)
	 */
	@Override
	protected boolean canPass(ElementInstance wThread) {
		return (!control.get(wThread.getElement()).isActivated() 
				&& (control.get(wThread.getElement()).getTrueChecked() == acceptValue));
	}
	
	/**
	 * Returns the acceptance value for this IFlow.
	 * @return The acceptance value for this IFlow
	 */
	public int getAcceptValue() {
		return acceptValue;
	}	
	
}
