package es.ull.simulation.hta.osdi.des;

import java.util.Optional;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.DetectionInterventionWrapper;
import es.ull.simulation.hta.osdi.ontology.DetectionStrategyWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionType;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.StrategyType;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;
import es.ull.simulation.hta.params.StandardParameter;

/**
 * A detection intervention (screening or diagnosis) in the discrete-event simulation. Its sensitivity and specificity are those of the detection
 * strategy of its arm (decision D2 of docs/osdi-1.3-diseno.md): the screening strategy for a screening intervention, or the diagnosis strategy for a
 * diagnosis intervention. Such strategy is searched first among the strategies that the intervention sets through its strategy replacement effects,
 * and then among the strategies of the diseases of the model. If there is none, or it does not define them, they are assumed to be perfect (1).
 * The DES does not simulate the strategies themselves (their costs are not applied).
 */
public class OSDiDetectionIntervention extends OSDiIntervention {
	private final static OSDiLogger log = OSDiLogger.getLogger(OSDiDetectionIntervention.class);
	/**
	 * The detection strategy of the arm of this intervention, if any.
	 */
	private final Optional<DetectionStrategyWrapper> detectionStrategy;

	public OSDiDetectionIntervention(OSDiDESModel model, DetectionInterventionWrapper interventionWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		super(model, interventionWrapper);
		this.detectionStrategy = findDetectionStrategy(interventionWrapper);
	}

	/**
	 * Finds the detection strategy of the arm of a detection intervention.
	 * @param interventionWrapper The detection intervention
	 * @return The detection strategy of the arm, if any
	 * @throws MalformedOSDiModelException If the ontology is malformed
	 * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered
	 */
	private static Optional<DetectionStrategyWrapper> findDetectionStrategy(DetectionInterventionWrapper interventionWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final StrategyType type = InterventionType.SCREENING.equals(interventionWrapper.getInterventionType()) ? StrategyType.SCREENING_STRATEGY : StrategyType.DIAGNOSIS_STRATEGY;
		for (EffectWrapper effect : interventionWrapper.getEffects()) {
			for (StrategyWrapper strategy : effect.getReplacementStrategies()) {
				if (type.equals(strategy.getStrategyType()) && strategy instanceof DetectionStrategyWrapper detection) {
					return Optional.of(detection);
				}
			}
		}
		for (DiseaseWrapper disease : interventionWrapper.getModelWrapper().getDiseaseIndividuals()) {
			for (StrategyWrapper strategy : disease.getStrategies()) {
				if (type.equals(strategy.getStrategyType()) && strategy instanceof DetectionStrategyWrapper detection) {
					return Optional.of(detection);
				}
			}
		}
		return Optional.empty();
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
