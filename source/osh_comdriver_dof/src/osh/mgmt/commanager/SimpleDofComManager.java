package osh.mgmt.commanager;

import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.ICALExchange;
import osh.core.com.ComManager;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.dof.DofStateExchange;
import osh.hal.exchange.DofComExchange;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class SimpleDofComManager extends ComManager {

	public SimpleDofComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		if (exchangeObject instanceof DofComExchange) {
			DofComExchange dce = (DofComExchange) exchangeObject;
			
			synchronized (this) {
				
				long now = getTimer().getUnixTime();
				
				for (Entry<UUID, Integer> en : dce.getDevice1stDegreeOfFreedom().entrySet()) {
					
					Integer FirstDegree = en.getValue();
					Integer SecondDegree = dce.getDevice2ndDegreeOfFreedom().get(en.getKey());
					
					if (FirstDegree == null || SecondDegree == null) {
						getGlobalLogger().logError("Recieved invalid DOF (null value) for: " + en.getKey());
					} else {
						DofStateExchange dse = new DofStateExchange(en.getKey(), now);
						dse.setDevice1stDegreeOfFreedom(FirstDegree);
						dse.setDevice2ndDegreeOfFreedom(SecondDegree);
						
						getOCRegistry().setStateOfSender(DofStateExchange.class, dse);
					}
				}
			}
		}
	}
}
