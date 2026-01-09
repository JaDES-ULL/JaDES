package es.ull.simulation.inforeceiver;

import es.ull.simulation.info.TimeChangeInfo;
import es.ull.simulation.model.Simulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DelayListener.
 */
class DelayListenerTest {
    private Simulation simulation;
    private DelayListener delayListener;

    @BeforeEach
    void setUp() {
        simulation = new Simulation(1, "Test Simulation");
    }

    @Test
    void shouldCreateDelayListener_withTimeout() {
        delayListener = new DelayListener(1);

        assertNotNull(delayListener);
        assertEquals("Delay listener", delayListener.toString());
    }

    @Test
    void shouldCreateDelayListener_withZeroTimeout() {
        delayListener = new DelayListener(0);

        assertNotNull(delayListener);
        assertEquals("Delay listener", delayListener.toString());
    }

    @Test
    void shouldCreateDelayListener_withLargeTimeout() {
        delayListener = new DelayListener(100);

        assertNotNull(delayListener);
        assertEquals("Delay listener", delayListener.toString());
    }

    @Test
    void shouldAddTimeChangeInfoAsTargetInformation() {
        delayListener = new DelayListener(1);

        assertTrue(delayListener.getTargetInformation().contains(TimeChangeInfo.class));
        assertEquals(1, delayListener.getTargetInformation().size());
    }

    @Test
    void shouldHaveCorrectDescription() {
        delayListener = new DelayListener(5);

        assertEquals("Delay listener", delayListener.toString());
    }

    @Test
    void shouldCallInfoEmited_withTimeChangeInfo() {
        delayListener = new DelayListener(0); // Use 0 timeout for fast test
        TimeChangeInfo info = new TimeChangeInfo(simulation, 100L);

        long startTime = System.currentTimeMillis();
        delayListener.infoEmited(info);
        long endTime = System.currentTimeMillis();

        // Should complete quickly with 0 timeout
        assertTrue(endTime - startTime < 1000);
    }

    @Test
    void shouldExtendBasicListener() {
        delayListener = new DelayListener(1);

        assertTrue(delayListener instanceof BasicListener);
    }

    @Test
    void shouldCreateMultipleDelayListeners_withDifferentTimeouts() {
        DelayListener listener1 = new DelayListener(1);
        DelayListener listener2 = new DelayListener(2);
        DelayListener listener3 = new DelayListener(3);

        assertNotNull(listener1);
        assertNotNull(listener2);
        assertNotNull(listener3);
        assertEquals("Delay listener", listener1.toString());
        assertEquals("Delay listener", listener2.toString());
        assertEquals("Delay listener", listener3.toString());
    }

    @Test
    void shouldImplementIListener() {
        delayListener = new DelayListener(1);

        assertTrue(delayListener instanceof IListener);
    }
}
