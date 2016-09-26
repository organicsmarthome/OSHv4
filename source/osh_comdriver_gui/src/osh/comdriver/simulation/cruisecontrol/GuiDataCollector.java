package osh.comdriver.simulation.cruisecontrol;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.cruisecontrol.GUIBatteryStorageStateExchange;
import osh.datatypes.cruisecontrol.OptimizedDataStorage;
import osh.datatypes.cruisecontrol.PowerSum;
import osh.datatypes.ea.Schedule;
import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;


/**
 * 
 * @author Till Schuberth, Kaibin Bao, Ingo Mauser, Jan Mueller, Sebastian Kramer
 *
 */
public class GuiDataCollector {

	private GuiMain driver;
	
	private List<Schedule> schedules;
	private EnumMap<AncillaryCommodity,PriceSignal> ps;
	private EnumMap<AncillaryCommodity,PowerLimitSignal> pwrLimit;
	private HashMap<UUID, EnumMap<Commodity, Double>> powerStates;
	private HashMap<UUID, TreeMap<Long, Double>> tankTemps = new HashMap<UUID, TreeMap<Long, Double>>();
	private HashMap<UUID, TreeMap<Long, Double>> hotWaterDemands = new HashMap<UUID, TreeMap<Long, Double>>();
	private HashMap<UUID, TreeMap<Long, Double>> hotWaterSupplys = new HashMap<UUID,TreeMap<Long, Double>>();
	
//			new OptimizedDataStorage<GUIWaterStorageStateExchange>();
	private HashMap<UUID, OptimizedDataStorage<GUIBatteryStorageStateExchange>> batteryStorageHistories = 
			new HashMap<>();
//			new OptimizedDataStorage<GUIWaterStorageStateExchange>();
	private EnumMap<Commodity,OptimizedDataStorage<PowerSum>> powerhistory = new EnumMap<>(Commodity.class);
	private boolean saveGraph;
	private TreeMap<Long, Double> predictedTankTemp = new TreeMap<Long, Double>();
	private TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<Long, Double>();
	private TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<Long, Double>();

	private HashMap<UUID, Long> lastWaterRefresh = new HashMap<>();
	private HashMap<UUID, Long> lastBatteryRefresh = new HashMap<>();
	
	private final int removeOlderThan = 4 * 86400;
	
	public GuiDataCollector(GuiMain driver, boolean saveGraph) {
		if (driver == null) throw new NullPointerException("driver is null");
		
		this.saveGraph = saveGraph;
		this.driver = driver;
	}
	
	public void updateEADeviceList(Set<DeviceTableEntry> deviceList) {
		driver.refreshDeviceTable(deviceList);
	}
	
	public void updateStateView(Set<Class<? extends StateExchange>> types, Map<UUID, ? extends StateExchange> states) {
		driver.refreshStateTable(types, states);
	}
	
	private void waitforuser(long timestamp) {
		//in case we are waiting deliver an updated water diagram
		//this is a dirty hack, but who cares
		//SEKR: the one getting a nullpointerexception at the start, another dirty hack:
		//check if a powerhistory exists before updating
		if (powerhistory.containsKey(Commodity.ACTIVEPOWER))
			doRealPastUpdate(timestamp,"water");
		
		driver.waitForUserIfRequested();
	}

	public void updateGlobalSchedule(
			EnumMap<AncillaryCommodity,PriceSignal> ps,
			long timestamp) {
		this.ps = ps;
		driver.refreshDiagram(this.schedules, ps, this.pwrLimit, timestamp, this.saveGraph);
		waitforuser(timestamp);

	}
	
	public void updateGlobalSchedule(
			long timestamp,
			EnumMap<AncillaryCommodity,PowerLimitSignal> pwrLimit) {
		this.pwrLimit = pwrLimit;
		driver.refreshDiagram(schedules, ps, pwrLimit, timestamp, saveGraph);
		waitforuser(timestamp);
	}
	
