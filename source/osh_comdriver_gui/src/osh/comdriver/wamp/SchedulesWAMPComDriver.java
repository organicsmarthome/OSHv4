package osh.comdriver.wamp;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.SparseLoadProfile;
import osh.hal.exchange.GUIEpsComExchange;
import osh.hal.exchange.GUIPlsComExchange;
import osh.hal.exchange.GUIScheduleComExchange;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class SchedulesWAMPComDriver extends CALComDriver implements Runnable {
	
	private Map<AncillaryCommodity, PriceSignal> priceSignals;
	private Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;
	private List<Schedule> schedules;
	private int stepSize;
	
	SchedulesWAMPDispatcher schedulesWampDispatcher;
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws UnknownHostException 
	 */
	public SchedulesWAMPComDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) throws UnknownHostException {
		super(controllerbox, deviceID, driverConfig);

	}	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		this.schedulesWampDispatcher = new SchedulesWAMPDispatcher(getGlobalLogger());
		
		new Thread(this, "push proxy of optimisation results to WAMP").start();
	}
	
	@Override
	public void run() {
		while (true) {
			synchronized (this.schedulesWampDispatcher) {
				try { // wait for new data
					this.schedulesWampDispatcher.wait();
				} 
				catch (InterruptedException e) {
					getGlobalLogger().logError("should not happen", e);
					break;
				}				
			}
		}		
	}
	
	private void cleanUpSchedulesAndSend() {
		Map<String, Map<Commodity, Map<Long, Integer>>> scheduleMap = new HashMap<String, Map<Commodity, Map<Long, Integer>>>();
		
//		long now = getTimer().getUnixTime();
		
		for (Schedule s : schedules) {
			Map<Commodity, Map<Long, Integer>> simpleProfile = ((SparseLoadProfile) s.getProfile())
					.getCompressedProfileByTimeSlot(stepSize)
					.convertToSimpleMap();
			
			if (!simpleProfile.isEmpty() && simpleProfile.containsKey(Commodity.ACTIVEPOWER)) {
				scheduleMap.put(s.getScheduleName(), simpleProfile);
			}			
		}
		
		SchedulesWAMPExchangeObject sweo = new SchedulesWAMPExchangeObject(priceSignals, powerLimitSignals, scheduleMap);
		this.schedulesWampDispatcher.sendSchedules(sweo);
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		if (exchangeObject instanceof GUIEpsComExchange) {
			priceSignals = ((GUIEpsComExchange) exchangeObject).getPriceSignals();
		} else if (exchangeObject instanceof GUIPlsComExchange) {			
			powerLimitSignals = ((GUIPlsComExchange) exchangeObject).getPowerLimitSignals();
		} else if (exchangeObject instanceof GUIScheduleComExchange) {
			schedules = ((GUIScheduleComExchange) exchangeObject).getSchedules();
			stepSize = ((GUIScheduleComExchange) exchangeObject).getStepSize();
			cleanUpSchedulesAndSend();
		}
	}
}
