package es.ull.simulation.hta.inforeceiver;

public interface TxtProducer extends OutputProducer {
	String DEFAULT_SEP = "\t";
	
	default String getSeparatorString() {
		return DEFAULT_SEP;
	}

    /**
	 * Returns a string with the header of the results
	 * @return A string with the header of the results
	 */
	public String getStrHeader();

	/**
	 * Returns a string with the results
	 * @return A string with the results
	 */
	public String getFormattedResult();    
}
