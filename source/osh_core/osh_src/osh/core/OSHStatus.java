package osh.core;

import java.util.UUID;

import osh.core.interfaces.IOSHStatus;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public class OSHStatus implements IOSHStatus {
	
	private UUID hhUUID;
	
	private String runID;
	private String configurationID;
	private String logDir;
	
	private Boolean isSimulation;
	private Boolean isVirtual;
	
	private Boolean hasGUI;
	
	boolean showSolverDebugMessages;
	
	

	@Override
	public String getRunID() {
		return runID;
	}
	
	public void setRunID(String runID) {
		this.runID = runID;
	}
	
	@Override
	public String getConfigurationID() {
		return configurationID;
	}

	public void setConfigurationID(String configurationID) {
		this.configurationID = configurationID;
	}
	
	@Override
	public String getLogDir() {
		return logDir;
	}

	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}
	
	@Override
	public UUID gethhUUID() {
		return hhUUID;
	}
	
	public void sethhUUID(UUID hhUUID) {
		this.hhUUID = hhUUID;
	}

	@Override
	public boolean isSimulation() {
		return isSimulation;
	}
	
	public void setIsSimulation(boolean isSimulation) {
		this.isSimulation = isSimulation;
	}
	
	@Override
	public boolean isRunningVirtual() {
		return isVirtual;
	}

	public void setVirtual(boolean virtual) {
		this.isVirtual = virtual;
	}

	
	@Override
	public boolean getShowSolverDebugMessages() {
		return showSolverDebugMessages;
	}
	
	
	@Override
	public boolean hasGUI() {
		return hasGUI;
	}
	
	public void setIsGUI(boolean hasGUI) {
		this.hasGUI = hasGUI;
	}
}
