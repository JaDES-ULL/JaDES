package es.ull.simulation.inforeceiver;

import es.ull.simulation.info.*;
import es.ull.simulation.model.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for StdInfoListener.
 */
class StdInfoListenerTest {
    private Simulation simulation;
    private StdInfoListener stdInfoListener;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
        // Capture System.out for testing print statements
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void shouldCreateStdInfoListener_withDefaultConstructor() {
        stdInfoListener = new StdInfoListener();
        
        assertNotNull(stdInfoListener);
        assertEquals("STANDARD INFO VIEW", stdInfoListener.toString());
    }

    @Test
    void shouldCreateStdInfoListener_withCustomPrintStream() {
        PrintStream customStream = new PrintStream(outputStream);
        stdInfoListener = new StdInfoListener(customStream);
        
        assertNotNull(stdInfoListener);
        assertEquals("STANDARD INFO VIEW", stdInfoListener.toString());
    }

    @Test
    void shouldAddMultipleTargetInformationClasses() {
        stdInfoListener = new StdInfoListener();
        
        assertTrue(stdInfoListener.getTargetInformation().contains(SimulationStartStopInfo.class));
        assertTrue(stdInfoListener.getTargetInformation().contains(ElementActionInfo.class));
        assertTrue(stdInfoListener.getTargetInformation().contains(ElementInfo.class));
        assertTrue(stdInfoListener.getTargetInformation().contains(ResourceInfo.class));
        assertTrue(stdInfoListener.getTargetInformation().contains(ResourceUsageInfo.class));
        assertTrue(stdInfoListener.getTargetInformation().contains(EntityLocationInfo.class));
        assertEquals(6, stdInfoListener.getTargetInformation().size());
    }

    @Test
    void shouldPrintSimulationStartInfo() {
        System.setOut(new PrintStream(outputStream));
        stdInfoListener = new StdInfoListener();
        SimulationStartStopInfo startInfo = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.START, 0L);
        
        stdInfoListener.infoEmited(startInfo);
        
        String output = outputStream.toString();
        assertTrue(output.contains("SIMULATION STARTS"));
    }

    @Test
    void shouldPrintSimulationEndInfo_withCpuTime() {
        System.setOut(new PrintStream(outputStream));
        stdInfoListener = new StdInfoListener();
        
        // First emit START to initialize cpuTime
        SimulationStartStopInfo startInfo = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.START, 0L);
        stdInfoListener.infoEmited(startInfo);
        
        // Then emit END
        SimulationStartStopInfo endInfo = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.END, 1000L);
        stdInfoListener.infoEmited(endInfo);
        
        String output = outputStream.toString();
        assertTrue(output.contains("SIMULATION ENDS"));
        assertTrue(output.contains("CPU Time"));
        assertTrue(output.contains("miliseconds"));
    }

    @Test
    void shouldPrintToCustomOutputStream() {
        PrintStream customStream = new PrintStream(outputStream);
        stdInfoListener = new StdInfoListener(customStream);
        SimulationStartStopInfo startInfo = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.START, 0L);
        
        stdInfoListener.infoEmited(startInfo);
        
        String output = outputStream.toString();
        assertTrue(output.contains("SIMULATION STARTS"));
    }

    @Test
    void shouldExtendBasicListener() {
        stdInfoListener = new StdInfoListener();
        
        assertTrue(stdInfoListener instanceof BasicListener);
    }

    @Test
    void shouldHaveCorrectDescription() {
        stdInfoListener = new StdInfoListener();
        
        assertEquals("STANDARD INFO VIEW", stdInfoListener.toString());
    }

    @Test
    void shouldImplementIListener() {
        stdInfoListener = new StdInfoListener();
        
        assertTrue(stdInfoListener instanceof IListener);
    }

    @Test
    void shouldPrintTimeChangeInfo() {
        System.setOut(new PrintStream(outputStream));
        stdInfoListener = new StdInfoListener();
        TimeChangeInfo timeInfo = new TimeChangeInfo(simulation, 100L);
        
        stdInfoListener.infoEmited(timeInfo);
        
        String output = outputStream.toString();
        assertFalse(output.isEmpty());
    }

    @Test
    void shouldCreateMultipleStdInfoListeners() {
        StdInfoListener listener1 = new StdInfoListener();
        StdInfoListener listener2 = new StdInfoListener(System.out);
        PrintStream customStream = new PrintStream(outputStream);
        StdInfoListener listener3 = new StdInfoListener(customStream);
        
        assertNotNull(listener1);
        assertNotNull(listener2);
        assertNotNull(listener3);
        assertEquals("STANDARD INFO VIEW", listener1.toString());
        assertEquals("STANDARD INFO VIEW", listener2.toString());
        assertEquals("STANDARD INFO VIEW", listener3.toString());
    }

    @Test
    void shouldHandleMultipleSimulationCycles() {
        System.setOut(new PrintStream(outputStream));
        stdInfoListener = new StdInfoListener();
        
        // First simulation cycle
        SimulationStartStopInfo start1 = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.START, 0L);
        stdInfoListener.infoEmited(start1);
        SimulationStartStopInfo end1 = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.END, 100L);
        stdInfoListener.infoEmited(end1);
        
        // Second simulation cycle
        SimulationStartStopInfo start2 = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.START, 200L);
        stdInfoListener.infoEmited(start2);
        SimulationStartStopInfo end2 = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.END, 300L);
        stdInfoListener.infoEmited(end2);
        
        String output = outputStream.toString();
        assertTrue(output.contains("CPU Time"));
        // Should have two CPU Time messages
        int count = output.split("CPU Time", -1).length - 1;
        assertEquals(2, count);
    }
}
