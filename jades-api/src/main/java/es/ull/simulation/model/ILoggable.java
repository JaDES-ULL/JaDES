/**
 * 
 */
package es.ull.simulation.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Indicates that an object can print debug messages
 * @author Iván Castilla Rodríguez
 *
 */
public interface ILoggable {
	final Logger logger = LogManager.getLogger();
}
