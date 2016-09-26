package constructsimulation.data;

import java.util.UUID;

import osh.configuration.system.DeviceTypes;


/**
 * 
 * @author Ingo Mauser
 *
 */
public class OldSimOSHConfigurationData {
	
	/* ######################
	 * # Yearly Consumption #
	 * ###################### */
	
	/** 
	 * Yearly Consumption in kWh (source: EA NRW 04/2011)<br>
	 * 1 - 6 persons (no electric water heating)
	 */
	public static int[] yearlyConsumption = {1798, 2850, 3733, 4480, 5311, 5816};
	/**
	 * Corrected consumption
	 */
	public static Integer[] yearlyIntelligentConsumption = {2019, 3084, 4019, 4729, 5570, null};
	
	
	/* ##########
	 * # System #
	 * ########## */
	
	public static String configFilesPath = "configfiles/";
	
	public static String systemPath = "system/";
	public static String screenplayFileName = "Screenplay";
	public static String descriptorFileName = "Descriptor.xml";
	
	public static String simulationPath = "simulation/";
	public static String eALConfigFileName = "EALConfig";
	public static String ocConfigFileName = "OCConfig";
	
	public static String mieleDeviceProfilesPath = "configfiles/driverMiele/";
	public static String screenplayMielePath = "sampleFiles/screenplayMiele/";
	
	public static String h0Filename = "configfiles/h0/H0Profile.csv";
	public static String ev0Filename = "configfiles/pv/ev0/EV0Profile.csv";
	
	public static String drawOffTypes = "configfiles/watertank/drawofftypes.csv";
	
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
	public static UUID comDeviceIdSimulationLoggerPart1 = UUID.fromString("11111111-c306-4fea-a487-1c48adc52ad4");
	public static UUID comDeviceIdSimulationLoggerNew = UUID.fromString("00000000-c306-4fea-a487-1c48adc52ad4");
	
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

	
	/* #######################
	 * # Intelligent Devices #
	 * ####################### */
	
	/* #################
	 * # Miele Devices #
	 * ################# */
	
//	public static String[] mieleDeviceProfilesPath = {
//		"configfiles/driver/",
//		"configfiles/driverComplex/"};
	
	public static DeviceTypes[] mieleDeviceTypes = {
		DeviceTypes.INDUCTIONCOOKTOP,
		DeviceTypes.COFFEESYSTEM,
		DeviceTypes.DISHWASHER,
		DeviceTypes.ELECTRICSTOVE,
		DeviceTypes.DRYER,
		DeviceTypes.WASHINGMACHINE};
	
	public static UUID[] mieleDeviceIds = {
		UUID.fromString("e2ef0d13-61b3-4188-b32a-1570dcbab4d1"), 
		UUID.fromString("de61f462-cda2-4941-8402-f93a1f1b3e57"),
		UUID.fromString("ab9519db-7a14-4e43-ac3a-ade723802194"),
		UUID.fromString("cef732b1-04ba-49e1-8189-818468a0d98e"),
		UUID.fromString("1468cc8a-dfdc-418a-8df8-96ba8c146156"),
		UUID.fromString("e7b3f13d-fdf6-4663-848a-222303d734b8")};
	
	public static String[] mieleLocalObserverClass = {
		osh.mgmt.localobserver.MieleApplianceLocalObserver.class.getName(),
		osh.mgmt.localobserver.MieleApplianceLocalObserver.class.getName(),
		osh.mgmt.localobserver.MieleApplianceLocalObserver.class.getName(),
		osh.mgmt.localobserver.MieleApplianceLocalObserver.class.getName(),
		osh.mgmt.localobserver.MieleApplianceLocalObserver.class.getName(),
		osh.mgmt.localobserver.MieleApplianceLocalObserver.class.getName()};
	
