package es.ull.simulation.hta.osdi.ontology;

import java.util.OptionalDouble;
import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;

/**
 * Enumeration of the probability distribution expressions defined in the OSDi ontology.
 * @author Iván Castilla Rodríguez
 * TODO: Process parameters when expressed in different ways. E.g. gamma parameters may be average and standard deviation
 */
public enum ProbabilityDistributionExpressionType implements WrapsOSDiClass {
	NORMAL(OSDiClass.NORMAL_DISTRIBUTION_EXPRESSION, new OSDiDataProperty[] {OSDiDataProperty.HAS_AVERAGE_PARAMETER, OSDiDataProperty.HAS_STANDARD_DEVIATION_PARAMETER}),
	UNIFORM(OSDiClass.UNIFORM_DISTRIBUTION_EXPRESSION, new OSDiDataProperty[] {OSDiDataProperty.HAS_LOWER_LIMIT_PARAMETER, OSDiDataProperty.HAS_UPPER_LIMIT_PARAMETER}),
	BETA(OSDiClass.BETA_DISTRIBUTION_EXPRESSION, new OSDiDataProperty[] {OSDiDataProperty.HAS_ALFA_PARAMETER, OSDiDataProperty.HAS_BETA_PARAMETER}),
	GAMMA(OSDiClass.GAMMA_DISTRIBUTION_EXPRESSION, new OSDiDataProperty[] {OSDiDataProperty.HAS_ALFA_PARAMETER, OSDiDataProperty.HAS_LAMBDA_PARAMETER}),
	EXPONENTIAL(OSDiClass.EXPONENTIAL_DISTRIBUTION_EXPRESSION, new OSDiDataProperty[] {OSDiDataProperty.HAS_LAMBDA_PARAMETER}),
	POISSON(OSDiClass.POISSON_DISTRIBUTION_EXPRESSION, new OSDiDataProperty[] {OSDiDataProperty.HAS_LAMBDA_PARAMETER}),
	BERNOULLI(OSDiClass.BERNOULLI_DISTRIBUTION_EXPRESSION, new OSDiDataProperty[] {OSDiDataProperty.HAS_PROBABILITY_PARAMETER});
	private final static TreeMap<OSDiClass, ProbabilityDistributionExpressionType> reverseDistribution = new TreeMap<>(); 
	static {
		for (ProbabilityDistributionExpressionType dist : ProbabilityDistributionExpressionType.values()) {
			reverseDistribution.put(dist.clazz, dist);
		}
	}

	private final OSDiClass clazz;
	private final OSDiDataProperty[] parameters;
	
	private ProbabilityDistributionExpressionType(OSDiClass clazz, OSDiDataProperty[] parameters) {
		this.clazz = clazz;
		this.parameters = parameters;
	}
	
	/**
	 * Returns the data properties that define the parameters of this distribution.
	 * @return An array of OSDiDataProperties that represent the parameters of the distribution.
	 */
	public OSDiDataProperty[] getParameters() {
		return parameters;
	}

	/**
	 * Returns the values of the parameters for the given instance IRI, which must be of the type defined by this distribution.
	 * @param wrap The wrapper for the OSDi model.
	 * @param individualIRI The IRI of the instance that represents the probability distribution.
	 * @return An array of parameter values as doubles.
	 * @throws MalformedOSDiModelException 
	 */
	public double[] getParameterValues(OSDiWrapper wrap, IRI individualIRI) throws MalformedOSDiModelException {
		double [] result = new double[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			final OptionalDouble value = wrap.getDoubleValue(individualIRI, parameters[i]);
			if (value.isEmpty()) {
				throw new MalformedOSDiModelException("The individual " + individualIRI + " of distribution " + name() + " is missing value for parameter " + parameters[i]);
			}
			result[i] = value.getAsDouble();
		}
		return result;
	}	

	@Override
	public OSDiClass getClazz() {
		return clazz;
	}

	/**
	 * @return the nParameters
	 */
	public int getnParameters() {
		return parameters.length;
	}
	
	/**
	 * Adds a new instance of this probability distribution to the OSDi model.
	 * @param wrap The wrapper for the OSDi model.
	 * @param instanceId The IRI of the instance to be created.
	 * @param parameterValues An array of parameter values for the distribution. The length of this array must match the number of parameters defined for this distribution.
	 * @throws IllegalArgumentException If the length of the parameterValues array does not match the number of parameters defined for this distribution.
	 */
	public void add(OSDiWrapper wrap, IRI instanceId, double[] parameterValues) {
		if (parameters.length != parameterValues.length)
			throw new IllegalArgumentException("Creating a " + name() + " probability distribution requires " + parameters.length + " parameters. Passed " + parameterValues.length);

		wrap.createIndividual(clazz, instanceId);
		for (int i = 0; i < parameterValues.length; i++) {
			wrap.assertDataProperty(instanceId, this.parameters[i], "" + parameterValues[i], OWL2Datatype.XSD_DOUBLE);
		}
	}

	/**
	 * Returns the OSDiProbabilityDistributionExpression associated with the given OSDiClass.
	 * @param clazz The OSDiClass to get the OSDiProbabilityDistributionExpressions for.
	 * @return The OSDiProbabilityDistributionExpression associated with the given OSDiClass.
	 */
	public static ProbabilityDistributionExpressionType fromOSDiClass(OSDiClass clazz) {
		return reverseDistribution.get(clazz);
	}
}