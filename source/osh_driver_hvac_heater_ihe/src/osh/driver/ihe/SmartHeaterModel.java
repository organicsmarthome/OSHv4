package osh.driver.ihe;

import java.io.Serializable;

/**
 * Heating element with the following specifications:<br>
 * 8 power states: 0...3.5 kW (in steps of 0.5 kW)<br>
 * <br>
 * 3 heating sub-elements:<br>
 * (0) 0.5 kW (1) 1.0 kW (2) 2.0 kW<br>
 * <br>
 * Min circuit times:<br>
 * (0) ? seconds (1) ? seconds (2) ? seconds<br>
 * <br>
 * TODO: 99% efficiency (or as parameter)
 * 
 * @author Ingo Mauser
 *
 */
public class SmartHeaterModel implements Serializable {
	
	// ### configuration ###
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3818872291707857561L;
	private final int setTemperature;
	private final int powerDelta = 100;
	
	// available states                0    1     2     3     4     5     6     7
	private final int[] powerStates = {0, 500, 1000, 1500, 2000, 2500, 3000, 3500};
	
	// minimum on and off times
	private final int[] minOnTimes = {10,10,10};
	private final int[] minOffTimes = {110,170,230};
	
	// ### variables ###
	/** 0...7 */
	private int currentState;
	/** 500 1000 2000 */
	private boolean[] currentStates;
	/** timestamp, timestamp, timestamp (absolute times) */
	private long[] timestampOfLastChangePerSubElement;
	
	
	// ### for logging purposes ###
	
	private long timestampOfLastChange = 0;
	
	// sub-element           0 1 2
	private int[] counter = {0,0,0}; // count only switch ON !
	private long[] runtime = {0,0,0}; // time in state ON
	
//									0,5kW 1kW 1.5kW 2kW 2.5kW 3kW 3.5kW
	private long[] powerTierRunTimes = {0, 0, 0, 0, 0, 0, 0}; //time power tier was on
//	private int[] minCircuitTimeViolatedCounter = {0,0,0};
	
	
	/**
	 * CONSTRUCTOR
	 */
	public SmartHeaterModel(
			int setTemperature, 
			int initialState,
			long[] timestampOfLastChangePerSubElement) {
		this.setTemperature = setTemperature;
		this.currentState = initialState;
		this.currentStates = convertStateToStates(initialState);
		this.timestampOfLastChangePerSubElement = new long[timestampOfLastChangePerSubElement.length];
		for (int i = 0; i < timestampOfLastChangePerSubElement.length; i++ ) {
			this.timestampOfLastChangePerSubElement[i] = timestampOfLastChangePerSubElement[i];
		}		
	}
	
	public SmartHeaterModel() {
		this.setTemperature = 0;
	}
	
	/**
	 * 
	 * @param availablePower [W] (value; negative value is net production/feed-in of building)
	 */
	public void updateAvailablePower(long now, double availablePower, double currentTemperature) {
		
		// power delta
		availablePower = availablePower + powerDelta; // safety margin of 100W
		
		// power used by smart heater
		int selfUsePower = currentState * 500;
		availablePower = availablePower - selfUsePower;
		
		// if net consumption in building
		if (availablePower >= 0) {
			availablePower = 0;
		}
		
		availablePower = Math.abs(availablePower);
		
		// calculate new state of heater
		double tempMaxPower = availablePower / 1000.0 * 2.0;
		int newState = Math.min(7, (int) tempMaxPower);
		
		// check temperature constraint
		if (currentTemperature > setTemperature) {
			// switch OFF
			newState = 0;
		}
		
		// check whether new state possible...(time logic)
		boolean[] newStates = convertStateToStates(newState);
		boolean[] toBeSwitched = {false, false, false}; 
		for (int i = 0; i < 3; i++) {
			if (currentStates[i] != newStates[i]) {
				toBeSwitched[i] = true;
			}
		}
		boolean timeViolation = false;
		for (int i = 0; i < 3; i++) {
			if (toBeSwitched[i]) {
				long diff = now - timestampOfLastChangePerSubElement[i];
				if (newStates[i] && diff < minOffTimes[i]) {
					//switch on violation
					timeViolation = true;
				}
				else if (!newStates[i] && diff < minOnTimes[i]) {
					//switch off violation
					timeViolation = true;
				}
			}
		}
		
		// update state if no min time is violated...
		if (!timeViolation) {
			updateState(now, newState);
		}
	}
	
