package es.ull.simulation.hta.osdi.decisiontree.factories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.parameters.Imports;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.OSDiLogger;
import es.ull.simulation.hta.osdi.decisiontree.Model;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.AttributeWrapper;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.ExpressionLanguageType;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.OSDiDataItemType;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.CalculatedParameterData;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.ParameterNatureData;
import es.ull.simulation.ontology.OWLOntologyWrapper.InstanceCheckMode;

public interface AbstractModelFactory {
    /**
     * Returns a suitability score (0-100) for the given wrapper.
     * @param wrap The wrapper to evaluate.
     * @return A suitability score (0-100) with the reasons for the score.
     */
    FactorySuitabilityScore getSuitability(ExperimentWrapper experiment) throws MalformedSimulationModelException;

    /**
     * Returns a DecisionTreeModel created from the given wrapper.
     * @param experiment The experiment wrapper containing the ontology and experiment details.
     * @return A DecisionTreeModel instance.
     * @throws MalformedSimulationModelException If the simulation model is malformed.
     * @throws MalformedOSDiModelException If the OSDi model is malformed.
     * @throws UnsupportedOSDiFeatureException If the model contains unsupported features.
     */
    Model createModelGenerator(ExperimentWrapper experiment) throws MalformedSimulationModelException, MalformedOSDiModelException, UnsupportedOSDiFeatureException;
    
    /**
     * A probability derived from a time to event T (assumed exponential): the probability that the event happens within a period, 1 - e^(-period / T).
     * @param time The time to event
     * @param periodParameter The parameter that defines the period (e.g., the evaluation time of an outcome), if any
     * @param periodValue The period, if it is not defined by a parameter (e.g., the time horizon of the experiment)
     */
    record TimeToEventProbability(ParameterWrapper time, Optional<ParameterWrapper> periodParameter, double periodValue) {}

    /**
     * Returns a probability parameter that can be used in the decision tree by processing the original parameter. If the original parameter
     * is already a probability or proportion returns the parameter unmodified.
     * A time to event T (DI_TimeToEvent, assumed exponential) is converted into the probability that the event happens within the time horizon
     * H of the experiment, 1 - e^(-H/T) (decision D6). If the experiment does not declare a time horizon, the annual probability 1 - e^(-1/T)
     * is used, with a warning. See {@link #getProbabilityParameterFromTimeToEvent(ParameterWrapper, Optional, double)}.
     * @throws UnsupportedOSDiFeatureException
     * @throws MalformedOSDiModelException
     */
    static ParameterWrapper getProbabilityParameterFromGenericParameter(ParameterWrapper originalParam) throws UnsupportedOSDiFeatureException, MalformedOSDiModelException {
        if (OSDiDataItemType.DI_TIME_TO_EVENT.equals(originalParam.getDataItemType())) {
            final OptionalDouble horizon = originalParam.getModelWrapper().getExperimentWrapper().getDeclaredTimeHorizon();
            if (horizon.isPresent()) {
                return getProbabilityParameterFromTimeToEvent(originalParam, Optional.empty(), horizon.getAsDouble());
            }
            OSDiLogger.getLogger(AbstractModelFactory.class).warn("The risk " + originalParam.getShortName() + " is a time to event, but the experiment does not declare a time horizon (hasTimeHorizon). "
                + "The annual probability of the event, 1 - exp(-1 / T), is used. Declare a time horizon to use the probability of the event within it.");
            return getProbabilityParameterFromTimeToEvent(originalParam, Optional.empty(), 1.0);
        }
        return getProbabilityParameterFromProbabilityTypes(originalParam);
    }

    /**
     * Returns a synthetic probability parameter P_T derived from a time to event T: the probability that the event happens within a period,
     * 1 - e^(-period / T). The synthetic parameter is not written in the Excel file: {@link #getTimeToEventProbability(ParameterWrapper)}
     * identifies it, so that the formula of each branch computes the probability from the time, modified by the effects of the path
     * (see {@link es.ull.simulation.hta.osdi.decisiontree.excelport.HTAExcelModelFactory#getParameterTextFormula(ParameterWrapper, Set)}).
     * @param time The time to event
     * @param periodParameter The parameter that defines the period (e.g., the evaluation time of an outcome), if any
     * @param periodValue The period, used if there is no period parameter
     * @return The synthetic probability parameter
     */
    static ParameterWrapper getProbabilityParameterFromTimeToEvent(ParameterWrapper time, Optional<ParameterWrapper> periodParameter, double periodValue) {
        final ArrayList<ParameterWrapper> dependantParameters = new ArrayList<>();
        dependantParameters.add(time);
        periodParameter.ifPresent(dependantParameters::add);
        final String period = periodParameter.isPresent() ? periodParameter.get().getShortName() : Double.toString(periodValue);
        final ParameterNatureData paramData = new CalculatedParameterData(TTE_PREFIX + period + TTE_SEPARATOR + time.getShortName() + ")",
            dependantParameters, new ArrayList<AttributeWrapper>(), ExpressionLanguageType.EXCEL);
        return new ParameterWrapper.SyntheticBuilder(time.getModelWrapper(), "P_" + time.getShortName() + (periodParameter.isPresent() ? "_" + period : ""), paramData)
            .withSource("Computed from " + time.getShortName())
            .withDescription("Probability that the event of the time to event " + time.getShortName() + " happens within " + period + " years")
            .withDataItemType(OSDiDataItemType.DI_PROBABILITY)
            .build();
    }

