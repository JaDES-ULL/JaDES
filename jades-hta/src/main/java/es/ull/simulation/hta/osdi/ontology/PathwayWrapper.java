package es.ull.simulation.hta.osdi.ontology;

import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public class PathwayWrapper extends BaseModelItemWrapper implements IModelItemWithCostWrapper, IModelItemWithUtilityWrapper {
    /**
     * The set of clinical progressions that are required by this pathway.
     */
    private final Set<ClinicalProgressionWrapper> requiredClinicalProgressions = new TreeSet<>();
    /**
     * The set of interventions that are required by this pathway.
     */
    private final Set<InterventionWrapper> requiredInterventions = new TreeSet<>();
    /**
     * The set of parameters that characterize the risks associated with this pathway.
     */
    private final Set<ParameterWrapper> riskParameters = new TreeSet<>();

    public PathwayWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        super(modelWrapper, individualIRI);
    }

    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final Set<IModelItemWrapper> requiredItems = getModelWrapper().getWrappersForProperty(getIndividualIRI(), OSDiObjectProperty.REQUIRES);
        for (IModelItemWrapper requiredItem : requiredItems) {
            if (requiredItem instanceof ClinicalProgressionWrapper) {
                requiredClinicalProgressions.add((ClinicalProgressionWrapper) requiredItem);
            } else if (requiredItem instanceof InterventionWrapper) {
                requiredInterventions.add((InterventionWrapper) requiredItem);
            } else {
                throw new MalformedOSDiModelException(OSDiClass.PATHWAY, getShortName(), OSDiObjectProperty.REQUIRES, "Only clinical progressions and interventions can be required by a pathway.");
            }
        }
        riskParameters.addAll(getModelWrapper().getWrappersForPropertyAs(getIndividualIRI(), OSDiObjectProperty.HAS_PARAMETER, ParameterWrapper.class));
        if (riskParameters.isEmpty() &&  requiredItems.isEmpty()) {
            throw new MalformedOSDiModelException("Pathway " + getShortName() + " requires at least one risk characterization parameter or one required item.");
        }
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }

    /**
     * Returns the wrappers of the clinical progressions that are required by this pathway.
     * @return The wrappers of the clinical progressions that are required by this pathway.
     */
    public Set<ClinicalProgressionWrapper> getRequiredClinicalProgressions() {
        return requiredClinicalProgressions;
    }

    /**
     * Returns the wrappers of the interventions that are required by this pathway.
     * @return The wrappers of the interventions that are required by this pathway.
     */
    public Set<InterventionWrapper> getRequiredInterventions() {
        return requiredInterventions;
    }

    /**
     * Returns the wrappers of the parameters that characterize the risks associated with this pathway.
     * @return The wrappers of the parameters that characterize the risks associated with this pathway.
     */    
    public Set<ParameterWrapper> getRiskParameters() {
        return riskParameters;
    }
}
