package osh.comdriver;

import java.util.EnumMap;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.hal.exchange.PlsComExchange;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class FlatPlsProviderComDriver extends CALComDriver {

	private EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals;
	
	/** Time after which a signal is send */
	private int newSignalAfterThisPeriod;
	
	/** Maximum time the signal is available in advance (36h) */
	private int signalPeriod;
	
	private long lastTimeSignalSent = 0L;
	
	private int activeLowerLimit;
	private int activeUpperLimit;
	
	private int reactiveLowerLimit;
	private int reactiveUpperLimit;

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 */
	public FlatPlsProviderComDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException {
		super(controllerbox, deviceID, driverConfig);		
		this.powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);
		
		try {
			this.newSignalAfterThisPeriod = Integer.valueOf(getComConfig().getParameter("newSignalAfterThisPeriod"));
		}
		catch (Exception e) {
			this.newSignalAfterThisPeriod = 43200; //12h
			getGlobalLogger().logWarning("Can't get newSignalAfterThisPeriod, using the default value: " + this.newSignalAfterThisPeriod);
		}
		
		try {
			this.signalPeriod = Integer.valueOf(getComConfig().getParameter("signalPeriod"));
		}
		catch (Exception e) {
			this.signalPeriod = 129600; //36h
			getGlobalLogger().logWarning("Can't get signalPeriod, using the default value: " + this.signalPeriod);
		}
		
		try {
			this.activeLowerLimit = Integer.valueOf(getComConfig().getParameter("activeLowerLimit"));
		}
		catch (Exception e) {
			this.activeLowerLimit = -3000; //kW
			getGlobalLogger().logWarning("Can't get activeLowerLimit, using the default value: " + this.activeLowerLimit);
		}
		
		try {
			this.activeUpperLimit = Integer.valueOf(getComConfig().getParameter("activeUpperLimit"));
		}
		catch (Exception e) {
			this.activeUpperLimit = 10000; //kW
			getGlobalLogger().logWarning("Can't get activeUpperLimit, using the default value: " + this.activeUpperLimit);
		}
		
		try {
			this.reactiveLowerLimit = Integer.valueOf(getComConfig().getParameter("reactiveLowerLimit"));
		}
		catch (Exception e) {
			this.reactiveLowerLimit = -3000; //kW
			getGlobalLogger().logWarning("Can't get reactiveLowerLimit, using the default value: " + this.reactiveLowerLimit);
		}
		
		try {
			this.reactiveUpperLimit = Integer.valueOf(getComConfig().getParameter("reactiveUpperLimit"));
		}
		catch (Exception e) {
			this.reactiveUpperLimit = 10000; //kW
			getGlobalLogger().logWarning("Can't get reactiveUpperLimit, using the default value: " + this.reactiveUpperLimit);
		}
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		long now = getTimer().getUnixTime();
		powerLimitSignals = generateNewPowerLimitSignal(now);
		PlsComExchange ex = new PlsComExchange(
				this.getDeviceID(), 
				now, 
				powerLimitSignals);
		this.notifyComManager(ex);
		
		lastTimeSignalSent = now;
		
		// register
		this.getTimer().registerComponent(this, 1);
	}
	
	
	//TODO: better signal
	private EnumMap<AncillaryCommodity,PowerLimitSignal> generateNewPowerLimitSignal(long now) {
		EnumMap<AncillaryCommodity,PowerLimitSignal> newPls = new EnumMap<>(AncillaryCommodity.class);
		
		PowerLimitSignal activePowerLimitSignal = new PowerLimitSignal();
		activePowerLimitSignal.setPowerLimit(now, activeUpperLimit, activeLowerLimit);
		activePowerLimitSignal.setKnownPowerLimitInterval(now, now + signalPeriod);
		newPls.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, activePowerLimitSignal);
		
		PowerLimitSignal reactivePowerLimitSignal = new PowerLimitSignal();
		reactivePowerLimitSignal.setPowerLimit(now, reactiveUpperLimit, reactiveLowerLimit);
		reactivePowerLimitSignal.setKnownPowerLimitInterval(now, now + signalPeriod);
		newPls.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, reactivePowerLimitSignal);		
		
		return newPls;
	}

	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		long now = getTimer().getUnixTime();
		// generate new PriceSignal and send it
		if ((now - lastTimeSignalSent) >= newSignalAfterThisPeriod) {
			// PLS
			powerLimitSignals = generateNewPowerLimitSignal(now);
			PlsComExchange ex = new PlsComExchange(
					this.getDeviceID(), 
					now, 
					powerLimitSignals);
			this.notifyComManager(ex);
			
			lastTimeSignalSent = now;
		}
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		//NOTHING
	}

}
