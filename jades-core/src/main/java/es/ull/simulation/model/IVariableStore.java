package es.ull.simulation.model;

import es.ull.simulation.variable.IVariable;

/**
 * An object capable to store {@link IVariable variables}
 * @author Iván Castilla Rodríguez
 *
 */
public interface IVariableStore
{
	/**
	 * Returns a simulation's variable.
	 * @param varName Variable name.
	 * @return The Variable.
	 */
	public IVariable getVar(final String varName);
	
	/**
	 * Assigns a value to a variable.
	 * @param varName Variable name.
	 * @param value The new value.
	 */
	public void putVar(final String varName, final IVariable value);
	
	/**
	 * Assigns a value to a variable.
	 * @param varName Variable name.
	 * @param value The new value.
	 */
	public void putVar(final String varName, final double value);
	
	/**
	 * Assigns a value to a variable.
	 * @param varName Variable name.
	 * @param value The new value.
	 */
	public void putVar(final String varName, final int value);

	/**
	 * Assigns a value to a variable.
	 * @param varName Variable name.
	 * @param value The new value.
	 */
	public void putVar(final String varName, final boolean value);
	
	/**
	 * Assigns a value to a variable.
	 * @param varName Variable name.
	 * @param value The new value.
	 */
	public void putVar(final String varName, final char value);
	
	/**
	 * Assigns a value to a variable.
	 * @param varName Variable name
	 * @param value The new value.
	 */
	public void putVar(final String varName, final byte value);

	/**
	 * Assigns a value to a variable.
	 * @param varName Variable name.
	 * @param value The new value.
	 */
	public void putVar(final String varName, final float value);
	
	/**
	 * Assigns a value to a variable.
	 * @param varName Variable name.
	 * @param value The new value.
	 */
	public void putVar(final String varName, final long value);
	
	/**
	 * Assigns a value to a variable.
	 * @param varName Variable name.
	 * @param value The new value.
	 */
	public void putVar(final String varName, final short value);
}
