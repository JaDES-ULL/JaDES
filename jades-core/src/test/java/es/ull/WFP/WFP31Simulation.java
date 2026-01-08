package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ParallelFlow;
import es.ull.simulation.model.flow.PartialJoinFlow;

/**
 * WFP 31. Banco
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP31Simulation extends StandardTestSimulation {

	public WFP31Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP31: Blocking Partial Join. EjBanco", args);
	}

	@Override
	protected void createModel() {
		ResourceType rt0 = getDefResourceType("Director");
	   	
        WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt0}, new int[] {1});

        ActivityFlow act0_0 = getDefActivity("AprobarCuenta", wg, false);
        ActivityFlow act0_1 = getDefActivity("AprobarCuenta", wg, false);
        ActivityFlow act0_2 = getDefActivity("AprobarCuenta", wg, false);
        ActivityFlow act1 = getDefActivity("ExpedirCheque", wg, false);
    	
        getDefResource("Director 1", rt0);        
        getDefResource("Director 2", rt0);       
        
        ParallelFlow root = new ParallelFlow(this);

        PartialJoinFlow part1 = new PartialJoinFlow(this, 2);
        
        root.link(act0_0);
        root.link(act0_1);
        root.link(act0_2);
        act0_0.link(part1);
        act0_1.link(part1);
        act0_2.link(part1);
        part1.link(act1);
        
        getDefGenerator(getDefElementType("Cliente"), root);
	}

}
