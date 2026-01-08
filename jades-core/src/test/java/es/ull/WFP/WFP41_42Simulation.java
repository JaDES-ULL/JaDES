/**
 * 
 */
package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ThreadMergeFlow;
import es.ull.simulation.model.flow.ThreadSplitFlow;

/**
 * WFP 41 - 42. 
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP41_42Simulation extends StandardTestSimulation {

	public WFP41_42Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP41_42: Thread Split-Merge", args);
	}

	@Override
	protected void createModel() {
		ResourceType rt0 = getDefResourceType("Program Chair"); 
		ResourceType rt1 = getDefResourceType("Peer Referee");
		
		WorkGroup wg0 = new WorkGroup(this, new ResourceType[] {rt0}, new int[] {1});
		WorkGroup wg1 = new WorkGroup(this, new ResourceType[] {rt1}, new int[] {1});

    	getDefResource("PC1", rt0);
    	getDefResource("Ref0", rt1);
    	getDefResource("Ref1", rt1);
    	getDefResource("Ref2", rt1);
		
    	ActivityFlow act0 = getDefActivity("Confirm paper receival", wg0, false);
    	ActivityFlow act1 = getDefActivity("Independent Peer review", 120L, wg1, false);
    	ActivityFlow act2 = getDefActivity("Notify authors", wg0, false);
		
        ThreadSplitFlow split = new ThreadSplitFlow(this, 3);
		act0.link(split);
		split.link(act1);
        ThreadMergeFlow merge = new ThreadMergeFlow(this, 3);
        act1.link(merge);
		merge.link(act2);

        getDefGenerator(getDefElementType("ET0"), act0);
	}
}
