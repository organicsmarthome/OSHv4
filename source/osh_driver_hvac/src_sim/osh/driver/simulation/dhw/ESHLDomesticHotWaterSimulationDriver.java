package osh.driver.simulation.dhw;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.simulation.ThermalDemandSimulationDriver;
import osh.driver.simulation.thermal.VDI6002DomesticHotWaterStatistics;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ESHLDomesticHotWaterSimulationDriver 
					extends ThermalDemandSimulationDriver {

	public ESHLDomesticHotWaterSimulationDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig, Commodity.DOMESTICHOTWATERPOWER);
	}

	@Override
	protected double getDayOfWeekCorrection(int convertUnixTime2CorrectedWeekdayInt) {
		return VDI6002DomesticHotWaterStatistics.getDayOfWeekCorrection(convertUnixTime2CorrectedWeekdayInt);
	}

	@Override
	protected double getMonthlyCorrection(int convertUnixTime2MonthInt) {
		return VDI6002DomesticHotWaterStatistics.getMonthlyCorrection(convertUnixTime2MonthInt);
	}

	@Override
	protected double getGeneralCorrection() {
		// manual correction to achieve 700 kWh / (PAX * a)
		return 0.77500775;
	}
	
}
