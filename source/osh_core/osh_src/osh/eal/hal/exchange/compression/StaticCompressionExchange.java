package osh.eal.hal.exchange.compression;

import java.util.UUID;

import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.eal.hal.exchange.HALObserverExchange;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class StaticCompressionExchange 
				extends HALObserverExchange {
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	public StaticCompressionExchange(UUID deviceID, Long timestamp) {
		super(deviceID, timestamp);
	}
	
	public StaticCompressionExchange(UUID deviceID, Long timestamp, LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super(deviceID, timestamp);
		this.compressionType = compressionType;
		this.compressionValue = compressionValue;
	}

	public LoadProfileCompressionTypes getCompressionType() {
		return compressionType;
	}

	public int getCompressionValue() {
		return compressionValue;
	}

	public void setCompressionType(LoadProfileCompressionTypes compressionType) {
		this.compressionType = compressionType;
	}

	public void setCompressionValue(int compressionValue) {
		this.compressionValue = compressionValue;
	}
}