	public void updateGlobalSchedule(
			List<Schedule> schedules, 
			long timestamp) {
		this.schedules = schedules;
		driver.refreshDiagram(schedules, ps, pwrLimit, timestamp, saveGraph);
		waitforuser(timestamp);
	}
	
	public void updateAncillaryMeter(
			AncillaryCommodityLoadProfile ancillaryMeter,
			long timestamp) {
		driver.refreshMeter(ancillaryMeter, timestamp);
		waitforuser(timestamp);
	}
	
	public synchronized void updateWaterStorageData(WaterStorageOCSX exws) {
		UUID tankId = exws.getTankId();

		if (tankTemps.get(tankId) == null) {
			tankTemps.put(tankId, new TreeMap<Long, Double>());
			lastWaterRefresh.put(tankId, 0L);
		}
		tankTemps.get(tankId).put(exws.getTimestamp(), exws.getCurrenttemp());
		
		if (hotWaterDemands.get(tankId) == null) {
			hotWaterDemands.put(tankId, new TreeMap<Long, Double>());
		}
		hotWaterDemands.get(tankId).put(exws.getTimestamp(), exws.getDemand());
		
		if (hotWaterSupplys.get(tankId) == null) {
			hotWaterSupplys.put(tankId, new TreeMap<Long, Double>());
		}
		hotWaterSupplys.get(tankId).put(exws.getTimestamp(), exws.getSupply());
		
		checkForPastUpdateOfWater(exws.getTimestamp());
	}
	
	public synchronized void updateWaterPredictionData(
			TreeMap<Long, Double> predictedTankTemp,
			TreeMap<Long, Double> predictedHotWaterDemand,
			TreeMap<Long, Double> predictedHotWaterSupply,
			long timestamp) {
		
		if (!predictedTankTemp.isEmpty()) {
			long predicitionStart = predictedTankTemp.firstKey();
			this.predictedTankTemp.tailMap(predicitionStart).clear();
			this.predictedTankTemp.putAll(predictedTankTemp);
		}
		if (!predictedHotWaterDemand.isEmpty()) {
			long predicitionStart = predictedHotWaterDemand.firstKey();
			this.predictedHotWaterDemand.tailMap(predicitionStart).clear();
			this.predictedHotWaterDemand.putAll(predictedHotWaterDemand);
		}
		if (!predictedHotWaterSupply.isEmpty()) {
			long predicitionStart = predictedHotWaterSupply.firstKey();
			this.predictedHotWaterSupply.tailMap(predicitionStart).clear();
			this.predictedHotWaterSupply.putAll(predictedHotWaterSupply);
		}
		
		checkForWaterPredictionHistoryCleanup(timestamp);
		driver.refreshWaterPredictionDiagram(this.predictedTankTemp, this.predictedHotWaterDemand, this.predictedHotWaterSupply);
		waitforuser(timestamp);
	}

