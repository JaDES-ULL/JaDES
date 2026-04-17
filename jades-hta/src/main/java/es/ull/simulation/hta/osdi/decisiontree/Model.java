package es.ull.simulation.hta.osdi.decisiontree;

import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;
import es.ull.simulation.hta.params.ParameterGroup;

/**
 * Interface representing a decision tree model defined in OSDi.
 * @author Iván Castilla Rodríguez
 */
public interface Model {
    /**
     * The default discount rate to be used when no other is specified in the model.
     */
    public final static double DEFAULT_DISCOUNT_RATE = 0.03;
    /**
     * Types of payoffs in the decision tree.
     */
    enum PayoffType {
        COST("C_"),
        LIFE_EXPECTANCY("LE_"),
        QALY("QALE_");
    
        private final String prefix;
    
        private PayoffType(String prefix) {
            this.prefix = prefix;
        }
    
        public String getPrefix() {
            return prefix;
        }
    }

    /**
     * Returns the ontology wrapper.
     * @return The ontology wrapper.
     */

    public OSDiWrapper getOSDiWrapper();

    /**
     * Returns the experiment wrapper.
     * @return The experiment wrapper.
     */
    public ExperimentWrapper getExperimentWrapper();

    public default ModelWrapper getModelWrapper() {
        return getExperimentWrapper().getModelWrapper();
    }
    
    /**
     * Generates the tree according to the knowledge contained in the ontology. Returns the root node of the decision tree.
     * @return The root node of the decision tree.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     * @throws MalformedSimulationModelException If the simulation model is malformed.
     * @throws UnsupportedOSDiFeatureException If the model contains unsupported features, such as multiple screening interventions.
     */
    public ChoiceNode getTree() throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException;

    /**
     * Returns the interventions used in the decision process. 
     * @return The interventions.
     */
    public ArrayList<InterventionWrapper> getInterventions();

    /**
     * Returns the intervention corresponding to a specific intervention IRI.
     * @param interventionIRI The IRI of the intervention.
     * @return The corresponding intervention, or null if not found.
     */
    public InterventionWrapper getIntervention(IRI interventionIRI);

    /**
     * Obtains the parameter wrappers for a specific type of parameters in the decision tree.
     * @param type The type to which the parameters belong, used for categorization in the decision tree.
     * @return A collection of parameter wrappers for the specified type.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     */
    public Set<ParameterWrapper> getParameters(ParameterGroup type)  throws MalformedOSDiModelException;

    /**
     * Obtains the parameter wrappers for all types of parameters in the decision tree.
     * @return A collection of parameter wrappers for all types.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     */
    public Set<ParameterWrapper> getParameters() throws MalformedOSDiModelException;

    /**
     * Returns the disutility combination method used in this model.
     *
     * @return The disutility combination method.
     */
    public DisutilityCombinationMethod getDisutilityCombinationMethod();

    /**
     * Returns the study year defined for this model.
     * @return The study year.
     */
    public default int getStudyYear() {
        return getExperimentWrapper().getStudyYear();
    }

    /**
     * Returns the discount rate for costs defined in the model.
     * @return The discount rate for costs, or NaN if not defined.
         */
    public default double getDiscountRateForCosts() {
        return getExperimentWrapper().getDefaultDiscountRateForCosts();
    }

    /**
     * Returns the discount rate for effects defined in the model.
     * @return The discount rate for effects, or NaN if not defined.
     */
    public default double getDiscountRateForEffects() {
        return getExperimentWrapper().getDefaultDiscountRateForEffects();
    }

    /**
     * Returns the depth of the decision tree.
     * The depth is defined as the maximum number of nodes from the root to a leaf node.
     *
     * @return The depth of the decision tree.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     * @throws MalformedSimulationModelException If the simulation model is malformed.
     * @throws UnsupportedOSDiFeatureException If the model contains unsupported features.
     */
    public default int getDepth() throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException {
        final ChoiceNode root = getTree();
        if (root == null) {
            return 0;
        }
        int maxDepth = 1;
        for (BranchDestinationNode successor : root.getSuccessors()) {
            if (!successor.isPayoff()) {
                maxDepth = Math.max(maxDepth, 1 + getDepth(successor));
            }            
        }
        return maxDepth;
    }

    private int getDepth(BranchDestinationNode node) {
        if (node == null) {
            return 0;
        }
        int maxDepth = 1;
        for (BranchDestinationNode successor : node.getSuccessors()) {
            if (!successor.isPayoff()) {
                maxDepth = Math.max(maxDepth, 1 + getDepth(successor));
            }
        }
        BranchDestinationNode successor = node.getDefaultSuccessor();
        if (successor != null) {
            if (!successor.isPayoff()) {
                maxDepth = Math.max(maxDepth, 1 + getDepth(successor));
            }
        }
        return maxDepth;
    }

}
