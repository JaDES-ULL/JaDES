package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.BranchDestinationNode;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.decisiontree.SingleDiseaseModel;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;

/**
 * A simple intervention for a single disease and population model.
 */
public class SimpleInterventionGenerator extends InterventionGenerator {

    /**
     * Constructs a new GenericInterventionTreePart with the specified context and part name.
     *
     * @param parentTreeModel The parent tree model associated with this factory.
     * @throws MalformedSimulationModelException If there is an issue with the simulation model.
     * @throws UnsupportedOSDiFeatureException   If an unsupported OSDi feature is encountered.
     * @throws MalformedOSDiModelException       If the OSDi model is malformed.
     */
    public SimpleInterventionGenerator(Model parentTreeModel, InterventionWrapper interventionWrapper) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        super(parentTreeModel, interventionWrapper);
    }

    /**
     * Returns the strategy replacements of an intervention (decision D2 of docs/osdi-1.3-diseno.md): for each health condition affected by a
     * strategy replacement effect of the intervention, the strategies that replace its own in the arm of the intervention. Interventions only
     * declare strategies through such effects.
     * @param intervention The intervention
     * @return The strategies that replace those of each health condition, indexed by the IRI of the health condition
     * @throws MalformedOSDiModelException If the intervention declares strategies directly (hasStrategy), or two strategy replacements affect
     * the same health condition
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered
     */
    public static Map<IRI, Set<StrategyWrapper>> getStrategyReplacements(InterventionWrapper intervention) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
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
     * Generates the sub-tree of the intervention: the sub-tree of the disease (see {@link DiseaseSubTreeGenerator}), whose root node also gets the
     * payoffs and effects of the intervention. The intervention applies from the start of the tree, i.e., from the appearance of its target (D1).
     * If the target is an acute manifestation, the annual costs and utilities of the intervention are ignored with a warning, as those of the
     * acute manifestation itself: the tree represents a single episode, so an annual payoff would be charged until death because of one episode.
     */
    @Override
    public BranchDestinationNode generate(String interventionName) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final BranchDestinationNode interventionNode;
        boolean hasAcuteTarget = false;
        if (getParentTreeModel() instanceof SingleDiseaseModel) {
            final DiseaseSubTreeGenerator diseaseGenerator = ((SingleDiseaseModel)getParentTreeModel()).getDiseaseGenerator();
            interventionNode = diseaseGenerator.generate(interventionName, getStrategyReplacements(getInterventionWrapper()));
            hasAcuteTarget = diseaseGenerator.hasAcuteTarget();
        }
        else {
            interventionNode = new BranchDestinationNode(getParentTreeModel(), interventionName);
        }
        // The payoffs of the intervention are collected apart, so that they can be filtered without affecting those of the disease
        final BranchDestinationNode interventionPayoffs = new BranchDestinationNode(getParentTreeModel(), interventionName);
        interventionPayoffs.updateCosts(this.getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT), true);
        interventionPayoffs.updateUtility(this.getInterventionWrapper(), Set.of(ResourceUsageSKOSCategory.FOLLOW_UP, ResourceUsageSKOSCategory.TREATMENT));
        interventionPayoffs.updateEffects(this.getInterventionWrapper());
        if (hasAcuteTarget) {
            final String owner = "The intervention " + getInterventionWrapper().getShortName() + ", whose target is an acute manifestation,";
            DiseaseSubTreeGenerator.removeAnnualPayoffs(owner, interventionPayoffs.getCostParameters());
            DiseaseSubTreeGenerator.removeAnnualPayoffs(owner, interventionPayoffs.getDisutilityParameters());
        }
        interventionNode.addCostParameters(interventionPayoffs.getCostParameters());
        interventionNode.addDisutilityParameters(interventionPayoffs.getDisutilityParameters());
        interventionNode.addEffects(interventionPayoffs.getEffects());
        return interventionNode;
    }
}
