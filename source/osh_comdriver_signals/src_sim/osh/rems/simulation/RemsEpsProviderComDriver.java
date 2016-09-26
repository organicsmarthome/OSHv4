package osh.rems.simulation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.signals.PriceSignalGenerator;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.hal.exchange.EpsComExchange;
import osh.utils.time.TimeConversion;


/**
 * Based on McFlat for REMS
 * 
 * @author Malte Schröder
 *
 */
public class RemsEpsProviderComDriver extends CALComDriver {

	private EnumMap<AncillaryCommodity,PriceSignal> currentPriceSignal = new EnumMap<AncillaryCommodity, PriceSignal>(AncillaryCommodity.class);


	/** Time after which a signal is send */
	private int newSignalAfterThisPeriod = 43200; // 12 hours
	/** Timestamp of the last price signal sent to global controller */
	private long lastSignalSent = 0L;
	/** Maximum time the signal is available in advance (36h) */
	private int signalPeriod = 129600; // 36 hours
	/** Minimum time the signal is available in advance (24h) */
	private int signalAvailableFor = signalPeriod - newSignalAfterThisPeriod;
	/** Signal is constant for 15 minutes */
	private int signalConstantPeriod = 900; // change every 15 minutes

	private double activePowerPrice = 25.0;
	private double reactivePowerPrice = 0.0;
	private double naturalGasPowerPrice = 6.0;

	private double activePowerFeedInPV = 12.0;
	//	private double activePowerFeedInPV = 10.0;
	private double activePowerFeedInCHP = 9.0;
	//	private double activePowerFeedInCHP = 5.0;

	private boolean newSignalReceived = false;
	private ArrayList<PriceSignal> newSignals;


	public RemsEpsProviderComDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);

	}

	//PS needs to be relative from now
	public void setNewSignal(PriceSignal signal) {
		newSignals = new ArrayList<PriceSignal>();
		long now = getTimer().getUnixTime();
		PriceSignal ps = new PriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
		ps.setKnownPriceInterval(now, now + signal.getPriceUnknownAtAndAfter());
		for (Entry<Long, Double> en : signal.getPrices().entrySet()) {
			ps.setPrice(en.getKey() + now, en.getValue());
		}
		newSignals.add(ps);
		newSignalReceived = true;
	}

	public void setNewSignals(ArrayList<PriceSignal> signals) {
		this.newSignals = new ArrayList<PriceSignal>(signals.size());
		long now = getTimer().getUnixTime();

		for (PriceSignal signal : signals) {
			PriceSignal ps = new PriceSignal(signal.getCommodity());
			ps.setKnownPriceInterval(now, now + signal.getPriceUnknownAtAndAfter());
			for (Entry<Long, Double> en : signal.getPrices().entrySet()) {
				ps.setPrice(en.getKey() + now, en.getValue());
			}
			newSignals.add(ps);
		}		

		newSignalReceived = true;
	}


	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();

		long now = getTimer().getUnixTime();

		{
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL, activePowerPrice);
			this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
		}
		{
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.REACTIVEPOWEREXTERNAL, reactivePowerPrice);
			this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
		}
		{
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.NATURALGASPOWEREXTERNAL, naturalGasPowerPrice);
			this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
		}
		{
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN, activePowerFeedInPV);
			this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
		}
		{
			PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, activePowerFeedInCHP);
			this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
		}

		// EPS
		EpsComExchange ex = new EpsComExchange(
				this.getDeviceID(), 
				now, 
				currentPriceSignal);
		this.notifyComManager(ex);

		this.lastSignalSent = now;

		// register
		this.getTimer().registerComponent(this, 1);
	}

	@Override
	public void onNextTimePeriod() {

		long now = getTimer().getUnixTime();

		if (newSignalReceived) {
			ArrayList<AncillaryCommodity> allRelevantCommodities = new ArrayList<AncillaryCommodity>();
			allRelevantCommodities.add(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
			allRelevantCommodities.add(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);	
			allRelevantCommodities.add(AncillaryCommodity.NATURALGASPOWEREXTERNAL);
			allRelevantCommodities.add(AncillaryCommodity.PVACTIVEPOWERFEEDIN);
			allRelevantCommodities.add(AncillaryCommodity.REACTIVEPOWEREXTERNAL);


			for (PriceSignal signal : newSignals) {
				this.currentPriceSignal.put(signal.getCommodity(), signal);
				allRelevantCommodities.remove(signal.getCommodity());
			}

			for (AncillaryCommodity vc : allRelevantCommodities) {

				//TODO: Fix
				if (vc == AncillaryCommodity.ACTIVEPOWEREXTERNAL){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL, activePowerPrice);
					this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
				}
				else if (vc == AncillaryCommodity.REACTIVEPOWEREXTERNAL){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.REACTIVEPOWEREXTERNAL, reactivePowerPrice);
					this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
				}
				else if (vc == AncillaryCommodity.NATURALGASPOWEREXTERNAL){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.NATURALGASPOWEREXTERNAL, naturalGasPowerPrice);
					this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
				}
				else if (vc == AncillaryCommodity.PVACTIVEPOWERFEEDIN){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN, activePowerFeedInPV);
					this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
				}
				else if (vc == AncillaryCommodity.CHPACTIVEPOWERFEEDIN){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, activePowerFeedInCHP);
					this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
				}
			}

			// EPS
			EpsComExchange ex = new EpsComExchange(
					this.getDeviceID(), 
					now, 
					currentPriceSignal,
					true);
			this.notifyComManager(ex);
