package es.ull.simulation.hta.osdi.ontology;

import java.time.Year;
import java.util.Optional;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;
import es.ull.simulation.ontology.OWLOntologyWrapper.InstanceCheckMode;

public class ExperimentWrapper implements IIndividualWrapper {
	/** 
	 * Default study year (current year) 
	 */
	private final static int DEF_STUDY_YEAR = Year.now().getValue();
    /**
     * Default time horizon (100 years)
     */
    private final static int DEF_TIME_HORIZON = 100;
	/**
	 * Default discount rate (0.00)
	 */
	private final static double DEF_DEFAULT_DISCOUNT_RATE = 0.00;
	/**
	 * Default number of PSA runs (0)
	 */
	private final static int DEF_N_PSA_RUNS = 0;
	/**
	 * Default number of simulated individuals (1)
	 */
	private final static int DEF_N_SIMULATED_INDIVIDUALS = 1;
	/**
	 * A logger for the ExperimentWrapper class
	 */
	private final static OSDiLogger log = OSDiLogger.getLogger(ExperimentWrapper.class);
    /**
     * The OSDi wrapper that contains the ontology
     */
    private final OSDiWrapper wrap;
    /**
     * The IRI of the experiment individual in the ontology
     */
    private final IRI individualIRI;
	/**
	 * The IRI of the instance that defines the working model in the ontology. This instance includes a collection of @link{OSDiObjectProperties#INCLUDES_MODEL_ITEM} 
	 * relationships that define the items that are part of the model, such as parameters, interventions, stages, etc.
	 */
	private final IRI workingModelIRI;
	/** 
	 * The combination method used to combine different disutilities 
	 */
    private DisutilityCombinationMethod duCombinationMethod;
	/**
	 * The type of model that is being defined in the working model. This is used to determine how to generate the model.
	 */
	private final ModelType modelType;
	/** 
	 * The year of the study (for cost updating). 
	 */
	private int studyYear;
    /**
     * The time horizon defined for the working model
     */
    private int timeHorizon;
	/**
	 * The number of PSA runs defined in the experiment
	 */
	private int nPSARuns;
	/**
	 * The number of simulated individuals defined in the experiment
	 */
	private int nSimulatedIndividuals;
	/** 
	 * The model wrapper that contains the working model
	 */
	private final ModelWrapper modelWrapper;
	/**
	 * The default discount rate for costs defined in the experiment
	 */
	private double defaultDiscountRateForCosts;
	/**
	 * The default discount rate for effects defined in the experiment
	 */
	private double defaultDiscountRateForEffects;

    public ExperimentWrapper(OSDiWrapper wrap, IRI individualIRI) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        this.wrap = wrap;
		if (!wrap.isInstanceOf(individualIRI, OSDiClass.EXPERIMENT)) {
			throw new MalformedOSDiModelException("The individual '" + individualIRI + "' is not an instance of the class " + OSDiClass.EXPERIMENT);
		}
        this.individualIRI = individualIRI;
		Optional<IRI> workingModelIRIOptional = wrap.getValue(this.individualIRI, OSDiObjectProperty.USES_MODEL);
		if (!workingModelIRIOptional.isPresent()) {
			throw new MalformedOSDiModelException("The experiment individual '" + this.individualIRI + "' does not define a working model using the property " + OSDiObjectProperty.USES_MODEL);
		}
		this.workingModelIRI = workingModelIRIOptional.get();
		this.modelType = ModelType.fromIRI(this.workingModelIRI, wrap);
		if (modelType == null) {
			throw new MalformedOSDiModelException("The working model instance '" + this.workingModelIRI + "' does not exist or has not a valid model type. Current superclasses: " + wrap.getTypes(this.workingModelIRI, Imports.INCLUDED, InstanceCheckMode.INFERRED_ALL).stream()
                          .collect(Collectors.joining(", ")));
		}

