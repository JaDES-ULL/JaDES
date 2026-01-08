package es.ull.simulation.model.flow;

import java.util.TreeSet;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.condition.TrueCondition;
import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.Simulation;

/**
 * A {@link AbstractStructuredFlow} whose initial step is a {@link MultiChoiceFlow} and whose final step
 * is a {@link SynchronizationFlow}. Meets the Structured Synchronization pattern (WFP7). 
 * @author Yeray Callero
 */
public class StructuredSynchroMergeFlow extends AbstractPredefinedStructuredFlow {
	
	/**
	 * Create a new StructuredSynchroMergeMetaFlow.
	 */
	public StructuredSynchroMergeFlow(Simulation model) {
		super(model);
		initialFlow = new MultiChoiceFlow(model);
		initialFlow.setParent(this);
		finalFlow = new SynchronizationFlow(model);
		finalFlow.setParent(this);
	}

	/**
	 * Variation of <code>addBranch</code> which allows to indicate a condition
	 * @param branch A unique IFlow defining an internal branch
	 * @param cond This branch's condition.
	 */
	
	public void addBranch(ITaskFlow branch, AbstractCondition<ElementInstance> cond) {
		addBranch(branch, branch, cond);
	}
	
	/**
	 * Variation of <code>addBranch</code> which allows to indicate a condition
	 * @param initialBranch First step of the internal branch
	 * @param finalBranch Last step of the internal branch
	 * @param cond This branch's condition.
	 */
	public void addBranch(IInitializerFlow initialBranch, IFinalizerFlow finalBranch,
						  AbstractCondition<ElementInstance> cond) {
		final TreeSet<IFlow> visited = new TreeSet<IFlow>(); 
		initialBranch.setRecursiveStructureLink(this, visited);
		((MultiChoiceFlow)initialFlow).link(initialBranch, cond);
		finalBranch.link(finalFlow);
	}
	
	@Override
	public void addBranch(IInitializerFlow initialBranch, IFinalizerFlow finalBranch) {
		addBranch(initialBranch, finalBranch, new TrueCondition<ElementInstance>());		
	}
	
	@Override
	public void addBranch(ITaskFlow initialBranch) {
		addBranch(initialBranch, new TrueCondition<ElementInstance>());
	}
}
