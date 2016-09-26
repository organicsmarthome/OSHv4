package constructMultiple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import constructsimulation.constructSimulationPackage;
import constructsimulation.datatypes.EPSTypes;

public class constructMultiplePackages {
	
	private static final String saveDirectory = "multiplePackages";
	private static PrintWriter pw;
	
	public static void main(String[] args) throws FileNotFoundException {
		
		new File(saveDirectory).mkdirs();
		
		HashMap<String, ArrayList<?>> totalValues = constructMultipleData.produceMap();
		ArrayList<Entry<String, ArrayList<?>>> entries = mapToEntryList(totalValues);
		ArrayList<HashMap<String, ?>> permutedMapList = new ArrayList<HashMap<String, ?>>();
		permuteMap(permutedMapList, 0, entries, new HashMap<String, Object>());
		
		File printFile = new File(saveDirectory + "/configNames.txt");
		if (printFile.exists()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line;
			do {
				System.out.println();
				System.out.println("ERROR: file for config names already exists. Delete (y/n)?");
				try {
					line = reader.readLine();
				} 
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			} while (!line.equals("y") && !line.equals("n"));
			
			if (line.equals("y")) {
				deleteDirectory(printFile);
				if (printFile.exists()) throw new RuntimeException("It still exists!");
			} else {
				System.out.println("Aborting...");
				System.exit(1);
			}
		}
		pw = new PrintWriter(new File(saveDirectory + "/configNames.txt"));
		
		for (HashMap<String, ?> config : permutedMapList) {
			buildForConfig(config);
		}
		
		pw.flush();
		pw.close();
	}
	
