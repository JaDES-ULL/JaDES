package es.ull.simulation.condition;
/**
 * Default {@link AbstractCondition} used to define not conditional branches and situations
 * without uncertainty. This {@link AbstractCondition} always returns false.
 * @author Iván Castilla Rodríguez
 *
 */
public final class FalseCondition<E> extends AbstractCondition<E> {
	
	/**
	 * Creates a new TrueCondition
	 */
	public FalseCondition(){
	}

	@Override
	public boolean check(E fe) {
		return false;
	}
}
