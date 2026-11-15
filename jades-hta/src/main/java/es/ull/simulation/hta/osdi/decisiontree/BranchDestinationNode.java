package es.ull.simulation.hta.osdi.decisiontree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.Named;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.TemporalConstraint;
import es.ull.simulation.hta.osdi.ontology.EffectType;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.HealthConditionWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWithEffectWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWithStrategyWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;

/**
 * Destinations of branches in a decision tree, either a probability node or a payoff node.
 */
public class BranchDestinationNode implements Named, DecisionTreeNode {
    private final static OSDiLogger log = OSDiLogger.getLogger(BranchDestinationNode.class);
    /**
     * The name of this node. It is used as the label of the node in the decision tree.
     */
    private final String name;
    /**
     * The predecessor of this probability node, which is the component that leads to this node in the decision tree.
     */
    private DecisionTreeNode predecessor;
    /**
     * The probability used to select this node from its predecessor.
     * This parameter can be null if the node is used as a default branch.
     */
    private ParameterWrapper probability;
    /**
     * The list of cost parameters associated with this node, which is used to calculate the cost of the decision represented by this node.
     */
    private final ArrayList<ParameterWrapper> costParameters;
    /**
     * The list of disutility parameters associated with this node, which is used to calculate the disutility of the decision represented by this node.
     */
    private final Set<ParameterWrapper> disutilityParameters;
    /**
     * The list of lifetime reduction parameters associated with this node, which is used to calculate the lifetime associated with the decision represented by this node.
     */
    private final Set<EffectWrapper> effects;
    /**
     * The costs of the health technologies of the strategies of this node that apply according to a guideline (see {@link StrategyCosts}).
     */
    private final ArrayList<StrategyCosts.GuidelineCost> guidelineCosts;
    /**
     * The temporal constraint for this node, which defines the time period during which this node is valid. Such restrictions are used to 
     * properly compute the payoffs of the decision tree.
     */
    private TemporalConstraint temporalConstraint;
    /**
     * The parent tree model that this part belongs to.
     */
    protected final Model parentTreeModel;
    /**
     * The list of links in this probability node, where each branch is associated with a parameter that characterizes it and points to another probability node or payoff.
     */
    private final ArrayList<BranchDestinationNode> successors;
    /**
     * The default link of this probability node. The probability of the default successor is always "1 -" the sum of the probabilities of the rest of successors.
     */
    private BranchDestinationNode defaultSuccessor = null;

