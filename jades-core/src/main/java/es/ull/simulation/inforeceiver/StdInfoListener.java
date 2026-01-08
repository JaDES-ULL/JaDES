package es.ull.simulation.inforeceiver;

import java.io.PrintStream;

import es.ull.simulation.info.ElementActionInfo;
import es.ull.simulation.info.ElementInfo;
import es.ull.simulation.info.EntityLocationInfo;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.ResourceInfo;
import es.ull.simulation.info.ResourceUsageInfo;
import es.ull.simulation.info.SimulationStartStopInfo;

/**
 * A listener that prints all the information received to the standard output.
 */
public class StdInfoListener extends BasicListener {
	/** The CPU time when the simulation started */
	private long cpuTime;
	/** The output stream */	
	private final PrintStream out;

	/**
	 * Creates a listener that prints all the information received to the standard output.
	 */
	public StdInfoListener() {
		this(System.out);
	}

	/**
	 * Creates a listener that prints all the information received to the specified output stream.
	 * @param out The output stream where the information will be printed.
	 */
	public StdInfoListener(PrintStream out) {
		super("STANDARD INFO VIEW");
		this.out = out;
		addTargetInformation(SimulationStartStopInfo.class);
		addTargetInformation(ElementActionInfo.class);
		addTargetInformation(ElementInfo.class);
		addTargetInformation(ResourceInfo.class);
		addTargetInformation(ResourceUsageInfo.class);
		addTargetInformation(EntityLocationInfo.class);
	}
	
	@Override
	public void infoEmited(IPieceOfInformation info) {
		out.println(info.toString());
		if (info instanceof SimulationStartStopInfo) { 
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.START.equals(tInfo.getType()))  {
				cpuTime = tInfo.getCpuTime();
			}
			else if (SimulationStartStopInfo.Type.END.equals(tInfo.getType())) {
				out.println("CPU Time = " + ((tInfo.getCpuTime() - cpuTime) / 1000000) + " miliseconds.");
			}
		} 
	}

}
