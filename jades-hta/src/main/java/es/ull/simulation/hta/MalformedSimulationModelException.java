package es.ull.simulation.hta;

/**
 * An exception related to a malformed simulation model.
 * This exception is thrown when the simulation model does not conform to the expected structure or content.
 */
public class MalformedSimulationModelException extends Exception {
	private static final long serialVersionUID = 7167363294337270171L;

	public MalformedSimulationModelException(String message, Throwable cause) {
		super("The model was incomplete or malformed: " + message, cause);
	}

	public MalformedSimulationModelException(String message) {
		super("The model was incomplete or malformed: " + message);
	}
			
}