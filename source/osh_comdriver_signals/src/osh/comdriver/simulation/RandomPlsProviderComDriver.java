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
import osh.datatypes.limit.PowerLimitSignal;
import osh.hal.exchange.PlsComExchange;
import osh.simulation.exception.SimulationSubjectException;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class RandomPlsProviderComDriver extends CALComDriver {

	private EnumMap<AncillaryCommodity,PowerLimitSignal> powerLimitSignals = new EnumMap<AncillaryCommodity, PowerLimitSignal>(AncillaryCommodity.class);	
	
	/** Time after which a signal is send */
	private int newSignalAfterThisPeriod = 12 * 60 * 60;
	/** Timestamp of the last price signal sent to global controller */
	private long lastSignalSent = 0L;
	/** Maximum time the signal is available in advance (36h) */
	private int signalPeriod = 36 * 60 * 60;
	
	private long gaussianTimeMu = 10 * 60; //changes every 10m on avaerage
	private long gaussianTimeSigma = 5 * 60; // 68-95-99.7% of all values will be within 1/2/3x mu +/- sigma
	
	private AncillaryCommodity[] limitsToSet = {
			AncillaryCommodity.ACTIVEPOWEREXTERNAL,
			AncillaryCommodity.REACTIVEPOWEREXTERNAL,
	};
	
	private int[][] limitsGaussianMu = {
			{3000, -3000},
			{2000, -2000},
	};
	
	private int[] limitsGaussianSigma = {
			500,
			300
	};

	
	/**
	 * CONSTRUCTOR
	 * @param controllerbox
	 * @param deviceID
	 * @param driverConfig
	 * @throws SimulationSubjectException
	 */
	public RandomPlsProviderComDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException {
		super(controllerbox, deviceID, driverConfig);		

	}
	
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		OSHRandomGenerator rand = new OSHRandomGenerator(new Random(getRandomGenerator().getNextLong()));
		generateLimitSignal(rand);
		long now = getTimer().getUnixTime();
		this.lastSignalSent = now;
		
		PlsComExchange ex = new PlsComExchange(
				this.getDeviceID(), 
				now, 
				powerLimitSignals);
		this.notifyComManager(ex);
		
		// register
		this.getTimer().registerComponent(this, 1);
	}
	
	@Override
	public void onNextTimePeriod() throws OSHException {
		super.onNextTimePeriod();
		
		long now = getTimer().getUnixTime();
		OSHRandomGenerator rand = new OSHRandomGenerator(new Random(getRandomGenerator().getNextLong()));
		
		if ((now - lastSignalSent) >= newSignalAfterThisPeriod) {
			generateLimitSignal(rand);
			
			lastSignalSent = now;
			
			PlsComExchange ex = new PlsComExchange(
					this.getDeviceID(), 
					now, 
					powerLimitSignals);
			this.notifyComManager(ex);
		}		
	}

	
	private void generateLimitSignal(OSHRandomGenerator randomGen) {

		long now = getTimer().getUnixTime();

		for (int i = 0; i < limitsToSet.length; i++) {
			AncillaryCommodity ac = limitsToSet[i];

			PowerLimitSignal pls = new PowerLimitSignal();
			long time = now - 1;
			int upperLimit = -1;
			int lowerLimit = 1;
			while (upperLimit < 1) {
				upperLimit = (int) Math.round((randomGen.getNextGaussian() * limitsGaussianSigma[i]) + limitsGaussianMu[i][0]);
			}
			while (lowerLimit > -1) {
				lowerLimit = (int) Math.round((randomGen.getNextGaussian() * limitsGaussianSigma[i]) + limitsGaussianMu[i][1]);
			}
			pls.setPowerLimit(time, upperLimit, lowerLimit);
			
			while (time < now + signalPeriod) {
				//new time
				long additional =  -1;
				
				while (additional < 1) {
					additional = Math.round((randomGen.getNextGaussian() * gaussianTimeSigma) + gaussianTimeMu);
				}
				time += additional;
				
				upperLimit = -1;
				lowerLimit = 1;
				while (upperLimit < 1) {
					upperLimit = (int) Math.round((randomGen.getNextGaussian() * limitsGaussianSigma[i]) + limitsGaussianMu[i][0]);
				}
				while (lowerLimit > -1) {
					lowerLimit = (int) Math.round((randomGen.getNextGaussian() * limitsGaussianSigma[i]) + limitsGaussianMu[i][1]);
				}
				
				pls.setPowerLimit(time, upperLimit, lowerLimit);
			}
			System.out.println(pls.getLimits());
			
			pls.setKnownPowerLimitInterval(now, now + signalPeriod);

			powerLimitSignals.put(ac, pls);
		}
	}


	@Override
	public void updateDataFromComManager(ICALExchange exchangeObject) {
		//NOTHING
	}

}
