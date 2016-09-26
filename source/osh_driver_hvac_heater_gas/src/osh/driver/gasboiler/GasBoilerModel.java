package osh.driver.gasboiler;

import java.io.Serializable;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class GasBoilerModel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6693604390224543213L;
	private final int TYPICAL_ACTIVEPOWER_ON;
	private final int TYPICAL_ACTIVEPOWER_OFF;
	private final int TYPICAL_REACTIVEPOWER_ON;
	private final int TYPICAL_REACTIVEPOWER_OFF;
	private final int MAX_HOT_WATER_POWER;
	private final int MAX_GAS_POWER;
	
	private int currentHotWaterPower = 0;
	private int currentGasPower = 0;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public GasBoilerModel(
			int MAX_HOT_WATER_POWER,
			int MAX_GAS_POWER,
			int activePowerOn,
			int activePowerOff,
			int reactivePowerOn,
			int reactivePowerOff,
			boolean currentState) {
		this.MAX_HOT_WATER_POWER = MAX_HOT_WATER_POWER;
		this.MAX_GAS_POWER = MAX_GAS_POWER;
		this.TYPICAL_ACTIVEPOWER_ON = activePowerOn;
		this.TYPICAL_ACTIVEPOWER_OFF = activePowerOff;
		this.TYPICAL_REACTIVEPOWER_ON = reactivePowerOn;
		this.TYPICAL_REACTIVEPOWER_OFF = reactivePowerOff;
		
		if (currentState) {
			switchOn();
		}
	}
	
	protected GasBoilerModel() {
		MAX_HOT_WATER_POWER = 0;
		MAX_GAS_POWER = 0;
		TYPICAL_ACTIVEPOWER_ON = 0;
		TYPICAL_ACTIVEPOWER_OFF = 0;
		TYPICAL_REACTIVEPOWER_ON = 0;
		TYPICAL_REACTIVEPOWER_OFF = 0;
	}
	
	
	public int getHotWaterPower() {
		return currentHotWaterPower;
	}
	
	public int getGasPower() {
		return currentGasPower;
	}
	
	public void switchOn() {
		currentHotWaterPower = MAX_HOT_WATER_POWER;
		currentGasPower = MAX_GAS_POWER;
	}
	
	public void switchOff() {
		currentHotWaterPower = 0;
		currentGasPower = 0;
	}
	
	public boolean isOn() {
		return (currentHotWaterPower > 0 ? true : false);
	}

	public int getActivePower() {
		return (isOn() ? TYPICAL_ACTIVEPOWER_ON : TYPICAL_ACTIVEPOWER_OFF);
	}
	
	public int getReactivePower() {
		return (isOn() ? TYPICAL_REACTIVEPOWER_ON : TYPICAL_REACTIVEPOWER_OFF);
	}
}
