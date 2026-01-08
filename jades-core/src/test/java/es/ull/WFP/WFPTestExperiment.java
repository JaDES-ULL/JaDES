/**
 * 
 */
package es.ull.WFP;

import com.beust.jcommander.JCommander;

import es.ull.StandardTestSimulation;
import es.ull.simulation.experiment.BaseExperiment;

/**
 * @author Iván Castilla Rodríguez
 *
 */
public class WFPTestExperiment extends BaseExperiment {
	final TestWFP.TestWFPArguments args;
	final int wfp;
	
	public WFPTestExperiment(TestWFP.TestWFPArguments args) {
		super("Testing WFP " + args.wfp, args);
		this.wfp = args.wfp;
		this.args = args;
	}
	
	@Override
	public void runExperiment(int ind) {
		StandardTestSimulation simul = null;
		switch (wfp) {
			case 1:		simul = new WFP01Simulation(ind, args);	break;
			case 2:		simul = new WFP02Simulation(ind, args);	break;
			case 3:		simul = new WFP03Simulation(ind, args);	break;
			case 4:		simul = new WFP04Simulation(ind, args);	break;
			case 5:		simul = new WFP05Simulation(ind, args);	break;
			case 6:		simul = new WFP06Simulation(ind, args);	break;
			case 7:		simul = new WFP07Simulation(ind, args);	break;
			case 8:		simul = new WFP08Simulation(ind, args);	break;
			case 9:		simul = new WFP09Simulation(ind, args);	break;
			case 10:	simul = new WFP10Simulation(ind, args);	break;
			case 12:	simul = new WFP12Simulation(ind, args);	break;
			case 13:	simul = new WFP13Simulation(ind, args);	break;
			case 17:	simul = new WFP17Simulation(ind, args);	break;
			case 19:	simul = new WFP19Simulation(ind, args);	break;
			case 21:	simul = new WFP21Simulation_For(ind, args);	break;
			case 211:	simul = new WFP21Simulation_WhileDo(ind, args);	break;
			case 212:	simul = new WFP21Simulation_DoWhile(ind, args);	break;
			case 28:	simul = new WFP28Simulation(ind, args);	break;
			case 30:	simul = new WFP30Simulation(ind, args);	break;
			case 40:
			default:	simul = new WFP40Simulation(ind, args);	break; 
		}
		simul.run();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final TestWFP.TestWFPArguments arguments = new TestWFP.TestWFPArguments();
		final JCommander jc = JCommander.newBuilder().addObject(arguments).build();
		jc.parse(args);

		new WFPTestExperiment(arguments).run();
	}

}
