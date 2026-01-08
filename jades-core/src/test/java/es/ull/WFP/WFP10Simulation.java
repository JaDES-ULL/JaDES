package es.ull.WFP;

import java.util.ArrayList;

import es.ull.StandardTestSimulation;
import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.NotCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ResourceType;
import es.ull.simulation.model.WorkGroup;
import es.ull.simulation.model.flow.ActivityFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.MultiChoiceFlow;

/**
 * WFP 10. Arbitrary Cycle
 * @author Yeray Callero
 * @author Iván Castilla Rodríguez
 *
 */
public class WFP10Simulation extends StandardTestSimulation {
	private static final double MAX_CAPACITY = 20.0;
	private double capacity;
	private int deliveries;

	/**
	 * Constructs a new WFP10Simulation object.
	 * WFP10Simulation represents a simulation specific to the WFP 10: Arbitrary Cycle scenario.
	 * This simulation includes functionality to track capacity and deliveries.
	 *
	 * @param id The identifier for this simulation.
	 */
	public WFP10Simulation(int id, TestWFP.TestWFPArguments args) {
		super(id, "WFP10: Arbitrary Cycle. Ej", args);
		capacity = 0.0;
		deliveries = 0;
	}

	/**
	 * @return the litrosIntroducidos
	 */
	private double getCapacity() {
		return capacity;
	}

	/**
	 * 
	 */
	private double fillBin() {
		final double filling = Math.min(Math.random() * 5, MAX_CAPACITY - capacity);
		capacity += filling;
		return filling;
	}

	/**
	 * 
	 */
	private void incDeliveries() {
		this.capacity = 0.0;
		this.deliveries++;
	}
	
	private class WFP10Condition extends AbstractCondition<ElementInstance> {
		
		public WFP10Condition() {
			super();
		}
		
    	@Override
    	public boolean check(ElementInstance fe) {
    		return (getCapacity() < MAX_CAPACITY);
    	}
	}
	
	/* (non-Javadoc)
	 * @see com.ull.simulation.test.WFP.WFPTestSimulationFactory#createModel(Model model)
	 */
	@Override
	protected void createModel() {
    	final ResourceType rt0 = getDefResourceType("Operator");
    	final ResourceType rt1 = getDefResourceType("Special operator");
    	
        final WorkGroup wg = new WorkGroup(this, new ResourceType[] {rt0}, new int[] {1});
	   	

        getDefResource("Operator1", rt0);
        getDefResource("Operator1", rt0);
        getDefResource("Special_operator1", rt1);
        
        final AbstractCondition<ElementInstance> cond = new WFP10Condition();
        
        final MultiChoiceFlow mul1 = new MultiChoiceFlow(this) {
        	@Override
        	public boolean beforeRequest(ElementInstance ei) {
        		ei.getElement().trace("Current volume: " + getCapacity());
         		return true;
        	}
        };
        
    	final ActivityFlow act0 = new TestActivityFlow("Fill bin", 0, wg, false) {
    		@Override
    		public void afterFinalize(ElementInstance ei) {
    			final double filling = fillBin();
    			ei.getElement().trace("Added: "+ filling + " / Current volume: " + getCapacity());
    		}
    	};
    	
    	final ActivityFlow act1 = new TestActivityFlow("Sent bin", 0, wg, false) {
    		@Override
    		public boolean beforeRequest(ElementInstance ei) {
    			incDeliveries();
    			ei.getElement().trace("Sent: " + deliveries);
    			return true;
    		}
    	};
        
        act0.link(mul1);
        ArrayList<IFlow> succList = new ArrayList<IFlow>();
        succList.add(act0);
        succList.add(act1);
        ArrayList<AbstractCondition<ElementInstance>> condList = new ArrayList<>();
        condList.add(cond);
        condList.add(new NotCondition<ElementInstance>(cond));
        mul1.link(succList, condList);
        
        getDefGenerator(getDefElementType("Client"), act0);
	}

}