	/**
	 * 
	 * @return [W] > 0
	 */
	public double getPower() {
		return powerStates[currentState];
	}
	
	public int getCurrentState() {
		return currentState;
	}
	
	public boolean isOn() {
		return currentState > 0;
	}
	
	public long[] getTimestampOfLastChangePerSubElement() {
		return timestampOfLastChangePerSubElement.clone();
	}
	
	
	// ### helper methods ###
	
	private void updateState(long timestamp, int newState) {
		int oldState = this.currentState;
		
		if (oldState != newState) {
			updateCounters(timestamp, oldState, newState);
		}
		
		this.currentState = newState;
		this.currentStates = convertStateToStates(newState);
	}
	
	private static boolean[] convertStateToStates(int state) {
		boolean[] states = {false, false, false};
		if (state % 2 == 1) {
			states[0] = true;
		}
		if (state == 2 || state == 3 || state == 6 || state == 7) {
			states[1] = true;
		}
		if (state >= 4) {
			states[2] = true;
		}
		return states;
	}
	
	
	// ### for logging purposes ###
	
	private void updateCounters(long now, int oldState, int newState) {
		// increase runtime
		long timeDiff = now - timestampOfLastChange;
		timestampOfLastChange = now;
		
		if (oldState % 2 == 1) {
			runtime[0] = runtime[0] + timeDiff;
		}
		if (oldState == 2 || oldState == 3 || oldState == 6 || oldState == 7) {
			runtime[1] = runtime[1] + timeDiff;
		}
		if (oldState >= 4) {
			runtime[2] = runtime[2] + timeDiff;
		}
		
		if (oldState != 0) {
			powerTierRunTimes[oldState - 1] = powerTierRunTimes[oldState - 1] + timeDiff;
		}
		
		// increase switch counters (only switch on)
		if ((oldState != newState) && (oldState % 2 == 0) && (newState % 2 == 1)) {
			counter[0]++;
			timestampOfLastChangePerSubElement[0] = now;
		}
		if (oldState != newState) {
			if (oldState != 2 && oldState != 3 && oldState != 6 && oldState != 7) {
				if (newState == 2 || newState == 3 || newState == 6 || newState == 7) {
					counter[1]++;
					timestampOfLastChangePerSubElement[1] = now;
				}
			}
		}
		if (oldState < 4 && newState >=4) {
			counter[2]++;
			timestampOfLastChangePerSubElement[2] = now;
		}
		
		// increase switch counters (switch off)
		if ((oldState != newState) && (newState % 2 == 0) && (oldState % 2 == 1)) {
			timestampOfLastChangePerSubElement[0] = now;
		}
		if (oldState != newState) {
			if (newState != 2 && newState != 3 && newState != 6 && newState != 7) {
				if (oldState == 2 || oldState == 3 || oldState == 6 || oldState == 7) {
					timestampOfLastChangePerSubElement[1] = now;
				}
			}
		}
		if (newState < 4 && oldState >=4) {
			timestampOfLastChangePerSubElement[2] = now;
		}
		
	}
	
	public int[] getCounter() {
		int[] clonedCounter = {counter[0], counter[1], counter[2]};
		return clonedCounter;
	}
	
	public long[] getRuntime() {
		long[] clonedRuntime = {runtime[0], runtime[1], runtime[2]};
		return clonedRuntime;
	}
	
	public long[] getPowerTierRunTimes() {
		long[] clonedPowerTierRunTime = {powerTierRunTimes[0], powerTierRunTimes[1], powerTierRunTimes[2],
				powerTierRunTimes[3], powerTierRunTimes[4], powerTierRunTimes[5], powerTierRunTimes[6]};
		return clonedPowerTierRunTime;
	}
	
//	public int[] getMinCircuitTimeViolatedCounter() {
//		int[] cloned = {
//				minCircuitTimeViolatedCounter[0],
//				minCircuitTimeViolatedCounter[1],
//				minCircuitTimeViolatedCounter[2]};
//		return cloned;
//	}
	
}
