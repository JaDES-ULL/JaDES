/**
 * 
 */
package es.ull.simulation.model.engine;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import es.ull.simulation.model.Element;
import es.ull.simulation.model.WorkToken;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.AbstractMergeFlow;
import es.ull.simulation.model.flow.MergeFlowControl;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class MergeFlowEngine extends AbstractEngineObject {
	final private AbstractMergeFlow modelFlow;

	/**
	 * Constructs a new MergeFlowEngine object.
	 * MergeFlowEngine represents the engine responsible for managing the merging of flow instances in a simulation.
	 * It is associated with a specific AbstractMergeFlow object.
	 *
	 * @param simul       The SimulationEngine to which this merge flow engine belongs.
	 * @param modelFlow   The associated AbstractMergeFlow object representing this merge flow.
	 */
	public MergeFlowEngine(SimulationEngine simul, AbstractMergeFlow modelFlow) {
		super(modelFlow.getIdentifier(), simul, modelFlow.getObjectTypeIdentifier());
		this.modelFlow = modelFlow;
	}

	/**
	 * @return the modelFlow
	 */
	public AbstractMergeFlow getModelFlow() {
		return modelFlow;
	}

	/* (non-Javadoc)
	 * @see com.ull.simulation.model.engine.IMergeFlowEngine#getControlStructureInstance()
	 */
	public Map<Element, MergeFlowControl> getControlStructureInstance() {
		return new TreeMap<Element, MergeFlowControl>();
	}

	public Map<IFlow, LinkedList<WorkToken>> getGeneralizedBranchesControlInstance() {
		return new TreeMap<IFlow, LinkedList<WorkToken>>();
	}

}
