package es.ull.simulation.info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import es.ull.simulation.model.Simulation;

class TimeStampedInfoTest {

    @Test
    void shouldReturnTimestamp() {
        DummyTimeStampedInfo info = new DummyTimeStampedInfo(123L);

        assertEquals(123L, info.getTs());
    }

    @Test
    void shouldReturnSimulationFromSimulationInfo() {
        Simulation simulation = new Simulation(1, "Test Simulation");
        DummySimulationInfo info = new DummySimulationInfo(simulation, 10L);

        assertSame(simulation, info.getSimul());
        assertEquals(10L, info.getTs());
    }

    private static final class DummyTimeStampedInfo extends TimeStampedInfo {
        private DummyTimeStampedInfo(long ts) {
            super(ts);
        }
    }

    private static final class DummySimulationInfo extends SimulationInfo {
        private DummySimulationInfo(Simulation simul, long ts) {
            super(simul, ts);
        }
    }
}
