package es.ull.simulation.condition;
/**
 * Condition used to build NOT logical operations. This NotCondition 
 * returns <tt>true</tt> if the associated {@link AbstractCondition} returns <tt>false</tt>,
 * and vice versa.
 * @author Yeray Callero
 *
 */
public final class NotCondition<E> extends AbstractCondition<E> {
	/** Associated Condition */
	final private AbstractCondition<E> cond;
	
	/**
	 * Create a new NotCondition
	 * @param newCond Associated Condition
	 */
	public NotCondition(AbstractCondition<E> newCond){
		cond = newCond;
	}
	
	/**
	 * Checks the associated condition and return the negated value.
	 * @param fe Element used to check the condition.
	 * @return The negated result of the associated Condition
	 */
	public boolean check(E fe) {
		return !cond.check(fe);
	}

	/**
	 * Returns the associated Condition.
	 * @return The associated Condition.
	 */
	public AbstractCondition<E> getCond() {
		return cond;
	}

}
