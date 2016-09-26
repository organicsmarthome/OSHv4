package osh.hal.exchange;

import java.util.UUID;

import osh.cal.CALExchange;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerRegistryEnum;


/**
 * 
 * @author Till Schuberth
 *
 */
public class GUIStateRegistrySelectedComExchange extends CALExchange {

	private final StateViewerRegistryEnum registry;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public GUIStateRegistrySelectedComExchange(UUID deviceID, Long timestamp, StateViewerRegistryEnum registry) {
		super(deviceID, timestamp);
		this.registry = registry;
	}

	
	public StateViewerRegistryEnum getSelected() {
		return registry;
	}
	
}
