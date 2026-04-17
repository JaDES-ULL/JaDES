package es.ull.simulation.hta.osdi.ontology;

import java.util.Set;
import java.util.TreeSet;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public interface IModelItemWithStrategyWrapper extends IModelItemWithCostWrapper, IModelItemWithUtilityWrapper {
    public default Set<StrategyWrapper> getStrategies() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        Set<StrategyWrapper> strategies = new TreeSet<>();
        for (StrategyWrapper strategy : getModelWrapper().getWrappersForPropertyAs(getIndividualIRI(), OSDiObjectProperty.HAS_STRATEGY, StrategyWrapper.class)) {
            strategies.add(strategy);
        }
        return strategies;
    }

    /**
     * Returns the set of specific strategies associated with this health condition.
     * @return The set of specific strategies.
     */
    public default Set<StrategyWrapper> getSpecificStrategies(StrategyType strategyType) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        Set<StrategyWrapper> strategies = new TreeSet<>();
        for (StrategyWrapper strategy : getStrategies()) {
            if (getOSDiWrapper().isInstanceOf(strategy.getIndividualIRI(), strategyType.getClazz())) {
                strategies.add(strategy);
            }
        }
        return strategies;
    }

}
