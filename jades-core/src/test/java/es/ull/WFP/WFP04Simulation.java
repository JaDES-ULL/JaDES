package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.NotCondition;
import es.ull.simulation.condition.TrueCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ExclusiveChoiceFlow;

/**
 * WFP 4 Example 1: Sistema Votación
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP04Simulation extends StandardTestSimulation {
	int ndays;
	
	public WFP04Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP4: Exclusive Choice. EjSistemaVotacion", args);
    }
    
    protected void createModel() {
        ResourceType rt = getDefResourceType("Encargado");
        WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt}, new int[] {1});
        
        ActivityFlow act0 = getDefActivity("Celebrar elecciones", wg, false);
        ActivityFlow act1 = getDefActivity("Recuentos de votos", wg, false);
        ActivityFlow act2 = getDefActivity("Declarar resultados", wg, false);
        
        getDefResource("Encargado 1", rt); 

        ExclusiveChoiceFlow excho1 = new ExclusiveChoiceFlow(this);
        
        act0.link(excho1);
        AbstractCondition<ElementInstance> falseCond = new NotCondition<ElementInstance>(new TrueCondition<ElementInstance>());
        excho1.link(act1);
        excho1.link(act2, falseCond);

        getDefGenerator(getDefElementType("Votante"), act0);

    }
	
}
