package osh.hal.exchange;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.thermal.IHALColdWaterPowerDetails;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class SpaceCoolingObserverExchange extends HALDeviceObserverExchange implements IHALColdWaterPowerDetails {
	
	private ArrayList<ChillerCalendarDate> dates;
	private Map<Long, Double> temperaturePrediction;
	private int coldWaterPower;
	

	public SpaceCoolingObserverExchange(
			UUID deviceID, 
			Long timestamp, 
			ArrayList<ChillerCalendarDate> dates, 
			Map<Long, Double> temperaturePrediction,
			int coldWaterPower) {
		super(deviceID, timestamp);
		
		this.dates = dates;
		this.temperaturePrediction = temperaturePrediction;
	}
	
	public ArrayList<ChillerCalendarDate> getDates() {
		return dates;
	}
	
	public Map<Long, Double> getTemperaturePrediction() {
		return temperaturePrediction;
	}

	@Override
	public int getColdWaterPower() {
		return coldWaterPower;
	}

	@Override
	public double getColdWaterTemperature() {
		return Double.NaN;
	}

}
