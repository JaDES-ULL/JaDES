package es.ull.simulation.hta.osdi.des;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import es.ull.simulation.hta.MalformedSimulationModelException;
import es.ull.simulation.hta.config.BaseHTAOutputConfigProvider;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.OSDiExperimentConfigurationProvider;
import es.ull.simulation.hta.osdi.cliargs.RunDESOsdiArgs;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.exceptions.UnsupportedOSDiFeatureException;
import es.ull.simulation.hta.progression.Disease;
import es.ull.simulation.hta.progression.DiseaseProgression;

@Tag("loadOntology")
@DisplayName("Test the loading of different model items from the ontology")
@TestInstance(Lifecycle.PER_CLASS)
public class TestLoadOntology {
	private static final String OWL_PATH = "/T1DM.ttl";
	private static final String OWL_EXPERIMENT_IRI = "T1DM_ExperimentBase";

    private OSDiExperiment desExperiment;
    private OSDiDESModel model;

    @BeforeAll
    public void setUp() throws MalformedSimulationModelException, IOException, OWLOntologyCreationException, MalformedOSDiModelException, UnsupportedOSDiFeatureException {
        OWLOntologyDocumentSource source = new StreamDocumentSource(Objects.requireNonNull(
                getClass().getResourceAsStream(OWL_PATH), "Ontology file not found in resources: " + OWL_PATH));
        final OSDiWrapper wrapper = new OSDiWrapper.Builder().build(source);
        final RunDESOsdiArgs args = new RunDESOsdiArgs();
        args.osdiExperiment = OWL_EXPERIMENT_IRI;
        final OSDiExperimentConfigurationProvider configProvider = new OSDiExperimentConfigurationProvider(args, wrapper);
        final BaseHTAOutputConfigProvider outputConfigProvider = new BaseHTAOutputConfigProvider();
        this.desExperiment = new OSDiExperiment(configProvider, outputConfigProvider);
        this.desExperiment.initializeModel();
        model = (OSDiDESModel) desExperiment.getModel();
    }

    @Test
    @OSDiTest
    @DisplayName("Test if the model can load basic properties of the diseases")
    public void testDisease() {        
        final Disease[] diseases = model.getRegisteredDiseases();
        assertAll("Disease: ",
            () -> assertTrue(diseases.length == 2, "There should be exactly two diseases"),
            () -> assertTrue(diseases[0].name().equals("HEALTHY"), "First disease should be HEALTHY. Found " + diseases[0].name() + " instead"),
            () -> assertTrue(diseases[1].name().equals("T1DM_Disease"), "Second disease should be T1DM_Disease. Found " + diseases[1].name() + " instead")
        );
    }

    @Test
    @OSDiTest
    @DisplayName("Test if the model can load basic properties of the disease progressions")
    public void testDiseaseProgressions() {
        for (DiseaseProgressionTemplate prog : DiseaseProgressionTemplate.values()) {
            final DiseaseProgression progression = model.getDiseaseProgression(prog.getDescription());
            assertTrue(progression != null);
            switch (prog.getType()) {
                case ACUTE_MANIFESTATION:
                    assertTrue(progression.getType().equals(DiseaseProgression.Type.ACUTE_MANIFESTATION), progression.name() + " should be of type ACUTE_MANIFESTATION");
                    break;
                case CHRONIC_MANIFESTATION:
                    assertTrue(progression.getType().equals(DiseaseProgression.Type.CHRONIC_MANIFESTATION), progression.name() + " should be of type CHRONIC_MANIFESTATION");
                    break;
                case STAGE:
                    assertTrue(progression.getType().equals(DiseaseProgression.Type.STAGE), progression.name() + " should be of type STAGE");
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    @OSDiTest
    @DisplayName("Test if the model can load basic properties of some parameters")
    public void testParameters() {
        Map<String, es.ull.simulation.hta.params.Parameter> parameters = model.getParameters();
        assertAll("Parameters: ",
            () -> assertTrue(parameters.containsKey("T1DM_BETA_Manif_NEU"), "This parameter should be created even since it is used by a Calculated Parameter"),
            () -> assertEquals(parameters.get("T1DM_BETA_Manif_NEU").getGroup(), es.ull.simulation.hta.params.ParameterGroup.RISK),
            () -> assertTrue(parameters.containsKey("T1DM_DCCT1_ComplicationsFree_U")),
            () -> assertEquals(parameters.get("T1DM_DCCT1_ComplicationsFree_U").getGroup(), es.ull.simulation.hta.params.ParameterGroup.UTILITY),
//            () -> assertTrue(!parameters.containsKey("T1DM_Stage_CHD_IncidenceBase_RR_U95CI"), "This parameter exists in the ontology but should not be created as an independent parameter"),
            () -> assertTrue(parameters.containsKey("T1DM_Manif_HF_AC")),
            () -> assertEquals(parameters.get("T1DM_Manif_HF_AC").getGroup(), es.ull.simulation.hta.params.ParameterGroup.COST)
        );
    }
}
