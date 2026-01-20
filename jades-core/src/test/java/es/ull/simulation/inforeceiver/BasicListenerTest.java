package es.ull.simulation.inforeceiver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import es.ull.simulation.info.IPieceOfInformation;

class BasicListenerTest {

    @Test
    void shouldManageTargetInformationAndDescription() {
        DummyListener listener = new DummyListener("Test Listener");

        listener.addTargetInformation(DummyInfo.class);

        assertEquals("Test Listener", listener.getDescription());
        assertEquals("Test Listener", listener.toString());
        assertEquals(1, listener.getTargetInformation().size());
        assertTrue(listener.getTargetInformation().contains(DummyInfo.class));
    }

    private static final class DummyListener extends BasicListener {
        private DummyListener(String description) {
            super(description);
        }

        @Override
        public void infoEmited(IPieceOfInformation info) {
        }
    }

    private static final class DummyInfo implements IPieceOfInformation {
    }
}
