/**
 * 
 */
package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ParallelFlow;
import es.ull.simulation.model.flow.ThreadSplitFlow;

/**
 * WFP 12. Multiple Instances without Synchronization
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP12Simulation extends StandardTestSimulation {
	static final int RES = 5;

	/**
	 * Constructs a new WFP12Simulation object.
	 * WFP12Simulation represents a simulation specific to the WFP 12: Multiple Instances without
	 * Synchronization scenario.This scenario explores the behavior of multiple instances without synchronization.
	 *
	 * @param id  The identifier for this simulation.
	 */
	public WFP12Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP12: Multiple Instances without Synchronization", args);
	}

	/* (non-Javadoc)
	 * @see com.ull.simulation.test.WFP.WFPTestSimulationFactory#createModel(Model model)
	 */
	@Override
	protected void createModel() {
		ResourceType rt0 = getDefResourceType("Policeman");
    	WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt0}, new int[] {1});
    	
    	ActivityFlow act0 = getDefActivity("Receive Infringment", 10L, wg);
    	ActivityFlow act1 = getDefActivity("Issue-Infringment-Notice", 30L, wg, false);
    	ActivityFlow act2 = getDefActivity("Coffee", 10L, wg);
    	
    	for (int i = 0; i < RES; i++)
    		getDefResource("RES" + i, rt0);
    	
        ParallelFlow pf = new ParallelFlow(this);
    	act0.link(pf);
    	ThreadSplitFlow tsf = new ThreadSplitFlow(this, 3);
    	tsf.link(act1);
    	pf.link(tsf);
    	pf.link(act2);
    	
        getDefGenerator(getDefElementType("Infringement"), act0);    	
	}

}
