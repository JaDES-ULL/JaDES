package es.ull.simulation.condition;

/**
 * Default {@link AbstractCondition} used to define not conditional branches and situations
 * without uncertainty. This {@link AbstractCondition} always returns true.
 * @author Yeray Callero
 *
 */
public final class TrueCondition<E> extends AbstractCondition<E> {
	
	/**
	 * Creates a new TrueCondition
	 */
	public TrueCondition(){
	}

	@Override
	public boolean check(E fe) {
		return true;
	}
}