	public synchronized void updateBatteryStorageData(BatteryStorageOCSX exbs) {
		UUID batteryId = exbs.getBatteryId();
		
		GUIBatteryStorageStateExchange guiBatteryStorageStateExchange = 
				new GUIBatteryStorageStateExchange(	
						exbs.getSender(), 
						exbs.getTimestamp(), 
						exbs.getStateOfCharge(), 
						exbs.getMinStateOfCharge(), 
						exbs.getMaxStateOfCharge(),
						batteryId);
		if (batteryStorageHistories.get(batteryId) == null) {
			batteryStorageHistories.put(batteryId, new OptimizedDataStorage<GUIBatteryStorageStateExchange>());
			lastBatteryRefresh.put(batteryId, 0L);
		}
		batteryStorageHistories.get(batteryId).add(exbs.getTimestamp(), guiBatteryStorageStateExchange);
		Object o = batteryStorageHistories.get(batteryId);
		checkForPastUpdateOfBattery(exbs.getTimestamp());
	}
	
	
	public void updatePowerStates(
			Long timestamp,
			HashMap<UUID, EnumMap<Commodity, Double>> powerStates) {
		EnumMap<Commodity,PowerSum> commodityPowerSum = new EnumMap<>(Commodity.class);
		
		this.powerStates = powerStates;

		if (powerStates != null) {
			for (Entry<UUID, EnumMap<Commodity, Double>> e : powerStates.entrySet()) {
				EnumMap<Commodity, Double> comMap = e.getValue();
				
				for (Entry<Commodity,Double> f : comMap.entrySet()) {
					PowerSum existingSum = commodityPowerSum.get(f.getKey());
					if (existingSum == null) {
						existingSum = new PowerSum(0, 0, 0);
					}
					double posSum = existingSum.getPosSum();
					double negSum = existingSum.getNegSum();
					double sum = existingSum.getSum();
					
					Double value = f.getValue();
					if (value == null) {
						value = 0.0;
					}
					
					// add to totalSum
					sum += value;
					
					if (value > 0) {
						posSum += value;
					}
					else if (value < 0) {
						negSum += value;
					}
					
					PowerSum newSum = new PowerSum(posSum, negSum, sum);
					commodityPowerSum.put(f.getKey(), newSum);
				} /* for( ... Commodity ... ) */
			} /* for( ... UUID ... ) */
			
			for( Entry<Commodity, PowerSum> ent : commodityPowerSum.entrySet() ) {
				OptimizedDataStorage<PowerSum> timeseries = powerhistory.get(ent.getKey());
				if( timeseries == null ) {
					timeseries = new OptimizedDataStorage<>();
					powerhistory.put(ent.getKey(), timeseries);
				}
				timeseries.add(timestamp, ent.getValue());
			}
		}

		checkForPastUpdateOfWater(timestamp);
		checkForPastUpdateOfBattery(timestamp);
		checkForPowerHistoryCleanup(timestamp);
		checkForWaterStorageHistoryCleanup(timestamp);
		checkForBatteryStorageHistoryCleanup(timestamp);
	}

	private void checkForPastUpdateOfWater(long now) {
		if (lastWaterRefresh.isEmpty()) {
			return;
		}
		else {
			for (Entry<UUID,Long> e : lastWaterRefresh.entrySet()) {
				//refresh only every hour
				if (Math.abs(e.getValue() - now) > 3600) {
					lastWaterRefresh.put(e.getKey(), now);
					doRealPastUpdate(now,"water");
				}
			}
		}
	}

	private void checkForPastUpdateOfBattery(long now) {
		if (lastBatteryRefresh.isEmpty()) {
			return;
		}
		else {
			for (Entry<UUID,Long> e : lastBatteryRefresh.entrySet()) {
				//refresh only every hour
				if (Math.abs(e.getValue() - now) > 3600) {
					lastBatteryRefresh.put(e.getKey(), now);
					doRealPastUpdate(now,"battery");
				}
			}
		}
	}
	
	
	private synchronized void doRealPastUpdate(long timestamp, String type) {
		// WaterStorage
		if (type == "water") {
			driver.refreshWaterDiagram(tankTemps, hotWaterDemands, hotWaterSupplys);
		}
		// BatteryStorage
		else if (type == "battery") {
			HashMap<UUID,TreeMap<Long, GUIBatteryStorageStateExchange>> optimizedBatteryStorageHistories = new HashMap<>();
			for (Entry<UUID, OptimizedDataStorage<GUIBatteryStorageStateExchange>> e : batteryStorageHistories.entrySet()) {
				optimizedBatteryStorageHistories.put(e.getKey(), e.getValue().getMap());
			}
			driver.refreshBatteryDiagram(optimizedBatteryStorageHistories);
		}
		
		
		// PowerSum History
		EnumMap<Commodity,TreeMap<Long,PowerSum>> commodityPowerSum = new EnumMap<>(Commodity.class);
		
		for( Commodity c : powerhistory.keySet() ) {
			TreeMap<Long,PowerSum> powersumseries = powerhistory.get(c).getMap();
			commodityPowerSum.put(c,powersumseries);
		}
		
		driver.refreshPowerSumDiagram(timestamp, commodityPowerSum);
	}
	
