/**
 * 
 */
package es.ull.simulation.model.flow;

import java.util.TreeSet;

import es.ull.simulation.model.Simulation;


/**
 * An structured IFlow with predefined both entry and exit points.
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class AbstractPredefinedStructuredFlow extends AbstractStructuredFlow {

	/**
	 * Creates a new structured IFlow with predefined entry and exit points.
	 */
	public AbstractPredefinedStructuredFlow(Simulation model) {
		super(model);
	}
	
	/**
	 * Adds a new branch starting in <code>initialBranch</code> and finishing in <code>finalBranch</code>.
	 * The <code>initialFlow</code> is linked to the <code>initialBranch</code> whereas
	 * the <code>final Branch</code> is linked to the <code>finalFlow</code> 
	 * @param initialBranch First step of the internal branch
	 * @param finalBranch Last step of the internal branch
	 */
	public void addBranch(IInitializerFlow initialBranch, IFinalizerFlow finalBranch) {
		final TreeSet<IFlow> visited = new TreeSet<IFlow>(); 
		initialBranch.setRecursiveStructureLink(this, visited);
		initialFlow.link(initialBranch);
		finalBranch.link(finalFlow);		
	}
	
	/**
	 * Adds a new branch consisting of a unique IFlow. The <code>branch</code> has the
	 * <code>initialFlow</code> as predecessor and the <code>finalFlow</code> as successor. 
	 * @param branch A unique IFlow defining an internal branch
	 */
	public void addBranch(ITaskFlow branch) {
		addBranch(branch, branch);		
	}

}
