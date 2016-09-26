package osh.datatypes.power;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.NavigableSet;
import java.util.TreeMap;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;

/**
 * 
 * @author Ingo Mauser, Sebastian Kramer
 *
 */
public class AncillaryCommodityLoadProfile extends LoadProfile<AncillaryCommodity> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3370495091976922940L;
	
	private int[] sequentialFloorValues = 
			new int[AncillaryCommodity.values().length];
	private static final AncillaryCommodity[] ancillaryCommodityValues = AncillaryCommodity.values();

	
	public AncillaryCommodityLoadProfile() {
		super(AncillaryCommodity.class);
		
		for (AncillaryCommodity c : getEnumValues()) {
			TreeMap<Long, Tick> loadProfile = new TreeMap<>();
			commodities.put(c, loadProfile);
		}
	}
	
	/**
	 * Just for internal use (cloning)
	 * 
	 * @param commodities
	 * @param endingTimeOfProfile
	 */
	@Deprecated
	private AncillaryCommodityLoadProfile(
			EnumMap<AncillaryCommodity, TreeMap<Long, Tick>> commodities,
			long endingTimeOfProfile) {
		super(AncillaryCommodity.class);
		this.commodities = commodities;
		this.endingTimeOfProfile = endingTimeOfProfile;
	}
	
	public void initSequential() {
		Arrays.fill(sequentialFloorValues, Integer.MAX_VALUE);
//		for (AncillaryCommodity ac : getEnumValues()) {
//			sequentialMap.put(ac, new SimulatedSortedMap());
//		}
	}
	
	public void endSequential() {
//		for (AncillaryCommodity ac : getEnumValues()) {
//			commodities.put(ac, new TreeMap<Long, Tick>(sequentialMap.get(ac)));
//		}
	}
	
	/** WARNING: Do NOT use if you want to either set Loads nonsequential (now or in the future) or if you want to compress/merge/clone ...
	 * 
	 * 
	 * Sets the load of the provided ancillaryCommodity, but will ignore same power level inputs
	 * 
	 * @param states the ancillary meter states
	 * @param t the time to put the ancillary meter values
	 */
	public void setLoadSequential(AncillaryMeterState state, Long t) {
		
		double[] allPowers = state.getAllPowerStates();
		
		for (int i = 0; i < allPowers.length; i++) {
			int power = (int) allPowers[i];
			int oldPower = sequentialFloorValues[i];
			
			if (oldPower != power) {
				commodities.get(ancillaryCommodityValues[i]).put(t, new Tick(power));
				sequentialFloorValues[i] = power;
			}
		}
	}
	
	public NavigableSet<Long> getAllLoadChangesFor(AncillaryCommodity ac, long from, long to) {
		return commodities.get(ac).subMap(from, false, to, false).navigableKeySet();
	}

	@Override
	public AncillaryCommodityLoadProfile merge(
			LoadProfile<AncillaryCommodity> other, 
			long offset) {
		AncillaryCommodityLoadProfile merged = new AncillaryCommodityLoadProfile();
		this.merge(other, offset, merged);
		return merged;
	}
	
	@Override
	public AncillaryCommodityLoadProfile getProfileWithoutDuplicateValues() {
		AncillaryCommodityLoadProfile compressed = new AncillaryCommodityLoadProfile();
		this.getProfileWithoutDuplicateValues(compressed);
		return compressed;
	}
	
	@Override
	public AncillaryCommodityLoadProfile getCompressedProfile(LoadProfileCompressionTypes ct, int powerEps, int time) {
		AncillaryCommodityLoadProfile compressed = new AncillaryCommodityLoadProfile();
		this.getCompressedProfile(ct, powerEps, time, compressed);
		return compressed;
	}
	
	@Override
	public AncillaryCommodityLoadProfile getCompressedProfileByDiscontinuities(double powerEps) {
		AncillaryCommodityLoadProfile compressed = new AncillaryCommodityLoadProfile();
		this.getCompressedProfileByDiscontinuities(powerEps, compressed);
		return compressed;
	}
	
	@Override
	public AncillaryCommodityLoadProfile getCompressedProfileByTimeSlot(final int time) {
		AncillaryCommodityLoadProfile compressed = new AncillaryCommodityLoadProfile();
		this.getCompressedProfileByTimeSlot(time, compressed);
		return compressed;
	}
	
	@Override
	public AncillaryCommodityLoadProfile clone() {
		AncillaryCommodityLoadProfile clone = new AncillaryCommodityLoadProfile();
		this.clone(clone);
		return clone;
	}
	
	@Override
	public AncillaryCommodityLoadProfile cloneAfter(long timestamp) {
		AncillaryCommodityLoadProfile clone = new AncillaryCommodityLoadProfile();
		this.cloneAfter(timestamp, clone);
		return clone;
	}

	@Override
	public AncillaryCommodityLoadProfile cloneBefore(long timestamp) {
		AncillaryCommodityLoadProfile clone = new AncillaryCommodityLoadProfile();
		this.cloneBefore(timestamp, clone);
		return clone;
	}

	@Override
	public AncillaryCommodityLoadProfile cloneWithOffset(long offset) {
		AncillaryCommodityLoadProfile clone = new AncillaryCommodityLoadProfile();
		this.cloneWithOffset(offset, clone);
		return clone;
	}
	
	public AncillaryCommodityLoadProfile cloneOnlyDuration() {
		return new AncillaryCommodityLoadProfile(commodities, endingTimeOfProfile);
	}
}
