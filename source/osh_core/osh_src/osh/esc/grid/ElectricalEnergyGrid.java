package osh.esc.grid;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import osh.configuration.grid.DevicePerMeter;
import osh.configuration.grid.GridLayout;
import osh.configuration.grid.LayoutConnection;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.UUIDCommodityMap;
import osh.esc.grid.carrier.Electrical;
import osh.utils.xml.XMLSerialization;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class ElectricalEnergyGrid implements EnergyGrid, Serializable {

	/** Serial ID */
	private static final long serialVersionUID = 420822007199349391L;

	private final Set<EnergySourceSink> sourceSinkList = new ObjectOpenHashSet<EnergySourceSink>();

	private final Set<UUID> meterUUIDs = new ObjectOpenHashSet<UUID>();

	private final List<EnergyRelation<Electrical>> relationList = new ObjectArrayList<>();
	
	private InitializedEnergyRelation[] initializedImprovedActiveToPassiveArray;
	
	private List<EnergyRelation<Electrical>> initializedPassiveToActiveRelationList = new ObjectArrayList<>();
	
	private boolean isSingular = false;
	private int singularMeter = -1;
	private int singularPvDevice = -1;
	private int singularChpDevice = -1;
	private int singularBatDevice = -1;

	
	private boolean hasBeenInitialized = false;
	private boolean hasBat = true, hasPV = true, hasCHP = true;

	private final Set<UUID> activeUUIDs = new ObjectOpenHashSet<UUID>();
	private final Set<UUID> passiveUUIDs = new ObjectOpenHashSet<UUID>();

	//not needed at the moment, but could be useful in the future
	//	private final Map<UUID, Set<UUID>> devicesPerMeter = new HashMap<UUID, Set<UUID>>();

	private final Map<UUID, Map<String, Set<UUID>>> devicesByTypePerMeter = new Object2ObjectOpenHashMap<UUID, Map<String, Set<UUID>>>();

	public ElectricalEnergyGrid(String layoutFilePath) throws JAXBException, FileNotFoundException {

		Object unmarshalled = XMLSerialization.file2Unmarshal(layoutFilePath, GridLayout.class);

		if (unmarshalled instanceof GridLayout) {
			GridLayout layout = (GridLayout) unmarshalled;			

			for (LayoutConnection conn : layout.getConnections()) {
				EnergySourceSink act = new EnergySourceSink(UUID.fromString(conn.getActiveEntityUUID()));
				EnergySourceSink pass = new EnergySourceSink(UUID.fromString(conn.getPassiveEntityUUID()));

				activeUUIDs.add(UUID.fromString(conn.getActiveEntityUUID()));
				passiveUUIDs.add(UUID.fromString(conn.getPassiveEntityUUID()));

				sourceSinkList.add(act);
				sourceSinkList.add(pass);

				relationList.add(new EnergyRelation<Electrical>(act, pass, 
						new Electrical(Commodity.fromString(conn.getActiveToPassiveCommodity())),
						new Electrical(Commodity.fromString(conn.getPassiveToActiveCommodity()))));				
			}

			for(String uuid : layout.getMeterUUIDs()) {
				meterUUIDs.add(UUID.fromString(uuid));
			}

			for (DevicePerMeter dev : layout.getDeviceMeterMap()) {

				UUID meter = UUID.fromString(dev.getMeterUUID());
				UUID device = UUID.fromString(dev.getDeviceUUID());

				//				Set<UUID> deviceSet = devicesPerMeter.get(meter);
				//				if (deviceSet == null) {
				//					deviceSet = new HashSet<UUID>();
				//					devicesPerMeter.put(meter, deviceSet);
				//				}
				//				deviceSet.add(device);

				Map<String, Set<UUID>> deviceTypeMap = devicesByTypePerMeter.get(meter);
				if (deviceTypeMap == null) {
					deviceTypeMap = new Object2ObjectOpenHashMap<String, Set<UUID>>();
					devicesByTypePerMeter.put(meter, deviceTypeMap);
				}
				Set<UUID> devicesByType = deviceTypeMap.get(dev.getDeviceType());
				if (devicesByType == null) {
					devicesByType = new ObjectOpenHashSet<UUID>();
					deviceTypeMap.put(dev.getDeviceType(), devicesByType);
				}
				devicesByType.add(device);				
			}

		} else 
			throw new IllegalArgumentException("layoutFile not instance of GridLayout-class (should not be possible)");

		//sanity check
		if (!Collections.disjoint(activeUUIDs, passiveUUIDs))
			throw new IllegalArgumentException("Same UUID is active and passive");	
	}
	
	/**
	 * only for serialisation, do not use normally
	 */
	@Deprecated
	protected ElectricalEnergyGrid() {
	}

	@Override
	public void initializeGrid(Set<UUID> allActiveNodes, Set<UUID> activeNeedsInputNodes, 
			Set<UUID> passiveNodes, Object2IntOpenHashMap<UUID> uuidToIntMap,
			Object2ObjectOpenHashMap<UUID, Commodity[]> uuidOutputMap) {
		
		
		initializedPassiveToActiveRelationList = new ObjectArrayList<EnergyRelation<Electrical>>();
		List<InitializedEnergyRelation> initializedImprovedActiveToPassiveList  = new ObjectArrayList<InitializedEnergyRelation>();
		Map<UUID, InitializedEnergyRelation> tempHelpMap = new Object2ObjectOpenHashMap<>();
		
		for (EnergyRelation<Electrical> rel : relationList) {

			UUID activeId = rel.getActiveEntity().getDeviceUuid();
			UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

			boolean activeTypeNI = activeNeedsInputNodes.contains(activeId);
			boolean activeType = allActiveNodes.contains(activeId);
			boolean passiveType = passiveNodes.contains(passiveId);
			boolean isMeter = meterUUIDs.contains(passiveId);

			//if both exist and the device really puts out the commodity add to the respective lists
			if (activeType && (passiveType || isMeter) 
					&& Arrays.stream(uuidOutputMap.get(activeId)).anyMatch(c -> c.equals(rel.getActiveToPassive().getCommodity()))) {
				InitializedEnergyRelation relNew = tempHelpMap.get(activeId);

				if (relNew == null) {
					relNew = new InitializedEnergyRelation(uuidToIntMap.getInt(activeId), new ObjectArrayList<InitializedEnergyRelationTarget>());
					tempHelpMap.put(activeId, relNew);
				}

				relNew.addEnergyTarget(new InitializedEnergyRelationTarget(uuidToIntMap.getInt(passiveId), rel.getActiveToPassive().getCommodity()));
			}
			if (activeTypeNI && passiveType)
				initializedPassiveToActiveRelationList.add(rel);
		}
		
		initializedImprovedActiveToPassiveList = new ObjectArrayList<InitializedEnergyRelation>(tempHelpMap.values());
		initializedImprovedActiveToPassiveList.forEach(e -> e.transformToArrayTargets());
		
		initializedImprovedActiveToPassiveArray = new InitializedEnergyRelation[initializedImprovedActiveToPassiveList.size()];
		initializedImprovedActiveToPassiveArray = initializedImprovedActiveToPassiveList.toArray(initializedImprovedActiveToPassiveArray);
		
		if (meterUUIDs.size() == 1) {
			UUID singularMeterUUID = meterUUIDs.iterator().next();
			singularMeter = uuidToIntMap.getInt(singularMeterUUID);
			
			if (devicesByTypePerMeter.get(singularMeterUUID).get("pv").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).count() <= 1
					&& devicesByTypePerMeter.get(singularMeterUUID).get("chp").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).count() <= 1
					&& devicesByTypePerMeter.get(singularMeterUUID).get("battery").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).count() <= 1) {
				
				isSingular = true;
				singularPvDevice = uuidToIntMap.getInt(devicesByTypePerMeter.get(singularMeterUUID).get("pv").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).findFirst().orElse(null));
				singularChpDevice = uuidToIntMap.getInt(devicesByTypePerMeter.get(singularMeterUUID).get("chp").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).findFirst().orElse(null));
				singularBatDevice = uuidToIntMap.getInt(devicesByTypePerMeter.get(singularMeterUUID).get("battery").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).findFirst().orElse(null));
				
			}
		}
		
		hasBat = false; 
		hasPV = false;
		hasCHP = false;
		
		for (UUID meter : meterUUIDs) {

			Map<String, Set<UUID>> meterDevices = devicesByTypePerMeter.get(meter);

			for (UUID pv : meterDevices.get("pv")) {
				if (allActiveNodes.contains(pv) || passiveNodes.contains(pv)) {
					hasPV = true;
					break;
				}
			}

			for (UUID chp : meterDevices.get("chp")) {
				if (allActiveNodes.contains(chp) || passiveNodes.contains(chp)) {
					hasCHP = true;
					break;
				}
			}

			for (UUID battery : meterDevices.get("battery")) {
				if (allActiveNodes.contains(battery) || passiveNodes.contains(battery)) {
					hasBat = true;
					break;
				}
			}
		}
		
		hasBeenInitialized = true;
	}
	
	@Override
	public void finalizeGrid() {
		hasBeenInitialized = false;
		initializedPassiveToActiveRelationList = null;
		
		singularMeter = -1;
		singularPvDevice = -1;
		singularChpDevice = -1;
		singularBatDevice = -1;
		
		isSingular = false;
		hasBat = true; 
		hasPV = true;
		hasCHP = true;
	}

	@Override	
	public void doCalculation(
			Map<UUID, LimitedCommodityStateMap> localCommodityStates,
			Map<UUID, LimitedCommodityStateMap> totalInputStates,
			AncillaryMeterState ancillaryMeterState) {

		for (EnergyRelation<Electrical> rel : relationList) {

			UUID activeId = rel.getActiveEntity().getDeviceUuid();
			UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

			if (localCommodityStates.containsKey(activeId) 
					|| localCommodityStates.containsKey(passiveId)) {								

				Commodity activeCommodity = rel.getActiveToPassive().getCommodity();
				Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();

				LimitedCommodityStateMap activeLocalCommodities = localCommodityStates.get(activeId);
				LimitedCommodityStateMap passiveLocalCommodities = localCommodityStates.get(passiveId);
				
				if (!activeLocalCommodities.containsCommodity(activeCommodity)) {
					continue;
				}

				// update active part...
				{
					// Active Part has no input state power					

					LimitedCommodityStateMap activeMap = totalInputStates.get(activeId);
					if (activeMap == null) {
						activeMap = new LimitedCommodityStateMap();
						totalInputStates.put(activeId, activeMap);
					}

					updateActivePart(activeMap, passiveLocalCommodities, passiveCommodity);

					// do not consider power: active part determines it's own power
				}

				// update passive part...
				{					
					LimitedCommodityStateMap passiveMap = totalInputStates.get(passiveId);
					if (passiveMap == null) {
						passiveMap = new LimitedCommodityStateMap();
						totalInputStates.put(passiveId, passiveMap);
					}

					updatePassivePart(passiveMap, activeLocalCommodities, activeCommodity);
				}				
			}			
		}		
		
		calculateMeter(localCommodityStates, totalInputStates, ancillaryMeterState);
	}

	@Override
	public void doActiveToPassiveCalculation(
			Set<UUID> passiveNodes,
			UUIDCommodityMap activeStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
		
		
		if (hasBeenInitialized) {
			doInitializedActiveToPassiveCalculation(activeStates, totalInputStates, ancillaryMeterState);
		} else {

			for (EnergyRelation<Electrical> rel : relationList) {

				UUID activeId = rel.getActiveEntity().getDeviceUuid();
				UUID passiveId = rel.getPassiveEntity().getDeviceUuid();
				
				boolean doSthActive = true;
				boolean doSthPassive = true;

				if (!hasBeenInitialized) {
					doSthActive = activeStates.containsKey(activeId);
					doSthPassive = passiveNodes.contains(passiveId);		

					if (!doSthPassive && meterUUIDs.contains(passiveId))
						doSthPassive = true;
					if (!doSthActive && meterUUIDs.contains(activeId))
						doSthActive = true;	
				}

				//if sum of types > 2 both exists and an exchange should be made
				if (doSthActive && doSthPassive) {								

					Commodity activeCommodity = rel.getActiveToPassive().getCommodity();

					LimitedCommodityStateMap activeLocalCommodities = activeStates.get(activeId);

					if (!activeLocalCommodities.containsCommodity(activeCommodity))
						continue;

					// update passive part...
					LimitedCommodityStateMap passiveMap = totalInputStates.get(passiveId);
					if (passiveMap == null) {
//						passiveMap = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
						passiveMap = new LimitedCommodityStateMap();
						totalInputStates.put(passiveId, passiveMap);
					}
					updatePassivePart(passiveMap, activeLocalCommodities, activeCommodity);
				}			
			}

			calculateMeter(activeStates, totalInputStates, ancillaryMeterState);
		}
	}

	@Override
	public void doPassiveToActiveCalculation(
			Set<UUID> activeNodes,
			UUIDCommodityMap passiveStates,
			UUIDCommodityMap totalInputStates) {

		
		if (hasBeenInitialized) {
			doInitializedPassiveToActiveCalculation(activeNodes, passiveStates, totalInputStates);
		} else {
			for (EnergyRelation<Electrical> rel : relationList) {

				UUID activeId = rel.getActiveEntity().getDeviceUuid();
				UUID passiveId = rel.getPassiveEntity().getDeviceUuid();
	
				int	activeType = activeNodes.contains(activeId) ? 2 : 0;
				int	passiveType = passiveStates.containsKey(passiveId) ? 2 : 0;			
	
				if (meterUUIDs.contains(passiveId))
					passiveType = 2;
				if (meterUUIDs.contains(activeId))
					activeType = 2;		
	
				//if sum of types > 2 both exists and an exchange should be made
				if (activeType + passiveType > 2) {								
	
					Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();
	
					LimitedCommodityStateMap passiveLocalCommodities = passiveStates.get(passiveId);
					
					if (!passiveLocalCommodities.containsCommodity(passiveCommodity))
						continue;
	
					// update active part...
	
					// Active Part has no input state power
					LimitedCommodityStateMap activeMap = totalInputStates.get(activeId);
					if (activeMap == null) {
						activeMap = new LimitedCommodityStateMap();
						totalInputStates.put(activeId, activeMap);
					}
					updateActivePart(activeMap, passiveLocalCommodities, passiveCommodity);
					// do not consider power: active part determines it's own power
				}
			}			
		}
	}
	
	private void doInitializedActiveToPassiveCalculation(
			UUIDCommodityMap activeStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
		
		for (InitializedEnergyRelation rel : initializedImprovedActiveToPassiveArray) {
			
			LimitedCommodityStateMap activeLocalCommodities = activeStates.get(rel.getSourceId());

			if (activeLocalCommodities == null) {
				continue;
			}
			
			for (InitializedEnergyRelationTarget target : rel.getTargets()) {
				
				// update passive part... (if possible
				
				if (activeLocalCommodities.containsCommodity(target.getCommodity())) {
					updatePassivePart(totalInputStates.get(target.getTargetID()), activeLocalCommodities, target.getCommodity());
				}
			}
		}
		
		calculateMeter(activeStates, totalInputStates, ancillaryMeterState);
	}
	
	private void doInitializedPassiveToActiveCalculation(
			Set<UUID> activeNodes,
			UUIDCommodityMap passiveStates,
			UUIDCommodityMap totalInputStates) {


		for (EnergyRelation<Electrical> rel : initializedPassiveToActiveRelationList) {

			UUID passiveId = rel.getPassiveEntity().getDeviceUuid();
			UUID activeId = rel.getActiveEntity().getDeviceUuid();	
			Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();
			
			LimitedCommodityStateMap passiveLocalCommodities = passiveStates.get(passiveId);

			if (passiveLocalCommodities == null) {
				continue;
			}
			
			if (!passiveLocalCommodities.containsCommodity(passiveCommodity)) {
				continue;
			}

			// update active part...

			// Active Part has no input state power
			updateActivePart(totalInputStates.get(activeId), passiveLocalCommodities, passiveCommodity);
			// do not consider power: active part determines it's own power
		}
	}

	//TODO: if at any time we will have multiple pvs/chps/batteries we have to adjust this so 
	// it does not rely on having only a single one of these devices for the sped-up methods  
	private void calculateMeter(
			UUIDCommodityMap localCommodityStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
		// calculate ancillary states

		if (ancillaryMeterState != null) {

			if (!hasBeenInitialized || !isSingular) {
				calculateMeterAll(localCommodityStates, totalInputStates, ancillaryMeterState);
			} else {
				if (!hasBat) {
					if (hasCHP && hasPV) {
						calculateMeterNoBat(localCommodityStates, totalInputStates, ancillaryMeterState);
					} else if (!hasCHP && hasPV) {
						calculateMeterNoBatNoChp(localCommodityStates, totalInputStates, ancillaryMeterState);
					} else if (!hasPV && hasCHP) {
						calculateMeterNoBatNoPV(localCommodityStates, totalInputStates, ancillaryMeterState);
					} else {
						calculateMeterNoBatNoChpNoPV(localCommodityStates, totalInputStates, ancillaryMeterState);
					}
				} else {
					calculateMeterAll(localCommodityStates, totalInputStates, ancillaryMeterState);
				}
			}
		}
	}
	
	private void calculateMeter(
			Map<UUID, LimitedCommodityStateMap> localCommodityStates,
			Map<UUID, LimitedCommodityStateMap> totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
	
		// calculate ancillary states

		for (UUID meter : meterUUIDs) {
			// ancillary ELECTRICAL calculation
			double pvPower = 0;
			double chpPower = 0;
			double batteryPower = 0;

			Map<String, Set<UUID>> meterDevices = devicesByTypePerMeter.get(meter);

			for (UUID pv : meterDevices.get("pv")) {
				LimitedCommodityStateMap pvMap = localCommodityStates.get(pv);
				pvPower += pvMap != null ? pvMap.getPower(Commodity.ACTIVEPOWER) : 0;
			}

			for (UUID chp : meterDevices.get("chp")) {
				LimitedCommodityStateMap chpMap = localCommodityStates.get(chp);
				chpPower += chpMap != null ? chpMap.getPower(Commodity.ACTIVEPOWER) : 0;
			}

			for (UUID battery : meterDevices.get("battery")) {
				LimitedCommodityStateMap batteryMap = localCommodityStates.get(battery);
				batteryPower += batteryMap != null ? batteryMap.getPower(Commodity.ACTIVEPOWER) : 0;
			}

			LimitedCommodityStateMap meterMap = totalInputStates.get(meter);

			double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);
			
			if (ancillaryMeterState == null) {
				ancillaryMeterState = new AncillaryMeterState();
			}

			// net consumption
			if (totalPower >= 0) {
				ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
				ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, batteryPower < 0 ? batteryPower : 0);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, batteryPower >= 0 ? batteryPower : 0);
				ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
			}
			// net production / feed-in
			else {
				double negBatteryPower = batteryPower < 0 ? batteryPower : 0;
				double totalProduction = pvPower + chpPower + negBatteryPower;

				double shareOfPV = pvPower < 0 ? pvPower / totalProduction : 0;
				double shareOfCHP = chpPower < 0 ? chpPower / totalProduction : 0;
				double shareOfBattery = batteryPower < 0 ? batteryPower / totalProduction : 0;


				double pvExternal = (int) Math.round(shareOfPV * totalPower);
				double pvInternal = pvPower - pvExternal;

				double chpExternal = (int) Math.round(shareOfCHP * totalPower);
				double chpInternal = chpPower - chpExternal;

				double batteryExternal = 0;
				double batteryInternal = 0;
				double batteryConsumption = 0;

				if (batteryPower < 0) {
					batteryExternal = (int) Math.round(shareOfBattery * totalPower);
					batteryInternal = batteryPower - batteryExternal;
				} else {
					batteryConsumption = batteryPower;
				}

				ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvInternal);
				ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvExternal);
				ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpInternal);
				ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpExternal);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, batteryInternal);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN, batteryExternal);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, batteryConsumption);
				ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
			}

			// Reactive Power
			if (meterMap.containsCommodity(Commodity.REACTIVEPOWER)) {
				ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
			}
		}			
	}
	
	private void calculateMeterAll(
			UUIDCommodityMap localCommodityStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
	
		// calculate ancillary states

		for (UUID meter : meterUUIDs) {
			// ancillary ELECTRICAL calculation
			double pvPower = 0;
			double chpPower = 0;
			double batteryPower = 0;

			Map<String, Set<UUID>> meterDevices = devicesByTypePerMeter.get(meter);

			for (UUID pv : meterDevices.get("pv")) {
				LimitedCommodityStateMap pvMap = localCommodityStates.get(pv);
				pvPower += pvMap != null ? pvMap.getPower(Commodity.ACTIVEPOWER) : 0;
			}

			for (UUID chp : meterDevices.get("chp")) {
				LimitedCommodityStateMap chpMap = localCommodityStates.get(chp);
				chpPower += chpMap != null ? chpMap.getPower(Commodity.ACTIVEPOWER) : 0;
			}

			for (UUID battery : meterDevices.get("battery")) {
				LimitedCommodityStateMap batteryMap = localCommodityStates.get(battery);
				batteryPower += batteryMap != null ? batteryMap.getPower(Commodity.ACTIVEPOWER) : 0;
			}

			LimitedCommodityStateMap meterMap = totalInputStates.get(meter);

			double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);

			if (ancillaryMeterState == null) {
				ancillaryMeterState = new AncillaryMeterState();
			}

			// net consumption
			if (totalPower >= 0) {
				ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
				ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, batteryPower < 0 ? batteryPower : 0);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, batteryPower >= 0 ? batteryPower : 0);
				ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
			}
			// net production / feed-in
			else {
				double negBatteryPower = batteryPower < 0 ? batteryPower : 0;
				double totalProduction = pvPower + chpPower + negBatteryPower;

				double shareOfPV = pvPower < 0 ? pvPower / totalProduction : 0;
				double shareOfCHP = chpPower < 0 ? chpPower / totalProduction : 0;
				double shareOfBattery = batteryPower < 0 ? batteryPower / totalProduction : 0;


				double pvExternal = (int) Math.round(shareOfPV * totalPower);
				double pvInternal = pvPower - pvExternal;

				double chpExternal = (int) Math.round(shareOfCHP * totalPower);
				double chpInternal = chpPower - chpExternal;

				double batteryExternal = 0;
				double batteryInternal = 0;
				double batteryConsumption = 0;

				if (batteryPower < 0) {
					batteryExternal = (int) Math.round(shareOfBattery * totalPower);
					batteryInternal = batteryPower - batteryExternal;
				} else {
					batteryConsumption = batteryPower;
				}

				ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvInternal);
				ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvExternal);
				ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpInternal);
				ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpExternal);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, batteryInternal);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN, batteryExternal);
				ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, batteryConsumption);
				ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
			}

			// Reactive Power
			if (meterMap.containsCommodity(Commodity.REACTIVEPOWER)) {
				ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
			}
		}			
	}

	private void calculateMeterNoBat(
			UUIDCommodityMap localCommodityStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {

		// ancillary ELECTRICAL calculation

		LimitedCommodityStateMap pvMap = localCommodityStates.get(singularPvDevice);
		double pvPower = pvMap != null ? pvMap.getPower(Commodity.ACTIVEPOWER) : 0;

		LimitedCommodityStateMap chpMap = localCommodityStates.get(singularChpDevice);
		double chpPower = chpMap != null ? chpMap.getPower(Commodity.ACTIVEPOWER) : 0;

		LimitedCommodityStateMap meterMap = totalInputStates.get(singularMeter);

		double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);

		ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);

		// net consumption
		if (totalPower >= 0) {
			ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
			ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
		}
		// net production / feed-in
		else {
			double totalProduction = pvPower + chpPower;

			double shareOfPV = pvPower < 0 ? pvPower / totalProduction : 0;
			double shareOfCHP = chpPower < 0 ? chpPower / totalProduction : 0;


			double pvExternal = (int) Math.round(shareOfPV * totalPower);
			double pvInternal = pvPower - pvExternal;

			double chpExternal = (int) Math.round(shareOfCHP * totalPower);
			double chpInternal = chpPower - chpExternal;

			ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvInternal);
			ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvExternal);
			ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpInternal);
			ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpExternal);
		}

		// Reactive Power
		ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
	}
	
	private void calculateMeterNoBatNoPV(
			UUIDCommodityMap localCommodityStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
		
		LimitedCommodityStateMap chpMap = localCommodityStates.get(singularChpDevice);
		double chpPower = chpMap != null ? chpMap.getPower(Commodity.ACTIVEPOWER) : 0;
		
		LimitedCommodityStateMap meterMap = totalInputStates.get(singularMeter);

		double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);

		ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
		
		if (totalPower >= 0) {
			ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
		} 
		// net production / feed-in
		else {
			ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION,  (chpPower - totalPower));
			ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, totalPower);
		}

		ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
	}
	
	private void calculateMeterNoBatNoChp(
			UUIDCommodityMap localCommodityStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {

		LimitedCommodityStateMap pvMap = localCommodityStates.get(singularPvDevice);
		double pvPower = pvMap != null ? pvMap.getPower(Commodity.ACTIVEPOWER) : 0;
		
		LimitedCommodityStateMap meterMap = totalInputStates.get(singularMeter);

		double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);

		ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
		
		if (totalPower >= 0) {
			ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
		} 
		// net production / feed-in
		else {
			ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION,  (pvPower - totalPower));
			ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN, totalPower);
		}

		ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));	
	}
	
	private void calculateMeterNoBatNoChpNoPV(
			UUIDCommodityMap localCommodityStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
		
		LimitedCommodityStateMap meterMap = totalInputStates.get(singularMeter);

		ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.ACTIVEPOWER));
		ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));	
	}

	private void updatePassivePart(
			LimitedCommodityStateMap passiveMap,
			LimitedCommodityStateMap activeMap,
			Commodity activeCommodity) {

		passiveMap.setOrAddPower(activeCommodity, activeMap.getPowerWithoutCheck(activeCommodity));
	}

	private void updateActivePart(
			LimitedCommodityStateMap activeMap,
			LimitedCommodityStateMap passiveMap,
			Commodity passiveCommodity) {
		
		activeMap.setPower(passiveCommodity, 0.0);
		
		//TODO: activate voltage exchange if it becomes relevant
	}

	@Override
	public Set<UUID> getMeterUUIDs() {
		return meterUUIDs;
	}

	@Override
	public Set<UUID> getActiveUUIDs() {
		return activeUUIDs;
	}

	@Override
	public Set<UUID> getPassiveUUIDs() {
		return passiveUUIDs;
	}
}