	public static String[] mieleLocalControllerClass = {
		osh.mgmt.localcontroller.MieleApplianceLocalController.class.getName(),
		osh.mgmt.localcontroller.MieleApplianceLocalController.class.getName(),
		osh.mgmt.localcontroller.MieleApplianceLocalController.class.getName(),
		osh.mgmt.localcontroller.MieleApplianceLocalController.class.getName(),
		osh.mgmt.localcontroller.MieleApplianceLocalController.class.getName(),
		osh.mgmt.localcontroller.MieleApplianceLocalController.class.getName()};
	
	public static boolean[] mieleDeviceControllable = {
		false,
		false,
		true,
		false,
		true,
		true};		
	
	/**
	 * E(X) = maxDof/2;
	 * X ~ Bin(maxDof, 0.5)
	 */
	public static String[] mieleDeviceMaxDoF = {
		"0",
		"0",
		"28800",
		"0",
		"28800",
		"28800"};
	public static String[] mieleDevice2ndDoF = {
		"0",
		"0",
		"1800",
		"0",
		"1800",
		"1800"};
//	public static String[] mieleDeviceMaxDoF = {
//		"0",
//		"0",
//		"0",
//		"0",
//		"0",
//		"0"};
//	public static String[] mieleDevice2ndDoF = {
//		"0",
//		"0",
//		"0",
//		"0",
//		"0",
//		"0"};
	
	/* ###########################
	 * ### Average Yearly Runs ###
	 * ########################### */
	
	/**
	 * source: EA NRW 04/2011<br>
	 * + own profiles...
	 */
	public static Double[][] mieleAverageYearlyRuns = {
		// 1p       2p ...
		{133.0, 255.0, 300.0, 360.0, 393.0,  null}, //COOKTOP
		{ null,  null,  null,  null,  null,  null}, //COFFEESYSTEM
		{126.0, 204.0, 282.0, 360.0, 432.0,  null}, //DISHWASHER
		{ 79.0, 151.0, 177.0, 213.0, 233.0,  null}, //STOVE
		{133.0, 236.0, 370.0, 443.0, 534.0,  null}, //DRYER
		{131.0, 209.0, 308.0, 390.0, 512.0,  null}  //WASHINGMACHINE
	};
	
	/* #########################
	 * ### Consumption Share ###
	 * ######################### */
	
	/**
	 * source: EA NRW 04/2011<br>
	 * "Herd" = STOVE + COOKTOP (about 50:50, see last living phase)<br>
	 * TODO: recalculate from "Erhebung: Wo im Haushalt bleibt der Strom?", slide 12 */
	public static Double[][] mieleConsumptionShare = {
		// 1p       2p ...
		{0.0480, 0.0580, 0.0520, 0.0520, 0.0480, 0.0470}, //COOKTOP
		{null, null, null, null, null, null},			  //COFFEESYSTEM
		{0.0280, 0.0510, 0.0610, 0.0700, 0.0720, 0.0720}, //DISHWASHER
		{0.0480, 0.0580, 0.0520, 0.0520, 0.0480, 0.0470}, //STOVE
		{0.0260, 0.0540, 0.0770, 0.0930, 0.1010, 0.1040}, //DRYER
		{0.0430, 0.0480, 0.0540, 0.0570, 0.0630, 0.0640}  //WASHINGMACHINE
	};
	
	/* ###########################
	 * ### Stock of Equipment ###
	 * ########################### */
	
