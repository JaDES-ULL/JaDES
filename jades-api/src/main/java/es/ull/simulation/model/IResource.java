package es.ull.simulation.model;

import es.ull.simulation.model.location.IMovable;

/**
 * Public contract for resources.
 */
public interface IResource extends IIdentifiable, IDescribable, IMovable {
	/**
	 * Returns the current resource type if any.
	 * @return current resource type or null
	 */
	IResourceType getCurrentResourceType();
}
