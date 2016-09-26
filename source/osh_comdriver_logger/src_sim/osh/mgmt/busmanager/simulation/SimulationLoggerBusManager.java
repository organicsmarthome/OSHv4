package osh.mgmt.busmanager.simulation;

import java.util.UUID;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.EventExchange;
import osh.mgmt.busmanager.LoggerBusManager;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class SimulationLoggerBusManager extends LoggerBusManager {

	/**
	 * CONSTRUCTOR
	 */
	public SimulationLoggerBusManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
	}


	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
//		// get overall power state and communicate to driver for logging
//		{
//			CommodityPowerStateExchange cpse = 
//					this.getOCRegistry().getState(
//							CommodityPowerStateExchange.class, 
//							getGlobalOCUnitUUID());
//
//			if (cpse != null) {
//				LoggerCommodityPowerHALExchange cphe = 
//						new LoggerCommodityPowerHALExchange(
//								cpse.getSender(), 
//								cpse.getTimestamp(), 
//								cpse.getPowerState());
//				updateOcDataSubscriber(cphe);
//			}
//		}
//		
//		// get power states of all devices and communicate to logger
//		{
////			DevicesPowerStateExchange dpse = 
////					this.getOCRegistry().getState(
////							DevicesPowerStateExchange.class, 
////							getGlobalOCUnitUUID());
////			
////			if (dpse != null) {
////				LoggerDevicesPowerHALExchange dphe = 
////						new LoggerDevicesPowerHALExchange(
////								dpse.getSender(), 
////								dpse.getTimestamp(), 
////								dpse.getPowerStateMap());
////				updateOcDataSubscriber(dphe);
////			}
//		}
//		
//		// get AncillaryCommodity Power states and communicate to logger
//		{
//			AncillaryCommodityPowerStateExchange vcse = 
//					this.getOCRegistry().getState(
//							AncillaryCommodityPowerStateExchange.class, 
//							getGlobalOCUnitUUID());
//			
//			if (vcse != null) {
//				LoggerAncillaryCommoditiesHALExchange vche =
//						new LoggerAncillaryCommoditiesHALExchange(
//								vcse.getSender(), 
//								vcse.getTimestamp(), 
//								vcse.getMap());
//				updateOcDataSubscriber(vche);
//			}
//		}
//		
//		// get EPS and PLS states and communicate to logger
//		{
//			EpsPlsStateExchange epse = 
//					this.getOCRegistry().getState(
//							EpsPlsStateExchange.class, 
//							getGlobalOCUnitUUID());
//			
//			if (epse != null) {
//				LoggerEpsPlsHALExchange ephe =
//						new LoggerEpsPlsHALExchange(
//								epse.getSender(), 
//								epse.getTimestamp(), 
//								epse.getPs(), 
//								epse.getPwrLimit());
//				updateOcDataSubscriber(ephe);
//			}
//		}
//		
//		// get AncillaryCommodity Power states and EPS and PLS states and communicate to logger
//		{
//			DetailedCostsLoggingStateExchange vcse = 
//					this.getOCRegistry().getState(
//							DetailedCostsLoggingStateExchange.class, 
//							getGlobalOCUnitUUID());
//			
//			if (vcse != null) {
//				LoggerDetailedCostsHALExchange vche =
//						new LoggerDetailedCostsHALExchange(
//								vcse.getSender(), 
//								vcse.getTimestamp(), 
//								vcse.getMap(),
//								vcse.getPs(),
//								vcse.getPwrLimit());
//				updateOcDataSubscriber(vche);
//			}
//		}
	}


	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T event) throws OSHException {
		//TODO
	}
	
	private UUID getGlobalOCUnitUUID() {
		return getOSH().getGlobalObserver().getAssignedOCUnit().getUnitID();
	}

}
