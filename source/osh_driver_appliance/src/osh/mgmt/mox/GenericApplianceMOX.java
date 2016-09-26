package osh.mgmt.mox;

import java.util.UUID;

import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.en50523.EN50523DeviceState;

/**
 * 
 * @author Ingo Mauser, Matthias Maerz
 *
 */
public class GenericApplianceMOX implements IModelOfObservationExchange {

	/**
	 * Required for:<br>
	 * - Decision: static / dynamic profile
	 */
	private EN50523DeviceState currentState;
	
	private ApplianceProgramConfigurationStatus acp;
	private UUID acpID;
	private Long acpReferenceTime;
	
	private Long dof;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	
	/**
	 * CONSTRUCTOR
	 * @param triggerScheduling
	 * @param timeDoF
	 * @param loadProfiles
	 */
	public GenericApplianceMOX(
			EN50523DeviceState currentState,
			ApplianceProgramConfigurationStatus acp,
			UUID acpID,
			Long acpReferenceTime,
			Long dof,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {

		this.currentState = currentState;
		
		if (acp != null) {
			this.acp = (ApplianceProgramConfigurationStatus) acp.clone();
		}
		
		this.acpID = acpID;
		this.acpReferenceTime = acpReferenceTime;
		
		this.compressionType = compressionType;
		this.compressionValue = compressionValue;
	
		this.setDof(dof);
		
	}

	public EN50523DeviceState getCurrentState() {
		return currentState;
	}

	/**
	 * != null IFF something changed
	 * @return
	 */
	public ApplianceProgramConfigurationStatus getAcp() {
		return acp;
	}

	public UUID getAcpID() {
		return acpID;
	}
	
	public Long getAcpReferenceTime() {
		return acpReferenceTime;
	}

	public Long getDof() {
		return dof;
	}

	public void setDof(Long dof) {
		this.dof = dof;
	}

	public LoadProfileCompressionTypes getCompressionType() {
		return compressionType;
	}

	public int getCompressionValue() {
		return compressionValue;
	}
}
