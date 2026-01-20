package simkit.random;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class RandomVariatesCoverageTest {

    @Test
    void shouldGenerateBivariateNormalValues() {
        BivariateNormalVariate variate = new BivariateNormalVariate();
        variate.setParameters(0.0, 1.0, 0.0, 1.0, 0.5);

        double[] values = variate.generateValues();
        assertEquals(2, values.length);
        assertEquals(5, variate.getParameters().length);
    }

    @Test
    void shouldGenerateContinuousAndDiscreteSelections() {
        ContinuousSelectorVariate continuous = new ContinuousSelectorVariate();
        continuous.setParameters(new double[] {1.0, 1.0}, new double[] {0.0, 10.0, 20.0});
        double value = continuous.generate();
        assertTrue(value >= 0.0 && value <= 20.0);

        DiscreteSelectorVariate discrete = new DiscreteSelectorVariate();
        discrete.setParameters(new double[] {1.0, 1.0, 1.0});
        int index = discrete.generateInt();
        assertTrue(index >= 0 && index < 3);
    }

        @Test
        void shouldRejectInvalidSelectorParameters() {
        ContinuousSelectorVariate continuous = new ContinuousSelectorVariate();
        assertThrows(IllegalArgumentException.class,
            () -> continuous.setParameters(new double[] {1.0}, new double[] {1.0}));
        assertThrows(IllegalArgumentException.class,
            () -> continuous.setParameters(new double[] {-1.0}, new double[] {0.0, 1.0}));
        assertThrows(IllegalArgumentException.class,
            () -> continuous.setParameters(new double[] {0.0, 0.0}, new double[] {0.0, 1.0, 2.0}));

        DiscreteSelectorVariate discrete = new DiscreteSelectorVariate();
        assertThrows(IllegalArgumentException.class, () -> discrete.setParameters());
        assertThrows(IllegalArgumentException.class,
            () -> discrete.setParameters(new double[] {-1.0, 1.0}));
        assertThrows(IllegalArgumentException.class,
            () -> discrete.setParameters(new double[] {0.0, 0.0}));
        }

    @Test
    void shouldGenerateGompertzInverseGaussianAndHyperExponential() {
        GompertzVariate gompertz = new GompertzVariate();
        gompertz.setParameters(1.0, 2.0, 0.0);
        assertTrue(Double.isFinite(gompertz.generate()));

        InverseGaussian2Variate inverse = new InverseGaussian2Variate();
        inverse.setParameters(1.0, 2.0);
        assertTrue(inverse.generate() > 0.0);

        HyperExponentialVariate hyper = new HyperExponentialVariate();
        hyper.setParameters(1.0, 2.0, 0.4);
        assertTrue(hyper.generate() > 0.0);
    }

    @Test
    void shouldExposeVariateMetadata() {
        GompertzVariate gompertz = new GompertzVariate();
        gompertz.setParameters(2.0, 3.0, 1.0);
        assertEquals(2.0, gompertz.getAlpha(), 1e-9);
        assertEquals(3.0, gompertz.getBeta(), 1e-9);
        assertEquals(1.0, gompertz.getAge(), 1e-9);
        assertTrue(GompertzVariate.generateGompertz(2.0, 3.0, 1.0, 0.5) > 0.0);

        HyperExponentialVariate hyper = new HyperExponentialVariate();
        hyper.setParameters(1.0, 2.0, 0.3);
        assertEquals(0.3, hyper.getLambda(), 1e-9);
        assertEquals(1.0, hyper.getMean1(), 1e-9);
        assertEquals(2.0, hyper.getMean2(), 1e-9);
        assertTrue(hyper.toString().contains("HyperExponential"));

        InverseGaussian2Variate inverse = new InverseGaussian2Variate();
        inverse.setParameters(1.0, 2.0);
        assertEquals(1.0, inverse.getMu(), 1e-9);
        assertEquals(2.0, inverse.getLambda(), 1e-9);
        assertTrue(inverse.toString().contains("Inverse Gaussian"));

        GeneralizedExtremeValueVariate gev = new GeneralizedExtremeValueVariate();
        gev.setParameters(0.2, 1.0, 2.0);
        assertEquals(0.2, gev.getK(), 1e-9);
        assertEquals(1.0, gev.getSigma(), 1e-9);
        assertEquals(2.0, gev.getMu(), 1e-9);
    }

        @Test
        void shouldRejectInvalidDistributionParameters() {
        GompertzVariate gompertz = new GompertzVariate();
        assertThrows(IllegalArgumentException.class, () -> gompertz.setParameters(0.0, 1.0, 0.0));

        HyperExponentialVariate hyper = new HyperExponentialVariate();
        assertThrows(IllegalArgumentException.class, () -> hyper.setParameters(1.0, 2.0, 1.5));
            assertThrows(IllegalArgumentException.class, () -> hyper.setMean1(0.0));
            assertThrows(IllegalArgumentException.class, () -> hyper.setMean2(0.0));
            assertThrows(IllegalArgumentException.class, () -> hyper.setLambda(0.0));

        InverseGaussian2Variate inverse = new InverseGaussian2Variate();
        assertThrows(IllegalArgumentException.class, () -> inverse.setParameters(1.0, -1.0));
            assertThrows(IllegalArgumentException.class, () -> inverse.setLambda(0.0));

            GeneralizedExtremeValueVariate gev = new GeneralizedExtremeValueVariate();
            assertThrows(IllegalArgumentException.class, () -> gev.setParameters(1.0, 2.0));
            assertThrows(IllegalArgumentException.class, () -> gev.setParameters(1.0, "x", 2.0));

        EmpiricalVariate empirical = new EmpiricalVariate();
        assertThrows(IllegalArgumentException.class, () -> empirical.setParameters(new double[] {0.5}, "x"));

        LimitedRandomVariate limited = new LimitedRandomVariate();
        DiscreteConstantVariate constant = new DiscreteConstantVariate();
        constant.setParameters(2);
        assertThrows(IllegalArgumentException.class,
            () -> limited.setParameters(constant, 1.0, "x"));

        assertThrows(IllegalArgumentException.class,
            () -> new RandomIntegerSelector(new double[] {0.0, 0.0}));
        assertThrows(IllegalArgumentException.class,
            () -> new RandomIntegerSelector(new double[] {-1.0, 1.0}));
        }

    @Test
    void shouldGenerateEmpiricalAndDirichletValues() {
        EmpiricalVariate empirical = new EmpiricalVariate();
        empirical.setParameters(new double[] {0.5, 0.5}, new double[] {1.0, 2.0});
        double val = empirical.generate();
        assertTrue(val == 1.0 || val == 2.0);

        DiscreteConstantVariate v1 = new DiscreteConstantVariate();
        v1.setParameters(5);
        DiscreteConstantVariate v2 = new DiscreteConstantVariate();
        v2.setParameters(7);
        empirical.setParameters(new double[] {0.25, 0.75}, new RandomVariate[] {v1, v2});
        assertEquals(2, empirical.getValues().length);
        assertTrue(empirical.getParameters()[0] instanceof RandomVariate[]);
        assertEquals(2, ((RandomVariate[]) empirical.getParameters()[0]).length);

        DirichletVariate dirichlet = new DirichletVariate();
        dirichlet.setParameters(new double[] {1.0, 2.0, 3.0});
        double[] values = dirichlet.generateValues(true);
        assertEquals(3, values.length);

        DirichletBetaVariate dirichletBeta = new DirichletBetaVariate();
        dirichletBeta.setParameters(new double[] {1.0, 2.0});
        double[] betaValues = dirichletBeta.generateValues(true);
        assertEquals(2, betaValues.length);
    }

    @Test
    void shouldSelectRandomIntegersAndLimits() {
        RandomIntegerSelector selector = new RandomIntegerSelector(new double[] {1.0, 1.0, 1.0});
        assertEquals(0, selector.generate(0.0));
        assertEquals(2, selector.generate(0.9));

        LimitedRandomVariate limited = new LimitedRandomVariate();
        DiscreteConstantVariate constant = new DiscreteConstantVariate();
        constant.setParameters(5);
        limited.setParameters(constant, Double.valueOf(1.0), Double.valueOf(3.0));
        assertEquals(3.0, limited.generate(), 1e-9);
        assertEquals(3, limited.getParameters().length);
    }

    @Test
    void shouldGenerateGeneralizedExtremeValueAndRRFromLnCI() {
        GeneralizedExtremeValueVariate gev = new GeneralizedExtremeValueVariate();
        gev.setParameters(0.1, 1.0, 0.0);
        assertTrue(Double.isFinite(gev.generate()));

        RRFromLnCIVariate rr = new RRFromLnCIVariate();
        rr.setParameters(1.5, 1.0, 2.0, 10);
        assertTrue(rr.generate() > 0.0);
        assertEquals(4, rr.getParameters().length);
    }

    @Test
    void shouldResampleFromFileAndStudentVariate() throws IOException {
        Path temp = Files.createTempFile("resample", ".txt");
        Files.write(temp, List.of("comment", "2", "1.0", "3.0"));
        try {
            FileResampleVariate fileVar = new FileResampleVariate();
            fileVar.setParameters(temp.toString());
            double value = fileVar.generate();
            assertTrue(value == 1.0 || value == 3.0);
        } finally {
            Files.deleteIfExists(temp);
        }

        StudentVariate student = new StudentVariate();
        student.setParameters(2);
        assertTrue(Double.isFinite(student.generate()));
    }

    @Test
    void shouldGenerateBetaLimitedValues() {
        BetaLimitedVariate betaLimited = new BetaLimitedVariate();
        betaLimited.setParameters(2.0, 3.0, 0.0, 1.0);
        double value = betaLimited.getValue(() -> 0.0);
        assertTrue(value >= 0.0 && value <= 1.0);
    }
}
