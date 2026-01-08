package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;

/**
 * WFP 19. Cancel Task: Credit card
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP19Simulation extends StandardTestSimulation {
	private boolean pass;

	/**
	 * Constructs a new WFP19Simulation object.
	 * WFP19Simulation represents a simulation specific to the WFP 19 scenario: Cancel Task - Credit Card.
	 *
	 * @param id  The identifier for this simulation.
	 */
	public WFP19Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP19: Cancel Task. EjTarjetaCredito", args);
		pass = false;
	}

	/**
	 * @return the pass
	 */
	public boolean isPass() {
		return pass;
	}
	/**
	 * 
	 */
	public void switchPass() {
		pass = !pass;
	}

	@Override
	protected void createModel() {
		ResourceType rt0 = getDefResourceType("Bank teller");
        WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt0}, new int[] {1});
        
        getDefResource("BankTeller1", rt0);
        getDefResource("BankTeller2", rt0);
        getDefResource("BankTeller3", rt0);

        // FIXME: NO FUNCIONABA!!!
    	final ActivityFlow act0 = new TestActivityFlow("Verify account", 0, wg, false) {
    		@Override
    		public boolean beforeRequest(ElementInstance fe) {
    			switchPass();
    			return isPass() && super.beforeRequest(fe);
    		}
    	};
    	final ActivityFlow act1 = getDefActivity("Obtain card details", wg, false);
        
        
        act0.link(act1);

        getDefGenerator(getDefElementType("Client"), act0);
	}
}
