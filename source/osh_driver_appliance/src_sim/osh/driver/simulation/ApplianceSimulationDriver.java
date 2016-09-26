package osh.driver.simulation;

import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.ScreenplayType;
import osh.utils.csv.CSVImporter;
import osh.utils.slp.IH0Profile;
import osh.utils.string.StringConversions;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Ingo Mauser
 *
 */
public abstract class ApplianceSimulationDriver 
							extends DeviceSimulationDriver {
	
	// screenplayType specific variables
		// screenplayType = DYNAMIC
	
		// DYNAMIC SCREENPLAY
	
		/** Number of avg daily runs for screenplay generation */
		private double avgYearlyRuns;
		
		/** Correction factor for different probabilities per day (get it from H0) */
		private double[] correctionFactorDay;
		
		/** Probability for run at a specific time of the day <br>
		 *  [d0] = weekday, [d1] = hour of day */
		private double[][] probabilityPerHourOfWeekday;
		
		/** Probability for run at a specific time of the day <br>
		 *  [d0] = weekday, [d1] = hour of day */
		private double[][] probabilityPerHourOfWeekdayCumulativeDistribution;
		
		/** Calculated probabilities per day */
		private double avgDailyRuns;
		
		/** Shares of configurations for screenplay generation */
		private Double[] configurationShares;
			
		
	// TEMPORAL DEGREE OF FREEDOM
	
	/** Max 1stTemporalDoF in ticks for generation of TDoF (initial optimization) */
	private int deviceMax1stTDof;
	
	/** Max 2ndTemporalDoF in ticks for generation of TDoF (rescheduling) */
	@SuppressWarnings("unused")
	@Deprecated
	private int deviceMax2ndTDof;
		
		
	/**
	 * CONSTRUCTOR
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ApplianceSimulationDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		// if DeviceClassification.APPLIANCE (but info at this point not yet available!)
		// all conditions after first && should NOT be necessary (but remain for safety reasons)
		if ( driverConfig.getParameter("screenplaytype") != null ) {
			
			ScreenplayType screenplayType = ScreenplayType.fromValue(driverConfig.getParameter("screenplaytype"));
			
			if (screenplayType == ScreenplayType.STATIC) {
				// screenplay is loaded from file...
			}
			else if (screenplayType == ScreenplayType.DYNAMIC) {
				// TEMPORAL DEGREE OF FREEDOM
				{
					// max 1st tDoF
					String deviceMax1stDofString = driverConfig.getParameter("devicemax1stdof");
					if (deviceMax1stDofString != null) {
						this.deviceMax1stTDof = Integer.valueOf(deviceMax1stDofString);
					}
					else {
						throw new RuntimeException("variable \"screenplaytype\" = DYNAMIC : missing parameter (devicemax1stdof)!");
					}
				}
				{
					// average yearly runs for dynamic daily screenplay
					String avgYearlyRunsString = driverConfig.getParameter("averageyearlyruns");
					if (avgYearlyRunsString != null) {
						this.avgYearlyRuns = Double.valueOf(avgYearlyRunsString);
					}
					else {
						throw new RuntimeException("Parameter missing: averageyearlyruns");
					}
				}
				
				IH0Profile h0Profile = null;
				
				// H0-PROFILE
				{
					String h0ProfileFileName = driverConfig.getParameter("h0filename");
					String h0ProfileClass = driverConfig.getParameter("h0classname");
					if (h0ProfileFileName != null) {
						try {
							Class h0Class = Class.forName(h0ProfileClass);
							
							h0Profile = (IH0Profile) h0Class.getConstructor(int.class, String.class, double.class)
								.newInstance(TimeConversion.convertUnixTime2Year(getTimer().getUnixTime()),
										h0ProfileFileName,
										1000);
							
						} catch (Exception ex) {
							throw new RuntimeException(ex);
						}
						
						this.correctionFactorDay = h0Profile.getCorrectionFactorForDay();
					}
					else {
						throw new RuntimeException("variable \"screenplaytype\" = DYNAMIC : missing parameter (h0filename)!");
					}
				}
				// Probability per hour of weekday
				{
					String probabiltyFileName = driverConfig.getParameter("probabilityfilename");
					if (probabiltyFileName != null) {
						double[][] probabilityPerHourOfWeekdayTemp = CSVImporter.readDouble2DimArrayFromFile(probabiltyFileName, ";");
						this.probabilityPerHourOfWeekday = new double[7][24];
						// transpose (d0 <-> d1)
						for (int d0 = 0; d0 < probabilityPerHourOfWeekdayTemp.length; d0++) {
							for (int d1 = 0; d1 < probabilityPerHourOfWeekdayTemp[d0].length; d1++) {
								this.probabilityPerHourOfWeekday[d1][d0] = probabilityPerHourOfWeekdayTemp[d0][d1];
							}
						}
						//calculate cumulative distribution
						this.probabilityPerHourOfWeekdayCumulativeDistribution = new double[probabilityPerHourOfWeekday.length][];
						for (int d0 = 0; d0 < probabilityPerHourOfWeekday.length; d0++) {
							this.probabilityPerHourOfWeekdayCumulativeDistribution[d0] = new double[probabilityPerHourOfWeekday[d0].length];
							
							double temp = 0;
							for (int d1 = 0; d1 < probabilityPerHourOfWeekday[d0].length; d1++) {
								if (d1 == probabilityPerHourOfWeekday[d0].length - 1) {
									temp = 1;
								}
								else {
									temp = temp + probabilityPerHourOfWeekday[d0][d1];
								}
								this.probabilityPerHourOfWeekdayCumulativeDistribution[d0][d1] = temp;
							}
						}
					}
					else {
						throw new RuntimeException("variable \"screenplaytype\" = DYNAMIC : missing parameter (probabiltyFileName)!");
					}
				}
				
				{
					// shares of different program configurations
					String configurationsharesString = driverConfig.getParameter("configurationshares");
					if (configurationsharesString != null) {
						this.configurationShares = StringConversions.fromStringToDoubleArray(configurationsharesString);
					}
					else {
						throw new RuntimeException("Parameter missing: configurationshares");
					}
				}
				
				// calculate average daily runs
				this.avgDailyRuns = (avgYearlyRuns / 365.0);
			}
			else {
				throw new RuntimeException("variable \"screenplaytype\" " + screenplayType + " : not implemented!");
			}
		}
	}
	
	
	/**
	 * Get random hour of day according to specific probability
	 * . Used for dynamic screenplay<br>
	 */
	private int getRandomHourToRunBasedOnProbabilityMap(
			long timestamp, 
			OSHRandomGenerator randomGen) throws OSHException {
		double randomDouble = randomGen.getNextDouble();
		int weekday = TimeConversion.convertUnixTime2CorrectedWeekdayInt(timestamp);
		int hour = 0;
		double[] probability = probabilityPerHourOfWeekdayCumulativeDistribution[weekday];
		for (int i = 0; i < probability.length; i++) {
			if (probability[i] > randomDouble) {
				hour = i;
				break;
			}
		}
		return hour;
	}
	
	
	/**
	 * Get random starting time of device
	 */
	public long getRandomTimestampForRunToday(
			long timestamp, 
			int middleOfPowerConsumption, 
			double randomValue, 
			OSHRandomGenerator randomGen) throws OSHException {
		int randomHour = getRandomHourToRunBasedOnProbabilityMap(timestamp, randomGen);
		
		double deviation = randomValue;
		if (randomGen.getNextBoolean()) {
			deviation = (-1) * deviation;
		}
		double deviatedHour = randomHour + 0.5 + deviation - (((double) middleOfPowerConsumption) / 3600.0);
		if (deviatedHour < 0) {
			deviatedHour = deviatedHour + 24;
		}
		else if (deviatedHour >= 24) {
			deviatedHour = deviatedHour - 24;
		}
		long startTime = (long) (getTimer().getUnixTime()  + deviatedHour * 3600.0);
		return startTime;
	}
	
	
	/**
	 * has to be implemented by every device...
	 */
	protected abstract void generateDynamicDailyScreenplay() throws OSHException;

	
	/**
	 * in case of use please override
	 */
	protected abstract int generateNewDof(boolean useRandomDof, int noActions, long applianceActionTimeTick, OSHRandomGenerator randomGen, int maxDof, int maxPossibleDof);

	
	@Override
	public final void triggerSubject() {
		super.triggerSubject();
		
		//if dynamic screenplay then generate daily screenplay
		if (getSimulationEngine().getScreenplayType() == ScreenplayType.DYNAMIC) {
			//FIXME: this can go very wrong, because the step size can be greater than 1
			if (getTimer().getUnixTime() % 86400 == 0) {
				try {
					generateDynamicDailyScreenplay();
				} 
				catch (OSHException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * A: Please don't ask why it works...but it should be fine!<br>
	 * B: No, it did not work, now it does ...
	 */
	protected int calcMax1stTDof(int actionCount, int availableTime, int maxProgramDuration) {
		int maxDof = getDeviceMax1stDof();
		
		// + 100 to be safe...		
		while (Math.floor(((double) availableTime) / (double)(maxDof + maxProgramDuration + 100)) < actionCount) {
			maxDof = (int) ((double) maxDof * 0.9);
			
			//no dof can be found, we need to rely on our run correction
			if (maxDof < 10) {
				return 0;
			}
		}		
		
		// run on last day immediately
		if ((getTimer().getUnixTime() - getTimer().getUnixTimeAtStart()) / 86400 
				== (getSimulationEngine().getSimulationDuration() / 86400 - 1)) {
			maxDof = 0;
		}
		
		return maxDof;
	}

	// used only within this class
	protected int getDeviceMax1stDof() {
		return deviceMax1stTDof;
	}

	// used by MieleAppliance
	protected double getAvgYearlyRuns() {
		return avgYearlyRuns;
	}
	
	// used by FutureAppliance
	protected double getAvgDailyRuns() {		
		return avgDailyRuns;
	}
	
	// used by FutureAppliance
	protected Double[] getConfigurationShares() {
		return configurationShares;
	}
	
	// used by FutureAppliance
	protected double[] getCorrectionFactorDay() {
		return correctionFactorDay;
	}

}
