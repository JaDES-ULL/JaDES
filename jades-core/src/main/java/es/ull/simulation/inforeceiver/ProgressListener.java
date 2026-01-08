/**
 * 
 */
package es.ull.simulation.inforeceiver;

import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.info.TimeChangeInfo;

/**
 * A listener that prints the progress of the simulation.
 * @author Iván Castilla Rodríguez
 *
 */
public class ProgressListener extends BasicListener {
	/** The next timestamp that the listener is waiting for before printing progression */
	private long nextMsg;
	/** The gap between each message */
	private final long gap;
	/** The percentage of the simulation that has been completed */
	private int percentage;

	/**
	 * Creates a listener that prints the progress of the simulation.
	 * @param endTs The end time of the simulation.
	 */
	public ProgressListener(long endTs) {
		super("Progress");
		addTargetInformation(TimeChangeInfo.class);
		addTargetInformation(SimulationStartStopInfo.class);
		this.gap = endTs / 100;
		this.nextMsg = gap;
		this.percentage = 0;
	}

	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo)info;
		
			if (SimulationStartStopInfo.Type.START.equals(tInfo.getType())) {
				System.out.println("Starting!!");
			}
		}
		else if (info instanceof TimeChangeInfo) {
			if (((TimeChangeInfo) info).getTs() >= nextMsg) {
				System.out.println("" + (++percentage) + "%");
				nextMsg += gap;
			}
		}	
	}
}
