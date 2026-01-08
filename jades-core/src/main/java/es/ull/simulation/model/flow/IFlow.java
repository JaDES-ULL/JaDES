/**
 * 
 */
package es.ull.simulation.model.flow;

import java.util.Set;

import es.ull.simulation.model.ElementInstance;
import es.ull.simulation.model.IIdentifiable;

/**
 * The process an element has to carry out.<p>
 * A IFlow is structured as a graph, so it can have successors and predecessor. Successors can be
 * added by using {@link #link(IFlow)}. This implies invoking the {@link #addPredecessor(IFlow)}
 * method for the new successor. By creating this basic structure, the Sequence workflow pattern
 * (WFP1) is implemented.<p>
 * Flows can have not only successors and predecessors, but can be enclosed by an structured 
 * IFlow, which is considered its parent.<p>
 * This IFlow can be requested to be carried out by an element. To do this, a set of user-defined
 * conditions are first checked by invoking <code>beforeRequest</code>. If this method is true, 
 * the Element can definitely request this IFlow.
 * @author Iván Castilla Rodríguez
 *
 */
public interface IFlow extends IIdentifiable {
	/**
	 * Adds a IFlow's successor. This method must invoke <code>successor.addPredecessor</code>
	 * to build the graph properly. 
	 * @param successor This IFlow's successor.
	 * @return The successor (useful for chained links)
	 */
	IFlow link(final IFlow successor);
	
	/**
	 * Notifies this IFlow that it has been linked (i.e. added as a successor) to
	 * another IFlow.
	 * @param predecessor This IFlow's predecessor.
	 */
	void addPredecessor(final IFlow predecessor);
	
	/**
	 * Returns the structured IFlow which contains this IFlow.
	 * @return the structured IFlow which contains this IFlow.
	 */
	AbstractStructuredFlow getParent();
	
	/**
	 * Sets the structured IFlow which contains this IFlow.
	 * @param parent the structured IFlow which contains this IFlow.
	 */
	void setParent(final AbstractStructuredFlow parent);
	
	/**
	 * Sets the structured IFlow which contains this IFlow and does the same for the
	 * successors of this IFlow.
	 * @param parent the structured IFlow which contains this IFlow and its successors.
	 * @param visited list of already visited flows (to prevent infinite recursion when 
	 * arbitrary loops are present)
	 */
	void setRecursiveStructureLink(final AbstractStructuredFlow parent, final Set<IFlow> visited);
	
	/**
	 * Allows a user to add conditions which the element requesting this IFlow must meet
	 * before request this IFlow.
	 * @param ei The element trying to request this IFlow.
	 * @return True if this IFlow can be requested; false in other case.
	 */
	boolean beforeRequest(ElementInstance ei);

	/**
	 * Requests this IFlow. An element, by means of a element instance, requests this IFlow to
	 * carry it out.
	 * @param ei The element instance requesting this IFlow.
	 */
	void request(final ElementInstance ei);
	
	/**
	 * Requests this IFlow successor(s) to continue the execution. This method is invoked
	 * after all the tasks associated to this IFlow has been successfully carried out.
	 * @param ei The element instance which requested this IFlow.
	 */
	void next(final ElementInstance ei);
}
