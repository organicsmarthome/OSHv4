package osh.driver;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.simulation.thermal.VDI6002DomesticHotWaterStatistics;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.HotWaterDemandObserverExchange;
import osh.hal.exchange.prediction.VDI6002WaterDemandPredictionExchange;
import osh.simulation.exception.SimulationSubjectException;
import osh.utils.csv.CSVImporter;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Sebastian Kramer, Ingo Mauser, Jan Mueller
 *
 */
public class VDI6002DomesticHotWaterDriver extends HALDeviceDriver {
	
	private String weekDayHourProbabilitiesFile;
	//d0 = hour, d1 = weekday
	private double[][] weekDayHourProbabilities;
	private double[][] cumulativeWeekDayHourProbabilities;
	
	private String drawOffTypesFile;
	//d0 = hour, d1 = weekday
	private double[][] drawOffTypes;
	private double[] cumulativeProfileProbabilities;
	
	private double avgYearlyRuns;
	
	private double avgYearlyDemand;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	private SparseLoadProfile dayProfile = null;
	private int lastSentValue = Integer.MIN_VALUE;
	
	private int[] profileLength;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public VDI6002DomesticHotWaterDriver(
			IOSH osh, 
			UUID deviceID,
			OSHParameterCollection driverConfig) throws HALException, SimulationSubjectException {
		super(osh, deviceID, driverConfig);
		
		this.drawOffTypesFile = driverConfig.getParameter("drawOffTypesFile");
		if (this.drawOffTypesFile == null) {
			throw new SimulationSubjectException("Parameter for Thermal VDI6002 Simulation missing!");
		}
		
		this.weekDayHourProbabilitiesFile = driverConfig.getParameter("weekDayHourProbabilitiesFile");
		if (this.drawOffTypesFile == null) {
			throw new SimulationSubjectException("Parameter for Thermal VDI6002 Simulation missing!");
		}	
		
		try {
			this.avgYearlyDemand = Double.parseDouble(getDriverConfig().getParameter("avgYearlyDemamd"));
		}
		catch (Exception e) {
			this.avgYearlyDemand = 700;
			getGlobalLogger().logWarning("Can't get avgYearlyDemamd, using the default value: " + this.avgYearlyDemand);
		}
		
		try {
			this.compressionType = LoadProfileCompressionTypes.valueOf(getDriverConfig().getParameter("compressionType"));
		}
		catch (Exception e) {
			this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
			getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
		}
		
		try {
			this.compressionValue = Integer.valueOf(getDriverConfig().getParameter("compressionValue"));
		}
		catch (Exception e) {
			this.compressionValue = 100;
			getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
		}
		
		weekDayHourProbabilities = CSVImporter.readAndTransposeDouble2DimArrayFromFile(weekDayHourProbabilitiesFile, ";");
		drawOffTypes = CSVImporter.readAndTransposeDouble2DimArrayFromFile(drawOffTypesFile, ";");
		
		generateRunsAndProbabilities();
	}
	
	
	private void generateRunsAndProbabilities() {
		
		double[] sumOfDrawOffProfiles = new double[drawOffTypes.length];
		double[] profileProbabilities = new double[drawOffTypes.length];		
		
		//calculate the total kWh of every drawOffType
		for (int d0 = 0; d0 < sumOfDrawOffProfiles.length; d0++) {
			sumOfDrawOffProfiles[d0] = Arrays.stream(drawOffTypes[d0]).sum() / 3600.0;
			profileProbabilities[d0] = 1 / sumOfDrawOffProfiles[d0];
		}
		
		double nonNormalizedProbabilitySum = Arrays.stream(profileProbabilities).sum(); 
		
		//probability for every profile is the normalized inverted sum of energy usage
		for (int d0 = 0; d0 < sumOfDrawOffProfiles.length; d0++) {
			profileProbabilities[d0] = profileProbabilities[d0] / nonNormalizedProbabilitySum;
		}
		
		double avgEnergySumPerRun = 0.0;
		for (int d0 = 0; d0 < sumOfDrawOffProfiles.length; d0++) {
			avgEnergySumPerRun += sumOfDrawOffProfiles[d0] * profileProbabilities[d0];
		}
		cumulativeProfileProbabilities = new double[profileProbabilities.length];
		
		for (int d0 = 0; d0 < profileProbabilities.length; d0++) {
				if (d0 == 0)				
					cumulativeProfileProbabilities[d0] = profileProbabilities[d0];
				else if (d0 == profileProbabilities.length - 1)
					cumulativeProfileProbabilities[d0] = 1;
				else
					cumulativeProfileProbabilities[d0] = cumulativeProfileProbabilities[d0 - 1] + profileProbabilities[d0];
		}
		
		
		avgYearlyRuns = avgYearlyDemand / avgEnergySumPerRun;
		
		cumulativeWeekDayHourProbabilities = new double[weekDayHourProbabilities.length][weekDayHourProbabilities[0].length];
		
		for (int d0 = 0; d0 < weekDayHourProbabilities.length; d0++) {
			for (int d1 = 0; d1 < weekDayHourProbabilities[d0].length; d1++) {
				if (d1 == 0)				
					cumulativeWeekDayHourProbabilities[d0][d1] = weekDayHourProbabilities[d0][d1];
				else if (d1 == weekDayHourProbabilities[d0].length - 1)
					cumulativeWeekDayHourProbabilities[d0][d1] = 1;
				else
					cumulativeWeekDayHourProbabilities[d0][d1] = cumulativeWeekDayHourProbabilities[d0][d1 - 1] + weekDayHourProbabilities[d0][d1];
			}
		}
		
		profileLength = new int[drawOffTypes.length];
		
		for (int d0 = 0; d0 < drawOffTypes.length; d0++) {
			int lastValue = 0;			
			for (int d1 = 0; d1 < drawOffTypes[d0].length; d1++) {
				if (drawOffTypes[d0][d1] != 0)
					lastValue = d1;
			}
			profileLength[d0] = lastValue + 1;
		}
	}
	
