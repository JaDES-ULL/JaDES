/**
 * 
 */
package es.ull.simulation.hta.osdi.ontology;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.checkerframework.checker.units.qual.s;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.parameters.Imports;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.ontology.OWLOntologyWrapper.InstanceCheckMode;

/**
 * A wrapper class for a parameter in the OSDi ontology.
 * @author Iván Castilla Rodríguez
 *
 */
public class ParameterWrapper extends BaseModelItemWrapper {
	/**
	 * A common interface for the data associated to the nature of a parameter, i.e., whether it is deterministic, first or second order uncertainty or calculated.
	 */
	public interface ParameterNatureData {
		ParameterNatureType getNature();
	}
	/**
	 * The data associated to a deterministic parameter, which is just a single value.
	 * @param value the value of the parameter
	 */
	public record DeterministicParameterData(double value) implements ParameterNatureData {
		@Override
		public ParameterNatureType getNature() {
			return ParameterNatureType.DETERMINISTIC;
		}
	}
	/**
	 * The data associated to a first order uncertainty parameter, which is a probabilistic expression that characterizes the uncertainty of the parameter.
	 * @param uncertaintyCharacterization the uncertainty characterization of the parameter
	 */
	public record FirstOrderUncertaintyParameterData(UncertaintyCharacterization uncertaintyCharacterization) implements ParameterNatureData {
		@Override
		public ParameterNatureType getNature() {
			return ParameterNatureType.FIRST_ORDER;
		}
	}
	/**
	 * The data associated to a second order uncertainty parameter, which is an expected value and a probabilistic expression that characterizes the uncertainty of the parameter.
	 * @param expectedValue the expected value of the parameter
	 * @param uncertaintyCharacterization the uncertainty characterization of the parameter
	 */
	public record SecondOrderUncertaintyParameterData(double expectedValue, UncertaintyCharacterization uncertaintyCharacterization) implements ParameterNatureData {
		@Override
		public ParameterNatureType getNature() {
			return ParameterNatureType.SECOND_ORDER;
		}
	}
	/**
	 * The data associated to a calculated parameter, which is an expression and the parameters and attributes it depends on.
	 * @param expression the expression used to calculate the parameter
	 * @param dependantParameters the parameters this calculated parameter depends on
	 * @param dependantAttributes the attributes this calculated parameter depends on
	 * @param expressionLanguage the language in which the expression is defined
	 */
	public record CalculatedParameterData(String expression, ArrayList<ParameterWrapper> dependantParameters, ArrayList<AttributeWrapper> dependantAttributes, ExpressionLanguageType expressionLanguage) implements ParameterNatureData {
		@Override
		public ParameterNatureType getNature() {
			return ParameterNatureType.CALCULATED;
		}
	}
	public interface SpecificInformation {}
	public record SpecificInformationForUtility(boolean appliesOneTime, boolean isDisutility) implements SpecificInformation {}
	public record SpecificInformationForCost(boolean appliesOneTime, int year, ResourceUsageSKOSCategory category) implements SpecificInformation {}
	public record SpecificInformationForAttributeValue(IRI attributeIRI) implements SpecificInformation {}

