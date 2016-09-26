package osh.datatypes.registry.oc.state;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import osh.datatypes.registry.EventExchange;


/**
 * 
 * @author Till Schuberth
 *
 */
public class GUIScheduleDebugExchange extends EventExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2002327995008392436L;
	private Map<UUID, String> eapartStrings = new TreeMap<UUID, String>();

	public GUIScheduleDebugExchange(UUID sender, long timestamp) {
		super(sender, timestamp);
	}
	
	public void addString(UUID device, String eapartstring) {
		eapartStrings.put(device, eapartstring);
	}
	
	public Map<UUID, String> getEaPartString() {
		return eapartStrings;
	}
}
