package es.ull.simulation.hta.osdi.ontology;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * The strategy replacements of the interventions (decision D2 of docs/osdi-1.3-diseno.md): an intervention changes the strategies of a health
 * condition in its arm through a strategy replacement effect. It is shared by the generators of decision trees and the discrete event simulation.
 */
public final class StrategyReplacements {

    private StrategyReplacements() {
    }

    /**
     * Returns the strategy replacements of an intervention: for each health condition affected by a strategy replacement effect of the intervention,
     * the strategies that replace its own in the arm of the intervention. Interventions only declare strategies through such effects.
     * @param intervention The intervention
     * @return The strategies that replace those of each health condition, indexed by the IRI of the health condition
     * @throws MalformedOSDiModelException If the intervention declares strategies directly (hasStrategy), or two strategy replacements affect
     * the same health condition
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered
     */
    public static Map<IRI, Set<StrategyWrapper>> of(InterventionWrapper intervention) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (!intervention.getStrategies().isEmpty()) {
            throw new MalformedOSDiModelException("The intervention " + intervention.getShortName() + " declares strategies (" + intervention.getStrategies()
                + "). Interventions must declare their strategies through a strategy replacement effect (" + OSDiClass.STRATEGY_REPLACEMENT_EFFECT.getShortName() + ").");
        }
        final Map<IRI, Set<StrategyWrapper>> replacements = new HashMap<>();
        final Map<IRI, EffectWrapper> effectsByHealthCondition = new HashMap<>();
        for (EffectWrapper effect : intervention.getEffects()) {
            if (effect.getAffectedHealthCondition().isPresent()) {
                final IRI healthCondition = effect.getAffectedHealthCondition().get().getIndividualIRI();
                final EffectWrapper previous = effectsByHealthCondition.put(healthCondition, effect);
                if (previous != null) {
                    throw new MalformedOSDiModelException("The intervention " + intervention.getShortName() + " defines two strategy replacements (" + previous.getShortName()
                        + " and " + effect.getShortName() + ") of the same health condition " + effect.getAffectedHealthCondition().get().getShortName() + ".");
                }
                replacements.put(healthCondition, effect.getReplacementStrategies());
            }
        }
        return replacements;
    }

    /**
     * Applies a strategy replacement by type of strategy (detection, i.e. screening or diagnosis; follow-up; or treatment): each replacement
     * strategy replaces the strategies of its type, or it is added if there was none of that type. The strategies of other types are kept.
     * @param originalStrategies The strategies of a health condition
     * @param replacement The replacement strategies, or null if there is no replacement
     * @return The strategies after the replacement
     */
    public static Set<StrategyWrapper> replaceByType(Set<StrategyWrapper> originalStrategies, Set<StrategyWrapper> replacement) {
        final Set<StrategyWrapper> strategies = new TreeSet<>(originalStrategies);
        if (replacement == null) {
            return strategies;
        }
        final Set<StrategyKind> replacedKinds = new TreeSet<>();
        for (StrategyWrapper strategy : replacement) {
            replacedKinds.add(StrategyKind.of(strategy));
        }
        strategies.removeIf(strategy -> replacedKinds.contains(StrategyKind.of(strategy)));
        strategies.addAll(replacement);
        return strategies;
    }

    /**
     * The types of strategy used to decide which strategies a strategy replacement replaces.
     */
    private enum StrategyKind {
        DETECTION, FOLLOW_UP, TREATMENT;

        static StrategyKind of(StrategyWrapper strategy) {
            switch (strategy.getStrategyType()) {
                case DIAGNOSIS_STRATEGY:
                case SCREENING_STRATEGY:
                    return DETECTION;
                case FOLLOW_UP_STRATEGY:
                    return FOLLOW_UP;
                case LINE_OF_THERAPY:
                default:
                    return TREATMENT;
            }
        }
    }
}
