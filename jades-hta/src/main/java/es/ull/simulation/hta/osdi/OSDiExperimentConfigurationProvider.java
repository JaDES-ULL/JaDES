package es.ull.simulation.hta.osdi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import es.ull.simulation.hta.config.IHTAExperimentConfigProvider;
import es.ull.simulation.hta.osdi.cliargs.RunDESOsdiArgs;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.outcomes.DisutilityCombinationMethod;

public class OSDiExperimentConfigurationProvider implements IHTAExperimentConfigProvider {
    private final RunDESOsdiArgs args;
    private final OSDiWrapper wrapper;
    private final ExperimentWrapper experiment;

    public OSDiExperimentConfigurationProvider(RunDESOsdiArgs args, OSDiWrapper wrapper) throws OWLOntologyCreationException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        this.args = args;
        this.wrapper = wrapper;
        this.experiment = this.wrapper.buildExperiment(wrapper.toIRI(args.osdiExperiment));
        if (args.htaArguments.commonArgs.getTimeHorizon().isPresent()) {
            this.experiment.setTimeHorizon(args.htaArguments.commonArgs.getTimeHorizon().getAsInt());
        }
        if (args.htaArguments.commonArgs.getNRuns().isPresent()) {
            this.experiment.setNPSARuns(args.htaArguments.commonArgs.getNRuns().getAsInt());
        }
    }

    public ExperimentWrapper getExperimentWrapper() {
        return experiment;
    }

    @Override
    public OptionalInt getNRuns() {
        return OptionalInt.of(experiment.getNPSARuns());
    }

    @Override
    public OptionalLong getSeed() {
        return args.htaArguments.commonArgs.getSeed();
    }

    @Override
    public OptionalInt getTimeHorizon() {
        return OptionalInt.of(experiment.getTimeHorizon());
    }

    @Override
    public OptionalInt getNThreads() {
        return args.htaArguments.commonArgs.getNThreads();
    }

    @Override
    public Optional<Boolean> isParallel() {
        return args.htaArguments.commonArgs.isParallel();
    }

    @Override
    public OptionalInt getNPatients() {
        return OptionalInt.of(experiment.getNSimulatedIndividuals());
    }

    @Override
    public OptionalInt getStudyYear() {
        return OptionalInt.of(experiment.getStudyYear());
    }

    @Override
    public OptionalDouble getDefaultDiscountRateForCosts() {
        return OptionalDouble.of(experiment.getDefaultDiscountRateForCosts());
    }

    @Override
    public OptionalDouble getDefaultDiscountRateForEffects() {
        return OptionalDouble.of(experiment.getDefaultDiscountRateForEffects());
    }

    @Override
    public Optional<Boolean> isBaseCaseEnabled() {
        // Returns true by default
        return Optional.of(true);
    }

    @Override
    public Optional<DisutilityCombinationMethod> getDisutilityCombinationMethod() {
        return Optional.of(experiment.getDisutilityCombinationMethod());
    }

    @Override
    public List<Integer> getDebugPatients() {
        List<Integer> patients = args.htaArguments.debugPatients;
        if (patients == null || patients.isEmpty()) {
            return new ArrayList<>();
        }
        return List.copyOf(patients);
    }


}
