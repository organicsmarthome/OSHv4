package osh.toolbox.appliance;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ToolApplianceConfigurationProgram {
	
	public int programID;
	public String programName;
	public String descriptionEN;
	public String descriptionDE;
	
	
	public ToolApplianceConfigurationProgram(
			int programID,
			String programName,
			String descriptionEN,
			String descriptionDE) {
		
		this.programID = programID;
		this.programName = programName;
		this.descriptionEN = descriptionEN;
		this.descriptionDE = descriptionDE;
	}

}
