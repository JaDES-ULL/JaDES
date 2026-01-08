/**
 * 
 */
package es.ull.simulation.inforeceiver;

import java.util.concurrent.TimeUnit;

import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.TimeChangeInfo;

/**
 * When used within a simulation, creates a delay each time the simulation clock is updated.
 * The delay lasts {@link DelayListener#timeout} seconds.
 * @author Iván Castilla Rodríguez
 */
public class DelayListener extends BasicListener {
	private final long timeout;
	
	/**
	 * Creates a listener that delays the simulation {@link DelayListener#timeout} seconds every
	 * time the simulation clock is updated.
	 * @param timeout Seconds of delay.
	 */
	public DelayListener(long timeout) {
		super("Delay listener");
		this.timeout = timeout;
		addTargetInformation(TimeChangeInfo.class);
	}

	/* (non-Javadoc)
	 * @see com.ull.simulation.inforeceiver.InfoReceiver#infoEmited(com.ull.simulation.info.SimulationInfo)
	 */
	@Override
	public void infoEmited(IPieceOfInformation info) {
		try {
			TimeUnit.SECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
