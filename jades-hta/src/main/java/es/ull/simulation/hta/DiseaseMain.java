/**
 * 
 */
package es.ull.simulation.hta;

import java.io.IOException;

import org.slf4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;

import es.ull.simulation.experiment.CommonArgumentsDefaultProvider;
import es.ull.simulation.hta.config.BaseHTAExperimentConfigProvider;
import es.ull.simulation.hta.config.JsonOutputConfigProvider;
import es.ull.simulation.hta.diab.T1DMModel;
import es.ull.simulation.hta.pbdmodel.PBDModel;
import es.ull.simulation.hta.simpletest.TestSimpleRareDiseaseModel;

/**
 * Main class to launch simulation experiments
 * 
 * @author Iván Castilla Rodríguez
 *
 */
public class DiseaseMain extends HTAExperiment {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(DiseaseMain.class);
	
	private final static int TEST_RARE_DISEASE1 = 1; 
	private final static int TEST_RARE_DISEASE2 = 2; 
	private final static int TEST_RARE_DISEASE3 = 3; 
	private final static int TEST_RARE_DISEASE4 = 4; 
	private final static int TEST_PBD = 5;
	private final static int TEST_T1DM_DCCT1 = 10;
	private final static int TEST_T1DM_DCCT2 = 11;
	private final static int TEST_T1DM_DCCT1_CONV = 12;
	private final static int TEST_T1DM_DCCT1_INTENS = 13;
	
	private final static boolean ALL_AFFECTED = true;
	// private final static String PARAMS = "-n 1000 -r 0 -t 0 -dis 12 -y 2019 -q -ep cr -po"; // Testing diabetes
//	private final static String PARAMS = "-n 1000 -r 0 -t 0 -dis " + TEST_T1DM_DCCT1_CONV + " -y 2019 -q -ep cr"; // Testing conventional DCCT
	//private final static String PARAMS = "-n 5000 -r 2 -t 0 -dis " + TEST_PBD + " -dr 0 -ep pr"; // Testing test diseases
//	private final static String PARAMS = "-n 5000 -r 0 -t 0 -dis " + TEST_PBD + " -c config_template.json -q"; // Testing test diseases
//	private final static String PARAMS = "-n 1 -r 0 -t 0 -dis " + TEST_PBD + " -dr 0 -ep ia -q -ps 0"; // Testing test diseases
//	private final static String PARAMS = "-n 100 -r 0 -dr 0 -q -t 0 -dis 1 -ps 3 -po"; // -o /tmp/result_david.txt
	private final static String PARAMS = "-n 1000 -r 0 -dr 0 -q -t 1 -mod T1DM_StdModelDES -po"; // -o /tmp/result_david.txt
	private final int disease;

	public DiseaseMain(int disease, BaseHTAExperimentConfigProvider configProvider, JsonOutputConfigProvider outputConfigProvider) throws MalformedSimulationModelException, IOException {
		super(configProvider, outputConfigProvider);
		this.disease = disease;
	}
	
	public static class Arguments {
        @ParametersDelegate
        public HTAArguments htaArguments = new HTAArguments();
		@Parameter(names = { "--disease", "-dis" }, description = "Disease to test with (1-4: Synthetic diseases; " 
			+ TEST_PBD + ": PBD; "
			+ TEST_T1DM_DCCT1 + ": T1DM (DCCT Primary cohort)"
			+ TEST_T1DM_DCCT2 + ": T1DM (DCCT Secondary cohort)"
			+ TEST_T1DM_DCCT1_CONV + ": T1DM (DCCT Primary cohort conventional therapy)"
			+ TEST_T1DM_DCCT1_INTENS + ": T1DM (DCCT Primary cohort intensive therapy)"
			+ ")", order = 3)
		public int disease = 1;
	}

	@Override
	public HTAModel createModel() throws MalformedSimulationModelException {
		HTAModel model = null;
		switch(disease) {
		case TEST_RARE_DISEASE1:
		case TEST_RARE_DISEASE2:
		case TEST_RARE_DISEASE3:
		case TEST_RARE_DISEASE4:
			log.info(String.format("\n\nExecuting the PROGRAMMATIC test for the rare disease [%d] \n\n", disease));
			model = new TestSimpleRareDiseaseModel(this, disease);
			break;
		case TEST_T1DM_DCCT1:
			log.info(String.format("\n\nExecuting the PROGRAMMATIC test for T1DM (DCCT primary cohort) \n\n"));
			model = new T1DMModel(this, T1DMModel.ModelConfig.DCCT1);
			break;
		case TEST_T1DM_DCCT2:
			log.info(String.format("\n\nExecuting the PROGRAMMATIC test for T1DM (DCCT secondary cohort) \n\n"));
			model = new T1DMModel(this, T1DMModel.ModelConfig.DCCT2);
			break;
		case TEST_T1DM_DCCT1_CONV:
			log.info(String.format("\n\nExecuting the PROGRAMMATIC test for T1DM (DCCT primary cohort, conventional therapy) \n\n"));
			model = new T1DMModel(this, T1DMModel.ModelConfig.DCCT1_CONV);
			break;
		case TEST_T1DM_DCCT1_INTENS:
			log.info(String.format("\n\nExecuting the PROGRAMMATIC test for T1DM (DCCT primary cohort, intensive therapy) \n\n"));
			model = new T1DMModel(this, T1DMModel.ModelConfig.DCCT1_INTENS);
			break;
		case TEST_PBD:
		default:
			log.info(String.format("\n\nExecuting the PROGRAMMATIC test for the rare disease PBD \n\n"));
			model = new PBDModel(this, ALL_AFFECTED);

			break;				
		}			
		return model;
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		final Arguments arguments = new Arguments();
		try {
			final JCommander jc = JCommander.newBuilder().addObject(arguments).defaultProvider(new CommonArgumentsDefaultProvider()).build();
			try {
				jc.parse((PARAMS == "") ? args : PARAMS.split(" "));
			} catch (Exception e) {
				log.error("Error parsing arguments: " + e.getMessage());
				jc.usage();
				System.exit(-1);
			}
			BaseHTAExperimentConfigProvider configProvider = new BaseHTAExperimentConfigProvider(arguments.htaArguments);
			JsonOutputConfigProvider outputConfigProvider = null;
			final DiseaseMain experiment = new DiseaseMain(arguments.disease, configProvider, outputConfigProvider);
			experiment.initializeModel();
			log.info("=====================================================================================================");
			log.info(es.ull.simulation.hta.params.Parameter.prettyPrintAll(""));
			log.info("=====================================================================================================");
			experiment.run();
		} catch (Exception e) {
			log.error("An error occurred during execution", e);
		}
	}
}
