package osh.toolbox.appliance;

/**
 * 
 * @author mauser
 *
 */
public class ToolApplianceConfiguration {
	
	public int configurationID;
	
	public ToolApplianceConfigurationProgram program;
	
	public ToolApplianceConfigurationExtra[] extras;
	
	public ToolApplianceConfigurationProfile[] profiles;
	
	
	/**
	 * 
	 * @param configurationID
	 * @param program
	 * @param extras
	 */
	public ToolApplianceConfiguration(
			int configurationID,
			ToolApplianceConfigurationProgram program,
			ToolApplianceConfigurationExtra[] extras,
			ToolApplianceConfigurationProfile[] profiles) {
		this.configurationID = configurationID;
		this.program = program;
		this.extras = extras;
		this.profiles = profiles;
	}

}
