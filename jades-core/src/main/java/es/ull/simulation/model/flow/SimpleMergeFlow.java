package es.ull.simulation.model.flow;

import java.util.Map;
import java.util.TreeMap;

import es.ull.simulation.model.Element;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;


/**
 * Creates an OR IFlow which allows all the true incoming branches to pass. The 
 * outgoing branch is activated only once when several incoming barnches arrive at
 * the same simulation time. 
 * Meets the Simple Merge pattern (WFP5).
 * @author ycallero
 *
 */
public class SimpleMergeFlow extends ORJoinFlow {
	protected Map<Element, Long> lastTs = new TreeMap<Element, Long>();
	
	/**
	 * Creates a new SimpleMergeFlow.
	 * @param model Simulation this IFlow belongs to.
	 */
	public SimpleMergeFlow(Simulation model) {
		super(model);
	}

	@Override
	protected boolean canPass(ElementInstance ei) {
		final Element elem = ei.getElement();
		if (!lastTs.containsKey(elem)) {
			lastTs.put(elem, (long)-1);
		}
		if (ei.isExecutable() && (elem.getTs() > lastTs.get(elem))) {
			lastTs.put(elem, elem.getTs());
			return true;
		}
		return false;
	}
	
	@Override
	protected void reset(ElementInstance wThread) {
		lastTs.remove(wThread.getElement());
		super.reset(wThread);
	}
}
