package osh.mgmt.commanager;

import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.hal.exchange.PlsComExchange;
import osh.registry.interfaces.IHasState;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class PlsProviderComManager 
				extends ComManager 
				implements IHasState {

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param uuid
	 */
	public PlsProviderComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}
	
	
	/**
	 * Receive data from ComDriver
	 */
	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		
		// receive signal from ComDriver as OX-object
		if (exchangeObject instanceof PlsComExchange) {
			
			getGlobalLogger().logInfo("SmartHome received new PLS signal...");
			PlsComExchange ox = (PlsComExchange) exchangeObject;
			
			
			// set states in oc registry
			PlsStateExchange pricedetails = new PlsStateExchange(
					getUUID(), 
					ox.getTimestamp());
			
			for (Entry<AncillaryCommodity,PowerLimitSignal> e : ox.getPowerLimitSignals().entrySet()) {
				pricedetails.setPowerLimitSignal(e.getKey(), e.getValue());
			}
			
			getOCRegistry().setState(PlsStateExchange.class, this, pricedetails);
		}
		else {
			try {
				throw new OSHException("Signal unknown");
			} 
			catch (OSHException e) {
				e.printStackTrace();
			}
		}
	}
	
}