	@Override
	public void onSystemIsUp() throws OSHException {
		super.onSystemIsUp();
		
		getTimer().registerComponent(this, 1);
		
		//initially give LocalObserver load data of past days
		VDI6002WaterDemandPredictionExchange _pred = new VDI6002WaterDemandPredictionExchange(
				getDeviceID(), 
				getTimer().getUnixTime(), 
				VDI6002DomesticHotWaterStatistics.monthlyCorrection,
				VDI6002DomesticHotWaterStatistics.dayOfWeekCorrection, 
				weekDayHourProbabilities, 
				avgYearlyDemand);		
		this.notifyObserver(_pred);
		
		StaticCompressionExchange _stat = new StaticCompressionExchange(getDeviceID(), getTimer().getUnixTime(), compressionType, compressionValue);
		this.notifyObserver(_stat);
	};

	@Override
	public void onNextTimePeriod() {
		
		long now = getTimer().getUnixTime();
		
		if (dayProfile == null || now % 86400 == 0) {
			if (dayProfile == null)
				dayProfile = new SparseLoadProfile();
			long initialNumber = getRandomGenerator().getNextLong();
			OSHRandomGenerator newRandomGen = new OSHRandomGenerator(new Random(initialNumber));
			generateDailyDemandProfile(now, newRandomGen);
		}			
		
		int power = dayProfile.getLoadAt(Commodity.DOMESTICHOTWATERPOWER, now);
		
		if (power != lastSentValue) {
			
			this.setPower(Commodity.DOMESTICHOTWATERPOWER, power);
			
			HotWaterDemandObserverExchange ox = 
					new HotWaterDemandObserverExchange(
							getDeviceID(), 
							now, 
							power);
			this.notifyObserver(ox);
			
			lastSentValue = power;
		}
	}
	
	
	private void generateDailyDemandProfile(long now, OSHRandomGenerator randomGen) {		
		
		int month = TimeConversion.convertUnixTime2MonthInt(now);
		int weekDay = TimeConversion.convertUnixTime2CorrectedWeekdayInt(now);
		long midnightToday = TimeConversion.getUnixTimeStampCurrentDayMidnight(now);
		
		int runsToday;
		double avgRunsToday = (avgYearlyRuns / 365.0) * VDI6002DomesticHotWaterStatistics.monthlyCorrection[month] * VDI6002DomesticHotWaterStatistics.dayOfWeekCorrection[weekDay];
		int runsFloor = (int) Math.floor(avgRunsToday);
		int runsCeil = (int) Math.ceil(avgRunsToday);
		
		if (randomGen.getNextDouble() < (avgRunsToday - Math.floor(avgRunsToday)))
			runsToday = runsCeil;
		else
			runsToday = runsFloor;
		
		SparseLoadProfile[] newDayProfiles = new SparseLoadProfile[runsToday];
		
//		boolean lastDay = (getTimer().getUnixTime() - getTimer().getUnixTimeAtStart()) / 86400 
//				== (getSimulationEngine().getSimulationDuration() / 86400 - 1);
		
		boolean lastDay = false;
		
		for (int i = 0; i < runsToday; i++) {
			
			newDayProfiles[i] = new SparseLoadProfile();
			
			double randomProfileNumber = randomGen.getNextDouble();
			int profileID = 0;
			
			for (int d0 = 0; d0 < cumulativeProfileProbabilities.length; d0++) {
				if (randomProfileNumber < cumulativeProfileProbabilities[d0]) {
					profileID = d0;
					break;
				}
			}
			//get random hour to start drawOff based on provided hour probabilities
			int hour = getRandomHourBasedOnProbabilities(randomGen, weekDay);
			//
			
			//last day should not schedule anything longer then the day
			if (lastDay && hour == 23)
				hour = 22;
			
			int randomSeconds = (int) (randomGen.getNextDouble() * 3600.0);
			
			//start of draw
			long startOfDrawOff = midnightToday + hour * 3600 + randomSeconds;
			
			//correct start time by half of the profile length (only when we not shift drawoff to the past)
			if (hour * 3600 + randomSeconds >= profileLength[profileID])
				startOfDrawOff -= Math.round(((double) profileLength[profileID]) * 0.5);
			
			for (int d1 = 0; d1 < profileLength[profileID]; d1++) {
				//profile is in kW, we use W
				newDayProfiles[i].setLoad(Commodity.DOMESTICHOTWATERPOWER, startOfDrawOff + d1, (int) Math.round(drawOffTypes[profileID][d1] * 1000.0));
			}
			newDayProfiles[i].setLoad(Commodity.DOMESTICHOTWATERPOWER, startOfDrawOff + profileLength[profileID], 0);
			newDayProfiles[i].setEndingTimeOfProfile(startOfDrawOff + profileLength[profileID] + 1);
		}
		//maybe loads are scheduled into this day (due to the randomSeconds deviation so merge them to be sure
		dayProfile = dayProfile.cloneAfter(midnightToday);
		
		for (int i = 0; i < runsToday; i++) {
			dayProfile = dayProfile.merge(newDayProfiles[i], 0);
		}		
		
		//making sure the profile is long enough
		dayProfile.setEndingTimeOfProfile(midnightToday + 86400 + 3600);
		//remove duplicate Values
//		dayProfile = dayProfile.getCompressedProfileByDiscontinuities(1);
		dayProfile = dayProfile.getProfileWithoutDuplicateValues();
//		SparseLoadProfile control = dayProfile.getCompressedProfileByDiscontinuities(1);
	}
	
	private int getRandomHourBasedOnProbabilities(OSHRandomGenerator randomGen, int weekDay) {
		double randomNumber = randomGen.getNextDouble();
		int hour = 0;
		for (int d1 = 0; d1 < cumulativeWeekDayHourProbabilities[weekDay].length; d1++) {
			if (cumulativeWeekDayHourProbabilities[weekDay][d1] > randomNumber){
				hour = d1;
				break;
			}
		}
		return hour;		
	}

	@Override
	protected void onControllerRequest(HALControllerExchange controllerRequest) throws HALException {
		//NOTHING
	}

}
