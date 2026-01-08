/**
 * 
 */
package es.ull.WFP;

import java.util.ArrayList;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.InterleavedParallelRoutingFlow;

/**
 * WFP 17. Interleaved Parallel Routing
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 * Creates an interleaved paralell routing example with the following activities: A, B, C, D, E, F;
 * and the following dependencies: A -> B, A -> C, C -> D -> E, B -> E. F has no dependencies 
 *
 */
public class WFP17Simulation extends StandardTestSimulation {
	final static int RES = 6;

	/**
	 * Constructs a new WFP17Simulation object.
	 * WFP17Simulation represents a simulation specific to the WFP 17 scenario: Interleaved Parallel Routing.
	 *
	 * @param id  The identifier for this simulation.
	 */
	public WFP17Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP17: Interleaved Parallel Routing", args);
	}

	/* (non-Javadoc)
	 * @see com.ull.simulation.test.WFP.WFPTestSimulationFactory#createModel(Model model)
	 */
	@Override
	protected void createModel() {
		ResourceType rt = getDefResourceType("RT");
    	WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt}, new int[] {1});

    	ArrayList<ActivityFlow> acts = new ArrayList<ActivityFlow>();
    	acts.add(getDefActivity("A", wg));
    	acts.add(getDefActivity("B", wg));
    	acts.add(getDefActivity("C", wg));
    	acts.add(getDefActivity("D", wg));
    	acts.add(getDefActivity("E", wg));
    	acts.add(getDefActivity("F", wg));
    	ActivityFlow finalAct = getDefActivity("G", wg);
    	
    	for (int i = 0; i < RES; i++)
    		getDefResource("RES" + i, rt);
    	
    	// Dependencies
    	ArrayList<ActivityFlow[]> dep = new ArrayList<ActivityFlow[]>();
    	dep.add(new ActivityFlow[] {acts.get(0), acts.get(1)});
    	dep.add(new ActivityFlow[] {acts.get(0), acts.get(2)});
    	dep.add(new ActivityFlow[] {acts.get(2), acts.get(3), acts.get(4)});
    	dep.add(new ActivityFlow[] {acts.get(1), acts.get(4)});
    	
    	InterleavedParallelRoutingFlow root = new InterleavedParallelRoutingFlow(this, acts, dep);
    	root.link(finalAct);

    	getDefGenerator(getDefElementType("ET0"), root);
	}
}
