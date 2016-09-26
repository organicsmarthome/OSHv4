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
public class SimulationThermalEnergyGrid extends GridInstance {
	
	// ### Devices ###
	
	// Virtual Smart Meter
	private EnergySourceSink meter = 
			new EnergySourceSink(UUID.fromString("00000000-0000-0000-0000-000000000000"));
	
	// Dishwasher
	EnergySourceSink applianceDW = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4457-000000000000"));
	
	// Dryer
	EnergySourceSink applianceTD = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-5444-000000000000"));
	
	// Washer
	EnergySourceSink applianceWM = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-574D-000000000000"));
	
	// Cooktop / Induction Hob
	private EnergySourceSink applianceIH = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4948-000000000000"));
	
	// Oven
	private EnergySourceSink applianceOV = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4F56-000000000000"));
	
	// Hot Water Storage
	EnergySourceSink hotWaterStorage = 
			new EnergySourceSink(UUID.fromString("00000000-0000-4857-4853-000000000000"));
	
//	// Cold Water Storage
	
	// Dachs CHP
	EnergySourceSink chp = 
			new EnergySourceSink(UUID.fromString("44414348-5300-0043-4850-000000000000"));
	
	// E.G.O. Insert Heating Element
	EnergySourceSink ihe = 
			new EnergySourceSink(UUID.fromString("45474F00-0000-0049-4845-000000000000"));
	
	// Gas Heating
	EnergySourceSink ghd = 
			new EnergySourceSink(UUID.fromString("00000000-0000-5748-4748-000000000000"));
			
	
//	// Adsorption Chiller
	EnergySourceSink adsorptionChiller = 
			new EnergySourceSink(UUID.fromString("66a74134-4e47-4fc3-8519-acb2817ecd1a"));
	
//	// Cold Water Usage 
	EnergySourceSink coldWaterStorage = 
			new EnergySourceSink(UUID.fromString("441c234e-d340-4c85-b0a0-dbac182b8f81"));
	
	//Space Cooling
	EnergySourceSink spaceCooling = 
			new EnergySourceSink(UUID.fromString("0121431a-6960-46d7-b8e9-337f7135cb4d"));
	
	// Domestic Hot Water Usage 
	EnergySourceSink dhwUsage = 
			new EnergySourceSink(UUID.fromString("00000000-0000-5348-4448-000000000000"));
	
	// Space Heating
	EnergySourceSink spaceHeating = 
			new EnergySourceSink(UUID.fromString("00000000-0000-5348-5348-000000000000"));
	
	
	
	/**
	 * CONSTRUCTOR
	 */
	public SimulationThermalEnergyGrid() {
		
		// ### Build sourceSinkList ###
		
		sourceSinkList.add(meter);
		sourceSinkList.add(applianceDW);
		sourceSinkList.add(applianceTD);
		sourceSinkList.add(applianceWM);
		sourceSinkList.add(applianceIH);
		sourceSinkList.add(applianceOV);
		sourceSinkList.add(hotWaterStorage);
		sourceSinkList.add(chp);
		sourceSinkList.add(ihe);
		sourceSinkList.add(ghd);
		sourceSinkList.add(dhwUsage);
		sourceSinkList.add(spaceHeating);
		sourceSinkList.add(coldWaterStorage);
		sourceSinkList.add(adsorptionChiller);
		sourceSinkList.add(spaceCooling);
		
		// ### Relations ###
		
		// GHD <-> Hot Water Storage
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					ghd, 
					hotWaterStorage, 
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					ghd,
					meter, 
					new Thermal(Commodity.NATURALGASPOWER), 
					new Thermal(Commodity.NATURALGASPOWER));
			relationList.add(relation);
		}
		
		
		// IHE <-> Hot Water Storage
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					ihe, 
					hotWaterStorage, 
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		
		
		// Dachs CHP <-> Hot Water Storage
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					chp, 
					hotWaterStorage, 
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					chp,
					meter, 
					new Thermal(Commodity.NATURALGASPOWER), 
					new Thermal(Commodity.NATURALGASPOWER));
			relationList.add(relation);
		}
		
		
		// Hot Water Storage <-> Space Heating
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					spaceHeating,
					hotWaterStorage, 
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		
		// Hot Water Storage <-> Domestic Hot Water
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					dhwUsage,
					hotWaterStorage, 
					new Thermal(Commodity.DOMESTICHOTWATERPOWER), 
					new Thermal(Commodity.DOMESTICHOTWATERPOWER));
			relationList.add(relation);
		}
		
		// Hot Water Storage <-> Dishwasher
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					applianceDW,
					hotWaterStorage, 
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		
		// Hot Water Storage <-> Dryer
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					applianceTD,
					hotWaterStorage,
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		
		// Hot Water Storage <-> Washer
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					applianceWM,
					hotWaterStorage,
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		
		// Cooktop
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					applianceIH,
					meter,
					new Thermal(Commodity.NATURALGASPOWER), 
					new Thermal(Commodity.NATURALGASPOWER));
			relationList.add(relation);
		}
		
		// Oven
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					applianceOV,
					meter, 
					new Thermal(Commodity.NATURALGASPOWER), 
					new Thermal(Commodity.NATURALGASPOWER));
			relationList.add(relation);
		}
		
		// Cold Water Storage <-> Space Cooling
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					spaceCooling,
					coldWaterStorage,
					new Thermal(Commodity.COLDWATERPOWER), 
					new Thermal(Commodity.COLDWATERPOWER));
			relationList.add(relation);
		}
		
		// Cold Water Storage <-> Adsorption Chiller
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					adsorptionChiller,
					coldWaterStorage,
					new Thermal(Commodity.COLDWATERPOWER), 
					new Thermal(Commodity.COLDWATERPOWER));
			relationList.add(relation);
		}
		
		// Hot Water Storage <-> Adsorption Chiller
		{
			EnergyRelation<Thermal> relation = new EnergyRelation<Thermal>(
					adsorptionChiller,
					hotWaterStorage,
					new Thermal(Commodity.HEATINGHOTWATERPOWER), 
					new Thermal(Commodity.HEATINGHOTWATERPOWER));
			relationList.add(relation);
		}
		
		meters.add(meter);
	}	
}
