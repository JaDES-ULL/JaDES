/**
 * 
 */
package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.InterleavedRoutingFlow;

/**
 * WFP 40. 
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP40Simulation extends StandardTestSimulation {
	static final int RES = 4;

	public WFP40Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP40: Interleaved Routing", args);
	}

	@Override
	protected void createModel() {
		ResourceType rt = getDefResourceType("Technician");
    	WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt}, new int[] {1});
    	
    	ActivityFlow act0 = getDefActivity("check oil", wg);
    	ActivityFlow act1 = getDefActivity("examine main unit", wg);
    	ActivityFlow act2 = getDefActivity("review warranty", wg);
    	ActivityFlow act3 = getDefActivity("final check", wg);
    	
    	for (int i = 0; i < RES; i++)
    		getDefResource("RES" + i, rt);
    	
    	InterleavedRoutingFlow root = new InterleavedRoutingFlow(this);
    	root.addBranch(act0);
    	root.addBranch(act1);
    	root.addBranch(act2);
    	root.link(act3);

        getDefGenerator(getDefElementType("ET0"), root);
		
	}
}
