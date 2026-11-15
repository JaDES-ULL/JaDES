package es.ull.simulation.hta.osdi.decisiontree;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiObjectProperty;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;

/**
 * Collects the costs of a strategy for a decision tree (decision D2 of docs/osdi-1.3-diseno.md):
 * <ul>
 * <li>The own costs of the strategy (hasCost), which apply annually or once, as any other cost.</li>
 * <li>The costs of each health technology that the strategy uses (usesHealthTechnology), according to the guideline that applies to it: that
 * of the technology (hasGuideline) or, if it has none, that of the strategy. Without guideline, the costs of the technology apply annually or once,
 * as any other cost. With a guideline, the cost of the technology is a cost per application (see {@link GuidelineCost}), and appliesOneTime
 * is ignored.</li>
 * </ul>
 * The decision tree does not support yet guidelines with sub-guidelines (hasSubGuideline), condition expressions (hasConditionExpression) or
 * application ages (hasMinApplicationAge, hasMaxApplicationAge), since they change the number of applications: such guidelines are rejected.
 * The dose (hasDose) is not used, since the cost is per application.
 */
public final class StrategyCosts {
    private final static OSDiLogger log = OSDiLogger.getLogger(StrategyCosts.class);

    /**
     * The cost of a health technology that is applied according to a guideline. With r = frequency * 8760 / period applications per year (the
     * period is expressed in hours; without period, the frequency is already annual), the guideline starts at the start offset (in years, 0 by
     * default) and lasts L = MIN(duration, count / r), where the missing terms are ignored:
     * <ul>
     * <li>With a frequency, the cost is an annual payoff r * c during [start, start + L], limited to the period of the node and the life of the leaf.</li>
     * <li>Without a frequency, the N applications given by hasCount (1 by default) are a one-time payoff N * c at the start.</li>
     * </ul>
     * @param unitCost The cost per application (c)
     * @param guidelineName The name of the guideline (used in messages and to identify the cost)
     * @param frequency The number of applications per period, if defined
     * @param period The length of the period in hours, if defined
     * @param duration The duration of the guideline in years, if defined
     * @param count The total number of applications, if defined
     * @param startOffset The time from the start of the item that has the strategy to the start of the guideline, in years, if defined
     */
    public record GuidelineCost(ParameterWrapper unitCost, String guidelineName, Optional<ParameterWrapper> frequency, Optional<ParameterWrapper> period,
            Optional<ParameterWrapper> duration, Optional<ParameterWrapper> count, Optional<ParameterWrapper> startOffset) {
        /**
         * Returns a key that identifies this cost: the cost parameter and the guideline.
         * @return A key that identifies this cost.
         */
        public String key() {
            return unitCost.getIndividualIRI() + "@" + guidelineName;
        }
    }

    private StrategyCosts() {
    }

