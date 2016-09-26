package osh.simulation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.power.PowerInterval;
import osh.utils.sql.SQLConnectionProvider;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class DatabaseLoggerThread extends Thread {
	
	 private static final String additionalMysqlOptions ="ROW_FORMAT=COMPRESSED";
	
	 private static String tableName;
	 private static String epsTableName;
	 private static String plsTableName;
	 private static String h0TableName;
	 private static String devicesTableName;
	 private static String baseloadTableName;
	 private static String hotWaterTableName;
	 private static String detailedPowerTableName;
	 private static String waterTankTableName;
	 private static String gaTableName;
	 private static String smartHeaterTableName;
	 private static String runName;
	 private static long startTime;
	 private static int[] preferredConnection;
	 private static Connection[] conn;
	 private static boolean running = true;
	 
	 private static boolean logDevices = false;
	 private static boolean logHotWater = false;
	 private static boolean logWaterTank = false;
	 private static boolean logGA = false;
	 private static boolean logSmartHeater = false; 

	public static void initLogger(String tableName, String runName, long startTime,
			int[] preferredConnection) {
		DatabaseLoggerThread.tableName = tableName;
		DatabaseLoggerThread.runName = runName;
		DatabaseLoggerThread.startTime = startTime;
		DatabaseLoggerThread.preferredConnection = preferredConnection;
		
		DatabaseLoggerThread.epsTableName = tableName + "_EPS";
		DatabaseLoggerThread.plsTableName = tableName + "_PLS";
		DatabaseLoggerThread.h0TableName = tableName + "_H0";
		DatabaseLoggerThread.devicesTableName = tableName + "_Devices";
		DatabaseLoggerThread.baseloadTableName = tableName + "_Baseload";
		DatabaseLoggerThread.hotWaterTableName = tableName + "_HotWater";
		DatabaseLoggerThread.detailedPowerTableName = tableName + "_DetailedPower";
		DatabaseLoggerThread.waterTankTableName = tableName + "_WaterTank";
		DatabaseLoggerThread.gaTableName = tableName + "_GA";
		DatabaseLoggerThread.smartHeaterTableName = tableName + "_SmartHeater";
		conn = new Connection[preferredConnection.length];
		
		DatabaseLoggerThread.logQueue = new LinkedBlockingQueue<QueueLogObject>();
		
//		trySetupConnection();
		
		new DatabaseLoggerThread().start();
	}
	
	public static void shutDown() {
		running = false;
	}
	
	private DatabaseLoggerThread() {
	} 
	
	private static void trySetupConnection(int preferredConnectionIndex) {
		try {
			conn[preferredConnectionIndex] = SQLConnectionProvider.getConnection(preferredConnection[preferredConnectionIndex]);
			if (conn == null) {
				throw new Exception("Connection is null, should not happen");
			}				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setupTable(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ tableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " Runtime BIGINT NOT NULL, "
				+ " StartTime BIGINT NOT NULL, "
				+ " LoggingStartRelative BIGINT NOT NULL, "
				+ " LoggingEndRelative BIGINT NOT NULL, "
				+ " ActivePowerConsumption DOUBLE NOT NULL, "
				+ " ActivePowerPV DOUBLE NOT NULL, "
				+ " ActivePowerPVAutoConsumption DOUBLE NOT NULL, "
				+ " ActivePowerPVFeedIn DOUBLE NOT NULL, "
				+ " ActivePowerCHP DOUBLE NOT NULL, "
				+ " ActivePowerCHPAutoConsumption DOUBLE NOT NULL, "
				+ " ActivePowerCHPFeedIn DOUBLE NOT NULL, "
				+ " ActivePowerBatteryCharging DOUBLE NOT NULL, "
				+ " ActivePowerBatteryDischarging DOUBLE NOT NULL, "
				+ " ActivePowerBatteryAutoConsumption DOUBLE NOT NULL, "
				+ " ActivePowerBatteryFeedIn DOUBLE NOT NULL, "
				+ " ActivePowerExternal DOUBLE NOT NULL, "
				+ " ReactivePowerExternal DOUBLE NOT NULL, "
				+ " GasPowerExternal DOUBLE NOT NULL, "
				+ " EpsCosts DOUBLE NOT NULL, "
				+ " PlsCosts DOUBLE NOT NULL, "
				+ " GasCosts DOUBLE NOT NULL, "
				+ " FeedInCostsPV DOUBLE NOT NULL, "
				+ " FeedInCostsCHP DOUBLE NOT NULL, "
				+ " AutoConsumptionCosts DOUBLE NOT NULL, "
				+ " TotalCosts DOUBLE NOT NULL, "
				+ " PRIMARY KEY (Runname, StartTime, LoggingStartRelative, LoggingEndRelative)"
				+ ")" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);
	}
	
	private static void setupDetailedPowerTable(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ detailedPowerTableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " AncillaryCommodity VARCHAR(565) NOT NULL, "
				+ " Time BIGINT NOT NULL, "
				+ " Power Int NOT NULL, "
				+ " PRIMARY KEY (RunName, AncillaryCommodity, Time ))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);
	}
	
	private static void setupEpsTable(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ epsTableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " VirtualCommodity VARCHAR(565) NOT NULL, "
				+ " Time BIGINT NOT NULL, "
				+ " Price DOUBLE NOT NULL, "
				+ " PRIMARY KEY (RunName, VirtualCommodity, Time ))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);
	}
	
	private static void setupPlsTable(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ plsTableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " VirtualCommodity VARCHAR(565) NOT NULL, "
				+ " Time BIGINT NOT NULL, "
				+ " LowerLimit DOUBLE NOT NULL, "
				+ " UpperLimit DOUBLE NOT NULL, "
				+ " PRIMARY KEY (RunName, VirtualCommodity, Time ))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);
	}	
	
	private static void setupH0Table(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ h0TableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " weekDay0 TEXT NOT NULL, "
				+ " weekDay1 TEXT NOT NULL, "
				+ " weekDay2 TEXT NOT NULL, "
				+ " weekDay3 TEXT NOT NULL, "
				+ " weekDay4 TEXT NOT NULL, "
				+ " weekDay5 TEXT NOT NULL, "
				+ " weekDay6 TEXT NOT NULL, "
				+ " days TEXT NOT NULL, "
				+ " PRIMARY KEY (RunName))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);		
	}
	
	private static void setupHotWaterTable(String hotWaterIdentifier, int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ hotWaterTableName + "_" + hotWaterIdentifier
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " weekDay0 TEXT NOT NULL, "
				+ " weekDay1 TEXT NOT NULL, "
				+ " weekDay2 TEXT NOT NULL, "
				+ " weekDay3 TEXT NOT NULL, "
				+ " weekDay4 TEXT NOT NULL, "
				+ " weekDay5 TEXT NOT NULL, "
				+ " weekDay6 TEXT NOT NULL, "
				+ " days TEXT NOT NULL, "
				+ " PRIMARY KEY (RunName))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);		
	}
	
	private static void setupDevicesTable(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ devicesTableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " startsDW INT DEFAULT 0, "				
				+ " startsIH INT DEFAULT 0, "
				+ " startsOV INT DEFAULT 0, "
				+ " startsTD INT DEFAULT 0, "
				+ " StartsWM INT DEFAULT 0, "
				+ " startsDWR INT DEFAULT 0, "				
				+ " startsIHR INT DEFAULT 0, "
				+ " startsOVR INT DEFAULT 0, "
				+ " startsTDR INT DEFAULT 0, "
				+ " StartsWMR INT DEFAULT 0, "
				+ " ConsumptionDW DOUBLE DEFAULT 0, "
				+ " ConsumptionIH DOUBLE DEFAULT 0, "
				+ " ConsumptionOV DOUBLE DEFAULT 0, "
				+ " ConsumptionTD DOUBLE DEFAULT 0, "
				+ " ConsumptionWM DOUBLE DEFAULT 0, "
				+ " startsDWProfiles TEXT, "
				+ " startsIHProfiles TEXT, "
				+ " startsOVProfiles TEXT, "
				+ " startsTDProfiles TEXT, "
				+ " startsWMProfiles TEXT, "
				+ " dofsDW TEXT, "
				+ " dofsIH TEXT, "
				+ " dofsOV TEXT, "
				+ " dofsTD TEXT, "
				+ " dofsWM TEXT, "
				+ " profilesSelectedDW TEXT, "
				+ " profilesSelectedIH TEXT, "
				+ " profilesSelectedOV TEXT, "
				+ " profilesSelectedTD TEXT, "
				+ " profilesSelectedWM TEXT, "
				+ " startTimesDW TEXT, "
				+ " startTimesIH TEXT, "
				+ " startTimesOV TEXT, "
				+ " startTimesTD TEXT, "
				+ " startTimesWM TEXT, "
				+ " PRIMARY KEY (RunName))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);
	}
	
	private static void setupBaseloadTable(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ baseloadTableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " activePower DOUBLE NOT NULL, "
				+ " reactivePower DOUBLE NOT NULL, "
				+ " PRIMARY KEY (RunName))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);
	}
	
	private static void setupWaterTankTable(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ waterTankTableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " avgTemperature DOUBLE, "
				+ " waterDemand DOUBLE, "
				+ " waterSupply DOUBLE, "
				+ " chpSupply DOUBLE, "
				+ " chpstarts INT, "
				+ " PRIMARY KEY (RunName))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);
	}
	
	private static void setupGATable(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ gaTableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " avgGenerationsUsed DOUBLE NOT NULL, "
				+ " avgFitnessChange TEXT NOT NULL, "
				+ " avgFitnessSpread TEXT NOT NULL, "
				+ " avgHomogeneity TEXT NOT NULL, "
				+ " noOfOptimizations INT NOT NULL, "
				+ " cervisiaCHP DOUBLE NOT NULL, "
				+ " cervisiaHWT DOUBLE NOT NULL, "
				+ " cervisiaDW DOUBLE NOT NULL, "
				+ " cervisiaTD DOUBLE NOT NULL, "
				+ " cervisiaWM DOUBLE NOT NULL, "
				+ " PRIMARY KEY (RunName))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);
	}
	
	private static void setupSmartHeaterTable(int preferredConnectionIndex) throws SQLException {
		Statement stmt = conn[preferredConnectionIndex].createStatement();
		String sqlT = "CREATE TABLE "
				+ smartHeaterTableName
				+ "(RunName VARCHAR(565) NOT NULL, "
				+ " switchOns0 INT NOT NULL, "
				+ " switchOns1 INT NOT NULL, "
				+ " switchOns2 INT NOT NULL, "
				+ " runTimes0 BIGINT NOT NULL, "
				+ " runTimes1 BIGINT NOT NULL, "
				+ " runTimes2 BIGINT NOT NULL, "		
				+ " powerTierRunTimes0 BIGINT NOT NULL, "		
				+ " powerTierRunTimes1 BIGINT NOT NULL, "	
				+ " powerTierRunTimes2 BIGINT NOT NULL, "	
				+ " powerTierRunTimes3 BIGINT NOT NULL, "	
				+ " powerTierRunTimes4 BIGINT NOT NULL, "	
				+ " powerTierRunTimes5 BIGINT NOT NULL, "	
				+ " powerTierRunTimes6 BIGINT NOT NULL, "	
				+ " PRIMARY KEY (RunName))" + additionalMysqlOptions + ";";
		stmt.executeUpdate(sqlT);
	}
	
	private static Queue<QueueLogObject> logQueue;	

	@Override
	public void run() {
		while ( running || !logQueue.isEmpty()) {
            try {
            	QueueLogObject work = null;

                synchronized ( logQueue ) {
                    while ( logQueue.isEmpty() )
                    	logQueue.wait();
                    
                    // Get the next work item off of the queue
                    work = logQueue.remove();
                }
                if (work instanceof SimulationLogObject) {
                	logSimulationResults((SimulationLogObject) work);
                } else if (work instanceof EpsLogObject) {
                	logEps((EpsLogObject) work);
                } else if (work instanceof PlsLogObject) {
                	logPls((PlsLogObject) work);
                } else if (work instanceof H0LogObject){
                	logH0((H0LogObject) work);
                } else if (work instanceof DevicesLogObject){
                	logDevices((DevicesLogObject) work);
                } else if (work instanceof BaseloadLogObject){
                	logBaseload((BaseloadLogObject) work);
                } else if (work instanceof HotWaterLogObject){
                	logHotWater((HotWaterLogObject) work);
                } else if (work instanceof DetailedPowerLogObject){
                	logDetailedPower((DetailedPowerLogObject) work);
                } else if (work instanceof WaterTankLogObject){
                	logWaterTank((WaterTankLogObject) work);
                } else if (work instanceof ChpLogObject){
                	logChp((ChpLogObject) work);
                } else if (work instanceof GALogObject){
                	logGA((GALogObject) work);
                } else {
                	logSmartHeater((SmartHeaterLogObject) work);
                }
            }
            catch ( InterruptedException ie ) {
                break;  // Terminate
            }
        }
		SQLConnectionProvider.closeConnection();
	}
	
	public static void enqueueSimResults(OSHSimulationResults simResults, long relativeStart, long relativeEnd) {
		synchronized(logQueue) {
			SimulationLogObject logInf = new SimulationLogObject(simResults, relativeStart, relativeEnd);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueSimResults(OSHSimulationResults simResults, long relativeStart, long relativeEnd, Long simRuntime) {
		synchronized(logQueue) {
			SimulationLogObject logInf = new SimulationLogObject(simResults, relativeStart, relativeEnd, simRuntime);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueDetailedPower(AncillaryCommodityLoadProfile loadProfile) {
		synchronized(logQueue) {
			DetailedPowerLogObject logInf = new DetailedPowerLogObject(loadProfile);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueEps(Map<AncillaryCommodity, PriceSignal> eps) {
		synchronized(logQueue) {
			EpsLogObject logInf = new EpsLogObject(eps);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueuePls(Map<AncillaryCommodity, PowerLimitSignal> pls) {
		synchronized(logQueue) {
			PlsLogObject logInf = new PlsLogObject(pls);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueH0(double[][][] aggrWeekdaysResults, double[][] aggrDaysResults) {
		synchronized(logQueue) {
			H0LogObject logInf = new H0LogObject(aggrWeekdaysResults, aggrDaysResults);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueHotWater(double[][] aggrWeekdaysResults, double[] aggrDaysResults, Commodity hotWater) {
		String hotWaterIdentifier = "";
		switch (hotWater) {
		case DOMESTICHOTWATERPOWER: hotWaterIdentifier = "DOM";
			break;
		case HEATINGHOTWATERPOWER: hotWaterIdentifier = "SPACE";
			break;
		default:
			break;
		}
		
		synchronized(logQueue) {
			HotWaterLogObject logInf = new HotWaterLogObject(aggrWeekdaysResults, aggrDaysResults, hotWaterIdentifier);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueDevices(int starts, int actualStarts, double activePowerConsumption, int[] profileStarts, 
			int[] dofs, int[] startTimes, int[] profilesSelected, DeviceTypes deviceType) {		
		String deviceIdentifier = "";
		switch(deviceType) {
			case DISHWASHER: deviceIdentifier = "DW";
				break;
			case DRYER: deviceIdentifier = "TD";
				break;
			case INDUCTIONCOOKTOP: deviceIdentifier = "IH";
				break;
			case WASHINGMACHINE: deviceIdentifier = "WM";
				break;
			case ELECTRICSTOVE: deviceIdentifier = "OV";
				break;
			default:				
		}
		
		synchronized(logQueue) {
			DevicesLogObject logInf = new DevicesLogObject(starts, actualStarts, profileStarts, dofs, startTimes, activePowerConsumption, profilesSelected, deviceIdentifier);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueBaseload(double activePower, double reativePower) {
		
		synchronized(logQueue) {
			BaseloadLogObject logInf = new BaseloadLogObject(activePower, reativePower);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueWaterTank(double avgTemperature, double demand, double supply) {
		
		synchronized(logQueue) {
			WaterTankLogObject logInf = new WaterTankLogObject(avgTemperature, demand, supply);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueChp(double supply, int starts) {
		
		synchronized(logQueue) {
			ChpLogObject logInf = new ChpLogObject(supply, starts);		
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueGA(double avgGenerationsUsed, double[] avgFitnessChange, 
			double[] avgFitnessSpread, double[] avgHomogeneity, int noOfOptimizations, double[] cervisia) {
		
		synchronized(logQueue) {
			GALogObject logInf = new GALogObject(avgGenerationsUsed, avgFitnessChange, 
					avgFitnessSpread, avgHomogeneity, noOfOptimizations, cervisia);
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	public static void enqueueSmartHeater(int[] switchOns, long[] runTimes, long[] powerTierRunTimes) {
		
		synchronized(logQueue) {
			SmartHeaterLogObject logInf = new SmartHeaterLogObject(switchOns, runTimes, powerTierRunTimes);
			logQueue.add(logInf);
			logQueue.notify();
		}		
	}
	
	private static void logSimulationResults(SimulationLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000)) 
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			
			int tries = 0;
			String sql = "";
			while (tries < 5) {
				System.out.println("Tried results: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData()
								.getTables(null, null, tableName, null).next()) {
							setupTable(i);
						}
						
						OSHSimulationResults results = logObj.simResults;
						
						sql = "REPLACE INTO "
								+ tableName
								+ "(RunName, "
								+ (logObj.simRuntime != null ? "Runtime, " : "")
								+ "StartTime, "
								+ "LoggingStartRelative, "
								+ "LoggingEndRelative, "
								+ "ActivePowerConsumption,"
								+ "ActivePowerPV, "
								+ "ActivePowerPVAutoConsumption, "
								+ "ActivePowerPVFeedIn, "
								+ "ActivePowerCHP, "
								+ "ActivePowerCHPAutoConsumption, "
								+ "ActivePowerCHPFeedIn, "
								+ "ActivePowerBatteryCharging, "
								+ "ActivePowerBatteryDischarging, "
								+ "ActivePowerBatteryAutoConsumption, "
								+ "ActivePowerBatteryFeedIn, "
								+ "ActivePowerExternal, "
								+ "ReactivePowerExternal, "
								+ "GasPowerExternal, "
								+ "EpsCosts, "
								+ "PlsCosts, "
								+ "GasCosts, "
								+ "FeedInCostsPV, "
								+ "FeedInCostsCHP, "
								+ "AutoConsumptionCosts, "
								+ "TotalCosts)"

									+ "VALUES ('" + runName + "'"
									+ (logObj.simRuntime != null ? ", " + logObj.simRuntime : "")
									+ ", " + startTime
									+ ", " + logObj.relativeStart
									+ ", " + logObj.relativeEnd
									+ ", " + results.getActivePowerConsumption()
									+ ", " + results.getActivePowerPV() 
									+ ", " + results.getActivePowerPVAutoConsumption()
									+ ", " + results.getActivePowerPVFeedIn()
									+ ", " + results.getActivePowerCHP()
									+ ", " + results.getActivePowerCHPAutoConsumption()
									+ ", " + results.getActivePowerCHPFeedIn()
									+ ", " + results.getActivePowerBatteryCharging()
									+ ", " + results.getActivePowerBatteryDischarging()
									+ ", " + results.getActivePowerBatteryAutoConsumption()
									+ ", " + results.getActivePowerBatteryFeedIn()
									+ ", " + results.getActivePowerExternal()
									+ ", " + results.getReactivePowerExternal()
									+ ", " + results.getGasPowerExternal()
									+ ", " + results.getEpsCosts()
									+ ", " + results.getPlsCosts() 
									+ ", " + results.getGasCosts() 
									+ ", " + results.getFeedInCostsPV()
									+ ", " + results.getFeedInCostsCHP()
									+ ", " + results.getAutoConsumptionCosts()
									+ ", " + results.getTotalCosts() + ");";
						stmt.executeUpdate(sql);

						stmt.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						tries++;
					}
				}
			}
		}		
	}
	
	private static void logDetailedPower(DetailedPowerLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000)) 
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			
			int tries = 0;
			String sql = "";
			while (tries < 5) {
				System.out.println("Tried detailed power: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData()
								.getTables(null, null, detailedPowerTableName, null).next()) {
							setupDetailedPowerTable(i);
						}
						
						AncillaryCommodity[] toLog = {
								AncillaryCommodity.ACTIVEPOWEREXTERNAL,
								AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION,
								AncillaryCommodity.CHPACTIVEPOWERFEEDIN,
								AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION,
								AncillaryCommodity.PVACTIVEPOWERFEEDIN
						};
						
						for (AncillaryCommodity a : toLog) {
							
							Long time = Long.MIN_VALUE;
							time = logObj.loadProfile.getNextLoadChange(a, time);
							
							while (time != null) {								
								int power = logObj.loadProfile.getLoadAt(a, time);
								
								sql = "REPLACE INTO "
										+ detailedPowerTableName
										+ "(RunName, "
										+ "AncillaryCommodity,"	
										+ "Time,"
										+ "Power)"

											+ "VALUES ('" + runName + "'"
											+ ", '" + a
											+ "', " + time
											+ ", " + power + ");";
								
								stmt.executeUpdate(sql);
							}							
						}
						stmt.close();
						break;
					} catch (Exception e) {
						tries++;
					}
				}
			}
		}		
	}
	
	private static void logEps(EpsLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			String sqlBase = "";
			while (tries < 5) {
				System.out.println("Tried eps: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, epsTableName, null).next()) {
							setupEpsTable(i);
						}
						sqlBase = "REPLACE INTO " + epsTableName + "(RunName," + "VirtualCommodity, " + "Time, "
								+ "Price)"

								+ "VALUES ('" + runName + "'" + ", '%s'" + ", %d" + ", %f);";

						for (Entry<AncillaryCommodity, PriceSignal> en : logObj.eps.entrySet()) {
							for (Entry<Long, Double> price : en.getValue().getPrices().entrySet()) {
								//forces decimal point
								String sql = String.format(Locale.ENGLISH, sqlBase, en.getKey().getCommodity(),
										price.getKey(), price.getValue());
								stmt.executeUpdate(sql);
							}
						}

						stmt.close();
						break;
					} catch (Exception e) {
						tries++;
					}
				}
			} 
		}
	}
	
	private static void logPls(PlsLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			String sqlBase = "";
			while (tries < 5) {
				System.out.println("Tried pls: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, plsTableName, null).next()) {
							setupPlsTable(i);
						}
						sqlBase = "REPLACE INTO " + plsTableName + "(RunName," + "VirtualCommodity, " + "Time, "
								+ "LowerLimit, " + "UpperLimit)"

								+ "VALUES ('" + runName + "'" + ", '%s'" + ", %d" + ", %f" + ", %f);";

						for (Entry<AncillaryCommodity, PowerLimitSignal> en : logObj.pls.entrySet()) {
							for (Entry<Long, PowerInterval> power : en.getValue().getLimits().entrySet()) {
								String sql = String.format(Locale.ENGLISH, sqlBase, en.getKey().getCommodity(),
										power.getKey(), power.getValue().getPowerLowerLimit(),
										power.getValue().getPowerUpperLimit());
								stmt.executeUpdate(sql);
							}
						}
						stmt.close();
						break;
					} catch (Exception e) {
						tries++;
					}
				}
			} 
		}
	}
	
	private static void logH0(H0LogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			while (tries < 5) {
				System.out.println("Tried h0: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, h0TableName, null).next()) {
							setupH0Table(i);
						}
						String sql = "REPLACE INTO " + h0TableName + "(RunName," + "weekDay0, " + "weekDay1, "
								+ "weekDay2, " + "weekDay3, " + "weekDay4, " + "weekDay5, " + "weekDay6, " + "days)"

								+ "VALUES ('" + runName + "'" + ", '"
								+ Arrays.toString(Arrays.stream(logObj.aggrWeekdayResults[0])
										.map(l -> Arrays.toString(l)).toArray(String[]::new))
								+ "'" + ", '"
								+ Arrays.toString(Arrays.stream(logObj.aggrWeekdayResults[1])
										.map(l -> Arrays.toString(l)).toArray(String[]::new))
								+ "'" + ", '"
								+ Arrays.toString(Arrays.stream(logObj.aggrWeekdayResults[2])
										.map(l -> Arrays.toString(l)).toArray(String[]::new))
								+ "'" + ", '"
								+ Arrays.toString(Arrays.stream(logObj.aggrWeekdayResults[3])
										.map(l -> Arrays.toString(l)).toArray(String[]::new))
								+ "'" + ", '"
								+ Arrays.toString(Arrays.stream(logObj.aggrWeekdayResults[4])
										.map(l -> Arrays.toString(l)).toArray(String[]::new))
								+ "'" + ", '"
								+ Arrays.toString(Arrays.stream(logObj.aggrWeekdayResults[5])
										.map(l -> Arrays.toString(l)).toArray(String[]::new))
								+ "'" + ", '"
								+ Arrays.toString(Arrays.stream(logObj.aggrWeekdayResults[6])
										.map(l -> Arrays.toString(l)).toArray(String[]::new))
								+ "'" + ", '" + Arrays.toString(Arrays.stream(logObj.aggrDayResults)
										.map(l -> Arrays.toString(l)).toArray(String[]::new))
								+ "'" + ");";

						stmt.executeUpdate(sql);
						stmt.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						tries++;
					}
				}
			} 
		}
	}
	
	private static void logHotWater(HotWaterLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			while (tries < 5) {
				System.out.println("Tried hotWater_" + logObj.hotWaterIdentifier + " : " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, hotWaterTableName + "_" + logObj.hotWaterIdentifier, null).next()) {
							setupHotWaterTable(logObj.hotWaterIdentifier, i);
						}
						String sql = "REPLACE INTO " + hotWaterTableName + "_" + logObj.hotWaterIdentifier + "(RunName," + "weekDay0, " + "weekDay1, "
								+ "weekDay2, " + "weekDay3, " + "weekDay4, " + "weekDay5, " + "weekDay6, " + "days)"

								+ "VALUES ('" + runName + "'" + ", '" + Arrays.toString(logObj.aggrWeekdayResults[0])
								+ "'" + ", '" + Arrays.toString(logObj.aggrWeekdayResults[1]) + "'" + ", '"
								+ Arrays.toString(logObj.aggrWeekdayResults[2]) + "'" + ", '"
								+ Arrays.toString(logObj.aggrWeekdayResults[3]) + "'" + ", '"
								+ Arrays.toString(logObj.aggrWeekdayResults[4]) + "'" + ", '"
								+ Arrays.toString(logObj.aggrWeekdayResults[5]) + "'" + ", '"
								+ Arrays.toString(logObj.aggrWeekdayResults[6]) + "'" + ", '"
								+ Arrays.toString(logObj.aggrDayResults) + "'" + ");";

						stmt.executeUpdate(sql);
						stmt.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						tries++;
					}
				}
			} 
		}
	}
	
	private static void logDevices(DevicesLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			while (tries < 5) {
				System.out.println("Tried devices: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, devicesTableName, null).next()) {
							setupDevicesTable(i);
						}

						String columnIdent = "starts" + logObj.deviceIdentifier;
						String columnIdent2 = "Consumption" + logObj.deviceIdentifier;
						String columnIdent3 = "dofs" + logObj.deviceIdentifier;
						String columnIdent4 = "startTimes" + logObj.deviceIdentifier;
						String columnIdent5 = "profilesSelected" + logObj.deviceIdentifier;

						String sql = "INSERT INTO " + devicesTableName + "(RunName," + columnIdent + "," + columnIdent
								+ "R," + columnIdent2 + "," + columnIdent + "Profiles," + columnIdent3 + ","
								+ columnIdent4 + ", " + columnIdent5 + ")"

								+ "VALUES ('" + runName + "'" 
								+ ", " + logObj.plannedDeviceStarts 
								+ ", " + logObj.actualDeviceStarts 
								+ ", " + logObj.activePowerConsumption 
								+ ", '" + Arrays.toString(logObj.profileStarts) 
								+ "', '" + Arrays.toString(logObj.dofs)
								+ "', '" + Arrays.toString(logObj.startTimes) 
								+ "', '" + Arrays.toString(logObj.profilesSelected)
								+ "') " + "ON DUPLICATE KEY UPDATE "
								+ columnIdent + "=VALUES(" + columnIdent 
								+ "), " + columnIdent + "R=VALUES("	+ columnIdent 
								+ "R), " + columnIdent2 + "=VALUES(" + columnIdent2 
								+ "), " + columnIdent + "Profiles=VALUES(" + columnIdent + "PROFILES" 
								+ ")," + columnIdent3 + "=VALUES(" + columnIdent3 
								+ "), " + columnIdent4 + "=VALUES(" + columnIdent4 
								+ "), " + columnIdent5 + "=VALUES(" + columnIdent5								
								+ ");";

						stmt.executeUpdate(sql);
						stmt.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						tries++;
					}
				}
			} 
		}
	}
	
	private static void logBaseload(BaseloadLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			while (tries < 5) {
				System.out.println("Tried baseload: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, baseloadTableName, null).next()) {
							setupBaseloadTable(i);
						}

						String sql = "REPLACE INTO " + baseloadTableName + "(RunName," + "activePower,"
								+ "reactivePower)"

								+ "VALUES ('" + runName + "'" + ", " + logObj.activePower + ", " + logObj.reactivePower
								+ ");";

						stmt.executeUpdate(sql);
						stmt.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						tries++;
					}
				}
			} 
		}
	}
	
	private static void logWaterTank(WaterTankLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			while (tries < 5) {
				System.out.println("Tried waterTank: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, waterTankTableName, null).next()) {
							setupWaterTankTable(i);
						}

						String sql = "INSERT INTO " + waterTankTableName 
								+ "(RunName," 
								+ "avgTemperature,"
								+ "waterDemand,"
								+ "waterSupply)"

								+ "VALUES ('" + runName + "'" 
								+ ", " + logObj.avgTemperature
								+ ", " + logObj.demand
								+ ", " + logObj.supply
								+ ") ON DUPLICATE KEY UPDATE "
								+ "avgTemperature=VALUES(avgTemperature)"
								+", waterDemand=VALUES(waterDemand)"
								+", waterSupply=VALUES(waterSupply);";

						stmt.executeUpdate(sql);
						stmt.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						tries++;
					}
				}
			} 
		}
	}
	
	private static void logChp(ChpLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			while (tries < 5) {
				System.out.println("Tried chp: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, waterTankTableName, null).next()) {
							setupWaterTankTable(i);
						}

						String sql = "INSERT INTO " + waterTankTableName 
								+ "(RunName," 
								+ "chpSupply,"
								+ "chpStarts)"

								+ "VALUES ('" + runName + "'" 
								+ ", " + logObj.supply
								+ ", " + logObj.starts
								+ ") ON DUPLICATE KEY UPDATE "
								+ "chpSupply=VALUES(chpSupply)"
								+ ", chpStarts=VALUES(chpStarts);";

						stmt.executeUpdate(sql);
						stmt.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						tries++;
					}
				}
			} 
		}
	}
	
	private static void logGA(GALogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			while (tries < 5) {
				System.out.println("Tried ga: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, gaTableName, null).next()) {
							setupGATable(i);
						}
						String sql = "REPLACE INTO " + gaTableName  + "("
								+ "RunName," 
								+ "avgGenerationsUsed, " 
								+ "avgFitnessChange, "
								+ "avgFitnessSpread, "
								+ "avgHomogeneity, "
								+ "noOfOptimizations,"
								+ "cervisiaCHP, "
								+ "cervisiaHWT, "
								+ "cervisiaDW, "
								+ "cervisiaTD, "
								+ "cervisiaWM)"

								+ "VALUES ('" + runName + "'" 
								+ ", " + logObj.avgGenerationsUsed 
								+ ", '" + Arrays.toString(logObj.avgFitnessChange) 
								+ "', '" + Arrays.toString(logObj.avgFitnessSpread) 
								+ "', '" + Arrays.toString(logObj.avgHomogeneity) 
								+ "', " + logObj.noOfOptimizations
								+ ", " + logObj.cervisia[0]
								+ ", " + logObj.cervisia[1] 
								+ ", " + logObj.cervisia[2] 
								+ ", " + logObj.cervisia[3] 
								+ ", " + logObj.cervisia[4] + ");";

						stmt.executeUpdate(sql);
						stmt.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						tries++;
					}
				}
			} 
		}
	}
	
	private static void logSmartHeater(SmartHeaterLogObject logObj) {
		for (int i = 0; i < preferredConnection.length; i++) {
			try {
				if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
					trySetupConnection(i);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			if (conn == null)
				return;
			int tries = 0;
			while (tries < 5) {
				System.out.println("Tried smartHeater: " + tries);
				if (conn != null) {
					try {
						Statement stmt = conn[i].createStatement();
						if (!conn[i].getMetaData().getTables(null, null, smartHeaterTableName, null).next()) {
							setupSmartHeaterTable(i);
						}
						String sql = "REPLACE INTO " + smartHeaterTableName  + "("
								+ "RunName," 
								+ "switchOns0, " 
								+ "switchOns1, " 
								+ "switchOns2, " 
								+ "runTimes0, " 
								+ "runTimes1, " 
								+ "runTimes2, "
								+ "powerTierRunTimes0, "
								+ "powerTierRunTimes1, "
								+ "powerTierRunTimes2, "
								+ "powerTierRunTimes3, "
								+ "powerTierRunTimes4, "
								+ "powerTierRunTimes5, "
								+ "powerTierRunTimes6)"

								+ "VALUES ('" + runName + "'" 
								+ ", " + logObj.switchOns[0]
								+ ", " + logObj.switchOns[1] 
								+ ", " + logObj.switchOns[2] 
								+ ", " + logObj.runTimes[0] 
								+ ", " + logObj.runTimes[1] 
								+ ", " + logObj.runTimes[2]  
								+ ", " + logObj.powerTierRunTimes[0]  
								+ ", " + logObj.powerTierRunTimes[1]  
								+ ", " + logObj.powerTierRunTimes[2]  
								+ ", " + logObj.powerTierRunTimes[3]  
								+ ", " + logObj.powerTierRunTimes[4]  
								+ ", " + logObj.powerTierRunTimes[5]  
								+ ", " + logObj.powerTierRunTimes[6]  + ");";

						stmt.executeUpdate(sql);
						stmt.close();
						break;
					} catch (Exception e) {
						e.printStackTrace();
						tries++;
					}
				}
			} 
		}
	}
	
	private static abstract class QueueLogObject {	
		
		public QueueLogObject() {
			super();
		}		
	}
	
	private static class SimulationLogObject extends QueueLogObject {
		
		public OSHSimulationResults simResults;
		public long relativeStart;
		public long relativeEnd;
		public Long simRuntime;
		
		public SimulationLogObject(OSHSimulationResults simResults, long relativeStart, long relativeEnd) {
			super();
			this.simResults = simResults;
			this.relativeStart = relativeStart;
			this.relativeEnd = relativeEnd;
		}	
		
		public SimulationLogObject(OSHSimulationResults simResults, long relativeStart, long relativeEnd, Long simRuntime) {
			super();
			this.simResults = simResults;
			this.relativeStart = relativeStart;
			this.relativeEnd = relativeEnd;
			this.simRuntime = simRuntime;
		}	
	}
	
	private static class EpsLogObject extends QueueLogObject {
		public Map<AncillaryCommodity, PriceSignal> eps;

		public EpsLogObject(Map<AncillaryCommodity, PriceSignal> eps) {
			super();
			this.eps = eps;
		}
	}
	
	private static class PlsLogObject extends QueueLogObject {
		public Map<AncillaryCommodity, PowerLimitSignal> pls;

		public PlsLogObject(Map<AncillaryCommodity, PowerLimitSignal> pls) {
			super();
			this.pls = pls;
		}
	}
	
	private static class DetailedPowerLogObject extends QueueLogObject {
		public AncillaryCommodityLoadProfile loadProfile;

		public DetailedPowerLogObject(AncillaryCommodityLoadProfile loadProfile) {
			super();
			this.loadProfile = loadProfile;
		}
	}
	
	private static class H0LogObject extends QueueLogObject {
		private double[][][] aggrWeekdayResults;
		private double[][] aggrDayResults;

		public H0LogObject(double[][][] aggrWeekdayResults, double[][] aggrDayResults) {
			super();
			this.aggrWeekdayResults = aggrWeekdayResults;
			this.aggrDayResults = aggrDayResults;
		}
	}
	
	private static class DevicesLogObject extends QueueLogObject {
		private int plannedDeviceStarts;
		private int actualDeviceStarts;
		private double activePowerConsumption;
		private int[] profileStarts;
		private int[] dofs;
		private int[] startTimes;
		private int[] profilesSelected;
		private String deviceIdentifier;

		public DevicesLogObject(int plannedDeviceStarts, int actualDeviceStarts,
				int[] profileStarts, int[] dofs, int[] startTimes, double activePowerConsumption, int[] profilesSelected, String deviceIdentifier) {
			super();
			this.plannedDeviceStarts = plannedDeviceStarts;
			this.actualDeviceStarts = actualDeviceStarts;
			this.activePowerConsumption = activePowerConsumption;
			this.profileStarts = profileStarts;
			this.deviceIdentifier = deviceIdentifier;
			this.dofs = dofs;
			this.startTimes = startTimes;
			this.profilesSelected = profilesSelected;
		}
	}
	
	private static class BaseloadLogObject extends QueueLogObject {
		private double activePower;
		private double reactivePower;

		public BaseloadLogObject(double activePower, double reactivePower) {
			super();
			this.activePower = activePower;
			this.reactivePower = reactivePower;
		}
	}
	
	private static class SmartHeaterLogObject extends QueueLogObject {
		private int[] switchOns;
		private long[] powerTierRunTimes;
		private long[] runTimes;

		public SmartHeaterLogObject(int[] switchOns, long[] runTimes, long[] powerTierRunTimes) {
			super();
			this.switchOns = switchOns;
			this.runTimes = runTimes;
			this.powerTierRunTimes = powerTierRunTimes;
		}
	}
	
	private static class WaterTankLogObject extends QueueLogObject {
		private double avgTemperature;
		private double demand;
		private double supply;

		public WaterTankLogObject(double avgTemperature, double demand, double supply) {
			super();
			this.avgTemperature = avgTemperature;
			this.demand = demand;
			this.supply = supply;
		}


	}
	
	private static class HotWaterLogObject extends QueueLogObject {
		private double[][] aggrWeekdayResults;
		private double[] aggrDayResults;
		private String hotWaterIdentifier;

		public HotWaterLogObject(double[][] aggrWeekdayResults, double[] aggrDayResults, String hotWaterIdentifier) {
			super();
			this.aggrWeekdayResults = aggrWeekdayResults;
			this.aggrDayResults = aggrDayResults;
			this.hotWaterIdentifier = hotWaterIdentifier;
		}
	}
	
	private static class ChpLogObject extends QueueLogObject {
		private double supply;
		private int starts;

		public ChpLogObject(double supply, int starts) {
			super();
			this.supply = supply;
			this.starts = starts;
		}
	}
	
	private static class GALogObject extends QueueLogObject {
		private double avgGenerationsUsed;
		private double[] avgFitnessChange;
		private double[] avgFitnessSpread;
		private double[] avgHomogeneity;
		private int noOfOptimizations;
		private double[] cervisia;
		
		
		public GALogObject(double avgGenerationsUsed, double[] avgFitnessChange, 
				double[] avgFitnessSpread, double[] avgHomogeneity, int noOfOptimizations, double[] cervisia) {
			super();
			this.avgGenerationsUsed = avgGenerationsUsed;
			this.avgFitnessChange = avgFitnessChange;
			this.avgFitnessSpread = avgFitnessSpread;
			this.avgHomogeneity = avgHomogeneity;
			this.noOfOptimizations = noOfOptimizations;
			this.cervisia = cervisia;
		}

	}

	public static boolean isLogDevices() {
		return logDevices;
	}

	public static void setLogDevices(boolean logDevices) {
		DatabaseLoggerThread.logDevices = logDevices;
	}
	
	public static boolean isLogHotWater() {
		return logHotWater;
	}

	public static void setLogHotWater(boolean logHotWater) {
		DatabaseLoggerThread.logHotWater = logHotWater;
	}
	
	public static boolean isLogWaterTank() {
		return logWaterTank;
	}

	public static void setLogWaterTank(boolean logWaterTank) {
		DatabaseLoggerThread.logWaterTank = logWaterTank;
	}
	
	public static boolean isLogGA() {
		return logGA;
	}

	public static void setLogGA(boolean logGA) {
		DatabaseLoggerThread.logGA = logGA;
	}
	
	public static boolean isLogSmartHeater() {
		return logSmartHeater;
	}

	public static void setLogSmartHeater(boolean logSmartHeater) {
		DatabaseLoggerThread.logSmartHeater = logSmartHeater;
	}
}
