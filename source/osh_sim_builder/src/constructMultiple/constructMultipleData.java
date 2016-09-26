package constructMultiple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import constructsimulation.datatypes.EPSTypes;
import osh.datatypes.power.LoadProfileCompressionTypes;

public abstract class constructMultipleData {
	
    //if should do aggregated logging for H0
	public static boolean logH0 = false;
	public static boolean logEpsPls = false;
	public static boolean logIntervalls = false;
	public static boolean logDevices = false;
	public static boolean logDetailedPower = false;
	public static boolean logHotWater = false;
	public static boolean logWaterTank = true;
	public static boolean logGA = true;
	public static boolean logSmartHeater = false;
	
	static final EPSTypes[] eps = {
//		EPSTypes.CSV,
//		EPSTypes.FLAT,
		EPSTypes.H0,
//		EPSTypes.HOURLY_ALTERNATING,
		EPSTypes.MC_FLAT,
//		EPSTypes.REMS,
//		EPSTypes.STEPS
//		EPSTypes.WIKWEEKDAY2015,
//		EPSTypes.WIKWEEKDAY2020,
//		EPSTypes.WIKWEEKDAY2025,
//		EPSTypes.WIKHOURLY2015,
//		EPSTypes.WIKHOURLY2020,	
//		EPSTypes.WIKHOURLY2025,
//		EPSTypes.WIK_BASED_THESIS
	};
	
	static final DeviceConfiguration[] deviceType = {
//		DeviceConfiguration.NORMAL,
		DeviceConfiguration.DELAYABLE,
		DeviceConfiguration.INTERRUPTIBLE,		
		DeviceConfiguration.HYBRID,
		DeviceConfiguration.HYBRID_DELAYABLE,
		DeviceConfiguration.HYBRID_INTERRUPTIBLE,
//		DeviceConfiguration.HYBRID_SINGLE,
	};
	
	static BatteryOrHeating[] insertHeatingElementOrBatteryStorage = {
		BatteryOrHeating.NONE,
//		BatteryOrHeating.BATTERY,
//		BatteryOrHeating.INSERTHEATING
	};
	
	static Integer[] tankSizes = {
//		150,
//		350,
//		250,
//		500,
		750,
	};
	
	static PVConfiguration[] pvType = {
		//				complexPowerMax, cosPhiMax, nominalPower, useESHLPV, useHOLLPV
//		new PVConfiguration("10000", "-0.8", 2000, true, false),
//		new PVConfiguration("10000", "-0.8", 2000, false, true),
		new PVConfiguration("0", "0", 0, false, false),
//		new PVConfiguration("10000", "-0.8", 2000, false, true),
		new PVConfiguration("10000", "-0.8", 4000, false, true),
//		new PVConfiguration("10000", "-0.8", 6000, false, true),
	};
	
	static CHPConfiguration[] chpType = {
		CHPConfiguration.DUMB,
		CHPConfiguration.INTELLIGENT,
		CHPConfiguration.NONE // -> add gas heating
	};
	
	static Integer[] persons = {
//		1,
//		2,
//		3,
		4,
//		5
	};
	
	static Integer[] epsOptimisationObjective = {
//			3,
			4,
	};
	
	static PLSType[] plsType = {
			PLSType.FULL,
//			PLSType.HALF_NEG,
//			PLSType.HALF_POS,
//			PLSType.NONE
	};

	static CompressionConfiguration[] compressionTypes = {
//			new CompressionConfiguration(LoadProfileCompressionTypes.DISCONTINUITIES, 1),
//			new CompressionConfiguration(LoadProfileCompressionTypes.DISCONTINUITIES, 10),
//			new CompressionConfiguration(LoadProfileCompressionTypes.DISCONTINUITIES, 50),
			new CompressionConfiguration(LoadProfileCompressionTypes.DISCONTINUITIES, 100),
//			new CompressionConfiguration(LoadProfileCompressionTypes.DISCONTINUITIES, 250),
//			new CompressionConfiguration(LoadProfileCompressionTypes.DISCONTINUITIES, 500),
//			new CompressionConfiguration(LoadProfileCompressionTypes.DISCONTINUITIES, 1000),
//			new CompressionConfiguration(LoadProfileCompressionTypes.TIMESLOTS, 1),
//			new CompressionConfiguration(LoadProfileCompressionTypes.TIMESLOTS, 15),
//			new CompressionConfiguration(LoadProfileCompressionTypes.TIMESLOTS, 60),
//			new CompressionConfiguration(LoadProfileCompressionTypes.TIMESLOTS, 300),
//			new CompressionConfiguration(LoadProfileCompressionTypes.TIMESLOTS, 900),
	};
	
	static Integer[] escResolution = {
//			15,
//			30,
			60,
//			120,
//			300,
//			900
	};
	
	static Double[] autoProbFactor = {
//			0.5, 
//			1.0,
//			2.0,
//			3.0,
//			4.0,
//			5.0,
//			6.0,
//			7.0,
//			7.5
//			8.0,
			9.0,
//			10.0
	};
	
	static Double[] crossoverProb = {
//			0.5, 
//			0.6,
//			0.65,
//			0.7,
//			0.75,
//			0.8,
//			0.85,
//			0.9,
//			0.95,
//			0.975,
			0.99,
//			1.0
	};
	
	public static HashMap<String, ArrayList<?>> produceMap() {
		HashMap<String, ArrayList<?>> map = new HashMap<String, ArrayList<?>>();
		
		map.put("eps", new ArrayList<Object>(Arrays.asList(eps)));
		map.put("devices", new ArrayList<Object>(Arrays.asList(deviceType)));
		map.put("heatingOrBattery", new ArrayList<Object>(Arrays.asList(insertHeatingElementOrBatteryStorage)));
		map.put("tankSizes", new ArrayList<Object>(Arrays.asList(tankSizes)));
		map.put("pvType", new ArrayList<Object>(Arrays.asList(pvType)));
		map.put("chpType", new ArrayList<Object>(Arrays.asList(chpType)));
		map.put("persons", new ArrayList<Object>(Arrays.asList(persons)));
		map.put("compression", new ArrayList<Object>(Arrays.asList(compressionTypes)));
//		map.put("escResolution", new ArrayList<Object>(Arrays.asList(escResolution)));
		map.put("autoProbFactor", new ArrayList<Object>(Arrays.asList(autoProbFactor)));
		map.put("crossoverProb", new ArrayList<Object>(Arrays.asList(crossoverProb)));
		map.put("epsOptimisationObjective", new ArrayList<Object>(Arrays.asList(epsOptimisationObjective)));
		map.put("pls", new ArrayList<Object>(Arrays.asList(plsType)));
		
		return map;
	}

}
