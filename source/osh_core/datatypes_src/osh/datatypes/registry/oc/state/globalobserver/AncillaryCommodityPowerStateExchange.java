package osh.datatypes.registry.oc.state.globalobserver;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.registry.StateExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class AncillaryCommodityPowerStateExchange extends StateExchange {
	
	private static final long serialVersionUID = 7876809071418906992L;
	private EnumMap<AncillaryCommodity,Integer> map;

	
	/**
	 * CONSTRUCTOR
	 */
	public AncillaryCommodityPowerStateExchange(UUID sender, long timestamp, EnumMap<AncillaryCommodity,Integer> map) {
		super(sender, timestamp);

		this.map = map;
	}
	
	
	public EnumMap<AncillaryCommodity, Integer> getMap() {
		return map;
	}


	@Override
	public StateExchange clone() {
		EnumMap<AncillaryCommodity,Integer> clonedMap = new EnumMap<>(AncillaryCommodity.class);
		
		for (Entry<AncillaryCommodity,Integer> e : this.map.entrySet()) {
			clonedMap.put(e.getKey(), e.getValue());
		}
		
		AncillaryCommodityPowerStateExchange cloned = new AncillaryCommodityPowerStateExchange(
				getSender(), 
				getTimestamp(), 
				clonedMap);
		
		return cloned;
	}
}
