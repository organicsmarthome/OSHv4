package osh.esc.grid.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import osh.datatypes.commodity.Commodity;
import osh.esc.grid.EnergyRelation;
import osh.esc.grid.EnergySourceSink;
import osh.esc.grid.carrier.Electrical;


/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class ESHLElectricalEnergyGrid extends GridInstance {
	
	
	// ### Devices ###
	
	// Virtual Smart Meter
	private EnergySourceSink meter = 
			new EnergySourceSink(UUID.fromString("00000000-0000-0000-0000-000000000000"));
	
	// BASELOAD
	private EnergySourceSink baseload = 
			new EnergySourceSink(UUID.fromString("00000000-0000-5348-424c-000000000000"));
	
	// PVESHL
	private EnergySourceSink pvESHL = 
			new EnergySourceSink(UUID.fromString("7fc1f1d9-39c3-4e5f-8907-aeb0cd1ee84c"));
	
	// Battery
	private EnergySourceSink battery = 
			new EnergySourceSink(UUID.fromString("42415454-4552-5900-0000-000000000000"));
	
	// A/C inverter
	private EnergySourceSink acInverter = 
			new EnergySourceSink(UUID.fromString("61c8cf99-3321-4fc4-bc50-24289f62a49c"));
	
	// Dishwasher
	private EnergySourceSink applianceDW = 
			new EnergySourceSink(UUID.fromString("a01021ca-4d49-4d49-0000-5601c0a80114"));
	
	// Dryer
	private EnergySourceSink applianceTD = 
			new EnergySourceSink(UUID.fromString("a0102154-4d49-4d49-0000-5602c0a80114"));
	
	// Cooktop
	private EnergySourceSink applianceIH = 
			new EnergySourceSink(UUID.fromString("a010338a-4d49-4d49-0000-5e09c0a80114"));
	
	// Oven
	private EnergySourceSink applianceOV = 
			new EnergySourceSink(UUID.fromString("a0102159-4d49-4d49-0000-5e06c0a80114"));
	
	// Washer
	private EnergySourceSink applianceWM = 
			new EnergySourceSink(UUID.fromString("a0102151-4d49-4d49-0000-5604c0a80114"));
	
	// Coffeesystem
	private EnergySourceSink applianceCS = 
			new EnergySourceSink(UUID.fromString("a0103230-4d49-4d49-0000-5e0ac0a80114"));
	
	// Dachs CHP
	private EnergySourceSink chp = 
			new EnergySourceSink(UUID.fromString("e83c5db0-93d9-4a24-9e7a-c756b67e0802"));
	
//	// Insert Heating Element
	private EnergySourceSink ihe = 
			new EnergySourceSink(UUID.fromString("d23f44bc-1b3e-4e38-a8e8-17bd618b4fe0"));
	
	
	
	/**
	 * CONSTRUCTOR
	 */
	public ESHLElectricalEnergyGrid() {
		super();
		
		// ### Build sourceSinkList ###
		
		sourceSinkList.add(meter);
		sourceSinkList.add(baseload);
//		sourceSinkList.add(pv);
		sourceSinkList.add(pvESHL);
		sourceSinkList.add(applianceDW);
		sourceSinkList.add(applianceTD);
		sourceSinkList.add(applianceWM);
		sourceSinkList.add(applianceIH);
		sourceSinkList.add(applianceOV);
		sourceSinkList.add(applianceCS);
		sourceSinkList.add(chp);
		sourceSinkList.add(ihe);
//		sourceSinkList.add(ghd);
		sourceSinkList.add(battery);
		sourceSinkList.add(acInverter);
		
		// ### Relations ###
		
		// Meter <-> baseload
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					baseload,
					meter, 
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					baseload,
					meter,
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
//		// Meter <-> PV
//		{
//			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
//					pv,
//					meter,
//					new Electrical(Commodity.ACTIVEPOWER), 
//					new Electrical(Commodity.ACTIVEPOWER)
//					);
//			relationList.add(relation);
//		}
//		{
//			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
//					pv,
//					meter,
//					new Electrical(Commodity.REACTIVEPOWER), 
//					new Electrical(Commodity.REACTIVEPOWER));
//			relationList.add(relation);
//		}
		
		// Meter <-> PVESHL
				{
					EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
							pvESHL,
							meter,
							new Electrical(Commodity.ACTIVEPOWER), 
							new Electrical(Commodity.ACTIVEPOWER)
							);
					relationList.add(relation);
				}
				{
					EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
							pvESHL,
							meter,
							new Electrical(Commodity.REACTIVEPOWER), 
							new Electrical(Commodity.REACTIVEPOWER));
					relationList.add(relation);
				}
		
		// Meter <-> battery
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					battery,
					meter, 
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					battery,
					meter,
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		// Meter <-> A/C inverter
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					acInverter,
					meter, 
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					acInverter,
					meter,
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		// Meter <-> IHE
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					ihe,
					meter,
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					ihe,
					meter,
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
//		// Meter <-> Gas Heating
//		{
//			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
//					ghd,
//					meter,
//					new Electrical(Commodity.ACTIVEPOWER), 
//					new Electrical(Commodity.ACTIVEPOWER));
//			relationList.add(relation);
//		}
//		{
//			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
//					ghd,
//					meter,
//					new Electrical(Commodity.REACTIVEPOWER), 
//					new Electrical(Commodity.REACTIVEPOWER));
//			relationList.add(relation);
//		}
		
		
		// Meter <-> Dachs CHP
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					chp,
					meter, 
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					chp,
					meter, 
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		
		// Meter <-> Dishwasher
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceDW,
					meter, 
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceDW,
					meter,
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		// Meter <-> Cooktop / Induction Hob
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceIH,
					meter,
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceIH,
					meter,
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		// Meter <-> Oven
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceOV,
					meter,
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceOV,
					meter, 
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		// Meter <-> Dryer
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceTD,
					meter, 
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceTD,
					meter, 
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		// Meter <-> Washer
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceWM,
					meter,
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceWM,
					meter, 
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		// Meter <-> CoffeeSystem
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceCS,
					meter,
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					applianceCS,
					meter, 
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		meters.add(meter);
		
		Map<String, List<EnergySourceSink>> spec = new HashMap<String, List<EnergySourceSink>>();
		List<EnergySourceSink> pvs = new ArrayList<EnergySourceSink>();
		List<EnergySourceSink> chps = new ArrayList<EnergySourceSink>();
		List<EnergySourceSink> batterys = new ArrayList<EnergySourceSink>();
		
		pvs.add(pvESHL);
		chps.add(chp);
		batterys.add(battery);
		
		spec.put("pv", pvs);
		spec.put("chp", chps);
		spec.put("battery", batterys);
		
		specialSnowflakes.put(meter, spec);
	}	
}
