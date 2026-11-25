package es.ull.simulation.hta.osdi.decisiontree;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import es.ull.simulation.hta.osdi.des.factories.ParameterFactory;
import es.ull.simulation.hta.osdi.exceptions.MalformedOSDiModelException;
import es.ull.simulation.hta.osdi.ontology.ModelWrapper;
import es.ull.simulation.hta.osdi.ontology.OSDiClass;
import es.ull.simulation.hta.osdi.ontology.OSDiDataProperty;
import es.ull.simulation.hta.osdi.ontology.OSDiTest;
import es.ull.simulation.hta.osdi.ontology.OSDiWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper;
import es.ull.simulation.hta.osdi.ontology.ParameterWrapper.SecondOrderUncertaintyParameterData;
import es.ull.simulation.hta.osdi.ontology.ProbabilisticExpressionWrapper;
import es.ull.simulation.hta.osdi.ontology.ProbabilityDistributionExpressionType;
import es.ull.simulation.hta.osdi.ontology.UncertaintyCharacterization;
import simkit.random.ExponentialVariate;
import simkit.random.GammaVariate;
import simkit.random.RandomVariate;

/**
 * Checks how the parameters of the probability distributions of OSDi are read and used.
 * <ul>
 * <li>A gamma distribution is defined by its shape (hasAlfaParameter, k) and its scale (hasThetaParameter, theta): mean = k * theta. OSDi 1.2
 * called the scale hasLambdaParameter, although lambda usually denotes the rate (1 / theta); OSDi 1.3 renames it.</li>
 * <li>An exponential distribution is defined by its rate (hasLambdaParameter): mean = 1 / lambda.</li>
 * </ul>
 */
@OSDiTest
@DisplayName("Parameters of the probability distributions")
public class TestProbabilityDistributionParameters {
    private static final double EPS = 1e-12;

    private static ModelWrapper loadPBD() throws Exception {
        return DecisionTreeTestSupport.loadModel("/PBD.ttl", "PBD_ExperimentBase").getModelWrapper();
    }

    private static ParameterWrapper findParameter(ModelWrapper modelWrapper, String shortName) {
        return modelWrapper.getParameters().stream().filter(p -> p.getShortName().equals(shortName)).findFirst()
            .orElseThrow(() -> new AssertionError("Parameter not found: " + shortName));
    }

    @Test
    @DisplayName("A gamma distribution is read as shape (hasAlfaParameter) and scale (hasThetaParameter)")
    void gammaIsShapeAndScale() throws Exception {
        assertArrayEquals(new OSDiDataProperty[] {OSDiDataProperty.HAS_ALFA_PARAMETER, OSDiDataProperty.HAS_THETA_PARAMETER},
            ProbabilityDistributionExpressionType.GAMMA.getParameters());
        final ModelWrapper modelWrapper = loadPBD();
        // Life expectancy of the base population of PBD: expected value 82.27, gamma with k = 25 and theta = 3.29 (mean 82.25)
        final ParameterWrapper lifeExpectancy = findParameter(modelWrapper, "PBD_BasePopulationLifeExpectancy");
        final SecondOrderUncertaintyParameterData data = assertInstanceOf(SecondOrderUncertaintyParameterData.class, lifeExpectancy.getParameterNatureData());
        final ProbabilisticExpressionWrapper expression = data.uncertaintyCharacterization().getProbabilisticExpression().orElseThrow();
        assertEquals(ProbabilityDistributionExpressionType.GAMMA, expression.getProbabilityDistribution());
        assertArrayEquals(new double[] {25.0, 3.29}, expression.getParameterValues(), EPS);

        final RandomVariate variate = ParameterFactory.getRandomVariateInstance(lifeExpectancy);
        final GammaVariate gamma = assertInstanceOf(GammaVariate.class, variate);
        assertEquals(25.0, gamma.getAlpha(), EPS);
        assertEquals(3.29, gamma.getBeta(), EPS, "The scale of the GammaVariate must be theta");
        assertEquals(data.expectedValue(), gamma.getAlpha() * gamma.getBeta(), 0.05, "The mean k * theta must match the expected value");
    }

    @Test
    @DisplayName("A gamma distribution with the hasLambdaParameter of OSDi 1.2 is rejected with a message that explains the change")
    void gammaWithLambdaIsRejected() throws Exception {
        // A fresh copy of the ontology, since an individual is added
        final ModelWrapper modelWrapper = DecisionTreeTestSupport.loadModelFromFile(Paths.get("src/test/resources/PBD.ttl"), "PBD_ExperimentBase").getModelWrapper();
        final OSDiWrapper wrap = modelWrapper.getOSDiWrapper();
        final IRI iri = wrap.toIRI("TEST_GammaWithLambda");
        wrap.createIndividual(OSDiClass.GAMMA_DISTRIBUTION_EXPRESSION, iri);
        wrap.assertDataProperty(iri, OSDiDataProperty.HAS_ALFA_PARAMETER, "25.0", OWL2Datatype.XSD_DOUBLE);
        wrap.assertDataProperty(iri, OSDiDataProperty.HAS_LAMBDA_PARAMETER, "3.29", OWL2Datatype.XSD_DOUBLE);
        final ProbabilisticExpressionWrapper expression = new ProbabilisticExpressionWrapper(modelWrapper, iri);
        final MalformedOSDiModelException ex = assertThrows(MalformedOSDiModelException.class, expression::initialize);
        assertTrue(ex.getMessage().contains("hasThetaParameter") && ex.getMessage().contains("hasLambdaParameter"),
            "The message must explain that the scale of a gamma is hasThetaParameter: " + ex.getMessage());
    }

    @Test
    @DisplayName("The lambda of an exponential distribution is its rate: the mean of the variate is 1 / lambda")
    void exponentialLambdaIsRate() throws Exception {
        final ModelWrapper modelWrapper = loadPBD();
        final ProbabilisticExpressionWrapper expression = new ProbabilisticExpressionWrapper.SyntheticBuilder(modelWrapper,
            modelWrapper.getOSDiWrapper().toIRI("TEST_ExponentialRate4"), ProbabilityDistributionExpressionType.EXPONENTIAL)
            .withParameterValues(new double[] {4.0}).build();
        final ParameterWrapper parameter = new ParameterWrapper.SyntheticBuilder(modelWrapper, "TEST_ExponentialParameter",
            new SecondOrderUncertaintyParameterData(0.25, new UncertaintyCharacterization(null, expression))).build();
        final ExponentialVariate exponential = assertInstanceOf(ExponentialVariate.class, ParameterFactory.getRandomVariateInstance(parameter));
        assertEquals(0.25, exponential.getMean(), EPS, "The mean of an exponential with rate 4 is 0.25");
    }
}
