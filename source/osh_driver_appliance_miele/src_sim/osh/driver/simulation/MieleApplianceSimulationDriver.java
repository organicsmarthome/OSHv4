package osh.driver.simulation;

import java.util.List;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.configuration.appliance.miele.DeviceProfile;
import osh.configuration.appliance.miele.ProfileTick.Load;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.xml.XMLSerialization;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public abstract class MieleApplianceSimulationDriver 
							extends ApplianceSimulationDriver {
	
	// single load profile...
	protected DeviceProfile deviceProfile;
	
	protected boolean hasProfile;
	protected boolean isIntelligent;
	private int progamDuration;
	
	/** middle in sense of consumption */
	protected int middleOfPowerConsumption = -1;
	protected long programStart = -1;
	
	/** in Ws (Watt-seconds)*/
	protected int activePowerSumPerRun = -1;
	
	@Deprecated
	protected int device2ndDof = 0;
	
	private boolean systemState;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 * @throws HALException 
	 */
	public MieleApplianceSimulationDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		String profileSourceName = driverConfig.getParameter("profilesource");
		try {
			this.deviceProfile = (DeviceProfile)XMLSerialization.file2Unmarshal(profileSourceName, DeviceProfile.class);
		}
		catch (Exception ex){
			getGlobalLogger().logError("An error ouccurd while loading the device profile: " +ex.getMessage());
		}
		
		this.device2ndDof = Integer.valueOf(driverConfig.getParameter("device2nddof"));
		
		this.progamDuration = this.deviceProfile.getProfileTicks().getProfileTick().size();
		
	}

	
	/*
	 * PLZ use onProcessingTimeTick() or onActiveTimeTick()
	 */
	@Override
	final public void onNextTimeTick() {
		
		this.onProcessingTimeTick();
		
		if (systemState) {
					
			//next tick
			if (programStart < 0) {
				getGlobalLogger().logError("systemState is true, but programStart < 0", new Exception());
				turnOff();
				return;
			}
			
			long currentTime = getTimer().getUnixTime();
			int currentDurationSinceStart = (int) (currentTime - programStart);
			if (currentDurationSinceStart < 0) {
				getGlobalLogger().logError("timewarp: currentDurationSinceStart is negative", new Exception());
			}
			
			
			if (progamDuration > currentDurationSinceStart) {
				
				// iterate commodities
				List<Load> loadList = deviceProfile.getProfileTicks().getProfileTick().get(currentDurationSinceStart).getLoad();
				for (Load load : loadList) {
					Commodity currentCommodity = Commodity.fromString(load.getCommodity());
					
					if (currentCommodity.equals(Commodity.ACTIVEPOWER)) {
						this.setPower(Commodity.ACTIVEPOWER, load.getValue());
					}
					else if (currentCommodity.equals(Commodity.REACTIVEPOWER)) {
						this.setPower(Commodity.REACTIVEPOWER, load.getValue());
					}
				}
				
				this.onActiveTimeTick();
			}
			else {
				// turn off the device
				turnOff();
			}
		}
	}
	
	private void turnOff() {
		systemState = false;
		this.setPower(Commodity.ACTIVEPOWER, 0);
		this.setPower(Commodity.REACTIVEPOWER, 0);
		programStart = -1;
		this.onProgramEnd();
	}
	
	
	/**
	 * is always invoked while processing a time tick (when onNextTimeTick() has been invoked)
	 */
	protected abstract void onProcessingTimeTick();
	
	/**
	 * is invoked while processing a time tick AND the appliance is running 
	 */
	protected abstract void onActiveTimeTick();
	
	/**
	 * is invoked when the program stops at the end of a work-item
	 */
	protected abstract void onProgramEnd();
		
	@Override
	public void performNextAction(SubjectAction nextAction) {
		//check if the appliance is running 
		setSystemState(nextAction.isNextState());
	}
	
	public void setHasProfile(boolean profile) {
		hasProfile = profile;
	}
	
	
	public void setSystemState(boolean systemState) {
		if (!this.systemState && systemState) {
			programStart = getTimer().getUnixTime();
		}
		
		this.systemState = systemState;
	}
	
	protected int getProgamDuration() {
		return progamDuration;
	}
	
	protected int getMiddleOfDuration() {
		return middleOfPowerConsumption;
	}
	
	@Override
	public boolean isIntelligent() {
		return isIntelligent;
	}
	
	public boolean hasProfile() {
		return hasProfile;
	}
	
}
