package osh.mgmt.ipp;

import java.util.BitSet;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.esc.LimitedCommodityStateMap;

/**
 * 
 * @author Ingo Mauser, Jan Mueller
 *
 */
public class HotWaterDemandNonControllableIPP 
					extends ThermalDemandNonControllableIPP {
	
	private static final long serialVersionUID = -1011574853269626608L;
	
	private SparseLoadProfile powerPrediction;	
	private Commodity usedCommodity;
	
	
	/**
	 * CONSTRUCTOR
	 */
	public HotWaterDemandNonControllableIPP(
			UUID deviceId, 
			DeviceTypes deviceType,
			IGlobalLogger logger,
			long now,
			boolean toBeScheduled,
			SparseLoadProfile powerPrediction,
			Commodity usedCommodity,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super(
				deviceId, 
				logger, 
				toBeScheduled, 
				false, //does not need ancillary meter state as Input State
				false, //does not react to input states
				now,
				deviceType,
				new Commodity[]{
						usedCommodity
				},
				compressionType,
				compressionValue);
		
		this.powerPrediction = powerPrediction.getCompressedProfile(
				this.compressionType, 
				this.compressionValue, 
				this.compressionValue);
		this.usedCommodity = usedCommodity;
	}
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected HotWaterDemandNonControllableIPP() {
		super();
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
			this.lp = powerPrediction.cloneAfter(maxReferenceTime);
		else
			this.lp = null;
		
		if (outputStatesCalculatedFor != maxReferenceTime) {
			long time = maxReferenceTime;
//			ObjectArrayList<EnumMap<Commodity, RealCommodityState>> tempAllOutputStates = new ObjectArrayList<EnumMap<Commodity, RealCommodityState>>();
			ObjectArrayList<LimitedCommodityStateMap> tempAllOutputStates = new ObjectArrayList<LimitedCommodityStateMap>();
			
			Commodity[] usedCommodities = new Commodity[] {usedCommodity};
			
			double lastPower = -1;
			
			while (time < maxHorizon) {
//				EnumMap<Commodity, RealCommodityState> output = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
				LimitedCommodityStateMap output = null;
				double power = (double) powerPrediction.getAverageLoadFromTill(usedCommodity, time, time + stepSize);
				
				if (power != 0 || lastPower != 0) {
					output = new LimitedCommodityStateMap(usedCommodities);
					output.setPower(usedCommodity, power);
					
					lastPower = power;
				}
				
//				output.put(usedCommodity, new RealThermalCommodityState(usedCommodity, 
//						, 0.0, null));
				tempAllOutputStates.add(output);
				
				time += stepSize;
			}
			//add zero if optimization goes longer then the profile
//			EnumMap<Commodity, RealCommodityState> output = new EnumMap<Commodity, RealCommodityState>(Commodity.class);				
//			output.put(usedCommodity, new RealThermalCommodityState(usedCommodity, 0.0, 0.0, null));
//			tempAllOutputStates.add(output);
			LimitedCommodityStateMap output = new LimitedCommodityStateMap(usedCommodities);
			output.setPower(usedCommodity, 0);
			tempAllOutputStates.add(output);
			
//			allOutputStates = (EnumMap<Commodity, RealCommodityState>[]) new EnumMap<?, ?>[tempAllOutputStates.size()];
			allOutputStates = new LimitedCommodityStateMap[tempAllOutputStates.size()];
			allOutputStates = tempAllOutputStates.toArray(allOutputStates);
			outputStatesCalculatedFor = maxReferenceTime;
		}
		
//		interdependentOutputStates = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
		setOutputStates(null);
	}

	
	@Override
	public Schedule getFinalInterdependentSchedule() {		
		if (this.lp != null) {
			if (this.lp.getEndingTimeOfProfile() > this.interdependentTime) {
				this.lp.setLoad(
						usedCommodity, 
						this.interdependentTime, 
						0);
			}			
			return new Schedule(lp, this.interdependentCervisia, this.getDeviceType().toString());
		}
		else {
			return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
		}
	}

	// ### to string ###
	
	@Override
	public String problemToString() {
		return "[" + getTimestamp() + "] HotWaterDemandNonControllableIPP";
	}
	
}
