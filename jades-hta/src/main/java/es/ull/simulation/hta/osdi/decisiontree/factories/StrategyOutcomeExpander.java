package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import es.ull.simulation.hta.osdi.ontology.EffectType;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiDataItemType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;
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
 * Cycles in the chain of strategies, outcomes whose risk is a time to event and strategies with outcomes whose costs follow a guideline are not
 * supported yet.
 */
final class StrategyOutcomeExpander {
    private final static OSDiLogger log = OSDiLogger.getLogger(StrategyOutcomeExpander.class);
    /** A strategy with outcomes pending to be expanded: the node of its health condition, its start, and the strategies of its chain */
    private record Pending(StrategyWrapper strategy, BranchDestinationNode owner, List<ParameterWrapper> start, Set<IRI> chain) {}
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
            final List<Pending> newPending = new ArrayList<>();
            if (outcome.nextStrategy().isPresent()) {
                final StrategyWrapper next = outcome.nextStrategy().get();
                if (current.chain().contains(next.getIndividualIRI())) {
                    throw new UnsupportedOSDiFeatureException("The chain of strategies that follows the outcome " + outcome.name() + " goes back to the strategy " + next.getShortName()
                        + ". Cycles are not supported in decision trees: a repetition must be modeled as another strategy.");
                }
                final List<ParameterWrapper> nextStart = getOutcomeTime(current, outcome);
                if (next.hasOutcomes()) {
                    final Set<IRI> chain = new HashSet<>(current.chain());
                    chain.add(next.getIndividualIRI());
                    newPending.add(new Pending(next, current.owner(), nextStart, chain));
                }
                else {
                    addStrategyPayoffs(node, next, new PayoffWindow(current.owner(), nextStart, Optional.empty()));
                }
            }
            newPending.addAll(rest);
            expandLeaf(node, newPending, active);
        }
    }

    /**
     * Creates the node of an outcome, with the payoffs of the strategy until the outcome, the payoffs and effects of the outcome, and the resolution
     * of the health condition, if any.
     */
    private BranchDestinationNode createOutcomeNode(Pending current, Outcome outcome) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final BranchDestinationNode node = new BranchDestinationNode(model, "OUTCOME: " + outcome.name());
        final List<ParameterWrapper> time = getOutcomeTime(current, outcome);
        final boolean stops = outcome.nextStrategy().isPresent() || outcome.resolves();
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
        return time;
    }

    /**
     * Returns the probability of an outcome, converted into a probability parameter if needed.
     */
    private static ParameterWrapper getProbability(StrategyWrapper strategy, Outcome outcome) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ParameterWrapper risk = outcome.risk().get();
        // TODO: Support times to event in decision trees (see docs/known-limitations.md)
        if (OSDiDataItemType.DI_TIME_TO_EVENT.equals(risk.getDataItemType())) {
            throw new UnsupportedOSDiFeatureException("The risk of the outcome " + outcome.name() + " of the strategy " + strategy.getShortName() + " is a time to event, which is not supported yet in decision trees.");
        }
        return AbstractModelFactory.getProbabilityParameterFromGenericParameter(risk);
    }
}
