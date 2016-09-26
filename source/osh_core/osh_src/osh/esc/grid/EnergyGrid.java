package osh.esc.grid;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.UUIDCommodityMap;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public interface EnergyGrid {
	
	// optimization, speeds up grid calculation by considering only relations
	// for every future calculation for the given UUIDs
	/**
	 * Initialize grid by loading all relations into lists
	 */
	public void initializeGrid(Set<UUID> allActiveNodes, Set<UUID> activeNeedsInputNodes, 
			Set<UUID> passiveNodes, Object2IntOpenHashMap<UUID> uuidToIntMap, Object2ObjectOpenHashMap<UUID, Commodity[]> uuidOutputMap);
	
	/**
	 * Finalize grid by unloading all relations
	 */
	public void finalizeGrid();
	
	/**
	 * Simulation: <br>
	 * Do grid calculation and update states
	 */
	public void doCalculation(
			Map<UUID, LimitedCommodityStateMap> commodityStates,
			Map<UUID, LimitedCommodityStateMap> totalInputStates,
//			UUIDEnumMap totalInputStates,
//			EnumMap<AncillaryCommodity, AncillaryCommodityState> totalAncillaryInputStates);
			AncillaryMeterState ancillaryMeterState);
	
	/**
	 * O/C-Simulation: <br>
	 * Do active to passive part update
	 */
	public void doActiveToPassiveCalculation(
			Set<UUID> passiveNodes,
//			Map<UUID, EnumMap<Commodity, RealCommodityState>> activeStates,
			UUIDCommodityMap activeStates,
			UUIDCommodityMap totalInputStates,
//			EnumMap<AncillaryCommodity, AncillaryCommodityState> totalAncillaryInputStates);
			AncillaryMeterState ancillaryMeterState);
	
	/**
	 * O/C-Simulation: <br>
	 * Do passive to active part update
	 */
	public void doPassiveToActiveCalculation(
			Set<UUID> activeNodes,
//			Map<UUID, EnumMap<Commodity, RealCommodityState>> passiveStates,
			UUIDCommodityMap passiveStates,
			UUIDCommodityMap totalInputStates);
	
	/**
	 * Get virtual meters
	 * @return Virtual meters
	 */
	public Set<UUID> getMeterUUIDs();
	
	/**
	 * Get UUIDs of active IPPs
	 * @return UUIDs of active IPPs
	 */
	public Set<UUID> getActiveUUIDs();
	
	/**
	 * Get UUIDs of passive IPPs
	 * @return UUIDs of passive IPPs
	 */
	public Set<UUID> getPassiveUUIDs();

}
