/**
 * 
 */
package es.ull.simulation.hta.interventions;

import java.util.ArrayList;
import es.ull.simulation.hta.DiseaseProgressionSimulation;
import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.Patient;
import es.ull.simulation.hta.info.PatientInfo;
import es.ull.simulation.hta.outcomes.ScreeningStrategy;
import es.ull.simulation.hta.outcomes.Strategy;
import es.ull.simulation.hta.params.DefinesSensitivityAndSpecificity;
import es.ull.simulation.model.DiscreteEvent;

/**
 * A screening intervention to detect a disease 
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class ScreeningIntervention extends Intervention implements DefinesSensitivityAndSpecificity {
	
	/**
	 * 
	 */
	public ScreeningIntervention(HTAModel model, String name, String description) {
		this(model, name, description, null);
	}
	
	/**
	 * 
	 */
	public ScreeningIntervention(HTAModel model, String name, String description, ScreeningStrategy strategy) {
		super(model, name, description, strategy);
	}
	
	@Override
	public ArrayList<DiscreteEvent> getEvents(Patient pat) {
		final ArrayList<DiscreteEvent> eventList = new ArrayList<>();
		if (getStrategy() == null)
			eventList.add(new ScreeningEvent(pat.getTs(), pat));
		else 
			eventList.add(new StrategyScreeningEvent(pat.getTs(), pat, getStrategy()));
		return eventList;
	}
	
	public class ScreeningEvent extends DiscreteEvent {
		private final Patient pat;
		
		public ScreeningEvent(long ts, Patient pat) {
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
					result = (pat.getRandomNumber(name()) >= getSpecificity(ScreeningIntervention.this, pat)) ? DetectionTestResult.FP : DetectionTestResult.TN;
				}
				else {
					result = (pat.getRandomNumber(name()) >= getSensitivity(ScreeningIntervention.this, pat)) ? DetectionTestResult.FN : DetectionTestResult.TP;					
				}
				simul.notifyInfo(new PatientInfo(simul, pat, PatientInfo.Type.SCREEN, result, this.getTs()));
				switch(result) {
				case TP:
					pat.setDiagnosed(true);
				case FP:
					simul.notifyInfo(new PatientInfo(simul, pat, PatientInfo.Type.DIAGNOSIS, ScreeningIntervention.this, this.getTs()));
					break;
				case FN:
				case TN:
				default:
					break;
				
				}
			}
		}
		
	}
	
	public class StrategyScreeningEvent extends DiscreteEvent {
		private final Patient pat;
		private final Strategy strategyStage;
		
		public StrategyScreeningEvent(long ts, Patient pat, Strategy strategyStage) {
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
					result = (pat.getRandomNumber(name() + "_" + strategyStage.name()) >= ((ScreeningStrategy)getStrategy()).getSpecificity(ScreeningIntervention.this, pat)) ? DetectionTestResult.FP : DetectionTestResult.TN;
				}
				else {
					result = (pat.getRandomNumber(name() + "_" + strategyStage.name()) >= ((ScreeningStrategy)getStrategy()).getSensitivity(ScreeningIntervention.this, pat)) ? DetectionTestResult.FN : DetectionTestResult.TP;					
				}
				simul.notifyInfo(new PatientInfo(simul, pat, PatientInfo.Type.SCREEN, result, this.getTs()));
				switch(result) {
				case TP:
					pat.setDiagnosed(true);
				case FP:
					simul.notifyInfo(new PatientInfo(simul, pat, PatientInfo.Type.DIAGNOSIS, ScreeningIntervention.this, this.getTs()));
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
