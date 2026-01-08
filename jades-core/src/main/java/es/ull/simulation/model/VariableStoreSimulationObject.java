/**
 * 
 */
package es.ull.simulation.model;

import java.util.TreeMap;

import es.ull.simulation.variable.BooleanVariable;
import es.ull.simulation.variable.ByteVariable;
import es.ull.simulation.variable.CharacterVariable;
import es.ull.simulation.variable.DoubleVariable;
import es.ull.simulation.variable.FloatVariable;
import es.ull.simulation.variable.IntVariable;
import es.ull.simulation.variable.LongVariable;
import es.ull.simulation.variable.ShortVariable;
import es.ull.simulation.variable.IUserVariable;
import es.ull.simulation.variable.IVariable;

/**
 * A simulation object that can handle {@link IVariable variables}.
 * @author Iván Castilla Rodríguez
 *
 */
public abstract class VariableStoreSimulationObject extends SimulationObject implements IVariableStore {
    /** Variable warehouse */
	protected final TreeMap<String, IVariable> varCollection = new TreeMap<String, IVariable>();

	/**
	 * Creates a simulation object capable of using {@link IVariable variables}
	 * @param simul Simulation this object belongs to
	 * @param id Object identifier
     * @param objectTypeId a String that identifies the type of simulation object
	 */
	public VariableStoreSimulationObject(final Simulation simul, final int id, final String objectTypeId) {
		super(simul, id, objectTypeId);
	}

	@Override
	public IVariable getVar(final String varName) {
		return varCollection.get(varName);
	}
	
	@Override
	public void putVar(final String varName, final IVariable value) {
		varCollection.put(varName, value);
	}
	
	@Override
	public void putVar(final String varName, final double value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new DoubleVariable(value));
	}
	
	@Override
	public void putVar(final String varName, final int value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new IntVariable(value));
	}

	@Override
	public void putVar(final String varName, final boolean value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new BooleanVariable(value));
	}

	@Override
	public void putVar(final String varName, final char value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new CharacterVariable(value));
	}
	
	@Override
	public void putVar(final String varName, final byte value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new ByteVariable(value));
	}

	@Override
	public void putVar(final String varName, final float value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new FloatVariable(value));
	}
	
	@Override
	public void putVar(final String varName, final long value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new LongVariable(value));
	}
	
	@Override
	public void putVar(final String varName, final short value) {
		IUserVariable v = (IUserVariable) varCollection.get(varName);
		if (v != null) {
			v.setValue(value);
			varCollection.put(varName, v);
		} else
			varCollection.put(varName, new ShortVariable(value));
	}
	
}
