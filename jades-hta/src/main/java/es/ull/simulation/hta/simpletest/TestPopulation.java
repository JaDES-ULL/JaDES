/**
 * 
 */
package es.ull.simulation.hta.simpletest;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.populations.Population;
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
public class TestPopulation extends StdPopulation {

	/**
	 * @param disease
	 */
	public TestPopulation(HTAModel model, Disease disease) throws MalformedSimulationModelException {
		super(model, "TEST_POP", "Test population", disease);
		setMinAge(0);
	}

	@Override
	protected DiscreteRandomVariate getSexVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), 0.5);
	}

	@Override
	protected DiscreteRandomVariate getDiseaseVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("BernoulliVariate", getCommonRandomNumber(), 1.0);
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
	public void createParameters() {
		addUsedParameter(StandardParameter.POPULATION_BASE_UTILITY, "", "Assumption", Population.DEF_U_GENERAL_POP);
	}

	@Override
	public TimeToEventCalculator initializeDeathCharacterization() {
		return new EmpiricalSpainDeathSubmodel(getModel());
	}
}
