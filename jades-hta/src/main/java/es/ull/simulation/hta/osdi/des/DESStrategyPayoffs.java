package es.ull.simulation.hta.osdi.des;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionType;
import es.ull.simulation.hta.osdi.ontology.ClinicalProgressionWrapper;
import es.ull.simulation.hta.osdi.ontology.DiseaseWrapper;
import es.ull.simulation.hta.osdi.ontology.HealthConditionWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiDataProperty;
import es.ull.simulation.hta.osdi.ontology.OSDiObjectProperty;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForCost;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SpecificInformationForUtility;
import es.ull.simulation.hta.osdi.ontology.ResourceUsageSKOSCategory;
import es.ull.simulation.hta.osdi.ontology.StrategyReplacements;
import es.ull.simulation.hta.osdi.ontology.StrategyType;
import es.ull.simulation.hta.osdi.ontology.StrategyWrapper;
import es.ull.simulation.hta.params.Discount;

/**
 * The payoffs of the strategies of the health conditions in each arm of a discrete event simulation model (decision D2 of docs/osdi-1.3-diseno.md).
 * As in decision trees:
 * <ul>
 * <li>The strategies of a health condition in an arm are its own strategies after the strategy replacements of the intervention of the arm, by type
 * (see {@link StrategyReplacements#replaceByType(Set, Set)}).</li>
 * <li>The costs and utilities of the treatment and follow-up strategies (those with the categories TreatmentCat or FollowUpCat) are added to those of
 * their health condition: the own costs of the strategy and those of the health technologies it uses, and its utilities.</li>
 * </ul>
 * The payoffs follow the schedule of their health condition: for the disease, annual payoffs from time 0 and one-time payoffs at time 0; for a chronic
 * manifestation or stage, annual payoffs while the patient has it and one-time payoffs at its onset (at time 0 if it is the target of the interventions);
 * for an acute manifestation, one-time payoffs with each episode, and annual payoffs are ignored with a warning. Every cost and utility is used, not
 * only the first one. A utility that is not a disutility is converted into the disutility "base utility - utility", as the rest of utilities.
 * <p>
 * The DES does not simulate the outcomes of the strategies, so a treatment or follow-up strategy with outcomes (StrategyOutcome or failsIf) or adverse
 * events is not supported. Guidelines (of the strategy or of its health technologies) are not supported either, since their payoffs need their own
 * windows of time. The detection strategies are used by the detection interventions (see {@link OSDiDetectionIntervention}). The replacements of the
 * strategies of a development are ignored with a warning, since the DES does not simulate developments.
 * </p>
 */
public final class DESStrategyPayoffs {
	private final static OSDiLogger log = OSDiLogger.getLogger(DESStrategyPayoffs.class);

	/**
	 * The payoffs of the strategies of a health condition in an arm
	 * @param oneTimeCosts The one-time costs
	 * @param annualCosts The annual costs
	 * @param oneTimeUtilities The one-time utilities
	 * @param annualUtilities The annual utilities
	 */
	public record Payoffs(List<ParameterWrapper> oneTimeCosts, List<ParameterWrapper> annualCosts, List<ParameterWrapper> oneTimeUtilities, List<ParameterWrapper> annualUtilities) {
		/** No payoffs */
		public static final Payoffs NONE = new Payoffs(List.of(), List.of(), List.of(), List.of());

		/**
		 * Returns the annual cost of the strategies during a period, discounted
		 */
		public double getCostWithinPeriod(Patient pat, double initYear, double endYear, Discount discountRate) {
			double cost = 0.0;
			for (ParameterWrapper param : annualCosts) {
				cost += discountRate.applyDiscount(getValue(pat, param), initYear, endYear);
			}
			return cost;
		}

		/**
		 * Returns the annual cost of the strategies during a period, discounted and broken down by year
		 */
		public double[] getAnnualizedCostWithinPeriod(Patient pat, double initYear, double endYear, Discount discountRate, double[] previous) {
			final double[] result = previous.clone();
			for (ParameterWrapper param : annualCosts) {
				final double[] values = discountRate.applyAnnualDiscount(getValue(pat, param), initYear, endYear);
				for (int i = 0; i < Math.min(result.length, values.length); i++) {
					result[i] += values[i];
				}
			}
			return result;
		}

		/**
		 * Returns the one-time cost of the strategies at a moment, discounted
		 */
		public double getStartingCost(Patient pat, double time, Discount discountRate) {
			double cost = 0.0;
			for (ParameterWrapper param : oneTimeCosts) {
				cost += discountRate.applyPunctualDiscount(getValue(pat, param), time);
			}
			return cost;
		}

		/**
		 * Returns the annual disutility of the strategies
		 */
		public double getAnnualDisutility(Patient pat) {
			double disutility = 0.0;
			for (ParameterWrapper param : annualUtilities) {
				disutility += getDisutility(pat, param);
			}
			return disutility;
		}

