package osh.driver;

import java.util.HashMap;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.datatypes.registry.driver.details.chp.ChpDriverDetails;
import osh.datatypes.registry.driver.details.chp.raw.DachsDriverDetails;
import osh.driver.dachs.WAMPDachsDispatcher;
import osh.eal.hal.exceptions.HALException;
import osh.registry.interfaces.IHasState;

/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class WAMPDachsChpDriver 
				extends DachsChpDriver 
				implements IHasState, Runnable {

	protected WAMPDachsDispatcher dachsInformationWAMPDispatcher;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public WAMPDachsChpDriver(
			IOSH osh, 
			UUID deviceID, 
			OSHParameterCollection driverConfig)
			throws OSHException, HALException {
		super(osh, deviceID, driverConfig);
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 300);
		
		this.dachsInformationWAMPDispatcher = new WAMPDachsDispatcher(getGlobalLogger(), this);
		new Thread(this, "pull proxy of dachs driver to WAMP").start();
	}
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
	} 
	
	@Override
	public void onSystemShutdown() throws OSHException {
		super.onSystemShutdown();
	}
	
	@Override
	public void run() {
		while (true) {
			synchronized (this.dachsInformationWAMPDispatcher) {
				try { // wait for new data
					this.dachsInformationWAMPDispatcher.wait();
				} catch (InterruptedException e) {
					getGlobalLogger().logError("should not happen", e);
					break;
				}

				// long timestamp = getTimer().getUnixTime();

				if (dachsInformationWAMPDispatcher.getDachsDetails().isEmpty()) {
					// an error has occurred
				}
			}
		}
	}

	@Override
	protected void sendPowerRequestToChp() {
		// Start new thread and send power request to CHP
		// (Has to be resend at least every 10 minutes)
		if (isOperationRequest()==false) {
			this.dachsInformationWAMPDispatcher.sendPowerRequest(isOperationRequest());
		}
	}

	// for callback of DachsInformationRequestThread
	public void processDachsDetails(DachsDriverDetails dachsDetails) {

		if (dachsDetails == null) {
			return;
		}

		// ### save DachsDetails into DB ###
		getDriverRegistry().setStateOfSender(DachsDriverDetails.class, dachsDetails);

		// ### transform DachsDetails to ChpDetails ###
		HashMap<String, String> values = dachsDetails.getValues();

		// convert Dachs Details to general CHP details
		ChpDriverDetails chpDetails = new ChpDriverDetails(getDeviceID(), getTimer().getUnixTime());

		// Heating request or power request? Or both?
		chpDetails.setPowerGenerationRequest(isElectricityRequest());
		chpDetails.setHeatingRequest(isHeatingRequest());

		// current power
		Double currentElectricalPower = parseDoubleStatus(values.get("Hka_Mw1.sWirkleistung"));
		if (currentElectricalPower == null) {
			currentElectricalPower = -1.0;
		} else {
			currentElectricalPower = currentElectricalPower * -1000.0;
		}
		chpDetails.setCurrentElectricalPower(currentElectricalPower);

		Double currentThermalPower = -1.0;
		if (Math.round(currentElectricalPower) < -1000) {
			currentThermalPower = -12500.0;
		}
		chpDetails.setCurrentThermalPower(currentThermalPower);

		// total energy
		Double generatedElectricalWork = parseDoubleStatus(values.get("Hka_Bd.ulArbeitElektr"));
		if (generatedElectricalWork == null) {
			generatedElectricalWork = -1.0;
		}
		chpDetails.setGeneratedElectricalWork(generatedElectricalWork);

		Double generatedThermalWork = parseDoubleStatus(values.get("Hka_Bd.ulArbeitThermHka"));
		if (generatedThermalWork != null) {
			generatedThermalWork = -1.0;
			chpDetails.setGeneratedThermalWork(generatedThermalWork);
		}

		// priorities
		Integer electicalPowerPriorizedControl = parseIntegerStatus(values.get("Hka_Bd.UStromF_Frei.bFreigabe"));
		if (electicalPowerPriorizedControl != null) {
			if (electicalPowerPriorizedControl == 255) {
				chpDetails.setElecticalPowerPriorizedControl(true);
			} else {
				chpDetails.setElecticalPowerPriorizedControl(false);
			}
		}
		// always with thermal priority
		chpDetails.setThermalPowerPriorizedControl(true);

		// temperature
		Integer temperatureIn = parseIntegerStatus(values.get("Hka_Mw1.Temp.sbGen"));
		if (temperatureIn == null) {
			temperatureIn = -1;
		}
		chpDetails.setTemperatureIn(temperatureIn);

		Integer temperatureOut = parseIntegerStatus(values.get("Hka_Mw1.Temp.sbMotor"));
		if (temperatureOut == null) {
			temperatureOut = -1;
		}
		chpDetails.setTemperatureOut(temperatureOut);

		// convert to TemperatureDetails
		Double waterStorageTemperature = parseDoubleStatus(values.get("Hka_Mw1.Temp.sbFuehler1"));
		if (waterStorageTemperature != null) {
			TemperatureDetails td = new TemperatureDetails(getHotWaterTankUuid(), getTimer().getUnixTime());
			td.setTemperature(waterStorageTemperature);
			getDriverRegistry().setStateOfSender(TemperatureDetails.class, td);
		}

		this.chpDriverDetails = chpDetails;
		getDriverRegistry().setState(ChpDriverDetails.class, this, chpDriverDetails);
		processChpDetailsAndNotify(chpDriverDetails);
	}
	
}
