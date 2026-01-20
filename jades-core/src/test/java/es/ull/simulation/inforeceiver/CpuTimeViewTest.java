package es.ull.simulation.inforeceiver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.model.Simulation;

class CpuTimeViewTest {
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
    }

    @Test
    void shouldReportUnavailableBeforeEnd() {
        CpuTimeView view = new CpuTimeView(System.out, false);

        SimulationStartStopInfo start = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.START, 0L);
        view.infoEmited(start);

        assertEquals(-1L, view.getCPUTime());
        assertEquals("CPU time not available (simulation not finished)", view.toString());
    }

    @Test
    void shouldComputeCpuTimeAndPrintOnEnd() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CpuTimeView view = new CpuTimeView(new PrintStream(out), true);

        SimulationStartStopInfo start = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.START, 0L);
        SimulationStartStopInfo end = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.END, 10L);

        view.infoEmited(start);
        view.infoEmited(end);

        assertTrue(view.getCPUTime() >= 0);
        assertTrue(view.toString().endsWith("ms"));
        assertTrue(out.toString().contains("ms"));
    }
}
