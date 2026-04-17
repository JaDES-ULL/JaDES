package es.ull.simulation.hta.osdi.ontology;

import java.util.Optional;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;

public class UncertaintyCharacterization {		
   	/**
	 * The logger for this class
	 */
	private final static OSDiLogger log = OSDiLogger.getLogger(UncertaintyCharacterization.class);

	private final Set<ParameterWrapper> parameters = new java.util.TreeSet<>();
	private ProbabilisticExpressionWrapper distribution;
	private final ParameterWrapper sourceParameter;

	public UncertaintyCharacterization(ParameterWrapper sourceParameter, double expectedValue, OSDiDataItemType dataType, Set<ParameterWrapper> parameters) throws MalformedOSDiModelException {
		this.sourceParameter = sourceParameter;
		this.parameters.addAll(parameters);
        this.distribution = forceProbabilisticExpression(expectedValue, dataType).orElse(null);
	}

	public UncertaintyCharacterization(ParameterWrapper sourceParameter, ProbabilisticExpressionWrapper distribution) {
		this.sourceParameter = sourceParameter;
		this.distribution = distribution;
	}

    public Optional<ProbabilisticExpressionWrapper> getProbabilisticExpression() {
        return Optional.ofNullable(distribution);
	}

	public Set<ParameterWrapper> getParameters() {
		return parameters;
	}

    /**
     * Factory method to create a synthetic probabilistic expression wrapper from a set of parameters representing uncertainty. 
     * @param modelWrapper
     * @param expressionIRI
     * @param uncertainty
     * @return
     * @throws MalformedOSDiModelException 
     * @throws UnsupportedOSDiFeatureException 
     */
    private Optional<ProbabilisticExpressionWrapper> forceProbabilisticExpression(double expectedValue, OSDiDataItemType dataType) throws MalformedOSDiModelException {
		ProbabilisticExpressionWrapper result = null;
        try {
            if (parameters.size() == 1) {
                final ParameterWrapper uncertainParam = (ParameterWrapper)parameters.toArray()[0];
                result = inferProbabilisticExpressionFromOneParameter(expectedValue, dataType, uncertainParam);
            }
            else if (parameters.size() == 2) {
                final ParameterWrapper uncertainParam1 = (ParameterWrapper)parameters.toArray()[0];
                final ParameterWrapper uncertainParam2 = (ParameterWrapper)parameters.toArray()[1];
                result = inferProbabilisticExpressionFromTwoParameters(expectedValue, dataType, uncertainParam1, uncertainParam2);
            }
            else if (parameters.size() > 2) {
                throw new UnsupportedOSDiFeatureException("More than two uncertainty characterization for a parameter not supported. Currently " + parameters.size());
            }
        } catch (UnsupportedOSDiFeatureException e) {
			log.warn("Failed to infer probabilistic expression from uncertainty parameters of parameter " + sourceParameter.getShortName() + ". Reason: " + e.getMessage());
		    result = null;
		}
		return Optional.ofNullable(result);
    }


