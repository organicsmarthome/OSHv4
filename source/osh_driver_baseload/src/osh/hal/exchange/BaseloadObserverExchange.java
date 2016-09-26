package osh.hal.exchange;

import java.util.EnumMap;
import java.util.Set;
import java.util.UUID;

import osh.datatypes.commodity.Commodity;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class BaseloadObserverExchange 
				extends HALDeviceObserverExchange
				implements IHALElectricalPowerDetails, IHALGasPowerDetails {
	
	private EnumMap<Commodity,Integer> powerMap;
	
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 * @param complexPower
	 */
	public BaseloadObserverExchange(
			UUID deviceID, 
			Long timestamp) {
		super(deviceID, timestamp);
		
		this.powerMap = new EnumMap<>(Commodity.class);
	}
	
	
	public Integer getPower(Commodity c) {
		return powerMap.get(c);
	}
	
	public void setPower(Commodity c, int power) {
		powerMap.put(c, power);
	}
	
	public Set<Commodity> getCommodities() {
		return powerMap.keySet();
	}
	
	@Override
	public int getGasPower() {
		return powerMap.get(Commodity.NATURALGASPOWER);
	}

	@Override
	public int getActivePower() {
		return powerMap.get(Commodity.ACTIVEPOWER);
	}

	@Override
	public int getReactivePower() {
		return powerMap.get(Commodity.REACTIVEPOWER);
	}

}