	public static class SyntheticBuilder {
		/**
		 * The IRI of the parameter in the ontology
		 */
		private final IRI paramIRI;
		/**
		 * The model wrapper that contains the ontology information.
		 */
		private final ModelWrapper modelWrapper;
		/**
		 * The source of the parameter, i.e., the source of the data used to define it
		 */
		private String source;
		/**
		 * The description of the parameter
		 */
		private String description;
		/**
		 * The type of data item this parameter represents
		 */
		private OSDiDataItemType dataItemType = null;
        /**
         * The data associated to the nature of the parameter, i.e., whether it is deterministic, first or second order uncertainty, and the expression and dependencies in case it is a calculated parameter.
         */
        private final ParameterNatureData parameterData;
		/**
		 * The classes of the parameter in the ontology
		 */
		private final Set<OSDiClass> types = new TreeSet<>();
		public SyntheticBuilder(ModelWrapper modelWrapper, String name, ParameterNatureData parameterData) {
			this(modelWrapper, IModelItemWrapper.getSyntheticIRI(name), parameterData);
		}
		public SyntheticBuilder(ModelWrapper modelWrapper, IRI sourceIRI, String name, ParameterNatureData parameterData) {
			this(modelWrapper, IModelItemWrapper.getSyntheticIRIFrom(sourceIRI, name), parameterData);
		}
		public SyntheticBuilder(ModelWrapper modelWrapper, IRI syntheticParamIRI, ParameterNatureData parameterData) {
			this.modelWrapper = modelWrapper;
			this.paramIRI = syntheticParamIRI;
            this.parameterData = parameterData;
			if (parameterData instanceof CalculatedParameterData) {
				this.types.add(OSDiClass.CALCULATED_PARAMETER);
			}
			else if (parameterData instanceof DeterministicParameterData) {
				this.types.add(OSDiClass.DETERMINISTIC_PARAMETER);
			}
			else if (parameterData instanceof FirstOrderUncertaintyParameterData) {
				this.types.add(OSDiClass.FIRST_ORDER_UNCERTAINTY_PARAMETER);
			}
			else if (parameterData instanceof SecondOrderUncertaintyParameterData) {
				this.types.add(OSDiClass.SECOND_ORDER_UNCERTAINTY_PARAMETER);
			}
			this.source = "Unknown";
			this.description = "";
		}
		public SyntheticBuilder withExtraTypes(Set<OSDiClass> extraTypes) {
			this.types.addAll(extraTypes);
			return this;
		}
		public SyntheticBuilder withSource(String source) {
			this.source = source;
			return this;
		}
		public SyntheticBuilder withDescription(String description) {
			this.description = description;
			return this;
		}
		public SyntheticBuilder withDataItemType(OSDiDataItemType dataItemType) {
			this.dataItemType = dataItemType;
			return this;
		}
		public ParameterWrapper build() {
			return new ParameterWrapper(this.modelWrapper, this.paramIRI, this.source, this.description, this.dataItemType, this.parameterData, this.types, new SpecificInformation() {});
		}
		public ParameterWrapper buildAsAttributeValueInfo(IRI attributeIRI) {
			SpecificInformationForAttributeValue specificInfo = new SpecificInformationForAttributeValue(attributeIRI);
			return new ParameterWrapper(this.modelWrapper, this.paramIRI, this.source, this.description, this.dataItemType, this.parameterData, this.types, specificInfo);
		}
		public ParameterWrapper buildAsCost(boolean appliesOneTime, ResourceUsageSKOSCategory category) {
			return buildAsCost(appliesOneTime, modelWrapper.getExperimentWrapper().getStudyYear(), category);
		}
		public ParameterWrapper buildAsCost(boolean appliesOneTime, int year, ResourceUsageSKOSCategory category) {
			SpecificInformationForCost specificInfo = new SpecificInformationForCost(appliesOneTime, year, category);
			return new ParameterWrapper(this.modelWrapper, this.paramIRI, this.source, this.description, this.dataItemType, this.parameterData, this.types, specificInfo);
		}
		public ParameterWrapper buildAsUtility(boolean appliesOneTime, boolean isDisutility) {
			SpecificInformationForUtility specificInfo = new SpecificInformationForUtility(appliesOneTime, isDisutility);
			return new ParameterWrapper(this.modelWrapper, this.paramIRI, this.source, this.description, this.dataItemType, this.parameterData, this.types, specificInfo);
		}
	}

	/**
	 * The logger for this class
	 */
	private final static OSDiLogger log = OSDiLogger.getLogger(ParameterWrapper.class);
	/**
	 * The source of the parameter, i.e., the source of the data used to define it
	 */
	private String source;
	/**
	 * The type of data item this parameter represents
	 */
	private OSDiDataItemType dataItemType;
	/**
	 * The data associated to the parameter depending on its nature
	 */
	private ParameterNatureData parameterData;
	/**
	 * The specific information for this parameter depending on its type (cost, utiity...)
	 */
	private SpecificInformation specificInformation;
	/**
	 * The description of the parameter (not obtained from the ontology, but provided when creating the wrapper).
	 */
	private final String syntheticDescription;
	/**
	 * The classes of the parameter in the ontology
	 */
	private final Set<OSDiClass> syntheticTypes = new TreeSet<>();
	/**
	 * Indicates whether this parameter wrapper is synthetic (i.e., created programmatically and not directly based on an ontology individual) or not. Synthetic parameters are created with all necessary information and do not depend on any other ontology item, so they can be set as ready immediately.
	 */
	private final boolean synthetic;

