package es.ull.simulation.info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Simulation;

class SimulationStartStopInfoTest {

    @Test
    void shouldExposeTypeAndCpuTime() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        SimulationStartStopInfo info = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.START, 5L);

        assertEquals(SimulationStartStopInfo.Type.START, info.getType());
        assertTrue(info.getCpuTime() > 0L);
    }

    @Test
    void shouldFormatToStringWithDescription() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        SimulationStartStopInfo info = new SimulationStartStopInfo(simulation, SimulationStartStopInfo.Type.END, 10L);

        String text = info.toString();

        assertNotNull(text);
        assertTrue(text.contains("SIM"));
        assertTrue(text.contains("SIMULATION ENDS"));
    }
}
