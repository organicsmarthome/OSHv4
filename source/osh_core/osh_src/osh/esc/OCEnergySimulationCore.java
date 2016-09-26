package osh.esc;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import osh.configuration.system.GridConfig;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.eal.hal.exceptions.HALManagerException;
import osh.esc.exception.EnergySimulationException;
import osh.esc.grid.EnergyGrid;
import osh.esc.grid.EnergySimulationTypes;

/**
 * EnergySimulationCore
 * 
 * @author Ingo Mauser, Sebastian Kramer
 */
public class OCEnergySimulationCore extends EnergySimulationCore implements Serializable {

	/** Serial ID */
	private static final long serialVersionUID = 350474217178426943L;

	private EnergyGrid[] allGrids;
	private EnergyGrid[] thermalGrids;

	UUIDCommodityMap a2pInputStateMap;
	UUIDCommodityMap p2aInputStateMap;
	
	/**
	 * CONSTRUCTOR
	 */
	public OCEnergySimulationCore(
			List<GridConfig> grids, 
			String meterUUID) throws HALManagerException {
		super(grids, meterUUID);
		
		allGrids = new EnergyGrid[this.grids.size()];
		allGrids = this.grids.values().toArray(allGrids);
		thermalGrids = new EnergyGrid[1];
		thermalGrids[0] = this.grids.get(EnergySimulationTypes.THERMAL);
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public OCEnergySimulationCore(
			Map<EnergySimulationTypes,EnergyGrid> grids, 
			UUID meterUUID) {
		super(grids, meterUUID);
		
		allGrids = new EnergyGrid[this.grids.size()];
		allGrids = this.grids.values().toArray(allGrids);
		thermalGrids = new EnergyGrid[1];
		thermalGrids[0] = this.grids.get(EnergySimulationTypes.THERMAL);
	}
	
	/**
	 * CONSTRUCTOR for serialization, do NOT use!
	 */
	@Deprecated
	protected OCEnergySimulationCore() {
		
	}
	
	public void initializeGrids(
			Set<UUID> allActiveNodes, 
			Set<UUID> activeNeedsInputNodes, 
			Set<UUID> passiveNodes,
			Object2IntOpenHashMap<UUID> uuidToIntMap,
			Object2ObjectOpenHashMap<UUID, Commodity[]> uuidOutputMap,
			Object2ObjectOpenHashMap<UUID, Commodity[]> uuidInputMap) {
		
		Object2IntOpenHashMap<UUID> uuidToIntMapWithMeter = new Object2IntOpenHashMap<UUID>(uuidToIntMap);
		uuidToIntMapWithMeter.put(meterUUID, uuidToIntMap.size());
		
		Object2ObjectOpenHashMap<UUID, Commodity[]> uuidOutputMapWithMeter = new Object2ObjectOpenHashMap<UUID, Commodity[]>(uuidOutputMap);
		uuidOutputMapWithMeter.put(meterUUID, Commodity.values());
		
		Object2ObjectOpenHashMap<UUID, Commodity[]> uuidInputMapWithMeter = new Object2ObjectOpenHashMap<UUID, Commodity[]>(uuidInputMap);
		uuidInputMapWithMeter.put(meterUUID, Commodity.values());
		
		
		for (Entry<EnergySimulationTypes,EnergyGrid> grid : grids.entrySet()) {
			grid.getValue().initializeGrid(allActiveNodes, activeNeedsInputNodes, passiveNodes, uuidToIntMapWithMeter, uuidOutputMapWithMeter);
		}
		
		ObjectOpenHashSet<UUID> passiveWithMeter = new ObjectOpenHashSet<UUID>(passiveNodes);
		passiveWithMeter.add(meterUUID);
		
		a2pInputStateMap = new UUIDCommodityMap(passiveWithMeter, uuidToIntMapWithMeter, uuidInputMapWithMeter, true);
		
		p2aInputStateMap = new UUIDCommodityMap(activeNeedsInputNodes, uuidToIntMap, uuidInputMap, true);
	}
	
	public void finalizeGrids() {
		for (Entry<EnergySimulationTypes,EnergyGrid> grid : grids.entrySet()) {
			grid.getValue().finalizeGrid();
		}
	}
	
	private UUIDCommodityMap getA2PInputStateMap() {
		
		a2pInputStateMap.clearInnerStates();
		return a2pInputStateMap;
		
	}
	
	private UUIDCommodityMap getP2AInputStateMap() {

		p2aInputStateMap.clearInnerStates();
		return p2aInputStateMap;
	}

	public void doActiveToPassiveExchange(
			UUIDCommodityMap activeCommodityStates,
			InterdependentProblemPart<?, ?>[] passiveParts,
			Set<UUID> passiveUUIDs,
			AncillaryMeterState ancillaryMeterState) throws EnergySimulationException {

		// input states
		UUIDCommodityMap totalInputStates = getA2PInputStateMap();

		// ancillary commodities input states
		ancillaryMeterState.clear();

		for (EnergyGrid grid : allGrids) {
			grid.doActiveToPassiveCalculation(
					passiveUUIDs, 
					activeCommodityStates, 
					totalInputStates, 
					ancillaryMeterState);
		}
		
		// inform subjects about states
		for (InterdependentProblemPart<?, ?> _simSubject : passiveParts) {
			
			AncillaryMeterState clonedAncillaryMeterState = null;
			LimitedCommodityStateMap simSubjState = null;
			
			if (_simSubject.isReactsToInputStates()) {
				simSubjState = totalInputStates.get(_simSubject.getId());
			}
			// clone ancillaryMeter if needed
			if (_simSubject.isNeedsAncillaryMeterState()) {					
				clonedAncillaryMeterState = ancillaryMeterState.clone();
			}

			_simSubject.setCommodityInputStates(simSubjState, clonedAncillaryMeterState);
		}
	}
	
	public void doPassiveToActiveExchange(
			AncillaryMeterState ancillaryMeterState,
			InterdependentProblemPart<?, ?>[] activeParts,
			Set<UUID> activeNodes, 
			UUIDCommodityMap passiveStates) throws EnergySimulationException {

		// input states
		UUIDCommodityMap totalInputStates = getP2AInputStateMap();
		
		//dont do calculation for electrical grid atm, because only voltage is exchanged which has no influence
		//TODO: as soon as voltage etc. becomes important uncomment first row
//		for (EnergyGrid grid : allGrids) {
		for (EnergyGrid grid : thermalGrids) {
			grid.doPassiveToActiveCalculation(
					activeNodes, 
					passiveStates, 
					totalInputStates);
		}
		
		
		// inform subjects about states
		for (InterdependentProblemPart<?, ?> _simSubject : activeParts) {
			LimitedCommodityStateMap simSubjState = null;
			AncillaryMeterState clonedAncillaryMeterState = null;
			
			if (_simSubject.isReactsToInputStates()) {
				simSubjState = totalInputStates.get(_simSubject.getId());
			}
			
			// clone AncillaryMeter AncillaryCommodities if needed
			if (_simSubject.isNeedsAncillaryMeterState()) {
				clonedAncillaryMeterState = ancillaryMeterState.clone();
			}

			_simSubject.setCommodityInputStates(simSubjState, clonedAncillaryMeterState);
		}		
	}	

	public void splitActivePassive(
			Set<UUID> allParts,
			Set<UUID> activeParts,
			Set<UUID> passiveParts) {
		Set<UUID> allActive = new HashSet<UUID>();
		Set<UUID> allPassive = new HashSet<UUID>();

		for (EnergyGrid grid : grids.values()) {
			allActive.addAll(grid.getActiveUUIDs());
			allPassive.addAll(grid.getPassiveUUIDs());
		}

		// sanity check
		if (!Collections.disjoint(allActive, allPassive))
			throw new IllegalArgumentException("Some Parts are categorized as active and passive");

		activeParts.addAll(allParts);
		passiveParts.addAll(allParts);

		activeParts.removeAll(allPassive);
		passiveParts.removeAll(allActive);

		allParts.removeAll(allActive);
		allParts.removeAll(allPassive);
		
		// sanity check
		if (!allParts.isEmpty())
			throw new IllegalArgumentException("Some Parts could not be categorized as active or passive");
	}
}
