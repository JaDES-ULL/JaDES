package es.ull.simulation.hta.osdi.des;

import java.util.Set;
import java.util.TreeSet;

import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionType;

public enum DiseaseProgressionTemplate {
	// SHE("Severe Hypoglycemic Episode", DiseaseProgressionType.ACUTE_MANIFESTATION),
	ANGINA("T1DM_Manif_ANGINA", ClinicalProgressionType.CHRONIC_MANIFESTATION),
	HF("T1DM_Manif_HF", ClinicalProgressionType.CHRONIC_MANIFESTATION),
	STROKE("T1DM_Manif_STROKE", ClinicalProgressionType.CHRONIC_MANIFESTATION),
	MI("T1DM_Manif_MI", ClinicalProgressionType.CHRONIC_MANIFESTATION),
//	BGRET("T1DM_Manif_BGRET", ClinicalProgressionType.CHRONIC_MANIFESTATION),
//	PRET("T1DM_Manif_PRET", ClinicalProgressionType.CHRONIC_MANIFESTATION),
//	ME("T1DM_Manif_ME", ClinicalProgressionType.CHRONIC_MANIFESTATION),
//	BLI("T1DM_Manif_BLI", ClinicalProgressionType.CHRONIC_MANIFESTATION),
	NEU("T1DM_Manif_NEU", ClinicalProgressionType.CHRONIC_MANIFESTATION),
	LEA("T1DM_Manif_LEA", ClinicalProgressionType.CHRONIC_MANIFESTATION),
	ALB1("T1DM_Manif_ALB1", ClinicalProgressionType.CHRONIC_MANIFESTATION),
	ALB2("T1DM_Manif_ALB2", ClinicalProgressionType.CHRONIC_MANIFESTATION),
	ESRD("T1DM_Manif_ESRD", ClinicalProgressionType.CHRONIC_MANIFESTATION),
	CHD("T1DM_Stage_CHD", ClinicalProgressionType.STAGE);
	
	static {
		// Define all the exclusions among manifestations
		DiseaseProgressionTemplate.ANGINA.addExclusions(Set.of(DiseaseProgressionTemplate.HF, DiseaseProgressionTemplate.STROKE, DiseaseProgressionTemplate.MI));
		DiseaseProgressionTemplate.HF.addExclusions(Set.of(DiseaseProgressionTemplate.ANGINA, DiseaseProgressionTemplate.STROKE, DiseaseProgressionTemplate.MI));
		DiseaseProgressionTemplate.STROKE.addExclusions(Set.of(DiseaseProgressionTemplate.HF, DiseaseProgressionTemplate.ANGINA, DiseaseProgressionTemplate.MI));
		DiseaseProgressionTemplate.MI.addExclusions(Set.of(DiseaseProgressionTemplate.HF, DiseaseProgressionTemplate.STROKE, DiseaseProgressionTemplate.ANGINA));
		// DiseaseProgressionTemplate.PRET.addExclusions(Set.of(DiseaseProgressionTemplate.BGRET));
		// DiseaseProgressionTemplate.BLI.addExclusions(Set.of(DiseaseProgressionTemplate.BGRET, DiseaseProgressionTemplate.PRET, DiseaseProgressionTemplate.ME));
		DiseaseProgressionTemplate.LEA.addExclusions(Set.of(DiseaseProgressionTemplate.NEU));
		DiseaseProgressionTemplate.ALB2.addExclusions(Set.of(DiseaseProgressionTemplate.ALB1));
		DiseaseProgressionTemplate.ESRD.addExclusions(Set.of(DiseaseProgressionTemplate.ALB1, DiseaseProgressionTemplate.ALB2));
	}
	
	private final String description;
	private final ClinicalProgressionType type;
	private final Set<DiseaseProgressionTemplate> exclusions;
	/**
	 * @param description
	 * @param type
	 */
	private DiseaseProgressionTemplate(String description, ClinicalProgressionType type) {
		this.description = description;
		this.type = type;
		this.exclusions = new TreeSet<>();
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return the type
	 */
	public ClinicalProgressionType getType() {
		return type;
	}
	
	/**
	 * @return the exclusions
	 */
	public Set<DiseaseProgressionTemplate> getExclusions() {
		return exclusions;
	}		
	
	public void addExclusions(Set<DiseaseProgressionTemplate> newExclusions) {
		exclusions.addAll(newExclusions);
	}
}