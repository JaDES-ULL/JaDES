/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

/**
 * The listeners that implement this interface must be prepared to print results in a single line, finishing by a separator.
 * @author Iván Castilla Rodríguez
 *
 */
public interface CSVProducer extends TxtProducer {
	String STR_AVG_PREFIX = "AVG_";
	String STR_L95CI_PREFIX = "L95CI_";
	String STR_U95CI_PREFIX = "U95CI_";

	/**
	 * Returns a string with the results for a single simulation in a single line
	 * @param simulationId The id of the simulation
	 * @return A string with the results for a single simulation
	 */
	public String getPartialFormattedResult(int simulationId);    
}
