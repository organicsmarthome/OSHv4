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
public class SimulationElectricalEnergyGrid extends GridInstance {
	
	
	// ### Devices ###
	
	// Virtual Smart Meter
	private EnergySourceSink meter = 
			new EnergySourceSink(UUID.fromString("00000000-0000-0000-0000-000000000000"));
	
	// BASELOAD
	private EnergySourceSink baseload = 
			new EnergySourceSink(UUID.fromString("00000000-0000-5348-424c-000000000000"));
	
	// PV
	private EnergySourceSink pv = 
			new EnergySourceSink(UUID.fromString("484F4C4C-0000-0000-5056-000000000000"));
	
	// PVESHL
		private EnergySourceSink pvESHL = 
				new EnergySourceSink(UUID.fromString("7fc1f1d9-39c3-4e5f-8907-aeb0cd1ee84c"));
	
	// Battery
	private EnergySourceSink battery = 
			new EnergySourceSink(UUID.fromString("42415454-4552-5900-0000-000000000000"));
	
	// Dishwasher
	private EnergySourceSink applianceDW = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4457-000000000000"));
	
	// Dryer
	private EnergySourceSink applianceTD = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-5444-000000000000"));
	
	// Cooktop
	private EnergySourceSink applianceIH = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4948-000000000000"));
	
	// Oven
	private EnergySourceSink applianceOV = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4F56-000000000000"));
	
	// Washer
	private EnergySourceSink applianceWM = 
			new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-574D-000000000000"));
	
	// Dachs CHP
	private EnergySourceSink chp = 
			new EnergySourceSink(UUID.fromString("44414348-5300-0043-4850-000000000000"));
	
	// E.G.O. Insert Heating Element
	private EnergySourceSink ihe = 
			new EnergySourceSink(UUID.fromString("45474F00-0000-0049-4845-000000000000"));
	
	// Adsorption Chiller
	EnergySourceSink adsorptionChiller = 
			new EnergySourceSink(UUID.fromString("66a74134-4e47-4fc3-8519-acb2817ecd1a"));
	
	// Gas Heating
	private EnergySourceSink ghd = 
			new EnergySourceSink(UUID.fromString("00000000-0000-5748-4748-000000000000"));
	
	
	/**
	 * CONSTRUCTOR
	 */
	public SimulationElectricalEnergyGrid() {
		super();
		
		// ### Build sourceSinkList ###
		
		sourceSinkList.add(meter);
		sourceSinkList.add(baseload);
		sourceSinkList.add(pv);
		sourceSinkList.add(pvESHL);
		sourceSinkList.add(applianceDW);
		sourceSinkList.add(applianceTD);
		sourceSinkList.add(applianceWM);
		sourceSinkList.add(applianceIH);
		sourceSinkList.add(applianceOV);
		sourceSinkList.add(chp);
		sourceSinkList.add(ihe);
		sourceSinkList.add(ghd);
		sourceSinkList.add(battery);
		sourceSinkList.add(adsorptionChiller);
		
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
		
		// Meter <-> PV
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					pv,
					meter,
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER)
					);
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					pv,
					meter,
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
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
		
		// Meter <-> Gas Heating
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					ghd,
					meter,
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					ghd,
					meter,
					new Electrical(Commodity.REACTIVEPOWER), 
					new Electrical(Commodity.REACTIVEPOWER));
			relationList.add(relation);
		}
		
		
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
		
		// Meter <-> AdsorptionChiller
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					adsorptionChiller,
					meter,
					new Electrical(Commodity.ACTIVEPOWER), 
					new Electrical(Commodity.ACTIVEPOWER));
			relationList.add(relation);
		}
		{
			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
					adsorptionChiller,
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
		
		pvs.add(pv);
		pvs.add(pvESHL);
		chps.add(chp);
		batterys.add(battery);
		
		spec.put("pv", pvs);
		spec.put("chp", chps);
		spec.put("battery", batterys);
		
		specialSnowflakes.put(meter, spec);
	}	
}
