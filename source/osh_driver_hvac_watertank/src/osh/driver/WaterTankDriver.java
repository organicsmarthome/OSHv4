package osh.driver;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;


/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public abstract class WaterTankDriver 
				extends HALDeviceDriver 
				implements IEventTypeReceiver, IHasState {
	
//	private WaterTank waterTank;
	
	protected LoadProfileCompressionTypes compressionType;
	protected int compressionValue;

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws OSHException
	 * @throws HALException 
	 */
	public WaterTankDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig)
			throws OSHException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		try {
			this.compressionType = LoadProfileCompressionTypes.valueOf(getDriverConfig().getParameter("compressionType"));
		}
		catch (Exception e) {
			this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
			getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
		}
		
		try {
			this.compressionValue = Integer.valueOf(getDriverConfig().getParameter("compressionValue"));
		}
		catch (Exception e) {
			this.compressionValue = 100;
			getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
		}
	}
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();

		getDriverRegistry().registerStateChangeListener(TemperatureDetails.class, this);
		
		StaticCompressionExchange observerExchange = 
				new StaticCompressionExchange(getDeviceID(), getTimer().getUnixTime(), compressionType, compressionValue);
		this.notifyObserver(observerExchange);
	}
	
}



