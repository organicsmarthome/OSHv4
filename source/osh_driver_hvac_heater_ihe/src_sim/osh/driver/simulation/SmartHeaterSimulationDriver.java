package osh.driver.simulation;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.driver.ihe.SmartHeaterModel;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.exception.EnergySimulationException;
import osh.hal.exchange.SmartHeaterOX;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class SmartHeaterSimulationDriver extends DeviceSimulationDriver {
	
	private int temperatureSetting;
	private final int INITIAL_STATE = 0;
	private final long[] INITIAL_LAST_CHANGE = {-1, -1, -1}; // do NOT use MIN_VALUE!!!
	
	private long newIppAfter;
	private int triggerIppIfDeltaTempBigger;	
	
	private SmartHeaterModel model;
	private double currentWaterTemperature;

	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 * @throws HALException 
	 */
	public SmartHeaterSimulationDriver(IOSH controllerbox, UUID deviceID,
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		try {
			this.temperatureSetting = Integer.valueOf(getDriverConfig().getParameter("temperatureSetting"));
		}
		catch (Exception e) {
			this.temperatureSetting = 80;
			getGlobalLogger().logWarning("Can't get temperatureSetting, using the default value: " + this.temperatureSetting);
		}
		
		try {
			this.newIppAfter = Long.valueOf(getDriverConfig().getParameter("newIppAfter"));
		}
		catch (Exception e) {
			this.newIppAfter = 1 * 3600; // 1 hour
			getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
		}
		
		try {
			this.triggerIppIfDeltaTempBigger = Integer.valueOf(getDriverConfig().getParameter("triggerIppIfDeltaTempBigger"));
		}
		catch (Exception e) {
			this.triggerIppIfDeltaTempBigger = 1;
			getGlobalLogger().logWarning("Can't get triggerIppIfDeltaTempBigger, using the default value: " + this.triggerIppIfDeltaTempBigger);
		}
		
		this.model = new SmartHeaterModel(
				temperatureSetting, 
				INITIAL_STATE,
				INITIAL_LAST_CHANGE);
	}
	
	@Override
	public void onSimulationIsUp() throws SimulationSubjectException {
		super.onSimulationIsUp();
		
		IPPSchedulingExchange _ise = new IPPSchedulingExchange(getDeviceID(), getTimer().getUnixTime());
		_ise.setNewIppAfter(newIppAfter);
		_ise.setTriggerIfDeltaX(triggerIppIfDeltaTempBigger);
		this.notifyObserver(_ise);
	}
	
	@Override
	public void onSystemShutdown() {
		if (DatabaseLoggerThread.isLogSmartHeater()) {
			DatabaseLoggerThread.enqueueSmartHeater(model.getCounter(), model.getRuntime(), model.getPowerTierRunTimes());
		}		
	};

	@Override
	public void onNextTimeTick() {
		long now = getTimer().getUnixTime();
		
		int availablePower = 0;
//		if (ancillaryInputStates != null) {
		if (ancillaryMeterState != null) {
			
			// #1
//			double chpFeedIn = 0;
//			double pvFeedIn = 0;
//			if (ancillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) != null) {
//				chpFeedIn = Math.abs(ancillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN).getPower());
//			}
//			if (ancillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN) != null) {
//				pvFeedIn = Math.abs(ancillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPower());
//			}
//			availablePower = (int) (chpFeedIn + pvFeedIn);
			
			// #2
//			if (ancillaryInputStates.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL) != null) {
				// iff < 0 -> use it with IHE
//				availablePower = (int) ancillaryInputStates.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getPower();
				
				availablePower = (int) ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
								
//				if (availablePower < -600) {
//					@SuppressWarnings("unused")
//					int xxx = 0;
//				}
//			}
		}
		
		this.model.updateAvailablePower(now, availablePower, currentWaterTemperature);
		
		int activePower = (int) this.model.getPower();
		int hotWaterPower = (int) -this.model.getPower();
		
		setPower(Commodity.ACTIVEPOWER, activePower);
		setPower(Commodity.REACTIVEPOWER, activePower);
		setPower(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
		
		if (hotWaterPower != 0) {
			@SuppressWarnings("unused")
			int xxx = 0;
		}
		
		SmartHeaterOX ox = new SmartHeaterOX(
				getDeviceID(), 
				getTimer().getUnixTime(),
				temperatureSetting,
				(int) currentWaterTemperature,
				this.model.getCurrentState(),
				activePower,
				hotWaterPower,
				this.model.getTimestampOfLastChangePerSubElement());
		this.notifyObserver(ox);
	}

	@Override
	public void setCommodityInputStates(
			LimitedCommodityStateMap inputStates,
			AncillaryMeterState ancillaryMeterState) throws EnergySimulationException {
		super.setCommodityInputStates(inputStates, ancillaryMeterState);

		if (inputStates != null) {
			if (inputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
				this.currentWaterTemperature = inputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
			}
		}
	}
	
	
	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}

}
