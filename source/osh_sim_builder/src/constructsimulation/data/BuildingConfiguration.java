package constructsimulation.data;

import java.util.UUID;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class BuildingConfiguration {
	
	/* ##########
	 * # System #
	 * ########## */
	
	public static String configFilesPath = "configfiles/";
	
	public static String systemPath = "system/";
	public static String screenplayFileName = "Screenplay";
	public static String descriptorFileName = "Descriptor.xml";
	
	public static String simulationPath = "simulation/";
	public static String EALConfigFileName = "EALConfig";
	public static String ocConfigFileName = "OCConfig";
	public static String calConfigFileName = "CALConfig";
	public static String oshConfigFileName = "OSHConfig";
	
	public static String mieleDeviceProfilesPath = "configfiles/driverMiele/";
	public static String screenplayMielePath = "sampleFiles/screenplayMiele/";
	
	public static String h0Filename1Hour = "configfiles/h0/H0ProfileNew.csv";
	public static String h0Filename15Min = "configfiles/h0/H0Profile15MinWinterSummerIntermediate.csv";
	public static String ev0Filename = "configfiles/pv/ev0/EV0Profile.csv";
	
	/* ###################
	 * # Global O/C Unit #
	 * ################### */
	
	/** UUID of Global O/C Unit */
	public static UUID globalOCUnitUUID = UUID.fromString("e5ad4b36-d417-4be6-a1c8-c3ad68e52977");
	
	
	/* #########################
	 * # Communication Devices #
	 * ######################### */
	
	public static UUID comDeviceIdEPS = UUID.fromString("0909624f-c281-4713-8bbf-a8eaf3f8e7d6");
	public static UUID comDeviceIdPLS = UUID.fromString("99999999-c281-4713-8bbf-a8eaf3f8e7d6");
	public static UUID comDeviceIdDoF = UUID.fromString("32c8b193-6c86-4abd-be5a-2e49fee11535");
	public static UUID comDeviceIdGui = UUID.fromString("6e95bc70-57cb-11e1-b86c-0800200c9a66");
	
	/* #################
	 * # EPS ComDevice #
	 * ################# */
	
	public static String epsComManagerClass = osh.mgmt.commanager.EpsProviderComManager.class.getName();
	// epsComDriverClassName depends on simulation type
	
	/* #################
	 * # PLS ComDevice #
	 * ################# */
	
	public static String plsComManagerClass = osh.mgmt.commanager.PlsProviderComManager.class.getName();
	// plsComDriverClassName depends on simulation type
	
	/* #################
	 * # Gui ComDevice #
	 * ################# */
	public static String guiComManagerClass = osh.mgmt.commanager.GuiComManager.class.getName();
	public static String guiComDriverClass = osh.comdriver.simulation.GuiComDriver.class.getName();
	
	/**
	 * sources:<br>
	 * 
	 */
	public static int[] avgYearlyConsumption = 
		// 1p, 2p, ...
		{2000,	3100,	4000,	4700,	5200}; 

	// ### Baseload
	public static UUID baseloadUUID = UUID.fromString("00000000-0000-5348-424C-000000000000"); // SH-BL
	
	public static int[] avgYearlyBaseloadConsumption = 
		// 1p, 2p, ...
		{1426,	2097,	2628,	2993,	3370}; 
	
	public static double baseloadCosPhi = 0.99;
	public static boolean baseloadIsInductive = true;
	
	// ### Big 5 ###
	
	public static UUID[] applianceUUID = {
		UUID.fromString("00000000-4D49-4D49-4457-000000000000"), // DW
		UUID.fromString("00000000-4D49-4D49-4948-000000000000"), // IH
		UUID.fromString("00000000-4D49-4D49-4F56-000000000000"), // OV
		UUID.fromString("00000000-4D49-4D49-5444-000000000000"), // TD
		UUID.fromString("00000000-4D49-4D49-574D-000000000000"), // WM
	};
	
	/**
	 * sources:<br>
	 * DESTATIS Fachserie 15 Reihe 2, etc........
	 */
	public static int[][] averageYearlyRuns = {
				// 1p, 	2p, 	...
		/* DW */  {90,	160,	240,	310,	340},
		/* IH */ {170,	300,	350,	400,	420},
		/* OV */ { 85,	150,	175,	200,	210},
		/* TD */ { 80,	140,	210,	270,	280},
		/* WM */ {120,	200,	280,	360,	420} 
	};
	
	/**
	 * sources:<br>
	 * 
	 */
	public static double[][] configurationShares = {
				// 0	1    2   ...
		/* DW */ {0.2, 0.3, 0.3, 0.2},
		/* IH */ {0.4, 0.4, 0.2},
		/* OV */ {0.4, 0.4, 0.2},
//		/* TD */ {0.2, 0.5, 0.3},
		/* TD */ {0.0, 0.2, 0.8},
		/* WM */ {0.2, 0.5, 0.3} 
	};
	
	// ### FreezerRefrigerator (FR) ###
	public static UUID freezerRefrigeratorUUID = UUID.fromString("00000000-0000-0000-4652-000000000000"); //FR
	
	// constant power of 30 Watt
	public static int averagePowerFR = 30;
	/**
	 * sources:<br>
	 * DESTATIS Fachserie 15 Reihe/Heft?
	 */
	public static double[] numberOfFR = {1.05, 1.3, 1.35, 1.4, 1.5};

	// ### CoffeeSystem (CS) ###
	// N/A
	
	// ### Cooker Hood (HD) ###
	// idea: synchronize with hob/cooktop (IH)? no.
	// N/A
	
	// ### MicroWave ###
	// N/A
	
	
	// #### Distributed Generation ####
	
	// ### PV System ###
	public static UUID pvUUID = UUID.fromString("484F4C4C-0000-0000-5056-000000000000"); //HOLL...PV
	/** 
	 * Nominal ActivePower of PV device in W (Ppeak/Pn = 4600 W)<br>
	 * IMPORTANT: negative value*/
	public static String[] pvNominalPower = {"-2000", "-3000", "-4000", "-4500", "-5000"};
	
	// ### CHP ###
	public static UUID chpUUID = UUID.fromString("44414348-5300-0043-4850-000000000000"); //DACH-S...C-HP
	
	// ### Battery Storage ###
	public static UUID batteryUUID = UUID.fromString("42415454-4552-5900-0000-000000000000"); //BATT-ER-Y...
	
	public static int batteryMinChargingState = 0;
	public static int batteryMaxChargingState = 5 * 1000 * 3600; // [Ws]
	public static int batteryMinDischargePower = -3500; // [W]
	public static int batteryMaxDischargePower = -100;
	public static int batteryMinChargePower = 100; // [W]
	public static int batteryMaxChargePower = 3500;
	public static int batteryMinInverterPower = -1000000000; // [W]
	public static int batteryMaxInverterPower = 1000000000;
	
	
	// #### THE SMART HOME ####
	
	// ### IHE ###
	public static UUID iheUUID = UUID.fromString("45474F00-0000-0049-4845-000000000000"); //EGO...I-HE
	
	// ### Gas Heating / Boiler ###
	public static UUID gasHeatingUUID = UUID.fromString("00000000-0000-5748-4748-000000000000"); // WH-GH
	
	// ### Hot WaterStorage ###
	public static UUID hotWaterTankUUID = UUID.fromString("00000000-0000-4857-4853-000000000000"); //HW-HS
	
	// ### Domestic Hot Water Usage ###
	public static UUID dhwUsageUUID = UUID.fromString("00000000-0000-5348-4448-000000000000"); //SH-DH
	
	// ### Space Heating ###
	public static UUID spaceHeatingUUID = UUID.fromString("00000000-0000-5348-5348-000000000000"); //SH-SH
	
	// ### Space Cooling ###
	public static UUID spaceCoolingUUID = UUID.fromString("00000000-0000-5348-5343-000000000000"); //SH-SC
	
	
	// ### Virtual Smart Meter ###
	public static UUID meterUUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //SH-SC
	
	
}
