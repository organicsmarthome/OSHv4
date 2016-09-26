package osh.mgmt.busmanager;

import java.util.UUID;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.Exchange;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;

/**
 * 
 * @author Florian Allerding, Ingo Mauser
 *
 */
public class KITLoggerBusManager extends LoggerBusManager {

//	private UUID chpsource = null;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param uuid
	 */
	public KITLoggerBusManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
		
//		this.ocRegistry.registerStateChangeListener(LoggerScheduleStateExchange.class, this);
//		this.ocRegistry.registerStateChangeListener(LoggerPriceAndLimitStateExchange.class, this);
//		this.ocRegistry.registerStateChangeListener(LoggerDeviceListStateExchange.class, this);
//		this.ocRegistry.registerStateChangeListener(LoggerWaterStorageStateExchange.class, this);
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		super.onQueueEventTypeReceived(type, event);
		
		Exchange tosend = null;

		if (event instanceof StateChangedExchange  && ((StateChangedExchange) event).getStatefulentity().equals(this.getUUID())) {
//			StateChangedExchange exsc = (StateChangedExchange) ex;
//			if (exsc.getType().equals(LoggerScheduleStateExchange.class)) {
//				tosend = (LoggerScheduleStateExchange) this.ocRegistry.getState(LoggerScheduleStateExchange.class, exsc.getStatefulentity());
//			} else if (exsc.getType().equals(LoggerDeviceListStateExchange.class)) {
//				tosend = (LoggerDeviceListStateExchange) this.ocRegistry.getState(LoggerDeviceListStateExchange.class, exsc.getStatefulentity());
//			} else if (exsc.getType().equals(LoggerPriceAndLimitStateExchange.class)) {
//				tosend = (LoggerPriceAndLimitStateExchange) this.ocRegistry.getState(LoggerPriceAndLimitStateExchange.class, exsc.getStatefulentity());
//			} else if (exsc.getType().equals(LoggerWaterStorageStateExchange.class)) {
//				//prevent two chps to interleave data. This solution is not very clever, but implemented in 5 lines
//				if (chpsource == null) {
//					chpsource = exsc.getStatefulentity();
//				}
//				if (chpsource.equals(exsc.getStatefulentity())) {
//					tosend = (LoggerWaterStorageStateExchange) this.ocRegistry.getState(LoggerWaterStorageStateExchange.class, exsc.getStatefulentity());
//				}
//			}
		} else {
			tosend = event;
		}

		if (tosend != null) {
//			updateUnit(new GenericAbstractExchangeHALWrapper(getUUID(), tosend.getTimestamp(), tosend));
		}
	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
//		double posSum = 0.0, negSum = 0.0, sum = 0.0;
//		
//		for (StateExchange se : this.ocRegistry.getStates(CommodityPowerStateExchange.class).values()) {
//			CommodityPowerStateExchange pd = (CommodityPowerStateExchange) se;
//			
//			Double tmp = pd.getPowerState(Commodity.ACTIVEPOWER);
//			double power = (tmp != null ? tmp : 0.0);
//			
//			if (power < 0) negSum += power; else posSum += power;
//			sum += power;
//		}
//		
	}
	
	
}
