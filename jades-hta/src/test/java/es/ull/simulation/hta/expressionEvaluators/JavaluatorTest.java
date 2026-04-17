/**
 * 
 */
package es.ull.simulation.hta.expressionEvaluators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.fathzer.soft.javaluator.AbstractVariableSet;
import com.fathzer.soft.javaluator.DoubleEvaluator;

/**
 * @author Iván Castilla
 *
 */
public class JavaluatorTest {
	private final static String[] TEST_STR = {
		"T1DM_Manif_ALB1_Incidence10 + T1DM_Manif_ALB1_Beta",
		"T1DM_Manif_ALB1_Incidence10 * T1DM_Manif_ALB1_Beta",
		"T1DM_Manif_ALB1_Incidence10 * ((Attribute_HbA1c / 10) ^ T1DM_Manif_ALB1_Beta)"
		};
		private final static double[] TEST_RESULT = {2.01, 0.02, 0.0025};
//	private final static Operator REF = new Operator("#", 1, Operator.Associativity.RIGHT, 0);
	
//	private final static Parameters PARAMETERS = DoubleEvaluator.getDefaultParameters();
	private final static TreeMap<String,Double> PAIRS = new TreeMap<>();
	
	static {
//		PARAMETERS.add(REF);
		PAIRS.put("T1DM_Manif_ALB1_Incidence10", 0.01);
		PAIRS.put("Attribute_HbA1c", 5.0);
		PAIRS.put("T1DM_Manif_ALB1_Beta", 2.0);
	}
	
	private final static AbstractVariableSet<Double> CONTEXT = new AbstractVariableSet<Double>() {

		@Override
		public Double get(String variableName) {
			return PAIRS.get(variableName);
		}
		
	};

	@Test
	public void test() {
		final DoubleEvaluator evaluator = new DoubleEvaluator();
		for (int i = 0; i < TEST_STR.length; i++) {
			assertEquals(TEST_RESULT[i], evaluator.evaluate(TEST_STR[i], CONTEXT));
		}
	}
	
}
