package osh.toolbox.appliance;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ToolApplianceConfigurationProfile {
	
	public int profileID;
	public String profileName;
	
	public String[] phaseNames;
	public String[] phaseInputFiles;
	
	public int activePowerColumn;
	public int reactivePowerColumn;
	public int naturalGasPowerColumn;
	public int domesticHotWaterPowerColumn;
	
	public ToolApplianceConfigurationProfile(
			int profileID,
			String profileName,
			String[] phaseNames, 
			String[] phaseInputFiles, 
			int activePowerColumn, 
			int reactivePowerColumn,
			int domesticHotWaterColumn,
			int naturalGasPowerColumn) {
		
		this.profileID = profileID;
		this.profileName = profileName;
		
		this.phaseNames = phaseNames;
		this.phaseInputFiles = phaseInputFiles;
		
		this.activePowerColumn = activePowerColumn;
		this.reactivePowerColumn = reactivePowerColumn;
		this.domesticHotWaterPowerColumn = domesticHotWaterColumn;
		this.naturalGasPowerColumn = naturalGasPowerColumn;
		
	}

}
