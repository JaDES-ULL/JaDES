package es.ull.simulation.hta.interventions;

import es.ull.simulation.hta.Named;

/**
 * The possible results of a diagnostic/screening test
 * @author Iván Castilla Rodríguez
 *
 */
public enum DetectionTestResult implements Named {
	TP,
	FP,
	TN,
	FN
}