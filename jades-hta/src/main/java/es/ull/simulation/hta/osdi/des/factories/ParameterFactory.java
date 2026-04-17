package es.ull.simulation.hta.osdi.des.factories;

import es.ull.simulation.hta.expressionEvaluators.ExpressionLanguageParameter;
import es.ull.simulation.hta.expressionEvaluators.JavaluatorParameter;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.ontology.ExpressionLanguageType;
import es.ull.simulation.hta.osdi.ontology.ParameterNatureType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ProbabilisticExpressionWrapper;
import es.ull.simulation.hta.osdi.ontology.ProbabilityDistributionExpressionType;
import es.ull.simulation.hta.osdi.ontology.UncertaintyCharacterization;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.CalculatedParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.FirstOrderUncertaintyParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.ParameterNatureData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SecondOrderUncertaintyParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForCost;
import es.ull.simulation.hta.params.ConstantNatureParameter;
import es.ull.simulation.hta.params.FirstOrderNatureParameter;
import es.ull.simulation.hta.params.Parameter;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.hta.params.SecondOrderNatureParameter;
import simkit.random.RandomVariate;
import simkit.random.RandomVariateFactory;

public interface ParameterFactory extends DESModelComponentFactory {
	final static OSDiLogger log = OSDiLogger.getLogger(ParameterFactory.class);

	public enum SupportedProbabilisticExpressions {
		NORMAL(ProbabilityDistributionExpressionType.NORMAL), 
		UNIFORM(ProbabilityDistributionExpressionType.UNIFORM), 
		BETA(ProbabilityDistributionExpressionType.BETA), 
		GAMMA(ProbabilityDistributionExpressionType.GAMMA), 
		EXPONENTIAL(ProbabilityDistributionExpressionType.EXPONENTIAL), 
		POISSON(ProbabilityDistributionExpressionType.POISSON), 
		BERNOULLI(ProbabilityDistributionExpressionType.BERNOULLI), 
		RR_FROM_LN_CI(null);
		private final ProbabilityDistributionExpressionType osdiMap;
		SupportedProbabilisticExpressions(ProbabilityDistributionExpressionType osdiMap) {
			this.osdiMap = osdiMap;
		}
		public ProbabilityDistributionExpressionType getOSDiMap() {
			return osdiMap;
		}
	}

    /**
     * Creates a parameter instance based on the provided ParameterWrapper and type.
     *
     * @param model The model to which the parameter will be added.
     * @param parameterWrapper The wrapper containing parameter details.
     * @param type The type of the parameter (e.g., COST, UTILITY, etc.).
     * @return A Parameter instance corresponding to the provided wrapper.
     */
    public static Parameter getParameterInstance(OSDiDESModel model, ParameterWrapper parameterWrapper, ParameterGroup type) {
        final String paramIRI = parameterWrapper.getShortName();
		if (model.getParameters().containsKey(paramIRI))
			return model.getParameters().get(paramIRI);
        final String description = parameterWrapper.getDescription().orElse("");
        final String source = parameterWrapper.getSource();
        final int year = (parameterWrapper.getSpecificInformation() instanceof SpecificInformationForCost) ? 
			((SpecificInformationForCost)parameterWrapper.getSpecificInformation()).year() : model.getStudyYear();
		final ParameterNatureData natureData = parameterWrapper.getParameterNatureData();
		switch(parameterWrapper.getNature()) {
		case DETERMINISTIC:
			return new ConstantNatureParameter(model, paramIRI, description, source, year, type, ((DeterministicParameterData) natureData).value());
		case FIRST_ORDER:
			return new FirstOrderNatureParameter(model, paramIRI, description, source, year, type, getRandomVariateInstance(parameterWrapper));
		case SECOND_ORDER:
			return new SecondOrderNatureParameter(model, paramIRI, description, source, year, type, ((SecondOrderUncertaintyParameterData) natureData).expectedValue(), getRandomVariateInstance(parameterWrapper));
		case CALCULATED:
			ExpressionLanguageType expressionLanguage = ((CalculatedParameterData) natureData).expressionLanguage();
			if (ExpressionLanguageType.JAVALUATOR.equals(expressionLanguage))
				return new JavaluatorParameter(model, paramIRI, description, source, year, type, ((CalculatedParameterData) natureData).expression());
			// Since valid expression languages were checked in the constructor, the only possibility is JEXL
			return new ExpressionLanguageParameter(model, paramIRI, description, source, year, type, ((CalculatedParameterData) natureData).expression());
		default:
			return null;
		}
    }