	/**
	 * source 1:<br>
	 * Ausstattung privater Haushalte mit ausgewählten Gebrauchsgütern - Fachserie 15 Heft 1 - 2008<br>
	 * DESTATIS 2008, 1-5 persons per HH (Annahme: 6pax = 5pax)<br>
	 * IMPORTANT: Ausstattungs-GRAD */
//	public static Double[][] mieleDegreeOfEquipment = {
//		{null, null, null, null, null, null}, 		//COOKTOP
//		{null, null, null, null, null, null},		//COFFEESYSTEM
//		{0.384, 0.694, 0.833, 0.906, 0.932, 0.932}, //DISHWASHER
//		{null, null, null, null, null, null}, 		//STOVE
//		{0.207, 0.412, 0.537, 0.650, 0.697, 0.697}, //DRYER
//		{null, null, null, null, null, null} 		//WASHINGMACHINE
//	};
	/**
	 * source 2:<br>
	 * Ausstattung privater Haushalte mit ausgewählten Gebrauchsgütern - Fachserie 15 Heft 1 - 2008<br>
	 * DESTATIS 2008, 1-5 persons per HH (Annahme: 6pax = 5pax)<br>
	 * IMPORTANT: Ausstattungs-Bestand */
//		public static Double[][] mieleDegreeOfEquipment = {
//		{null, null, null, null, null, null}, 		//COOKTOP
//		{null, null, null, null, null, null},		//COFFEESYSTEM
//		{0.386, 0.704, 0.851, 0.922, 0.954, 0.954}, //DISHWASHER
//		{null, null, null, null, null, null}, 		//STOVE
//		{0.207, 0.414, 0.541, 0.652, 0.705, 0.705}, //DRYER
//		{null, null, null, null, null, null} 		//WASHINGMACHINE
//	};
	/**
	 * source 3:<br>
	 * Ausstattung privater Haushalte mit ausgewählten Gebrauchsgütern<br> 
	 * - Fachserie 15 Reihe 2 - 2000 bis 2006<br>
	 * DESTATIS 29.03.2011<br>
	 * https://www.destatis.de/DE/Publikationen/Thematisch/EinkommenKonsumLebensbedingungen/LfdWirtschaftsrechnungen/AusstattungprivaterHaushalte2150200067004.pdf?__blob=publicationFile<br>
	 * IMPORTANT: Ausstattungs-Bestand */
//		public static Double[][] mieleDegreeOfEquipment = {
//		{null, null, null, null, null, null}, 		//COOKTOP
//		{null, null, null, null, null, null},		//COFFEESYSTEM
//		{0.380, null, null, null, null, null}, 		//DISHWASHER
//		{null, null, null, null, null, null}, 		//STOVE
//		{0.190, null, null, null, null, null}, 		//DRYER
//		{0.901, null, null, null, null, null} 		//WASHINGMACHINE
//	};	
	/**
	 * source 4:<br>
	 * Ausstattung privater Haushalte mit ausgewählten Gebrauchsgütern<br>
	 * - Fachserie 15 Reihe 2 - 2010<br>
	 * DESTATIS 11.08.2011, 1-5 persons per HH (Annahme: 6pax = 5pax)<br>
	 * IMPORTANT: Ausstattungs-Bestand<br>
	 * 2P = (1.173 * Alleinerziehend mit Kind(ern) + 10.661 * Paare ohne Kind) / (1.173 + 10.661)<br>
	 * 3P = (1.935 * Paare mit Kind) / 1.935<br>
	 * 4P = (2.241 * Paare mit 2 Kindern) / 2.241<br>
	 * 5P = Paare mit 3 oder mehr Kindern<br>
	 * 6P = Paare mit 3 oder mehr Kindern<br>
	 * Anmerkung: 58% der Alleinerziehenden haben nur 1 Kind */
//	public static Double[][] mieleDegreeOfEquipment = {
//		{null, null, null, null, null, null}, 		//COOKTOP
//		{null, null, null, null, null, null},		//COFFEESYSTEM
//		{	0.432, 
//			(1.173*0.710+10.661*0.776)/(1.173+10.661), 
//			0.870, 0.939, 0.955, 0.955}, 			//DISHWASHER
//		{null, null, null, null, null, null}, 		//STOVE
//		{	0.242, 
//			(1.173*0.370+10.661*0.456)/(1.173+10.661), 
//			0.533, 0.646, 0.690, 0.690}, 			//DRYER
//		{null, null, null, null, null, null} 		//WASHINGMACHINE
//	};
	/**
	 * RESULT: Stock of Equipment
	 * source 4: DISHWASHER & DRYER
	 * source 3: WASHINGMASHINE 1PAX
	 */
	public static Double[][] mieleDegreeOfEquipment = {
		{null, null, null, null, null, null}, 		//COOKTOP
		{null, null, null, null, null, null},		//COFFEESYSTEM
		{0.432, 0.769, 0.870, 0.939, 0.955, 0.955}, //DISHWASHER
		{null, null, null, null, null, null}, 		//STOVE
		{0.242, 0.447, 0.533, 0.646, 0.690, 0.690}, //DRYER
		{0.901, null, null, null, null, null} 		//WASHINGMACHINE
	};
		
	
	/* #######################
	 * # Refrigerator Device #
	 * ####################### */
	
//	public static UUID[] refrigeratorDeviceIds = {
//		UUID.fromString(null)};
	
