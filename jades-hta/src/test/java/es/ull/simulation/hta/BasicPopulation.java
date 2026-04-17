/**
 * 
 */
package es.ull.simulation.hta;

import es.ull.simulation.hta.params.ConstantNatureParameter;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.hta.params.StandardParameter;
import es.ull.simulation.hta.populations.Population;
import es.ull.simulation.hta.populations.StdPopulation;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.calculator.ConstantDeathSubmodel;
import es.ull.simulation.hta.progression.calculator.TimeToEventCalculator;
import simkit.random.DiscreteRandomVariate;
import simkit.random.RandomVariate;
import simkit.random.RandomVariateFactory;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class BasicPopulation extends StdPopulation {
	final public static String ATTRIBUTE_LDL = "LDL";
	final public static String ATTRIBUTE_HDL = "HDL";
	private double ldl = 85.0;
	private double hdl = 55.0;
	private int sex = 0;
	private double age = Population.DEF_MIN_AGE;
	
	/**
	 * 
	 * @param disease
	 */
	public BasicPopulation(HTAModel model, Disease disease) throws MalformedSimulationModelException {
		super(model, "TEST_POP", "Test population", disease);
	}

	@Override
	protected DiscreteRandomVariate getSexVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("DiscreteConstantVariate", getCommonRandomNumber(), sex);
	}

	@Override
	protected DiscreteRandomVariate getDiseaseVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("DiscreteConstantVariate", getCommonRandomNumber(), 1.0);
	}

	@Override
	protected DiscreteRandomVariate getDiagnosedVariate(Patient pat) {
		return RandomVariateFactory.getDiscreteRandomVariateInstance("DiscreteConstantVariate", getCommonRandomNumber(), 1.0);
	}

	@Override
	protected RandomVariate getBaselineAgeVariate(Patient pat) {
		return RandomVariateFactory.getInstance("ConstantVariate", age);
	}

	@Override
	public void createParameters() {
		model.addParameter(new ConstantNatureParameter(getModel(), ATTRIBUTE_HDL, "High Density Lipoprotein", "", ParameterGroup.ATTRIBUTE, hdl));
		model.addParameter(new ConstantNatureParameter(getModel(), ATTRIBUTE_LDL, "Low Density Lipoprotein", "", ParameterGroup.ATTRIBUTE, ldl));

		addUsedParameter(StandardParameter.POPULATION_BASE_UTILITY, "", "Assumption", 1.0);
	}

	@Override
	public TimeToEventCalculator initializeDeathCharacterization() {
		return new ConstantDeathSubmodel(getMaxAge() - getMinAge());
	}

	public double getLDL() {
		return ldl;
	}

	public void setLDL(double ldl) {
		this.ldl = ldl;
	}

	public double getHDL() {
		return hdl;
	}

	public void setHDL(double hdl) {
		this.hdl = hdl;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public void setAge(double age) {
		this.age = age;
		setMinAge(Math.min(getMinAge(), age));
	}

}
