package es.ull.simulation.hta.osdi.ontology;

import java.util.Objects;

import org.semanticweb.owlapi.model.IRI;

/**
 * An enumeration that helps handle the different types of instances in the OSDi ontology.
 * It provides the information to get the IRI of instances for each concept.
 */
public enum InstanceIRITemplate {
	// In general, attributes do not use the prefix of the working model, since they should be defined for every disease and model
	ATTRIBUTE("Attribute_", ""),
	DISEASE("", ""),
	EXPRESSION("", "_Expression"),
	INTERVENTION("Intervention_", ""),
	EFFECT("Effect_", ""),
	DEVELOPMENT("Development_", ""),
	DEVELOPMENT_PATHWAY("Development_Pathway_", ""),
	MANIFESTATION("Manif_", ""),
	MANIFESTATION_GROUP("Group_Manif_", ""),
	MANIFESTATION_PATHWAY("Manif_Pathway_", ""),
	PARAMETER("", ""),
	POPULATION("Population_", ""),
	STAGE("Stage_", ""),
	STAGE_PATHWAY("Stage_Pathway_", ""),
	PARAM_ANNUAL_COST("", "_AC"),
	PARAM_DEATH_PROBABILITY("", "_ProbDeath"),
	PARAM_INCREASED_MORTALITY_RATE("", "_IMR"),
	PARAM_INCIDENCE("", "_Incidence"),
	PARAM_ONE_TIME_COST("", "_TC"),
	PARAM_PREVALENCE("", "_Prevalence"),
	PARAM_PROPORTION("", "_Proportion"),
	PARAM_RELATIVE_RISK("", "_RR"),
	PARAM_UTILITY("", "_U"),
	UNCERTAINTY_HETEREOGENEITY("", "_Heterogeneity"),
	UNCERTAINTY_L95CI("", "_L95CI"),
	UNCERTAINTY_PARAM("", "_ParamUncertainty"),
	UNCERTAINTY_STOCHASTIC("", "_StochasticUncertainty"),
	UNCERTAINTY_U95CI("", "_U95CI");
	final String prefix;
	final String suffix;
	private InstanceIRITemplate(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public IRI createIRI(String modelPrefix, String conceptName) {
		StringBuilder sb = new StringBuilder();
		if (modelPrefix != null && !modelPrefix.isEmpty()) {
			sb.append(modelPrefix);
		}
		sb.append(this.prefix);
		if (conceptName != null && !conceptName.isEmpty()) {
			sb.append(conceptName);
		}
		sb.append(this.suffix);
		return IRI.create(Objects.requireNonNull(sb.toString()));
	}
}