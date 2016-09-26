package osh.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.details.common.DeviceMetaDriverDetails;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.driver.bacnet.BacNetDispatcher;
import osh.driver.bacnet.BacNetDispatcher.BacNetObject;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.hal.exchange.BacNetThermalExchange;


/**
 * BacNet/IP thermal sensors and A/C control
 * @author Kaibin Bao
 *
 */
@Deprecated
public class BacNetThermalDriver extends HALDeviceDriver {
	
	static private BacNetDispatcher dispatcher = null;

	public static final String TEMPERATUREKEY_SETPOINT = "setpoint";
	
	private DeviceMetaDriverDetails deviceMetaDetails;
	private BacNetObject sensorObject = null;
	private List<BacNetObject> actuatorObjects;
	private String sensorObjectName;
	private String actuatorObjectName;
	

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws OSHException
	 * @throws HALException 
	 */
	public BacNetThermalDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) throws OSHException, HALException {
		super(controllerbox, deviceID, driverConfig);
	}
	
	private void init( OSHParameterCollection config ) throws OSHException {
		deviceMetaDetails = new DeviceMetaDriverDetails(getDeviceID(), getTimer().getUnixTime());
		deviceMetaDetails.setName(config.getParameter("name"));
		deviceMetaDetails.setLocation(config.getParameter("location"));
		// deviceDetails.setDeviceType(getDeviceType().toString());
		// deviceDetails.setDeviceClass(getDeviceClassification().toString());
		
		if( dispatcher == null ) {
			dispatcher = new BacNetDispatcher(getTimer(), getGlobalLogger());
			try {
				dispatcher.init();
			} catch (IOException e) {
				throw new OSHException("could not initialize BacNet dispatcher", e);
			}
		}
		
		String bacNetController = config.getParameter("controller");
		if( bacNetController == null || bacNetController.length() <= 0 )
			throw new OSHException("Invalid config parameter: controller");

		dispatcher.addDevice(bacNetController, 47808);
		
		sensorObjectName = config.getParameter("sensor");
		{
			if( sensorObjectName.length() <= 0 )
				throw new OSHException("Invalid Sensor");

			String[] oid = sensorObjectName.split("/");
			if( oid.length != 2 )
				throw new OSHException("Invalid Sensor");
			
			try {
				int devOid = Integer.parseInt(oid[0]); // device id of bacnet controller
				int objOid = Integer.parseInt(oid[1]); // sensor object id
				sensorObject = new BacNetObject(devOid, objOid);
			} catch ( NumberFormatException e ) {
				throw new OSHException("Invalid Sensor", e);
			}
		}
		
		actuatorObjects = new ArrayList<BacNetObject>();
		// works? get setpoints from config
		actuatorObjectName = config.getParameter("actuator");
		{
			if( actuatorObjectName.length() <= 0 )
				throw new OSHException("Invalid Actuator");
			
			// 4 combinations of deviceId, objectId
			String[] actuators = actuatorObjectName.replaceAll("\\[|\\]", "").split(",");
					
			// for
			for (int i = 0; i < actuators.length; i++) {
				String[] oid = actuators[i].split("/");
				if( oid.length != 2 )
					throw new OSHException("Invalid Actuator");
				
				try {
					int devOid = Integer.parseInt(oid[0]); // device id of bacnet controller
					int objOid = Integer.parseInt(oid[1]); // actuator object id
					actuatorObjects.add(new BacNetObject(devOid, objOid));
				} catch ( NumberFormatException e ) {
					throw new OSHException("Invalid Actuator", e);
				}
			}
		}
	}
	
	@Override
	public void onSystemIsUp() throws OSHException {
		init(getDriverConfig());
		getTimer().registerComponent(this, 1);
		super.onSystemIsUp();
	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		// build new HAL exchange
		BacNetThermalExchange _ox = buildObserverExchange(); 
		this.notifyObserver(_ox);
		
		// create TemperatureDetails and save to DriverRegistry
		getDriverRegistry().setStateOfSender(TemperatureDetails.class, _ox.getTemperatureDetails());
		
	}
	
	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) {
		//NOTHING
	}
	
	private BacNetThermalExchange buildObserverExchange() {
		BacNetThermalExchange _ox = new BacNetThermalExchange(this.getDeviceID(), getTimer().getUnixTime());
		
		_ox.setDeviceMetaDetails(deviceMetaDetails);
		
		TemperatureDetails _td = new TemperatureDetails(getDeviceID(), getTimer().getUnixTime());
		
		_td.setTemperature(dispatcher.getAnalogInputState(sensorObject));
		
		// get setpoints from dispatcher and use average setpoint
		Double average = 0.0;
		int i;
		for (i = 0; i < actuatorObjects.size(); i++) {
			BacNetObject obj = actuatorObjects.get(i);
			Double temp = dispatcher.getAnalogValueState(obj);
			if( temp == null )
				temp = 0.0;
			average = average + temp;
		}
		average = average / (i + 1);		
		
		// vorher: 22.0
		_td.addAuxiliaryTemperatures(TEMPERATUREKEY_SETPOINT, average);
		
		_ox.setTemperatureDetails(_td);
		
		return _ox;
	}
	
	// Hannah
	public List<BacNetDispatcher.BacNetObject> getActuatorObjects() {
		return actuatorObjects;
		
	}
}
