package es.ull.WFP;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.beust.jcommander.Parameter;

import es.ull.StandardTestSimulation.TestArguments;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class TestWFP {

    private final TestWFPArguments arguments = new TestWFPArguments();

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 17, 19, 21, 211, 212, 28, 30, 40})
    @DisplayName("Test all WFP")
    public void testAllWFP(int wfp) {
        arguments.wfp = wfp;
        final WFPTestExperiment exp = new WFPTestExperiment(arguments);
        exp.run();
    }
    
    public static class TestWFPArguments extends TestArguments {
    	@Parameter(names = { "--wfp", "-wfp" }, description = "Selects a specific WFP to test with", order = 5)
    	public int wfp = -1;
    }
 
    public static void main(String[] args) {
        final TestWFPArguments arguments = new TestWFPArguments();
        arguments.wfp = 1;
        final WFPTestExperiment experiment = new WFPTestExperiment(arguments);
        experiment.run();
    }
}
