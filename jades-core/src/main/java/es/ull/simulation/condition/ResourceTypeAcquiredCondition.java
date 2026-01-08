package es.ull.simulation.condition;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.ResourceType;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class ResourceTypeAcquiredCondition extends AbstractCondition<ElementInstance> {
	final private ResourceType rt;
	
	/**
	 * 
	 */
	public ResourceTypeAcquiredCondition(ResourceType rt) {
		this.rt = rt;
	}

	@Override
	public boolean check(ElementInstance fe) {
		return (fe.getElement().isAcquiredResourceType(rt));
	}
	
	public ResourceType getResourceType() {
		return rt;
	}
}
