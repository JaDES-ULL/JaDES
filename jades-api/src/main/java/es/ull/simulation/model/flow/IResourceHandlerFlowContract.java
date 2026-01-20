package es.ull.simulation.model.flow;

/**
 * Contract for flows that handle resources.
 */
public interface IResourceHandlerFlowContract extends IActionFlowContract {
	int getResourcesId();
}
