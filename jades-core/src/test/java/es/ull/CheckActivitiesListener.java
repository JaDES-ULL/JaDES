/**
 * 
 */
package es.ull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.TreeMap;

import es.ull.simulation.info.ElementActionInfo;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.DelayFlow;
import es.ull.simulation.model.flow.ITaskFlow;
/**
 * Checks the elements created and finished during the simulation
 * @author Iván Castilla Rodríguez
 *
 */
public class CheckActivitiesListener extends BasicListener {
	private final static String ERROR_ACQ_NOT_REQ = "Resources acquired but not requested";
	private final static String ERROR_START_NOT_REQ = "Activity started but not requested";
	private final static String ERROR_END_NOT_REQ = "Activity ended but not requested";
	private final static String ERROR_END_NOT_START = "Activity ended but not started";
	private final static String ERROR_REL_NOT_ACQ = "Resources released but not acquired";
	private final static String ERROR_DURATION = "The activity did not last the expected time";
	private final static String ERROR_FINISHED = "Not all the activities requested were finished";
	private final static String ERROR_EXCLUSIVE = "Element in exclusive mode accessing another exclusive activity";
	
	private final PairQueue [] request;
	private final PairQueue [] start;
	private final PairQueue [] acquire;
	private final ArrayList<Long> actDuration;
	private final TreeMap<ITaskFlow, Integer> actIndex;
	private final boolean []exclusive;
	private final TrioQueue[] expectedTerminations;

	/**
	 * Constructs a new CheckActivitiesListener object.
	 * CheckActivitiesListener represents a listener used to monitor activities in a simulation.
	 *
	 * @param nElems        The total number of elements in the simulation.
	 * @param actIndex      A TreeMap where each key is an ActivityFlow and the value is the index of the activity.
	 * @param actDuration   An ArrayList containing the duration of each activity.
	 */
	public CheckActivitiesListener(final int nElems, final TreeMap<ITaskFlow, Integer> actIndex,
								   final ArrayList<Long> actDuration) {
		super("Activity checker ");
		this.actIndex = actIndex;
		this.actDuration = actDuration;
		exclusive = new boolean[nElems];
		request = new PairQueue[actDuration.size()];
		start = new PairQueue[actDuration.size()];
		acquire = new PairQueue[actDuration.size()];
		expectedTerminations = new TrioQueue[actDuration.size()];
		for (int i = 0; i < actDuration.size(); i++) {
			request[i] = new PairQueue();
			start[i] = new PairQueue();
			acquire[i] = new PairQueue();
			expectedTerminations[i] = new TrioQueue();
		}
		addTargetInformation(ElementActionInfo.class);
	}

