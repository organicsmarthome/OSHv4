package osh.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import osh.comdriver.details.WeatherPredictionDetails;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.driver.model.BuildingThermalModel;
import osh.driver.model.ESHLThermalModel;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.SpaceHeatingPredictionObserverExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.utils.physics.TemperatureUtil;

/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class SpaceHeatingDriver 
						extends HALDeviceDriver
						implements IEventTypeReceiver, IHasState{

	private BuildingThermalModel model;
	private WeatherPredictionDetails weatherPredictionDetails;
	//private TemperaturePrediction temperaturePrediction; 
	private Map<Long, Double> predictedHeatConsumptionMap;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	private UUID weahterPredictionProviderUUID;
	
	
	public SpaceHeatingDriver(
			IOSH controllerbox, 
			UUID deviceID, 
			OSHParameterCollection driverConfig)
			throws HALException {
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
		
		try {
			this.weahterPredictionProviderUUID = UUID.fromString(getDriverConfig().getParameter("weahterPredictionProviderUUID"));
		}
		catch (Exception e) {
			this.weahterPredictionProviderUUID = UUID.fromString("00000000-0000-2200-0000-7265ab8ef219");
			getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.weahterPredictionProviderUUID);
		}
		
		
		this.model = new ESHLThermalModel();
		this.predictedHeatConsumptionMap = new HashMap<>();
	}

	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getDriverRegistry().registerStateChangeListener(WeatherPredictionDetails.class, this);
		
		StaticCompressionExchange observerExchange = 
				new StaticCompressionExchange(getDeviceID(), getTimer().getUnixTime(), compressionType, compressionValue);
			
		this.notifyObserver(observerExchange);
	}
	
	
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) throws HALException {
		//NOTHING
	}


	@Override
	public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {
		if( event instanceof StateChangedExchange  && ((StateChangedExchange) event).getStatefulentity().equals(weahterPredictionProviderUUID)) {
			StateChangedExchange exsc = (StateChangedExchange) event;
			boolean updateOx = false;
			
			if (exsc.getType().equals(WeatherPredictionDetails.class)) {
				weatherPredictionDetails = getDriverRegistry().getState(WeatherPredictionDetails.class, exsc.getStatefulentity());
				
				for (int index = 0; index < weatherPredictionDetails.getTemperatureForecastList().getList().size(); index++) {
					double temperaturePrediction = TemperatureUtil.convertKelvinToCelsius(
							weatherPredictionDetails.getTemperatureForecastList().getList().get(index).getMain().getTemp());
					long timeOfPrediction = weatherPredictionDetails.getTemperatureForecastList().getList().get(index).getDt();
					predictedHeatConsumptionMap.put(timeOfPrediction, model.calculateHeatingDemand(temperaturePrediction));
				}
				updateOx = true;
			}		
			
			if (updateOx){
				SpaceHeatingPredictionObserverExchange observerExchange = 
					new SpaceHeatingPredictionObserverExchange(
							getDeviceID(), 
							getTimer().getUnixTime(),
							predictedHeatConsumptionMap);
				this.notifyObserver(observerExchange);
			}
		}
	}


	@Override
	public UUID getUUID() {
		return getDeviceID();
	}

}
