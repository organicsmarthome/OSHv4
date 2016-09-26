package osh.busdriver;

import java.util.UUID;

import osh.comdriver.logger.KITValueDatabaseLogger;
import osh.comdriver.logger.ValueDatabaseLogger;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.eal.hal.exchange.IHALExchange;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser
 *
 */
public class KITLoggerBusDriver extends LoggerBusDriver {

	@SuppressWarnings("unused")
	private long lastPriceSignalLoggedAt = 0;
	
	protected ValueDatabaseLogger legacyRawLog = null;
	protected ValueDatabaseLogger legacySmartHomeLog1 = null;
	
	public KITLoggerBusDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
		
		// legacy logger
//		if ( valueLoggerConfiguration.getIsValueLoggingToDatabaseActive() ) {
//			legacyRawLog = new MeregioMobilLegacyDatabaseLogger(getGlobalLogger());
//			legacySmartHomeLog1 = new SmartHomeLog1LegacyDatabaseLogger(getGlobalLogger());
//			initSmartHome1Logging();
//		}
		
		if ( valueLoggerConfiguration.getIsValueLoggingToDatabaseActive() ) {
			this.databaseLog = new KITValueDatabaseLogger(getGlobalLogger());
		}
		
	}

	/**
	 * Register to Timer for timed logging operations (logger gets data to log by itself)<br>
	 * Register to DriverRegistry for logging operations trigger by Drivers
	 */
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
	}
	
	/**
	 * Pull-logging
	 */
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
	}
	


	private boolean isWagoMeter(UUID id) {
		return (id.getMostSignificantBits() >> 32) == 0x75086001 /* Wago Meter */;
	}


	private int getController(UUID id) {
		int controller = -1;
		if( id.getLeastSignificantBits() == 0x23c3c0a80134L ) {
			controller = 2;
		} else
		if( id.getLeastSignificantBits() == 0x23c3c0a80132L ) {
			controller = 3;
		} else
		if( id.getLeastSignificantBits() == 0x23c3c0a80133L ) {
			controller = 1;
		}
		return controller;
	}

	private int getPort(UUID id) {
		return (int) (id.getMostSignificantBits() & 0xFFFF);
	}

	private int getMeter(UUID id) {
		return (int) ((id.getMostSignificantBits() >> 16) & 0xFFFF);
	}

	
	/**
	 * Get things to log from O/C-layer
	 * @param exchangeObject
	 */
	@Override
	public void updateDataFromBusManager(IHALExchange exchangeObject) {
		//NOTHING
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
}
