package osh.hal.exchange;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.UUID;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.PowerProfileTick;
import osh.eal.hal.interfaces.common.IHALProgramRemainingTime;
import osh.hal.interfaces.appliance.IHALMieleApplianceProgramDetails;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class MieleApplianceObserverExchange 
				extends GenericMieleApplianceObserverExchange
				implements IHALProgramRemainingTime, IHALMieleApplianceProgramDetails {
	
	private int programRemainingTime;
	private long deviceStartTime;
	private EnumMap<Commodity, ArrayList<PowerProfileTick>> expectedLoadProfiles;
	private String programName;
	private String phaseName;
	
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public MieleApplianceObserverExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}

	@Override
	public int getProgramRemainingTime() {
		return programRemainingTime;
	}

	public void setProgramRemainingTime(int remainingTime) {
		this.programRemainingTime = remainingTime;
	}	
	
	public long getDeviceStartTime() {
		return deviceStartTime;
	}
	
	public void setDeviceStartTime(long deviceStartTime) {
		this.deviceStartTime = deviceStartTime;
	}
	
	public void setExpectedLoadProfiles(EnumMap<Commodity, ArrayList<PowerProfileTick>> expectedLoadProfiles) {
		this.expectedLoadProfiles = expectedLoadProfiles;
	}
	
	public EnumMap<Commodity, ArrayList<PowerProfileTick>> getExpectedLoadProfiles() {
		//TODO clone
		return expectedLoadProfiles;
	}

	@Override
	public String getProgramName() {
		return programName;
	}
	
	public void setProgramName(String programName) {
		this.programName = programName;
	}

	@Override
	public String getPhaseName() {
		return phaseName;
	}

	public void setPhaseName(String phaseName) {
		this.phaseName = phaseName;
	}
}
