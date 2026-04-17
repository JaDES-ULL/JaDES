/**
 * 
 */
package es.ull.simulation.hta.simpletest;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.Patient;
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
public class TestNotDiagnosedPopulation extends StdPopulation {
	private static final double BIRTH_PREVALENCE = 0.1;

	/**
	 * @param disease
	 */
	public TestNotDiagnosedPopulation(HTAModel model, Disease disease) throws MalformedSimulationModelException {
		super(model, "TESTPOP", "Test undiagnosed population", disease);
		setMinAge(0);
	}

	@Override
	protected DiscreteRandomVariate getSexVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), 0.5);
	}

	@Override
	protected DiscreteRandomVariate getDiseaseVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), BIRTH_PREVALENCE);
	}

	@Override
	protected DiscreteRandomVariate getDiagnosedVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), 1.0);
	}

	@Override
	protected RandomVariate getBaselineAgeVariate(Patient pat) {
		return RandomVariateFactory.getInstance("ConstantVariate", 0.0);
	}

	@Override
	public TimeToEventCalculator initializeDeathCharacterization() {
		return new EmpiricalSpainDeathSubmodel(getModel());
	}
}
