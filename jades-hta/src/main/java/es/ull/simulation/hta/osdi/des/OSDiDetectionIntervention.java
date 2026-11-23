package es.ull.simulation.hta.osdi.des;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.DetectionStrategyWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.StrategyReplacements;
import es.ull.simulation.hta.osdi.ontology.StrategyType;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;
import es.ull.simulation.hta.params.StandardParameter;

/**
 * A detection intervention (screening or diagnosis) in the discrete-event simulation. Its sensitivity and specificity are those of the detection
 * strategy of its arm (decision D2 of docs/osdi-1.3-diseno.md): the screening strategy for a screening intervention, or the diagnosis strategy for a
 * diagnosis intervention. The strategies of the arm are those of the disease after the strategy replacements of the intervention, by type, as in
 * decision trees. More than one strategy of the type in the arm is not supported. If there is none, or it does not define them, they are assumed to
 * be perfect (1). The costs of the detection strategies are not applied yet.
 */
public class OSDiDetectionIntervention extends OSDiIntervention {
	private final static OSDiLogger log = OSDiLogger.getLogger(OSDiDetectionIntervention.class);
	/**
	 * The detection strategy of the arm of this intervention, if any.
	 */
	private final Optional<DetectionStrategyWrapper> detectionStrategy;

	public OSDiDetectionIntervention(OSDiDESModel model, DetectionInterventionWrapper interventionWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		super(model, interventionWrapper);
		// The sensitivity and specificity must be registered, so that the values assumed if the strategy does not define them are added to the model
		// (see createParameters)
		registerUsedParameter(StandardParameter.SENSITIVITY);
		registerUsedParameter(StandardParameter.SPECIFICITY);
		this.detectionStrategy = findDetectionStrategy(interventionWrapper);
		// TODO: Support explicit detection outcomes in the DES (see docs/known-limitations.md)
		if (detectionStrategy.isPresent() && detectionStrategy.get().hasExplicitOutcomes()) {
			throw new UnsupportedOSDiFeatureException("The detection strategy " + detectionStrategy.get().getShortName() + " of the arm of " + interventionWrapper.getShortName()
				+ " defines explicit outcomes, which are not supported yet in discrete event simulation models. Use the short form (sensitivity and specificity).");
		}
	}

	/**
	 * Finds the detection strategy of the arm of a detection intervention: among the strategies of the disease after the strategy replacements of
	 * the intervention, by type (see {@link StrategyReplacements#replaceByType}), the screening strategy for a screening intervention or the diagnosis
	 * strategy for a diagnosis intervention.
	 * @param interventionWrapper The detection intervention
	 * @return The detection strategy of the arm, if any
	 * @throws MalformedOSDiModelException If the ontology is malformed
	 * @throws UnsupportedOSDiFeatureException If the arm has more than one detection strategy of the type
	 */
	private static Optional<DetectionStrategyWrapper> findDetectionStrategy(DetectionInterventionWrapper interventionWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final StrategyType type = InterventionType.SCREENING.equals(interventionWrapper.getInterventionType()) ? StrategyType.SCREENING_STRATEGY : StrategyType.DIAGNOSIS_STRATEGY;
		// The DES uses a single disease (see OSDiDESModel)
		final DiseaseWrapper disease = interventionWrapper.getModelWrapper().getDiseaseIndividuals().iterator().next();
		final Set<StrategyWrapper> armStrategies = StrategyReplacements.replaceByType(disease.getStrategies(),
			StrategyReplacements.of(interventionWrapper).get(disease.getIndividualIRI()));
		final List<DetectionStrategyWrapper> found = new ArrayList<>();
		for (StrategyWrapper strategy : armStrategies) {
			if (type.equals(strategy.getStrategyType()) && strategy instanceof DetectionStrategyWrapper detection) {
				found.add(detection);
			}
		}
		if (found.size() > 1) {
			final String typeName = StrategyType.SCREENING_STRATEGY.equals(type) ? "screening" : "diagnosis";
			throw new UnsupportedOSDiFeatureException("The arm of the detection intervention " + interventionWrapper.getShortName() + " has " + found.size() + " " + typeName
				+ " strategies (" + found + "). Only one screening strategy and one diagnosis strategy per arm are supported.");
		}
		return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
	}

	/**
	 * The one-time payoffs (cost and disutility) of a detection intervention are applied with each test, not when the patient starts the simulation.
	 * TODO: This class does not schedule detection events yet (see docs/osdi-1.3-diseno.md, phase 14), so such payoffs are never applied.
	 */
	@Override
	public boolean appliesOneTimePayoffsAtStart() {
		return false;
	}

	public OSDiDESModel getModel() {
		return (OSDiDESModel) super.getModel();
	}

	@Override
	public void createParameters() {
		super.createParameters();
		final Optional<ParameterWrapper> sensitivity = detectionStrategy.isPresent() ? detectionStrategy.get().getSensitivityParameter() : Optional.empty();
		final Optional<ParameterWrapper> specificity = detectionStrategy.isPresent() ? detectionStrategy.get().getSpecificityParameter() : Optional.empty();
		if (sensitivity.isEmpty()) {
			log.warn("No sensitivity defined for the detection strategy of the arm of " + getInterventionWrapper().getShortName() + ". Assuming perfect sensitivity = 1.0.");
			addUsedParameter(StandardParameter.SENSITIVITY, "Assumed sensitivity", "Assumption", 1.0);
		}
		else
			addUsedParameter(StandardParameter.SENSITIVITY, ParameterFactory.getParameterInstance(getModel(), sensitivity.get(), StandardParameter.SENSITIVITY.getGroup()));
		if (specificity.isEmpty()) {
			log.warn("No specificity defined for the detection strategy of the arm of " + getInterventionWrapper().getShortName() + ". Assuming perfect specificity = 1.0.");
			addUsedParameter(StandardParameter.SPECIFICITY, "Assumed specificity", "Assumption", 1.0);
		}
		else
			addUsedParameter(StandardParameter.SPECIFICITY, ParameterFactory.getParameterInstance(getModel(), specificity.get(), StandardParameter.SPECIFICITY.getGroup()));
	}

}
