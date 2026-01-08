package es.ull.simulation.inforeceiver;

import java.util.ArrayList;

import es.ull.simulation.info.IPieceOfInformation;

/**
 * Classes implementing this interface are interested in receiving information from the simulation. Once received, the information
 * is processed in the method {@link #infoEmited(IPieceOfInformation)}.
 */
public interface IListener {
	/**
	 * Processes the received information.
	 * @param info The piece of information that has been received.
	 */
	void infoEmited(IPieceOfInformation info);

	/**
	 * Makes this listener listen to the specified {@link IPieceOfInformation piece of information}.
	 * @param cl The class of the piece of information to listen to.
	 */
	void addTargetInformation(Class<? extends IPieceOfInformation> cl);

	/**
	 * Returns a list with the {@link IPieceOfInformation pieces of information} this listener is interested in.
	 * @return A list with the {@link IPieceOfInformation pieces of information} this listener is interested in.
	 */
	public ArrayList<Class<? extends IPieceOfInformation>> getTargetInformation();    
}