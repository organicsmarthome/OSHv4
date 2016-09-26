package osh.driver;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.driver.thermal.SimpleHotWaterTank;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.hal.exchange.HotWaterTankObserverExchange;


/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class ESHLHotWaterTankDriver extends WaterTankDriver {

	private SimpleHotWaterTank waterTank;
	
	private long newIppAfter;
	private double triggerIppIfDeltaTempBigger;
	
//	private TemperatureDetails currentTemperatureDetails = null;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws OSHException
	 * @throws HALException 
	 */
	public ESHLHotWaterTankDriver(IOSH controllerbox, UUID deviceID,
			OSHParameterCollection driverConfig)
			throws OSHException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		// tank capacity in liters
		double tankCapacity;
		try {
			tankCapacity = Double.parseDouble(driverConfig.getParameter("tankCapacity"));
		} catch (Exception e) {
			tankCapacity = 750;
			getGlobalLogger().logWarning("Can't get tankCapacity, using the default value: " + tankCapacity);
		}
		
		double tankDiameter;
		try {
			tankDiameter = Double.parseDouble(driverConfig.getParameter("tankDiameter"));
		} catch (Exception e) {
			tankDiameter = 0.5;
			getGlobalLogger().logWarning("Can't get tankDiameter, using the default value: " + tankDiameter);
		}
		
		double initialTemperature;
		try {
			initialTemperature = Double.parseDouble(driverConfig.getParameter("initialTemperature"));
		} catch (Exception e) {
			initialTemperature = 70.0;
			getGlobalLogger().logWarning("Can't get initialTemperature, using the default value: " + initialTemperature);
		}
		
		double ambientTemperature;
		try {
			ambientTemperature = Double.parseDouble(driverConfig.getParameter("ambientTemperature"));
		} catch (Exception e) {
			ambientTemperature = 20.0;
			getGlobalLogger().logWarning("Can't get ambientTemperature, using the default value: " + ambientTemperature);
		}
		
		try {
			this.newIppAfter = Long.valueOf(getDriverConfig().getParameter("newIppAfter"));
		}
		catch (Exception e) {
			this.newIppAfter = 1 * 3600; // 1 hour
			getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
		}
		
		try {
			this.triggerIppIfDeltaTempBigger = Double.valueOf(getDriverConfig().getParameter("triggerIppIfDeltaTempBigger"));
		}
		catch (Exception e) {
			this.triggerIppIfDeltaTempBigger = 0.25;
			getGlobalLogger().logWarning("Can't get triggerIppIfDeltaTempBigger, using the default value: " + this.triggerIppIfDeltaTempBigger);
		}
		
		this.waterTank = new SimpleHotWaterTank(
				tankCapacity, 
				tankDiameter, 
				initialTemperature, 
				ambientTemperature);
	}
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		StaticCompressionExchange _stat = 
				new StaticCompressionExchange(getDeviceID(), getTimer().getUnixTime());
		_stat.setCompressionType(compressionType);
		_stat.setCompressionValue(compressionValue);
		this.notifyObserver(_stat);
		
		IPPSchedulingExchange _ise = new IPPSchedulingExchange(getDeviceID(), getTimer().getUnixTime());
		_ise.setNewIppAfter(newIppAfter);
		_ise.setTriggerIfDeltaX(triggerIppIfDeltaTempBigger);
		this.notifyObserver(_ise);
		
		HotWaterTankObserverExchange observerExchange = 
				new HotWaterTankObserverExchange(
						getDeviceID(), 
						getTimer().getUnixTime(),
						waterTank.getCurrentWaterTemperature(),
						waterTank.getTankCapacity(),
						waterTank.getTankDiameter(),
						waterTank.getAmbientTemperature(),
						0,
						0);
		this.notifyObserver(observerExchange);
	}

	
	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
		
		if( event instanceof StateChangedExchange && ((StateChangedExchange) event).getStatefulentity().equals(getDeviceID())) {
			StateChangedExchange exsc = (StateChangedExchange) event;
			boolean updateOx = false;
			
			if (exsc.getType().equals(TemperatureDetails.class)) {
				TemperatureDetails currentTemperatureDetails = getDriverRegistry().getState(TemperatureDetails.class, exsc.getStatefulentity());
				this.waterTank.setCurrentWaterTemperature(currentTemperatureDetails.getTemperature());
				updateOx = true;
			}			
			
			if (updateOx){
				HotWaterTankObserverExchange observerExchange = 
						new HotWaterTankObserverExchange(
								getDeviceID(), 
								getTimer().getUnixTime(),
								waterTank.getCurrentWaterTemperature(),
								waterTank.getTankCapacity(),
								waterTank.getTankDiameter(),
								waterTank.getAmbientTemperature(),
								0,
								0);
				this.notifyObserver(observerExchange);
			}
		}
	}
	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) throws HALException {
		//NOTHING
	}
	
}
