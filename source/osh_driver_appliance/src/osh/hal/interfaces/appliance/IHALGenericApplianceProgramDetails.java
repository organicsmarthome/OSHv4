package osh.hal.interfaces.appliance;

import java.util.UUID;

import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;

/**
 * 
 * @author Ingo Mauser
 *
 */
public interface IHALGenericApplianceProgramDetails {
	public ApplianceProgramConfigurationStatus getApplianceConfigurationProfile();
	public UUID getAcpID();
	public Long getAcpReferenceTime();
}
