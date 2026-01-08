package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.DiscriminatorFlow;
import es.ull.simulation.model.flow.ParallelFlow;

/**
 * WFP 28. Comprobacion credenciales
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP28Simulation extends StandardTestSimulation {

	/**
	 * @param type
	 * @param id
	 * @param detailed
	 */
	public WFP28Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP28: Blocking Discriminator. EjComprobacionCredenciales", args);
	}

	/* (non-Javadoc)
	 * @see com.ull.simulation.test.WFP.WFPTestSimulationFactory#createModel(Model model)
	 */
	@Override
	protected void createModel() {
		ResourceType rt0 = getDefResourceType("Asistente");
        ResourceType rt1 = getDefResourceType("Personal Seguridad");
        
        WorkGroup wg0 = new WorkGroup(this, new ResourceType[] {rt0}, new int[] {1});
        WorkGroup wg1 = new WorkGroup(this, new ResourceType[] {rt1}, new int[] {1});
	   	
        ActivityFlow act0 = getDefActivity("Confirmar llegada delegacion", 15L, wg0, false);
        ActivityFlow act1 = getDefActivity("Chequeo de seguridad", 20L, wg1, false);
        ActivityFlow act2 = getDefActivity("Preparacion para nueva delegacion", 15L, wg0, false);

        getDefResource("Asistente 1", rt0);        
        getDefResource("Asistente 2", rt0);
        getDefResource("Segurita 1", rt1);
        getDefResource("Segurita 2", rt1);
        
        ParallelFlow root = new ParallelFlow(this);
        DiscriminatorFlow dis1 = new DiscriminatorFlow(this);
        
        root.link(act0);
        root.link(act1);
        act0.link(dis1);
        act1.link(dis1);
        dis1.link(act2);
        
        getDefGenerator(getDefElementType("Asistente"), root);
	}
}
