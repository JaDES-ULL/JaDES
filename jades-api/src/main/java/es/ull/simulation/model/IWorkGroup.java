package es.ull.simulation.model;

/**
 * Public contract for work groups.
 */
public interface IWorkGroup extends IIdentifiable, IDescribable {
	int size();
	IResourceType getResourceType(final int ind);
	int[] getNeeded();
	IResourceType[] getResourceTypes();
}