	/**
	 * Constructor for a parameter wrapper without a default description
	 * @param modelWrapper The model wrapper that contains the ontology information.
	 * @param paramIRI The IRI of the parameter in the ontology
	 */
	protected ParameterWrapper(ModelWrapper modelWrapper, IRI paramIRI) {
		super(modelWrapper, paramIRI);
		this.syntheticDescription = null;
		this.synthetic = false;
	}
	
	/**
	 * Constructor for a synthetic parameter wrapper with all the fields defined. 
	 * @param modelWrapper The model wrapper that contains the ontology information.
	 * @param paramIRI The IRI of the parameter in the ontology
	 * @param source The source of the parameter, i.e., the source of the data used to define it
	 * @param description The description of the parameter
	 * @param year The year of the parameter, i.e., the year when the data used to define it was collected
	 * @param dataItemType The type of data item this parameter represents
	 * @param parameterData The data associated to the nature of the parameter
	 * @param attributeIRI The IRI of the attribute this parameter is value of, if it is an attribute value. Otherwise, null.
	 * @param appliesOneTime Indicates if the parameter applies one time or annually
	 */
	public ParameterWrapper(ModelWrapper modelWrapper, IRI paramIRI, String source, String description, OSDiDataItemType dataItemType,
        ParameterNatureData parameterData, Set<OSDiClass> types, SpecificInformation specificInformation) {
        super(modelWrapper, paramIRI);
		this.syntheticDescription = description;
		setSource(source);
        this.syntheticTypes.addAll(types);
        setParameterNatureData(parameterData);
		setSpecificInformation(specificInformation);
		// Synthetic parameters are created with all necessary information and do not depend on any other ontology item, so they can be set as ready immediately.
		setStatus(Status.READY);
		this.synthetic = true;
	}

	@Override
	public boolean isSynthetic() {
		return synthetic;
	}

