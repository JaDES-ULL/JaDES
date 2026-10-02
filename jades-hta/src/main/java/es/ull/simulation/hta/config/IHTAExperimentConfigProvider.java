package es.ull.simulation.hta.config;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import es.ull.simulation.experiment.IExperimentConfigurationProvider;
import es.ull.simulation.hta.outcomes.CostCombinationMethod;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;
import es.ull.simulation.hta.outcomes.LifeExpectancyReductionCombinationMethod;
import es.ull.simulation.hta.outcomes.MortalityRateCombinationMethod;

public interface IHTAExperimentConfigProvider extends IExperimentConfigurationProvider {
    /**
     * Returns the number of patients to be generated during each simulation
     * @return the number of patients to be generated during each simulation
     */
    OptionalInt getNPatients();
    
    /**
     * Returns the study year for costs and health outcomes updating
     * @return the study year for costs and health outcomes updating
     */
    OptionalInt getStudyYear();
    /**
     * Returns the default discount rate to be applied to costs
     * @return the default discount rate to be applied to costs
     */
    OptionalDouble getDefaultDiscountRateForCosts();
    /**
     * Returns the default discount rate to be applied to health outcomes
     * @return the default discount rate to be applied to health outcomes
     */
    OptionalDouble getDefaultDiscountRateForEffects();
    /**
     * Indicates whether the base case simulation using the expected values for second-order parameters is enabled
     * @return true if the base case simulation is enabled; false otherwise
     */
    Optional<Boolean> isBaseCaseEnabled();
    /**
     * Returns the identifiers of individuals patients to debug
     * @return the identifiers of individuals patients to debug
     */
    List<Integer> getDebugPatients();

	/**
	 * Returns the combination method used to combine different disutilities
	 * @return the combination method used to combine different disutilities; empty to use the default method
	 */
	default Optional<DisutilityCombinationMethod> getDisutilityCombinationMethod() {
        return Optional.empty();
    }

	/**
	 * Returns the combination method used to combine the annual costs of the items that are simultaneously active for a patient
	 * @return the combination method used to combine annual costs; empty to use the default method
	 */
	default Optional<CostCombinationMethod> getCostCombinationMethod() {
		return Optional.empty();
	}

	/**
	 * Returns the combination method used to combine the life expectancy reductions of the items that are simultaneously active for a patient
	 * @return the combination method used to combine life expectancy reductions; empty to use the default method
	 */
	default Optional<LifeExpectancyReductionCombinationMethod> getLifeExpectancyReductionCombinationMethod() {
		return Optional.empty();
	}

	/**
	 * Returns the combination method used to combine the increased mortality rates of the items that are simultaneously active for a patient
	 * @return the combination method used to combine increased mortality rates; empty to use the default method
	 */
	default Optional<MortalityRateCombinationMethod> getMortalityRateCombinationMethod() {
		return Optional.empty();
	}
}
