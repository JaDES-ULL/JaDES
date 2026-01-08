/**
 * 
 */
package es.ull.simulation.model;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.model.flow.IInitializerFlow;
import es.ull.simulation.model.location.Location;

/**
 * A time-driven generator for {@link Element elements}. Can create different proportions of elements that
 * appear at different locations.
 * @author Ivan Castilla Rodriguez
 *
 */
public class TimeDrivenElementGenerator extends TimeDrivenGenerator<StandardElementGenerationInfo> {
	
	/**
	 * Creates a creator of elements.
	 * @param model The simulation model this generator belongs to
	 * @param nElem Number of objects created each time this creator is invoked.
	 * @param cycle A function to determine when are created the elements
	 */
	public TimeDrivenElementGenerator(final Simulation model, final int nElem, final ISimulationCycle cycle) {
		super(model, nElem, cycle);
	}

	/**
	 * Creates a generator of elements.
	 * @param model The simulation model this generator belongs to
	 * @param nElem Number of objects created each time this generator is invoked
	 * @param cycle A function to determine when are created the elements
	 */
	public TimeDrivenElementGenerator(final Simulation model, final AbstractTimeFunction nElem,
									  final ISimulationCycle cycle) {
		super(model, nElem, cycle);
	}

	/**
	 * Creates a creator of a single type of elements.
	 * @param model The simulation model this generator belongs to
	 * @param nElem Number of objects created each time this creator is invoked.
	 * @param et The type of the elements to be created
	 * @param IFlow The description of the IFlow of the elements to be created.
	 * @param cycle A function to determine when are created the elements
	 */
	public TimeDrivenElementGenerator(final Simulation model, final int nElem, final ElementType et,
									  final IInitializerFlow IFlow, final ISimulationCycle cycle) {
		super(model, nElem, cycle);
		add(new StandardElementGenerationInfo(et, IFlow, 0, null, 1.0));
	}
	
	/**
	 * Creates a creator of a single type of elements.
	 * @param model The simulation model this generator belongs to
	 * @param nElem Number of objects created each time this creator is invoked.
	 * @param et The type of the elements to be created
	 * @param IFlow The description of the IFlow of the elements to be created.
	 * @param cycle A function to determine when are created the elements
	 */
	public TimeDrivenElementGenerator(final Simulation model, final AbstractTimeFunction nElem, final ElementType et,
									  final IInitializerFlow IFlow, final ISimulationCycle cycle) {
		super(model, nElem, cycle);
		add(new StandardElementGenerationInfo(et, IFlow, 0, null, 1.0));
	}
	
	/**
	 * Creates a generator of a single type of IMovable elements.
	 * @param model The simulation model this generator belongs to
	 * @param nElem Number of objects created each time this generator is invoked
	 * @param et The type of the elements to be created
	 * @param IFlow The first step in the IFlow of the elements to be created
	 * @param size A function to determine the size of the generated elements 
	 * @param initLocation The initial {@link Location} where the elements appear
	 * @param cycle A function to determine when are created the elements
	 */
	public TimeDrivenElementGenerator(final Simulation model, final AbstractTimeFunction nElem, final ElementType et,
									  final IInitializerFlow IFlow, final AbstractTimeFunction size,
									  final Location initLocation, final ISimulationCycle cycle) {
		super(model, nElem, cycle);
		add(new StandardElementGenerationInfo(et, IFlow, size, initLocation, 1.0));
	}
	
	/**
	 * Creates a generator of a single type of IMovable elements.
	 * @param model The simulation model this generator belongs to
	 * @param nElem Number of objects created each time this generator is invoked
	 * @param et The type of the elements to be created
	 * @param IFlow The first step in the IFlow of the elements to be created
	 * @param size A function to determine the size of the generated elements 
	 * @param initLocation The initial {@link Location} where the elements appear
	 * @param cycle A function to determine when are created the elements
	 */
	public TimeDrivenElementGenerator(final Simulation model, final int nElem, final ElementType et,
									  final IInitializerFlow IFlow, final AbstractTimeFunction size,
									  final Location initLocation, final ISimulationCycle cycle) {
		super(model, nElem, cycle);
		add(new StandardElementGenerationInfo(et, IFlow, size, initLocation, 1.0));
	}
	
	/**
	 * Creates a generator of a single type of IMovable elements.
	 * @param model The simulation model this generator belongs to
	 * @param nElem Number of objects created each time this generator is invoked
	 * @param et The type of the elements to be created
	 * @param IFlow The first step in the IFlow of the elements to be created
	 * @param size The size of the generated elements 
	 * @param initLocation The initial {@link Location} where the elements appear
	 * @param cycle A function to determine when are created the elements
	 */
	public TimeDrivenElementGenerator(final Simulation model, final int nElem, final ElementType et,
									  final IInitializerFlow IFlow, final int size, final Location initLocation,
									  final ISimulationCycle cycle) {
		super(model, nElem, cycle);
		add(new StandardElementGenerationInfo(et, IFlow, size, initLocation, 1.0));
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Only creates the element if the initial location has enough available capacity
	 * @param ind Index of the element created
	 * @param info Information required to create the element  
	 */
	public IEventSource createEventSource(final int ind, final StandardElementGenerationInfo info) {
		return new Element(simul, info);
	}

	
}
