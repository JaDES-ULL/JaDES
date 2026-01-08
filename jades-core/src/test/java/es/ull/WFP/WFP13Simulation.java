/**
 * 
 */
package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.SynchronizedMultipleInstanceFlow;

/**
 * WFP 13. Multiple Instances with a priori design-time knowledge
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP13Simulation extends StandardTestSimulation {
	final static int RES = 6;

	/**
	 * Constructs a new WFP13Simulation object.
	 * WFP13Simulation represents a simulation specific to the WFP 13 scenario: Multiple Instances
	 * with a priori design-time knowledge.
	 *
	 * @param id  The identifier for this simulation.
	 */
	public WFP13Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP13: Multiple Instances with a priori design-time knowledge", args);
	}

	@Override
	protected void createModel() {
		ResourceType rt0 = getDefResourceType("Director");
    	WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt0}, new int[] {1});
    	
    	ActivityFlow act0 = getDefActivity("Sign Annual Report", wg);
    	ActivityFlow act1 = getDefActivity("Check acceptance", wg);
    	
    	for (int i = 0; i < RES; i++)
    		getDefResource("Director" + i, rt0);

		SynchronizedMultipleInstanceFlow root = new SynchronizedMultipleInstanceFlow(this, 6);
    	root.addBranch(act0);
    	root.link(act1);

    	getDefGenerator(getDefElementType("ET0"), root);
	}

}
