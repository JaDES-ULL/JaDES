package es.ull.simulation.model;

import java.util.ArrayDeque;
import java.util.TreeMap;

import es.ull.simulation.model.flow.RequestResourcesFlow;

/**
 * A collection of resources that have been seized by an element. They are arranged in two levels: the
 * first level represents resource groups, as logically defined by the modeler; the second level represents
 * resource types.
 * 
 * Extracted from Element inner class to improve maintainability and testability.
 * Implements IResourceManager following DIP.
 * 
 * @author Iván Castilla Rodríguez
 */
public class SeizedResourcesCollection implements IResourceManager {
	/** Reference to the owning element for error reporting */
	private Element element;
	/** List of seized resources indexed as groups by an identifier */
	final protected TreeMap<Integer, TreeMap<ResourceType, ArrayDeque<Resource>>> resources;
	
	/**
	 * Creates an empty collection of seized resources.
	 * @param element The element that owns this collection
	 */
	public SeizedResourcesCollection(final Element element) {
		this.element = element;
		resources = new TreeMap<Integer, TreeMap<ResourceType,ArrayDeque<Resource>>>();
		resources.put(0, new TreeMap<ResourceType, ArrayDeque<Resource>>());
	}
	
	/**
	 * Sets the element reference. Used for dependency injection pattern.
	 * @param element The element that owns this collection
	 */
	public void setElement(final Element element) {
		this.element = element;
	}
	
	@Override
	public boolean hasResourceType(final ResourceType rt) {
		return containsResourceType(rt);
	}
	
	/**
	 * Returns true if the collection contains any resource of the specified resource type; false otherwise
	 * @param rt Searched resource type 
	 * @return true if the collection contains any resource of the specified resource type; false otherwise
	 */
	public boolean containsResourceType(final ResourceType rt) {
		for (TreeMap<ResourceType, ArrayDeque<Resource>> item1 : resources.values()) {
			if (item1.containsKey(rt))
				return true;
		}
		return false;
	}
	
	@Override
	public ArrayDeque<Resource> getAllResources() {
		return getAll();
	}
	
	/**
	 * Returns the whole list of resources seized by the element
	 * @return the whole list of resources seized by the element
	 */
	public ArrayDeque<Resource> getAll() {
		final ArrayDeque<Resource> list = new ArrayDeque<Resource>();
		
		for (TreeMap<ResourceType, ArrayDeque<Resource>> item1 : resources.values()) {
			for (ArrayDeque<Resource> item2 : item1.values()) {
				list.addAll(item2);
			}
		}
		return list;
	}
	
	@Override
	public ArrayDeque<Resource> getResourcesByFlow(final RequestResourcesFlow reqFlow, final ElementInstance ei) {
		final int resId = (reqFlow.getResourcesId() < 0) ? -ei.getIdentifier() : reqFlow.getResourcesId();
		return get(resId);
	}
	
	@Override
	public ArrayDeque<Resource> getResourcesByWorkGroup(final int resourcesId, final WorkGroup wg) {
		return get(resourcesId, wg);
	}
	
	/**
	 * Returns the list of resources caught by the specified element instance when performing the specified flow
	 * @param resourcesId Identifier of the group of resources
	 * @return the list of resources caught by the specified element instance when performing the specified flow
	 */
	public ArrayDeque<Resource> get(final int resourcesId) {
		final ArrayDeque<Resource> list = new ArrayDeque<Resource>();
		final TreeMap<ResourceType, ArrayDeque<Resource>> item1 = resources.get(resourcesId);
		for (ArrayDeque<Resource> item2 : item1.values()) {
			list.addAll(item2);
		}
		return list;
	}
	
	/**
	 * Returns the list of resources identifier by the resourcesId and belonging to the specified workgroup
	 * @param resourcesId Identifier of the group of resources
	 * @param wg Resource types 
	 * @return the list of resources caught by the specified element instance when performing the specified flow
	 */
	public ArrayDeque<Resource> get(final int resourcesId, final WorkGroup wg) {
		final ArrayDeque<Resource> list = new ArrayDeque<Resource>();
		final TreeMap<ResourceType, ArrayDeque<Resource>> item1 = resources.get(resourcesId);
		final ResourceType[] rts = wg.getResourceTypes();
		for (int i = 0; i < rts.length; i++) {
			list.addAll(item1.get(rts[i]));
		}
		return list;
	}
	
	/**
	 * Adds the specified resources to the list of seized resources
	 * @param resourcesId Identifier of the group of resources
	 * @param newResources New resources seized by the element
	 */
	@Override
	public void addResources(final int resourcesId, final ArrayDeque<Resource> newResources) {
		// If it's a request flow inside an activity, use minus the element instance identifier
		TreeMap<ResourceType, ArrayDeque<Resource>> collection = resources.get(resourcesId);
		// Not already created
		if (collection == null) {
			collection = new TreeMap<ResourceType, ArrayDeque<Resource>>();
			resources.put(resourcesId, collection);
		}
		for (final Resource res : newResources) {
			final ResourceType currentRT = res.getCurrentResourceType();
			if (!collection.containsKey(currentRT))
				collection.put(currentRT, new ArrayDeque<Resource>());
			collection.get(currentRT).push(res);
		}
	}
	
	/**
	 * Removes the resources specified in the workgroup from the list of seized resources
	 * @param resourcesId Identifier of the group of resources
	 * @param wg Workgroup
	 * @return The released resources
	 */
	@Override
	public ArrayDeque<Resource> removeResources(final int resourcesId, final WorkGroup wg) {
		final TreeMap<ResourceType, ArrayDeque<Resource>> collection = resources.get(resourcesId);
		// Not already created
		if (collection == null) {
			element.error("Trying to release group of resources not already created. ID:" + resourcesId);
			return null;	    		
		}
		final ArrayDeque<Resource> toRemove = new ArrayDeque<Resource>();
		// Remove all resources from group
		if (wg == null) {
			for (ArrayDeque<Resource> list : collection.values()) {
				toRemove.addAll(list);
			}
			collection.clear();
		}
		else {
			final ResourceType[] rts = wg.getResourceTypes();
			for (int i = 0; i < rts.length; i++) {
				final ArrayDeque<Resource> list = collection.get(rts[i]);
				if (list == null) {
					element.error("Trying to release non-seized resource of type "
							+ rts[i] + " from group with ID " + resourcesId);
				}
				else {
					int max = wg.getNeeded(i);
					// More resources to remove than available
					if (wg.getNeeded(i) > list.size()) {
						max = collection.get(rts[i]).size();
						element.error("Trying to release " + wg.getNeeded(i)
								+ " resources of type " + rts[i] + " from group with ID "
								+ resourcesId + ". Available: " + list.size());
						toRemove.addAll(collection.remove(rts[i]));
					}
					// Remove all resources from that type
					else if (wg.getNeeded(i) == collection.get(rts[i]).size()) {
						toRemove.addAll(collection.remove(rts[i]));
					}
					else {
						for (int j = 0; j < max; j++) {
							toRemove.add(list.pop());
						}
					}
				}
			}
		}
		return toRemove;
	}
	
	@Override
	public void releaseAll() {
		for (TreeMap<ResourceType, ArrayDeque<Resource>> collection : resources.values()) {
			collection.clear();
		}
	}
}
