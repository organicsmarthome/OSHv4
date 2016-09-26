package osh.driver.simulation.cooling;

import java.util.ArrayList;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.driver.simulation.spacecooling.HollOutdoorTemperatures;
import osh.driver.simulation.spacecooling.OutdoorTemperatures;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.screenplay.SubjectAction;

/**
 * 
 * @author Ingo Mauser
 *
 */
public abstract class SpaceCoolingSimulationDriver extends DeviceSimulationDriver {

	protected ArrayList<ChillerCalendarDate> dates;
	/** [W] (negative value) */
	protected double coldWaterPowerDemand;
	/** [°C] */
	protected OutdoorTemperatures outdoorTemperature;
	
	/**
	 * CONSTRUCTOR
	 */
	public SpaceCoolingSimulationDriver(
			IOSH osh, 
			UUID deviceID, 
			OSHParameterCollection driverConfig)
			throws HALException {
		super(osh, deviceID, driverConfig);
	}
	
	
	@Override
	public void onSystemIsUp() {
		super.onSystemIsUp();
	
		this.outdoorTemperature = new HollOutdoorTemperatures(getGlobalLogger());
	}
	
	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}
	
}
