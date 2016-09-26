package osh.esc.grid;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import osh.configuration.grid.GridLayout;
import osh.configuration.grid.LayoutConnection;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.UUIDCommodityMap;
import osh.esc.grid.carrier.Thermal;
import osh.utils.xml.XMLSerialization;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class ThermalEnergyGrid implements EnergyGrid, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6032713739337800610L;

	private final Set<EnergySourceSink> sourceSinkList = new HashSet<EnergySourceSink>();

	private final Set<UUID> meterUUIDs = new ObjectOpenHashSet<UUID>();

	private final List<EnergyRelation<Thermal>> relationList = new ObjectArrayList<>();

	private InitializedEnergyRelation[] initializedImprovedActiveToPassiveArray;
	private InitializedEnergyRelation[] initializedImprovedPassiveToActiveArray;


	private boolean hasBeenInitialized = false;
	private boolean isSingular = false;
	private int singularMeter = -1;

	private final Set<UUID> activeUUIDs = new ObjectOpenHashSet<UUID>();
	private final Set<UUID> passiveUUIDs = new ObjectOpenHashSet<UUID>();

	public ThermalEnergyGrid(String layoutFilePath) throws JAXBException, FileNotFoundException {

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

				relationList.add(new EnergyRelation<Thermal>(act, pass, 
						new Thermal(Commodity.fromString(conn.getActiveToPassiveCommodity())),
						new Thermal(Commodity.fromString(conn.getPassiveToActiveCommodity()))));				
			}

			for(String uuid : layout.getMeterUUIDs()) {
				meterUUIDs.add(UUID.fromString(uuid));
			}

		} else 
			throw new IllegalArgumentException("layoutFile not instance of GridLayout-class (should not be possible)");

		//sanity
		if (!Collections.disjoint(activeUUIDs, passiveUUIDs))
			throw new IllegalArgumentException("Same UUID is active and passive");	
	}

	/**
	 * only for serialisation, do not use normally
	 */
	@Deprecated
	protected ThermalEnergyGrid() {
		// NOTHING
	}

	@Override
	public void initializeGrid(Set<UUID> allActiveNodes, Set<UUID> activeNeedsInputNodes, 
			Set<UUID> passiveNodes, Object2IntOpenHashMap<UUID> uuidToIntMap,
			Object2ObjectOpenHashMap<UUID, Commodity[]> uuidOutputMap) {

		List<InitializedEnergyRelation> initializedImprovedActiveToPassiveList = new ObjectArrayList<InitializedEnergyRelation>();
		List<InitializedEnergyRelation> initializedImprovedPassiveToActiveList = new ObjectArrayList<InitializedEnergyRelation>();
		Map<UUID, InitializedEnergyRelation> tempA2PHelpMap = new Object2ObjectOpenHashMap<>();
		Map<UUID, InitializedEnergyRelation> tempP2AHelpMap = new Object2ObjectOpenHashMap<>();

		for (EnergyRelation<Thermal> rel : relationList) {

			UUID activeId = rel.getActiveEntity().getDeviceUuid();
			UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

			boolean activeTypeNI = activeNeedsInputNodes.contains(activeId);
			boolean activeType = allActiveNodes.contains(activeId);
			boolean passiveType = passiveNodes.contains(passiveId);
			boolean isMeter = meterUUIDs.contains(passiveId);

			//if both exist and an exchange should be made add to the respective lists
			if (activeType && (passiveType || isMeter)
					&& Arrays.stream(uuidOutputMap.get(activeId)).anyMatch(c -> c.equals(rel.getActiveToPassive().getCommodity()))) {

				InitializedEnergyRelation relNew = tempA2PHelpMap.get(activeId);

				if (relNew == null) {
					relNew = new InitializedEnergyRelation(uuidToIntMap.getInt(activeId), new ObjectArrayList<InitializedEnergyRelationTarget>());
					tempA2PHelpMap.put(activeId, relNew);
				}

				relNew.addEnergyTarget(new InitializedEnergyRelationTarget(uuidToIntMap.getInt(passiveId), rel.getActiveToPassive().getCommodity()));
			}
			if (activeTypeNI && passiveType
					&& Arrays.stream(uuidOutputMap.get(passiveId)).anyMatch(c -> c.equals(rel.getPassiveToActive().getCommodity()))) {

				InitializedEnergyRelation relNew = tempP2AHelpMap.get(passiveId);

				if (relNew == null) {
					relNew = new InitializedEnergyRelation(uuidToIntMap.getInt(passiveId), new ObjectArrayList<InitializedEnergyRelationTarget>());
					tempP2AHelpMap.put(passiveId, relNew);
				}

				relNew.addEnergyTarget(new InitializedEnergyRelationTarget(uuidToIntMap.getInt(activeId), rel.getPassiveToActive().getCommodity()));
			}
		}

		initializedImprovedActiveToPassiveList.addAll(tempA2PHelpMap.values());
		initializedImprovedActiveToPassiveList.forEach(e -> e.transformToArrayTargets());

		initializedImprovedActiveToPassiveArray = new InitializedEnergyRelation[initializedImprovedActiveToPassiveList.size()];
		initializedImprovedActiveToPassiveArray = initializedImprovedActiveToPassiveList.toArray(initializedImprovedActiveToPassiveArray);

		initializedImprovedPassiveToActiveList.addAll(tempP2AHelpMap.values());
		initializedImprovedPassiveToActiveList.forEach(e -> e.transformToArrayTargets());

		initializedImprovedPassiveToActiveArray = new InitializedEnergyRelation[initializedImprovedPassiveToActiveList.size()];
		initializedImprovedPassiveToActiveArray = initializedImprovedPassiveToActiveList.toArray(initializedImprovedPassiveToActiveArray);

		hasBeenInitialized = true;

		if (meterUUIDs.size() == 1) {
			isSingular = true;
			singularMeter = uuidToIntMap.getInt(meterUUIDs.iterator().next());
		}
	}

	@Override
	public void finalizeGrid() {
		hasBeenInitialized = false;

		isSingular = false;
		singularMeter = -1;
	}

	@Override
	public void doCalculation(
			Map<UUID, LimitedCommodityStateMap> localCommodityStates,
			Map<UUID, LimitedCommodityStateMap> totalInputStates,
			AncillaryMeterState ancillaryMeterState) {

		for (EnergyRelation<Thermal> rel : relationList) {

			UUID activeId = rel.getActiveEntity().getDeviceUuid();
			UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

			if (localCommodityStates.containsKey(activeId) 
					|| localCommodityStates.containsKey(passiveId)) {								

				Commodity activeCommodity = rel.getActiveToPassive().getCommodity();
				Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();

				LimitedCommodityStateMap activeLocalCommodities = localCommodityStates.get(activeId);
				LimitedCommodityStateMap passiveLocalCommodities = localCommodityStates.get(passiveId);

				boolean hasActive = activeLocalCommodities != null ? activeLocalCommodities.containsCommodity(activeCommodity) : false;
				boolean hasPassive = passiveLocalCommodities != null ? passiveLocalCommodities.containsCommodity(passiveCommodity) : false;

				if (!hasActive && !hasPassive) {
					continue;
				}

				// update active part...
				if (hasPassive){
					// Active Part has no input state power					

					LimitedCommodityStateMap activeMap = totalInputStates.get(activeId);
					if (activeMap == null) {
						activeMap = new LimitedCommodityStateMap();
						totalInputStates.put(activeId, activeMap);
					}

					updateActivePart(activeMap, passiveLocalCommodities, passiveCommodity);
					//TODO mass flow


					// do not consider power: active part determines it's own power
				}

				// update passive part...
				if (hasActive){		
					LimitedCommodityStateMap passiveMap = totalInputStates.get(passiveId);
					if (passiveMap == null) {
						passiveMap = new LimitedCommodityStateMap();
						totalInputStates.put(passiveId, passiveMap);
					}

					updatePassivePart(passiveMap, activeLocalCommodities, activeCommodity);	
				}				
			}			
		}
		// calculate ancillary states		
		calculateMeter(totalInputStates, ancillaryMeterState);
	}

	private void calculateMeter(
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
		if (hasBeenInitialized && isSingular) {
			calculateInitializedMeter(totalInputStates, ancillaryMeterState);
		} else {
			calculateMeterAll(totalInputStates, ancillaryMeterState);
		}
	}

	private void calculateInitializedMeter(
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
		if (ancillaryMeterState != null) {
			// ancillary THERMAL calculation
			ancillaryMeterState.setPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL, totalInputStates.get(singularMeter).getPower(Commodity.NATURALGASPOWER));
		}
	}

	private void calculateMeter(
			Map<UUID, LimitedCommodityStateMap> totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
		if (ancillaryMeterState != null) {
			for (UUID meter : meterUUIDs) {
				// ancillary THERMAL calculation
				LimitedCommodityStateMap calculatedMeterState = totalInputStates.get(meter);

				if (ancillaryMeterState == null) {
					ancillaryMeterState = new AncillaryMeterState();
				}

				// Gas Power
				{
					if (calculatedMeterState == null){
//						System.out.println("Probably no heating device in configuration!");
					} else {
						ancillaryMeterState.setPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL, 
								calculatedMeterState.getPower(Commodity.NATURALGASPOWER));
					}	
				}
			}
		}
	}

	private void calculateMeterAll(
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {
		if (ancillaryMeterState != null) {
			for (UUID meter : meterUUIDs) {
				// ancillary THERMAL calculation
				LimitedCommodityStateMap calculatedMeterState = totalInputStates.get(meter);

				if (ancillaryMeterState == null) {
					ancillaryMeterState = new AncillaryMeterState();
				}

				// Gas Power
				{
					if (calculatedMeterState == null){
//						System.out.println("Probably no heating device in configuration!");
					} else if (calculatedMeterState.containsCommodity(Commodity.NATURALGASPOWER)){

						ancillaryMeterState.setPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL, 
								calculatedMeterState.getPower(Commodity.NATURALGASPOWER));
					}		
				}
			}
		}
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

	@Override
	public void doActiveToPassiveCalculation(
			Set<UUID> passiveNodes,
			UUIDCommodityMap activeStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {

		if (hasBeenInitialized) {
			doInitializedActiveToPassiveGridCalculation(passiveNodes, activeStates, totalInputStates, ancillaryMeterState);
		} else {

			for (EnergyRelation<Thermal> rel : relationList) {

				UUID activeId = rel.getActiveEntity().getDeviceUuid();
				UUID passiveId = rel.getPassiveEntity().getDeviceUuid();			

				int	activeType = 2;
				int	passiveType = 2;

				if (!hasBeenInitialized) {
					activeType = activeStates.containsKey(activeId) ? 2 : 0;
					passiveType = passiveNodes.contains(passiveId) ? 2 : 0;			

					if (meterUUIDs.contains(passiveId))
						passiveType = 2;
					if (meterUUIDs.contains(activeId))
						activeType = 2;	
				}

				//if sum of types > 2 both exists and an exchange should be made
				if (activeType + passiveType > 2) {								

					Commodity activeCommodity = rel.getActiveToPassive().getCommodity();

					LimitedCommodityStateMap activeLocalCommodities = activeStates.get(activeId);

					if (activeLocalCommodities == null || !activeLocalCommodities.containsCommodity(activeCommodity)) {
						continue;
					}

					// update passive part...
					LimitedCommodityStateMap passiveMap = totalInputStates.get(passiveId);
					if (passiveMap == null) {
						passiveMap = new LimitedCommodityStateMap();
						totalInputStates.put(passiveId, passiveMap);
					}
					updatePassivePart(passiveMap, activeLocalCommodities, activeCommodity);
				}			
			}

			calculateMeter(totalInputStates, ancillaryMeterState);
		}		
	}

	private void doInitializedActiveToPassiveGridCalculation(
			Set<UUID> passiveNodes,
			UUIDCommodityMap activeStates,
			UUIDCommodityMap totalInputStates,
			AncillaryMeterState ancillaryMeterState) {

		for (InitializedEnergyRelation rel : initializedImprovedActiveToPassiveArray) {

			LimitedCommodityStateMap activeLocalCommodities = activeStates.get(rel.getSourceId());

			if (activeLocalCommodities != null) {


				for (InitializedEnergyRelationTarget target : rel.getTargets()) {

					// update passive part...
					if (activeLocalCommodities.containsCommodity(target.getCommodity())) {
						updatePassivePart(totalInputStates.get(target.getTargetID()), activeLocalCommodities, target.getCommodity());
					}
				}
			}
		}
		calculateInitializedMeter(totalInputStates, ancillaryMeterState);
	}

	@Override
	public void doPassiveToActiveCalculation(
			Set<UUID> activeNodes, 
			UUIDCommodityMap passiveStates,
			UUIDCommodityMap totalInputStates) {

		if (hasBeenInitialized) {
			doInitializedPassiveToActiveGridCalculation(passiveStates, activeNodes, totalInputStates);
		} else {

			for (EnergyRelation<Thermal> rel : relationList) {

				UUID activeId = rel.getActiveEntity().getDeviceUuid();
				UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

				int activeType = activeNodes.contains(activeId) ? 2 : 0;
				int passiveType = passiveStates.containsKey(passiveId) ? 2 : 0;			

				if (meterUUIDs.contains(passiveId))
					passiveType = 2;
				if (meterUUIDs.contains(activeId))
					activeType = 2;	

				//if sum of types > 2 both exists and an exchange should be made
				if (activeType + passiveType > 2) {								

					Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();

					LimitedCommodityStateMap passiveLocalCommodities = passiveStates.get(passiveId);

					if (passiveLocalCommodities == null 
							|| !passiveLocalCommodities.containsCommodity(passiveCommodity)) {
						continue;
					}

					// update active part...

					// Active Part has no input state power
					LimitedCommodityStateMap activeMap = totalInputStates.get(activeId);
					if (activeMap == null) {
						activeMap = new LimitedCommodityStateMap();
						totalInputStates.put(activeId, activeMap);
					}
					updateActivePart(activeMap, passiveLocalCommodities, passiveCommodity);
					//TODO mass flow
					// do not consider power: active part determines it's own power
				}			
			}	
		}	
	}	

	private void doInitializedPassiveToActiveGridCalculation(
			UUIDCommodityMap passiveStates,
			Set<UUID> activeNodes, 
			UUIDCommodityMap totalInputStates) {

		for (InitializedEnergyRelation rel : initializedImprovedPassiveToActiveArray) {

			LimitedCommodityStateMap passiveMap = passiveStates.get(rel.getSourceId());

			if (passiveMap != null) {


				for (InitializedEnergyRelationTarget target : rel.getTargets()) {

					// update passive part...
					if (passiveMap.containsCommodity(target.getCommodity())) {
						updateActivePart(totalInputStates.get(target.getTargetID()), passiveMap, target.getCommodity());
					}
				}
			}
		}
	}	

	private void updatePassivePart(
			LimitedCommodityStateMap passiveMap,
			LimitedCommodityStateMap activeMap,
			Commodity activeCommodity) {
		passiveMap.setOrAddPower(activeCommodity, activeMap.getPowerWithoutCheck(activeCommodity));
		passiveMap.setTemperature(activeCommodity, activeMap.getTemperatureWithoutCheck(activeCommodity));
	}

	private void updateActivePart(
			LimitedCommodityStateMap activeMap,
			LimitedCommodityStateMap passiveMap,
			Commodity passiveCommodity) {

		activeMap.setTemperature(passiveCommodity, passiveMap.getTemperatureWithoutCheck(passiveCommodity));
	}
}
