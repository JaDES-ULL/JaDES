package es.ull.simulation.inforeceiver;

import java.util.ArrayList;

import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.model.IDescribable;

/**
 * A basic implementation of a listener that is interested in receiving information from the simulation.
 */
public abstract class BasicListener implements IDescribable, IListener {
	/** The list of pieces of information this listener is interested in */
	private final ArrayList<Class<? extends IPieceOfInformation>> targetInformation;
	/** The description of the listener */
	private final String description;
	
	/**
	 * Creates a listener with a description.
	 * @param description The description of the listener.
	 */
	public BasicListener (String description) {
		this.description = description;
		targetInformation = new ArrayList<>();
	}

	@Override
	public void addTargetInformation(Class<? extends IPieceOfInformation> cl) {
		targetInformation.add(cl);
	}
	
	public String toString() {
		return description;
	}

	@Override
	public ArrayList<Class<? extends IPieceOfInformation>> getTargetInformation() {
		return targetInformation;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
}
