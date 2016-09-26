package osh.comdriver.simulation;

import java.util.EnumMap;
import java.util.Random;
import java.util.UUID;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.hal.exchange.EpsComExchange;
import osh.simulation.exception.SimulationSubjectException;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class RandomEpsProviderComDriver extends CALComDriver {

	
	private EnumMap<AncillaryCommodity,PriceSignal> currentPriceSignal = new EnumMap<AncillaryCommodity, PriceSignal>(AncillaryCommodity.class);	
	
	/** Time after which a signal is send */
	private int newSignalAfterThisPeriod = 12 * 60 * 60;
	/** Timestamp of the last price signal sent to global controller */
	private long lastSignalSent = 0L;
	/** Maximum time the signal is available in advance (36h) */
	private int signalPeriod = 36 * 60 * 60;
//	/** Minimum time the signal is available in advance (24h) */
//	private int signalAvailableFor = 24 * 60 * 60;

	
	private long gaussianTimeMu = 10 * 60; //changes every 10m on avaerage
	private long gaussianTimeSigma = 5 * 60; // 68-95-99.7% of all values will be within 1/2/3x mu +/- sigma
	
	private AncillaryCommodity[] pricesToSet = {
			AncillaryCommodity.ACTIVEPOWEREXTERNAL,
			AncillaryCommodity.REACTIVEPOWEREXTERNAL,
			AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION,
			AncillaryCommodity.CHPACTIVEPOWERFEEDIN,
			AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION,
			AncillaryCommodity.PVACTIVEPOWERFEEDIN,
			AncillaryCommodity.NATURALGASPOWEREXTERNAL,
	};
	
	private double[] pricesGaussianMu = {
			30.0,
			10.0,
			10.0,
			8.0,
			5.0,
			10.0,
			6.0
	};
	
	private double[] pricesGaussianSigma = {
			10.0,
			2.0,
			2.5,
			1.5,
			1.0,
			2.0,
			1.0
	};
	
	
	public RandomEpsProviderComDriver(
			IOSH controllerbox, 
			UUID deviceID,
			OSHParameterCollection driverConfig) throws SimulationSubjectException {
		super(controllerbox, deviceID, driverConfig);
	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		OSHRandomGenerator rand = new OSHRandomGenerator(new Random(getRandomGenerator().getNextLong()));
		generatePriceSignal(rand);
		long now = getTimer().getUnixTime();
		this.lastSignalSent = now;
		
		// EPS
		EpsComExchange ex = new EpsComExchange(
				this.getDeviceID(), 
				now, 
				currentPriceSignal);
		this.notifyComManager(ex);
		
		// register
		this.getTimer().registerComponent(this, 1);			
	}
	
	@Override
	public void onNextTimePeriod() {
		
		long now = getTimer().getUnixTime();
		OSHRandomGenerator rand = new OSHRandomGenerator(
				new Random(getOSH().getRandomGenerator().getNextLong()));
		
		if ((now - lastSignalSent) >= newSignalAfterThisPeriod) {
			generatePriceSignal(rand);
			
			lastSignalSent = now;
			
			// EPS
			EpsComExchange ex = new EpsComExchange(
					this.getDeviceID(), 
					now, 
					currentPriceSignal);
			this.notifyComManager(ex);
		}
		
	}


	@Override
	public void updateDataFromComManager(ICALExchange hx) {
		//NOTHING
	}
	
	
	private void generatePriceSignal(OSHRandomGenerator randomGen) {

		long now = getTimer().getUnixTime();

		for (int i = 0; i < pricesToSet.length; i++) {
			AncillaryCommodity ac = pricesToSet[i];

			PriceSignal ps = new PriceSignal(ac);
			long time = now - 1;
			double price = -1;
			while (price < 1) {
				price = Math.round((randomGen.getNextGaussian() * pricesGaussianSigma[i]) + pricesGaussianMu[i]);
			}
			ps.setPrice(time, price);
			
			while (time < now + signalPeriod) {
				//new time
				long additional =  -1;
				
				while (additional < 1) {
					additional = Math.round((randomGen.getNextGaussian() * gaussianTimeSigma) + gaussianTimeMu);
				}
				time += additional;
				
				price = -1;
				while (price < 1) {
					price = Math.round((randomGen.getNextGaussian() * pricesGaussianSigma[i]) + pricesGaussianMu[i]);
				}
				ps.setPrice(time, price);
			}
			System.out.println(ps.getPrices());
			
			ps.setKnownPriceInterval(now, now + signalPeriod);

			currentPriceSignal.put(ac, ps);
		}
	}
	
	
}
