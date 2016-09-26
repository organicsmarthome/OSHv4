package osh.simulation;

import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public abstract class SimulationComDriver extends CALComDriver {

	private ISimulationActionLogger simlogger = null;
	
	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 */
	public SimulationComDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException {
		super(controllerbox, deviceID, driverConfig);
	}

	@Override
	public final UUID getDeviceID() {
		return super.getDeviceID();
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		//NOTHING
	}
	
	
	/** Please use only for logging stuff */
	public PriceSignal getPriceSignal(AncillaryCommodity c) {
		return null;
	}
	
	/** Please use only for logging stuff */
	public PowerLimitSignal getPowerLimitSignal(AncillaryCommodity c) {
		return null;
	}
	
	
	protected ISimulationActionLogger getSimlogger() {
		return simlogger;
	}

	public void setSimulationActionLogger(ISimulationActionLogger simulationLogger) {
		this.simlogger = simulationLogger;
	}

}
