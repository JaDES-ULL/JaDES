package es.ull.simulation.condition;

/**
 * A logical condition which is used for creating restrictions or uncertain situations. A {@link AbstractCondition} is
 * "checked" by using the {@link #check(E)} method and returns <tt>true</tt> if the condition is satisfied and
 * <tt>false</tt> otherwise.
 * @author Yeray Callero
 */

public abstract class AbstractCondition<E> {
	
	/** 
	 * Creates a new Condition.
	 */
	public AbstractCondition(){
	}
	
	/**
	 * Checks the condition to obtain the result of the logical operation.
	 * @param fe FlowExecutor which want to check the condition.
	 * @return The boolean result of the logical operation.
	 */
	public abstract boolean check(E fe);
	
}
