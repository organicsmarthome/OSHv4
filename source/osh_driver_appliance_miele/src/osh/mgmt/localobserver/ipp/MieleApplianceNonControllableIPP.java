package osh.mgmt.localobserver.ipp;

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
 * @author Ingo Mauser
 *
 */
public class MieleApplianceNonControllableIPP extends NonControllableIPP<ISolution, IPrediction> {

	private static final long serialVersionUID = -5820880081859248470L;
	
	private SparseLoadProfile profile;
	private SparseLoadProfile lp;
	
	private LimitedCommodityStateMap[] allOutputStates;
	private long outputStatesCalculatedFor = Long.MIN_VALUE;
	
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected MieleApplianceNonControllableIPP() {
		super();
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public MieleApplianceNonControllableIPP(
			UUID deviceId, 
			IGlobalLogger logger, 
			long timestamp,
			SparseLoadProfile profile,
			boolean toBeScheduled,
			DeviceTypes deviceType,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		
		super(
				deviceId, 
				logger, 
				toBeScheduled, 
				false, //does not need ancillary meter
				false, //does not react to input states
				false, //is not static
				timestamp,
				deviceType,
				new Commodity[]{Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
				compressionType,
				compressionValue);

		this.profile = profile.cloneAfter(timestamp).getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue);
		
	}
	
	
	@Override
	public void initializeInterdependentCalculation(long maxReferenceTime,
			BitSet solution, int stepSize, boolean createLoadProfile,
			boolean keepPrediction) {
		
		if (maxReferenceTime != this.getReferenceTime()) {
			this.interdependentTime = maxReferenceTime;
			
		} else 
			this.interdependentTime = this.getReferenceTime();
		
		this.stepSize = stepSize;
		
		if (createLoadProfile)
			lp = profile.clone();
		else
			lp = null;
		
		
		if (outputStatesCalculatedFor != maxReferenceTime) {
			long time = maxReferenceTime;
			ObjectArrayList<LimitedCommodityStateMap> tempOutputStates = new ObjectArrayList<LimitedCommodityStateMap>();
			
			while (time < profile.getEndingTimeOfProfile()) {
				LimitedCommodityStateMap output = null;
				
				double activePower = profile.getAverageLoadFromTill(Commodity.ACTIVEPOWER, time, time + stepSize);
				double reactivePower = profile.getAverageLoadFromTill(Commodity.REACTIVEPOWER, time, time + stepSize);
				
				if (activePower != 0.0 || reactivePower != 0.0) {
					output = new LimitedCommodityStateMap(allOutputCommodities);
					output.setPower(Commodity.ACTIVEPOWER, activePower);
					output.setPower(Commodity.REACTIVEPOWER, reactivePower);
				}
				tempOutputStates.add(output);
				
				time += stepSize;
			}
			//add zero if optimisation goes longer then the profile
			LimitedCommodityStateMap output = new LimitedCommodityStateMap(new Commodity[]{Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER});
			output.setPower(Commodity.ACTIVEPOWER, 0.0);
			output.setPower(Commodity.REACTIVEPOWER, 0.0);
			tempOutputStates.add(output);
			
			allOutputStates = new LimitedCommodityStateMap[tempOutputStates.size()];
			allOutputStates = tempOutputStates.toArray(allOutputStates);
			outputStatesCalculatedFor = maxReferenceTime;
		}
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
			if (this.lp.getEndingTimeOfProfile() > 0) {
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
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		this.setReferenceTime(currentTime);
	}

	@Override
	public String problemToString() {
		if (profile.getEndingTimeOfProfile() != 0) {
			return "miele appl running till " + profile.getEndingTimeOfProfile();
		} else {
			return "miele appl not running";
		}

	}

}
