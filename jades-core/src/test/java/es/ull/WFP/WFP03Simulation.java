package es.ull.WFP;

import es.ull.StandardTestSimulation;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.ParallelFlow;
import es.ull.simulation.model.flow.SynchronizationFlow;

/**
 * WFP 3, example 1: Envío de mercancias
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP03Simulation extends StandardTestSimulation {
	
	public WFP03Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP3: Synchronization. EjEnvioMercacias", args);
    }
    
    protected void createModel() {
        ResourceType rt = getDefResourceType("Comercial");

        WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt}, new int[] {1});
        ActivityFlow act0 = getDefActivity("Generacion de factura", wg, false);
        ActivityFlow act1 = getDefActivity("Comprobacion de factura", 10L, wg, false);
        ActivityFlow act2 = getDefActivity("Envio de mercancias", wg, false);
        

        getDefResource("Comercial1", rt);
        getDefResource("Comercial2", rt);
        getDefResource("Comercial3", rt);
        
        ParallelFlow root = new ParallelFlow(this);
        SynchronizationFlow synchro1 = new SynchronizationFlow(this);
        
        root.link(act0);
        root.link(act1);
        act0.link(synchro1);
        act1.link(synchro1);
        synchro1.link(act2);
        
        getDefGenerator(getDefElementType("Cliente"), root);

//        addInfoReceiver(new WFP03CheckView(this, detailed));
//        getSimulation().addInfoReceiver(new CheckFlowsView(getSimulation(), root,
//        new Time[] {DEFACTDURATION[0], DEFACTDURATION[1], DEFACTDURATION[0]}, detailed));
    }
	
}
