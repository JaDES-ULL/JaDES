/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.model.Resource;

/**
 * A IFlow step that can interact with {@link Resource resources} 
 * @author Iván Castilla Rodríguez
 *
 */
public interface IResourceHandlerFlow extends IActionFlow {
	/**
	 * Returns a unique identifier for the set of resources that handles
	 * @return a unique identifier for the set of resources that handles
	 */
	int getResourcesId();
}
