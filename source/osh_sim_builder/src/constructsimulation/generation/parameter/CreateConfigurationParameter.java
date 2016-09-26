package constructsimulation.generation.parameter;

import osh.configuration.system.ConfigurationParameter;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class CreateConfigurationParameter {

	public static ConfigurationParameter createConfigurationParameter(
			String name, 
			String type, 
			String value) {
		ConfigurationParameter cp = new ConfigurationParameter();
		cp.setParameterName(name);
		cp.setParameterType(type);
		cp.setParameterValue(value);
		return cp;
	}
}
