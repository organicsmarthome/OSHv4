package osh.hal.exchange;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.CALComExchange;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class DofComExchange extends CALComExchange {

	private HashMap<UUID, Integer> device1stDegreeOfFreedom;
	private HashMap<UUID, Integer> device2ndDegreeOfFreedom;
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public DofComExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
		
	}

	
	public HashMap<UUID, Integer> getDevice1stDegreeOfFreedom() {
		return device1stDegreeOfFreedom;
	}

	public void setDevice1stDegreeOfFreedom(
			HashMap<UUID, Integer> device1stDegreeOfFreedom) {
		this.device1stDegreeOfFreedom = new HashMap<UUID, Integer>();
		
		for (Entry<UUID, Integer> e : device1stDegreeOfFreedom.entrySet()) {
			this.device1stDegreeOfFreedom.put(e.getKey(), e.getValue());
		}
	}

	public HashMap<UUID, Integer> getDevice2ndDegreeOfFreedom() {
		return device2ndDegreeOfFreedom;
	}

	public void setDevice2ndDegreeOfFreedom(
			HashMap<UUID, Integer> device2ndDegreeOfFreedom) {
		this.device2ndDegreeOfFreedom = new HashMap<UUID, Integer>();
		
		for (Entry<UUID, Integer> e : device2ndDegreeOfFreedom.entrySet()) {
			this.device2ndDegreeOfFreedom.put(e.getKey(), e.getValue());
		}
	}

	
	
}
