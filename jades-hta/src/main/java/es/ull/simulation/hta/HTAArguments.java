/**
 * 
 */
package es.ull.simulation.hta;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.ParametersDelegate;

import es.ull.simulation.experiment.CommonArguments;

/**
 * Commonly used input arguments for creating a Health Technology Assessment application. The configuration is passed through a JSON file.
 * @author Iván Castilla
 */
public class HTAArguments {

	@ParametersDelegate
	public CommonArguments commonArgs = new CommonArguments();
	@Parameter(names = { "--debugPatient", "-dp" }, description = "Identifiers of patients to debug", validateWith = PatientListValidator.class, converter = PatientListConverter.class, order = 2)
	public List<Integer> debugPatients = new java.util.ArrayList<>();
	@Parameter(names = { "--config", "-c" }, description = "Configuration file, according to the JSON schema", order = 1)
	public String configFile = null;
	@Parameter(names = { "--outputs", "-out" }, description = "Configuration file for outputs, according to the JSON schema", order = 1)
	public String outputsConfigFile = null;

	/**
	 * 
	 */
	public HTAArguments() {
	}

	public static class PatientListValidator implements IParameterValidator {
		@Override
		public void validate(String name, String value) throws ParameterException {
			String[] values = value.split(",");
			for (String val : values) {
				int d = Integer.parseInt(val);
				if (d < 0)
					throw new IllegalArgumentException("The patient id must be a positive integer: " + val);
			}
		}
	}	

	public static class PatientListConverter implements IStringConverter<List<Integer>> {
		@Override
		public List<Integer> convert(String value) {
			String[] values = value.split(",");
			List<Integer> result = new ArrayList<>();
			for (String val : values)
				result.add(Integer.parseInt(val));
			return result;
		}
	}
}
