package osh.driver.simulation;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.simulation.thermal.ThermalDemandData;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.HotWaterDemandObserverExchange;
import osh.hal.exchange.prediction.WaterDemandPredictionExchange;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Sebastian Kramer, Ingo Mauser
 *
 */
public abstract class ThermalDemandSimulationDriver 
							extends DeviceSimulationDriver {
	
	private String inputSourceFile;
	private ThermalDemandData demandData;

	private int pastDaysPrediction;
	private float weightForOtherWeekday;
	private float weightForSameWeekday;
	
	private Commodity hotWaterType;
	
	private boolean log = false;
	
	private double[][] avgWeekDayLoad;
	private int[][] avgWeekDayLoadCounter;
	
	private double[] avgDayLoad;
	private int[] avgDayLoadCounter;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public ThermalDemandSimulationDriver(IOSH controllerbox,
			UUID deviceID, OSHParameterCollection driverConfig,
			Commodity hotWaterType)
			throws SimulationSubjectException, HALException {
		super(controllerbox, deviceID, driverConfig);
		
		this.inputSourceFile = driverConfig.getParameter("sourcefile");
		if (this.inputSourceFile == null) {
			throw new SimulationSubjectException("Parameter for Thermal ESHL Simulation missing!");
		}
		
		try {
			this.demandData = new ThermalDemandData(inputSourceFile, hotWaterType);
		} 
		catch (IOException e) {
			throw new SimulationSubjectException(e);
		}
		
		try {
			this.pastDaysPrediction = Integer.valueOf(driverConfig.getParameter("pastDaysPrediction"));
		}
		catch (Exception e) {
			this.pastDaysPrediction = 14;
			getGlobalLogger().logWarning("Can't get pastDaysPrediction, using the default value: " + this.pastDaysPrediction);
		}
		
		try {
			this.weightForOtherWeekday = Float.valueOf(driverConfig.getParameter("weightForOtherWeekday"));
		}
		catch (Exception e) {
			this.weightForOtherWeekday = 1.0f;
			getGlobalLogger().logWarning("Can't get weightForOtherWeekday, using the default value: " + this.weightForOtherWeekday);
		}
		
		try {
			this.weightForSameWeekday = Float.valueOf(driverConfig.getParameter("weightForSameWeekday"));
		}
		catch (Exception e) {
			this.weightForSameWeekday = 5.0f;
			getGlobalLogger().logWarning("Can't get weightForSameWeekday, using the default value: " + this.weightForSameWeekday);
		}
		
		this.hotWaterType = hotWaterType;
	}
	
	
	@Override
	public void onSimulationIsUp() throws SimulationSubjectException {
		super.onSimulationIsUp();
		//initially give LocalObserver load data of past days
		long startTime = getTimer().getUnixTimeAtStart();
		
		List<SparseLoadProfile> predictions = new LinkedList<SparseLoadProfile>();
		
		//starting in reverse so that the oldest profile is at index 0 in the list
		for (int i = pastDaysPrediction; i >= 1; i--) {
			int day = (int) Math.floorMod((startTime / 86400 - i), 365);

			predictions.add(demandData.getProfileForDayOfYear(day).getProfileWithoutDuplicateValues());
		}
		
		WaterDemandPredictionExchange _ox = new WaterDemandPredictionExchange(this.getDeviceID(), getTimer().getUnixTime(), 
				predictions, pastDaysPrediction, weightForOtherWeekday, weightForSameWeekday);
		this.notifyObserver(_ox);
		
		if (DatabaseLoggerThread.isLogHotWater()) {
			avgWeekDayLoad = new double[7][1440];
			avgWeekDayLoadCounter = new int[7][1440];
			
			for (int i = 0; i < avgWeekDayLoadCounter.length; i++) {
				Arrays.fill(avgWeekDayLoad[i], 0.0);
				Arrays.fill(avgWeekDayLoadCounter[i], 0);
			}
			
			int daysInYear = TimeConversion.getNumberOfDaysInYearFromTimeStamp(startTime);
			avgDayLoad = new double[daysInYear];
			avgDayLoadCounter = new int[daysInYear];
			Arrays.fill(avgDayLoad, 0.0);
			Arrays.fill(avgDayLoadCounter, 0);
			
			log = true;
		}
	};


	@Override
	public void onNextTimeTick() {
		OSHRandomGenerator ownGen = new OSHRandomGenerator(new Random(getRandomGenerator().getNextLong()));
		
		int randomHourShift = 2; // % 2 == 0
		
		// get new values
		long now = getTimer().getUnixTime();
		if (now % 3600 == 0) {
//			double demand = 0;
			int randomNumber = ownGen.getNextInt(randomHourShift + 1); // randomHourShift + 1 exclusive!! --> max == randomHourShift
			double demand = (0.5 + ownGen.getNextDouble()) * demandData.getTotalThermalDemand(now, randomNumber, randomHourShift);
//			demand += 0.25 * demandData.getTotalThermalDemand(now - 3600, 0, 0);
//			demand += 0.5 * demandData.getTotalThermalDemand(now, 0, 0);
//			demand += 0.25 * demandData.getTotalThermalDemand(now + 3600, 0, 0);
			
			// demand: month correction
			demand = demand * getMonthlyCorrection(TimeConversion.convertUnixTime2MonthInt(now));
			
			// demand: day of week correction
			demand = demand * getDayOfWeekCorrection(TimeConversion.convertUnixTime2CorrectedWeekdayInt(now));
			
			// demand: general correction value
			demand = demand * getGeneralCorrection();
			
			this.setPower(hotWaterType, (int) Math.round(demand));
		
			HotWaterDemandObserverExchange ox = 
					new HotWaterDemandObserverExchange(
							getDeviceID(), 
							now, 
							(int) demand);
			this.notifyObserver(ox);
		}
		
		if (log) {
			int power = this.getPower(hotWaterType);
			int weekDay = TimeConversion.convertUnixTime2CorrectedWeekdayInt(now);
			int minute = TimeConversion.convertUnixTime2MinuteOfDay(now);
			int dayOfYear = TimeConversion.convertUnixTime2CorrectedDayOfYear(now);
			avgWeekDayLoad[weekDay][minute] += power;
			avgWeekDayLoadCounter[weekDay][minute]++;
			avgDayLoad[dayOfYear] += power;
			avgDayLoadCounter[dayOfYear]++;
		}
	}
	
	@Override
	public void onSystemShutdown() throws OSHException {
		super.onSystemShutdown();

		if (getOSH().getOSHstatus().isSimulation() && log) {
			
			for (int d0 = 0; d0 < avgWeekDayLoad.length; d0++) {
				for (int d1 = 0; d1 < avgWeekDayLoad[d0].length; d1++) {
					double factor = (((double) avgWeekDayLoadCounter[d0][d1]) / 60.0) * 3600000.0;
					avgWeekDayLoad[d0][d1] = avgWeekDayLoad[d0][d1] / factor;
				}
			}
			
			for (int d0 = 0; d0 < avgDayLoad.length; d0++) {
				double factor = (((double) avgDayLoadCounter[d0]) / 86400.0) * 3600000.0;				
				avgDayLoad[d0] = avgDayLoad[d0] / factor;
			}

			DatabaseLoggerThread.enqueueHotWater(avgWeekDayLoad, avgDayLoad, hotWaterType);
		}
	}

	
	protected abstract double getGeneralCorrection();

	protected abstract double getDayOfWeekCorrection(int convertUnixTime2CorrectedWeekdayInt);

	protected abstract double getMonthlyCorrection(int convertUnixTime2MonthInt);
	
	@Override
	public void performNextAction(SubjectAction nextAction) {
		//NOTHING
	}
}
