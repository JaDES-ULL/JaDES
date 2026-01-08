/**
 * 
 */
package es.ull.simulation.model.flow;

import es.ull.simulation.functions.AbstractTimeFunction;
import es.ull.simulation.functions.TimeFunctionFactory;
import es.ull.simulation.model.Element;
import es.ull.simulation.model.Simulation;

/**
 * A IFlow that makes an element be delayed for a certain time, specified at modeling time
 * @author Iván Castilla Rodríguez
 *
 */
public class TimeFunctionDelayFlow extends DelayFlow {
	/** Duration of the delay */
    private final AbstractTimeFunction duration;

    /**
     * Creates a delay IFlow
     * @param model The simulation model this IFlow belongs to
     * @param description A short text describing this IFlow
     * @param duration The duration of the delay
     */
	public TimeFunctionDelayFlow(final Simulation model, final String description, final AbstractTimeFunction duration) {
		super(model, description);
		this.duration = duration;
	}

    /**
     * Creates a delay IFlow
     * @param model The simulation model this IFlow belongs to
     * @param description A short text describing this IFlow
     * @param duration The duration of the delay
     */
	public TimeFunctionDelayFlow(final Simulation model, final String description, final long duration) {
		this(model, description, TimeFunctionFactory.getInstance("ConstantVariate", duration));
	}
    
	/**
	 * Returns the time function that characterizes the duration of the delay
	 * @return the time function that characterizes the duration of the delay
	 */
	public AbstractTimeFunction getDuration() {
		return duration;
	}

    /**
     * Returns the duration of the delay
     * The value returned by the random number function could be negative. In this case, it returns 0.
     * @param elem The element delaying
     * @return The duration of the delay
     */
	@Override
    public long getDurationSample(final Element elem) {
    	return Math.max(0, Math.round(getDuration().getValue(elem)));
    }

}
