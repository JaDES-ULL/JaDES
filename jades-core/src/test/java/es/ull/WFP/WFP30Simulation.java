package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.StructuredPartialJoinFlow;

/**
 * WFP 30. Expedición Cheques
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
// TODO: Check carefully
public class WFP30Simulation extends StandardTestSimulation {

	/**
	 * @param type
	 * @param id
	 * @param detailed
	 */
	public WFP30Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP30: EjExpedicionCheques", args);
	}

	/* (non-Javadoc)
	 * @see com.ull.simulation.test.WFP.WFPTestSimulationFactory#createModel(Model model)
	 */
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
        
        StructuredPartialJoinFlow root = new StructuredPartialJoinFlow(this, 2);
        root.addBranch(act0_0);
        root.addBranch(act0_1);
        root.addBranch(act0_2);
        root.link(act1);
        
        getDefGenerator(getDefElementType("Peticion de cheque"), root);
	}
}
