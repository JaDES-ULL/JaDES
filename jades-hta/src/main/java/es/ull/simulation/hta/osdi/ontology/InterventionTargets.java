package es.ull.simulation.hta.osdi.ontology;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;

import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

/**
 * Resolves the target of the interventions of a model (decision D1 of docs/osdi-1.3-diseno.md): the health condition to which they are applied.
 * It is shared by the generators of decision trees and the discrete event simulation, which decide by themselves which targets they support.
 */
public final class InterventionTargets {
    private final static OSDiLogger log = OSDiLogger.getLogger(InterventionTargets.class);

    private InterventionTargets() {
    }

    /**
     * Resolves the common target of the interventions of a model.
     * <p>
     * The target of an intervention is read from isInterventionOf and from its inverse, hasIntervention, since the reasoner does not infer inverse
     * properties. Only the targets included in the model are considered: the ontology states that every intervention has exactly one target, but
     * the same intervention individual may be shared by several models. Hence:
     * <ul>
     * <li>An intervention without target in the model is assumed to target the disease of the model, with a warning.</li>
     * <li>An intervention with more than one target in the model is malformed.</li>
     * <li>The target must be the disease of the model or one of its progressions; otherwise, the model is malformed.</li>
     * <li>The compared interventions must have the same target; otherwise, the model is malformed.</li>
     * </ul>
     * </p>
     * @param modelWrapper The model
     * @param diseaseWrapper The disease of the model
     * @return The clinical progression element (development, manifestation or stage) that the interventions target, or an empty optional if they
     * target the disease.
     * @throws MalformedOSDiModelException If an intervention has more than one target, its target does not belong to the disease of the model, or the
     * interventions have different targets.
     * @throws UnsupportedOSDiFeatureException If an unsupported OSDi feature is encountered while reading the model
     */
    public static Optional<ClinicalProgressionWrapper> resolve(ModelWrapper modelWrapper, DiseaseWrapper diseaseWrapper) throws MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        final IRI diseaseIRI = diseaseWrapper.getIndividualIRI();
        // The health conditions that may be targeted: the disease and its progressions
        final Map<IRI, ClinicalProgressionWrapper> progressions = new LinkedHashMap<>();
        for (ClinicalProgressionWrapper progression : diseaseWrapper.getDevelopments()) {
            progressions.put(progression.getIndividualIRI(), progression);
        }
        for (ClinicalProgressionWrapper progression : diseaseWrapper.getManifestations()) {
            progressions.put(progression.getIndividualIRI(), progression);
        }
        for (ClinicalProgressionWrapper progression : diseaseWrapper.getStages()) {
            progressions.put(progression.getIndividualIRI(), progression);
        }
        final List<IRI> healthConditions = new ArrayList<>();
        healthConditions.add(diseaseIRI);
        healthConditions.addAll(progressions.keySet());

        IRI commonTarget = null;
        InterventionWrapper firstIntervention = null;
        for (InterventionWrapper intervention : new TreeSet<>(modelWrapper.getInterventionIndividuals())) {
            final IRI interventionIRI = intervention.getIndividualIRI();
            final Set<IRI> targets = new LinkedHashSet<>();
            for (IModelItemWrapper target : modelWrapper.getWrappersForProperty(interventionIRI, OSDiObjectProperty.IS_INTERVENTION_OF)) {
                targets.add(target.getIndividualIRI());
            }
            for (IRI healthCondition : healthConditions) {
                for (IModelItemWrapper item : modelWrapper.getWrappersForProperty(healthCondition, OSDiObjectProperty.HAS_INTERVENTION)) {
                    if (item.getIndividualIRI().equals(interventionIRI)) {
                        targets.add(healthCondition);
                    }
                }
            }
            final IRI target;
            if (targets.isEmpty()) {
                log.warn("The intervention " + intervention.getShortName() + " does not define its target (isInterventionOf or hasIntervention) in the model. It is assumed to target the disease " + diseaseWrapper.getShortName() + ".");
                target = diseaseIRI;
            }
            else if (targets.size() > 1) {
                throw new MalformedOSDiModelException("The intervention " + intervention.getShortName() + " defines more than one target in the model: " + targets + ". Every intervention must have exactly one target.");
            }
            else {
                target = targets.iterator().next();
            }
            if (!target.equals(diseaseIRI) && !progressions.containsKey(target)) {
                throw new MalformedOSDiModelException("The target " + target + " of the intervention " + intervention.getShortName() + " is neither the disease " + diseaseWrapper.getShortName() + " nor one of its progressions.");
            }
            if (commonTarget == null) {
                commonTarget = target;
                firstIntervention = intervention;
            }
            else if (!commonTarget.equals(target)) {
                throw new MalformedOSDiModelException("The compared interventions must have the same target, but " + firstIntervention.getShortName() + " targets " + commonTarget
                    + " and " + intervention.getShortName() + " targets " + target + ".");
            }
        }
        if (commonTarget == null || commonTarget.equals(diseaseIRI)) {
            return Optional.empty();
        }
        return Optional.of(progressions.get(commonTarget));
    }
}
