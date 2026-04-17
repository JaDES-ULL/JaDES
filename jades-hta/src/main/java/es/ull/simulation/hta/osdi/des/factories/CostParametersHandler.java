package es.ull.simulation.hta.osdi.des.factories;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForCost;

/**
 * A class to handle the cost parameters of any component of the model (diseases, interventions, etc.). 
 * It will be used to appropriately manage the costs related to the model components.
 */
public class CostParametersHandler {
    /**
     * The set of cost parameters defined for a model component
     */
    private final Set<ParameterWrapper> costParameters;
    /**
     * A mapping of the cost parameters by their category. This will allow to easily retrieve the cost parameters related to a specific category of resource usage (e.g. hospitalization costs, medication costs, etc.).
     */
    private final Map<ResourceUsageSKOSCategory, Set<ParameterWrapper>> categorizedCostParameters;
    /**
     * The set of cost parameters that apply one-time costs. This will allow to easily retrieve the cost parameters that should be applied as one-time costs, as opposed to annual costs.
     */
    private final Set<ParameterWrapper> oneTimeCostParameters;

    /**
     * Constructor of the CostParametersHandler class. It takes the set of cost parameters defined for a model component and categorizes them by their category and application (one-time vs annual).
     * @param costParameters The set of cost parameters defined for a model component. It is expected that the specific information of these parameters is of type SpecificInformationForCost, so that the category and application of the cost can be determined.
     */
    public CostParametersHandler(Set<ParameterWrapper> costParameters) {
        this.costParameters = costParameters;
        categorizedCostParameters = new TreeMap<>();
        oneTimeCostParameters = new TreeSet<>();
        for (ParameterWrapper costParam : costParameters) {
            final SpecificInformationForCost specificInfo = (SpecificInformationForCost) costParam.getSpecificInformation();
            ResourceUsageSKOSCategory category = specificInfo.category();
            if (category == null) {
                category = ResourceUsageSKOSCategory.HEALTH_CARE; // If no category is specified, we will consider it as a general healthcare cost.
            }
            final boolean appliesOneTime = specificInfo.appliesOneTime();
            categorizedCostParameters.computeIfAbsent(category, k -> new TreeSet<>()).add(costParam);
            if (appliesOneTime) {
                oneTimeCostParameters.add(costParam);
            }
        }
    }

    /**
     * Returns the set of cost parameters defined for a model component.
     * @return The set of cost parameters defined for a model component.
     */
    public Set<ParameterWrapper> getCostParameters() {
        return costParameters;
    }

    /**
     * Returns the set of cost parameters that apply one-time costs or annual costs, depending on the value of the appliesOneTime parameter.
     * @param appliesOneTime If true, the method will return the set of cost parameters that apply one-time costs. If false, it will return the set of cost parameters that apply annual costs.
     * @return The set of cost parameters that apply one-time costs or annual costs, depending on the value of the appliesOneTime parameter.
     */
    public Set<ParameterWrapper> getCostParametersByApplication(boolean appliesOneTime) {
        if (appliesOneTime) {
            return oneTimeCostParameters;
        } else {
            final Set<ParameterWrapper> annualCostParameters = new TreeSet<>(costParameters);
            annualCostParameters.removeAll(oneTimeCostParameters);
            return annualCostParameters;
        }
    }

    /**
     * Returns the set of cost parameters that belong to a specific category of resource usage.
     * @param category The category of resource usage for which the cost parameters should be retrieved. This is expected to be one of the categories defined in the ResourceUsageSKOSCategory class (e.g. hospitalization costs, medication costs, etc.).
     * @return The set of cost parameters that belong to the specified category of resource usage. If no cost parameters are defined for that category, an empty set will be returned.
     */
    public Set<ParameterWrapper> getCostParametersByCategory(ResourceUsageSKOSCategory category) {
        return categorizedCostParameters.getOrDefault(category, new TreeSet<>());
    }

    /**
     * Returns the set of cost parameters that belong to a specific category of resource usage and apply one-time costs or annual costs, depending on the value of the appliesOneTime parameter.
     * @param category The category of resource usage for which the cost parameters should be retrieved. This is expected to be one of the categories defined in the ResourceUsageSKOSCategory class (e.g. hospitalization costs, medication costs, etc.).
     * @param appliesOneTime If true, the method will return the set of cost parameters that belong to the specified category and apply one-time costs. If false, it will return the set of cost parameters that belong to the specified category and apply annual costs.
     * @return The set of cost parameters that belong to the specified category of resource usage and apply one-time costs or annual costs, depending on the value of the appliesOneTime parameter. If no cost parameters are defined for that category and application, an empty set will be returned.
     */
    public Set<ParameterWrapper> getCostParametersByCategoryAndApplication(ResourceUsageSKOSCategory category, boolean appliesOneTime) {
        final Set<ParameterWrapper> filteredParams = getCostParametersByApplication(appliesOneTime);
        filteredParams.retainAll(getCostParametersByCategory(category));
        return filteredParams;
    }   
}
