package es.ull.simulation.condition;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A logical OR condition among a collection of conditions
 * @author Iván Castilla Rodríguez
 */
public class OrCondition<E> extends AbstractCondition<E> {
	/** Collection of conditions */
	private final Collection<AbstractCondition<E>> conditionList;
	
	/**
	 * Creates a logical OR condition between two conditions
	 * @param cond1 First condition
	 * @param cond2 Second condition
	 */
	public OrCondition(AbstractCondition<E> cond1, AbstractCondition<E> cond2) {
		this.conditionList = new ArrayList<>();
		this.conditionList.add(cond1);
		this.conditionList.add(cond2);
	}

	/**
	 * Creates a logical OR condition among a collection of conditions
	 * @param conditionList Collection of conditions
	 */
	public OrCondition(Collection<AbstractCondition<E>> conditionList) {
		this.conditionList = new ArrayList<>();
		this.conditionList.addAll(conditionList);		
	}
	
	@Override
	public boolean check(E fe) {
		for (AbstractCondition<E> cond : conditionList)
			if (cond.check(fe))
				return true;
		return false;
	}
	
	/**
	 * Returns the collection of conditions associated to this condition
	 * @return the collection of conditions associated to this condition
	 */
	public Collection<AbstractCondition<E>> getConditionList() {
		return conditionList;
	}
}
