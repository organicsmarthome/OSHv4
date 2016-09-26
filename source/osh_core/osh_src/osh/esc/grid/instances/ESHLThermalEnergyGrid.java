package osh.esc.grid.instances;

import java.util.UUID;

import osh.datatypes.commodity.Commodity;
import osh.esc.grid.EnergyRelation;
import osh.esc.grid.EnergySourceSink;
import osh.esc.grid.carrier.Thermal;


/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class ESHLThermalEnergyGrid extends GridInstance {
	
	/**
	 * CONSTRUCTOR
	 */
	public ESHLThermalEnergyGrid() {
		// ### Devices ###
		
		// Virtual Smart Meter
		EnergySourceSink meter = 
				new EnergySourceSink(UUID.fromString("00000000-0000-0000-0000-000000000000"));
		
		// Combined Hot Water Storage
		EnergySourceSink combinedHotWaterStorage = 
				new EnergySourceSink(UUID.fromString("268ea9bd-572c-46dd-a383-960b4ed65337"));
		
		// CHP
		EnergySourceSink chp = 
				new EnergySourceSink(UUID.fromString("e83c5db0-93d9-4a24-9e7a-c756b67e0802"));
		
		// Electrical Insert Heating Element
		EnergySourceSink electricHeater = 
				new EnergySourceSink(UUID.fromString("d23f44bc-1b3e-4e38-a8e8-17bd618b4fe0"));

		// Domestic Hot Water Usage
		EnergySourceSink dhwu = 
				new EnergySourceSink(UUID.fromString("db56fbb0-c305-4361-839a-9f2ba5809611"));
		
		// Space Heating Hot Water Usage
		EnergySourceSink spaceHeating = 
				new EnergySourceSink(UUID.fromString("2a6e51d7-2f18-4034-bd9a-4ed68acc7bfe"));
		
		
		// Chilled Water Storage
		EnergySourceSink chilledWaterStorage = 
				new EnergySourceSink(UUID.fromString("bc2b5c73-ca4d-42c3-ac5a-56064e0d8112"));
		
		// Space Cooling Hot Water Usage
		EnergySourceSink spaceCooling = 
				new EnergySourceSink(UUID.fromString("a3f762e1-373d-4aa2-b1c3-44967646762a"));
		
		
		// ### Relations ###
		
		// Combined Hot Water Storage <-> CHP
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					chp,
					combinedHotWaterStorage, 
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		
		// CHP <-> Meter
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					chp,
					meter, 
					new Thermal(Commodity.NATURALGASPOWER), 
					new Thermal(Commodity.NATURALGASPOWER));
			relationList.add(relation);
		}
		 
		// Combined Hot Water Storage <-> Electric Heater
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					electricHeater,
					combinedHotWaterStorage, 
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		
		// Combined Hot Water Storage <-> Domestic Hot Water Usage
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					dhwu,
					combinedHotWaterStorage, 
					new Thermal(Commodity.DOMESTICHOTWATERPOWER), 
					new Thermal(Commodity.DOMESTICHOTWATERPOWER));
			relationList.add(relation);
		}
		
		// Combined Hot Water Storage <-> Heating Hot Water Usage
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					spaceHeating,
					combinedHotWaterStorage,
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}	
		
		// Combined Hot Water Storage <-> Space Heating
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					spaceHeating,
					combinedHotWaterStorage,
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		
		
		// Chilled Water Storage <-> Space Cooling
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					spaceCooling,
					chilledWaterStorage,
					new Thermal(Commodity.COLDWATERPOWER), 
					new Thermal(Commodity.COLDWATERPOWER));
			relationList.add(relation);
		}
		
		meters.add(meter);
	}

	
	
	//OLD!!
	// HotWaterStorage
	
//	private UUID centralHeatingId = UUID
//			.fromString("34693aa1-4a15-4504-b6d8-931658b81f09");
	
	// CHP

//	private UUID chpId = UUID
//			.fromString("05971dd0-34d1-40bb-9cb6-7a498460fcef");
	
	
	// DomesticHotWater
	
//	private UUID domesticHotWaterId = UUID
//			.fromString("1cbd8276-e615-492e-ad2f-a51a8ffc353c");
	
	
	// SpaceHeaters/Radiators
//	private UUID heatingDeviceId_Bath = UUID
//			.fromString("e3870611-34a4-4be5-8ab6-d50a11630881");
//	private UUID heatingDeviceId_Bed1 = UUID
//			.fromString("fa064943-fbd7-4f89-86c5-35dee53d49fa");
//	private UUID heatingDeviceId_Bed2 = UUID
//			.fromString("da9d58bd-d043-46d0-8398-19e95871f7ee");
//	private UUID heatingDeviceId_Living1 = UUID
//			.fromString("9220e62f-1bf4-48dd-9ffb-ff1a11dac4b2");
//	private UUID heatingDeviceId_Living2 = UUID
//			.fromString("c43e2ae7-4ee9-4530-b84c-b7cd8e261f28");
	

}
