package osh.datatypes.power;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import osh.datatypes.commodity.Commodity;

public class SparseLoadProfile extends LoadProfile<Commodity> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1683959091461015374L;

	public SparseLoadProfile() {
		super(Commodity.class);
	}
	
	/**
	 * Just for internal use (cloning)
	 * 
	 * @param commodities
	 * @param endingTimeOfProfile
	 */
	@Deprecated
	private SparseLoadProfile(
			EnumMap<Commodity, TreeMap<Long, Tick>> commodities,
			long endingTimeOfProfile) {
		super(Commodity.class);
		this.commodities = commodities;
		this.endingTimeOfProfile = endingTimeOfProfile;
	}
	
	@Override
	public SparseLoadProfile merge(
			LoadProfile<Commodity> other, 
			long offset) {
		SparseLoadProfile merged = new SparseLoadProfile();
		this.merge(other, offset, merged);
		
		return merged;
	}
	
	@Override
	public SparseLoadProfile getProfileWithoutDuplicateValues() {
		SparseLoadProfile compressed = new SparseLoadProfile();
		this.getProfileWithoutDuplicateValues(compressed);
		return compressed;
	}
	
	@Override
	public SparseLoadProfile getCompressedProfile(LoadProfileCompressionTypes ct, int powerEps, int time) {
		SparseLoadProfile compressed = new SparseLoadProfile();
		this.getCompressedProfile(ct, powerEps, time, compressed);
		return compressed;
	}
	
	@Override
	public SparseLoadProfile getCompressedProfileByDiscontinuities(double powerEps) {
		SparseLoadProfile compressed = new SparseLoadProfile();
		this.getCompressedProfileByDiscontinuities(powerEps, compressed);
		return compressed;
	}
	
	@Override
	public SparseLoadProfile getCompressedProfileByTimeSlot(final int time) {
		SparseLoadProfile compressed = new SparseLoadProfile();
		this.getCompressedProfileByTimeSlot(time, compressed);
		return compressed;
	}
	
	@Override
	public SparseLoadProfile clone() {
		SparseLoadProfile clone = new SparseLoadProfile();
		this.clone(clone);
		return clone;
	}
	
	@Override
	public SparseLoadProfile cloneAfter(long timestamp) {
		SparseLoadProfile clone = new SparseLoadProfile();
		this.cloneAfter(timestamp, clone);
		return clone;
	}

	@Override
	public SparseLoadProfile cloneBefore(long timestamp) {
		SparseLoadProfile clone = new SparseLoadProfile();
		this.cloneBefore(timestamp, clone);
		return clone;
	}

	@Override
	public SparseLoadProfile cloneWithOffset(long offset) {
		SparseLoadProfile clone = new SparseLoadProfile();
		this.cloneWithOffset(offset, clone);
		return clone;
	}
	
	public SparseLoadProfile cloneOnlyDuration() {
		return new SparseLoadProfile(commodities, endingTimeOfProfile);
	}
	
	
	/************************
	 * static methods
	 ************************/
	
	public static SparseLoadProfile[][] getCompressedLoadProfilesByTimeSlot(SparseLoadProfile[][] original, final int time) {
		SparseLoadProfile[][] compressed = null;
		if (original != null) {
			compressed = new SparseLoadProfile[original.length][];
			for (int i = 0; i < original.length; i++) {
				compressed[i] = SparseLoadProfile.getCompressedLoadProfilesByTimeSlot(original[i], time);
			}
		}
		return compressed;
	}
	
	public static SparseLoadProfile[] getCompressedLoadProfilesByTimeSlot(SparseLoadProfile[] original, final int time) {
		SparseLoadProfile[] compressed = null;
		if (original != null) {
			compressed = new SparseLoadProfile[original.length];
			for (int i = 0; i < original.length; i++) {
				compressed[i] = (SparseLoadProfile) original[i].getCompressedProfileByTimeSlot(time);
			}
		}
		return compressed;
	}
	
	public static SparseLoadProfile[][] getCompressedLoadProfilesByDiscontinuities(SparseLoadProfile[][] original, final int powerEps) {
		SparseLoadProfile[][] compressed = null;
		if (original != null) {
			compressed = new SparseLoadProfile[original.length][];
			for (int i = 0; i < original.length; i++) {
				compressed[i] = SparseLoadProfile.getCompressedLoadProfilesByDiscontinuities(original[i], powerEps);
			}
		}
		return compressed;
	}
	
	public static SparseLoadProfile[] getCompressedLoadProfilesByDiscontinuities(SparseLoadProfile[] original, final int powerEps) {
		SparseLoadProfile[] compressed = null;
		if (original != null) {
			compressed = new SparseLoadProfile[original.length];
			for (int i = 0; i < original.length; i++) {
				compressed[i] = (SparseLoadProfile) original[i].getCompressedProfileByDiscontinuities(powerEps);
			}
		}
		return compressed;
	}
	
	public static SparseLoadProfile[][] getCompressedProfile(
			LoadProfileCompressionTypes ct, 
			SparseLoadProfile[][] original, 
			final int powerEps, 
			final int time) {
		if (ct == LoadProfileCompressionTypes.DISCONTINUITIES) {
			return getCompressedLoadProfilesByDiscontinuities(original, powerEps);
		}
		else if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
			return getCompressedLoadProfilesByTimeSlot(original, time);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Convert and do NOT compress
	 * @param powerProfile
	 * @return
	 */
	public static SparseLoadProfile convertToSparseProfile(
			EnumMap<Commodity, ArrayList<PowerProfileTick>> powerProfile,
			LoadProfileCompressionTypes ct,
			final int timeSlotDuration) {
		// conversion without compression
		return convertToSparseProfile(
				powerProfile, 
				ct,
				-1,
				timeSlotDuration);
	}
	
	public static SparseLoadProfile[] getCompressedProfile(
			LoadProfileCompressionTypes ct, 
			SparseLoadProfile[] original, final int powerEps, final int time) {
		if (ct == LoadProfileCompressionTypes.DISCONTINUITIES) {
			return getCompressedLoadProfilesByDiscontinuities(original, powerEps);
		}
		if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
			return getCompressedLoadProfilesByTimeSlot(original, time);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Convert and compress
	 * @param powerProfiles
	 * @param powerEps max delta value for new point
	 * @return
	 */
	public static SparseLoadProfile convertToSparseProfile(
			EnumMap<Commodity,ArrayList<PowerProfileTick>> powerProfiles,
			LoadProfileCompressionTypes ct,
			int powerEps,
			final int timeSlotDuration) {
		SparseLoadProfile profile = new SparseLoadProfile();
		return convertToSparseProfile(powerProfiles, ct, powerEps, timeSlotDuration, profile);
	}
	
	protected static SparseLoadProfile convertToSparseProfile(
			EnumMap<Commodity,ArrayList<PowerProfileTick>> powerProfiles,
			LoadProfileCompressionTypes ct,
			int powerEps,
			final int timeSlotDuration,
			SparseLoadProfile profile) {		
		
		if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
			powerEps = -1;
		}
		
		for (Entry<Commodity, ArrayList<PowerProfileTick>> e : powerProfiles.entrySet()) {
			
			int powerLastAvg = 0;
			long powerAvgSum = 0;
			long powerAvgCnt = 0;

			long i = 0;
			long prevI = 0;
			
			List<PowerProfileTick> powerProfile = e.getValue();
			Commodity currentCommodity = e.getKey();
			
			for (PowerProfileTick p : powerProfile) {
				if (i == 0) {
					powerAvgSum = powerAvgSum + p.load;
					powerAvgCnt++;
					powerLastAvg = (int) Math.round(powerAvgSum / powerAvgCnt);

				} 
				else if (i == (powerProfile.size() - 1)) {
					profile.setLoad(currentCommodity, prevI, powerLastAvg);

					profile.setLoad(currentCommodity, i, p.load);
				} 
				else {
					if (Math.abs(powerLastAvg - p.load) > powerEps) {

						profile.setLoad(currentCommodity, prevI, powerLastAvg);
						powerLastAvg = p.load;
						powerAvgSum = p.load;
						powerAvgCnt = 1;

						prevI = i;
					} 
					else {
						powerAvgSum = powerAvgSum + p.load;
						powerAvgCnt++;
						powerLastAvg = (int) Math.round(powerAvgSum / powerAvgCnt);
					}
				}
				i++;
			}
			
		}
		
		if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
			profile = profile.getCompressedProfile(ct, powerEps, timeSlotDuration);
		}

		return profile;
	}
	
	public EnumMap<Commodity, Map<Long, Integer>> convertToSimpleMap() {
		EnumMap<Commodity, Map<Long, Integer>> ret = new EnumMap<Commodity, Map<Long, Integer>>(Commodity.class);
		
		for (Entry<Commodity, TreeMap<Long, Tick>> en : commodities.entrySet()) {
			
			if (en.getValue().isEmpty()) {
				continue;
			}
			
			TreeMap<Long, Integer> map = new TreeMap<Long, Integer>();
			ret.put(en.getKey(), map);
			
			Iterator<Entry<Long, Tick>> it = en.getValue().entrySet()
					.iterator();
			
			Entry<Long, Tick> entry = getNext(it, this.endingTimeOfProfile);

			while (entry != null) {
				map.put(entry.getKey(), entry.getValue().value);
				entry = getNext(it, this.endingTimeOfProfile);
			}
			
			if (!map.isEmpty()) {
				map.put(this.endingTimeOfProfile, map.floorEntry(this.endingTimeOfProfile).getValue());
				//just to be sure
				map.put(this.endingTimeOfProfile + 1, 0);
			}			
		}
		return ret;
	}
	
	public EnumMap<Commodity, Map<Long, Integer>> convertToSimpleMap(long maxTime) {
		long endingTime = Math.min(maxTime, endingTimeOfProfile);
		EnumMap<Commodity, Map<Long, Integer>> ret = new EnumMap<Commodity, Map<Long, Integer>>(Commodity.class);
		
		for (Entry<Commodity, TreeMap<Long, Tick>> en : commodities.entrySet()) {
			
			if (en.getValue().isEmpty()) {
				continue;
			}
			
			TreeMap<Long, Integer> map = new TreeMap<Long, Integer>();
			ret.put(en.getKey(), map);
			
			Iterator<Entry<Long, Tick>> it = en.getValue().entrySet()
					.iterator();
			
			Entry<Long, Tick> entry = getNext(it, endingTime);

			while (entry != null) {
				map.put(entry.getKey(), entry.getValue().value);
				entry = getNext(it, endingTime);
			}
			
			if (!map.isEmpty()) {
				map.put(endingTime, map.floorEntry(endingTime).getValue());
				//just to be sure
				map.put(endingTime + 1, 0);
			}			
		}
		return ret;
	}
}
