package osh.hal.exchange;

import java.util.ArrayList;
import java.util.UUID;

import osh.cal.CALComExchange;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;


/**
 * 
 * @author ???
 *
 */
public class ScheduledApplianceUIexchange extends CALComExchange {

	private ArrayList<ExpectedStartTimeExchange> currentApplianceSchedules;

	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public ScheduledApplianceUIexchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}
	
	/**
	 * @return the currentApplianceSchedules
	 */
	public ArrayList<ExpectedStartTimeExchange> getCurrentApplianceSchedules() {
		return currentApplianceSchedules;
	}

	/**
	 * @param currentApplianceSchedules the currentApplianceSchedules to set
	 */
	public void setCurrentApplianceSchedules(
			ArrayList<ExpectedStartTimeExchange> currentApplianceSchedules) {
		this.currentApplianceSchedules = currentApplianceSchedules;
	} 
}
