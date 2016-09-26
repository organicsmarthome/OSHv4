package osh.comdriver.wamp;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.hal.exchange.EpsComExchange;
import osh.hal.exchange.PlsComExchange;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class SignalsWAMPComDriver extends CALComDriver implements Runnable {
	
	private Map<AncillaryCommodity, PriceSignal> priceSignals;
	private Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;
	
	SignalsWAMPDispatcher signalsWampDispatcher;
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws UnknownHostException 
	 */
	public SignalsWAMPComDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) throws UnknownHostException {
		super(controllerbox, deviceID, driverConfig);

	}	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		this.signalsWampDispatcher = new SignalsWAMPDispatcher(getGlobalLogger());
		
		new Thread(this, "push proxy of EPS/PLS signals driver to WAMP").start();
	}
	
	@Override
	public void run() {
		while (true) {
			synchronized (this.signalsWampDispatcher) {
				try { // wait for new data
					this.signalsWampDispatcher.wait();
				} 
				catch (InterruptedException e) {
					getGlobalLogger().logError("should not happen", e);
					break;
				}				
			}
		}		
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		if (exchangeObject instanceof EpsComExchange) {
			priceSignals = ((EpsComExchange) exchangeObject).getPriceSignals();
			this.signalsWampDispatcher.sendEPS(priceSignals);
		} else if (exchangeObject instanceof PlsComExchange) {			
			powerLimitSignals = ((PlsComExchange) exchangeObject).getPowerLimitSignals();
			this.signalsWampDispatcher.sendPLS(powerLimitSignals);
		}
	}
}