	private int find(PairQueue queue, ElementActionInfo eInfo) {
		int index = 0;
		for (; index < queue.size(); index++) {
			final ElementActionInfo info = queue.get(index);
			if (info.getElementInstance().equals(eInfo.getElementInstance())) {
				return index;
			}
		}
		return -1;
	}
	
	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof ElementActionInfo) {
			final ElementActionInfo eInfo = (ElementActionInfo)info;
			if (eInfo.getActivity() instanceof ActivityFlow) {
				checkActivityFlow(eInfo);
			}
			else if (eInfo.getActivity() instanceof DelayFlow) {
				checkDelayFlow(eInfo);
			}
		}
		else if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType()))  {
				for (int actId = 0; actId < request.length; actId++) {
					assertFalse(request[actId].size() == 0, "[ACT" + actId + "]" + "\t" + ERROR_FINISHED);
				}
			}
		}
		
	}

	private void checkDelayFlow(ElementActionInfo eInfo) {
		final DelayFlow act = (DelayFlow)eInfo.getActivity(); 
		final int actId = actIndex.get(act);
		switch(eInfo.getType()) {
		case END:
			final int indexStart = find(start[actId], eInfo);
			assertNotEquals(indexStart, -1, eInfo.getElement().toString() + "\t" + ERROR_END_NOT_START);
			if (indexStart != -1) {
				assertEquals(expectedTerminations[actId].get(eInfo.getElementInstance())[0], eInfo.getTs(), eInfo.getElement().toString() + "\t" + ERROR_DURATION + " " + act.getDescription());
				expectedTerminations[actId].remove(eInfo.getElementInstance());
				start[actId].remove(indexStart);
			}
			break;
		case START:
			start[actId].add(eInfo);
			Long[] expected = new Long[2];
			expected[0] = eInfo.getTs() + actDuration.get(actId);
			expected[1] = eInfo.getTs();
			expectedTerminations[actId].put(eInfo.getElementInstance(), expected);
			break;
		default:
			break;
		}
	}

	private void checkActivityFlow(ElementActionInfo eInfo) {
		final ActivityFlow act = (ActivityFlow)eInfo.getActivity().getParent(); 
		final int actId = actIndex.get(act);
		switch(eInfo.getType()) {
		case ACQ:
			assertNotEquals(find(request[actId], eInfo), -1, eInfo.getElement().toString() + "\t" + ERROR_ACQ_NOT_REQ);
			acquire[actId].add(eInfo);
			break;
		case END:
			final int indexReq = find(request[actId], eInfo);
			assertNotEquals(indexReq, -1, eInfo.getElement().toString() + "\t" + ERROR_END_NOT_REQ);
			if (indexReq != -1) {
				request[actId].remove(indexReq);
			}
			final int indexStart = find(start[actId], eInfo);
			assertNotEquals(indexStart, -1, eInfo.getElement().toString() + "\t" + ERROR_END_NOT_START);
			if (indexStart != -1) {
				assertEquals(expectedTerminations[actId].get(eInfo.getElementInstance().getParent())[0], eInfo.getTs(), eInfo.getElement().toString() + "\t" + ERROR_DURATION + " " + act.getDescription());
				expectedTerminations[actId].remove(eInfo.getElementInstance().getParent());
				start[actId].remove(indexStart);
			}
			if (act.isExclusive() && exclusive[eInfo.getElement().getIdentifier()]) {
				exclusive[eInfo.getElement().getIdentifier()] = false;
			}
			break;
		case REL:
			final int indexAcq = find(acquire[actId], eInfo);
			assertNotEquals(indexAcq, -1, eInfo.getElement().toString() + "\t" + ERROR_REL_NOT_ACQ);
			if (indexAcq != -1) {
				acquire[actId].remove(indexAcq);
			}
			break;
		case REQ:
			request[actId].add(eInfo);
			break;
		case START:
			assertNotEquals(find(request[actId], eInfo), -1, eInfo.getElement().toString() + "\t" + ERROR_START_NOT_REQ);
			start[actId].add(eInfo);
			if (act.isExclusive()) {
				assertFalse(exclusive[eInfo.getElement().getIdentifier()], eInfo.getElement().toString() + "\t" + ERROR_EXCLUSIVE + " " + act.getDescription());
				if (!exclusive[eInfo.getElement().getIdentifier()]) {
					exclusive[eInfo.getElement().getIdentifier()] = true;
				}
			}
			Long[] expected = new Long[2];
			expected[0] = eInfo.getTs() + actDuration.get(actId);
			expected[1] = eInfo.getTs();
			expectedTerminations[actId].put(eInfo.getElementInstance().getParent(), expected);
			break;
		case RESACT:
			long expectedTs = expectedTerminations[actId].get(eInfo.getElementInstance().getParent())[0];
			long delay = eInfo.getTs() - expectedTerminations[actId].get(eInfo.getElementInstance().getParent())[1];
			expectedTerminations[actId].get(eInfo.getElementInstance().getParent())[0] = expectedTs + delay;
			start[actId].add(eInfo);
		break;
		case INTACT:
			expectedTerminations[actId].get(eInfo.getElementInstance().getParent())[1] = eInfo.getTs();
			final int indexPreviousStart = find(start[actId], eInfo);
			assertNotEquals(indexPreviousStart, -1, eInfo.getElement().toString() + "\t" + ERROR_END_NOT_START);
			start[actId].remove(eInfo);
			break;
		default:
			break;
		
		}
	}

	private static class PairQueue extends ArrayList<ElementActionInfo> {
		private static final long serialVersionUID = 1L;

		public PairQueue() {
			super();
		}
	}

	private static class TrioQueue extends TreeMap<ElementInstance, Long[]> {
		private static final long serialVersionUID = 1L;

		public TrioQueue() {
			super();
		}
	}
}
