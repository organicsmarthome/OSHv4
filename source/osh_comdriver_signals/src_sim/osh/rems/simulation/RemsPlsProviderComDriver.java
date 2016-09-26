package osh.rems.simulation;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.power.PowerInterval;
import osh.hal.exchange.PlsComExchange;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Simone Droll
 *
 */

public class RemsPlsProviderComDriver extends CALComDriver {

	private EnumMap<AncillaryCommodity, PowerLimitSignal> remsPowerLimitSignals;

	/** Time after which a signal is send */
	private int newSignalAfterThisPeriod;

	/** Maximum time the signal is available in advance (36h) */
	private int signalPeriod;

	private int activeLowerLimit;
	private int activeUpperLimit;

	private int reactiveLowerLimit;
	private int reactiveUpperLimit;

	private EnumMap<AncillaryCommodity, PowerLimitSignal> newSignals;
	private boolean newSignalReceived = false;
	
	private long lastTimeSignalSent = 0L;

	public RemsPlsProviderComDriver(IOSH controllerbox, UUID deviceID, OSHParameterCollection driverConfig)
			throws SimulationSubjectException {
		super(controllerbox, deviceID, driverConfig);
		this.remsPowerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

		try {
			this.newSignalAfterThisPeriod = Integer.valueOf(getComConfig().getParameter("newSignalAfterThisPeriod"));
		} catch (Exception e) {
			this.newSignalAfterThisPeriod = 43200; // 12h
			getGlobalLogger().logWarning(
					"Can't get newSignalAfterThisPeriod, using the default value: " + this.newSignalAfterThisPeriod);
		}

		try {
			this.signalPeriod = Integer.valueOf(getComConfig().getParameter("signalPeriod"));
		} catch (Exception e) {
			this.signalPeriod = 129600; // 36h
			getGlobalLogger().logWarning("Can't get signalPeriod, using the default value: " + this.signalPeriod);
		}

		try {
			this.activeLowerLimit = Integer.valueOf(getComConfig().getParameter("activeLowerLimit"));
		} catch (Exception e) {
			this.activeLowerLimit = -3000; // kW
			getGlobalLogger()
					.logWarning("Can't get activeLowerLimit, using the default value: " + this.activeLowerLimit);
		}

		try {
			this.activeUpperLimit = Integer.valueOf(getComConfig().getParameter("activeUpperLimit"));
		} catch (Exception e) {
			this.activeUpperLimit = 10000; // kW
			getGlobalLogger()
					.logWarning("Can't get activeUpperLimit, using the default value: " + this.activeUpperLimit);
		}

		try {
			this.reactiveLowerLimit = Integer.valueOf(getComConfig().getParameter("reactiveLowerLimit"));
		} catch (Exception e) {
			this.reactiveLowerLimit = -3000; // kW
			getGlobalLogger()
					.logWarning("Can't get reactiveLowerLimit, using the default value: " + this.reactiveLowerLimit);
		}

		try {
			this.reactiveUpperLimit = Integer.valueOf(getComConfig().getParameter("reactiveUpperLimit"));
		} catch (Exception e) {
			this.reactiveUpperLimit = 10000; // kW
			getGlobalLogger()
					.logWarning("Can't get reactiveUpperLimit, using the default value: " + this.reactiveUpperLimit);
		}
	}

	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();

		long now = getTimer().getUnixTime();
		remsPowerLimitSignals = generateNewPowerLimitSignal(now);
		PlsComExchange ex = new PlsComExchange(this.getDeviceID(), now, remsPowerLimitSignals);
		this.notifyComManager(ex);
		
		lastTimeSignalSent = now;

		// register
		this.getTimer().registerComponent(this, 1);
	}

	// TODO: better signal
	private EnumMap<AncillaryCommodity, PowerLimitSignal> generateNewPowerLimitSignal(long now) {
		EnumMap<AncillaryCommodity, PowerLimitSignal> newPls = new EnumMap<>(AncillaryCommodity.class);

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
			remsPowerLimitSignals = generateNewPowerLimitSignal(now);
			PlsComExchange ex = new PlsComExchange(this.getDeviceID(), now, remsPowerLimitSignals);
			this.notifyComManager(ex);
			
			lastTimeSignalSent = now;
		} else if (newSignalReceived) {
			EnumMap<AncillaryCommodity, PowerLimitSignal> plSignals = new EnumMap<AncillaryCommodity, PowerLimitSignal>(AncillaryCommodity.class);
			for (Entry<AncillaryCommodity, PowerLimitSignal> pls : newSignals.entrySet()) {
				plSignals.put(pls.getKey(), pls.getValue());
			}

			PlsComExchange ex = new PlsComExchange(this.getDeviceID(), now, remsPowerLimitSignals);
			this.notifyComManager(ex);

			newSignalReceived = false;
			newSignals.clear();
			
			lastTimeSignalSent = now;
		}
	}

	// PLS needs to be relative from now
	public void setNewSignal(PowerLimitSignal signal) {
		// System.out.println("PowerLimitSignal received");
		newSignals = new EnumMap<AncillaryCommodity, PowerLimitSignal>(AncillaryCommodity.class);
		long now = getTimer().getUnixTime();
		PowerLimitSignal pls = new PowerLimitSignal();
		pls.setKnownPowerLimitInterval(now, now + signal.getLimitUnknownAtAndAfter());
		for (Entry<Long, PowerInterval> en : signal.getPowerLimits().entrySet()) {
			pls.setPowerLimit(en.getKey() + now, en.getValue());
		}
		newSignals.put(signal.getAc(), pls);
		newSignalReceived = true;
	}

	public EnumMap<AncillaryCommodity, PowerLimitSignal> getPowerLimitSignals() {
		return remsPowerLimitSignals;
	}

	public void setPowerLimitSignals(EnumMap<AncillaryCommodity, PowerLimitSignal> remsPowerLimitSignals) {
		this.remsPowerLimitSignals = remsPowerLimitSignals;
	}

	public int getNewSignalAfterThisPeriod() {
		return newSignalAfterThisPeriod;
	}

	public void setNewSignalAfterThisPeriod(int newSignalAfterThisPeriod) {
		this.newSignalAfterThisPeriod = newSignalAfterThisPeriod;
	}

	public int getSignalPeriod() {
		return signalPeriod;
	}

	public void setSignalPeriod(int signalPeriod) {
		this.signalPeriod = signalPeriod;
	}

	public int getActiveLowerLimit() {
		return activeLowerLimit;
	}

	public void setActiveLowerLimit(int activeLowerLimit) {
		this.activeLowerLimit = activeLowerLimit;
	}

	public int getActiveUpperLimit() {
		return activeUpperLimit;
	}

	public void setActiveUpperLimit(int activeUpperLimit) {
		this.activeUpperLimit = activeUpperLimit;
	}

	public int getReactiveLowerLimit() {
		return reactiveLowerLimit;
	}

	public void setReactiveLowerLimit(int reactiveLowerLimit) {
		this.reactiveLowerLimit = reactiveLowerLimit;
	}

	public int getReactiveUpperLimit() {
		return reactiveUpperLimit;
	}

	public void setReactiveUpperLimit(int reactiveUpperLimit) {
		this.reactiveUpperLimit = reactiveUpperLimit;
	}

	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		// NOTHING

	}

}