    /**
     * Collects the costs of a strategy.
     * @param strategy The strategy
     * @param ordinaryCosts The collection where the costs that apply annually or once are added
     * @param guidelineCosts The collection where the costs that apply according to a guideline are added
     * @throws MalformedOSDiModelException If the ontology is malformed
     * @throws UnsupportedOSDiFeatureException If a guideline uses features not supported by decision trees, or an item defines several guidelines
     */
    public static void collect(StrategyWrapper strategy, Collection<ParameterWrapper> ordinaryCosts, Collection<GuidelineCost> guidelineCosts) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = strategy.getModelWrapper();
        ordinaryCosts.addAll(strategy.getCosts());
        final Optional<IModelItemWrapper> strategyGuideline = getGuideline(modelWrapper, strategy);
        for (IModelItemWrapper technology : modelWrapper.getWrappersForProperty(strategy.getIndividualIRI(), OSDiObjectProperty.USES_HEALTH_TECHNOLOGY)) {
            final Set<ParameterWrapper> technologyCosts = modelWrapper.getWrappersForPropertyAs(technology.getIndividualIRI(), OSDiObjectProperty.HAS_COST, ParameterWrapper.class);
            Optional<IModelItemWrapper> guideline = getGuideline(modelWrapper, technology);
            if (guideline.isEmpty()) {
                guideline = strategyGuideline;
            }
            if (guideline.isEmpty()) {
                ordinaryCosts.addAll(technologyCosts);
            }
            else {
                for (ParameterWrapper unitCost : technologyCosts) {
                    guidelineCosts.add(createGuidelineCost(modelWrapper, unitCost, guideline.get()));
                }
            }
        }
    }

    /**
     * Returns the guideline of a strategy or health technology, if any.
     * @param modelWrapper The model wrapper
     * @param item A strategy or health technology
     * @return The guideline of the item, if any
     * @throws UnsupportedOSDiFeatureException If the item defines several guidelines
     * @throws MalformedOSDiModelException If the ontology is malformed
     */
    private static Optional<IModelItemWrapper> getGuideline(ModelWrapper modelWrapper, IModelItemWrapper item) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Set<IModelItemWrapper> guidelines = modelWrapper.getWrappersForProperty(item.getIndividualIRI(), OSDiObjectProperty.HAS_GUIDELINE);
        if (guidelines.size() > 1) {
            throw new UnsupportedOSDiFeatureException(item.getShortName() + " defines several guidelines (" + guidelines + "). Decision trees support only one guideline per strategy or health technology.");
        }
        return guidelines.stream().findFirst();
    }

    /**
     * Creates the cost of a health technology that is applied according to a guideline.
     * @param modelWrapper The model wrapper
     * @param unitCost The cost per application
     * @param guideline The guideline
     * @return The cost of the health technology according to the guideline
     * @throws UnsupportedOSDiFeatureException If the guideline uses features not supported by decision trees
     * @throws MalformedOSDiModelException If the ontology is malformed
     */
    private static GuidelineCost createGuidelineCost(ModelWrapper modelWrapper, ParameterWrapper unitCost, IModelItemWrapper guideline) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final IRI iri = guideline.getIndividualIRI();
        // TODO: Support sub-guidelines, condition expressions and application ages (see docs/known-limitations.md)
        for (OSDiObjectProperty unsupported : new OSDiObjectProperty[] {OSDiObjectProperty.HAS_SUB_GUIDELINE, OSDiObjectProperty.HAS_CONDITION_EXPRESSION,
                OSDiObjectProperty.HAS_MIN_APPLICATION_AGE, OSDiObjectProperty.HAS_MAX_APPLICATION_AGE}) {
            if (!modelWrapper.getOSDiWrapper().getValues(iri, unsupported).isEmpty()) {
                throw new UnsupportedOSDiFeatureException("The guideline " + guideline.getShortName() + " defines " + unsupported.getShortName()
                    + ", which is not supported yet in decision trees, since it changes the number of applications.");
            }
        }
        final Optional<ParameterWrapper> frequency = modelWrapper.getWrapperForPropertyAs(iri, OSDiObjectProperty.HAS_FREQUENCY, ParameterWrapper.class);
        final Optional<ParameterWrapper> period = modelWrapper.getWrapperForPropertyAs(iri, OSDiObjectProperty.HAS_PERIOD, ParameterWrapper.class);
        final Optional<ParameterWrapper> duration = modelWrapper.getWrapperForPropertyAs(iri, OSDiObjectProperty.HAS_DURATION, ParameterWrapper.class);
        final Optional<ParameterWrapper> count = modelWrapper.getWrapperForPropertyAs(iri, OSDiObjectProperty.HAS_COUNT, ParameterWrapper.class);
        final Optional<ParameterWrapper> startOffset = modelWrapper.getWrapperForPropertyAs(iri, OSDiObjectProperty.HAS_START_OFFSET, ParameterWrapper.class);
        if (frequency.isEmpty()) {
            if (period.isPresent() || duration.isPresent()) {
                log.warn("The guideline " + guideline.getShortName() + " defines a period or a duration but no frequency. They are ignored: the guideline is applied "
                    + (count.isPresent() ? "as many times as its count (hasCount) says" : "once") + ", at its start.");
            }
        }
        return new GuidelineCost(unitCost, guideline.getShortName(), frequency, period, duration, count, startOffset);
    }
}
