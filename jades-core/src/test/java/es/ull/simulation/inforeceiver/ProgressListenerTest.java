package es.ull.simulation.inforeceiver;

import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.info.TimeChangeInfo;
import es.ull.simulation.model.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ProgressListener.
 */
class ProgressListenerTest {
    private Simulation simulation;
    private ProgressListener progressListener;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        // Capture System.out for testing print statements
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void shouldCreateProgressListener_withEndTimestamp() {
        progressListener = new ProgressListener(1000L);

        assertNotNull(progressListener);
        assertEquals("Progress", progressListener.toString());
    }

    @Test
    void shouldCreateProgressListener_withSmallEndTimestamp() {
        progressListener = new ProgressListener(100L);

        assertNotNull(progressListener);
        assertEquals("Progress", progressListener.toString());
    }

    @Test
    void shouldCreateProgressListener_withLargeEndTimestamp() {
        progressListener = new ProgressListener(10000L);

        assertNotNull(progressListener);
        assertEquals("Progress", progressListener.toString());
    }

    @Test
    void shouldAddTargetInformationClasses() {
        progressListener = new ProgressListener(1000L);

        assertTrue(progressListener.getTargetInformation().contains(TimeChangeInfo.class));
        assertTrue(progressListener.getTargetInformation().contains(SimulationStartStopInfo.class));
        assertEquals(2, progressListener.getTargetInformation().size());
    }

    @Test
    void shouldPrintStartingMessage_whenSimulationStarts() {
        progressListener = new ProgressListener(1000L);
        SimulationStartStopInfo startInfo = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.START, 0L);

        progressListener.infoEmited(startInfo);

        String output = outputStream.toString();
        assertTrue(output.contains("Starting!!"));
    }

    @Test
    void shouldNotPrintStartingMessage_whenSimulationEnds() {
        progressListener = new ProgressListener(1000L);
        SimulationStartStopInfo endInfo = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.END, 1000L);

        progressListener.infoEmited(endInfo);

        String output = outputStream.toString();
        assertFalse(output.contains("Starting!!"));
    }

    @Test
    void shouldPrintPercentage_whenTimeChangeReachesGap() {
        progressListener = new ProgressListener(100L); // gap = 1
        TimeChangeInfo timeInfo = new TimeChangeInfo(simulation, 1L);

        progressListener.infoEmited(timeInfo);

        String output = outputStream.toString();
        assertTrue(output.contains("1%"));
    }

    @Test
    void shouldNotPrintPercentage_whenTimeBelowGap() {
        progressListener = new ProgressListener(1000L); // gap = 10
        TimeChangeInfo timeInfo = new TimeChangeInfo(simulation, 5L);

        progressListener.infoEmited(timeInfo);

        String output = outputStream.toString();
        assertFalse(output.contains("%"));
    }

    @Test
    void shouldPrintMultiplePercentages_forIncrementalTimeChanges() {
        progressListener = new ProgressListener(100L); // gap = 1

        progressListener.infoEmited(new TimeChangeInfo(simulation, 1L));
        progressListener.infoEmited(new TimeChangeInfo(simulation, 2L));
        progressListener.infoEmited(new TimeChangeInfo(simulation, 3L));

        String output = outputStream.toString();
        assertTrue(output.contains("1%"));
        assertTrue(output.contains("2%"));
        assertTrue(output.contains("3%"));
    }

    @Test
    void shouldExtendBasicListener() {
        progressListener = new ProgressListener(1000L);

        assertTrue(progressListener instanceof BasicListener);
    }

    @Test
    void shouldHaveCorrectDescription() {
        progressListener = new ProgressListener(1000L);

        assertEquals("Progress", progressListener.toString());
    }

    @Test
    void shouldImplementIListener() {
        progressListener = new ProgressListener(1000L);

        assertTrue(progressListener instanceof IListener);
    }

    @Test
    void shouldCreateMultipleProgressListeners_withDifferentEndTimes() {
        ProgressListener listener1 = new ProgressListener(100L);
        ProgressListener listener2 = new ProgressListener(1000L);
        ProgressListener listener3 = new ProgressListener(10000L);

        assertNotNull(listener1);
        assertNotNull(listener2);
        assertNotNull(listener3);
        assertEquals("Progress", listener1.toString());
        assertEquals("Progress", listener2.toString());
        assertEquals("Progress", listener3.toString());
    }
}
