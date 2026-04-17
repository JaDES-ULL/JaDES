package es.ull.simulation.hta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.info.IPieceOfInformation;
import es.ull.simulation.info.SimulationStartStopInfo;
import es.ull.simulation.inforeceiver.BasicListener;

public class DeathEventChecker extends BasicListener {
    private final long expectedDeathTs;
    private boolean checked;
    public DeathEventChecker(long expectedDeathTs) {
        super("Disease 0 listener");
        this.expectedDeathTs = expectedDeathTs;
        this.checked = false;
		addTargetInformation(PatientInfo.class);
        addTargetInformation(SimulationStartStopInfo.class);
    }

    @Override
    public void infoEmited(IPieceOfInformation info) {
        if (info instanceof PatientInfo) {
            final PatientInfo pInfo = (PatientInfo) info;
            if (PatientInfo.Type.DEATH.equals(pInfo.getType())) {
                assertEquals(expectedDeathTs, pInfo.getTs(), "Unexpected death time");
                checked = true;
            }
        }
        else if (info instanceof SimulationStartStopInfo) {
            if (((SimulationStartStopInfo) info).getType() == SimulationStartStopInfo.Type.END) {
                assertTrue(checked, "Death event not found");
            }
        }
    }
}