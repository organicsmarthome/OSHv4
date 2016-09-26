package osh.datatypes.logger;

import java.util.EnumMap;
import java.util.UUID;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.eal.hal.exchange.HALExchange;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class LoggerEpsPlsHALExchange extends HALExchange {

	private EnumMap<AncillaryCommodity,PriceSignal> ps;
	private EnumMap<AncillaryCommodity,PowerLimitSignal> pwrLimit;
	
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 * @param ps
	 * @param pwrLimit
	 */
	public LoggerEpsPlsHALExchange(
			UUID sender, 
			long timestamp,
			EnumMap<AncillaryCommodity,PriceSignal> ps,
			EnumMap<AncillaryCommodity,PowerLimitSignal> pwrLimit) {
		super(sender, timestamp);
		
		this.ps = ps;
		this.pwrLimit = pwrLimit;
	}


	public EnumMap<AncillaryCommodity, PriceSignal> getPs() {
		return ps;
	}

	public void setPs(EnumMap<AncillaryCommodity, PriceSignal> ps) {
		this.ps = ps;
	}

	public EnumMap<AncillaryCommodity,PowerLimitSignal> getPwrLimit() {
		return pwrLimit;
	}

	public void setPwrLimit(EnumMap<AncillaryCommodity,PowerLimitSignal> pwrLimit) {
		this.pwrLimit = pwrLimit;
	}
	
}
