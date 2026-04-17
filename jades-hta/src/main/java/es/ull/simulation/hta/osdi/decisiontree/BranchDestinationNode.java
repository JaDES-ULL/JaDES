package es.ull.simulation.hta.osdi.decisiontree;

import java.util.ArrayList;
import java.util.Collection;
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

    public void updateCosts(IModelItemWithStrategyWrapper osdiElement, Set<ResourceUsageSKOSCategory> categories, boolean includeUncategorized) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Set<ParameterWrapper> costParameters = new TreeSet<>();
        final Set<StrategyWrapper> strategies = new TreeSet<>();
        for (ResourceUsageSKOSCategory category : categories) {
            costParameters.addAll(osdiElement.getOSDiWrapper().<ParameterWrapper>filterModelItemsWithSKOSCategory(osdiElement.getCosts(), category));
            strategies.addAll(osdiElement.getOSDiWrapper().<StrategyWrapper>filterModelItemsWithSKOSCategory(osdiElement.getStrategies(), category));
        }
        if (includeUncategorized) {
            costParameters.addAll(osdiElement.getOSDiWrapper().filterModelItemsWithoutAnySKOSCategory(osdiElement.getCosts()));
        }
        if (costParameters.isEmpty()) {
            for (StrategyWrapper strategy : strategies) {
                // All the costs from a specific strategy are supposed to belong to the expected categories
                final Set<ParameterWrapper> strategyCostParameters = strategy.getCosts();
                if (!strategyCostParameters.isEmpty()) {
                    addCostParameters(strategyCostParameters);
                }
            }
        } else {
            addCostParameters(costParameters);
            if (!strategies.isEmpty()) {
                log.warn("Strategies defined in the disease (" + strategies + ") will be ignored in the model, since costs were also defined: " + costParameters);
            }
        }
    }

    public void updateUtility(IModelItemWithStrategyWrapper osdiElement, Set<ResourceUsageSKOSCategory> categories) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Set<ParameterWrapper> utilityParameters = new TreeSet<>();
        final Set<StrategyWrapper> strategies = new TreeSet<>();
        // Utility parameters do not use categories, so they are added regardless of the categories defined for the disease. 
        if (osdiElement.getUtilities() != null) {
            utilityParameters.addAll(osdiElement.getUtilities());
        }
        for (ResourceUsageSKOSCategory category : categories) {
            strategies.addAll(osdiElement.getOSDiWrapper().<StrategyWrapper>filterModelItemsWithSKOSCategory(osdiElement.getStrategies(), category));
        }
        if (utilityParameters.isEmpty()) {
            for (StrategyWrapper strategy : strategies) {
                final Set<ParameterWrapper> strategyUtilityParameters = strategy.getUtilities();
                if (!strategyUtilityParameters.isEmpty()) {
                    addDisutilityParameters(strategyUtilityParameters);
                }
            }
        } else {
            addDisutilityParameters(utilityParameters);
            if (!strategies.isEmpty()) {
                log.warn("Strategies defined in the disease (" + strategies + ") will be ignored in the model, since at least one utility was also defined: " + utilityParameters);
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
