package es.ull.simulation.hta.osdi.ontology;

import java.util.TreeMap;

public enum EpidemiologicCharacterizationType {
    BIRTH_PREVALENCE(OSDiDataItemType.DI_BIRTH_PREVALENCE, "Birth prevalence"),
    PREVALENCE(OSDiDataItemType.DI_PREVALENCE, "Prevalence"),
    INCIDENCE(OSDiDataItemType.DI_INCIDENCE, "Incidence");

	private static final TreeMap<OSDiDataItemType, EpidemiologicCharacterizationType> reverseEpidemiologicalCharacterization = new TreeMap<>();
	static {
		for (EpidemiologicCharacterizationType epidem : EpidemiologicCharacterizationType.values()) {
			reverseEpidemiologicalCharacterization.put(epidem.getType(), epidem);
		}
	}

    private final OSDiDataItemType type;
    private final String label;

    private EpidemiologicCharacterizationType(OSDiDataItemType type, String label) {
        this.type = type;
        this.label = label;
    }

    public OSDiDataItemType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getIndividualIRI() {
        return type.getShortName();
    }

    /**
     * Returns the epidemiologic characterization associated to the given data item type. 
     * @param dataType The data item type associated to the epidemiologic characterization.
     * @return The epidemiologic characterization associated to the given data item type. Null if the data item type is not associated to any epidemiologic characterization.
     */
    public static EpidemiologicCharacterizationType fromDataItemType(OSDiDataItemType dataType) {
        return reverseEpidemiologicalCharacterization.get(dataType);
    }
}