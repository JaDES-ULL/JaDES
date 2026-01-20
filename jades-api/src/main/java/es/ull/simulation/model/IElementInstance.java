package es.ull.simulation.model;

import es.ull.simulation.model.flow.IFlowDescriptor;

/**
 * Public read-only contract for element instances.
 */
public interface IElementInstance extends IIdentifiable {
	int getPriority();
	IElement getElement();
	boolean isExecutable();
	void notifyEnd();
	int getArrivalOrder();
	void setArrivalOrder(final int arrivalOrder);
	long getArrivalTs();
	void setArrivalTs(final long arrivalTs);
	IFlowDescriptor getCurrentFlow();
	IWorkGroup getExecutionWG();
}
