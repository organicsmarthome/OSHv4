package osh.hal.exchange;

import java.util.List;
import java.util.UUID;

import osh.eal.hal.exchange.HALControllerExchange;

/**
 * 
 * @author Jan Mueller
 *
 */
public class BatteryStorageCX extends HALControllerExchange {

	private List<Integer> controllList;
	private Long referenceTime;

	
	/**
	 * CONSTRUCTOR
	 */
	public BatteryStorageCX(
			UUID deviceID, 
			Long timestamp, 
			List<Integer> list,
			Long referenceTime
			) {
		super(deviceID, timestamp);
		this.controllList = list;
		this.referenceTime = referenceTime;
	}
	

	public long getreferenceTime(){
		return this.referenceTime;
	}
	
	public List<Integer> getControllList(){
		return this.controllList;
	}
	
}
