package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.ull.simulation.hta.osdi.ontology.ExperimentWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.osdi.decisiontree.factories.CentralModelFactory;
import es.ull.simulation.hta.osdi.decisiontree.factories.SimpleModel;
import es.ull.simulation.hta.osdi.decisiontree.nbsdecisiontree.NBSModel;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;

public class TestSingleDiseaseAndPopulationDecisionTreeModelGeneration {
    private static final Logger log = LoggerFactory.getLogger(TestSingleDiseaseAndPopulationDecisionTreeModelGeneration.class);
    static Stream<Arguments> provideDecisionTreeModelTestCases() {
        return Stream.of(
            /*Arguments.of("resources/OSDi_PBD.owl", "PBD_ModelTree", "PBD_ProfoundBiotinidaseDeficiency", "PBD_BasePopulation", 
                new String[] {"PBD_InterventionScreening", "PBD_InterventionNoScreening"}, NBSDecisionTreeModel.class, 8),*/
            Arguments.of("/OSDi_test.ttl", "TEST_Experiment1", "TEST_Disease1", "TEST_Population", 
                new String[] {"TEST_InterventionEffective", "TEST_InterventionIneffective"}, SimpleModel.class, 2),
            Arguments.of("/OSDi_test.ttl", "TEST_Experiment2", "TEST_Disease2", "TEST_Population", 
                new String[] {"TEST_InterventionEffective", "TEST_InterventionIneffective"}, SimpleModel.class, 3),
            Arguments.of("/OSDi_test.ttl", "TEST_Experiment3", "TEST_Disease3", "TEST_Population", 
                new String[] {"TEST_InterventionEffective", "TEST_InterventionIneffective"}, SimpleModel.class, 4),
            Arguments.of("/PBD.ttl", "PBD_ExperimentBase", "PBD_ProfoundBiotinidaseDeficiency", "PBD_BasePopulation", 
                new String[] {"PBD_InterventionNoScreening", "PBD_InterventionScreening"}, NBSModel.class, 9)
        );
    }

    @ParameterizedTest(name = "[{index}] => ontology={0}, experiment={1}, diseaseIRI={2}, populationIRI={3}, interventionIRIs={4}, expectedModelClass={5}, expectedDepth={6}")
    @MethodSource("provideDecisionTreeModelTestCases")
    public void testDecisionTreeModelGeneration(String ontologyFilePath, String experimentName, String diseaseIRI, String populationIRI, String[] interventionIRIs, Class<? extends SingleDiseaseAndPopulationModel> expectedModelClass, int expectedDepth) throws MalformedOSDiModelException, MalformedSimulationModelException, UnsupportedOSDiFeatureException, OWLOntologyCreationException {
        OWLOntologyDocumentSource source = new StreamDocumentSource(Objects.requireNonNull(
                getClass().getResourceAsStream(ontologyFilePath), "Ontology file not found in resources: " + ontologyFilePath));
        final OSDiWrapper wrapper = new OSDiWrapper.Builder().build(source);

        final ExperimentWrapper experimentWrapper = wrapper.buildExperiment(wrapper.toIRI(experimentName));
        CentralModelFactory.register(NBSModel.getFactory());
        CentralModelFactory.register(SimpleModel.getFactory());
        Model tempModel = CentralModelFactory.create(experimentWrapper);
        assertTrue(SingleDiseaseAndPopulationModel.class.isInstance(tempModel), "The model must be a SingleDiseaseAndPopulationDecisionTreeModel");
        assertTrue(expectedModelClass.isInstance(tempModel), "The model is expected to be subclass of " + expectedModelClass.getSimpleName() + ", but got " + tempModel.getClass().getSimpleName());
        final SingleDiseaseAndPopulationModel model = (SingleDiseaseAndPopulationModel) tempModel;
        final ModelVisualizer visualizer = new ModelVisualizer(model);
        assertEquals(diseaseIRI, model.getDiseaseWrapper().getShortName(), "The disease IRI does not match the expected one. Found: " + model.getDiseaseWrapper().getShortName() + ", expected: " + diseaseIRI);
        assertEquals(populationIRI, model.getPopulationWrapper().getShortName(), "The population IRI does not match the expected one. Found: " + model.getPopulationWrapper().getShortName() + ", expected: " + populationIRI);
        assertEquals(interventionIRIs.length, model.getInterventions().size(), "The number of intervention arms does not match the expected one.");
        assertNotNull(model.getIntervention(wrapper.toIRI(interventionIRIs[0])), "The model does not contain the arm for intervention " + interventionIRIs[0]);
        assertNotNull(model.getIntervention(wrapper.toIRI(interventionIRIs[1])), "The model does not contain the arm for intervention " + interventionIRIs[1]);
        log.debug("Generated model visualization:\n" + visualizer.print());
        assertEquals(expectedDepth, model.getDepth());
    }
}
