package osh.mgmt.globalobserver;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.GlobalObserver;
import osh.core.oc.LocalOCUnit;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.AncillaryCommodityPowerStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.DetailedCostsLoggingStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.DevicesPowerStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIDeviceListStateExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.utils.uuid.UUIDLists;



/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public class OSHGlobalObserver 
					extends GlobalObserver 
					implements IEventTypeReceiver, IHasState {

	private class ProblemPartComparator implements Comparator<InterdependentProblemPart<?, ?>> {

		@Override
		public int compare(InterdependentProblemPart<?, ?> o1, InterdependentProblemPart<?, ?> o2) {
			return o1.getDeviceID().compareTo(o2.getDeviceID());
		}
		
	}
	
	
	private Map<UUID, InterdependentProblemPart<?, ?>> iproblempart;
	private boolean reschedule = false;
	
	private HashMap<UUID,EnumMap<Commodity,Double>> deviceCommodityMap = new HashMap<>();
	private EnumMap<Commodity,Double> commodityTotalPowerMap = new EnumMap<Commodity, Double>(Commodity.class);
	
	private EnumMap<AncillaryCommodity,PriceSignal> priceSignals = new EnumMap<>(AncillaryCommodity.class);
	private EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);
	
	List<UUID> totalPowerMeter = null;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param configurationParameters
	 * @throws OSHException 
	 */
	public OSHGlobalObserver(
			IOSHOC controllerbox, 
			OSHParameterCollection configurationParameters) throws OSHException {
		super(controllerbox, configurationParameters);

		this.iproblempart = Collections.synchronizedMap(new HashMap<UUID, InterdependentProblemPart<?, ?>>());
		
		String strTotalPowerMeter = configurationParameters.getParameter("totalpowermeter");
		if( strTotalPowerMeter != null ) {
			totalPowerMeter = parseUUIDArray(strTotalPowerMeter);
		}
	}

	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		this.getTimer().registerComponent(this, 1);
		
		this.getOCRegistry().registerStateChangeListener(InterdependentProblemPart.class, this);
		this.getOCRegistry().register(StateChangedExchange.class, this);
		
		this.getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
		this.getOCRegistry().registerStateChangeListener(PlsStateExchange.class, this);
	}

	@Override
	public void onSystemShutdown() {
		// finalize everything
			//currently nothing
	}

	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(
			Class<T> type, T ex) throws OSHException {
			boolean problemPartListChanged = false;
			
			if (ex instanceof StateChangedExchange) {
				StateChangedExchange sce = (StateChangedExchange) ex;
				
				if (sce.getType().equals(EpsStateExchange.class)) {
					EpsStateExchange eee = this.getOCRegistry().getState(EpsStateExchange.class, sce.getStatefulentity());
					this.priceSignals = eee.getPriceSignals();
				}
				else if (sce.getType().equals(PlsStateExchange.class)) {
					PlsStateExchange eee = this.getOCRegistry().getState(PlsStateExchange.class, sce.getStatefulentity());
					this.powerLimitSignals = eee.getPowerLimitSignals();
				}
				
				else if (sce.getType().equals(InterdependentProblemPart.class)) {
					UUID entity = sce.getStatefulentity();
					InterdependentProblemPart<?, ?> ppex = (InterdependentProblemPart<?, ?>) this.getOCRegistry().getState(InterdependentProblemPart.class, entity);
					this.iproblempart.put(entity, ppex);
					if (ppex.isToBeScheduled())  {
						reschedule = true;
					}
					problemPartListChanged = true;
				}
			}
			if (problemPartListChanged && this.getControllerBoxStatus().hasGUI()) {
				this.getOCRegistry().setState(
						GUIDeviceListStateExchange.class, this,
						new GUIDeviceListStateExchange(
								getUUID(), 
								getTimer().getUnixTime(),
								getDeviceList(getProblemParts())
						)
				);
			}
			
	}

	
	private Set<DeviceTableEntry> getDeviceList(List<InterdependentProblemPart<?, ?>> problemparts) {
		Set<DeviceTableEntry> entries = new HashSet<DeviceTableEntry>();
		int i = 1;
		for (InterdependentProblemPart<?, ?> p : problemparts) {
			String type = null;
			try {
				LocalObserver lo = getLocalObserver(p.getDeviceID());
				LocalOCUnit ocunit = lo.getAssignedOCUnit();
				type = ocunit.getDeviceType().toString() + "(" + ocunit.getDeviceClassification().toString() + ")";
			} catch (NullPointerException e) {}
			DeviceTableEntry e = new DeviceTableEntry(i, p.getDeviceID(), type, p.getBitCount(), "[" + p.getTimestamp() + "] " + p.isToBeScheduled(), p.problemToString());
			entries.add(e);
			i++;
		}
		return entries;
	}

	/**
	 * Collect all EAProblemParts from local observers
	 */
	final public List<InterdependentProblemPart<?, ?>> getProblemParts() {
		InterdependentProblemPart<?, ?> [] array = iproblempart.values().toArray(new InterdependentProblemPart<?, ?>[0]);
		Arrays.sort(array,new ProblemPartComparator());
		return Arrays.asList(array);
	}
	
	public boolean getAndResetProblempartChangedFlag() {
		boolean tmp = reschedule;
		reschedule = false;
		return tmp;
	}

	@Override
	public UUID getUUID() {
		return getAssignedOCUnit().getUnitID();
	}
	
	
	private void getCurrentPowerOfDevices() {
		// get new power values
		Map<UUID, CommodityPowerStateExchange> powerStatesMap = 
				this.getOCRegistry().getStates(CommodityPowerStateExchange.class);
		
//		if (powerStatesMap.size() > 2) {
//			@SuppressWarnings("unused")
//			int debug = 0;
//		}
				
		commodityTotalPowerMap = new EnumMap<>(Commodity.class);
		
		if( totalPowerMeter != null ) {
			for (Entry<UUID, CommodityPowerStateExchange> e : powerStatesMap.entrySet()) {
				if (e.getKey().equals(getUUID())) {// ignore own CommodityPowerStateExchange
					continue;
				}
				if (totalPowerMeter.contains(e.getKey())) {
					updateDeviceCommodityMap(e);
				}
			}
		}  
		else {
			for (Entry<UUID, CommodityPowerStateExchange> e : powerStatesMap.entrySet()) {
				if (e.getKey().equals(getUUID())) {// ignore own CommodityPowerStateExchange
					continue;
				}
				updateDeviceCommodityMap(e);
			}
		}
		
		long now = getTimer().getUnixTime();
		
		// Export Commodity powerStates
		CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
				getUUID(), 
				now,
				DeviceTypes.OTHER);
		for (Entry<Commodity,Double> e : commodityTotalPowerMap.entrySet()) {
			cpse.addPowerState(e.getKey(), e.getValue());
		}
		
		this.getOCRegistry().setState(
				CommodityPowerStateExchange.class, 
				this, 
				cpse);

		// Export Device powerStates
		DevicesPowerStateExchange dpse = new DevicesPowerStateExchange(getUUID(), now);
		
		if( totalPowerMeter != null ) {
			for (Entry<Commodity, Double> e : commodityTotalPowerMap.entrySet()) {
				dpse.addPowerState(getUUID(), e.getKey(), e.getValue());
			}
		} 
		else { /* ( totalPowerMeter == null ) */
			for (Entry<UUID, CommodityPowerStateExchange> e : powerStatesMap.entrySet()) {
				if (!e.getKey().equals(getUUID())) {
					for (Commodity c : Commodity.values()) {
						Double value = (e.getValue()).getPowerState(c);
						if (value != null) {
							dpse.addPowerState(e.getKey(), c, value);
						}
					}
				} /* if (!e.getKey().equals(getUUID())) */
			} /* for */
		} /* if( totalPowerMeter != null ) */ 
		
		// save as state
		this.getOCRegistry().setState(
				DevicesPowerStateExchange.class,
				this,
				dpse);
		
		//
		int totalGasPower = 0;
		
		int totalHotWaterPowerProduction = 0;
		int totalHotWaterPowerConsumption = 0;
		int totalColdWaterPowerProduction = 0;
		int totalColdWaterPowerConsumption = 0;
		
		
		// save current ancillaryCommodity power states to registry (for logger and maybe more in the future)
		{
			EnumMap<AncillaryCommodity,Integer> vcMap = new EnumMap<>(AncillaryCommodity.class);
			
			HashMap<DeviceTypes,Integer> devMapActivePower = new HashMap<>();
			devMapActivePower.put(DeviceTypes.PVSYSTEM, 0);
			devMapActivePower.put(DeviceTypes.CHPPLANT, 0);
			devMapActivePower.put(DeviceTypes.ECAR, 0);
			devMapActivePower.put(DeviceTypes.OTHER, 0);
			
			// calc current ancillaryCommodity power state
			for (Entry<UUID, CommodityPowerStateExchange> e : powerStatesMap.entrySet()) {
				
				// check whether UUID is global OC unit and exclude then...
				if (e.getKey().equals(this.getUUID())) {
					continue;
				}
				
				// Active Power
				if (e.getValue().getDeviceType() == DeviceTypes.PVSYSTEM) {
					Integer pvPower = devMapActivePower.get(DeviceTypes.PVSYSTEM);
					Double addPowerDouble = e.getValue().getPowerState(Commodity.ACTIVEPOWER);
					if (addPowerDouble != null) {
						Integer additionalPower = (int) Math.round(addPowerDouble);
						pvPower = pvPower + additionalPower;
						devMapActivePower.put(DeviceTypes.PVSYSTEM, pvPower);
					}
				}
				else if (e.getValue().getDeviceType() == DeviceTypes.CHPPLANT) {
					Integer chpPower = devMapActivePower.get(DeviceTypes.CHPPLANT);
					Double addPowerDouble = e.getValue().getPowerState(Commodity.ACTIVEPOWER);
					if (addPowerDouble != null) {
						Integer additionalPower = (int) Math.round(addPowerDouble);
						chpPower = chpPower + additionalPower;
						devMapActivePower.put(DeviceTypes.CHPPLANT, chpPower);
					}
				}
				else if (e.getValue().getDeviceType() == DeviceTypes.ECAR) {
					Integer ecarPower = devMapActivePower.get(DeviceTypes.ECAR);
					Double addPowerDouble = e.getValue().getPowerState(Commodity.ACTIVEPOWER);
					if (addPowerDouble != null) {
						Integer additionalPower = (int) Math.round(addPowerDouble);
						ecarPower = ecarPower + additionalPower;
						devMapActivePower.put(DeviceTypes.ECAR, ecarPower);
					}
				}
				else {
					// all other device are OTHER for active power
					Integer otherPower = devMapActivePower.get(DeviceTypes.OTHER);
					Double addPowerDouble = e.getValue().getPowerState(Commodity.ACTIVEPOWER);
					if (addPowerDouble != null) {
						Integer additionalPower = (int) Math.round(addPowerDouble);
						otherPower = otherPower + additionalPower;
						devMapActivePower.put(DeviceTypes.OTHER, otherPower);
					}
				}
				
				// Gas power
				{
					Double gasPowerDouble = e.getValue().getPowerState(Commodity.NATURALGASPOWER);
					if (gasPowerDouble != null) {
						Integer gasPower = (int) Math.round(gasPowerDouble);
						totalGasPower = totalGasPower + gasPower;
					}
				}
				
				// Hot water power
				{
					Double hotWaterPowerDouble = e.getValue().getPowerState(Commodity.HEATINGHOTWATERPOWER);
					if (hotWaterPowerDouble != null) {
						if (hotWaterPowerDouble > 0) {
							Integer hotWaterPower = (int) Math.round(hotWaterPowerDouble);
							totalHotWaterPowerConsumption = totalHotWaterPowerConsumption + hotWaterPower;
						}
						else if (hotWaterPowerDouble < 0) {
							Integer hotWaterPower = (int) Math.round(hotWaterPowerDouble);
							totalHotWaterPowerProduction = totalHotWaterPowerProduction + hotWaterPower;
						}
					}
				}
				
				// Cold water power
				{
					Double coldWaterPowerDouble = e.getValue().getPowerState(Commodity.COLDWATERPOWER);
					if (coldWaterPowerDouble != null) {
						if (coldWaterPowerDouble > 0) {
							Integer coldWaterPower = (int) Math.round(coldWaterPowerDouble);
							totalColdWaterPowerConsumption = totalColdWaterPowerConsumption + coldWaterPower;
						}
						else if (coldWaterPowerDouble < 0) {
							Integer coldWaterPower = (int) Math.round(coldWaterPowerDouble);
							totalColdWaterPowerProduction = totalColdWaterPowerProduction + coldWaterPower;
						}
					}
				}
				
				 
			}
			
			double shareofPV = 0;
			double shareofCHP = 0;
			
			Integer pvPower = devMapActivePower.get(DeviceTypes.PVSYSTEM);
			Integer chpPower = devMapActivePower.get(DeviceTypes.CHPPLANT);
			Integer ecarPower = devMapActivePower.get(DeviceTypes.ECAR);
			Integer otherPower = devMapActivePower.get(DeviceTypes.OTHER);
			
			if (pvPower != 0 && chpPower != 0) {
				shareofPV = (double) pvPower / (pvPower + chpPower);
				shareofCHP = (double) chpPower / (pvPower + chpPower);
			}
			else if (pvPower != 0 && chpPower == 0) {
				shareofPV = 1;
			}
			else if (pvPower == 0 && chpPower != 0) {
				shareofCHP = 1;
			}
			
			if (pvPower == null) {
				pvPower = 0;
			}
			if (chpPower == null) {
				chpPower = 0;
			}
			if (ecarPower == null) {
				ecarPower = 0;
			}
			if (otherPower == null) {
				otherPower = 0;
			}
			
			int totalPower = pvPower + chpPower + ecarPower + otherPower;
			
			// net consumption
			if (totalPower >= 0) {
				vcMap.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
				vcMap.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, 0);
				vcMap.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
				vcMap.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, 0);
				vcMap.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
			}
			// net production / feed-in
			else {
				int pvExternal = (int) Math.round(shareofPV * totalPower);
				int pvInternal = pvPower - pvExternal;
				
				int chpExternal = (int) Math.round(shareofCHP * totalPower);
				int chpInternal = chpPower - chpExternal;
				
				vcMap.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvInternal);
				vcMap.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvExternal);
				vcMap.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpInternal);
				vcMap.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpExternal);
				vcMap.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
			}
			
			vcMap.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, totalGasPower);
			
			// FIXME: external...