    /**
     * Creates a new ParameterizedDecisionTreeComponent.
     *
     * @param context The context for this node.
     * @param name The name of this node.
     * @throws MalformedSimulationModelException
     */
    public BranchDestinationNode(Model parentTreeModel, String name) {
        this.parentTreeModel = parentTreeModel;
        this.name = name;
        this.predecessor = null;
        this.probability = null;
        this.costParameters = new ArrayList<>();
        this.disutilityParameters = new TreeSet<>();
        this.effects = new TreeSet<>();
        this.guidelineCosts = new ArrayList<>();
        this.temporalConstraint = null; // Initialize temporal constraint as null
        this.successors = new ArrayList<>();
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Returns the predecessor of this node.
     * @return The predecessor of this node.
     */
    public DecisionTreeNode getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(DecisionTreeNode predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * Returns the probability used to select this node from its predecessor.
     * This parameter can be null if the node is used as a default branch.
     * @return The probability used to select this node, or null if it is a default branch.
     */
    public ParameterWrapper getProbability() {
        return probability;
    }

    public void setProbability(ParameterWrapper probability) {
        this.probability = probability;
    }

    /**
     * Returns the list of cost parameters associated with this branch destination component.
     * The cost associated with the decision represented by this component is calculated as the sum of these parameters.
     * @return The list of cost parameters for this branch destination component.
     */
    public ArrayList<ParameterWrapper> getCostParameters() {
        return costParameters;
    }

    /**
     * Returns the list of disutility parameters associated with this branch destination component.
     * The disutility associated with the decision represented by this component is calculated according to the disutility combination method defined for the model.
     * @return The list of disutility parameters for this branch destination component.
     */
    public Set<ParameterWrapper> getDisutilityParameters() {
        return disutilityParameters;
    }

    /**
     * Returns the list of effects associated with this branch destination component.
     * @return The list of effects for this branch destination component.
     */
    public Set<EffectWrapper> getEffects() {
        return effects;
    }

    /**
     * A period, relative to the start of the node of a health condition (owner), during which some payoffs apply (decision D4): from the sum of the
     * "from" times to the sum of the "to" times (or until the end of the owner, if there is no "to"). One-time payoffs apply at the start of the period.
     * @param owner The node of the health condition that applies the strategy
     * @param from The times (evaluation times of the previous outcomes) whose sum gives the start of the period, since the start of the owner
     * @param to The times whose sum gives the end of the period, since the start of the owner; empty if the period lasts until the end of the owner
     */
    public record PayoffWindow(BranchDestinationNode owner, List<ParameterWrapper> from, Optional<List<ParameterWrapper>> to) {}
    /**
     * A cost or utility that applies within a period (see {@link PayoffWindow}).
     * @param parameter The cost or utility
     * @param window The period
     */
    public record WindowedPayoff(ParameterWrapper parameter, PayoffWindow window) {}
    /**
     * The resolution of a health condition by an outcome of a strategy: the annual payoffs of the health condition (those of its node when the
     * outcomes were expanded) stop at the given time since the start of its node.
     * @param owner The node of the health condition
     * @param time The times whose sum gives the moment of the resolution, since the start of the owner
     * @param parameters The payoffs of the health condition that stop
     */
    public record Resolution(BranchDestinationNode owner, List<ParameterWrapper> time, Set<ParameterWrapper> parameters) {}

    /** The health condition represented by this node, if any */
    private Optional<HealthConditionWrapper> healthCondition = Optional.empty();
    /** The strategies with outcomes of the health condition of this node, whose outcomes are expanded below the leaves (see StrategyOutcomeExpander) */
    private final List<StrategyWrapper> outcomeStrategies = new ArrayList<>();
    /** The costs of this node that apply within a period */
    private final List<WindowedPayoff> windowedCosts = new ArrayList<>();
    /** The disutilities of this node that apply within a period */
    private final List<WindowedPayoff> windowedDisutilities = new ArrayList<>();
    /** The resolution of a health condition produced by this node, if any */
    private Optional<Resolution> resolution = Optional.empty();

    public Optional<HealthConditionWrapper> getHealthCondition() {
        return healthCondition;
    }

    public void setHealthCondition(HealthConditionWrapper healthCondition) {
        this.healthCondition = Optional.of(healthCondition);
    }

    public List<StrategyWrapper> getOutcomeStrategies() {
        return outcomeStrategies;
    }

    public List<WindowedPayoff> getWindowedCosts() {
        return windowedCosts;
    }

    public List<WindowedPayoff> getWindowedDisutilities() {
        return windowedDisutilities;
    }

    public Optional<Resolution> getResolution() {
        return resolution;
    }

    public void setResolution(Resolution resolution) {
        this.resolution = Optional.of(resolution);
    }

    /**
     * Returns the costs of the health technologies of the strategies of this node that apply according to a guideline.
     * @return The costs that apply according to a guideline.
     */
    public List<StrategyCosts.GuidelineCost> getGuidelineCosts() {
        return guidelineCosts;
    }

    /**
     * Adds costs that apply according to a guideline to this node.
     * @param guidelineCosts The costs to add.
     */
    public void addGuidelineCosts(Collection<StrategyCosts.GuidelineCost> guidelineCosts) {
        this.guidelineCosts.addAll(guidelineCosts);
    }

    /**
     * Adds a list of cost parameters to this branch destination component.
     * These parameters are used to calculate the cost associated with the decision represented by this component.
     * The final cost is calculated as the sum of the cost parameters.
     * @param costParameters The list of cost parameters to add.
     */
    public void addCostParameters(Collection<ParameterWrapper> costParameters) {
        this.costParameters.addAll(costParameters);
    }

    /**
     * Adds a list of disutility parameters to this branch destination component.
     * These parameters are used to calculate the disutility associated with the decision represented by this component.
     * The final disutility is calculated according to the disutility combination method defined for the model.
     * @param disutilityParameters The list of disutility parameters to add.
     */
    public void addDisutilityParameters(Collection<ParameterWrapper> disutilityParameters) {
        this.disutilityParameters.addAll(disutilityParameters);
    }

    /**
     * Adds a list of effects to this branch destination component.
     * @param effects The list of effects to add.
     */
    public void addEffects(Collection<EffectWrapper> effects) {
        this.effects.addAll(effects);
    }

    /**
     * Adds a cost parameter for this branch destination component.
     * This parameter is used to calculate the cost associated with the decision represented by this component. 
     * The final cost is calculated as the sum of the cost parameters.
     * @param costParameter The cost parameter to set.
     */
    void addCostParameter(ParameterWrapper costParameter) {
        this.costParameters.add(costParameter);
    }

    /**
     * Adds a disutility parameter for this branch destination component.
     * This parameter is used to calculate the disutility associated with the decision represented by this component.
     * The final disutility is calculated according to the disutility combination method defined for the model.
     * @param disutilityParameter The disutility parameter to set.
     */
    void addDisutilityParameter(ParameterWrapper disutilityParameter) {
        this.disutilityParameters.add(disutilityParameter);
    }

    /**
     * Adds an effect for this branch destination component.
     * @param effect The effect to set.
     */ 
    void addEffect(EffectWrapper effect) {
        this.effects.add(effect);
    }

    /**
     * Sets the temporal constraint for this node.
     * This constraint defines the time period during which this node is valid.
     * @param temporalConstraint The temporal constraint to set.
     */
    public void setTemporalConstraint(TemporalConstraint temporalConstraint) {
        this.temporalConstraint = temporalConstraint;
    }
    
    /** Returns the temporal constraint for this node. 
     * This constraint defines the time period during which this node is valid.
     */
    public TemporalConstraint getTemporalConstraint() {
        return temporalConstraint;
    }

    @Override
    public void addSuccessor(BranchDestinationNode successor) throws MalformedSimulationModelException {
        if (successor == null) {
            throw new IllegalArgumentException("Successor cannot be null");
        }
        // If the successor did not specify a probability, it is considered a default branch.
        if (successor.getProbability() == null) {
            if (this.defaultSuccessor != null) {
                throw new MalformedSimulationModelException("A default successor already exists for this probability node");
            }
            this.defaultSuccessor = successor;
        }
        else
            successors.add(successor);
    }

    @Override
    public ArrayList<BranchDestinationNode> getSuccessors() {
        return successors;
    }

    /**
     * Returns the default link of this probability node.
     * @return The default link, or null if no default branch has been set.
     */
    public BranchDestinationNode getDefaultSuccessor() {
        return defaultSuccessor;
    }

    /**
     * Returns true if this node is a payoff, i.e., it has no successors
     * @return True if this node is a payoff, i.e., it has no successors
     */
    public boolean isPayoff() {
        return successors.isEmpty() && (defaultSuccessor == null);
    }

    /**
     * Adds the costs of an element of the model, including those of its own strategies (see {@link #updateCosts(IModelItemWithStrategyWrapper, Set, Set, boolean)}).
     * @param osdiElement The element of the model
     * @param categories The categories of the costs and strategies to include
     * @param includeUncategorized If true, the own costs without category are also included
     * @throws MalformedOSDiModelException If the ontology is malformed
     * @throws UnsupportedOSDiFeatureException If the strategies use features not supported by decision trees
     */
    public void updateCosts(IModelItemWithStrategyWrapper osdiElement, Set<ResourceUsageSKOSCategory> categories, boolean includeUncategorized) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        updateCosts(osdiElement, osdiElement.getStrategies(), categories, includeUncategorized);
    }

    /**
     * Adds the costs of an element of the model: its own costs of the given categories (and, optionally, those without category), plus the costs of
     * the given strategies of such categories (see {@link StrategyCosts}). Both are added: a modeler may define an overall estimated cost for an
     * element and a detailed cost through its strategies, and include in each model the one that suits it, since only the items included in the
     * model are read.
     * @param osdiElement The element of the model
     * @param strategies The strategies of the element in the current arm of the tree (they may have been replaced by an intervention)
     * @param categories The categories of the costs and strategies to include
     * @param includeUncategorized If true, the own costs without category are also included
     * @throws MalformedOSDiModelException If the ontology is malformed
     * @throws UnsupportedOSDiFeatureException If the strategies use features not supported by decision trees
     */
    public void updateCosts(IModelItemWithStrategyWrapper osdiElement, Set<StrategyWrapper> strategies, Set<ResourceUsageSKOSCategory> categories, boolean includeUncategorized) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Set<ParameterWrapper> costParameters = new TreeSet<>();
        final Set<StrategyWrapper> selectedStrategies = new TreeSet<>();
        for (ResourceUsageSKOSCategory category : categories) {
            costParameters.addAll(osdiElement.getOSDiWrapper().<ParameterWrapper>filterModelItemsWithSKOSCategory(osdiElement.getCosts(), category));
            selectedStrategies.addAll(osdiElement.getOSDiWrapper().<StrategyWrapper>filterModelItemsWithSKOSCategory(strategies, category));
        }
        if (includeUncategorized) {
            costParameters.addAll(osdiElement.getOSDiWrapper().filterModelItemsWithoutAnySKOSCategory(osdiElement.getCosts()));
        }
        addCostParameters(costParameters);
        for (StrategyWrapper strategy : selectedStrategies) {
            // The payoffs of a strategy with outcomes depend on its outcomes: they are added when the outcomes are expanded (see StrategyOutcomeExpander)
            if (strategy.hasOutcomes()) {
                if (!outcomeStrategies.contains(strategy)) {
                    outcomeStrategies.add(strategy);
                }
                continue;
            }
            final ArrayList<ParameterWrapper> strategyCosts = new ArrayList<>();
            StrategyCosts.collect(strategy, strategyCosts, guidelineCosts);
            addCostParameters(strategyCosts);
        }
    }