	/** source: EA NRW 04/2011 */
	public static Double[][] refrigeratorConsumptionShare = {
		{0.185, 0.138, 0.115, 0.101, 0.089, 0.089} //REFRIDGERATOR
	};
	
	/**
	 * source:<br>
	 * Ausstattung privater Haushalte mit ausgewählten Gebrauchsgütern - Fachserie 15 Heft 1 - 2008<br>
	 * DESTATIS 2008, 1-5 persons per HH (Annahme: 6pax = 5pax)<br>
	 * IMPORTANT: Stock of Equipment (not Degree of Equipment) */
	public static Double[][] refrigeratorDegreeOfEquipment = {
		{1.040, 1.253, 1.331, 1.382, 1.412, 1.412}, //REFRIDGERATOR
	};
	
	
	/* ######################
	 * # DeepFreezer Device #
	 * ###################### */
	
//	public static UUID[] deepFreezerDeviceIds = {
//		UUID.fromString(null)};
	
	/** source: EA NRW 04/2011 */
	public static Double[][] deepFreezerConsumptionShare = {
		{0.031, 0.055, 0.056, 0.057, 0.056, 0.060} //DEEPFREEZER
	};
	
	/**
	 * source:<br>
	 * Ausstattung privater Haushalte mit ausgewählten Gebrauchsgütern - Fachserie 15 Heft 1 - 2008<br>
	 * DESTATIS 2008, 1-5 persons per HH (Annahme: 6pax = 5pax)<br>
	 * IMPORTANT: Stock of Equipment (not Degree of Equipment) */
	public static Double[][] deepFreezerDegreeOfEquipment = {
		{0.347, 0.675, 0.743, 0.839, 0.957, 0.957} // DEEPFREEZER
	};
	

	/* #############
	 * # PV Device #
	 * ############# */
	
	public static UUID[] pvDeviceIds = {
		UUID.fromString("f72c8ecb-7b82-4c8d-89db-751c9a139075")};
	
	public static String[] pvLocalObserverClass = {
		osh.mgmt.localobserver.PvLocalObserver.class.getName()};

	public static String[] pvLocalControllerClass = {
		osh.mgmt.localcontroller.PvLocalController.class.getName()};
	
	public static String[] pvDriverClass = {
		osh.driver.simulation.PvSimulationDriverEv0.class.getName()};
	
	
	/* ###################
	 * # Baseload Device #
	 * ################### */
	
	public static UUID baseloadDeviceId = 
			UUID.fromString("d313ddbb-b991-46d7-8572-26ae9359a621");
	
	public static String baseloadLocalObserverClass = 
			osh.mgmt.localobserver.BaseloadLocalObserver.class.getName();
	
	public static String baseloadLocalControllerClass =
			null;
	
	public static String baseloadDriverClass = 
			osh.driver.simulation.BaseloadSimulationDriver.class.getName();
	
	public static double baseloadCosPhi = 0.99;
	
	public static boolean baseloadIsInductive = true;
	
	public static Double[] baseloadConsumptionShare = {
		0.7186, // 1 PAX
		0.6755,
		0.6539,
		0.6405,
		0.6369,
		null
	};
	
	
	/* ####################
	 * # Dachs-CHP Device #
	 * #################### */
	
	public static UUID chpDeviceId = 
			UUID.fromString("e83c5db0-93d9-4a24-9e7a-c756b67e0802");
	
	
}
