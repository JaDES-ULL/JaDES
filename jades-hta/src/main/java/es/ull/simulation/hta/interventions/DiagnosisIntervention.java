/**
 * 
 */
package es.ull.simulation.hta.interventions;

import java.util.ArrayList;
import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.outcomes.DiagnosisStrategy;
import es.ull.simulation.hta.outcomes.Strategy;
import es.ull.simulation.hta.params.DefinesSensitivityAndSpecificity;
import es.ull.simulation.model.DiscreteEvent;

/**
 * A diagnosis intervention to detect a disease 
 * TODO: Process complex strategies that may produce several events
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class DiagnosisIntervention extends Intervention implements DefinesSensitivityAndSpecificity {
	
	/**
	 * 
	 */
	public DiagnosisIntervention(HTAModel model, String name, String description) {
		this(model, name, description, null);
	}
	
	/**
	 * 
	 */
	public DiagnosisIntervention(HTAModel model, String name, String description, DiagnosisStrategy strategy) {
		super(model, name, description, strategy);
	}
	
	@Override
	public ArrayList<DiscreteEvent> getEvents(Patient pat) {
		final ArrayList<DiscreteEvent> eventList = new ArrayList<>();
		if (getStrategy() == null)
			eventList.add(new DiagnosisEvent(pat.getTs(), pat));
		else
			eventList.add(new StrategyDiagnosisEvent(pat.getTs(), pat, getStrategy()));			
		return eventList;
	}

	public class DiagnosisEvent extends DiscreteEvent {
		private final Patient pat;
		
		public DiagnosisEvent(long ts, Patient pat) {
			super(ts);
			this.pat = pat;
		}

		@Override
		public void event() {
			final DiseaseProgressionSimulation simul = pat.getSimulation();
			// If the patient is already diagnosed, no sense in performing screening
			if (!pat.isDiagnosed()) {
				DetectionTestResult result;
				// Healthy patients can be wrongly identified as false positives 
				if (pat.isHealthy()) {
					result = (pat.getRandomNumber(name()) >= getSpecificity(DiagnosisIntervention.this, pat)) ? DetectionTestResult.FP : DetectionTestResult.TN;
				}
				else {
					result = (pat.getRandomNumber(name()) >= getSensitivity(DiagnosisIntervention.this, pat)) ? DetectionTestResult.FN : DetectionTestResult.TP;					
				}
				simul.notifyInfo(new PatientInfo(simul, pat, PatientInfo.Type.SCREEN, result, this.getTs()));
				switch(result) {
				case TP:
					pat.setDiagnosed(true);
				case FP:
					simul.notifyInfo(new PatientInfo(simul, pat, PatientInfo.Type.DIAGNOSIS, DiagnosisIntervention.this, this.getTs()));
					break;
				case FN:
				case TN:
				default:
					break;
				
				}
			}
		}
		
	}
	
	public class StrategyDiagnosisEvent extends DiscreteEvent {
		private final Patient pat;
		private final Strategy strategyStage;
		
		public StrategyDiagnosisEvent(long ts, Patient pat, Strategy strategyStage) {
			super(ts);
			this.pat = pat;
			this.strategyStage = strategyStage;
		}

		@Override
		public void event() {
			final DiseaseProgressionSimulation simul = pat.getSimulation();
			// If the patient is already diagnosed, no sense in performing screening
			if (!pat.isDiagnosed() && strategyStage.getCondition().check(pat)) {
				DetectionTestResult result;
				// Healthy patients can be wrongly identified as false positives 
				if (pat.isHealthy()) {
					result = (pat.getRandomNumber(name() + "_" + strategyStage.name()) >= ((DiagnosisStrategy)getStrategy()).getSpecificity(DiagnosisIntervention.this, pat)) ? DetectionTestResult.FP : DetectionTestResult.TN;
				}
				else {
					result = (pat.getRandomNumber(name() + "_" + strategyStage.name()) >= ((DiagnosisStrategy)getStrategy()).getSensitivity(DiagnosisIntervention.this, pat)) ? DetectionTestResult.FN : DetectionTestResult.TP;					
				}
				simul.notifyInfo(new PatientInfo(simul, pat, PatientInfo.Type.SCREEN, result, this.getTs()));
				switch(result) {
				case TP:
					pat.setDiagnosed(true);
				case FP:
					simul.notifyInfo(new PatientInfo(simul, pat, PatientInfo.Type.DIAGNOSIS, DiagnosisIntervention.this, this.getTs()));
					break;
				case FN:
				case TN:
				default:
					break;
				
				}
			}
		}
		
	}
}