	/**
	 * Creates a RandomVariate instance based on the provided ParameterWrapper.
	 * This method determines the type of distribution and its parameters from the wrapper.
	 * 
	 * @param model The model to which the random variate will be added.
	 * @param parameterWrapper The wrapper containing probabilistic expression details.
	 * @throws IllegalArgumentException If the probability distribution is not supported.
	 * @return A RandomVariate instance corresponding to the provided wrapper.
	 */
    public static RandomVariate getRandomVariateInstance(ParameterWrapper parameterWrapper) {
		if (ParameterNatureType.DETERMINISTIC.equals(parameterWrapper.getNature())) {
			return RandomVariateFactory.getInstance("ConstantVariate", ((DeterministicParameterData) parameterWrapper.getParameterNatureData()).value());
		}
		if (ParameterNatureType.CALCULATED.equals(parameterWrapper.getNature())) {
			log.warn("Parameter " + parameterWrapper.getShortName() + " is a calculated parameter. We cannot create a random variate instance for it, so we will return a constant variate with value 0.0.");
			return RandomVariateFactory.getInstance("ConstantVariate", 0.0);
		}
		double expectedValue = 0.0;		
		UncertaintyCharacterization uncertaintyCharacterization = null;
		if (ParameterNatureType.FIRST_ORDER.equals(parameterWrapper.getNature())) {
			uncertaintyCharacterization = ((FirstOrderUncertaintyParameterData) parameterWrapper.getParameterNatureData()).uncertaintyCharacterization();
		}
		else if (ParameterNatureType.SECOND_ORDER.equals(parameterWrapper.getNature())) {
			uncertaintyCharacterization = ((SecondOrderUncertaintyParameterData) parameterWrapper.getParameterNatureData()).uncertaintyCharacterization();
			expectedValue = ((SecondOrderUncertaintyParameterData) parameterWrapper.getParameterNatureData()).expectedValue();
		}
		if (uncertaintyCharacterization == null) {
			log.warn("Parameter " + parameterWrapper.getShortName() + " does not have a valid uncertainty characterization. We will assume a constant variate with value " + expectedValue);
			return RandomVariateFactory.getInstance("ConstantVariate", expectedValue);
		}
		final ProbabilisticExpressionWrapper probExpr = uncertaintyCharacterization.getProbabilisticExpression().orElse(null);
		if (probExpr == null) {
			log.warn("Uncertainty characterization for " + parameterWrapper.getShortName() + " does not have a probabilistic expression defined. We will assume a constant variate with value " + expectedValue);
			return RandomVariateFactory.getInstance("ConstantVariate", expectedValue);
		}
		String distributionName = "";
		switch(probExpr.getProbabilityDistribution()) {
			case NORMAL:
				distributionName = "NormalVariate";
				break;
			case UNIFORM:
				distributionName = "UniformVariate";
				break;
			case BETA:
				distributionName = "BetaVariate";
				break;
			case GAMMA:
				distributionName = "GammaVariate";
				break;
			case EXPONENTIAL:
				distributionName = "ExponentialVariate";
				break;
			case POISSON:
				distributionName = "PoissonVariate";
				break;
			case BERNOULLI:
				distributionName = "BernoulliVariate";
				break;
/*			case RR_FROM_LN_CI:
				distributionName = "RRFromLnCIVariate";
				break;
*/			default:
				throw new IllegalArgumentException("Unsupported probability distribution: " + probExpr.getProbabilityDistribution());
		}
		double []numParams = probExpr.getParameterValues();
		RandomVariate rnd = null;
		if (numParams.length == 4)
			rnd = RandomVariateFactory.getInstance(distributionName, numParams[0], numParams[1], numParams[2], numParams[3]);
		else if (numParams.length == 3)
			rnd = RandomVariateFactory.getInstance(distributionName, numParams[0], numParams[1], numParams[2]);
		else if (numParams.length == 2)
			rnd = RandomVariateFactory.getInstance(distributionName, numParams[0], numParams[1]);
		else
			rnd = RandomVariateFactory.getInstance(distributionName, numParams[0]);
		if (probExpr.getScale() != 1.0 || probExpr.getOffset() != 0.0) {
			rnd = RandomVariateFactory.getInstance("ScaledVariate", rnd, probExpr.getScale(), probExpr.getOffset());
		}
		return rnd;
    }
}
