package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode.PayoffWindow;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode.Resolution;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode.WindowedPayoff;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.decisiontree.StrategyCosts;
import es.ull.simulation.hta.osdi.decisiontree.StrategyCosts.GuidelineCost;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.AdverseEventWrapper;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.osdi.ontology.EffectType;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.NextStrategy;
import es.ull.simulation.hta.osdi.ontology.OSDiDataItemType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;
import es.ull.simulation.hta.osdi.ontology.TemporalConstraint;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper.Outcome;

/**
 * Expands the outcomes of the strategies of a decision tree (decision D4 of docs/osdi-1.3-diseno.md). The outcomes of a strategy are independent of
 * the rest of branches of the tree, so they are expanded below each leaf of the sub-tree, for every strategy with outcomes of the health conditions
 * active in the path to the leaf (which also allows knowing whether a health condition that makes a strategy fail is active):
 * <ul>
 * <li>Each outcome is a branch with its risk (the outcome without risk takes the remaining probability). If the strategy fails when a health
 * condition appears (failsIf), the failure is the only branch where such health condition is active, and the success, where it is not.</li>
 * <li>The outcome happens at T = start of the strategy + evaluation time (the start of the strategy, with a warning, if there is no evaluation
 * time). The annual payoffs of the strategy apply from its start until T if the outcome is followed by another strategy or resolves the health
 * condition, and until the end of the node of the health condition otherwise. The next strategy starts at T and its outcomes are expanded in turn.</li>
 * <li>The payoffs of the outcome apply once, at T; annual ones are ignored with a warning. Its effects apply to the leaf.</li>
 * <li>If the outcome resolves the health condition, the annual payoffs of the health condition stop at T. Its effects on the life expectancy
 * cannot stop in a decision tree, so they are kept, with a warning.</li>
 * </ul>
 * The adverse events of a strategy (decision D8) are expanded each time the strategy is applied, before its outcomes, also for the strategies
 * without outcomes and for those that follow an outcome:
 * <ul>
 * <li>Each adverse event is an independent branch with its risk (a probability per application) and a branch without it.</li>
 * <li>Its payoffs apply once, at the start of the strategy; annual ones are ignored with a warning (D7). Its effects apply to the leaf, and an
 * InstantDeathEffect splits the branch into death (with the probability of the magnitude of the effect) and survival. The death happens at the
 * start of the strategy: the life years of its leaf are MIN(start of the strategy, life expectancy without the death), and the payoffs are cut
 * there (phase 16). Nothing else is expanded after the death.</li>
 * <li>The strategies of the adverse event are applied in its branch.</li>
 * <li>The adverse event is active in its branch, so a strategy that fails if the adverse event appears (failsIf) fails only there.</li>
 * <li>The adverse event does not activate the structure of the health condition it represents (isOccurrenceOf).</li>
 * </ul>
 * A time to event T as the risk of an outcome is the probability 1 - e^(-τ/T), where τ is its evaluation time or, if there is none, the time horizon.
 * Cycles in the chain of strategies, adverse events whose risk is a time to event, outcomes whose risk is a time to event without evaluation time
 * or time horizon, and strategies with outcomes whose costs follow a
 * guideline are not supported yet.
 */
final class StrategyOutcomeExpander {
    private final static OSDiLogger log = OSDiLogger.getLogger(StrategyOutcomeExpander.class);
    /**
     * A strategy with outcomes or adverse events pending to be expanded: the node of its health condition, its start, the strategies of its chain,
     * and whether its adverse events have already been expanded
     */
    private record Pending(StrategyWrapper strategy, BranchDestinationNode owner, List<ParameterWrapper> start, Set<IRI> chain, boolean adverseEventsExpanded) {
        Pending(StrategyWrapper strategy, BranchDestinationNode owner, List<ParameterWrapper> start, Set<IRI> chain) {
            this(strategy, owner, start, chain, false);
        }

        Pending withAdverseEventsExpanded() {
            return new Pending(strategy, owner, start, chain, true);
        }
    }
    /** A leaf of the tree, with the strategies with outcomes and the health conditions of its path */
    private record Leaf(BranchDestinationNode node, List<Pending> pending, Set<IRI> activeHealthConditions) {}

    private final Model model;

    private StrategyOutcomeExpander(Model model) {
        this.model = model;
    }

