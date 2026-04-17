package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public class ProgressionCombinationRuleWrapper extends BaseModelItemWrapper {
    private final static OSDiLogger log = OSDiLogger.getLogger(ProgressionCombinationRuleWrapper.class);

    private final Set<ClinicalProgressionWrapper> affectedProgressions = new TreeSet<>();
    private ProgressionCombinationRuleType type = null;
    private ClinicalProgressionType combinedElementsType = null;
    private Optional<Boolean> hasNullProgression = Optional.empty();
    /**
     * Indicates whether this progression combination rule wrapper is synthetic (i.e., created programmatically and not directly based on an ontology individual) or not. Synthetic progression combination rules are created with all necessary information and do not depend on any other ontology item, so they can be set as ready immediately.
     */
    private final boolean synthetic;

    public ProgressionCombinationRuleWrapper(ModelWrapper modelWrapper, IRI individualIRI) {
        super(modelWrapper, individualIRI);
        this.synthetic = false;
    }

    /**
     * Creates a synthetic progression combination rule wrapper with the given type and IRI. 
     * Synthetic progression combination rules are created with all necessary information and 
     * do not depend on any other ontology item, so they can be set as ready immediately.
     * @param modelWrapper
     * @param individualIRI
     * @param type
     */
    public ProgressionCombinationRuleWrapper(ModelWrapper modelWrapper, IRI individualIRI, ProgressionCombinationRuleType type) {
        super(modelWrapper, individualIRI);
        setCombinationType(type);
        // Synthetic progression combination rules are created with all necessary information and do not depend on any other ontology item, so they can be set as ready immediately.
        setStatus(Status.READY);
        this.synthetic = true;
    }

	@Override
	public boolean isSynthetic() {
		return synthetic;
	}
	
    @Override
    public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final ModelWrapper modelWrapper = getModelWrapper();
        final IRI individualIRI = getIndividualIRI();
		for (ProgressionCombinationRuleType pcType : ProgressionCombinationRuleType.values()) {
			if (getOSDiWrapper().isInstanceOf(individualIRI, pcType.getClazz())) {
				this.type = pcType;
				break;
			}
		}
		if (type == null && getOSDiWrapper().isInstanceOf(individualIRI, OSDiClass.PROGRESSION_COMBINATION_RULE)) {
			log.warn("Progression combination rule " + individualIRI + " is not a subclass of any known combination rule type. Defaulting to COEXISTENT.");
			this.type = ProgressionCombinationRuleType.COEXISTENT;
		}
        this.hasNullProgression = getOSDiWrapper().getBooleanValue(individualIRI, OSDiDataProperty.HAS_NULL_PROGRESSION);
        final Set<ClinicalProgressionWrapper> progressions = modelWrapper.getWrappersForPropertyAs(individualIRI, OSDiObjectProperty.AFFECTS_PROGRESSION_ELEMENT, ClinicalProgressionWrapper.class);
        if (progressions.isEmpty()) {
            throw new MalformedOSDiModelException("The combination rule " + individualIRI + " does not affect any progression element.");
        } else {
            if (progressions.size() == 1)
                log.warn("The combination rule " + individualIRI + " only affects one progression element. It should be ignored");
            combinedElementsType = null;
            for (ClinicalProgressionWrapper progression : progressions) {
                ClinicalProgressionType progressionType = ClinicalProgressionType.fromIRI(progression.getIndividualIRI(), getOSDiWrapper());
                if (combinedElementsType == null) {
                    combinedElementsType = progressionType;
                } else if (combinedElementsType != progressionType) {
                    throw new MalformedOSDiModelException("The combination rule " + individualIRI + " affects progressions of different types.");
                }
                addProgression(progression);
            }
        }
    }

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }

    public ProgressionCombinationRuleType getCombinationType() {
        return type;
    }
 
    public Optional<Boolean> hasNullProgression() {
        return hasNullProgression;
    }

    protected void setHasNullProgression(boolean hasNullProgression) {
        this.hasNullProgression = Optional.of(hasNullProgression);
    }
    
    public ClinicalProgressionType getCombinedElementsType() {
        return combinedElementsType;
    }
    protected void setCombinationType(ProgressionCombinationRuleType type) {
        this.type = type;
    }

    public Set<ClinicalProgressionWrapper> getProgressions() {
        return new TreeSet<>(affectedProgressions);
    }
    
    public void addProgression(ClinicalProgressionWrapper progression) {
        this.affectedProgressions.add(progression);
    }

}
