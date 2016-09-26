package osh.hal.exchange;

import java.util.UUID;

import osh.cal.CALExchange;
import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Till Schuberth
 *
 */
public class GUIStateSelectedComExchange extends CALExchange {

	private final Class<? extends StateExchange> cls;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public GUIStateSelectedComExchange(UUID deviceID, Long timestamp, Class<? extends StateExchange> cls) {
		super(deviceID, timestamp);
		this.cls = cls;
	}

	public Class<? extends StateExchange> getSelected() {
		return cls;
	}
	
}
