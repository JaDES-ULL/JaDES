package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.parameters.Imports;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.AttributeWrapper;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.ExpressionLanguageType;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.OSDiDataItemType;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.CalculatedParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.ParameterNatureData;
import es.ull.simulation.ontology.OWLOntologyWrapper.InstanceCheckMode;

public interface AbstractModelFactory {
    /**
     * Returns a suitability score (0-100) for the given wrapper.
     * @param wrap The wrapper to evaluate.
     * @return A suitability score (0-100) with the reasons for the score.
     */
    FactorySuitabilityScore getSuitability(ExperimentWrapper experiment) throws MalformedSimulationModelException;

    /**
     * Returns a DecisionTreeModel created from the given wrapper.
     * @param experiment The experiment wrapper containing the ontology and experiment details.
     * @return A DecisionTreeModel instance.
     * @throws MalformedSimulationModelException If the simulation model is malformed.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     * @throws UnsupportedOSDiFeatureException If the model contains unsupported features.
     */
    Model createModelGenerator(ExperimentWrapper experiment) throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException;
    
    /**
     * Returns a probability parameter that can be used in the decision tree by processing the original parameter. If the original parameter
     * is already a probability or proportion returns the parameter unmodified.
     * @throws UnsupportedOSDiFeatureException 
     * @throws MalformedOSDiModelException 
     */
    static ParameterWrapper getProbabilityParameterFromGenericParameter(ParameterWrapper originalParam) throws UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        switch (originalParam.getDataItemType()) {
            case OSDiDataItemType.DI_PROBABILITY:
            case OSDiDataItemType.DI_PROPORTION:
            case OSDiDataItemType.DI_INCIDENCE:
            case OSDiDataItemType.DI_PREVALENCE:
            case OSDiDataItemType.DI_BIRTH_PREVALENCE:
            case OSDiDataItemType.DI_SENSITIVITY:
            case OSDiDataItemType.DI_SPECIFICITY:
                return originalParam;
            case OSDiDataItemType.DI_TIME_TO_EVENT:
                final ArrayList<ParameterWrapper> dependantParameters = new ArrayList<>();
                dependantParameters.add(originalParam);
                ParameterNatureData paramData = new CalculatedParameterData("1.0 - EXP(-1.0 / " + originalParam.getShortName() + ")", 
                    dependantParameters, new ArrayList<AttributeWrapper>(), ExpressionLanguageType.EXCEL);
                ParameterWrapper.SyntheticBuilder builder = new ParameterWrapper.SyntheticBuilder(originalParam.getModelWrapper(), 
                    "P_" + originalParam.getShortName(), paramData)
                    .withSource("Computed from " + originalParam.getShortName())
                    .withDescription("Probability derived from time to event " + originalParam.getShortName()); 
                return builder.build();
            default:
                throw new UnsupportedOSDiFeatureException("The data type (" + originalParam.getDataItemType() + ") for the parameter " + originalParam.getShortName() + " cannot be used as a probability in the decision tree.");
        }
    }

    /**
     * Returns the first intervention marked as "comparator" in the ontology.
     * @param wrap The OSDi wrapper containing the interventions.
     * @return The first comparator intervention, or null if not found.
     */
    static InterventionWrapper findFirstComparatorIntervention(ModelWrapper modelWrapper) {
        for (InterventionWrapper intervention : modelWrapper.getInterventionIndividuals()) {
            if (!intervention.isAssessedIntervention().orElse(false)) {
                return intervention;
            }
        }
        return null;
    }

    /**
     * Returns the first intervention marked as "assessed" in the ontology.
     * @param wrap The OSDi wrapper containing the interventions.
     * @return The first assessed intervention, or null if not found.
     */
    static InterventionWrapper findFirstAssessedIntervention(ModelWrapper modelWrapper) {
        for (InterventionWrapper intervention : modelWrapper.getInterventionIndividuals()) {
            if (intervention.isAssessedIntervention().orElse(false)) {
                return intervention;
            }
        }
        return null;
    }

    /**
     * Returns the class of a given intervention IRI.
     * The intervention class is determined by checking its superclasses in the ontology.
     * @param interventionIRI The IRI of the intervention.
     * @return The class of the intervention.
     * @throws MalformedOSDiModelException If the intervention is not a subclass of Intervention.
     */
    static OSDiClass getInterventionClass(OSDiWrapper wrap, IRI interventionIRI) throws MalformedOSDiModelException {
        final Set<IRI> superclasses = wrap.getTypes(interventionIRI, Imports.INCLUDED, InstanceCheckMode.ASSERTED_ALL);
        if (!superclasses.contains(wrap.toIRI(OSDiClass.INTERVENTION)))
            throw new MalformedOSDiModelException("The intervention " + interventionIRI + " is not a subclass of Intervention");
        if (superclasses.contains(wrap.toIRI(OSDiClass.SCREENING_INTERVENTION)))
            return OSDiClass.SCREENING_INTERVENTION;
        else if (superclasses.contains(wrap.toIRI(OSDiClass.DIAGNOSIS_INTERVENTION)))
            return OSDiClass.DIAGNOSIS_INTERVENTION;
        else if (superclasses.contains(wrap.toIRI(OSDiClass.DETECTION_INTERVENTION)))
            return OSDiClass.DETECTION_INTERVENTION;
        else if (superclasses.contains(wrap.toIRI(OSDiClass.THERAPEUTIC_INTERVENTION)))
            return OSDiClass.THERAPEUTIC_INTERVENTION;
        return OSDiClass.INTERVENTION;
    }
}
