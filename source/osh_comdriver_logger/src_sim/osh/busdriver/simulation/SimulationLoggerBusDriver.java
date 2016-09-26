package osh.busdriver.simulation;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.UUID;

import osh.busdriver.LoggerBusDriver;
import osh.configuration.OSHParameterCollection;
import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.logger.LoggerAncillaryCommoditiesHALExchange;
import osh.datatypes.logger.LoggerDetailedCostsHALExchange;
import osh.datatypes.logger.LoggerEpsPlsHALExchange;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.DevicesPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.hal.exchange.LoggerCommodityPowerHALExchange;
import osh.hal.exchange.LoggerDevicesPowerHALExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class SimulationLoggerBusDriver extends LoggerBusDriver {
	
	private boolean firstLineSum = true;
	private boolean firstLineDetails = true;
	private boolean firstLineVirtualCommodities = true;
	private boolean firstLineEpsPls = true;
	

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 */
	public SimulationLoggerBusDriver(IOSH controllerbox,
			UUID deviceID, OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);

	}
	
	
	/**
	 * Register to Timer for timed logging operations (logger gets data to log by itself)<br>
	 * Register to DriverRegistry for logging operations trigger by Drivers
	 */
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
	}

	/**
	 * Get things to log from O/C-layer
	 * @param exchangeObject
	 */
	@SuppressWarnings("unused")
	@Override
	public void updateDataFromBusManager(IHALExchange exchangeObject) {
		long now = getTimer().getUnixTime();
		
		if ( valueLoggerConfiguration != null && valueLoggerConfiguration.getIsValueLoggingToFileActive() ) {
			if (exchangeObject instanceof LoggerCommodityPowerHALExchange) {
				
				LoggerCommodityPowerHALExchange lcphe = (LoggerCommodityPowerHALExchange) exchangeObject;
				
				CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
						lcphe.getDeviceID(), 
						lcphe.getTimestamp(),
						lcphe.getPowerState(),
						DeviceTypes.OTHER);
				
				if (firstLineSum) {
					String firstEntry = "";
					for (Commodity c : Commodity.values()) {
						firstEntry += c + ";";
					}
					fileLog.logPower(firstEntry);
					firstLineSum = false;
				}
				
				String entryLine = "";
				
				for (Commodity c : Commodity.values()) {
					Double power = cpse.getPowerState(c);
					if (power != null) {
						entryLine += power + ";";
					}
					else {
						entryLine += "0;";
					}
				}
					
				fileLog.logPower(entryLine);
			}
		
			else if (exchangeObject instanceof LoggerDevicesPowerHALExchange) {
				
				LoggerDevicesPowerHALExchange ldphe = (LoggerDevicesPowerHALExchange) exchangeObject;
				
				DevicesPowerStateExchange dpse = new DevicesPowerStateExchange(
						ldphe.getDeviceID(), 
						ldphe.getTimestamp(), 
						ldphe.getPowerStates());
				
				if (firstLineDetails) {
					String entryLine = "";
					
					// induction cooktop
					for (Commodity c : Commodity.values()) {
						entryLine += "HB_" + c + ";";
					}
					
					// dishwasher
					for (Commodity c : Commodity.values()) {
						entryLine += "DW_" + c + ";";
					}
					
					// oven
					for (Commodity c : Commodity.values()) {
						entryLine += "OV_" + c + ";";
					}
			        
			        // dryer
					for (Commodity c : Commodity.values()) {
						entryLine += "DR_" + c + ";";
					}
			        
			        // washer
					for (Commodity c : Commodity.values()) {
						entryLine += "WM_" + c + ";";
					}
			        
			        // PV
					for (Commodity c : Commodity.values()) {
						entryLine += "PV_" + c + ";";
					}
					
					// baseload
					for (Commodity c : Commodity.values()) {
						entryLine += "BL_" + c + ";";
					}
					
					// CHP
					for (Commodity c : Commodity.values()) {
						entryLine += "CHP_" + c + ";";
					}
					
					fileLog.logPowerDetails(entryLine);
					firstLineDetails = false;
				}	
				
				HashMap<UUID,EnumMap<Commodity,Double>> map = dpse.getPowerStateMap();
				String entryLine = "";

				// induction cooktop
				UUID hb = UUID.fromString("00000000-4D49-4D49-4948-000000000000");
				for (Commodity c : Commodity.values()) {
					if (map.get(hb) == null) {
						entryLine += "0;";
					}
					else
					entryLine += map.get(hb).get(c) + ";";
				}
				// dishwasher
				UUID dw = UUID.fromString("00000000-4D49-4D49-4457-000000000000");
				for (Commodity c : Commodity.values()) {
					if (map.get(dw) == null) {
						entryLine += "0;";
					}
					else
					entryLine += map.get(dw).get(c) + ";";
				}
				// oven
				UUID ov = UUID.fromString("00000000-4D49-4D49-4F56-000000000000");
				for (Commodity c : Commodity.values()) {
					if (map.get(ov) == null) {
						entryLine += "0;";
					}
					else
					entryLine += map.get(ov).get(c) + ";";
				}
				
				// dryer
				UUID dr = UUID.fromString("00000000-4D49-4D49-5444-000000000000");
				for (Commodity c : Commodity.values()) {
					if (map.get(dr) == null) {
						entryLine += "0;";
					}
					else
					entryLine += map.get(dr).get(c) + ";";
				}
				
				// washer
				UUID wm = UUID.fromString("00000000-4D49-4D49-574D-000000000000");
				for (Commodity c : Commodity.values()) {
					if (map.get(wm) == null) {
						entryLine += "0;";
					}
					else
					entryLine += map.get(wm).get(c) + ";";
				}
				
				// PV
				UUID pv = UUID.fromString("484F4C4C-0000-0000-5056-000000000000");
				for (Commodity c : Commodity.values()) {
					if (map.get(pv) == null) {
						entryLine += "0;";
					}
					else
					entryLine += map.get(pv).get(c) + ";";
				}
				
				// baseload
				UUID bl = UUID.fromString("00000000-0000-5348-424C-000000000000");
				for (Commodity c : Commodity.values()) {
					if (map.get(bl) == null) {
						entryLine += "0;";
					}
					else
					entryLine += map.get(bl).get(c) + ";";
				}

				// CHP
				UUID chp_eshl = UUID.fromString("44414348-5300-0043-4850-000000000000");
				for (Commodity c : Commodity.values()) {
					if (map.get(chp_eshl) == null) {
						entryLine += "0;";
					}
					else
					entryLine += map.get(chp_eshl).get(c) + ";";
				}
				
				fileLog.logPowerDetails(entryLine);
			}
			else if (exchangeObject instanceof LoggerAncillaryCommoditiesHALExchange) {
				LoggerAncillaryCommoditiesHALExchange lvche = (LoggerAncillaryCommoditiesHALExchange) exchangeObject;
				EnumMap<AncillaryCommodity,Integer> map = lvche.getMap();
				
				if (firstLineVirtualCommodities) {
					String entryLine = "timestamp;";
					
					for (AncillaryCommodity vc : AncillaryCommodity.values()) {
						entryLine = entryLine + vc.toString() + ";";
					}
					
					fileLog.logAncillaryCommodityPowerDetails(entryLine);
					firstLineVirtualCommodities = false;
				}
				String entryLine = now + ";";
				
				for (AncillaryCommodity vc : AncillaryCommodity.values()) {
					Integer value = map.get(vc);
					if (value == null) {
						value = 0;
					}
					entryLine = entryLine + value + ";";
				}
				
				fileLog.logAncillaryCommodityPowerDetails(entryLine);
			}
			
			else if (exchangeObject instanceof LoggerEpsPlsHALExchange) {
				LoggerEpsPlsHALExchange lephe = (LoggerEpsPlsHALExchange) exchangeObject;
				
				if (firstLineEpsPls) {
					String entryLine = "";
					
					for (AncillaryCommodity vc : AncillaryCommodity.values()) {
						entryLine = entryLine + "EPS_" + vc.toString() + ";";
					}
					
					for (AncillaryCommodity vc : AncillaryCommodity.values()) {
						entryLine = entryLine + "PLS_upper_" + vc.toString() + ";";
						entryLine = entryLine + "PLS_lower_" + vc.toString() + ";";
					}
					
					fileLog.logExternalSignals(entryLine);
					firstLineEpsPls = false;
				}
				
				String entryLine = "";
				
				for (AncillaryCommodity vc : AncillaryCommodity.values()) {
					EnumMap<AncillaryCommodity,PriceSignal> pss = lephe.getPs();
					
					double value = 0.0;
					if (pss != null) {
						PriceSignal ps = pss.get(vc);
						if (ps != null) {
							value = ps.getPrice(now);
						}
					}
					
					entryLine = entryLine + value + ";";
				}
				
				for (AncillaryCommodity vc : AncillaryCommodity.values()) {
					EnumMap<AncillaryCommodity, PowerLimitSignal> plss = lephe.getPwrLimit();
					
					double uvalue = 0.0;
					double lvalue = 0.0;
					if (plss != null) {
						PowerLimitSignal pls = plss.get(vc);
						if (pls != null) {
							uvalue = pls.getPowerUpperLimit(now);
							lvalue = pls.getPowerLowerLimit(now);
						}
					}
					
					entryLine = entryLine + uvalue + ";" + lvalue + ";";
				}
				
				fileLog.logExternalSignals(entryLine);
			}
			
			else if (exchangeObject instanceof LoggerDetailedCostsHALExchange) {
				LoggerDetailedCostsHALExchange lvche = (LoggerDetailedCostsHALExchange) exchangeObject;
				EnumMap<AncillaryCommodity,Integer> map = lvche.getPowerValueMap();
				
				if (firstLineVirtualCommodities) {
					String entryLine = "timestamp;";
					
					for (AncillaryCommodity vc : AncillaryCommodity.values()) {
						entryLine = entryLine + vc.toString() + ";";
					}
					
					for (AncillaryCommodity vc : AncillaryCommodity.values()) {
						entryLine = entryLine + "EPS_" + vc.toString() + ";";
					}
					
					for (AncillaryCommodity vc : AncillaryCommodity.values()) {
						entryLine = entryLine + "PLS_upper_" + vc.toString() + ";";
						entryLine = entryLine + "PLS_lower_" + vc.toString() + ";";
					}
					
					// total costs
					entryLine = entryLine + "totalEPScosts";
					
					fileLog.logCostDetailed(entryLine);
					firstLineVirtualCommodities = false;
				}
				
				String entryLine = now + ";";
				
				for (AncillaryCommodity vc : AncillaryCommodity.values()) {
					Integer value = map.get(vc);
					if (value == null) {
						value = 0;
					}
					entryLine = entryLine + value + ";";
				}
				
				for (AncillaryCommodity vc : AncillaryCommodity.values()) {
					EnumMap<AncillaryCommodity,PriceSignal> pss = lvche.getPs();
					
					double value = 0.0;
					if (pss != null) {
						PriceSignal ps = pss.get(vc);
						if (ps != null) {
							value = ps.getPrice(now);
						}
					}
					
					entryLine = entryLine + value + ";";
				}
				
				for (AncillaryCommodity vc : AncillaryCommodity.values()) {
					EnumMap<AncillaryCommodity, PowerLimitSignal> plss = lvche.getPwrLimit();
					
					double uvalue = 0.0;
					double lvalue = 0.0;
					if (plss != null) {
						PowerLimitSignal pls = plss.get(vc);
						if (pls != null) {
							uvalue = pls.getPowerUpperLimit(now);
							lvalue = pls.getPowerLowerLimit(now);
						}
					}
					
					entryLine = entryLine + uvalue + ";" + lvalue + ";";
				}
				
				// total costs
				double costs = 0.0;
				
				{
					// EPS
					double additional = 0;
					
					double currentActivePowerExternal = 0;
					if (lvche.getPowerValueMap().get(AncillaryCommodity.ACTIVEPOWEREXTERNAL) != null
							&& lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN) != null
							&& lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) != null) {
						currentActivePowerExternal = lvche.getPowerValueMap().get(AncillaryCommodity.ACTIVEPOWEREXTERNAL)
								+ lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN)
								+ lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);
					}
					
					double currentActivePowerPv = 0;
					if (lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN) != null
							&& lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION) != null) {
						currentActivePowerPv = lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN)
								+ lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION);
					}
					
					double currentActivePowerChp = 0;
					if (lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) != null
							&& lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION) != null) {
						currentActivePowerPv = lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)
								+ lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION);
					}
					
					// FIXME
					
//					additional = additional + EPSCostCalculator.calcEpsOptimizationObjective3(
//							now, 
//							1.0 / 3600000.0, 
//							lvche.getPs(), 
//							currentActivePowerExternal, 
//							currentActivePowerPv, 
//							currentActivePowerChp);
					
					double currentGasPower = 0;
					if (lvche.getPowerValueMap().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL) != null) {
						currentGasPower = lvche.getPowerValueMap().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL);
					}
					
					// gas
//					additional = additional + EPSCostCalculator.calcNaturalGasPower(
//							now, 
//							1.0 / 3600000.0, 
//							lvche.getPs(), 
//							currentGasPower);
					
					costs = costs + additional;
					
					//PLS
					//FIXME
					
					
				}
				
				entryLine = entryLine + costs;
				
				fileLog.logCostDetailed(entryLine);
			}
			
		}
		
	}
	

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
}
