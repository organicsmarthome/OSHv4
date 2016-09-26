package osh.toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import osh.configuration.appliance.miele.DeviceProfile;
import osh.configuration.appliance.miele.ProfileTick;
import osh.configuration.appliance.miele.ProfileTicks;
import osh.configuration.appliance.miele.ProfileTick.Load;
import osh.datatypes.commodity.Commodity;
import osh.utils.xml.XMLSerialization;

/**
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class generateDeviceProfile {
	
	static boolean hasProfile = true;
	
	static boolean isIntelligent = true;
//	static boolean isIntelligent = false;
	
//	static String inputFile = "sampleFiles/*/clean/*.csv";
//	static String outputFile = "configFiles/appliances/*/*.xml";
	
	static String inputFile = "sampleFiles/dryer/td_bosch/heatpumpDryer_HI_2186Wh_Bosch_4stunden.csv";
	static String outputFile = "configFiles/appliances/dryer/heatpumpDryer_HI_2186Wh_Bosch_4stunden.xml";
	
//	static String inputFile = "sampleFiles/MieleProfilesComplex/COFFEESYSTEM_FAKE_40Wh.csv";
//	static String outputFile = "configFiles/driverMiele/COFFEESYSTEM_Profile.xml";
	
//	static String inputFile = "sampleFiles/fzi/clean/clean_dryer_bosch_wtw86562_1b_-_3.csv";
//	static String outputFile = "configFiles/appliances/dryer/bosch/wtw86562/dryer_bosch_wtw86562_1b_-_3.xml";
	
//	static String inputFile = "sampleFiles/MieleProfilePhasesHybrid/washingMachine_MID_1039Wh_old_hybrid_Phase1.csv.csv";
//	static String outputFile = "configFiles/appliances/mi/mi/w3985/MID_654Wh_hybrid.xml";
	
	// Robert
	static double gasEfficiency = 0.8;
	
//	static String inputFile = "sampleFiles/robert/spuelmaschine_hybrid.csv";
//	static String outputFile = "configFiles/robert/spuelmaschine_hybrid.xml";
//	static String inputFile = "sampleFiles/robert/spuelmaschine.csv";
//	static String outputFile = "configFiles/robert/spuelmaschine.xml";
	
//	static String inputFile = "sampleFiles/robert/trockner_hybrid.csv";
//	static String outputFile = "configFiles/robert/trockner_hybrid.xml";
//	static String inputFile = "sampleFiles/robert/trockner.csv";
//	static String outputFile = "configFiles/robert/trockner.xml";
	
//	static String inputFile = "sampleFiles/robert/waschmaschine_hybrid.csv";
//	static String outputFile = "configFiles/robert/waschmaschine_hybrid.xml";
//	static String inputFile = "sampleFiles/robert/waschmaschine.csv";
//	static String outputFile = "configFiles/robert/waschmaschine.xml";
	
	// KIT
//	static int activePowerColumn = 0;	 	// N/A = -1
//	static int reactivePowerColumn = 1; 	// N/A = -1
//	static int gasPowerColumn = -1;			// N/A = -1
	// FZI
//	static int activePowerColumn = 1; 		// N/A = -1
//	static int reactivePowerColumn = -1; 	// N/A = -1
//	static int gasPowerColumn = -1;
	// Robert
//	static int activePowerColumn = 0;
//	static int reactivePowerColumn = -1;
//	static int gasPowerColumn = -1;
//	static int gasPowerColumn = 1;
	// Julian
	static int activePowerColumn = 0;
	static int reactivePowerColumn = 1; // N/A = -1
	static int gasPowerColumn = -1;
	

	public static void main(String[] args) {
		DeviceProfile deviceProfile = new DeviceProfile();
		
		deviceProfile.setHasProfile(hasProfile);
		deviceProfile.setIntelligent(isIntelligent);
		
		ProfileTicks profileTicks = new ProfileTicks();
		deviceProfile.setProfileTicks(profileTicks);
		
		List<ProfileTick> ticks = profileTicks.getProfileTick();
		
		File temp = new File(inputFile);
		System.out.println(temp.getAbsolutePath());
		
		FileReader fr;
		BufferedReader br = null;
		String line;

		try {
			fr = new FileReader(inputFile);
			br = new BufferedReader(fr);
		
			line = "";
			while (true) {
				try {
					line = br.readLine();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
				
				if ( line == null || line.equals("") ) {
					break;
				}
				
				String[] splittedLine = line.split(";");
				
				String activePower = splittedLine[activePowerColumn];
				if (activePowerColumn != -1) {
					activePower = splittedLine[activePowerColumn];
				}
				else {
					activePower = "0";
				}
				
				String reactivePower;
				if (reactivePowerColumn != -1) {
					reactivePower = splittedLine[reactivePowerColumn];
				}
				else {
					reactivePower = "0";
				}
				
				String naturalgasPower;
				if (gasPowerColumn != -1) {
					naturalgasPower = splittedLine[gasPowerColumn];
					int naturalGasPowerInt = Integer.valueOf(naturalgasPower);
					naturalGasPowerInt = (int) Math.round(naturalGasPowerInt / gasEfficiency);
					naturalgasPower = "" + naturalGasPowerInt;
				}
				else {
					naturalgasPower = "0";
				}
				
				ProfileTick tick = new ProfileTick();
				List<Load> loadList = tick.getLoad();
				
				Load activePowerLoad = new Load();
				activePowerLoad.setCommodity(Commodity.ACTIVEPOWER.toString());
				activePowerLoad.setValue(Integer.valueOf(activePower));
				loadList.add(activePowerLoad);

				Load reactivePowerLoad = new Load();
				reactivePowerLoad.setCommodity(Commodity.REACTIVEPOWER.toString());
				reactivePowerLoad.setValue(Integer.valueOf(reactivePower));
				loadList.add(reactivePowerLoad);
				
				Load naturalgasPowerLoad = new Load();
				naturalgasPowerLoad.setCommodity(Commodity.NATURALGASPOWER.toString());
				naturalgasPowerLoad.setValue(Integer.valueOf(naturalgasPower));
				loadList.add(naturalgasPowerLoad);
				
				ticks.add(tick);
			}
			
			br.close();
		
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			XMLSerialization.marshal2File(outputFile, deviceProfile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("DONE");
		
	}
}
