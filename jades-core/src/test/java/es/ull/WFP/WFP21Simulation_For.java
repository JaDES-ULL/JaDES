package es.ull.WFP;


import es.ull.StandardTestSimulation;
import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.model.ElementType;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ForLoopFlow;

/**
 * WFP 21. Example 2: Revelado fotográfico (implemented with a for structure)
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP21Simulation_For extends StandardTestSimulation {

	/**
	 * @param type
	 * @param id
	 * @param detailed
	 */
	public WFP21Simulation_For(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP21: Structured Loop (For). EjReveladoFotografico", args);
	}

	/* (non-Javadoc)
	 * @see com.ull.simulation.test.WFP.WFPTestSimulationFactory#createModel(Model model)
	 */
	@Override
	protected void createModel() {
		ResourceType rt0 = getDefResourceType("Maquina revelado");
        
        WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt0}, new int[] {1});
    	
    	ActivityFlow act0 = getDefActivity("Revelar foto", wg, false);

        getDefResource("Maquina 1", rt0);        
        getDefResource("Maquina 2", rt0);
        
        ForLoopFlow root = new ForLoopFlow(this, act0, TimeFunctionFactory.getInstance("ConstantVariate", 2));

        ElementType et = getDefElementType("Cliente");
        getDefGenerator(et, root);
	}
}
