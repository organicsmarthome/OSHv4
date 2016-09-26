package osh.driver.simulation;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.driver.thermal.SimpleWaterTank;
import osh.eal.hal.exceptions.HALException;
import osh.registry.interfaces.IHasState;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Ingo Mauser
 *
 */
public abstract class WaterTankSimulationDriver 
							extends DeviceSimulationDriver
							implements IHasState {
	
	protected SimpleWaterTank waterTank;
	
	/**
	 * CONSTRUCTOR
	 */
	public WaterTankSimulationDriver(
			IOSH osh, 
			UUID deviceID,
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(osh, deviceID, driverConfig);
	}
	
//	Nothing to do here
//	@Override
//	public void onSimulationIsUp() throws SimulationSubjectException {
//		super.onSimulationIsUp();
//	}	
}