		/**
		 * Returns the one-time disutility (QALY loss) of the strategies
		 */
		public double getStartingDisutility(Patient pat) {
			double disutility = 0.0;
			for (ParameterWrapper param : oneTimeUtilities) {
				disutility += getDisutility(pat, param);
			}
			return disutility;
		}

		private static double getValue(Patient pat, ParameterWrapper param) {
			return pat.getSimulation().getModel().getParameterValue(param.getShortName(), pat);
		}

		private static double getDisutility(Patient pat, ParameterWrapper param) {
			final double value = getValue(pat, param);
			if (param.getSpecificInformation() instanceof SpecificInformationForUtility info && info.isDisutility()) {
				return value;
			}
			return pat.getSimulation().getModel().getPopulation().getBaseUtility(pat) - value;
		}
	}

	/** The payoffs of the strategies of each health condition (by IRI) in each arm */
	private final Map<Intervention, Map<IRI, Payoffs>> payoffsByArm;

	private DESStrategyPayoffs(Map<Intervention, Map<IRI, Payoffs>> payoffsByArm) {
		this.payoffsByArm = payoffsByArm;
	}

	/**
	 * Returns the payoffs of the strategies of a health condition in the arm of an intervention
	 * @param intervention The intervention of the arm
	 * @param healthCondition The IRI of the health condition
	 * @return The payoffs of the strategies of the health condition in the arm
	 */
	public Payoffs get(Intervention intervention, IRI healthCondition) {
		final Map<IRI, Payoffs> arm = payoffsByArm.get(intervention);
		if (arm == null) {
			return Payoffs.NONE;
		}
		return arm.getOrDefault(healthCondition, Payoffs.NONE);
	}

	/**
	 * Collects the payoffs of the strategies of the disease and its progressions in each arm of the model
	 * @param model The model, whose interventions are already created
	 * @param diseaseWrapper The disease of the model
	 * @return The payoffs of the strategies in each arm
	 * @throws MalformedOSDiModelException If the ontology is malformed (e.g., an intervention declares strategies or two replacements of the same health condition)
	 * @throws UnsupportedOSDiFeatureException If a treatment or follow-up strategy has outcomes, adverse events or a guideline
	 */
	public static DESStrategyPayoffs build(OSDiDESModel model, DiseaseWrapper diseaseWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final List<HealthConditionWrapper> healthConditions = new ArrayList<>();
		healthConditions.add(diseaseWrapper);
		healthConditions.addAll(diseaseWrapper.getManifestations());
		healthConditions.addAll(diseaseWrapper.getStages());
		final Set<IRI> developments = new TreeSet<>();
		for (ClinicalProgressionWrapper development : diseaseWrapper.getDevelopments()) {
			developments.add(development.getIndividualIRI());
		}
		final Set<String> warned = new TreeSet<>();
		final Map<Intervention, Map<IRI, Payoffs>> payoffsByArm = new HashMap<>();
		for (Intervention intervention : model.getRegisteredInterventions()) {
			final OSDiIntervention osdiIntervention = (OSDiIntervention) intervention;
			final Map<IRI, Set<StrategyWrapper>> replacements = StrategyReplacements.of(osdiIntervention.getInterventionWrapper());
			for (IRI replaced : replacements.keySet()) {
				if (developments.contains(replaced)) {
					log.warn("The intervention " + intervention.name() + " replaces the strategies of the development " + replaced.getShortForm()
						+ ", which are ignored, since the DES does not simulate developments.");
				}
			}
			final Map<IRI, Payoffs> arm = new HashMap<>();
			for (HealthConditionWrapper healthCondition : healthConditions) {
				final Set<StrategyWrapper> strategies = StrategyReplacements.replaceByType(healthCondition.getStrategies(), replacements.get(healthCondition.getIndividualIRI()));
				final Payoffs payoffs = collect(model.getModelWrapper(), healthCondition, strategies, warned);
				if (payoffs != Payoffs.NONE) {
					arm.put(healthCondition.getIndividualIRI(), payoffs);
				}
			}
			payoffsByArm.put(intervention, arm);
		}
		return new DESStrategyPayoffs(payoffsByArm);
	}

