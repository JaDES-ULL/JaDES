/**
 * 
 */
package es.ull.simulation.hta.pbdmodel;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.populations.StdPopulation;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.calculator.EmpiricalSpainDeathSubmodel;
import es.ull.simulation.hta.progression.calculator.TimeToEventCalculator;
import simkit.random.DiscreteRandomVariate;
import simkit.random.RandomVariate;
import simkit.random.RandomVariateFactory;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class PBDPopulation extends StdPopulation {
	private static final double BIRTH_PREVALENCE = 1.47884E-05;
	private final boolean allAffected;
	/**
	 * @param disease
	 */
	public PBDPopulation(HTAModel model, Disease disease, boolean allAffected) throws MalformedSimulationModelException {
		super(model, "PBD_POP", "Population for PBD", disease);
		setMinAge(0.0);
		this.allAffected = allAffected;
	}

	@Override
	protected DiscreteRandomVariate getSexVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), 0.5);
	}

	@Override
	protected DiscreteRandomVariate getDiseaseVariate(Patient pat) {
		final double birthPrev = allAffected ? 1.0 : model.getParameterValue(StandardParameter.BIRTH_PREVALENCE.createName(disease), pat);
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), birthPrev);
	}

	@Override
	protected DiscreteRandomVariate getDiagnosedVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), 0.0);
	}

	@Override
	protected RandomVariate getBaselineAgeVariate(Patient pat) {
		return RandomVariateFactory.getInstance("ConstantVariate", 0.0);
	}

	@Override
	public void createParameters() {
		addUsedParameter(StandardParameter.POPULATION_BASE_UTILITY, "", "Utility for Spanish general population", 0.8861);
		if (!allAffected)
			StandardParameter.BIRTH_PREVALENCE.addToModel(model, disease, "", 2013, BIRTH_PREVALENCE, 
				RandomVariateFactory.getInstance("BetaVariate", 8, 540955));
	}

	@Override
	public TimeToEventCalculator initializeDeathCharacterization() {
		return new EmpiricalSpainDeathSubmodel(getModel());
	}
}
