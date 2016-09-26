package osh.core.interfaces;

import java.util.UUID;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser, Sebastian Kramer
 *
 */
public interface IOSHStatus {

	public String getRunID();
	
	public String getConfigurationID();
	
	public String getLogDir();
	
	public UUID gethhUUID();
	
	
	public boolean isSimulation();
	
	public boolean isRunningVirtual();
	
	public boolean getShowSolverDebugMessages();
	
	public boolean hasGUI();
	
}
