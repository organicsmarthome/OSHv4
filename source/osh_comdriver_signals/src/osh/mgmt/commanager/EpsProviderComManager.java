package osh.mgmt.commanager;

import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.hal.exchange.EpsComExchange;
import osh.registry.interfaces.IHasState;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class EpsProviderComManager 
				extends ComManager 
				implements IHasState {

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param uuid
	 */
	public EpsProviderComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}
	
	
	/**
	 * Receive data from ComDriver
	 */
	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		
		// receive signal from ComDriver as OX-object
		if (exchangeObject instanceof EpsComExchange) {
			
			getGlobalLogger().logInfo("SmartHome received new EPS signal...");
			EpsComExchange ox = (EpsComExchange) exchangeObject;
			
			// set states in oc registry
			EpsStateExchange pricedetails = new EpsStateExchange(
					getUUID(), 
					ox.getTimestamp(),
					ox.causeScheduling());
			
			for (Entry<AncillaryCommodity,PriceSignal> e : ox.getPriceSignals().entrySet()) {
				pricedetails.setPriceSignal(e.getKey(), e.getValue());
			}
			
			getOCRegistry().setState(EpsStateExchange.class, this, pricedetails);
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
