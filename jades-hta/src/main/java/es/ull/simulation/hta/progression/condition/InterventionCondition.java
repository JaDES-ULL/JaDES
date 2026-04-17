/**
 * 
 */
package es.ull.simulation.hta.progression.condition;

import es.ull.simulation.condition.AbstractCondition;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.progression.DiseaseProgressionPathway;

/**
 * A condition that meets if the patient includes in his/her state all of the disease progressions specified 
 * @author Iván Castilla Rodríguez
 *
 */
public class InterventionCondition extends AbstractCondition<DiseaseProgressionPathway.ConditionInformation> {
	private final Intervention requiredIntervention;

	/**
	 */
	public InterventionCondition(Intervention requiredIntervention) {
		super();
		this.requiredIntervention = requiredIntervention;
	}

	@Override
	public boolean check(DiseaseProgressionPathway.ConditionInformation info) {
		return (info.getPatient().getIntervention().equals(requiredIntervention));
	}

	public Intervention getRequiredIntervention() {
		return requiredIntervention;
	}

}
