package es.ull.simulation.hta.config;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import es.ull.simulation.experiment.IExperimentConfigurationProvider;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;

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
	 * @return the combination method used to combine different disutilities
	 */
	Optional<DisutilityCombinationMethod> getDisutilityCombinationMethod();
}
