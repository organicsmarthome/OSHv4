package osh.mgmt.ipp;

import java.util.BitSet;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.esc.LimitedCommodityStateMap;

/** 
 * 
 * @author Sebastian Kramer, Ingo Mauser, Till Schuberth
 *
 */
public class PvNonControllableIPP extends NonControllableIPP<ISolution, IPrediction> {
	
	private static final long serialVersionUID = -5962394305617101302L;
	
	protected LimitedCommodityStateMap[] allOutputStates;
	long outputStatesCalculatedFor = Long.MIN_VALUE;	
	
	private SparseLoadProfile predictedPVProfile;
	private SparseLoadProfile lp = null;
	
	private long maxHorizon = Long.MIN_VALUE;
	

	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected PvNonControllableIPP() {
		super();
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public PvNonControllableIPP(UUID deviceId, IGlobalLogger logger, long timestamp, SparseLoadProfile predictedPVProfile,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		
		super(
				deviceId, 
				logger, 
				false, //causes rescheduling
				false, //does not need ancillary meter state
				false, //does not react to input states 
				false, //is not static
				timestamp, 
				DeviceTypes.PVSYSTEM,
				new Commodity[]{
						Commodity.ACTIVEPOWER,
						Commodity.REACTIVEPOWER
				},
				compressionType,
				compressionValue);		
		this.predictedPVProfile = predictedPVProfile.getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue);
	}
	

	@Override
	public void initializeInterdependentCalculation(
						long maxReferenceTime,
						BitSet solution, 
						int stepSize, 
						boolean createLoadProfile,
						boolean keepPrediction) {
		
		this.stepSize = stepSize;
		
		if (maxReferenceTime != this.getReferenceTime()) { 
			this.interdependentTime = maxReferenceTime;
		}
		else {
			this.interdependentTime = this.getReferenceTime();
		}
		
		if (createLoadProfile) {
			lp = predictedPVProfile.cloneAfter(maxReferenceTime);
		}
		else {
			lp = null;
		}
		
		
		if (outputStatesCalculatedFor != maxReferenceTime) {
			long time = maxReferenceTime;
			ObjectArrayList<LimitedCommodityStateMap> tempAllOutputStates = new ObjectArrayList<LimitedCommodityStateMap>();
			
			while (time < maxHorizon) {
				LimitedCommodityStateMap output = null;
				
				double actPower = (double) predictedPVProfile.getAverageLoadFromTill(Commodity.ACTIVEPOWER, time, time + stepSize);
				double reactPower = (double) predictedPVProfile.getAverageLoadFromTill(Commodity.REACTIVEPOWER, time, time + stepSize);
				if (actPower != 0 || reactPower != 0) {
					
					output = new LimitedCommodityStateMap(allOutputCommodities);
					output.setPower(Commodity.ACTIVEPOWER, actPower);
					output.setPower(Commodity.REACTIVEPOWER, reactPower);
				}				
				
				tempAllOutputStates.add(output);
				
				time += stepSize;
			}
			//add zero if optimization goes longer then the profile
			LimitedCommodityStateMap output = new LimitedCommodityStateMap(allOutputCommodities);
			output.setPower(Commodity.ACTIVEPOWER, 0.0);
			output.setPower(Commodity.REACTIVEPOWER, 0.0);
			tempAllOutputStates.add(output);
			
			allOutputStates = new LimitedCommodityStateMap[tempAllOutputStates.size()];
			allOutputStates = tempAllOutputStates.toArray(allOutputStates);
			
			outputStatesCalculatedFor = maxReferenceTime;
		}
		setOutputStates(null);
	}

	@Override
	public void calculateNextStep() {		
		int index = (int)((interdependentTime - outputStatesCalculatedFor) / stepSize);
		if (index < allOutputStates.length) {
			setOutputStates(allOutputStates[index]);
		} else {
			setOutputStates(null);
		}
		interdependentTime += stepSize;
	}

	@Override
	public Schedule getFinalInterdependentSchedule() {
		if (this.lp != null) {
			if (this.lp.getEndingTimeOfProfile() > this.interdependentTime) {
				this.lp.setLoad(
						Commodity.ACTIVEPOWER, 
						this.interdependentTime, 
						0);
				this.lp.setLoad(
						Commodity.REACTIVEPOWER, 
						this.interdependentTime, 
						0);
			}			
			return new Schedule(lp, 0.0, this.getDeviceType().toString());
		}
		else {
			return new Schedule(new SparseLoadProfile(), 0.0, this.getDeviceType().toString());
		}
	}

	@Override
	public void recalculateEncoding(long referenceTime, long maxHorizon) {
		this.setReferenceTime(referenceTime);
		this.maxHorizon = maxHorizon;		
	}

	@Override
	public String problemToString() {
		return "[" + getTimestamp() + "] PvIPP";
	}

}