//			vcMap.put(AncillaryCommodity.HOTWATERPOWERCONSUMPTION, totalHotWaterPowerConsumption);
//			vcMap.put(AncillaryCommodity.HOTWATERPOWERPRODUCTION, totalHotWaterPowerProduction);
//			vcMap.put(AncillaryCommodity.COLDWATERPOWERCONSUMPTION, totalColdWaterPowerConsumption);
//			vcMap.put(AncillaryCommodity.COLDWATERPOWERPRODUCTION, totalColdWaterPowerProduction);
			
			AncillaryCommodityPowerStateExchange vcpse = new AncillaryCommodityPowerStateExchange(
					getUUID(), 
					now, 
					vcMap);
			
			this.getOCRegistry().setState(
					AncillaryCommodityPowerStateExchange.class,
					this,
					vcpse);
			
			DetailedCostsLoggingStateExchange dclse = new DetailedCostsLoggingStateExchange(
					getUUID(), 
					now,
					vcMap, 
					priceSignals, 
					powerLimitSignals);
			this.getOCRegistry().setState(
					DetailedCostsLoggingStateExchange.class,
					this,
					dclse);
		}
	}
	
	private void updateDeviceCommodityMap(Entry<UUID, CommodityPowerStateExchange> e) {
		for (Commodity c : Commodity.values()) {
			Double value = (e.getValue()).getPowerState(c);
			if (value != null && value != 0) {
				EnumMap<Commodity,Double> deviceCommodityPowerMap = deviceCommodityMap.get(c);
				if (deviceCommodityPowerMap == null) {
					deviceCommodityPowerMap = new EnumMap<>(Commodity.class);
					deviceCommodityMap.put(e.getKey(), deviceCommodityPowerMap);
				}
				deviceCommodityPowerMap.put(c, value);
				
				// calculate new total power
				Double commodityTotalPower = commodityTotalPowerMap.get(c);
				if (commodityTotalPower == null) {
					commodityTotalPower = 0.0;
				}
				commodityTotalPower += value;
				commodityTotalPowerMap.put(c, commodityTotalPower);
			}
		}
	}


	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		// get current power states
		//  and also send them to logger
		getCurrentPowerOfDevices();
	}


	@Override
	public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
		return null;
	}
	
	
	// HELPER METHODS
	private List<UUID> parseUUIDArray(String parameter) throws OSHException {
		try {
			List<UUID> list = UUIDLists.parseUUIDArray(parameter);
			return list;
		} catch( IllegalArgumentException e ) {
			throw new OSHException(e);
		}
	}


}