//			String uuid = this.getDeviceID().toString();
			
			lastSignalSent = now;
			//System.out.println("SENT NEW PRICE SIGNAL TO OSH: " + newSignals.get(0).getPrices() + " FOR: " + uuid.substring(uuid.length() - 3));
			//System.out.println("SENT NEW PRICE SIGNAL TO OSH: " + newSignals.get(0).getPrices() + newSignals.get(1).getPrices()+newSignals.get(2).getPrices());
		} else if ((now - lastSignalSent) >= newSignalAfterThisPeriod) {
			
			//DIRTY FIX, pls make better ...
			ArrayList<AncillaryCommodity> allRelevantCommodities = new ArrayList<AncillaryCommodity>();
			allRelevantCommodities.add(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
			allRelevantCommodities.add(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);	
			allRelevantCommodities.add(AncillaryCommodity.NATURALGASPOWEREXTERNAL);
			allRelevantCommodities.add(AncillaryCommodity.PVACTIVEPOWERFEEDIN);
			allRelevantCommodities.add(AncillaryCommodity.REACTIVEPOWEREXTERNAL);

			for (AncillaryCommodity vc : allRelevantCommodities) {

				//TODO: Fix
				if (vc == AncillaryCommodity.ACTIVEPOWEREXTERNAL){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.ACTIVEPOWEREXTERNAL, activePowerPrice);
					this.currentPriceSignal.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, newSignal);
				}
				else if (vc == AncillaryCommodity.REACTIVEPOWEREXTERNAL){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.REACTIVEPOWEREXTERNAL, reactivePowerPrice);
					this.currentPriceSignal.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, newSignal);
				}
				else if (vc == AncillaryCommodity.NATURALGASPOWEREXTERNAL){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.NATURALGASPOWEREXTERNAL, naturalGasPowerPrice);
					this.currentPriceSignal.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, newSignal);
				}
				else if (vc == AncillaryCommodity.PVACTIVEPOWERFEEDIN){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.PVACTIVEPOWERFEEDIN, activePowerFeedInPV);
					this.currentPriceSignal.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, newSignal);
				}
				else if (vc == AncillaryCommodity.CHPACTIVEPOWERFEEDIN){
					PriceSignal newSignal = generatePriceSignal(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, activePowerFeedInCHP);
					this.currentPriceSignal.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, newSignal);
				}
			}

			// EPS
			EpsComExchange ex = new EpsComExchange(
					this.getDeviceID(), 
					now, 
					currentPriceSignal,
					true);
			this.notifyComManager(ex);
			
			lastSignalSent = now;
		}

		newSignalReceived = false;
	}



	@Override
	public void updateDataFromComManager(ICALExchange hx) {
		//NOTHING
	}


	private PriceSignal generatePriceSignal(AncillaryCommodity commodity, double price) {
		PriceSignal priceSignal = null;


		if ( currentPriceSignal == null ) {
			// initial price signal
			long now = getTimer().getUnixTime();
			long timeSinceMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(now);
			long timeTillEndOfDay = 86400 - timeSinceMidnight;
			long additionalTime = signalAvailableFor;

			priceSignal = PriceSignalGenerator.getConstantPriceSignal(
					commodity, 
					now, 
					now + timeTillEndOfDay + additionalTime, 
					signalConstantPeriod, 
					price);

		}
		else {
			// generate every 12 hours
			long now = getTimer().getUnixTime();

			priceSignal = PriceSignalGenerator.getConstantPriceSignal(
					commodity, 
					now, 
					now + signalPeriod, 
					signalConstantPeriod, 
					price);
		}

		return priceSignal;
	}


}
