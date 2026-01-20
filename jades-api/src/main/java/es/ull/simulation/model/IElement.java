package es.ull.simulation.model;

import es.ull.simulation.model.location.IMovable;
import es.ull.simulation.utils.Prioritizable;

/**
 * Public contract for elements.
 */
public interface IElement extends IIdentifiable, IDescribable, IMovable, Prioritizable {
	/**
	 * Returns the element type.
	 * @return the element type
	 */
	IElementType getType();
}