	private ProbabilisticExpressionWrapper inferProbabilisticExpressionFromOneParameter(double expectedValue, OSDiDataItemType dataType, ParameterWrapper uncertainParam) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final OSDiWrapper wrap = uncertainParam.getOSDiWrapper();
        final ModelWrapper modelWrapper = uncertainParam.getModelWrapper();
		final IRI dataItemTypeIRI = wrap.toIRI(dataType);
		// If the uncertainty is characterized by a standard deviation, then we use a normal distribution
		if (OSDiDataItemType.DI_STANDARD_DEVIATION.equals(uncertainParam.getDataItemType())) {
			if (Double.isNaN(expectedValue)) {
				throw new MalformedOSDiModelException("Parameter " + sourceParameter.getShortName() + " tries to characterize uncertainty with standard deviation (" + uncertainParam.getShortName() + ") but expected value is not defined");
			}
			if (!(uncertainParam.getParameterNatureData() instanceof DeterministicParameterData)) {
				throw new UnsupportedOSDiFeatureException("Only deterministic parameter nature currently supported for standard deviation parameters. Parameter " + uncertainParam.getShortName() + " has parameter nature " + uncertainParam.getParameterNatureData().getNature());
			}
			double sdValue = ((DeterministicParameterData) uncertainParam.getParameterNatureData()).value();
			if (Double.isNaN(sdValue)) {
				throw new MalformedOSDiModelException("Parameter " + uncertainParam.getShortName() + " has standard deviation data item type but value is not defined");
			}
			// Default distribution for costs is Gamma
			if (wrap.isInstanceOf(dataItemTypeIRI, OSDiClass.CURRENCY)) {
				return ProbabilisticExpressionWrapper.buildGammaFromAverageAndSD(modelWrapper, sourceParameter.getShortName() + "_uncertainty", expectedValue, sdValue);
			}
			// Default distribution for quality of life parameters is Beta
			else if (wrap.isInstanceOf(dataItemTypeIRI, OSDiClass.QOL_DATA_ITEM_TYPE)) {
				return ProbabilisticExpressionWrapper.buildBetaFromAverageAndSD(modelWrapper, sourceParameter.getShortName() + "_uncertainty", expectedValue, sdValue);
			}
			// For other types of parameters, we use a normal distribution
			else {
				return ProbabilisticExpressionWrapper.buildNormalFromAverageAndSD(modelWrapper, sourceParameter.getShortName() + "_uncertainty", expectedValue, sdValue);
			}
		}
		else {
			throw new MalformedOSDiModelException(OSDiClass.PARAMETER, uncertainParam.getShortName(), OSDiObjectProperty.HAS_DATA_ITEM_TYPE, "Data item type not supported for characterizing uncertainty.");
		}
	}

	private ProbabilisticExpressionWrapper inferProbabilisticExpressionFromTwoParameters(double expectedValue, OSDiDataItemType dataType, ParameterWrapper uncertainParam1, ParameterWrapper uncertainParam2) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final OSDiWrapper wrap = uncertainParam1.getOSDiWrapper();
        final ModelWrapper modelWrapper = uncertainParam1.getModelWrapper();
		ParameterWrapper lowerCI = null;
		ParameterWrapper upperCI = null;
		if (OSDiDataItemType.DI_LOWER_95_CONFIDENCE_LIMIT.equals(uncertainParam1.getDataItemType())) {
			lowerCI = uncertainParam1;
			upperCI = uncertainParam2;
			if (!OSDiDataItemType.DI_UPPER_95_CONFIDENCE_LIMIT.equals(uncertainParam2.getDataItemType())) {
				throw new MalformedOSDiModelException(OSDiClass.PARAMETER, uncertainParam2.getIndividualIRI(), OSDiObjectProperty.HAS_DATA_ITEM_TYPE, "Upper and lower confidence intervals required to represent uncertainty. This parameter should include data item type " + OSDiDataItemType.DI_UPPER_95_CONFIDENCE_LIMIT);
			}
		}
		else if (OSDiDataItemType.DI_UPPER_95_CONFIDENCE_LIMIT.equals(uncertainParam1.getDataItemType())) {
			upperCI = uncertainParam1;
			lowerCI = uncertainParam2;
			if (!OSDiDataItemType.DI_LOWER_95_CONFIDENCE_LIMIT.equals(uncertainParam2.getDataItemType())) {
				throw new MalformedOSDiModelException(OSDiClass.PARAMETER, uncertainParam2.getIndividualIRI(), OSDiObjectProperty.HAS_DATA_ITEM_TYPE, "Upper and lower confidence intervals required to represent uncertainty. This parameter should include data item type " + OSDiDataItemType.DI_LOWER_95_CONFIDENCE_LIMIT);
			}				
		}
		else {
			throw new UnsupportedOSDiFeatureException("Unsupported combination of valuables (" + uncertainParam1.getIndividualIRI() + ", " + uncertainParam2.getIndividualIRI() + ") to define the uncertainty");
		}
		if (!(lowerCI.getParameterNatureData() instanceof DeterministicParameterData)) {
			throw new UnsupportedOSDiFeatureException("Only deterministic parameter nature currently supported for confidence interval parameters. Parameter " + lowerCI.getShortName() + " has parameter nature " + lowerCI.getParameterNatureData().getNature());
		}
		if (!(upperCI.getParameterNatureData() instanceof DeterministicParameterData)) {
			throw new UnsupportedOSDiFeatureException("Only deterministic parameter nature currently supported for confidence interval parameters. Parameter " + upperCI.getShortName() + " has parameter nature " + upperCI.getParameterNatureData().getNature());
		}
		if (Double.isNaN(expectedValue)) {
			log.warn("Parameter " + sourceParameter.getShortName() + " tries to characterize uncertainty with confidence intervals but expected value is not defined. Using average of upper and lower confidence intervals as expected value");
			expectedValue = (((DeterministicParameterData)lowerCI.getParameterNatureData()).value() + ((DeterministicParameterData)upperCI.getParameterNatureData()).value()) / 2.0;
		}
		double[] ciValues = new double[] {((DeterministicParameterData) lowerCI.getParameterNatureData()).value(), ((DeterministicParameterData) upperCI.getParameterNatureData()).value()};
		// Default distribution for costs is Gamma
		IRI dataItemTypeIRI = wrap.toIRI(dataType);
		if (wrap.isInstanceOf(dataItemTypeIRI, OSDiClass.CURRENCY)) {
			return ProbabilisticExpressionWrapper.buildGammaFromAverageAndCIs(modelWrapper, sourceParameter.getShortName() + "_uncertainty", expectedValue, ciValues);
		}
		// Default distribution for quality of life parameters is Beta
		else if (wrap.isInstanceOf(dataItemTypeIRI, OSDiClass.QOL_DATA_ITEM_TYPE)) {
			return ProbabilisticExpressionWrapper.buildBetaFromAverageAndCIs(modelWrapper, sourceParameter.getShortName() + "_uncertainty", expectedValue, ciValues);
		}
		// For other types of parameters, we use a normal distribution
		else {
			return ProbabilisticExpressionWrapper.buildNormalFromAverageAndCIs(modelWrapper, sourceParameter.getShortName() + "_uncertainty", expectedValue, ciValues);
		}
	}

}