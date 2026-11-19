package es.ull.simulation.hta.osdi.des;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeSet;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionType;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.EpidemiologicCharacterizationType;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionTargets;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.DeterministicParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SecondOrderUncertaintyParameterData;
import es.ull.simulation.hta.osdi.ontology.PathwayWrapper;
import es.ull.simulation.hta.osdi.ontology.PopulationWrapper;

/**
 * The target of the interventions in a discrete event simulation model (decision D1 of docs/osdi-1.3-diseno.md). Time 0 is the moment when the
 * intervention is applied, and the population describes the patients at that moment. Hence, if the interventions target a clinical progression
 * element T instead of the disease, every patient presents T at time 0 and the intervention is active during the whole simulation:
 * <ul>
 * <li>If T is a chronic manifestation, every patient starts with T (see {@link OSDiDiseaseProgression}). The onset of T is not simulated, its risk
 * is ignored, and the progressions from T are planned from time 0, as if T had just appeared. Its one-time payoffs apply at time 0, as in decision
 * trees, and its annual payoffs from time 0.</li>
 * <li>If T is an acute manifestation, every patient suffers an episode at time 0, and the next ones come from the frequency of T (hasFrequency) or,
 * if it has none, from its risk (see {@link es.ull.simulation.hta.osdi.des.factories.DiseaseFactory}). The one-time payoffs of the intervention
 * apply with each episode, and its annual payoffs are ignored with a warning (see {@link OSDiIntervention}).</li>
 * <li>The other progressions present at time 0 are only those with an initial proportion declared by the population (hasInitialProportion): JaDES
 * does not deduce the progressions that precede T.</li>
 * </ul>
 * The population cannot contradict the target: an explicit initial proportion of T different from 1, a prevalence of the disease lower than 1, an
 * incidence of the disease, or a progression with an initial proportion that supersedes T are malformed. If the population does not declare the
 * initial proportion of T, it is assumed to be 1 with a warning. Developments, stages, manifestations that depend on a development and targets with
 * a temporal constraint (onset or end age) are not supported.
 */
public final class DESInterventionTarget {
	private final static OSDiLogger log = OSDiLogger.getLogger(DESInterventionTarget.class);

	private DESInterventionTarget() {
	}

	/**
	 * Resolves the target of the interventions of a model (see {@link InterventionTargets#resolve}) and checks that the discrete event simulation
	 * supports it and that the population does not contradict it.
	 * @param modelWrapper The model
	 * @param diseaseWrapper The disease of the model
	 * @param populationWrapper The population of the model
	 * @return The clinical progression element that the interventions target, or an empty optional if they target the disease
	 * @throws MalformedOSDiModelException If the target is not valid or the population contradicts it
	 * @throws UnsupportedOSDiFeatureException If the target is not supported by the discrete event simulation
	 */
	public static Optional<ClinicalProgressionWrapper> resolve(ModelWrapper modelWrapper, DiseaseWrapper diseaseWrapper, PopulationWrapper populationWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final Optional<ClinicalProgressionWrapper> resolved = InterventionTargets.resolve(modelWrapper, diseaseWrapper);
		if (resolved.isEmpty()) {
			return resolved;
		}
		final ClinicalProgressionWrapper target = resolved.get();
		checkSupported(diseaseWrapper, target);
		checkEpidemiologicalParameters(diseaseWrapper, populationWrapper, target);
		checkInitialProportions(populationWrapper, target);
		warnAboutPreviousProgressions(populationWrapper, target);
		return resolved;
	}

	private static void checkSupported(DiseaseWrapper diseaseWrapper, ClinicalProgressionWrapper target) throws UnsupportedOSDiFeatureException {
		final ClinicalProgressionType type = target.getClinicalProgressionType();
		if (ClinicalProgressionType.DEVELOPMENT.equals(type)) {
			throw new UnsupportedOSDiFeatureException("The interventions of the model target the development " + target.getShortName()
				+ ". Developments are not simulated in discrete event simulation models.");
		}
		if (ClinicalProgressionType.STAGE.equals(type)) {
			throw new UnsupportedOSDiFeatureException("The interventions of the model target the stage " + target.getShortName()
				+ ". Stages are not supported as the target of the interventions in discrete event simulation models.");
		}
		for (ClinicalProgressionWrapper development : diseaseWrapper.getDevelopments()) {
			if (diseaseWrapper.getManifestationsLinkedToDevelopment(development).contains(target)) {
				throw new UnsupportedOSDiFeatureException("The interventions of the model target the manifestation " + target.getShortName() + ", which depends on the development "
					+ development.getShortName() + ". A manifestation that depends on a development is not supported as the target of the interventions in discrete event simulation models.");
			}
		}
		if (target.getTemporalConstraintForProgression().isPresent()) {
			throw new UnsupportedOSDiFeatureException("The interventions of the model target " + target.getShortName() + ", which defines a temporal constraint (onset or end age)."
				+ " Every patient presents the target of the interventions at time 0, so such constraint is not supported in discrete event simulation models.");
		}
	}

