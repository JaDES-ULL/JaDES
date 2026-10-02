package es.ull.simulation.hta.outcomes;

import es.ull.simulation.hta.HTAModel;
import es.ull.simulation.hta.params.ParameterTemplate;
import es.ull.simulation.hta.params.UsesParameters;

/**
 * Utility methods to identify the parameter that actually defines a payoff of a model component in a discrete-event simulation.
 * These keys are used by {@link ActiveItemsCombiner} to detect parameters shared by several simultaneously active items.
 * @author Iván Castilla Rodríguez
 */
public final class PayoffKeys {

	private PayoffKeys() {
	}

	/**
	 * Returns the name of the first parameter, among those used by the component for the specified templates, that is actually
	 * defined in the model. Components that share a parameter defined in the model (e.g., two manifestations that point to the same
	 * cost individual in the ontology) return the same key. Returns null if none of the templates is bound to a parameter defined in
	 * the model (i.e., the component is using a default value, which is never considered as shared).
	 * @param model The model
	 * @param component A model component that uses parameters
	 * @param templates The templates to check, in order of preference (e.g. a disutility and its equivalent utility)
	 * @return The name of the parameter that defines the payoff, or null if the component is using a default value
	 */
	public static String getKey(HTAModel model, UsesParameters component, ParameterTemplate... templates) {
		for (ParameterTemplate template : templates) {
			final String name = component.getUsedParameterName(template);
			if (name != null && model.getParameters().containsKey(name)) {
				return name;
			}
		}
		return null;
	}
}
