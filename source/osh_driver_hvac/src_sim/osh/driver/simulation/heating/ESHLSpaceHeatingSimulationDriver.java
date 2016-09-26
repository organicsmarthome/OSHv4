package osh.driver.simulation.heating;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.simulation.ThermalDemandSimulationDriver;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ESHLSpaceHeatingSimulationDriver 
					extends ThermalDemandSimulationDriver {

	public ESHLSpaceHeatingSimulationDriver(IOSH controllerbox,
			UUID deviceID, OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig, Commodity.HEATINGHOTWATERPOWER);
	}
	
	@Override
	protected double getDayOfWeekCorrection(
			int convertUnixTime2CorrectedWeekdayInt) {
		return 1;
	}

	@Override
	protected double getMonthlyCorrection(int convertUnixTime2MonthInt) {
		return 1;
	}

	// scale to 2000 kWh / (PAX * a) 
	@Override
	protected double getGeneralCorrection() {
		return 1.035576807;
	}

}