    /**
     * Expands the outcomes of the strategies of a sub-tree.
     * @param model The tree model
     * @param root The root of the sub-tree
     * @throws MalformedOSDiModelException If the outcomes are malformed
     * @throws UnsupportedOSDiFeatureException If the outcomes use features not supported yet
     * @throws MalformedSimulationModelException If the tree cannot be built
     */
    static void expand(Model model, BranchDestinationNode root) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        final List<Leaf> leaves = new ArrayList<>();
        collectLeaves(root, new ArrayList<>(), new HashSet<>(), leaves);
        final StrategyOutcomeExpander expander = new StrategyOutcomeExpander(model);
        for (Leaf leaf : leaves) {
            expander.expandLeaf(leaf.node(), leaf.pending(), leaf.activeHealthConditions());
        }
    }

    private static void collectLeaves(BranchDestinationNode node, List<Pending> pending, Set<IRI> active, List<Leaf> leaves) {
        final List<Pending> newPending = new ArrayList<>(pending);
        for (StrategyWrapper strategy : node.getOutcomeStrategies()) {
            newPending.add(new Pending(strategy, node, List.of(), Set.of(strategy.getIndividualIRI())));
        }
        for (StrategyWrapper strategy : node.getAdverseEventStrategies()) {
            newPending.add(new Pending(strategy, node, List.of(), Set.of(strategy.getIndividualIRI())));
        }
        final Set<IRI> newActive = new HashSet<>(active);
        node.getHealthCondition().ifPresent(hc -> newActive.add(hc.getIndividualIRI()));
        final List<BranchDestinationNode> children = new ArrayList<>(node.getSuccessors());
        if (node.getDefaultSuccessor() != null) {
            children.add(node.getDefaultSuccessor());
        }
        if (children.isEmpty()) {
            if (!newPending.isEmpty()) {
                leaves.add(new Leaf(node, newPending, newActive));
            }
            return;
        }
        for (BranchDestinationNode child : children) {
            collectLeaves(child, newPending, newActive, leaves);
        }
    }

    private void expandLeaf(BranchDestinationNode leaf, List<Pending> pending, Set<IRI> active) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        if (pending.isEmpty()) {
            return;
        }
        final Pending current = pending.get(0);
        final List<Pending> rest = pending.subList(1, pending.size());
        // The adverse events are expanded first, since the strategy may fail because of them
        if (!current.adverseEventsExpanded() && current.strategy().hasAdverseEvents()) {
            expandAdverseEvents(leaf, current, current.strategy().getAdverseEvents(), 0, rest, active);
            return;
        }
        // The payoffs of a strategy without outcomes were already added
        if (!current.strategy().hasOutcomes()) {
            expandLeaf(leaf, new ArrayList<>(rest), active);
            return;
        }
        final List<Outcome> outcomes = current.strategy().getOutcomes();
        // A failure produced by the appearance of a health condition is decided by the path
        final Optional<Outcome> conditionedFailure = outcomes.stream().filter(o -> o.failureCondition().isPresent()).findFirst();
        for (Outcome outcome : outcomes) {
            final BranchDestinationNode node = createOutcomeNode(current, outcome);
            if (conditionedFailure.isPresent()) {
                final boolean failed = active.contains(conditionedFailure.get().failureCondition().get().getIndividualIRI());
                if ((outcome == conditionedFailure.get()) != failed) {
                    continue;
                }
                leaf.link(node);
            }
            else if (outcome.risk().isPresent()) {
                leaf.link(node, getProbability(current.strategy(), outcome));
            }
            else {
                leaf.link(node);
            }
            final NextStrategy next = outcome.next();
            if (!next.isGroup()) {
                final List<Pending> newPending = new ArrayList<>();
                if (next.isPresent()) {
                    applyNextStrategy(node, current, outcome, next.alternatives().get(0).strategy(), newPending);
                }
                newPending.addAll(rest);
                expandLeaf(node, newPending, active);
            }
            else {
                // A group of alternative strategies: a branch per strategy, with its proportion (the strategy without proportion takes the rest)
                for (NextStrategy.Alternative alternative : next.alternatives()) {
                    final BranchDestinationNode branch = new BranchDestinationNode(model, "STRATEGY: " + alternative.strategy().getShortName());
                    if (alternative.proportion().isPresent()) {
                        node.link(branch, alternative.proportion().get());
                    }
                    else {
                        node.link(branch);
                    }
                    final List<Pending> newPending = new ArrayList<>();
                    applyNextStrategy(branch, current, outcome, alternative.strategy(), newPending);
                    newPending.addAll(rest);
                    expandLeaf(branch, newPending, active);
                }
            }
        }
    }

    /**
     * Applies the strategy that follows an outcome, from the time of the outcome: its payoffs are added to the node if it has no outcomes, and it is
     * added to the pending strategies if it has outcomes or adverse events.
     * @param node The node of the outcome (or of the alternative of a group) from which the strategy applies
     * @param current The strategy whose outcome is followed by the next strategy
     * @param outcome The outcome
     * @param next The next strategy
     * @param newPending The pending strategies, to which the next strategy is added
     * @throws UnsupportedOSDiFeatureException If the chain of strategies goes back to a strategy already applied
     */
    private void applyNextStrategy(BranchDestinationNode node, Pending current, Outcome outcome, StrategyWrapper next, List<Pending> newPending) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (current.chain().contains(next.getIndividualIRI())) {
            throw new UnsupportedOSDiFeatureException("The chain of strategies that follows the outcome " + outcome.name() + " goes back to the strategy " + next.getShortName()
                + ". Cycles are not supported in decision trees: a repetition must be modeled as another strategy.");
        }
        final List<ParameterWrapper> nextStart = getOutcomeTime(current, outcome);
        if (!next.hasOutcomes()) {
            addStrategyPayoffs(node, next, new PayoffWindow(current.owner(), nextStart, Optional.empty()));
        }
        if (next.hasOutcomes() || next.hasAdverseEvents()) {
            final Set<IRI> chain = new HashSet<>(current.chain());
            chain.add(next.getIndividualIRI());
            newPending.add(new Pending(next, current.owner(), nextStart, chain));
        }
    }

    /**
     * Expands the adverse events of a strategy, from the i-th one, below a node. Each adverse event is an independent branch with its risk, and a branch
     * without it. When all of them are expanded, the expansion continues with the outcomes of the strategy and with the rest of pending strategies.
     * @param node The node below which the adverse events are expanded
     * @param current The strategy whose adverse events are expanded
     * @param adverseEvents The adverse events of the strategy
     * @param i The index of the next adverse event to expand
     * @param rest The rest of pending strategies
     * @param active The health conditions active in the path (including the adverse events that happened)
     */
    private void expandAdverseEvents(BranchDestinationNode node, Pending current, List<AdverseEventWrapper> adverseEvents, int i, List<Pending> rest, Set<IRI> active)
            throws MalformedOSDiModelException, UnsupportedOSDiFeatureException, MalformedSimulationModelException {
        if (i == adverseEvents.size()) {
            final List<Pending> newPending = new ArrayList<>();
            newPending.add(current.withAdverseEventsExpanded());
            newPending.addAll(rest);
            expandLeaf(node, newPending, active);
            return;
        }
        final AdverseEventWrapper adverseEvent = adverseEvents.get(i);
        final BranchDestinationNode happens = createAdverseEventNode(current, adverseEvent);
        node.link(happens, getProbability(current.strategy(), adverseEvent));
        final BranchDestinationNode doesNotHappen = new BranchDestinationNode(model, "NO ADVERSE EVENT: " + adverseEvent.getShortName());
        node.link(doesNotHappen);

        // The branch where the adverse event happens. An instant death is a branch of its own, where nothing else is expanded
        BranchDestinationNode alive = happens;
        for (EffectWrapper effect : adverseEvent.getEffects()) {
            if (EffectType.INSTANT_DEATH_EFFECT.equals(effect.getEffectType())) {
                final BranchDestinationNode death = new BranchDestinationNode(model, "Death from " + adverseEvent.getShortName());
                death.updateEffects(adverseEvent, EffectType.INSTANT_DEATH_EFFECT);
                // The death happens at the start of the strategy
                death.setDeathTime(new PayoffWindow(current.owner(), current.start(), Optional.empty()));
                happens.link(death, effect.getEffectMagnitude());
                if (alive == happens) {
                    alive = new BranchDestinationNode(model, "Alive with " + adverseEvent.getShortName());
                }
            }
        }
        if (alive != happens) {
            happens.link(alive);
        }
        // The strategies of the adverse event are applied in its branch
        final List<Pending> restWithAdverseEvent = new ArrayList<>();
        for (StrategyWrapper strategy : adverseEvent.getStrategies()) {
            if (current.chain().contains(strategy.getIndividualIRI())) {
                throw new UnsupportedOSDiFeatureException("The adverse event " + adverseEvent.getShortName() + " is treated with the strategy " + strategy.getShortName()
                    + ", which is already applied in the chain. Cycles are not supported in decision trees: a repetition must be modeled as another strategy.");
            }
            if (!strategy.hasOutcomes()) {
                addStrategyPayoffs(happens, strategy, new PayoffWindow(current.owner(), current.start(), Optional.empty()));
            }
            if (strategy.hasOutcomes() || strategy.hasAdverseEvents()) {
                final Set<IRI> chain = new HashSet<>(current.chain());
                chain.add(strategy.getIndividualIRI());
                restWithAdverseEvent.add(new Pending(strategy, current.owner(), current.start(), chain));
            }
        }
        restWithAdverseEvent.addAll(rest);
        final Set<IRI> activeWithAdverseEvent = new HashSet<>(active);
        activeWithAdverseEvent.add(adverseEvent.getIndividualIRI());
        expandAdverseEvents(alive, current, adverseEvents, i + 1, restWithAdverseEvent, activeWithAdverseEvent);
        // The branch without the adverse event
        expandAdverseEvents(doesNotHappen, current, adverseEvents, i + 1, rest, active);
    }

    /**
     * Creates the node of an adverse event, with its payoffs, which apply once, at the start of the strategy, and its effects (except death, which
     * is a branch of its own).
     */
    private BranchDestinationNode createAdverseEventNode(Pending current, AdverseEventWrapper adverseEvent) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final BranchDestinationNode node = new BranchDestinationNode(model, "ADVERSE EVENT: " + adverseEvent.getShortName());
        final PayoffWindow atStart = new PayoffWindow(current.owner(), current.start(), Optional.empty());
        final List<ParameterWrapper> costs = new ArrayList<>(adverseEvent.getCosts());
        final List<ParameterWrapper> utilities = new ArrayList<>(adverseEvent.getUtilities());
        final String owner = "The adverse event " + adverseEvent.getShortName();
        DiseaseSubTreeGenerator.removeAnnualPayoffs(owner, costs);
        DiseaseSubTreeGenerator.removeAnnualPayoffs(owner, utilities);
        costs.forEach(cost -> node.getWindowedCosts().add(new WindowedPayoff(cost, atStart)));
        utilities.forEach(utility -> node.getWindowedDisutilities().add(new WindowedPayoff(utility, atStart)));
        node.updateEffects(adverseEvent, EffectType.GENERAL_EFFECT);
        node.updateEffects(adverseEvent, EffectType.MORTALITY_RATE_EFFECT);
        node.updateEffects(adverseEvent, EffectType.LIFE_EXPECTANCY_REDUCTION_EFFECT);
        return node;
    }

    /**
     * Returns the probability of an adverse event per application of the strategy, converted into a probability parameter if needed.
     */
    private static ParameterWrapper getProbability(StrategyWrapper strategy, AdverseEventWrapper adverseEvent) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ParameterWrapper risk = adverseEvent.getRisk();
        // TODO: Support times to event in the risks of the adverse events in decision trees (see docs/known-limitations.md)
        if (OSDiDataItemType.DI_TIME_TO_EVENT.equals(risk.getDataItemType())) {
            throw new UnsupportedOSDiFeatureException("The risk of the adverse event " + adverseEvent.getShortName() + " of the strategy " + strategy.getShortName()
                + " is a time to event, which is not supported in decision trees: it must be a probability per application of the strategy.");
        }
        return AbstractModelFactory.getProbabilityParameterFromGenericParameter(risk);
    }

    /**
     * Creates the node of an outcome, with the payoffs of the strategy until the outcome, the payoffs and effects of the outcome, and the resolution
     * of the health condition, if any.
     */
    private BranchDestinationNode createOutcomeNode(Pending current, Outcome outcome) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final BranchDestinationNode node = new BranchDestinationNode(model, "OUTCOME: " + outcome.name());
        final List<ParameterWrapper> time = getOutcomeTime(current, outcome);
        final boolean stops = outcome.next().isPresent() || outcome.resolves();
        addStrategyPayoffs(node, current.strategy(), new PayoffWindow(current.owner(), current.start(), stops ? Optional.of(time) : Optional.empty()));
        if (outcome.wrapper().isPresent()) {
            final PayoffWindow atOutcome = new PayoffWindow(current.owner(), time, Optional.empty());
            final List<ParameterWrapper> costs = new ArrayList<>(outcome.wrapper().get().getCosts());
            final List<ParameterWrapper> utilities = new ArrayList<>(outcome.wrapper().get().getUtilities());
            final String owner = "The outcome " + outcome.name();
            DiseaseSubTreeGenerator.removeAnnualPayoffs(owner, costs);
            DiseaseSubTreeGenerator.removeAnnualPayoffs(owner, utilities);
            costs.forEach(cost -> node.getWindowedCosts().add(new WindowedPayoff(cost, atOutcome)));
            utilities.forEach(utility -> node.getWindowedDisutilities().add(new WindowedPayoff(utility, atOutcome)));
            node.updateEffects(outcome.wrapper().get());
        }
        if (outcome.resolves()) {
            final BranchDestinationNode owner = current.owner();
            final Set<ParameterWrapper> resolved = new TreeSet<>(owner.getCostParameters());
            resolved.addAll(owner.getDisutilityParameters());
            node.setResolution(new Resolution(owner, time, resolved));
            for (EffectWrapper effect : owner.getEffects()) {
                if (EffectType.LIFE_EXPECTANCY_REDUCTION_EFFECT.equals(effect.getEffectType()) || EffectType.MORTALITY_RATE_EFFECT.equals(effect.getEffectType())) {
                    log.warn("The outcome " + outcome.name() + " resolves " + owner.name() + ", but its effect " + effect.getShortName() + " on the life expectancy is kept, since it cannot stop in a decision tree.");
                }
            }
        }
        return node;
    }

    /**
     * Adds the costs and utilities of a strategy to a node, within a period.
     */
    private static void addStrategyPayoffs(BranchDestinationNode node, StrategyWrapper strategy, PayoffWindow window) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final List<ParameterWrapper> costs = new ArrayList<>();
        final List<GuidelineCost> guidelineCosts = new ArrayList<>();
        StrategyCosts.collect(strategy, costs, guidelineCosts);
        // TODO: Support guidelines in strategies with outcomes (see docs/known-limitations.md)
        if (!guidelineCosts.isEmpty()) {
            throw new UnsupportedOSDiFeatureException("The strategy " + strategy.getShortName() + " has outcomes, and some of its costs follow a guideline. Guidelines are not supported yet in the chains of strategies with outcomes.");
        }
        costs.forEach(cost -> node.getWindowedCosts().add(new WindowedPayoff(cost, window)));
        strategy.getUtilities().forEach(utility -> node.getWindowedDisutilities().add(new WindowedPayoff(utility, window)));
    }

    /**
     * Returns the time of an outcome, since the start of the node of its health condition: the start of the strategy plus its evaluation time.
     */
    private static List<ParameterWrapper> getOutcomeTime(Pending current, Outcome outcome) {
        final List<ParameterWrapper> time = new ArrayList<>(current.start());
        if (outcome.evaluationTime().isPresent()) {
            time.add(outcome.evaluationTime().get());
        }
        else if (outcome.failureCondition().isEmpty()) {
            log.warn("The outcome " + outcome.name() + " does not define an evaluation time. It is assumed to happen at the start of the strategy " + current.strategy().getShortName() + ".");
        }
        // Decision D5: the failure produced by a health condition happens when the condition appears, i.e., not before its onset age
        if (outcome.failureCondition().isPresent() && outcome.failureCondition().get() instanceof ClinicalProgressionWrapper condition) {
            final Optional<ParameterWrapper> onsetAge = condition.getTemporalConstraintForProgression().flatMap(TemporalConstraint::startTime);
            if (onsetAge.isPresent()) {
                time.add(BranchDestinationNode.notBeforeAge(onsetAge.get()));
            }
        }
        return time;
    }

    /**
     * Returns the probability of an outcome, converted into a probability parameter if needed. A time to event T (decision D6) becomes the
     * probability that the outcome happens before it is evaluated, 1 - e^(-τ/T), where τ is its evaluation time; without evaluation time, the
     * time horizon H of the experiment is used, 1 - e^(-H/T).
     * @throws UnsupportedOSDiFeatureException If the risk is a time to event, and neither the outcome defines an evaluation time nor the experiment a time horizon
     */
    private ParameterWrapper getProbability(StrategyWrapper strategy, Outcome outcome) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ParameterWrapper risk = outcome.risk().get();
        if (OSDiDataItemType.DI_TIME_TO_EVENT.equals(risk.getDataItemType())) {
            if (outcome.evaluationTime().isPresent()) {
                return AbstractModelFactory.getProbabilityParameterFromTimeToEvent(risk, outcome.evaluationTime(), Double.NaN);
            }
            final OptionalDouble horizon = model.getExperimentWrapper().getDeclaredTimeHorizon();
            if (horizon.isPresent()) {
                return AbstractModelFactory.getProbabilityParameterFromTimeToEvent(risk, Optional.empty(), horizon.getAsDouble());
            }
            throw new UnsupportedOSDiFeatureException("The risk of the outcome " + outcome.name() + " of the strategy " + strategy.getShortName() + " is a time to event, "
                + "but neither the outcome defines an evaluation time (hasEvaluationTime) nor the experiment a time horizon (hasTimeHorizon), so it cannot be converted into a probability.");
        }
        return AbstractModelFactory.getProbabilityParameterFromGenericParameter(risk);
    }
}
