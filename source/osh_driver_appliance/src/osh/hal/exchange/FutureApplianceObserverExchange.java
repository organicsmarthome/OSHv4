package osh.hal.exchange;

import java.util.UUID;

import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALThermalPowerDetails;
import osh.en50523.EN50523DeviceState;
import osh.hal.interfaces.appliance.IHALGenericApplianceDOF;
import osh.hal.interfaces.appliance.IHALGenericApplianceDetails;
import osh.hal.interfaces.appliance.IHALGenericApplianceIsCurrentlyControllable;
import osh.hal.interfaces.appliance.IHALGenericApplianceProgramDetails;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class FutureApplianceObserverExchange 
				extends HALDeviceObserverExchange
				implements  IHALElectricalPowerDetails,
							IHALGasPowerDetails,
							IHALGenericApplianceDetails,
							IHALGenericApplianceProgramDetails,
							IHALGenericApplianceDOF,
							IHALGenericApplianceIsCurrentlyControllable,
							IHALThermalPowerDetails {

	// ### IHALElectricPowerDetails ###
	private int activePower;
	private int reactivePower;
	
	// ### IHALGasPowerDetails ###
	private int gasPower;
	
	// ### IHALThermalPowerDetails ###
	private int hotWaterPower;
	private int domesticHotWaterPower;
	
	
	// ### IHALGenericApplianceDetails ###
	private EN50523DeviceState en50523DeviceState;
	
	// ### IHALGenericApplianceProgramDetails ###
	private ApplianceProgramConfigurationStatus applianceConfigurationProfile;
	private UUID acpID;
	private Long acpReferenceTime;
	
	// ### IHALGenericApplianceDOF ###
	private Long dof;
	
	// ### IHALGenericApplianceIsCurrentlyControllable ###
	private boolean currentlyControllable;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public FutureApplianceObserverExchange(
			UUID deviceID, 
			Long timestamp,
			int activePower,
			int reactivePower,
			int hotWaterPower,
			int domesticHotWaterPower,
			int gasPower) {
		super(deviceID, timestamp);
		
		this.activePower = activePower;
		this.reactivePower = reactivePower;
		this.hotWaterPower = hotWaterPower;
		this.domesticHotWaterPower = domesticHotWaterPower;
		this.gasPower = gasPower;
	}

	@Override
	public int getActivePower() {
		return activePower;
	}

	@Override
	public int getReactivePower() {
		return reactivePower;
	}

	@Override
	public int getGasPower() {
		return gasPower;
	}


	@Override
	public EN50523DeviceState getEN50523DeviceState() {
		return en50523DeviceState;
	}
	

	public void setEn50523DeviceState(EN50523DeviceState en50523DeviceState) {
		this.en50523DeviceState = en50523DeviceState;
	}


	@Override
	public ApplianceProgramConfigurationStatus getApplianceConfigurationProfile() {
		return applianceConfigurationProfile;
	}


	public void setApplianceConfigurationProfile(
			ApplianceProgramConfigurationStatus applianceConfigurationProfile,
			LoadProfileCompressionTypes profileType,
			final int powerEps,
			final int timeSlotDuration) {
		// clone and compress
		
		if (applianceConfigurationProfile != null) {
			SparseLoadProfile[][] dynamicLoadProfiles = applianceConfigurationProfile.getDynamicLoadProfiles();
			SparseLoadProfile[][] compressedDynamicLoadProfiles = 
					SparseLoadProfile.getCompressedProfile(
							profileType, 
							(SparseLoadProfile[][]) dynamicLoadProfiles, 
							powerEps, 
							timeSlotDuration);
			
			ApplianceProgramConfigurationStatus acp = new ApplianceProgramConfigurationStatus(
					applianceConfigurationProfile.getAcpID(), 
					compressedDynamicLoadProfiles,
					applianceConfigurationProfile.getMinMaxDurations(),
					applianceConfigurationProfile.getAcpReferenceTime(),
					applianceConfigurationProfile.isDoNotReschedule());
			
			this.applianceConfigurationProfile = acp;
		}
		else {
			this.applianceConfigurationProfile = null;
		}
			
	}


	@Override
	public UUID getAcpID() {
		return acpID;
	}
	
	public void setAcpID(UUID acpID) {
		this.acpID = acpID;
	}
	
	@Override
	public Long getAcpReferenceTime() {
		return acpReferenceTime;
	}
	
	public void setAcpReferenceTime(long acpReferenceTime) {
		this.acpReferenceTime = acpReferenceTime;
	}


	@Override
	public boolean isCurrentlyControllable() {
		return currentlyControllable;
	}


	public void setCurrentlyControllable(boolean currentlyControllable) {
		this.currentlyControllable = currentlyControllable;
	}


	@Override
	public int getHotWaterPower() {
		return hotWaterPower;
	}


	@Override
	public int getDomesticHotWaterPower() {
		return domesticHotWaterPower;
	}

	@Override
	public Long getDOF() {
		return dof;
	}
	
	public void setDOF(long dof) {
		this.dof = dof;
	}
	
	
}