	/**
	 * Checks that the epidemiological parameters of the population do not contradict the target: every patient has the disease at time 0
	 */
	private static void checkEpidemiologicalParameters(DiseaseWrapper diseaseWrapper, PopulationWrapper populationWrapper, ClinicalProgressionWrapper target) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		for (ParameterWrapper param : populationWrapper.getEpidemiologicalCharacterizationOfDisease(diseaseWrapper)) {
			final EpidemiologicCharacterizationType type = EpidemiologicCharacterizationType.fromDataItemType(param.getDataItemType());
			if (EpidemiologicCharacterizationType.INCIDENCE.equals(type)) {
				throw new MalformedOSDiModelException("The population " + populationWrapper.getShortName() + " declares an incidence (" + param.getShortName() + ") of the disease "
					+ diseaseWrapper.getShortName() + ", but the interventions target " + target.getShortName() + ": every patient presents it when the intervention is applied (time 0).");
			}
			if (EpidemiologicCharacterizationType.PREVALENCE.equals(type) || EpidemiologicCharacterizationType.BIRTH_PREVALENCE.equals(type)) {
				final OptionalDouble value = getExpectedValue(param);
				if (value.isEmpty()) {
					log.warn("The " + type.getLabel().toLowerCase() + " " + param.getShortName() + " of the disease " + diseaseWrapper.getShortName() + " cannot be checked. Every patient is assumed to present "
						+ target.getShortName() + ", the target of the interventions, at time 0.");
				}
				else if (value.getAsDouble() < 1.0) {
					throw new MalformedOSDiModelException("The population " + populationWrapper.getShortName() + " declares a " + type.getLabel().toLowerCase() + " of " + value.getAsDouble()
						+ " for the disease " + diseaseWrapper.getShortName() + ", but the interventions target " + target.getShortName() + ": every patient presents it when the intervention is applied (time 0).");
				}
			}
		}
	}

	/**
	 * Checks the initial proportions declared by the population: that of the target must be 1 (assumed with a warning if absent), and no progression
	 * present at time 0 can supersede the target
	 */
	private static void checkInitialProportions(PopulationWrapper populationWrapper, ClinicalProgressionWrapper target) throws MalformedOSDiModelException {
		final Map<ClinicalProgressionWrapper, ParameterWrapper> initialProportions = populationWrapper.getInitialProportions();
		final ParameterWrapper targetProportion = initialProportions.get(target);
		if (targetProportion == null) {
			log.warn("The population " + populationWrapper.getShortName() + " does not declare the initial proportion of " + target.getShortName()
				+ " (hasInitialProportion), the target of the interventions. Every patient is assumed to present it at time 0.");
		}
		else {
			final OptionalDouble value = getExpectedValue(targetProportion);
			if (value.isEmpty()) {
				log.warn("The initial proportion " + targetProportion.getShortName() + " of " + target.getShortName() + " cannot be checked. Every patient is assumed to present it at time 0, since it is the target of the interventions.");
			}
			else if (value.getAsDouble() != 1.0) {
				throw new MalformedOSDiModelException("The population " + populationWrapper.getShortName() + " declares an initial proportion of " + value.getAsDouble() + " for " + target.getShortName()
					+ " (" + targetProportion.getShortName() + "), but it is the target of the interventions: every patient presents it when the intervention is applied (time 0).");
			}
		}
		for (ClinicalProgressionWrapper progression : initialProportions.keySet()) {
			if (!progression.equals(target) && progression.getSupersededProgressions().contains(target)) {
				throw new MalformedOSDiModelException("The population " + populationWrapper.getShortName() + " declares an initial proportion for " + progression.getShortName() + ", which supersedes "
					+ target.getShortName() + ", the target of the interventions: every patient presents the target at time 0.");
			}
		}
	}

	/**
	 * Warns if the target can only be reached from progressions whose initial proportion is not declared, since JaDES does not deduce them
	 */
	private static void warnAboutPreviousProgressions(PopulationWrapper populationWrapper, ClinicalProgressionWrapper target) {
		final Set<IModelItemWrapper> risks = target.getProgressionRisks();
		if (risks.isEmpty()) {
			return;
		}
		final Set<ClinicalProgressionWrapper> undeclared = new TreeSet<>();
		for (IModelItemWrapper risk : risks) {
			if (!(risk instanceof PathwayWrapper pathway)) {
				return;
			}
			final Set<ClinicalProgressionWrapper> missing = new TreeSet<>(pathway.getRequiredClinicalProgressions());
			missing.removeAll(populationWrapper.getInitialProportions().keySet());
			if (missing.isEmpty()) {
				return;
			}
			undeclared.addAll(missing);
		}
		log.warn(target.getShortName() + ", the target of the interventions, can only be reached from progressions without an initial proportion declared by the population " + populationWrapper.getShortName()
			+ " (" + undeclared + "). JaDES does not deduce them: at time 0, the patients only present the target and the progressions with a declared initial proportion.");
	}

	/**
	 * Returns the expected value of a deterministic or second-order parameter
	 * @param param A parameter
	 * @return The expected value of the parameter, or an empty optional if it is a first-order or calculated parameter
	 */
	private static OptionalDouble getExpectedValue(ParameterWrapper param) {
		if (param.getParameterNatureData() instanceof DeterministicParameterData data) {
			return OptionalDouble.of(data.value());
		}
		if (param.getParameterNatureData() instanceof SecondOrderUncertaintyParameterData data) {
			return OptionalDouble.of(data.expectedValue());
		}
		return OptionalDouble.empty();
	}
}
