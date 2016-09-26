package osh.esc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;

public class UUIDCommodityMap implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2823226803772827321L;

	private Object2IntOpenHashMap<UUID> keyMap;
	
//	private EnumMap<Commodity, RealCommodityState>[] innerValues;
	private LimitedCommodityStateMap[] innerValues;
	
	private int[] partIdToArrayIdMap;
	
	
	public UUIDCommodityMap(InterdependentProblemPart<?, ?>[] allParts, 
			Object2IntOpenHashMap<UUID> uuidIntMap) {
		initiateFromArrayWithMap(allParts, uuidIntMap);
	}
	
	private void initiateFromArrayWithMap(InterdependentProblemPart<?, ?>[] allParts, 
			Object2IntOpenHashMap<UUID> uuidIntMap) {
		keyMap = new Object2IntOpenHashMap<UUID>(allParts.length);
		partIdToArrayIdMap = new int[uuidIntMap.size()];
		innerValues = new LimitedCommodityStateMap[allParts.length];
		
		keyMap.defaultReturnValue(-1);
		Arrays.fill(partIdToArrayIdMap, -1);
		
		for (int i = 0; i < allParts.length; i++) {
			if (uuidIntMap.containsKey(allParts[i].getDeviceID())) {
				keyMap.put(allParts[i].getDeviceID(), i);
				partIdToArrayIdMap[allParts[i].getId()] = i;
				innerValues[i] = new LimitedCommodityStateMap();
			} else {
				throw new IllegalArgumentException("no mapping for specified key");
			}
		}
	}
	
	public UUIDCommodityMap(InterdependentProblemPart<?, ?>[] allParts, 
			Object2IntOpenHashMap<UUID> uuidIntMap,
			boolean makeNoMap) {
		
		if (!makeNoMap) {
			initiateFromArrayWithMap(allParts, uuidIntMap);
		}
		keyMap = null;
		partIdToArrayIdMap = new int[uuidIntMap.size()];
		innerValues = new LimitedCommodityStateMap[allParts.length];
		
		Arrays.fill(partIdToArrayIdMap, -1);
		
		for (int i = 0; i < allParts.length; i++) {
			if (uuidIntMap.containsKey(allParts[i].getDeviceID())) {
				partIdToArrayIdMap[allParts[i].getId()] = i;
				innerValues[i] = new LimitedCommodityStateMap();
			} else {
				throw new IllegalArgumentException("no mapping for specified key");
			}
		}
	}
	
	public UUIDCommodityMap(Set<UUID> allUUIDs, 
			Object2IntOpenHashMap<UUID> uuidIntMap,
			Object2ObjectOpenHashMap<UUID, Commodity[]> uuidInputMap,
			boolean makeNoMap) {
		keyMap = null;
		partIdToArrayIdMap = new int[uuidIntMap.size()];
		innerValues = new LimitedCommodityStateMap[allUUIDs.size()];
		
		Arrays.fill(partIdToArrayIdMap, -1);
		
		Iterator<UUID> it = allUUIDs.iterator();
		for (int i = 0; i < allUUIDs.size(); i++) {
			UUID curr = it.next();
			if (uuidIntMap.containsKey(curr)) {
				partIdToArrayIdMap[uuidIntMap.getInt(curr)] = i;
				innerValues[i] = new LimitedCommodityStateMap(uuidInputMap.get(curr));
			} else {
				throw new IllegalArgumentException("no mapping for specified key");
			}
		}
	}
	
	public UUIDCommodityMap(List<InterdependentProblemPart<?, ?>> allParts, Object2IntOpenHashMap<UUID> uuidIntMap) {
		keyMap = new Object2IntOpenHashMap<UUID>(allParts.size());
		partIdToArrayIdMap = new int[uuidIntMap.size()];
		innerValues = new LimitedCommodityStateMap[allParts.size()];
		
		keyMap.defaultReturnValue(-1);
		Arrays.fill(partIdToArrayIdMap, -1);
		
		for (int i = 0; i < allParts.size(); i++) {
			if (uuidIntMap.containsKey(allParts.get(i).getDeviceID())) {
				keyMap.put(allParts.get(i).getDeviceID(), i);
				partIdToArrayIdMap[uuidIntMap.getInt(allParts.get(i).getDeviceID())] = i;
			} else {
				throw new IllegalArgumentException("no mapping for specified key");
			}
		}
		
		for (int i = 0; i < innerValues.length; i++) {
			innerValues[i] = new LimitedCommodityStateMap();
		}
	}
	
	public UUIDCommodityMap(Set<UUID> allUUIDs, Object2IntOpenHashMap<UUID> uuidIntMap) {
		keyMap = new Object2IntOpenHashMap<UUID>(allUUIDs.size());
		partIdToArrayIdMap = new int[uuidIntMap.size()];
		innerValues = new LimitedCommodityStateMap[allUUIDs.size()];
		
		keyMap.defaultReturnValue(-1);
		Arrays.fill(partIdToArrayIdMap, -1);
		
		Iterator<UUID> it = allUUIDs.iterator();
		for (int i = 0; i < allUUIDs.size(); i++) {
			UUID curr = it.next();
			if (uuidIntMap.containsKey(curr)) {
				keyMap.put(curr, i);
				partIdToArrayIdMap[uuidIntMap.getInt(curr)] = i;
			} else {
				throw new IllegalArgumentException("no mapping for specified key");
			}
		}
		
		for (int i = 0; i < innerValues.length; i++) {
			innerValues[i] = new LimitedCommodityStateMap();
		}
	}
	
	/** for serialisation only - do not use */
	@Deprecated
	protected UUIDCommodityMap() {
		
	}
	
	public void put(int id, LimitedCommodityStateMap stateMap) {
		innerValues[partIdToArrayIdMap[id]] = stateMap;
	}
	
	public void put(UUID uuid, LimitedCommodityStateMap stateMap) {
		innerValues[keyMap.getInt(uuid)] = stateMap;
	}
	
	public LimitedCommodityStateMap get(int id) {
		return innerValues[partIdToArrayIdMap[id]];
	}
	
	public LimitedCommodityStateMap get(UUID uuid) {
		return innerValues[keyMap.getInt(uuid)];
	}
	
	public double getPower(int id, Commodity commodity) {
		return innerValues[partIdToArrayIdMap[id]].getPower(commodity);
	}
	
	public void setPower(int id, Commodity commodity, double power) {
		innerValues[partIdToArrayIdMap[id]].setPower(commodity, power);
	}
	
	public double getTemperature(int id, Commodity commodity) {
		return innerValues[partIdToArrayIdMap[id]].getTemperature(commodity);
	}
	
	public void setTemperature(int id, Commodity commodity, double temperature) {
		innerValues[partIdToArrayIdMap[id]].setTemperature(commodity, temperature);
	}
	
	public void clearInnerStates() {
		for (LimitedCommodityStateMap map : innerValues) {
			if (map != null) {
				map.clear();
			}
		}
	}
	
	public boolean containsKey(UUID uuid) {
		return keyMap.containsKey(uuid);
	}
}
