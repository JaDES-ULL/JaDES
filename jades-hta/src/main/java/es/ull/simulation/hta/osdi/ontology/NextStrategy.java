package es.ull.simulation.hta.osdi.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * The strategy that follows a strategy or one of its outcomes (followedByStrategy, followedOnFailureBy, followedOnSuccessBy): either a single strategy,
 * or a group of alternative strategies (phase 12 of docs/osdi-1.3-diseno.md). In a group of alternatives (Group with hasGroupItem), each patient
 * receives one of its strategies, with the proportion that the strategy declares (hasProportionWithinGroup). At most one strategy of the group may
 * lack a proportion: it takes the rest (1 minus the sum of the proportions of the others).
 * <p>
 * A group whose strategies do not declare proportions is not supported as the next strategy: in a strategy replacement (StrategyReplacementEffect),
 * a group means that all its strategies apply together. The proportion is a property of the strategy, so a strategy that belongs to several groups
 * has the same proportion in all of them.
 * </p>
 * @param alternatives The alternative strategies, with their proportions; the one without proportion, if any, is the last one. Empty if there is no next strategy
 * @param group The group of alternatives, if the next strategy is a group
 */
public record NextStrategy(List<Alternative> alternatives, Optional<IModelItemWrapper> group) {
    /**
     * One of the alternative strategies that may follow a strategy or an outcome
     * @param strategy The strategy
     * @param proportion The proportion of the patients who receive the strategy; empty for a single next strategy, or for the strategy of a group that takes the rest
     */
    public record Alternative(StrategyWrapper strategy, Optional<ParameterWrapper> proportion) {}

    /** No next strategy */
    public static final NextStrategy NONE = new NextStrategy(List.of(), Optional.empty());

    /**
     * Returns true if there is a next strategy (a single one or a group of alternatives)
     * @return true if there is a next strategy
     */
    public boolean isPresent() {
        return !alternatives.isEmpty();
    }

    /**
     * Returns true if the next strategy is a group of alternative strategies
     * @return true if the next strategy is a group of alternative strategies
     */
    public boolean isGroup() {
        return group.isPresent();
    }

    /**
     * Returns the next strategy, if it is a single strategy
     * @return The next strategy, if it is a single strategy; empty if there is none or if it is a group of alternatives
     */
    public Optional<StrategyWrapper> single() {
        return (isGroup() || alternatives.isEmpty()) ? Optional.empty() : Optional.of(alternatives.get(0).strategy());
    }

    /**
     * Returns this next strategy, if present, or another one otherwise
     * @param other The next strategy to use if this one is not present
     * @return This next strategy, if present, or the other one
     */
    public NextStrategy or(NextStrategy other) {
        return isPresent() ? this : other;
    }

    /**
     * Returns a description of the next strategy, for the messages
     * @return The name of the strategy or of the group
     */
    public String name() {
        if (group.isPresent()) {
            return group.get().getShortName();
        }
        return alternatives.isEmpty() ? "none" : alternatives.get(0).strategy().getShortName();
    }

    /**
     * Reads the strategy, or the group of alternative strategies, that an individual points to with a property.
     * @param modelWrapper The model
     * @param individualIRI The individual (a strategy or an outcome)
     * @param property The property (followedByStrategy, followedOnFailureBy or followedOnSuccessBy)
     * @return The next strategy
     * @throws MalformedOSDiModelException If the individual points to something that is neither a strategy nor a group of strategies, a group has
     * members that are not strategies, more than one member without proportion, or deterministic proportions that do not add up to 1
     * @throws UnsupportedOSDiFeatureException If none of the strategies of a group declares a proportion
     */
    public static NextStrategy read(ModelWrapper modelWrapper, IRI individualIRI, OSDiObjectProperty property) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Optional<IModelItemWrapper> value = modelWrapper.getWrapperForProperty(individualIRI, property);
        if (value.isEmpty()) {
            return NONE;
        }
        final IModelItemWrapper item = value.get();
        final String source = individualIRI.getShortForm() + " (" + property.getShortName() + ")";
        if (item instanceof StrategyWrapper strategy) {
            strategy.initialize();
            return new NextStrategy(List.of(new Alternative(strategy, Optional.empty())), Optional.empty());
        }
        if (!modelWrapper.getOSDiWrapper().isInstanceOf(item.getIndividualIRI(), OSDiClass.GROUP)) {
            throw new MalformedOSDiModelException("The next strategy of " + source + ", " + item.getShortName() + ", is neither a strategy nor a group of strategies.");
        }
        final Set<IModelItemWrapper> members = modelWrapper.getWrappersForProperty(item.getIndividualIRI(), OSDiObjectProperty.HAS_GROUP_ITEM);
        if (members.isEmpty()) {
            throw new MalformedOSDiModelException("The group of strategies " + item.getShortName() + ", which follows " + source + ", has no items in the model.");
        }
        final List<Alternative> withProportion = new ArrayList<>();
        final List<Alternative> withoutProportion = new ArrayList<>();
        double sum = 0.0;
        boolean deterministic = true;
        for (IModelItemWrapper member : members) {
            if (!(member instanceof StrategyWrapper strategy)) {
                throw new MalformedOSDiModelException("The group of strategies " + item.getShortName() + ", which follows " + source + ", includes " + member.getShortName() + ", which is not a strategy.");
            }
            strategy.initialize();
            final Optional<ParameterWrapper> proportion = modelWrapper.getWrapperForPropertyAs(strategy.getIndividualIRI(), OSDiObjectProperty.HAS_PROPORTION_WITHIN_GROUP, ParameterWrapper.class);
            if (proportion.isEmpty()) {
                withoutProportion.add(new Alternative(strategy, Optional.empty()));
                continue;
            }
            proportion.get().initialize();
            withProportion.add(new Alternative(strategy, proportion));
            if (proportion.get().getParameterNatureData() instanceof ParameterWrapper.DeterministicParameterData data) {
                sum += data.value();
            }
            else if (proportion.get().getParameterNatureData() instanceof ParameterWrapper.SecondOrderUncertaintyParameterData data) {
                sum += data.expectedValue();
            }
            else {
                deterministic = false;
            }
        }
        if (withProportion.isEmpty()) {
            throw new UnsupportedOSDiFeatureException("The group of strategies " + item.getShortName() + " follows " + source + ", but its strategies do not declare their proportions "
                + "(hasProportionWithinGroup). As the next strategy, a group is a choice among alternative strategies, so each strategy must declare the proportion of patients who receive it "
                + "(one of them may take the rest).");
        }
        if (withoutProportion.size() > 1) {
            throw new MalformedOSDiModelException("The group of strategies " + item.getShortName() + ", which follows " + source + ", has several strategies without proportion: "
                + withoutProportion.stream().map(a -> a.strategy().getShortName()).toList() + ". Only one of them may take the rest.");
        }
        if (deterministic && withoutProportion.isEmpty() && Math.abs(sum - 1.0) > 1e-9) {
            throw new MalformedOSDiModelException("The proportions of the strategies of the group " + item.getShortName() + ", which follows " + source + ", add up to " + sum
                + " instead of 1, and none of them takes the rest.");
        }
        if (deterministic && sum > 1.0 + 1e-9) {
            throw new MalformedOSDiModelException("The proportions of the strategies of the group " + item.getShortName() + ", which follows " + source + ", add up to " + sum + ", more than 1.");
        }
        final List<Alternative> alternatives = new ArrayList<>(withProportion);
        alternatives.addAll(withoutProportion);
        return new NextStrategy(alternatives, Optional.of(item));
    }
}