	private void checkForPowerHistoryCleanup(long timestamp) {
		if ( timestamp % 3600 == 0 ) {
			
			synchronized (powerhistory) {
				for( Commodity c : powerhistory.keySet() ) {
					Map<Long,PowerSum> map = powerhistory.get(c).getMap();
					
					for (Iterator<Map.Entry<Long, PowerSum>> it = map.entrySet().iterator(); it.hasNext();) {
						Entry<Long,PowerSum> e = it.next();
						if ( e.getKey() < (timestamp - this.removeOlderThan) ) {
							it.remove();
						}
					}
				}
			}
			
		}
	}

	
	private void checkForWaterStorageHistoryCleanup(long timestamp) {
		if ( timestamp % 86400 == 0 ) {
			synchronized (tankTemps) {
				for (UUID key : tankTemps.keySet()) {
					TreeMap<Long, Double> newerValues = new TreeMap<Long, Double>();
					newerValues.putAll(tankTemps.get(key).tailMap(timestamp - this.removeOlderThan));					
					tankTemps.put(key, newerValues);
				}
			}
			synchronized (hotWaterDemands) {
				for (UUID key : hotWaterDemands.keySet()) {
					TreeMap<Long, Double> newerValues = new TreeMap<Long, Double>();
					newerValues.putAll(hotWaterDemands.get(key).tailMap(timestamp - this.removeOlderThan));					
					hotWaterDemands.put(key, newerValues);
				}
			}
			synchronized (hotWaterSupplys) {
				for (UUID key : hotWaterSupplys.keySet()) {
					TreeMap<Long, Double> newerValues = new TreeMap<Long, Double>();
					newerValues.putAll(hotWaterSupplys.get(key).tailMap(timestamp - this.removeOlderThan));					
					hotWaterSupplys.put(key, newerValues);
				}
			}
		}
	}
	
	private void checkForWaterPredictionHistoryCleanup(long timestamp) {
		if ( timestamp % 86400 == 0 ) {			
			synchronized (predictedTankTemp) {
				TreeMap<Long, Double> newerValues = new TreeMap<Long, Double>();
				newerValues.putAll(predictedTankTemp.tailMap(timestamp - this.removeOlderThan));
				predictedTankTemp = newerValues;
			}			
			synchronized (predictedHotWaterDemand) {					
				TreeMap<Long, Double> newerValues = new TreeMap<Long, Double>();
				newerValues.putAll(predictedHotWaterDemand.tailMap(timestamp - this.removeOlderThan));
				predictedHotWaterDemand = newerValues;
			}
			synchronized (predictedHotWaterSupply) {					
				TreeMap<Long, Double> newerValues = new TreeMap<Long, Double>();
				newerValues.putAll(predictedHotWaterSupply.tailMap(timestamp - this.removeOlderThan));
				predictedHotWaterSupply = newerValues;
			}
		}
	}
	
	private void checkForBatteryStorageHistoryCleanup(long timestamp) {
		if ( timestamp % 3600 == 0 ) {	
			synchronized (batteryStorageHistories) {
				TreeMap<Long, GUIBatteryStorageStateExchange> optimizedBatteryStorageHistories = new TreeMap<>();
				for (Entry<UUID, OptimizedDataStorage<GUIBatteryStorageStateExchange>> e : batteryStorageHistories.entrySet()) {
					optimizedBatteryStorageHistories = (e.getValue().getMap());
					for (Iterator<Map.Entry<Long, GUIBatteryStorageStateExchange>> it = optimizedBatteryStorageHistories.entrySet().iterator(); it.hasNext();) {
						Entry<Long,GUIBatteryStorageStateExchange> ex = it.next();
						if ( ex.getKey() < (timestamp - this.removeOlderThan) ) {
							it.remove();
						}
					}
				}
			}
			
		}
	}

}
