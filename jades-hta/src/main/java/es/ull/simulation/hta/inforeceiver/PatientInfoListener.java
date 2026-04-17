/**
 * 
 */
package es.ull.simulation.hta.inforeceiver;

import java.io.PrintStream;

import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.inforeceiver.BasicListener;

/**
 * A listener that prints all the information received regarding a specific patient.
 * 
 * @author Iván Castilla Rodríguez
 *
 */
public class PatientInfoListener extends BasicListener {
	/** The output stream */
	private final PrintStream out;
	/** The identifier of the patient to be followed up */
	private final int patientId;
	
	/**
	 * Creates a listener that prints all the information received regarding a specific patient.
	 * If the patient identifier is -1, the information of all patients will be printed.
	 * 
	 * @param out The output stream where the information will be printed.
	 * @param patientId The identifier of the patient to be followed up.
	 */
	public PatientInfoListener(PrintStream out, int patientId) {
		super("Standard patient viewer");
		addTargetInformation(PatientInfo.class);
		this.patientId = patientId;
		this.out = out;
	}

	/**
	 * Creates a listener that prints all the information received regarding a specific patient to the standard output.
	 * If the patient identifier is -1, the information of all patients will be printed.
	 * 
	 * @param patientId The identifier of the patient to be followed up.
	 */
	public PatientInfoListener(int patientId) {
		this(System.out, patientId);
	}

	/**
	 * Creates a listener that prints all the information received regarding all patients to the standard output.
	 */
	public PatientInfoListener() {
		this(System.out, -1);
	}

	@Override
	public void infoEmited(IPieceOfInformation info) {
		if (info instanceof PatientInfo) {
			if (patientId == -1 || patientId == ((PatientInfo)info).getPatient().getIdentifier())
				out.println(info.toString());
		}
		
	}
}
