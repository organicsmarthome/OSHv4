package osh.hal.exchange;

import java.util.UUID;

import osh.driver.simulation.spacecooling.OutdoorTemperatures;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.hvac.IHALAdsorptionChillerDetails;
import osh.eal.hal.interfaces.thermal.IHALColdWaterPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALHotWaterPowerDetails;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class ChillerObserverExchange 
				extends HALDeviceObserverExchange
				implements 	IHALAdsorptionChillerDetails,
							IHALElectricalPowerDetails,
							IHALColdWaterPowerDetails,
							IHALHotWaterPowerDetails{
	
	// ### IHALAdsortionChillerDetails ###
	private boolean running;
	private int minRuntimeRemaining;
	
	// ### IHALElectricPowerDetails ###
	private int activePower;
	private int reactivePower;
	
	// ### IHALColdWaterPowerDetails ###
	private int coldWaterPower;
	
	// ### IHALHotWaterPowerDetails ###
	private int hotWaterPower;
	
	//DIRTY HACK!
	private OutdoorTemperatures outdoorTemperature;

	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public ChillerObserverExchange(
			UUID deviceID, 
			Long timestamp,
			boolean running,
			OutdoorTemperatures outdoorTemperature) {
		super(deviceID, timestamp);
		
		this.running = running;
		this.outdoorTemperature = outdoorTemperature;
	}

	@Override
	public int getMinRuntimeRemaining() {
		return minRuntimeRemaining;
	}

	public void setMinRuntimeRemaining(int minRuntimeRemaining) {
		this.minRuntimeRemaining = minRuntimeRemaining;
	}

	@Override
	public int getActivePower() {
		return activePower;
	}

	public void setActivePower(int activePower) {
		this.activePower = activePower;
	}

	@Override
	public int getReactivePower() {
		return reactivePower;
	}

	public void setReactivePower(int reactivePower) {
		this.reactivePower = reactivePower;
	}

	

	public int getColdWaterPower() {
		return coldWaterPower;
	}

	public void setColdWaterPower(int coldWaterPower) {
		this.coldWaterPower = coldWaterPower;
	}

	public int getHotWaterPower() {
		return hotWaterPower;
	}

	public void setHotWaterPower(int hotWaterPower) {
		this.hotWaterPower = hotWaterPower;
	}

	@Override
	public boolean isRunning() {
		return running;
	}
	
	public OutdoorTemperatures getOutdoorTemperature() {
		return outdoorTemperature;
	}

	@Override
	public double getHotWaterTemperature() {
		return Double.NaN;
	}

	@Override
	public double getColdWaterTemperature() {
		return Double.NaN;
	}
}