	@Override
	public void doInitialize() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final IRI paramIRI = getIndividualIRI();
		final ModelWrapper modelWrapper = getModelWrapper();
		final OSDiWrapper wrap = getOSDiWrapper();
		setSource(wrap.getStringValue(paramIRI, OSDiDataProperty.HAS_SOURCE).orElse("Unknown"));
		final Optional<IRI> dataItemTypeIRI = wrap.getValue(paramIRI, OSDiObjectProperty.HAS_DATA_ITEM_TYPE);
		setDataItemType(OSDiDataItemType.fromIndividualIRI(dataItemTypeIRI.orElse(null)));
		if (OSDiDataItemType.DI_UNDEFINED.equals(getDataItemType())) {
			log.warn(paramIRI, OSDiObjectProperty.HAS_DATA_ITEM_TYPE, "Data item type not specified for parameter. Assigning UNDEFINED");
		}
		if (wrap.isInstanceOf(paramIRI, OSDiClass.COST)) {
			int year = modelWrapper.parseHasYearProperty(paramIRI);
			boolean appliesOneTime = wrap.getBooleanValue(paramIRI, OSDiDataProperty.APPLIES_ONE_TIME).orElse(false);
			IRI categoryIRI = wrap.getValue(paramIRI, OSDiObjectProperty.HAS_CATEGORY).orElse(null);
			ResourceUsageSKOSCategory category = (categoryIRI != null) ? ResourceUsageSKOSCategory.fromIRI(categoryIRI) : null;
			this.specificInformation = new SpecificInformationForCost(appliesOneTime, year, category);
		}
		else if (wrap.isInstanceOf(paramIRI, OSDiClass.UTILITY)) {
			boolean appliesOneTime = wrap.getBooleanValue(paramIRI, OSDiDataProperty.APPLIES_ONE_TIME).orElse(false);
			boolean isDisutility = wrap.getBooleanValue(paramIRI, OSDiDataProperty.IS_DISUTILITY).orElse(false);
			this.specificInformation = new SpecificInformationForUtility(appliesOneTime, isDisutility);
		}
		else if (wrap.getValue(paramIRI, OSDiObjectProperty.IS_VALUE_OF_ATTRIBUTE).isPresent()) {
			IRI attributeIRI = wrap.getValue(paramIRI, OSDiObjectProperty.IS_VALUE_OF_ATTRIBUTE).get();
			this.specificInformation = new SpecificInformationForAttributeValue(attributeIRI);
		}
		else {
			this.specificInformation = new SpecificInformation() {};
		}	
		if (wrap.isInstanceOf(paramIRI, OSDiClass.DETERMINISTIC_PARAMETER)) {
			setParameterNatureData(new DeterministicParameterData(wrap.getDoubleValue(paramIRI, OSDiDataProperty.HAS_EXPECTED_VALUE).orElse(0.0)));
		}
		else if (wrap.isInstanceOf(paramIRI, OSDiClass.FIRST_ORDER_UNCERTAINTY_PARAMETER)) {
			try {
				setParameterNatureData(new FirstOrderUncertaintyParameterData(getUncertaintyCharacterization(Double.NaN, getDataItemType())));
			}
			catch (MalformedOSDiModelException e) {
				throw new MalformedOSDiModelException(OSDiClass.FIRST_ORDER_UNCERTAINTY_PARAMETER, paramIRI, OSDiObjectProperty.HAS_UNCERTAINTY_CHARACTERIZATION, "First order parameter requires an uncertainty characterization", e);
			}
		}
		else if (wrap.isInstanceOf(paramIRI, OSDiClass.SECOND_ORDER_UNCERTAINTY_PARAMETER)) {
			// TODO: Try to use the expected value of the uncertainty characterization if not defined explicitly, instead of defaulting to 0.0
			double expectedValue = wrap.getDoubleValue(paramIRI, OSDiDataProperty.HAS_EXPECTED_VALUE).orElse(0.0);
			try {
				setParameterNatureData(new SecondOrderUncertaintyParameterData(expectedValue, getUncertaintyCharacterization(expectedValue, getDataItemType())));
			}
			catch (MalformedOSDiModelException e) {
				throw new MalformedOSDiModelException(OSDiClass.SECOND_ORDER_UNCERTAINTY_PARAMETER, paramIRI, OSDiObjectProperty.HAS_UNCERTAINTY_CHARACTERIZATION, "Second order parameter requires an uncertainty characterization", e);
			}
		}
		else if (wrap.isInstanceOf(paramIRI, OSDiClass.CALCULATED_PARAMETER)) {
			String expression = wrap.getStringValue(paramIRI, OSDiDataProperty.HAS_EXPRESSION_VALUE).orElse("");
			if (expression.equals("")) {			
				throw new MalformedOSDiModelException(OSDiClass.CALCULATED_PARAMETER, paramIRI, OSDiDataProperty.HAS_EXPRESSION_VALUE, "Calculated parameter requires an expression value");
			}
			final Set<IModelItemWrapper> dependantItems = modelWrapper.getWrappersForProperty(paramIRI, OSDiObjectProperty.DEPENDS_ON);
			ArrayList<ParameterWrapper> dependantParameters = new ArrayList<>();
			ArrayList<AttributeWrapper> dependantAttributes = new ArrayList<>();
			for (IModelItemWrapper dependantItem : dependantItems) {
				if (dependantItem instanceof ParameterWrapper)
					dependantParameters.add((ParameterWrapper) dependantItem);
				else if (dependantItem instanceof AttributeWrapper)
					// TODO: Should be the attribute value and not the attribute itself??
					dependantAttributes.add((AttributeWrapper) dependantItem);
			}
			final Optional<IRI> elIRI = wrap.getValue(paramIRI, OSDiObjectProperty.HAS_EXPRESSION_LANGUAGE);
			if (elIRI.isEmpty()) {
				throw new MalformedOSDiModelException(OSDiClass.CALCULATED_PARAMETER, paramIRI, OSDiObjectProperty.HAS_EXPRESSION_LANGUAGE, "Expression language not specified for calculated parameter");
			}
			ExpressionLanguageType expressionLanguage = ExpressionLanguageType.fromIndividualIRI(elIRI.get());
			switch (expressionLanguage) {
				case JAVALUATOR:
				case JEXL:
				case EXCEL:
					break;
				default:
					throw new MalformedOSDiModelException(OSDiClass.CALCULATED_PARAMETER, paramIRI, OSDiObjectProperty.HAS_EXPRESSION_LANGUAGE, "Expression language " + expressionLanguage + " not yet supported");
			}
			setParameterNatureData(new CalculatedParameterData(expression, dependantParameters, dependantAttributes, expressionLanguage));
		}
		else {
			throw new MalformedOSDiModelException("Parameter " + paramIRI + " is not a valid parameter. It should be an instance of " + OSDiClass.DETERMINISTIC_PARAMETER + ", " + OSDiClass.FIRST_ORDER_UNCERTAINTY_PARAMETER + ", " + OSDiClass.CALCULATED_PARAMETER + " or " + OSDiClass.SECOND_ORDER_UNCERTAINTY_PARAMETER);
		}
	}

    @Override
    public void doPersist() throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        // TODO
    }

	private UncertaintyCharacterization getUncertaintyCharacterization(double expectedValue, OSDiDataItemType dataType) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final Set<IModelItemWrapper> uncertaintyParams = getModelWrapper().getAndInitializeWrappersForProperty(getIndividualIRI(), OSDiObjectProperty.HAS_UNCERTAINTY_CHARACTERIZATION);
		if (uncertaintyParams.isEmpty()) {
			throw new MalformedOSDiModelException("Parameter " + getShortName() + " is missing uncertainty characterization");
		}
		else if (uncertaintyParams.size() == 1) {
			IModelItemWrapper wrapper = uncertaintyParams.iterator().next();
			if (wrapper instanceof ProbabilisticExpressionWrapper) {
				return new UncertaintyCharacterization(this, (ProbabilisticExpressionWrapper) wrapper);
			}
			else if (wrapper instanceof ParameterWrapper) {
				return new UncertaintyCharacterization(this, expectedValue, dataType, Set.of((ParameterWrapper) wrapper));
			}
			else {
				throw new MalformedOSDiModelException("Uncertainty characterization should be either a probabilistic expression or a set of parameters, but an instance of " + wrapper.getClass().getSimpleName() + " was found");
			}
		}
		else {
			Set<ParameterWrapper> parameterWrappers = uncertaintyParams.stream().filter(wrapper -> wrapper instanceof ParameterWrapper).map(wrapper -> (ParameterWrapper) wrapper).collect(java.util.stream.Collectors.toSet());
			if (parameterWrappers.size() != uncertaintyParams.size()) {
				throw new MalformedOSDiModelException("Uncertainty characterization should be either a probabilistic expression or a set of parameters, but a mixture was found for parameter " + getShortName());
			}
			return new UncertaintyCharacterization(this, expectedValue, dataType, parameterWrappers);
		}
	}

	@Override
	public Optional<String> getDescription() {
		if (isSynthetic()) {
			return Optional.ofNullable(syntheticDescription);
		}
		return super.getDescription();
	}

	@Override
	public Set<OSDiClass> getTypes(InstanceCheckMode mode) {
		if (isSynthetic()) {
			final OSDiWrapper wrap = getOSDiWrapper();
			final Set<OSDiClass> result = new TreeSet<>(syntheticTypes);
			if (mode == InstanceCheckMode.ASSERTED_DIRECT) {
				return result;
			}
			else {
				for (OSDiClass type : syntheticTypes) {
					final Set<IRI> superClasses = wrap.getSuperClasses(wrap.toIRI(type), Imports.INCLUDED, mode);
					for (IRI superClass : superClasses) {
						Optional<OSDiClass> superClassOpt = OSDiClass.fromIRI(superClass);
						if (!superClassOpt.isEmpty()) {
							result.add(superClassOpt.get());
						}
					}
				}
			}
			return result;
		}
		return super.getTypes(mode);
	}

	/**
	 * Returns the data associated to the parameter depending on its nature
	 * @return the data associated to the parameter depending on its nature
	 */
	public ParameterNatureData getParameterNatureData() {
		return parameterData;
	}

	/**
	 * Sets the data associated to the parameter depending on its nature
	 * @param parameterData the data associated to the parameter depending on its nature
	 */
	protected void setParameterNatureData(ParameterNatureData parameterData) {
		this.parameterData = parameterData;
	}
	
	/**
	 * Returns the nature of the parameter, i.e., deterministic, first or second order uncertainty.
	 * @return the nature of the parameter, i.e., deterministic, first or second order uncertainty.
	 */
	public ParameterNatureType getNature() {
		return parameterData.getNature();
	}

	/**
	 * Returns the specific information associated with the parameter.
	 * @return the specific information associated with the parameter.
	 */
	public SpecificInformation getSpecificInformation() {
		return specificInformation;
	}

	/**
	 * Sets the specific information associated with the parameter.
	 * @param specificInformation the specific information associated with the parameter.
	 */
	public void setSpecificInformation(SpecificInformation specificInformation) {
		this.specificInformation = specificInformation;
	}

	/**
	 * Gets the type of data item this parameter represents
	 * @return the type of data item this parameter represents
	 */
	public OSDiDataItemType getDataItemType() {
		return dataItemType;
	}

	/**
	 * Sets the type of data item this parameter represents
	 * @param dataItemType the type of data item this parameter represents
	 */
	protected void setDataItemType(OSDiDataItemType dataItemType) {
		this.dataItemType = dataItemType;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source of the parameter, i.e., the source of the data used to define it
	 * @param source the source to set
	 */
	protected void setSource(String source) {
		this.source = source;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getShortName()).append(": ");
		switch (parameterData.getNature()) {
			case DETERMINISTIC:
				sb.append(((DeterministicParameterData) parameterData).value());
				break;
			case FIRST_ORDER:
				sb.append(((FirstOrderUncertaintyParameterData) parameterData).uncertaintyCharacterization());
				break;
			case SECOND_ORDER:
				sb.append(((SecondOrderUncertaintyParameterData) parameterData).expectedValue()).append(":").append(((SecondOrderUncertaintyParameterData) parameterData).uncertaintyCharacterization());
				break;
			case CALCULATED:
				sb.append(((CalculatedParameterData) parameterData).expression()).append(" with ").append(((CalculatedParameterData) parameterData).dependantParameters().size()).append(" dependant parameters and ").append(((CalculatedParameterData) parameterData).dependantAttributes().size()).append(" dependant attributes");
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + parameterData.getNature());
		}
		return sb.toString();
	}

    /**
     * Determines the type of a parameter wrapper based on its specific information. This method is used to categorize parameters in the decision tree.
     * @param parameterWrapper The parameter wrapper for which to determine the type.
     * @return The type of the parameter wrapper.
     * @throws MalformedOSDiModelException If the parameter wrapper is malformed.
     */
    public ParameterGroup getParameterGroup() throws MalformedOSDiModelException {
		if (this.getSpecificInformation() instanceof SpecificInformationForAttributeValue) {
			return ParameterGroup.ATTRIBUTE;
		}
		if (this.getSpecificInformation() instanceof SpecificInformationForCost) {
			return ParameterGroup.COST;
		}
		if (this.getSpecificInformation() instanceof SpecificInformationForUtility) {
            SpecificInformationForUtility specificInfo = (SpecificInformationForUtility) this.getSpecificInformation();
			return (specificInfo.isDisutility()) ? ParameterGroup.DISUTILITY : ParameterGroup.UTILITY;
		}

		OSDiDataItemType dataItemType = this.getDataItemType();
		if (dataItemType != null) {
			if (getOSDiWrapper().isInstanceOf(getOSDiWrapper().toIRI(dataItemType), OSDiClass.CURRENCY)) {
				return ParameterGroup.COST;
			}
			else if (getOSDiWrapper().isInstanceOf(getOSDiWrapper().toIRI(dataItemType), OSDiClass.QOL_DATA_ITEM_TYPE)) {
				return (this.getSpecificInformation() instanceof SpecificInformationForUtility && ((SpecificInformationForUtility)this.getSpecificInformation()).isDisutility()) ? ParameterGroup.DISUTILITY : ParameterGroup.UTILITY;
			}
			switch (dataItemType) {
				case DI_PREVALENCE:
				case DI_BIRTH_PREVALENCE:
				case DI_INCIDENCE:
				case DI_RELATIVE_RISK:
				case DI_PROBABILITY:
				case DI_PROPORTION:
					return ParameterGroup.RISK;
				default:
					break;
			}
		}
		// TODO: Complete with other parameter types based on the data item type or other characteristics of the parameter
		return ParameterGroup.UNKNOWN;
    }

}