    /**
     * Adds the utilities of an element of the model, including those of its own strategies (see {@link #updateUtility(IModelItemWithStrategyWrapper, Set, Set)}).
     * @param osdiElement The element of the model
     * @param categories The categories of the strategies to include
     * @throws MalformedOSDiModelException If the ontology is malformed
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered
     */
    public void updateUtility(IModelItemWithStrategyWrapper osdiElement, Set<ResourceUsageSKOSCategory> categories) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        updateUtility(osdiElement, osdiElement.getStrategies(), categories);
    }

    /**
     * Adds the utilities of an element of the model: its own utilities (which do not use categories), plus the utilities of the given strategies
     * of the given categories. Both are added, as the costs (see {@link #updateCosts(IModelItemWithStrategyWrapper, Set, Set, boolean)}).
     * @param osdiElement The element of the model
     * @param strategies The strategies of the element in the current arm of the tree (they may have been replaced by an intervention)
     * @param categories The categories of the strategies to include
     * @throws MalformedOSDiModelException If the ontology is malformed
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered
     */
    public void updateUtility(IModelItemWithStrategyWrapper osdiElement, Set<StrategyWrapper> strategies, Set<ResourceUsageSKOSCategory> categories) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        if (osdiElement.getUtilities() != null) {
            addDisutilityParameters(osdiElement.getUtilities());
        }
        final Set<StrategyWrapper> selectedStrategies = new TreeSet<>();
        for (ResourceUsageSKOSCategory category : categories) {
            selectedStrategies.addAll(osdiElement.getOSDiWrapper().<StrategyWrapper>filterModelItemsWithSKOSCategory(strategies, category));
        }
        for (StrategyWrapper strategy : selectedStrategies) {
            // The utilities of a strategy with outcomes are added when the outcomes are expanded (see StrategyOutcomeExpander)
            if (!strategy.hasOutcomes()) {
                addDisutilityParameters(strategy.getUtilities());
            }
        }
    }

    public void updateEffects(IModelItemWithEffectWrapper osdiElement) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Set<EffectWrapper> effectWrappers = osdiElement.getEffects();
        addEffects(effectWrappers);
        // Effects may also define cost parameters
        for (EffectWrapper effectWrapper : effectWrappers) {
            final Optional<ParameterWrapper> effectCost = effectWrapper.getCost();
            if (effectCost.isPresent()) {
                addCostParameter(effectCost.get());
            }
        }
    }

    public void updateEffects(IModelItemWithEffectWrapper osdiElement, EffectType effectType) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Set<EffectWrapper> effectWrappers = osdiElement.getEffects();
        // Effects may also define cost parameters
        for (EffectWrapper effectWrapper : effectWrappers) {
            if (effectWrapper.getEffectType().equals(effectType)) {
                addEffect(effectWrapper);
                final Optional<ParameterWrapper> effectCost = effectWrapper.getCost();
                if (effectCost.isPresent()) {
                    addCostParameter(effectCost.get());
                }
            }
        }
    }
}
