/**
 * 
 */
package es.ull;

import java.util.ArrayList;
import java.util.TreeMap;

import es.ull.simulation.info.ElementActionInfo;
import es.ull.simulation.info.ElementInfo;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;
import es.ull.simulation.model.Element;
import es.ull.simulation.model.flow.IActionFlow;
import es.ull.simulation.model.flow.IFlow;
import es.ull.simulation.model.flow.IInitializerFlow;
import es.ull.simulation.model.flow.AbstractSingleSuccessorFlow;

/**
 * Checks the elements created and finished during the simulation
 * @author Iván Castilla Rodríguez
 *
 */
public class CheckFlowsListener extends BasicListener {
	private final NodeInfo[] checkStructure;

	/**
	 * Constructs a new CheckFlowsListener object.
	 * CheckFlowsListener represents a listener used to monitor flows in a simulation.
	 *
	 * @param nElem  The total number of elements in the simulation.
	 */
	public CheckFlowsListener(int nElem) {
		super("Activity checker ");
		this.checkStructure = new NodeInfo[nElem];
		addTargetInformation(ElementActionInfo.class);
		addTargetInformation(ElementInfo.class);
	}

	private static final NodeInfo buildCheckStructure(IInitializerFlow initFlow) {
		if (initFlow instanceof AbstractSingleSuccessorFlow) {
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private static final NodeInfo createNode(IFlow IFlow, boolean optional, int minIterations, int maxIterations) {
		if (IFlow instanceof IActionFlow) {
			return new NodeLeafInfo((IActionFlow)IFlow, optional, minIterations, maxIterations);
		}
		return new NodeBranchInfo(optional, minIterations, maxIterations);
	}
	private static interface NodeInfo {
		public boolean isOptional();
		public int getMaxIterations();
		public int getMinIterations();
	}
	
	
	private static class NodeLeafInfo implements NodeInfo {
		private final IActionFlow IFlow;
		private final boolean optional;
		private final int minIterations;
		private final int maxIterations;
		
		/**
		 * @param IFlow
		 * @param optional
		 * @param minIterations
		 * @param maxIterations
		 */
		public NodeLeafInfo(IActionFlow IFlow, boolean optional, int minIterations, int maxIterations) {
			super();
			this.IFlow = IFlow;
			this.optional = optional;
			this.minIterations = minIterations;
			this.maxIterations = maxIterations;
		}

		/**
		 * @return the IFlow
		 */
		@SuppressWarnings("unused")
		public IActionFlow getFlow() {
			return IFlow;
		}

		public boolean isOptional() {
			return optional;
		}

		public int getMinIterations() {
			return minIterations;
		}

		public int getMaxIterations() {
			return maxIterations;
		}
	}
	
	private static class NodeBranchInfo implements NodeInfo {
		private final boolean optional;
		private final int minIterations;
		private final int maxIterations;
		private final ArrayList<NodeInfo> next;
		private final TreeMap<NodeInfo, Integer> components;
		
		/**
		 * @param optional
		 * @param minIterations
		 * @param maxIterations
		 */
		public NodeBranchInfo(boolean optional, int minIterations, int maxIterations) {
			this.optional = optional;
			this.minIterations = minIterations;
			this.maxIterations = maxIterations;
			next = new ArrayList<NodeInfo>();
			components = new TreeMap<NodeInfo, Integer>();
		}
		
		@SuppressWarnings("unused")
		public void addComponent(final NodeInfo info, final int maxN) {
			components.put(info, maxN);
		}
		
		@SuppressWarnings("unused")
		public void link(final NodeInfo nextInfo) {
			next.add(nextInfo);
		}

		/**
		 * @return the next
		 */
		@SuppressWarnings("unused")
		public ArrayList<NodeInfo> getNext() {
			return next;
		}

		/**
		 * @return the components
		 */
		@SuppressWarnings("unused")
		public TreeMap<NodeInfo, Integer> getComponents() {
			return components;
		}

		public boolean isOptional() {
			return optional;
		}

		public int getMinIterations() {
			return minIterations;
		}

		public int getMaxIterations() {
			return maxIterations;
		}
	}
	
	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof ElementInfo) {
			final ElementInfo eInfo = (ElementInfo)info;
			final Element elem = eInfo.getElement();
			switch (eInfo.getType()) {
			case FINISH:
				break;
			case START:
				checkStructure[elem.getIdentifier()] = buildCheckStructure(elem.getFlow());
				break;
			default:
				break;
			}
		}
		if (info instanceof ElementActionInfo) {
			final ElementActionInfo eInfo = (ElementActionInfo)info;
			switch(eInfo.getType()) {
			case ACQ:
				break;
			case END:
				break;
			case REL:
				break;
			case REQ:
				break;
			case START:
				break;
			case RESACT:
			case INTACT:
			default:
				break;
			
			}
		}
		else if (info instanceof SimulationStartStopInfo) {
			final SimulationStartStopInfo tInfo = (SimulationStartStopInfo) info;
			if (SimulationStartStopInfo.Type.END.equals(tInfo.getType()))  {
			}
		}
		
	}
}
