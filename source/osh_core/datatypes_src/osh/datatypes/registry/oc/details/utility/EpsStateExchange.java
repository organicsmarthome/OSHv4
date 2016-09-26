package osh.datatypes.registry.oc.details.utility;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class EpsStateExchange extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4956166786914687416L;
	private EnumMap<AncillaryCommodity,PriceSignal> priceSignals;
	private boolean causeScheduling = false;
	
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 */
	public EpsStateExchange(UUID sender, long timestamp) {
		super(sender, timestamp);
		
		priceSignals = new EnumMap<>(AncillaryCommodity.class);
	}
	
	public EpsStateExchange(UUID sender, long timestamp, boolean causeScheduling) {
		super(sender, timestamp);
		
		priceSignals = new EnumMap<>(AncillaryCommodity.class);
		this.causeScheduling = causeScheduling;
	}
	
	public void setPriceSignals(EnumMap<AncillaryCommodity,PriceSignal> priceSignals) {
		this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
		
		for (Entry<AncillaryCommodity,PriceSignal> e : priceSignals.entrySet()) {
			this.priceSignals.put(e.getKey(), e.getValue().clone());
		}
	}
	
	
	public void setPriceSignal(AncillaryCommodity vc, PriceSignal priceSignal) {
		PriceSignal copy = priceSignal.clone();
		priceSignals.put(vc, copy);
	}


	public EnumMap<AncillaryCommodity, PriceSignal> getPriceSignals() {
		return priceSignals;
	}
	
	public boolean causeScheduling() {
		return causeScheduling;
	}
	
	@Override
	public EpsStateExchange clone() {
		EpsStateExchange clonedX = new EpsStateExchange(getSender(), getTimestamp());
		clonedX.setPriceSignals(this.getPriceSignals());
		clonedX.causeScheduling = this.causeScheduling;
		return clonedX;
	}
}