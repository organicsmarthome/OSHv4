package osh.datatypes.registry.oc.details.utility;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class PlsStateExchange extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7026114115887147889L;
	private EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals;
	
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public PlsStateExchange(UUID sender, long timestamp) {
		super(sender, timestamp);
		
		this.powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);
	}
	
	public void setPowerLimitSignals(EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals) {
		this.powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);
		
		for (Entry<AncillaryCommodity,PowerLimitSignal> e : powerLimitSignals.entrySet()) {
			this.powerLimitSignals.put(e.getKey(), e.getValue().clone());
		}
	}
	
	
	public void setPowerLimitSignal(AncillaryCommodity vc, PowerLimitSignal powerLimitSignal) {
		PowerLimitSignal copy = powerLimitSignal.clone();
		this.powerLimitSignals.put(vc, copy);
	}


	public EnumMap<AncillaryCommodity, PowerLimitSignal> getPowerLimitSignals() {
		return powerLimitSignals;
	}
	
	@Override
	public PlsStateExchange clone() {
		PlsStateExchange clonedX = new PlsStateExchange(getSender(), getTimestamp());
		clonedX.setPowerLimitSignals(this.getPowerLimitSignals());
		return clonedX;
	}
}