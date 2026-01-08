/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.ElementInstance;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class SafeMergeFlowControl extends MergeFlowControl {
	/** Current amount of arrived branches */
	protected int checked;

	public SafeMergeFlowControl(AbstractMergeFlow IFlow) {
		super(IFlow);
	}

	/* (non-Javadoc)
	 * @see com.ull.simulation.MergeFlowControl#arrive(com.ull.simulation.FlowExecutor)
	 */
	@Override
	public void arrive(ElementInstance wThread) {
		if (wThread.isExecutable())
			trueChecked++;
		else
			outgoingFalseToken.addFlow(wThread.getToken().getPath());
		checked++;
	}

	@Override
	public boolean reset() {
		checked = 0;
		return super.reset();
	}
	
	/* (non-Javadoc)
	 * @see com.ull.simulation.MergeFlowControl#canReset()
	 */
	@Override
	public boolean canReset(int checkValue) {
		return (checked == checkValue);
	}

}
