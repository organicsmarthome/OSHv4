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
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.esc.LimitedCommodityStateMap;
import osh.utils.BitSetConverter;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class MieleApplianceIPP extends ControllableIPP<ISolution, IPrediction> {

	private static final long serialVersionUID = -665656608383318760L;
	
	private long earliestStarttime;
	private long latestStarttime;
	private boolean predicted = false;

	private SparseLoadProfile profile;
	private SparseLoadProfile lp;
	
	private LimitedCommodityStateMap[] allOutputStates;
	private long outputStatesCalculatedFor;
	
	
	/** 
	 * CONSTRUCTOR 
	 * for serialization only, do NOT use */
	@Deprecated
	protected MieleApplianceIPP() {
		super();
	}
	
	/**
	 * CONSTRUCTOR
	 */
	public MieleApplianceIPP(
			UUID deviceId, 
			IGlobalLogger logger, 
			long timestamp,
			long earliestStarttime, 
			long latestStarttime,
			SparseLoadProfile profile,
			boolean toBeScheduled,
			boolean predicted,
			long optimizationHorizon,
			DeviceTypes deviceType,
			LoadProfileCompressionTypes compressionType,
			int compressionValue) {
	
		super(
				deviceId, 
				logger, 
				timestamp, 
				calculateBitCount(earliestStarttime, latestStarttime), 
				toBeScheduled, 
				false, //does not need ancillary meter
				false, //does not react to input states
				optimizationHorizon, 
				timestamp,
				deviceType,
				new Commodity[]{Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
				compressionType,
				compressionValue);
		
		if (profile == null) {
			throw new NullPointerException("profile is null");
		}
		
		this.earliestStarttime = earliestStarttime;
		this.latestStarttime = latestStarttime;
		this.profile = profile.getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue);
		this.predicted = predicted;
	}
	
	
	@Override
	public void initializeInterdependentCalculation(long maxReferenceTime,
			BitSet solution, int stepSize, boolean createLoadProfile,
			boolean keepPrediction) {
		
		this.interdependentTime = maxReferenceTime;		
		this.stepSize = stepSize;
		
		lp = profile.cloneWithOffset(earliestStarttime + getStartOffset(solution));

		long time = maxReferenceTime;
		ObjectArrayList<LimitedCommodityStateMap> tempOutputStates = new ObjectArrayList<LimitedCommodityStateMap>();

		while (time < lp.getEndingTimeOfProfile()) {
			LimitedCommodityStateMap output = null;
			double activePower = lp.getAverageLoadFromTill(Commodity.ACTIVEPOWER, time, time + stepSize);
			double reactivePower = lp.getAverageLoadFromTill(Commodity.REACTIVEPOWER, time, time + stepSize);
			
			if (activePower != 0.0 || reactivePower != 0) {
				output = new LimitedCommodityStateMap(allOutputCommodities);
				output.setPower(Commodity.ACTIVEPOWER, activePower);
				output.setPower(Commodity.REACTIVEPOWER, reactivePower);
			}
			tempOutputStates.add(output);
			time += stepSize;
		}
		//add zero if optimization goes longer then the profile
		LimitedCommodityStateMap output = new LimitedCommodityStateMap(allOutputCommodities);				
		output.setPower(Commodity.ACTIVEPOWER, 0.0);
		output.setPower(Commodity.REACTIVEPOWER, 0.0);
		tempOutputStates.add(output);
		
		allOutputStates = new LimitedCommodityStateMap[tempOutputStates.size()];
		allOutputStates = tempOutputStates.toArray(allOutputStates);
		
		outputStatesCalculatedFor = maxReferenceTime;	
		
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
	public String solutionToString(BitSet bits) {
		if (bits == null)
			return "ERROR: no solution bits";
		return "start time: " + getStartTime(bits);
	}

	
	@Override
	public Schedule getFinalInterdependentSchedule() {
		return new Schedule(lp, 0.0, this.getDeviceType().toString());
	}

	@Override
	public ISolution transformToPhenotype(BitSet solution) {
		return new MieleSolution(getStartTime(solution), predicted);
	}

	@Override
	public ISolution transformToFinalInterdependetPhenotype(BitSet solution) {
		return transformToPhenotype(solution);
	}

	@Override
	public void recalculateEncoding(long currentTime, long maxHorizon) {
		this.setReferenceTime(currentTime);
		if (earliestStarttime < currentTime) {
			if (currentTime > latestStarttime) {
				earliestStarttime = latestStarttime;
			} else {
				earliestStarttime = currentTime;
			}
			setBitCount(calculateBitCount(
					earliestStarttime,
					latestStarttime));
		}
	}

	@Override
	public String problemToString() {
		return "MieleIPP Profile: " + profile.toStringShort() + " DoF:" + earliestStarttime + "-" + latestStarttime
				+ "(" + (latestStarttime - earliestStarttime) + ")" + (predicted ? " (predicted)" : "");
	}
	
	public long getStartTime(BitSet solution) {
		return earliestStarttime + getStartOffset(solution);
	}
	
	/**
	 * returns the needed amount of bits for the EA
	 * 
	 * @param earliestStarttime
	 * @param latestStarttime
	 */
	private static int calculateBitCount(
			long earliestStarttime,
			long latestStarttime) {
		if (earliestStarttime > latestStarttime) {
			return 0;
		}

		long diff = latestStarttime - earliestStarttime + 1;
		int bits = (int) Math.ceil(Math.log(diff) / Math.log(2));

		return bits;
	}
	
	private long getStartOffset(BitSet solution) {
		long maxoffset = latestStarttime - earliestStarttime;
		return (long) Math.floor(Double.valueOf(BitSetConverter.gray2long(solution))
				/ Math.pow(2, getBitCount()) * maxoffset);
	}
}
