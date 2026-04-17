package es.ull.simulation.hta.osdi.des.factories;

import java.util.Set;
import java.util.TreeSet;

import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;

/**
 * A class to handle the utility parameters of any component of the model (diseases, interventions, etc.). 
 * It will be used to appropriately manage the utilities related to the model components.
 */
public class UtilityParametersHandler {
    /**
     * The set of utility parameters defined for a model component
     */
    private final Set<ParameterWrapper> utilityParameters;
    /**
     * The set of utility parameters that apply one-time utilities. This will allow to easily retrieve the utility parameters that should be applied as one-time utilities, as opposed to annual utilities.
     */
    private final Set<ParameterWrapper> oneTimeUtilityParameters;
    /**
     * The set of utility parameters that are disutilities. This will allow to easily retrieve the utility parameters that should be applied as disutilities, as opposed to regular utilities.
     */
    private final Set<ParameterWrapper> disutilityParameters;

    /**
     * Constructor of the UtilityParametersHandler class. It takes the set of utility parameters defined for a model component and categorizes them by their application (one-time vs annual) and type (disutility vs regular utility).
     * @param utilityParameters The set of utility parameters defined for a model component. It is expected that the specific information of these parameters is of type SpecificInformationForUtility, so that the application and type of the utility can be determined.
     */
    public UtilityParametersHandler(Set<ParameterWrapper> utilityParameters) {
        this.utilityParameters = utilityParameters;
        oneTimeUtilityParameters = new TreeSet<>();
        disutilityParameters = new TreeSet<>();
        for (ParameterWrapper utilityParam : utilityParameters) {
            final SpecificInformationForUtility specificInfo = (SpecificInformationForUtility) utilityParam.getSpecificInformation();
            if (specificInfo.appliesOneTime()) {
                oneTimeUtilityParameters.add(utilityParam);
            }
            if (specificInfo.isDisutility()) {
                disutilityParameters.add(utilityParam);
            } 
        }
    }

    /**
     * Returns the set of utility parameters defined for a model component.
     * @return The set of utility parameters defined for a model component.
     */
    public Set<ParameterWrapper> getUtilityParameters() {
        return utilityParameters;
    }

    /**
     * Returns the set of utility parameters that apply one-time utilities or annual utilities, depending on the value of the appliesOneTime parameter.
     * @param appliesOneTime If true, the method will return the set of utility parameters that apply one-time utilities. If false, it will return the set of utility parameters that apply annual utilities.
     * @return The set of utility parameters that apply one-time utilities or annual utilities, depending on the value of the appliesOneTime parameter.
     */
    public Set<ParameterWrapper> getUtilityParametersByApplication(boolean appliesOneTime) {
        if (appliesOneTime) {
            return oneTimeUtilityParameters;
        } else {
            final Set<ParameterWrapper> annualUtilityParameters = new TreeSet<>(utilityParameters);
            annualUtilityParameters.removeAll(oneTimeUtilityParameters);
            return annualUtilityParameters;
        }
    }

    /**
     * Returns the set of utility parameters that are disutilities or regular utilities, depending on the value of the isDisutility parameter.
     * @param isDisutility If true, the method will return the set of utility parameters that are disutilities. If false, it will return the set of utility parameters that are regular utilities.
     * @return The set of utility parameters that are disutilities or regular utilities, depending on the value of the isDisutility parameter.
     */
    public Set<ParameterWrapper> getUtilityParametersByType(boolean isDisutility) {
        if (isDisutility) {
            return disutilityParameters;
        } else {
            final Set<ParameterWrapper> regularUtilityParameters = new TreeSet<>(utilityParameters);
            regularUtilityParameters.removeAll(disutilityParameters);
            return regularUtilityParameters;
        }
    }

    /**
     * Returns the set of utility parameters that are disutilities or regular utilities, depending on the value of the isDisutility parameter, and that apply one-time utilities or annual utilities, depending on the value of the appliesOneTime parameter.
     * @param isDisutility If true, the method will return the set of utility parameters that are disutilities. If false, it will return the set of utility parameters that are regular utilities.
     * @param appliesOneTime If true, the method will return the set of utility parameters that apply one-time utilities. If false, it will return the set of utility parameters that apply annual utilities.
     * @return The set of utility parameters that are disutilities or regular utilities, depending on the value of the isDisutility parameter, and that apply one-time utilities or annual utilities, depending on the value of the appliesOneTime parameter.
     */
    public Set<ParameterWrapper> getUtilityParametersByTypeAndApplication(boolean isDisutility, boolean appliesOneTime) {
        final Set<ParameterWrapper> filteredParams = getUtilityParametersByType(isDisutility);
        if (appliesOneTime) {
            filteredParams.retainAll(oneTimeUtilityParameters);
        } else {
            filteredParams.removeAll(oneTimeUtilityParameters);
        }
        return filteredParams;
    }
}