    /** Beginning of the expression of a probability derived from a time to event, followed by the period */
    static final String TTE_PREFIX = "1.0 - EXP(-";
    /** Separator between the period and the time to event in the expression of a probability derived from a time to event */
    static final String TTE_SEPARATOR = " / ";

    /**
     * Returns the time to event, and the period, from which a synthetic probability parameter was derived by
     * {@link #getProbabilityParameterFromTimeToEvent(ParameterWrapper, Optional, double)}.
     * @param param A parameter
     * @return The time to event and the period from which the parameter was derived; an empty optional if the parameter is not a synthetic
     * probability derived from a time to event
     */
    static Optional<TimeToEventProbability> getTimeToEventProbability(ParameterWrapper param) {
        if (param.isSynthetic() && param.getParameterNatureData() instanceof CalculatedParameterData data && !data.dependantParameters().isEmpty()
                && data.expression().startsWith(TTE_PREFIX)) {
            final ParameterWrapper time = data.dependantParameters().get(0);
            if (OSDiDataItemType.DI_TIME_TO_EVENT.equals(time.getDataItemType())) {
                if (data.dependantParameters().size() > 1) {
                    return Optional.of(new TimeToEventProbability(time, Optional.of(data.dependantParameters().get(1)), Double.NaN));
                }
                final String period = data.expression().substring(TTE_PREFIX.length(), data.expression().indexOf(TTE_SEPARATOR));
                return Optional.of(new TimeToEventProbability(time, Optional.empty(), Double.parseDouble(period)));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the parameter unmodified if its data item type is a probability, proportion, incidence, prevalence, sensitivity or specificity.
     * @throws UnsupportedOSDiFeatureException If the data item type cannot be used as a probability in the decision tree
     */
    private static ParameterWrapper getProbabilityParameterFromProbabilityTypes(ParameterWrapper originalParam) throws UnsupportedOSDiFeatureException {
        switch (originalParam.getDataItemType()) {
            case OSDiDataItemType.DI_PROBABILITY:
            case OSDiDataItemType.DI_PROPORTION:
            case OSDiDataItemType.DI_INCIDENCE:
            case OSDiDataItemType.DI_PREVALENCE:
            case OSDiDataItemType.DI_BIRTH_PREVALENCE:
            case OSDiDataItemType.DI_SENSITIVITY:
            case OSDiDataItemType.DI_SPECIFICITY:
                return originalParam;
            default:
                throw new UnsupportedOSDiFeatureException("The data type (" + originalParam.getDataItemType() + ") for the parameter " + originalParam.getShortName() + " cannot be used as a probability in the decision tree.");
        }
    }

    /**
     * Returns the class of a given intervention IRI.
     * The intervention class is determined by checking its superclasses in the ontology.
     * @param interventionIRI The IRI of the intervention.
     * @return The class of the intervention.
     * @throws MalformedOSDiModelException If the intervention is not a subclass of Intervention.
     */
    static OSDiClass getInterventionClass(OSDiWrapper wrap, IRI interventionIRI) throws MalformedOSDiModelException {
        final Set<IRI> superclasses = wrap.getTypes(interventionIRI, Imports.INCLUDED, InstanceCheckMode.ASSERTED_ALL);
        if (!superclasses.contains(wrap.toIRI(OSDiClass.INTERVENTION)))
            throw new MalformedOSDiModelException("The intervention " + interventionIRI + " is not a subclass of Intervention");
        if (superclasses.contains(wrap.toIRI(OSDiClass.SCREENING_INTERVENTION)))
            return OSDiClass.SCREENING_INTERVENTION;
        else if (superclasses.contains(wrap.toIRI(OSDiClass.DIAGNOSIS_INTERVENTION)))
            return OSDiClass.DIAGNOSIS_INTERVENTION;
        else if (superclasses.contains(wrap.toIRI(OSDiClass.DETECTION_INTERVENTION)))
            return OSDiClass.DETECTION_INTERVENTION;
        else if (superclasses.contains(wrap.toIRI(OSDiClass.THERAPEUTIC_INTERVENTION)))
            return OSDiClass.THERAPEUTIC_INTERVENTION;
        return OSDiClass.INTERVENTION;
    }
}
