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
import es.ull.simulation.hta.outcomes.CostCombinationMethod;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;
import es.ull.simulation.hta.outcomes.LifeExpectancyReductionCombinationMethod;
import es.ull.simulation.hta.outcomes.MortalityRateCombinationMethod;
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
	 * Default method to combine disutilities (ADD)
	 */
	public final static DisutilityCombinationMethod DEF_DISUTILITY_COMBINATION_METHOD = DisutilityCombinationMethod.ADD;
	/**
	 * Default method to combine annual costs (ADD)
	 */
	public final static CostCombinationMethod DEF_COST_COMBINATION_METHOD = CostCombinationMethod.ADD;
	/**
	 * Default method to combine life expectancy reductions (MAX)
	 */
	public final static LifeExpectancyReductionCombinationMethod DEF_LIFE_EXPECTANCY_REDUCTION_COMBINATION_METHOD = LifeExpectancyReductionCombinationMethod.MAX;
	/**
	 * Default method to combine increased mortality rates (MAX)
	 */
	public final static MortalityRateCombinationMethod DEF_MORTALITY_RATE_COMBINATION_METHOD = MortalityRateCombinationMethod.MAX;
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
	 * The combination method used to combine the annual costs of simultaneously active items
	 */
    private CostCombinationMethod costCombinationMethod;
	/** 
	 * The combination method used to combine the life expectancy reductions of simultaneously active items
	 */
    private LifeExpectancyReductionCombinationMethod lerCombinationMethod;
	/** 
	 * The combination method used to combine the increased mortality rates of simultaneously active items
	 */
    private MortalityRateCombinationMethod imrCombinationMethod;
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

        // Initializes the combination methods. Only the disutility combination method is mandatory in OSDi
		this.duCombinationMethod = readCombinationMethod(OSDiDataProperty.HAS_DISUTILITY_COMBINATION_METHOD, DisutilityCombinationMethod.class, DEF_DISUTILITY_COMBINATION_METHOD, true);
		this.costCombinationMethod = readCombinationMethod(OSDiDataProperty.HAS_COST_COMBINATION_METHOD, CostCombinationMethod.class, DEF_COST_COMBINATION_METHOD, false);
		this.lerCombinationMethod = readCombinationMethod(OSDiDataProperty.HAS_LIFE_EXPECTANCY_REDUCTION_COMBINATION_METHOD, LifeExpectancyReductionCombinationMethod.class, DEF_LIFE_EXPECTANCY_REDUCTION_COMBINATION_METHOD, false);
		this.imrCombinationMethod = readCombinationMethod(OSDiDataProperty.HAS_MORTALITY_RATE_COMBINATION_METHOD, MortalityRateCombinationMethod.class, DEF_MORTALITY_RATE_COMBINATION_METHOD, false);
		this.modelWrapper = new ModelWrapper(this, this.workingModelIRI);
    }

	/**
	 * Reads a combination method defined for the experiment. If the property is not defined, or its value is not valid, returns the default method.
	 * @param <E> The enum type of the combination method
	 * @param property The data property that defines the combination method
	 * @param methodClass The enum class of the combination method
	 * @param defaultMethod The method to use if the property is not defined or is not valid
	 * @param warnIfMissing If true, a warning is logged when the property is not defined; otherwise, only an informative message is logged
	 * @return The combination method defined for the experiment, or the default one
	 */
	private <E extends Enum<E>> E readCombinationMethod(OSDiDataProperty property, Class<E> methodClass, E defaultMethod, boolean warnIfMissing) {
		final Optional<OWLLiteral> literal = wrap.getValue(this.individualIRI, property);
		if (literal.isEmpty()) {
			if (warnIfMissing) {
				log.warn(this.individualIRI, property, "Combination method not defined. Using default: " + defaultMethod);
			}
			else {
				log.info("Combination method " + property + " not defined for " + this.individualIRI + ". Using default: " + defaultMethod);
			}
			return defaultMethod;
		}
		try {
			return Enum.valueOf(methodClass, literal.get().getLiteral().trim());
		}
		catch(IllegalArgumentException ex) {
			log.warn(this.individualIRI, property, "Combination method not valid. \"" + literal.get().getLiteral() + "\" not found. Using default: " + defaultMethod);
			return defaultMethod;
		}
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
	 * Returns the combination method used to combine the annual costs of simultaneously active items
	 * @return The cost combination method
	 */
	public CostCombinationMethod getCostCombinationMethod() {
		return costCombinationMethod;
	}

	/**
	 * Sets the combination method used to combine the annual costs of simultaneously active items
	 * @param costCombinationMethod The cost combination method to set
	 */
	public void setCostCombinationMethod(CostCombinationMethod costCombinationMethod) {
		this.costCombinationMethod = costCombinationMethod;
	}

	/**
	 * Returns the combination method used to combine the life expectancy reductions of simultaneously active items
	 * @return The life expectancy reduction combination method
	 */
	public LifeExpectancyReductionCombinationMethod getLifeExpectancyReductionCombinationMethod() {
		return lerCombinationMethod;
	}

	/**
	 * Sets the combination method used to combine the life expectancy reductions of simultaneously active items
	 * @param lerCombinationMethod The life expectancy reduction combination method to set
	 */
	public void setLifeExpectancyReductionCombinationMethod(LifeExpectancyReductionCombinationMethod lerCombinationMethod) {
		this.lerCombinationMethod = lerCombinationMethod;
	}

	/**
	 * Returns the combination method used to combine the increased mortality rates of simultaneously active items
	 * @return The mortality rate combination method
	 */
	public MortalityRateCombinationMethod getMortalityRateCombinationMethod() {
		return imrCombinationMethod;
	}

	/**
	 * Sets the combination method used to combine the increased mortality rates of simultaneously active items
	 * @param imrCombinationMethod The mortality rate combination method to set
	 */
	public void setMortalityRateCombinationMethod(MortalityRateCombinationMethod imrCombinationMethod) {
		this.imrCombinationMethod = imrCombinationMethod;
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
		create(wrap, individualIRI, workingModelIRI, duCombinationMethod, DEF_COST_COMBINATION_METHOD, DEF_LIFE_EXPECTANCY_REDUCTION_COMBINATION_METHOD, DEF_MORTALITY_RATE_COMBINATION_METHOD,
			studyYear, timeHorizon, discountRateForCosts, discountRateForEffects, nPSARuns, nSimulatedIndividuals);
	}

	public static void create(OSDiWrapper wrap, IRI individualIRI, IRI workingModelIRI, DisutilityCombinationMethod duCombinationMethod, CostCombinationMethod costCombinationMethod, 
		LifeExpectancyReductionCombinationMethod lerCombinationMethod, MortalityRateCombinationMethod imrCombinationMethod, int studyYear, int timeHorizon, 
		double discountRateForCosts, double discountRateForEffects, int nPSARuns, int nSimulatedIndividuals) {
		wrap.createIndividual(OSDiClass.EXPERIMENT, individualIRI);
		wrap.assertObjectProperty(individualIRI, OSDiObjectProperty.USES_MODEL, workingModelIRI);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_TIME_HORIZON, "" + timeHorizon, OWL2Datatype.XSD_INTEGER);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DISCOUNT_FOR_COSTS, "" + discountRateForCosts, OWL2Datatype.XSD_DOUBLE);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DISCOUNT_FOR_EFFECTS, "" + discountRateForEffects, OWL2Datatype.XSD_DOUBLE);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_YEAR, "" + studyYear, OWL2Datatype.XSD_INTEGER);		
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_DISUTILITY_COMBINATION_METHOD, duCombinationMethod.name());
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_COST_COMBINATION_METHOD, costCombinationMethod.name());
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_LIFE_EXPECTANCY_REDUCTION_COMBINATION_METHOD, lerCombinationMethod.name());
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_MORTALITY_RATE_COMBINATION_METHOD, imrCombinationMethod.name());
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_NUMBER_OF_PSA_RUNS, "" + nPSARuns, OWL2Datatype.XSD_INTEGER);
		wrap.assertDataProperty(individualIRI, OSDiDataProperty.HAS_NUMBER_OF_SIMULATED_INDIVIDUALS, "" + nSimulatedIndividuals, OWL2Datatype.XSD_INTEGER);
	}
}