		// Initializes the study year
		this.studyYear = wrap.getIntegerValue(this.individualIRI, OSDiDataProperty.HAS_YEAR).orElse(DEF_STUDY_YEAR);
        // Initializes the time horizon
		this.timeHorizon = wrap.getIntegerValue(this.individualIRI, OSDiDataProperty.HAS_TIME_HORIZON).orElse(DEF_TIME_HORIZON);
		// Initializes the default discount rates
		this.defaultDiscountRateForCosts = wrap.getDoubleValue(this.individualIRI, OSDiDataProperty.HAS_DISCOUNT_FOR_COSTS).orElse(DEF_DEFAULT_DISCOUNT_RATE);
		this.defaultDiscountRateForEffects = wrap.getDoubleValue(this.individualIRI, OSDiDataProperty.HAS_DISCOUNT_FOR_EFFECTS).orElse(DEF_DEFAULT_DISCOUNT_RATE);
		// Initializes the number of PSA runs
		this.nPSARuns = wrap.getIntegerValue(this.individualIRI, OSDiDataProperty.HAS_NUMBER_OF_PSA_RUNS).orElse(DEF_N_PSA_RUNS);
		// Initializes the number of simulated individuals
		this.nSimulatedIndividuals = wrap.getIntegerValue(this.individualIRI, OSDiDataProperty.HAS_NUMBER_OF_SIMULATED_INDIVIDUALS).orElse(DEF_N_SIMULATED_INDIVIDUALS);

