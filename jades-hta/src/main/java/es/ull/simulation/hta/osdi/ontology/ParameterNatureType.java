package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

/**
 * A list of the different parameter natures that can be defined in OSDi. A Parameter nature determines how the parameter handles uncertainty.
 */
public enum ParameterNatureType implements WrapsOSDiClass {
	DETERMINISTIC(OSDiClass.DETERMINISTIC_PARAMETER),
	CALCULATED(OSDiClass.CALCULATED_PARAMETER),
	FIRST_ORDER(OSDiClass.FIRST_ORDER_UNCERTAINTY_PARAMETER),
	SECOND_ORDER(OSDiClass.SECOND_ORDER_UNCERTAINTY_PARAMETER);

	private final static TreeMap<OSDiClass, ParameterNatureType> reverseParameterNature = new TreeMap<>();
	static {
		for (ParameterNatureType nature : ParameterNatureType.values()) {
			reverseParameterNature.put(nature.clazz, nature);
		}
	}
	private final OSDiClass clazz;
	private ParameterNatureType(OSDiClass clazz) {
		this.clazz = clazz;
	}

	@Override
	public OSDiClass getClazz() {
		return clazz;
	}
	/**
	 * Returns the ParameterNatureWrapper corresponding to the given OSDi class.
	 * @param clazz The OSDiClasses to get the ParameterNatureWrapper for.
	 * @return The ParameterNatureWrapper associated with the given OSDiClasses.
	 */
	public static ParameterNatureType fromOSDiClass(OSDiClass clazz) {
		return reverseParameterNature.get(clazz);
	}
}