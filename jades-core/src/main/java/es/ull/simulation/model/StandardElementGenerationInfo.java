package es.ull.simulation.model;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.model.flow.IInitializerFlow;
import es.ull.simulation.model.location.Location;

/**
 * Description of a set of {@link Element elements} a generator can create.
 * @author Iván Castilla Rodríguez
 */
public class StandardElementGenerationInfo extends Generator.GenerationInfo {
	/** Type of the created elements. */
	protected final ElementType et;
	/** Description of the IFlow that the elements carry out. */
	protected final IInitializerFlow IFlow;
	/** Function to determine the size of the elements created */ 
	protected final AbstractTimeFunction size;
	/** The initial {@link Location} where the elements appear */
	protected final Location initLocation;
	
	/**
	 * Creates a new kind of elements to generate.
	 * @param et Element type
	 * @param IFlow Description of the activity IFlow that the elements carry out.
	 * @param size A function to determine the size of the generated elements 
	 * @param initLocation The initial {@link Location} where the elements appear
	 * @param prop Proportion of elements corresponding to this IFlow.
	 */
	public StandardElementGenerationInfo(final ElementType et, final IInitializerFlow IFlow, final int size,
										 final Location initLocation, final double prop) {
		this(et, IFlow, TimeFunctionFactory.getInstance("ConstantVariate", size), initLocation, prop);
	}
	
	/**
	 * Creates a new kind of elements to generate.
	 * @param et Element type
	 * @param IFlow Description of the activity IFlow that the elements carry out.
	 * @param size A function to determine the size of the generated elements 
	 * @param initLocation The initial {@link Location} where the elements appear
	 * @param prop Proportion of elements corresponding to this IFlow.
	 */
	public StandardElementGenerationInfo(final ElementType et, final IInitializerFlow IFlow,
										 final AbstractTimeFunction size, final Location initLocation,
										 final double prop) {
		super(prop);
		this.et = et;
		this.IFlow = IFlow;
		this.size = size;
		this.initLocation = initLocation;
	}
	
	/**
	 * Returns the element type.
	 * @return Returns the element type.
	 */
	public ElementType getElementType() {
		return et;
	}
	
	/**
	 * Returns the IFlow.
	 * @return the IFlow
	 */
	public IInitializerFlow getFlow() {
		return IFlow;
	}

	/**
	 * Returns the function that determines the size of the generated elements 
	 * @return the function that determines the size of the generated elements
	 */
	public int getSize(Element e) {
		return (size == null) ? 0 : (int)size.getValue(e);
	}

	/**
	 * Returns the initial {@link Location} where the elements appear
	 * @return The initial {@link Location} where the elements appear
	 */
	public Location getInitLocation() {
		return initLocation;
	}
}