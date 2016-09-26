package osh.datatypes.registry.oc.state.globalobserver;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.StateExchange;


/**
 * Current power consumption of all devices covered by this EMS
 * @author Ingo Mauser
 *
 */
public class CommodityPowerStateExchange extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2451111383309555786L;
	EnumMap<Commodity,Double> powerState;
	DeviceTypes deviceType;


	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public CommodityPowerStateExchange(
			UUID sender, 
			long timestamp,
			DeviceTypes deviceType) {
		this(
				sender, 
				timestamp, 
				new EnumMap<Commodity, Double>(Commodity.class),
				deviceType);
	}
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 * @param powerState
	 */
	public CommodityPowerStateExchange(
			UUID sender, 
			long timestamp,
			EnumMap<Commodity,Double> powerState,
			DeviceTypes deviceType) {
		super(sender, timestamp);

		this.powerState = powerState;
		this.deviceType = deviceType;
	}
	
	
	public void addPowerState(Commodity commodity, double value) {
		powerState.put(commodity, value);
	}
	
	
	public Double getPowerState(Commodity commodity) {
		return powerState.get(commodity);
	}
	
	
	public EnumMap<Commodity, Double> getPowerState() {
		return powerState;
	}
	
	public DeviceTypes getDeviceType() {
		return deviceType;
	}

	@Override
	public CommodityPowerStateExchange clone() {
		CommodityPowerStateExchange cloned = new CommodityPowerStateExchange(
				this.getSender(), 
				this.getTimestamp(),
				this.getDeviceType());
		
		for (Entry<Commodity,Double> e : powerState.entrySet()) {
			Double value = e.getValue();
			if (value != null) {
				value = (double) value;
			}
			cloned.addPowerState(e.getKey(), value);
		}
		
		return cloned;
	
	}

	@Override
	public String toString() {
		return "CommodityPowerState: " + powerState.toString();
	}
	
}
