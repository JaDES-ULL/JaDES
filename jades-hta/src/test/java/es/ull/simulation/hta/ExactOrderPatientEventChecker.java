package es.ull.simulation.hta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;

import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;

public class ExactOrderPatientEventChecker extends BasicListener {
    public static class PatientEvent {
        private final long ts;
        private final PatientInfo.Type type;
        private final int patientId;
    
        public PatientEvent(int patientId, long ts, PatientInfo.Type type) {
            this.ts = ts;
            this.type = type;
            this.patientId = patientId;
        }
        /**
         * @return the ts
         */
        public long getTs() {
            return ts;
        }
        /**
         * @return the type
         */
        public PatientInfo.Type getType() {
            return type;
        }
        /**
         * @return the patientId
         */
        public int getPatientId() {
            return patientId;
        }
    
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PatientEvent) {
                final PatientEvent ev = (PatientEvent) obj;
                return ev.getPatientId() == patientId && ev.getTs() == ts && ev.getType().equals(type);
            }
            else if (obj instanceof PatientInfo) {
                final PatientInfo pInfo = (PatientInfo) obj;
                return pInfo.getPatient().getIdentifier() == patientId && pInfo.getTs() == ts && pInfo.getType().equals(type);
            }
            return false;
        }
    
        @Override
        public String toString() {
            return ts + " Days\t[PAT" + patientId + "]\t" + type;
        }
    }

    private final ArrayList<ExactOrderPatientEventChecker.PatientEvent> expectedEvents;
    public ExactOrderPatientEventChecker(ArrayList<ExactOrderPatientEventChecker.PatientEvent> expectedEvents) {
        super("Disease 0 listener");
        this.expectedEvents = expectedEvents;
		addTargetInformation(PatientInfo.class);
        addTargetInformation(SimulationStartStopInfo.class);
    }

    @Override
    public void infoEmited(IPieceOfInformation info) {
        if (info instanceof PatientInfo) {
            assertNotEquals(0, expectedEvents.size(), "Unexpected event: " + info);
            final ExactOrderPatientEventChecker.PatientEvent ev = expectedEvents.remove(0);
            final PatientInfo pInfo = (PatientInfo) info;
            assertEquals(ev, pInfo);
        }
        else if (info instanceof SimulationStartStopInfo) {
            if (((SimulationStartStopInfo) info).getType() == SimulationStartStopInfo.Type.END) {
                assertEquals(0, expectedEvents.size(), "Missing events: " + expectedEvents.size());
            }
        }
    }
}