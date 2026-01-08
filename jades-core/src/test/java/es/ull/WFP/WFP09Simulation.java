package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.StructuredDiscriminatorFlow;

/**
 * WFP 9. Paro cardiaco
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP09Simulation extends StandardTestSimulation {

	/**
	 * Constructs a new WFP09Simulation object.
	 * WFP09Simulation represents a simulation specific to the WFP9: Structured Discriminator. EjParoCardiaco scenario.
	 *
	 * @param id  The identifier for this simulation.
	 */
	public WFP09Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP9: Structured Discriminator. EjParoCardiaco", args);
	}

	/* (non-Javadoc)
	 * @see com.ull.simulation.test.WFP.WFPTestSimulationFactory#createModel(Model model)
	 */
	@Override
	protected void createModel() {
		ResourceType rt0 = getDefResourceType("Doctor");
	   	
        WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt0}, new int[] {1});

        ActivityFlow act0 = getDefActivity("Comprobar respiracion", wg, false);
        ActivityFlow act1 = getDefActivity("Comprobar pulso", 10L, wg, false);
        ActivityFlow act2 = getDefActivity("Masaje cardiaco", 15L, wg, false);
        
        getDefResource("Doctor 1", rt0);        
        getDefResource("Doctor 2", rt0);        
        
        StructuredDiscriminatorFlow root = new StructuredDiscriminatorFlow(this);
              
        root.addBranch(act0);
        root.addBranch(act1);
        root.link(act2);
        
        getDefGenerator(getDefElementType("Paciente"), root);

	}

}
