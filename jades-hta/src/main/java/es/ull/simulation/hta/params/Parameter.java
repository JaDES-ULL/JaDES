package es.ull.simulation.hta.params;

import java.util.Map;
import java.util.TreeMap;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.NamedAndDescribed;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.PrettyPrintable;
import simkit.random.RandomVariate;
import simkit.random.RandomVariateFactory;

/**
 * A parameter that defines a value for each patient. It may define a fixed value (constant parameter), a different value per simulation (second-order uncertainty), 
 * and even different value per patient (heterogeneity or first-order uncertainty). These are the parameters that uses {@link SecondOrderParamsRepository}. 
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class Parameter implements NamedAndDescribed, PrettyPrintable, Comparable<Parameter>, UsesParameters {
	/** Default second order variation for different parameter types */
    public static class DEF_SECOND_ORDER_VARIATION {
    	public final static double COST = 0.2;
    	public final static double UTILITY = 0.2;
    	public final static double PROBABILITY = 0.5;
    }

	private static final Map<ParameterGroup, Map<String, Parameter>> parametersByType = new TreeMap<>();
	static {
		for (ParameterGroup group : ParameterGroup.values()) {
			parametersByType.put(group, new java.util.TreeMap<>());
		}
	}
	
    /** Short name and identifier of the parameter */
	private final String name;
	/** The group of the parameter */
	private final ParameterGroup group;
	/** Full description of the parameter */
	private final String description;
	/** The reference from which this parameter was estimated/taken */
	private final String source;
	/** Year when the parameter was originally estimated */
	private final int year;
    /** The model this parameter belongs to */
    protected final HTAModel model;
    /** A collection of names for parameters used by this model component */
    private final Map<ParameterTemplate, String> usedParameterNames;

	/**
	 * Creates a parameter
	 * @param name Short name and identifier of the parameter. Must be unique within the simulation.
	 */
	public Parameter(HTAModel model, String name, String description, String source, int year, ParameterGroup group) {
		this.model = model;
		this.name = name;
		this.description = description;
		this.source = source;
		this.year = year;
		this.group = group;
		if (parametersByType.get(group).get(name) != null)
			throw new IllegalArgumentException("Parameter " + name + " already exists");
		parametersByType.get(group).put(name, this);
        this.usedParameterNames = new TreeMap<>();
	}

	/**
	 * Creates a parameter
	 * @param name Short name and identifier of the parameter. Must be unique within the simulation.
	 */
	public Parameter(HTAModel model, String name, String description, String source, ParameterGroup group) {
		this(model, name, description, source, model.getStudyYear(), group);
	}

	/**
	 * Returns the short name and identifier of the parameter
	 * @return the short name and identifier of the parameter
	 */
	public String name() {
		return name;
	}

	/**
	 * Returns the group of the parameter
	 * @return the group of the parameter
	 */
	public ParameterGroup getGroup() {
		return group;
	}

	/**
	 * Returns the full description of the parameter
	 * @return the full description of the parameter
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the reference from which this parameter was estimated/taken
	 * @return the reference from which this parameter was estimated/taken
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Returns the year when the parameter was originally estimated
	 * @return the year when the parameter was originally estimated
	 */
	public int getYear() {
		return year;
	}

    /**
     * Returns the model this component belongs to
     * @return the model this component belongs to
     */
    public HTAModel getModel() {
        return model;
    }

	/**
	 * Calculates and returns the value of a parameter for a patient at a specific simulation timestamp
	 * @param pat A patient
	 * @return the value of a parameter for a patient at a specific simulation timestamp
	 */
	public abstract double getValue(Patient pat);

    @Override
    public String getUsedParameterName(ParameterTemplate param) {
        return usedParameterNames.get(param);
    }

    @Override
    public void setUsedParameterName(ParameterTemplate param, String name) {
        usedParameterNames.put(param, name);
    }

    @Override
    public Map<ParameterTemplate, String> getUsedParameterNames() {
        return usedParameterNames;
    }

    @Override
    public void registerUsedParameter(ParameterTemplate param) {
        setUsedParameterName(param, param.createName(this));
    }
	
	@Override
	public String prettyPrint(String linePrefix) {
		StringBuilder sb = new StringBuilder(linePrefix).append(name);
		return sb.toString();		
	}

	@Override
	public int compareTo(Parameter o) {
		return name.compareTo(o.name);
	}
	
	/**
     * Creates a uniform distribution to add uncertainty to a deterministic probability. Uses the {@link BasicConfigParams#DEF_SECOND_ORDER_VARIATION} 
     * parameters to adjust the uncertainty
     * @param detProb Deterministic probability
     * @return a uniform distribution that represents the uncertainty around a probability parameter
     */
    public static RandomVariate getRandomVariateForProbability(double detProb) {
    	if (detProb == 0.0) {
    		return RandomVariateFactory.getInstance("ConstantVariate", detProb);
    	}
    	final double instRate = -Math.log(1 - detProb);
    	return RandomVariateFactory.getInstance("UniformVariate", 1 - Math.exp(-instRate * (1 - Parameter.DEF_SECOND_ORDER_VARIATION.PROBABILITY)), 1 - Math.exp(-instRate * (1 + Parameter.DEF_SECOND_ORDER_VARIATION.PROBABILITY)));
    }

    /**
     * Creates a Gamma distribution to add uncertainty to a deterministic cost. Uses the {@link BasicConfigParams#DEF_SECOND_ORDER_VARIATION} 
     * parameters to adjust the uncertainty
     * @param detCost Deterministic cost
     * @return a Gamma random distribution that represents the uncertainty around a cost
     */
    public static RandomVariate getRandomVariateForCost(double detCost) {
    	if (detCost == 0.0) {
    		return RandomVariateFactory.getInstance("ConstantVariate", detCost);
    	}
    	final double costVariance2 = Parameter.DEF_SECOND_ORDER_VARIATION.COST * Parameter.DEF_SECOND_ORDER_VARIATION.COST;
    	final double invCostVariance2 = 1 / costVariance2;
    	return RandomVariateFactory.getInstance("GammaVariate", invCostVariance2, costVariance2 * detCost);
    }

    /**
	 * Creates a string that contains a tab separated list of the parameter names defined in this repository
	 * @return a string that contains a tab separated list of the parameter names defined in this repository
	 */
	public static String getStrHeader() {
		StringBuilder str = new StringBuilder();
		for (ParameterGroup group : ParameterGroup.values()) {
			for (Parameter param : parametersByType.get(group).values()) {
				if (param instanceof SecondOrderNatureParameter) {
					str.append(param.name()).append("\t");
				}
			}
		}
		return str.toString();
	}
	
	public static String prettyPrintAll(String linePrefix) {
		StringBuilder str = new StringBuilder();
		for (ParameterGroup group : ParameterGroup.values()) {
			for (Parameter param : parametersByType.get(group).values()) {
				str.append(param.prettyPrint(linePrefix)).append("\n");
			}
		}
		return str.toString();
	}
	
	public static String print(int id) {
		StringBuilder str = new StringBuilder();
		for (ParameterGroup group : ParameterGroup.values()) {
			for (Parameter param : parametersByType.get(group).values()) {
				if (param instanceof SecondOrderNatureParameter)
					str.append(((SecondOrderNatureParameter)param).getValue(id)).append("\t");
			}
		}
		return str.toString();
	}

	/**
	 * Removes all the defined parameters
	 */
	public static void resetAll() {
		for (ParameterGroup group : ParameterGroup.values()) {
			parametersByType.get(group).clear();
		}
	}

	public static Map<String, Parameter> getParametersByType(ParameterGroup group) {
		return parametersByType.get(group);
	}
}