	private static void buildForConfig(HashMap<String, ?> config) {
		constructSimulationPackage.showGui = false;
		
		if (config.containsKey("eps"))
			constructSimulationPackage.epsType = (EPSTypes) config.get("eps");
		if (config.containsKey("devices"))
			constructSimulationPackage.genericAppliancesToSimulate = 
			DeviceConfiguration.getAppliancesValues((DeviceConfiguration) config.get("devices"));
		if (config.containsKey("heatingOrBattery")) {
			switch ((BatteryOrHeating) config.get("heatingOrBattery")) {
			case BATTERY:
				constructSimulationPackage.useIHESmartHeater = false;
				constructSimulationPackage.useBatteryStorage = true;
				break;
			case INSERTHEATING:
				constructSimulationPackage.useIHESmartHeater = true;
				constructSimulationPackage.useBatteryStorage = false;
				break;
			case NONE:
				constructSimulationPackage.useIHESmartHeater = false;
				constructSimulationPackage.useBatteryStorage = false;
				break;
			default: throw new IllegalArgumentException();			
			}
		};
		if (config.containsKey("tankSizes"))
			constructSimulationPackage.tankSize = (int) config.get("tankSizes");
		if (config.containsKey("pvType")) {
			PVConfiguration pvConfig = (PVConfiguration) config.get("pvType");
			constructSimulationPackage.pvComplexPowerMax = pvConfig.pvComplexPowerMax;
			constructSimulationPackage.pvCosPhiMax = pvConfig.pvCosPhiMax;
			constructSimulationPackage.pvNominalPower = pvConfig.pvNominalPower;
			constructSimulationPackage.usePVRealESHL = pvConfig.usePVRealESHL;
			constructSimulationPackage.usePVRealHOLL = pvConfig.usePVRealHOLL;
		}
		if (config.containsKey("chpType")) {
			CHPConfiguration chpConfig = (CHPConfiguration) config.get("chpType");
			switch(chpConfig) {
			case NONE: {
				constructSimulationPackage.useDachsCHP = false;
				constructSimulationPackage.intelligentCHPcontrol = false;
				constructSimulationPackage.useGasHeating = true;
				break;
			}
			case DUMB: {
				constructSimulationPackage.useDachsCHP = true;
				constructSimulationPackage.intelligentCHPcontrol = false;
				constructSimulationPackage.useGasHeating = false;
				break;
			}
			case INTELLIGENT: {
				constructSimulationPackage.useDachsCHP = true;
				constructSimulationPackage.intelligentCHPcontrol = true;
				constructSimulationPackage.useGasHeating = false;
				break;
			}
			default: throw new IllegalArgumentException();
			}
		}
		if (config.containsKey("persons")) {
			constructSimulationPackage.numberOfPersons = (int) config.get("persons");
			constructSimulationPackage.simPackage.getDynamicScreenplayArguments().getNumPersons().clear();
			constructSimulationPackage.simPackage.getDynamicScreenplayArguments().getNumPersons().add((int) config.get("persons"));
		}
		if (config.containsKey("compression")) {
			constructSimulationPackage.compressionType = ((CompressionConfiguration) config.get("compression")).compressionType;
			constructSimulationPackage.compressionValue = ((CompressionConfiguration) config.get("compression")).compressionValue;
		}
		if (config.containsKey("escResolution")) {
			constructSimulationPackage.stepSizeESCinOptimization = (int) config.get("escResolution");
		}
		if (config.containsKey("autoProbFactor")) {
			constructSimulationPackage.autoProbMuatationFactor = (double) config.get("autoProbFactor");
		}
		if (config.containsKey("crossoverProb")) {
			constructSimulationPackage.crossoverProbability = (double) config.get("crossoverProb");
		}
		if (config.containsKey("epsOptimisationObjective")) {
			constructSimulationPackage.simPackage.getEPSOptimizationObjectives().clear();
			constructSimulationPackage.simPackage.getEPSOptimizationObjectives().add(
					(int) config.get("epsOptimisationObjective"));
		}
		if (config.containsKey("pls")) {
			PLSType plsType = (PLSType) config.get("pls");
			
			constructSimulationPackage.simPackage.getPLSOptimizationObjectives().clear();
			if (plsType != PLSType.NONE) {
				constructSimulationPackage.simPackage.getPLSOptimizationObjectives().add(1);
			} else {
				constructSimulationPackage.simPackage.getPLSOptimizationObjectives().add(0);
			}
			
			if (plsType == PLSType.HALF_POS) {
				constructSimulationPackage.upperOverlimitFactor = 1.0;
				constructSimulationPackage.lowerOverlimitFactor = 0.0;
			} else if (plsType == PLSType.HALF_NEG) {
				constructSimulationPackage.upperOverlimitFactor = 0.0;
				constructSimulationPackage.lowerOverlimitFactor = 1.0;
			} else if (plsType == PLSType.FULL) {
				constructSimulationPackage.upperOverlimitFactor = 1.0;
				constructSimulationPackage.lowerOverlimitFactor = 1.0;
			} else {
				constructSimulationPackage.upperOverlimitFactor = 0.0;
				constructSimulationPackage.lowerOverlimitFactor = 0.0;
			}
		}
		
		
		setLogValues();
		
		String name = configToString(config);
		pw.println("\"" + name + "\",");
		
		constructSimulationPackage.generate(saveDirectory + "/" + name + "/");		
	}
	
	private static void setLogValues() {
		constructSimulationPackage.logH0 = constructMultipleData.logH0;
		constructSimulationPackage.logEpsPls = constructMultipleData.logEpsPls;
		constructSimulationPackage.logIntervalls = constructMultipleData.logIntervalls;
		constructSimulationPackage.logDevices = constructMultipleData.logDevices;
		constructSimulationPackage.logDetailedPower = constructMultipleData.logDetailedPower;
		constructSimulationPackage.logHotWater = constructMultipleData.logHotWater;
		constructSimulationPackage.logWaterTank = constructMultipleData.logWaterTank;
		constructSimulationPackage.logGA = constructMultipleData.logGA;
		constructSimulationPackage.logSmartHeater = constructMultipleData.logSmartHeater;
	}
	
