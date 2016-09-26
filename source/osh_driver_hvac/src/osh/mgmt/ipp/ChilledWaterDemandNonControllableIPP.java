package osh.mgmt.ipp;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.exception.EnergySimulationException;
import osh.utils.time.TimeConversion;

/**
 * 
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Julian Feder
 *
 */
public class ChilledWaterDemandNonControllableIPP 
					extends ThermalDemandNonControllableIPP {

	private static final long serialVersionUID = 3835919942638394624L;
	
	private final ArrayList<ChillerCalendarDate> dates;
	private ArrayList<ChillerCalendarDate> datesForEvaluation = null;
	private final Map<Long, Double> temperaturePrediction;
	private double coldWaterPower = 0;

	
	/**
	 * CONSTRUCTOR
	 */
	public ChilledWaterDemandNonControllableIPP(
			UUID deviceId, 
			IGlobalLogger logger,
			long now,
			boolean toBeScheduled,
			ArrayList<ChillerCalendarDate> dates,
			Map<Long, Double> temperaturePrediction,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super(
				deviceId, 
				logger,
				toBeScheduled,
				false,	//needsAncillaryMeterstate
				false,	//reactsToInputStates
				now,
				DeviceTypes.SPACECOOLING,
				new Commodity[]{
						Commodity.COLDWATERPOWER,
				},
				compressionType,
				compressionValue
				);
		
		this.dates = new ArrayList<ChillerCalendarDate>();
		for (int i = 0; i < dates.size(); i++) {
			ChillerCalendarDate date = new ChillerCalendarDate(
					dates.get(i).getStartTimestamp(), 
					dates.get(i).getlength(), 
					dates.get(i).getAmountOfPerson(),
					dates.get(i).getSetTemperature(),
					dates.get(i).getKnownPower());
			this.dates.add(date);
		}
		
		this.temperaturePrediction = temperaturePrediction;
		
	}
	
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected ChilledWaterDemandNonControllableIPP() {
		super();
		temperaturePrediction = new HashMap<Long, Double>();
		dates = new ArrayList<ChillerCalendarDate>();
	}

	
	// ### interdependent problem part stuff ###

	@Override
	public void initializeInterdependentCalculation(
			long maxReferenceTime,
			BitSet solution,
			int stepSize,
			boolean calculateLoadProfile,
			boolean keepPrediction) {
		
		if (maxReferenceTime != this.getReferenceTime()) 
			this.interdependentTime = maxReferenceTime;
		else 
			this.interdependentTime = this.getReferenceTime();
		
		this.stepSize = stepSize;
		
		if (calculateLoadProfile)
			this.lp = new SparseLoadProfile();
		else
			this.lp = null;
		
		this.interdependentCervisia = 0.0;

		this.coldWaterPower = 0;
		this.datesForEvaluation = new ArrayList<ChillerCalendarDate>();
		for (int i = 0; i < dates.size(); i++) {
			ChillerCalendarDate date = new ChillerCalendarDate(
					dates.get(i).getStartTimestamp(), 
					dates.get(i).getlength(), 
					dates.get(i).getAmountOfPerson(),
					dates.get(i).getSetTemperature(),
					dates.get(i).getKnownPower());
			this.datesForEvaluation.add(date);
		}
		
		if (outputStatesCalculatedFor != maxReferenceTime) {
			long time = maxReferenceTime;
			ObjectArrayList<LimitedCommodityStateMap> tempAllOutputStates = new ObjectArrayList<LimitedCommodityStateMap>();
			
			while (time < maxHorizon) {
				LimitedCommodityStateMap output = null;
				
				coldWaterPower = 0;
				// Date active?
				if(!datesForEvaluation.isEmpty()) {
					
					ChillerCalendarDate date = datesForEvaluation.get(0);
				
					if (date.getStartTimestamp() <= time 
							&& date.getStartTimestamp() + date.getlength() >= time) {
						
						long secondsFromYearStart = TimeConversion.convertUnixTime2SecondsFromYearStart(time);
						
						double outdoorTemperature = temperaturePrediction.get((secondsFromYearStart / 300) * 300); // keep it!!
						coldWaterPower = Math.max(0, ((0.4415 * outdoorTemperature) - 9.6614) * 1000);
						
//						if (demand < 0) {
//							System.out.println("Demand:" + demand + "outdoor: " + currentOutdoorTemperature);
//						}
					}
					else if(date.getStartTimestamp() + date.getlength() < time) {
						datesForEvaluation.remove(0);
					}
					
					output = new LimitedCommodityStateMap(allOutputCommodities);
					output.setPower(Commodity.COLDWATERPOWER, coldWaterPower);
				}				
				tempAllOutputStates.add(output);
				
				if (lp != null)
					lp.setLoad(Commodity.COLDWATERPOWER, time, (int) coldWaterPower); 
				
				time += stepSize;
			}
			//add zero if optimisation goes longer then the profile
			LimitedCommodityStateMap output = new LimitedCommodityStateMap(allOutputCommodities);
			output.setPower(Commodity.COLDWATERPOWER, 0.0);
			tempAllOutputStates.add(output);
			
			allOutputStates = new LimitedCommodityStateMap[tempAllOutputStates.size()];
			allOutputStates = tempAllOutputStates.toArray(allOutputStates);
			if (lp != null)
				lp.setLoad(Commodity.COLDWATERPOWER, time, 0); 
			outputStatesCalculatedFor = maxReferenceTime;
		}
		setOutputStates(null);
		
	}

	
	@Override
	public Schedule getFinalInterdependentSchedule() {

		if (lp == null) {
			return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
		}
		else {
			SparseLoadProfile slp = lp.getCompressedProfile(compressionType, compressionValue, compressionValue);
			return new Schedule(slp, this.interdependentCervisia, this.getDeviceType().toString());
		}
	}

	@Override
	public void setCommodityInputStates(
			LimitedCommodityStateMap inputStates,
//			EnumMap<AncillaryCommodity, AncillaryCommodityState> ancillaryInputStates)
			AncillaryMeterState ancillaryMeterState)
			throws EnergySimulationException {
		//Do Nothing
	}
	
	// ### to string ###
	
	@Override
	public String problemToString() {
		return "SpaceCooling NonControllableIPP";
	}
	
}
