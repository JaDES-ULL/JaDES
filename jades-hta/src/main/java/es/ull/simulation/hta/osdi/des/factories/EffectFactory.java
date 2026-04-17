package es.ull.simulation.hta.osdi.des.factories;

import java.util.Set;

import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.params.Parameter;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.hta.params.modifiers.DiffParameterModifier;
import es.ull.simulation.hta.params.modifiers.FactorParameterModifier;
import es.ull.simulation.hta.params.modifiers.ParameterModifier;
import es.ull.simulation.hta.params.modifiers.SetParameterModifier;

public interface EffectFactory extends DESModelComponentFactory {
	/**
	 * Creates a parameter modifier instance based on the provided ParameterModifierWrapper and type.
	 * This method also registers the modifier with the model for the specified modified items.
	 * @param model The model to which the parameter modifier will be added.
	 * @param effectWrapper The wrapper containing parameter modifier details.
	 * @param type The type of the parameter (e.g., COST, UTILITY, etc.).
	 * @return A Parameter instance corresponding to the provided wrapper.
	 */
    public static Parameter getParameterModifierInstance(OSDiDESModel model, EffectWrapper effectWrapper, ParameterGroup type) {
		final Parameter param = ParameterFactory.getParameterInstance(model, effectWrapper.getEffectMagnitude(), type);
		ParameterModifier mod = null;
		switch(effectWrapper.getMagnitudeType()) {
		case DIFF:
			mod = new DiffParameterModifier(param.name());
			break;
		case FACTOR:
			mod = new FactorParameterModifier(param.name());
			break;
		case SET:
			mod = new SetParameterModifier(param.name());
			break;
		default: // Should never occur
			mod = new SetParameterModifier(param.name());			
			break;		
		}
        final Set<IModelItemWrapper> modifiedItems = effectWrapper.getModifiedItems();
        final Set<IModelItemWrapper> causes = effectWrapper.getCauses();
		// TODO: Process all the causes. Currently, only interventions are considered as causes
		final Set<IModelItemWrapper> interventionCauses = causes.stream().filter(c -> c instanceof InterventionWrapper).collect(java.util.stream.Collectors.toSet());
		for (IModelItemWrapper modifiedParam : modifiedItems) {
			for (IModelItemWrapper interventionCause : interventionCauses) {
				model.addParameterModifier(modifiedParam.getShortName(), model.getIntervention(interventionCause.getShortName()), mod);
			}
		}
		return param;
    }


}