        // Initializes the combination method for disutilities
		Optional<OWLLiteral> duMethod = wrap.getValue(this.individualIRI, OSDiDataProperty.HAS_DISUTILITY_COMBINATION_METHOD);
        DisutilityCombinationMethod duCombinationMethod;
		if (duMethod.isEmpty()) {
			duCombinationMethod = DisutilityCombinationMethod.ADD;
			log.warn(this.workingModelIRI, OSDiDataProperty.HAS_DISUTILITY_COMBINATION_METHOD, "Disutility combination method not defined. Using default: " + DisutilityCombinationMethod.ADD);
		}
		else {
			try {
				duCombinationMethod = DisutilityCombinationMethod.valueOf(duMethod.get().getLiteral());
			}
			catch(IllegalArgumentException ex) {
				log.warn(this.workingModelIRI, OSDiDataProperty.HAS_DISUTILITY_COMBINATION_METHOD, "Disutility combination method not valid. \"" + duMethod + "\" not found. Using default: " + DisutilityCombinationMethod.ADD);
				duCombinationMethod = DisutilityCombinationMethod.ADD;
			}
		}
        this.duCombinationMethod = duCombinationMethod;
		this.modelWrapper = new ModelWrapper(this, this.workingModelIRI);
    }

    @Override
    public OSDiWrapper getOSDiWrapper() {
        return wrap;
    }

	@Override
    public IRI getIndividualIRI() {
        return individualIRI;
    }

	/**
	 * Returns the study year defined for the working model
	 * @return The study year
	 */
	public int getStudyYear() {
		return studyYear;
	}

	/**
	 * Sets the study year defined for the working model
	 * @param studyYear The study year to set
	 */	
	public void setStudyYear(int studyYear) {
		this.studyYear = studyYear;	
	}

    /**
     * Returns the time horizon defined for the working model
     * @return The time horizon
     */
    public int getTimeHorizon() {
        return timeHorizon;
    }
    
	/**
	 * Sets the time horizon defined for the working model
	 * @param timeHorizon The time horizon to set
	 */	
	public void setTimeHorizon(int timeHorizon) {
		this.timeHorizon = timeHorizon;	
	}

	/**
	 * Returns the number of PSA runs defined in the experiment
	 * @return The number of PSA runs
	 */
	public int getNPSARuns() {
		return nPSARuns;
	}

	/**
	 * Sets the number of PSA runs defined in the experiment
	 * @param nPSARuns The number of PSA runs to set
	 */
	public void setNPSARuns(int nPSARuns) {
		this.nPSARuns = nPSARuns;
	}

	/**
	 * Returns the number of simulated individuals defined in the experiment
	 * @return The number of simulated individuals
	 */
	public int getNSimulatedIndividuals() {
		return nSimulatedIndividuals;
	}

	/**
	 * Sets the number of simulated individuals defined in the experiment
	 * @param nSimulatedIndividuals The number of simulated individuals to set
	 */
	public void setNSimulatedIndividuals(int nSimulatedIndividuals) {
		this.nSimulatedIndividuals = nSimulatedIndividuals;
	}

	/**
	 * Returns the combination method used to combine different disutilities
	 * @return The disutility combination method
	 */
	public DisutilityCombinationMethod getDisutilityCombinationMethod() {
		return duCombinationMethod;
	}

	/**
	 * Sets the combination method used to combine different disutilities
	 * @param duCombinationMethod The disutility combination method to set
	 */
	public void setDisutilityCombinationMethod(DisutilityCombinationMethod duCombinationMethod) {
		this.duCombinationMethod = duCombinationMethod;
	}

	/**
	 * Gets the IRI of the working model used in the experiment
	 * @return the IRI of the working model used in the experiment
	 */
	public IRI getWorkingModelIRI() {
		return workingModelIRI;
	}
	
	/**
	 * Returns the model wrapper that contains the working model
	 * @return the model wrapper that contains the working model
	 */
	public ModelWrapper getModelWrapper() {
		return modelWrapper;
	}

	/**
	 * Returns the model type of the working model
	 * @return the model type of the working model
	 */
	public ModelType getModelType() {
		return modelType;
	}

	/**
	 * Returns the default discount rate for costs defined in the experiment
	 * @return the default discount rate for costs defined in the experiment
	 */
	public double getDefaultDiscountRateForCosts() {
		return defaultDiscountRateForCosts;
	}

	/**
	 * Sets the default discount rate for costs defined in the experiment
	 * @param defaultDiscountRateForCosts The default discount rate for costs to set
	 */
	public void setDefaultDiscountRateForCosts(double defaultDiscountRateForCosts) {
		this.defaultDiscountRateForCosts = defaultDiscountRateForCosts;
	}

	/**
	 * Returns the default discount rate for effects defined in the experiment
	 * @return the default discount rate for effects defined in the experiment
	 */
	public double getDefaultDiscountRateForEffects() {
		return defaultDiscountRateForEffects;
	}

	/**
	 * Sets the default discount rate for effects defined in the experiment
	 * @param defaultDiscountRateForEffects The default discount rate for effects to set
	 */
	public void setDefaultDiscountRateForEffects(double defaultDiscountRateForEffects) {
		this.defaultDiscountRateForEffects = defaultDiscountRateForEffects;
	}
	
	public static void create(OSDiWrapper wrap, IRI individualIRI, IRI workingModelIRI, DisutilityCombinationMethod duCombinationMethod, int studyYear, int timeHorizon, 
		double discountRateForCosts, double discountRateForEffects, int nPSARuns, int nSimulatedIndividuals) {
		wrap.createIndividual(OSDiClass.EXPERIMENT, individualIRI);
		wrap.assertObjectProperty(individualIRI, OSDiObjectProperty.USES_MODEL, workingModelIRI);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_TIME_HORIZON, "" + timeHorizon, OWL2Datatype.XSD_INTEGER);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DISCOUNT_FOR_COSTS, "" + discountRateForCosts, OWL2Datatype.XSD_DOUBLE);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DISCOUNT_FOR_EFFECTS, "" + discountRateForEffects, OWL2Datatype.XSD_DOUBLE);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_YEAR, "" + studyYear, OWL2Datatype.XSD_INTEGER);		
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DISUTILITY_COMBINATION_METHOD, duCombinationMethod.name());
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_NUMBER_OF_PSA_RUNS, "" + nPSARuns, OWL2Datatype.XSD_INTEGER);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_NUMBER_OF_SIMULATED_INDIVIDUALS, "" + nSimulatedIndividuals, OWL2Datatype.XSD_INTEGER);
	}
}
