package osh.datatypes.registry.commands;

import java.util.EnumMap;
import java.util.UUID;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.CommandExchange;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class PriceSignalCommandExchange extends CommandExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3519660001234351000L;
	private EnumMap<AncillaryCommodity,PriceSignal> priceSignals;
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param receiver
	 * @param timestamp
	 * @param priceSignals
	 */
	public PriceSignalCommandExchange(
			UUID sender, 
			UUID receiver, 
			long timestamp,
			EnumMap<AncillaryCommodity,PriceSignal> priceSignals) {
		super(sender, receiver, timestamp);
		
		this.priceSignals = priceSignals;
	}
	
	
	public EnumMap<AncillaryCommodity,PriceSignal> getPriceSignals() {
		return priceSignals;
	}
	
	public PriceSignal getPriceSignal(AncillaryCommodity c) {
		if (priceSignals != null) {
			return priceSignals.get(c);
		}
		else {
			return null;
		}
	}
	
}
