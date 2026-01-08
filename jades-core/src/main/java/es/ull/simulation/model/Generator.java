/**
 * 
 */
package es.ull.simulation.model;

import java.util.ArrayList;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.model.engine.SimulationEngine;

/**
 * Defines the way a generator creates elements when it's time to create them.
 * @author Iván Castilla Rodríguez
 */
public abstract class Generator<INF extends Generator.GenerationInfo> extends SimulationObject {
	/** Number of objects created each time this creator is invoked. */
	protected final AbstractTimeFunction nElem;
	/** Each IFlow that will be generated */
	protected final ArrayList<INF> genInfo = new ArrayList<INF>();

	/**
	 * Creates a creator of elements.
	 * @param nElem Number of objects created each time this creator is invoked.
	 */
	public Generator(final Simulation model, final int id, final AbstractTimeFunction nElem) {
		super(model, id, "GEN");
		this.nElem = nElem;
	}

	/**
	 * Constructs a generator of elements.
	 *
	 * This method creates a generator object responsible for creating a specified number of elements each time it is invoked.
	 *
	 * @param model The Simulation object to which this generator belongs.
	 * @param id    The identifier of this generator.
	 * @param nElem The number of objects created each time this creator is invoked.
	 */
	public Generator(final Simulation model, final int id, final int nElem) {
		this(model, id, TimeFunctionFactory.getInstance("ConstantVariate", nElem));
	}
	
	@Override
	public double getTime() {
		return getTs();
	}
	
	/**
	 * Adds a generation info.
	 * @param info Generaion info
	 */
	public void add(final INF info) {
		genInfo.add(info);
	}

	/**
	 * Allows a user to define actions to be executed before the elements are created. This
	 * method is invoked inside <code>create</code> and can be used to change the current 
	 * amount of elements to be generated. By default, returns the predefined value.
	 * @param n Computed number of elements to create.
	 * @return New number of elements to create.
	 */
	public int beforeCreateElements(final int n) {
		return n; 
	}
	
	// User methods
	/**
	 * Allows a user to define actions to be executed after the elements are created.
	 * This method is invoked inside <code>create</code>. By default, does nothing.
	 */
	public void afterCreateElements() {}
	
	// End of user methods

	/**
	 * Creates a single event source. Every class extending this one must fill in this method to describe
	 * how new event sources, such as
	 * {@link Element}, or {@link Resource} are created. 
	 * @param ind The index of the new event source to be created
	 * @param info The information used to create the event source
	 * @return The newly created event source 
	 */
	public abstract IEventSource createEventSource(final int ind, final INF info);
	
	/**
	 * Creates all the event sources. It uses the specified proportions and the total number to create event sources.
	 * @return The event sources created
	 */
	public IEventSource[] create() {
		int n = getSampleNElem();
		n = beforeCreateElements(n);
		final IEventSource[] elems = new IEventSource[n];
        for (int i = 0; i < n; i++) {
            double p = Math.random();
            for (INF gt : genInfo) {
            	p -= gt.getProp();
            	if (p <= 0.0){
            		elems[i] = createEventSource(i, gt);
            		// Some generators may not create the element for some reason
            		if (elems[i] != null) {
	    	            final DiscreteEvent e = elems[i].onCreate(getTs());
	    	            simul.addEvent(e);
            		}
    	            break;
            	}
            }
        }
        afterCreateElements();
		return elems;
	}
	
	/**
	 * Returns the function that characterizes the total number of entities to be created each
	 * time the {@link #create(long)} method is invoked
	 * @return the function that characterizes the total number of entities to be created each
	 * time the {@link #create(long)} method is invoked
	 */
	public AbstractTimeFunction getNElem() {
		return nElem;
	}

	/**
	 * Returns the total number of entities to be created each time the {@link #create(long)} method is invoked
	 * @return the total number of entities to be created each time the {@link #create(long)} method is invoked
	 */	
	public int getSampleNElem() {
		return (int) nElem.getValue(this);
	}
	
	/**
	 * Returns an array with the generation informations used to create event sources
	 * @return an array with the generation informations used to create event sources
	 */
	public ArrayList<INF> getGenerationInfos() {
		return genInfo;
	}

	@Override
	protected void assignSimulation(final SimulationEngine simul) {
		// Nothing to do
	}

	/**
	 * Description of a set of elements a generator can create.
	 * @author Iván Castilla Rodríguez
	 */
    public static class GenerationInfo {
		/** Proportion of elements corresponding to this IFlow. */
		protected final double prop;
		
		/**
		 * Creates a new kind of elements to generate.
		 * @param prop Proportion of elements corresponding to this IFlow.
		 */
		protected GenerationInfo(final double prop) {
			this.prop = prop;
		}
		
		/**
		 * Returns the proportion of elements to be created of this kind of element.
		 * @return Returns the proportion.
		 */
		public double getProp() {
			return prop;
		}    	
    }
    
	@Override
	public String getObjectTypeIdentifier() {
		return "GEN";
	}
}
