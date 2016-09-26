package osh.hal.exchange;

import java.util.HashMap;
import java.util.UUID;

import osh.datatypes.hal.interfaces.ITemperatureDetails;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.eal.hal.exchange.HALDeviceObserverExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class BacNetThermalExchange 
				extends HALDeviceObserverExchange 
				implements ITemperatureDetails {

	private TemperatureDetails temperatureDetails;
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public BacNetThermalExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
		temperatureDetails = new TemperatureDetails(deviceID, timestamp);
	}
	
	public double getTemperature() {
		return temperatureDetails.getTemperature();
	}

	public void setTemperature(double temperature) {
		this.temperatureDetails.setTemperature(temperature);
	}

	
	public HashMap<String, Double> getAuxiliaryTemperatures() {
		return temperatureDetails.getAuxiliaryTemperatures();
	}

	public void setAuxiliaryTemperatures(HashMap<String, Double> auxiliaryTemperatures) {
		temperatureDetails.setAuxiliaryTemperatures(auxiliaryTemperatures);
	}

	@Override
	public String toString() {
		return temperatureDetails.toString();
	}
	
	public TemperatureDetails getTemperatureDetails() {
		return temperatureDetails;
	}
	
	public void setTemperatureDetails(TemperatureDetails temperatureDetails) {
		this.temperatureDetails = temperatureDetails;
	}
}
