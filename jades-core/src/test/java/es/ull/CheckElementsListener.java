/**
 * 
 */
package es.ull;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import es.ull.simulation.info.ElementInfo;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;

/**
 * Checks the elements created and finished during the simulation
 * @author Iván Castilla Rodríguez
 *
 */
public class CheckElementsListener extends BasicListener {
	private final static String ERROR_FINISHED = "Wrong number of elements finished";
	private final static String ERROR_CREATED = "Wrong number of elements created";
	private ArrayList<Integer> elements;
	private int[] elemCreated;
	private int[] elemFinished;

	/**
	 * Constructs a new CheckElementsListener object.
	 * CheckElementsListener represents a listener used to monitor elements in a simulation.
	 *
	 * @param elements  An ArrayList where each element represents the number of elements of a specific type
	 *                  to be created.
	 */
	public CheckElementsListener(final ArrayList<Integer> elements) {
		super("Element checker ");
		this.elements = elements;
		elemCreated = new int[elements.size()];
		elemFinished = new int[elements.size()];
		addTargetInformation(ElementInfo.class);
	}

	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof ElementInfo) {
			ElementInfo eInfo = (ElementInfo)info;
			int et;
			switch(eInfo.getType()) {
			case START:
				et = eInfo.getElement().getType().getIdentifier();
				elemCreated[et]++;
				break;
			case FINISH:
				et = eInfo.getElement().getType().getIdentifier();
				elemFinished[et]++;
				break;
			}
		}
		else if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType()))  {
				for (int i = 0; i < elements.size(); i++) {
					assertEquals(elemFinished[i], elemCreated[i], ERROR_FINISHED + "\tType:" + i);
					assertEquals(elemCreated[i], elements.get(i), ERROR_CREATED + "\tType:" + i);
				}
			}
		}
		
	}

}