	/**
	 * Collects the payoffs of the treatment and follow-up strategies of a health condition in an arm, and checks that they are supported
	 */
	private static Payoffs collect(ModelWrapper modelWrapper, HealthConditionWrapper healthCondition, Set<StrategyWrapper> strategies, Set<String> warned) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
		final List<ParameterWrapper> oneTimeCosts = new ArrayList<>();
		final List<ParameterWrapper> annualCosts = new ArrayList<>();
		final List<ParameterWrapper> oneTimeUtilities = new ArrayList<>();
		final List<ParameterWrapper> annualUtilities = new ArrayList<>();
		final boolean acute = healthCondition instanceof ClinicalProgressionWrapper progression && ClinicalProgressionType.ACUTE_MANIFESTATION.equals(progression.getClinicalProgressionType());
		for (StrategyWrapper strategy : strategies) {
			// Detection strategies are used by the detection interventions
			if (StrategyType.SCREENING_STRATEGY.equals(strategy.getStrategyType()) || StrategyType.DIAGNOSIS_STRATEGY.equals(strategy.getStrategyType())) {
				continue;
			}
			if (strategy.hasOutcomes()) {
				throw new UnsupportedOSDiFeatureException("The strategy " + strategy.getShortName() + " defines outcomes (hasStrategyOutcome or failsIf), which are not simulated in discrete event simulation models.");
			}
			if (strategy.hasAdverseEvents()) {
				throw new UnsupportedOSDiFeatureException("The strategy " + strategy.getShortName() + " defines adverse events (hasAdverseEvent), which are not simulated in discrete event simulation models.");
			}
			final Set<ResourceUsageSKOSCategory> categories = modelWrapper.getOSDiWrapper().getInferredSKOSCategories(strategy.getIndividualIRI());
			if (!categories.contains(ResourceUsageSKOSCategory.TREATMENT) && !categories.contains(ResourceUsageSKOSCategory.FOLLOW_UP)) {
				continue;
			}
			final List<ParameterWrapper> costs = new ArrayList<>(strategy.getCosts());
			if (!modelWrapper.getWrappersForProperty(strategy.getIndividualIRI(), OSDiObjectProperty.HAS_GUIDELINE).isEmpty()) {
				throw new UnsupportedOSDiFeatureException("The strategy " + strategy.getShortName() + " uses a guideline (hasGuideline), which is not supported in discrete event simulation models.");
			}
			for (IModelItemWrapper technology : modelWrapper.getWrappersForProperty(strategy.getIndividualIRI(), OSDiObjectProperty.USES_HEALTH_TECHNOLOGY)) {
				if (!modelWrapper.getWrappersForProperty(technology.getIndividualIRI(), OSDiObjectProperty.HAS_GUIDELINE).isEmpty()) {
					throw new UnsupportedOSDiFeatureException("The strategy " + strategy.getShortName() + " uses a guideline (hasGuideline) through the health technology " + technology.getShortName()
						+ ", which is not supported in discrete event simulation models.");
				}
				costs.addAll(modelWrapper.getWrappersForPropertyAs(technology.getIndividualIRI(), OSDiObjectProperty.HAS_COST, ParameterWrapper.class));
			}
			final List<ParameterWrapper> annual = new ArrayList<>();
			for (ParameterWrapper cost : costs) {
				if (appliesOneTime(modelWrapper, cost)) {
					oneTimeCosts.add(cost);
				}
				else {
					annual.add(cost);
				}
			}
			final List<ParameterWrapper> annualUtility = new ArrayList<>();
			for (ParameterWrapper utility : strategy.getUtilities()) {
				if (appliesOneTime(modelWrapper, utility)) {
					oneTimeUtilities.add(utility);
				}
				else {
					annualUtility.add(utility);
				}
			}
			if (acute) {
				if ((!annual.isEmpty() || !annualUtility.isEmpty()) && warned.add(strategy.getShortName() + "@" + healthCondition.getShortName())) {
					final List<ParameterWrapper> ignored = new ArrayList<>(annual);
					ignored.addAll(annualUtility);
					log.warn("The strategy " + strategy.getShortName() + " of the acute manifestation " + healthCondition.getShortName() + " defines annual costs or utilities (" + ignored
						+ "), which are ignored: the strategy applies with each episode, so only its one-time costs and utilities apply.");
				}
			}
			else {
				annualCosts.addAll(annual);
				annualUtilities.addAll(annualUtility);
			}
		}
		if (oneTimeCosts.isEmpty() && annualCosts.isEmpty() && oneTimeUtilities.isEmpty() && annualUtilities.isEmpty()) {
			return Payoffs.NONE;
		}
		return new Payoffs(oneTimeCosts, annualCosts, oneTimeUtilities, annualUtilities);
	}

	/**
	 * Returns true if a cost or utility applies once (appliesOneTime)
	 */
	private static boolean appliesOneTime(ModelWrapper modelWrapper, ParameterWrapper param) {
		if (param.getSpecificInformation() instanceof SpecificInformationForCost info) {
			return info.appliesOneTime();
		}
		if (param.getSpecificInformation() instanceof SpecificInformationForUtility info) {
			return info.appliesOneTime();
		}
		return modelWrapper.getOSDiWrapper().getBooleanValue(param.getIndividualIRI(), OSDiDataProperty.APPLIES_ONE_TIME).orElse(false);
	}
}
