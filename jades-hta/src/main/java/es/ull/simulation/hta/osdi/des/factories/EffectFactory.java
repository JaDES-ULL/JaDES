package es.ull.simulation.hta.osdi.des.factories;

import java.util.Set;

import es.ull.simulation.hta.interventions.Intervention;
import es.ull.simulation.hta.osdi.des.OSDiDESModel;
import es.ull.simulation.hta.osdi.ontology.EffectWrapper;
import es.ull.simulation.hta.osdi.ontology.IModelItemWrapper;
import es.ull.simulation.hta.osdi.ontology.InterventionWrapper;
import es.ull.simulation.hta.params.Parameter;
import es.ull.simulation.hta.params.ParameterGroup;
import es.ull.simulation.hta.params.modifiers.CompositeParameterModifier;
import es.ull.simulation.hta.params.modifiers.OperationParameterModifier;
import es.ull.simulation.hta.params.modifiers.ParameterModifier;

public interface EffectFactory extends DESModelComponentFactory {
	/**
	 * Creates the parameter that represents the magnitude of an effect, and registers in the model a modifier for each item affected by
	 * the effect. The modifier applies the operation defined by {@link EffectWrapper#getOperation(IModelItemWrapper)} (see decision D3
	 * in docs/osdi-1.3-diseno.md). When several effects of the same intervention modify the same item, their modifiers are combined into a
	 * {@link CompositeParameterModifier}, which applies them in the order: replace, multiply, subtract.
	 * Currently, only interventions are considered as causes of the effects.
	 * @param model The model to which the parameter modifier will be added.
	 * @param effectWrapper The wrapper containing parameter modifier details.
	 * @param type The type of the parameter (e.g., COST, UTILITY, etc.).
	 * @return A Parameter instance corresponding to the provided wrapper.
	 */
    public static Parameter getParameterModifierInstance(OSDiDESModel model, EffectWrapper effectWrapper, ParameterGroup type) {
		final Parameter param = ParameterFactory.getParameterInstance(model, effectWrapper.getEffectMagnitude(), type);
        final Set<IModelItemWrapper> modifiedItems = effectWrapper.getModifiedItems();
        final Set<IModelItemWrapper> causes = effectWrapper.getCauses();
		// TODO: Process all the causes. Currently, only interventions are considered as causes
		final Set<IModelItemWrapper> interventionCauses = causes.stream().filter(c -> c instanceof InterventionWrapper).collect(java.util.stream.Collectors.toSet());
		for (IModelItemWrapper modifiedParam : modifiedItems) {
			final OperationParameterModifier mod = new OperationParameterModifier(effectWrapper.getOperation(modifiedParam), model, param.name());
			for (IModelItemWrapper interventionCause : interventionCauses) {
				final Intervention intervention = model.getIntervention(interventionCause.getShortName());
				final ParameterModifier previous = model.getParameterModifier(modifiedParam.getShortName(), intervention);
				if (previous instanceof CompositeParameterModifier) {
					((CompositeParameterModifier) previous).add(mod);
				}
				else {
					final CompositeParameterModifier composite = new CompositeParameterModifier();
					composite.add(mod);
					model.addParameterModifier(modifiedParam.getShortName(), intervention, composite);
				}
			}
		}
		return param;
    }


}
