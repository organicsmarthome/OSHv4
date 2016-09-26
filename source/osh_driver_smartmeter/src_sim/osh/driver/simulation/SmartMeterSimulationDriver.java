package osh.driver.simulation;


import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.eal.hal.exceptions.HALException;
import osh.registry.interfaces.IHasState;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class SmartMeterSimulationDriver 
						extends DeviceSimulationDriver
						implements IHasState {
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 * @throws HALException 
	 */
	public SmartMeterSimulationDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		// NOTHING
	}
	
	
	@Override
	public void onNextTimeTick() {
		
		// get voltage
		//TODO
		
		// communicate state to logger
	}
	
	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}

	
	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
	
	
}