	private static String configToString(HashMap<String, ?> config) {
		String name = "";
		if (config.containsKey("persons")) 
			name += config.get("persons") + "pax_";
		if (config.containsKey("devices")) 
			name += DeviceConfiguration.toShortName((DeviceConfiguration) config.get("devices")) + "_";
		if (config.containsKey("heatingOrBattery")) 
			name += BatteryOrHeating.toShortString((BatteryOrHeating) config.get("heatingOrBattery"))  + "_";		
		if (config.containsKey("tankSizes")) 
			name += config.get("tankSizes") + "_";
		if (config.containsKey("pvType")) {
			PVConfiguration pvConfig = (PVConfiguration) config.get("pvType");
			name += pvConfig.toShortName() + "_";
		}
		if (config.containsKey("chpType"))
			name += CHPConfiguration.toShortName((CHPConfiguration) config.get("chpType")) + "_";
		if (config.containsKey("eps"))
			name += typeToName((EPSTypes) config.get("eps")) + "_";
//		if (config.containsKey("compression")) {
//			name += ((CompressionConfiguration) config.get("compression")).toShortName() + "_";
//		}
//		if (config.containsKey("escResolution")) {
//			name += epsResToName((int) config.get("escResolution")) + "_";
//		}
//		if (config.containsKey("autoProbFactor")) {
//			name += "mu" + (double) config.get("autoProbFactor") + "_";
//		}
//		if (config.containsKey("crossoverProb")) {
//			name += "xo" + (double) config.get("crossoverProb");
//		}
		if (config.containsKey("pls")) {
			name += PLSType.toShortString(((PLSType) config.get("pls")));
		}
		
		if (name.endsWith("_")) {
			name.substring(0, name.length() - 1);
		}		
		return name;
	}
	
	private static ArrayList<Entry<String, ArrayList<?>>> mapToEntryList(HashMap<String, ArrayList<?>> map) {
		ArrayList<Entry<String, ArrayList<?>>> entryList = 
				new ArrayList<Entry<String, ArrayList<?>>>();
		
		for (Entry<String, ArrayList<?>> entry : map.entrySet()) {
			entryList.add(entry);
		}
		
		return entryList;
	}
	
	private static void permuteMap(
			ArrayList<HashMap<String, ?>> result,
			int depth,
			ArrayList<Entry<String, ArrayList<?>>> src,
			HashMap<String, ?> current) {
		
		if (depth == src.size()) {
			result.add(current);
			return;
		}
		
		Entry<String, ArrayList<?>> varEntry = src.get(depth);
		for(Object varValue : varEntry.getValue()) {
			HashMap<String, Object> copy = copyMap(current);
			copy.put(varEntry.getKey(), varValue);
			permuteMap(result, depth + 1, src, copy);
		}
	}
	
	private static HashMap<String, Object> copyMap(HashMap<String, ?> src) {
		HashMap<String, Object> copy = new HashMap<String, Object>();
		copy.putAll(src);
		return copy;
	}
	
	private static String typeToName(EPSTypes type) {
		switch(type) {
		case CSV: return "csv";
		case H0: return "h0";
		case MC_FLAT: return "mcflat";
		case REMS: return "rems";
		case STEPS: return "steps";
		case WIK_BASED_THESIS: return "wik-based-thesis";
		case WIKHOURLY2015: return "wik-hourly-2015";
		case WIKHOURLY2020: return "wik-hourly-2020";
		case WIKHOURLY2025: return "wik-hourly-2025";
		case WIKWEEKDAY2015: return "wik-weekly-2015";
		case WIKWEEKDAY2020: return "wik-weekly-2020";
		case WIKWEEKDAY2025: return "wik-weekly-2025";
		case HOURLY_ALTERNATING: return "alt";
		default: return null;
		}
	}
	
	@SuppressWarnings("unused")
	private static String epsResToName(int epsResolution) {
		return "escRes-" + epsResolution;
	}
	
	private static boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				} 
			}
		}
		return( path.delete() );
	}

}
