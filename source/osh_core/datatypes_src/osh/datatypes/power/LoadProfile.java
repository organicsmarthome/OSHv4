package osh.datatypes.power;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Sebastian Kramer, Ingo Mauser, Till Schuberth
 *
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class LoadProfile<C extends Enum<C>> implements ILoadProfile<C>, Serializable {

	private static final long serialVersionUID = 4196506764284322858L;
	
	@XmlType
	@XmlAccessorType(XmlAccessType.FIELD)
	public class Tick implements Serializable {
		/** Serial */
		public static final long serialVersionUID = 3044446898974710703L;
		public int value;
//		public long positiveIntegral;
//		public long negativeIntegral;


		public Tick() {
		}
		
		public Tick(int value) {
			this.value = value;
		}

		public Tick(Tick other) {
			this.value = other.value;
//			this.positiveIntegral = other.positiveIntegral;
//			this.negativeIntegral = other.negativeIntegral;
		}

		@Override
		public String toString() {
			return value + "W";
		}
		
		public String toLongString() {
			return value + "W  P/Q+";// + positiveIntegral + "Ws  P/Q-" + negativeIntegral
					//+ "Ws";
		}

		@Override
		public Tick clone() {
			return new Tick(this);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
//			result = prime * result + (int) (negativeIntegral ^ (negativeIntegral >>> 32));
//			result = prime * result + (int) (positiveIntegral ^ (positiveIntegral >>> 32));
			result = prime * result + value;
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tick other = (Tick) obj;
//			if (negativeIntegral != other.negativeIntegral)
//				return false;
//			if (positiveIntegral != other.positiveIntegral)
//				return false;
			if (value != other.value)
				return false;
			return true;
		}
	}
	
	protected Class<C> enumType;

	protected EnumMap<C, TreeMap<Long, Tick>> commodities;

	protected long endingTimeOfProfile;

	public LoadProfile(Class<C> enumType) {
		commodities = new EnumMap<>(enumType);
		this.enumType = enumType;
		for (C commodity : getEnumValues()) {
			TreeMap<Long, Tick> profile = new TreeMap<Long, Tick>();
			commodities.put(commodity, profile);
		}

		this.endingTimeOfProfile = 0;
	}

	/**
	 * EndingTime is defined as the point in time (timestamp) where
	 * the profile stops having a value other than 0, NOT the length of the profile.
	 * See {@link ILoadProfile#getEndingTimeOfProfile()}
	 */
	public void setEndingTimeOfProfile(long endingTimeOfProfile) {
		this.endingTimeOfProfile = endingTimeOfProfile;
	}

	protected TreeMap<Long, Tick> getLoadProfile(C commodity) {
		return commodities.get(commodity);
	}

	public void setLoad(C commodity, long t, int power) {
		Tick v = new Tick();
		v.value = power;

		TreeMap<Long, Tick> loadProfile = getLoadProfile(commodity);
		if (loadProfile == null) {
			loadProfile = new TreeMap<>();
			commodities.put(commodity, loadProfile);
		}

		loadProfile.put(t, v);
		if (this.endingTimeOfProfile < t + 1) {
			if (this.endingTimeOfProfile == t 
					&& power == 0) {
				//do nothing
				// KEEP this: only 1 tick with value 0 -> no tick
			} 
			else {
				this.endingTimeOfProfile = t + 1;
			}
		}		
	}

	protected <T> Entry<Long, T> getNext(
			Iterator<Entry<Long, T>> it,
			long duration) {
		if (it.hasNext()) {
			Entry<Long, T> e = it.next();
			if (e.getKey() < duration)
				return e;
			else
				return null;
		} else
			return null;
	}
	
	public Iterator<Entry<Long, Tick>> getIteratorForSubMap(C c, long from, long to) {
		return commodities.get(c).subMap(from, false, to, false).entrySet().iterator();
	}
	
	public Entry<Long, Tick> getFloorEntry(C c, long t) {
		return commodities.get(c).floorEntry(t);
	}
	
	@Override
	public ILoadProfile<C> merge(ILoadProfile<C> other, long offset) {
		if (other instanceof LoadProfile) {
			ILoadProfile<C> merged = merge((LoadProfile<C>) other, offset);
			if (this.getEndingTimeOfProfile() != 0 || other.getEndingTimeOfProfile() != 0) {
				if (merged.getEndingTimeOfProfile() == 0) {
					// still possible because of offset, but not normal
					System.out.println("ERROR: merged.getEndingTimeOfProfile() == 0, although one profile not 0");
				}
			}
			return merged;
		} 
		else {
			if (other == null) {
				throw new NullPointerException("other is null");
			}
			throw new UnsupportedOperationException();
		}
	}
	
	public abstract LoadProfile<C> merge(
			LoadProfile<C> other, 
			long offset);

	
	protected void merge(
			LoadProfile<C> other, 
			long offset,
			LoadProfile<C> merged) {

		merged.setEndingTimeOfProfile(Math.max(this.endingTimeOfProfile, other.endingTimeOfProfile + offset));

		for (C commodity : getEnumValues()) {
			TreeMap<Long, Tick> loadProfile1 = this.getLoadProfile(commodity);
			TreeMap<Long, Tick> loadProfile2 = other.getLoadProfile(commodity);

			Iterator<Entry<Long, Tick>> iSet1 = loadProfile1.entrySet()
					.iterator();
			Iterator<Entry<Long, Tick>> iSet2 = loadProfile2.entrySet()
					.iterator();

			Entry<Long, Tick> entry1 = null;
			Entry<Long, Tick> entry2 = null;

			int activeValue1 = 0;
			int activeValue2 = 0;

			entry1 = getNext(iSet1, this.endingTimeOfProfile);
			entry2 = getNext(iSet2, other.endingTimeOfProfile);

			while (entry1 != null && entry2 != null) {

				if (entry1.getKey() < entry2.getKey() + offset) {
					merged.setLoad(commodity, entry1.getKey(),
							entry1.getValue().value + activeValue2);

					activeValue1 = entry1.getValue().value;

					entry1 = getNext(iSet1, this.endingTimeOfProfile);
				} else if (entry1.getKey() > entry2.getKey() + offset) {
					merged.setLoad(commodity, entry2.getKey() + offset,
							activeValue1 + entry2.getValue().value);

					activeValue2 = entry2.getValue().value;

					entry2 = getNext(iSet2, other.endingTimeOfProfile);
				} else /* (entry1.getKey() == entry2.getKey() + offset) */{
					merged.setLoad(commodity, entry2.getKey() + offset,
							entry1.getValue().value + entry2.getValue().value);

					activeValue1 = entry1.getValue().value;
					activeValue2 = entry2.getValue().value;

					entry1 = getNext(iSet1, this.endingTimeOfProfile);
					entry2 = getNext(iSet2, other.endingTimeOfProfile);
				}
			}

			while (entry1 != null) { // 1st profile still has data points
				if (entry1.getKey() < other.endingTimeOfProfile + offset) {
					merged.setLoad(commodity, entry1.getKey(),
							entry1.getValue().value + activeValue2);
					activeValue1 = entry1.getValue().value;
				} else { // 2nd profile has ended
					if (activeValue2 != 0) {
						merged.setLoad(commodity, other.endingTimeOfProfile + offset,
								activeValue1);
						activeValue2 = 0;
					}
					merged.setLoad(commodity, entry1.getKey(),
							entry1.getValue().value + activeValue2);
				}

				entry1 = getNext(iSet1, this.endingTimeOfProfile);
			}
			while (entry2 != null) {
				if (entry2.getKey() + offset < this.endingTimeOfProfile) {
					merged.setLoad(commodity, entry2.getKey() + offset,
							entry2.getValue().value + activeValue1);
					activeValue2 = entry2.getValue().value;
				} else {
					if (activeValue1 != 0) {
						merged.setLoad(commodity, endingTimeOfProfile, activeValue2);
						activeValue1 = 0;
					}
					merged.setLoad(commodity, entry2.getKey() + offset,
							entry2.getValue().value + activeValue1);
				}

				entry2 = getNext(iSet2, other.endingTimeOfProfile);
			}

			// handling the end of profiles
			if (activeValue1 != 0 && activeValue2 != 0) {
				if (this.endingTimeOfProfile > other.endingTimeOfProfile + offset) {
					merged.setLoad(commodity, other.endingTimeOfProfile + offset,
							activeValue1);
				} else if (this.endingTimeOfProfile < other.endingTimeOfProfile + offset) {
					merged.setLoad(commodity, this.endingTimeOfProfile, activeValue2);
				} else { /* == */
					assert (this.endingTimeOfProfile == merged.endingTimeOfProfile);
				}
			} else if (activeValue2 != 0) {
				merged.setLoad(commodity, other.endingTimeOfProfile + offset, activeValue1);
			} else if (activeValue1 != 0) {
				merged.setLoad(commodity, endingTimeOfProfile, activeValue2);
			}
		}
	}
	
//	protected void mergeOther(
//			LoadProfile<C> other, 
//			long offset,
//			LoadProfile<C> merged) {
//
//		merged.setEndingTimeOfProfile(Math.max(this.endingTimeOfProfile, other.endingTimeOfProfile + offset));
//
//		for (C commodity : getEnumValues()) {
//			TreeMap<Long, Tick> loadProfile1 = this.getLoadProfile(commodity);
//			TreeMap<Long, Tick> loadProfile2 = other.getLoadProfile(commodity);
//			
//			TreeMap<Long, Tick> result;
//			TreeMap<Long, Tick> toMerge;
//			TreeMap<Long, Tick> cloned;
//			long newOffset;
//			long endingTime;
//			long otherEndingTime;
//			if (loadProfile1.size() > loadProfile2.size()) {
//				result = new TreeMap<Long, Tick>(loadProfile1);
//				toMerge = loadProfile2;
//				newOffset = offset;
//				endingTime = other.endingTimeOfProfile;
//				cloned = loadProfile1;
//				otherEndingTime = this.endingTimeOfProfile;
//			} else {
//				result = new TreeMap<Long, Tick>(loadProfile2);
//				result.forEach((k, v) -> k += offset);
//				toMerge = loadProfile1;
//				newOffset = 0;
//				endingTime = this.endingTimeOfProfile;
//				cloned = loadProfile2;
//				otherEndingTime = other.endingTimeOfProfile;
//			}
//			
//
//			Iterator<Entry<Long, Tick>> currentIterator = toMerge.entrySet()
//					.iterator();
//			Entry<Long, Tick> currentToMerge = getNext(currentIterator, endingTime);
//			Entry<Long, Tick> nextToMerge = getNext(currentIterator, endingTime);
//			
//			Iterator<Entry<Long, Tick>> otherIterator = cloned.subMap(currentToMerge.getKey(), false, 
//					otherEndingTime, true).entrySet()
//					.iterator();
//			Entry<Long, Tick> currentOther = cloned.floorEntry(currentToMerge.getKey());
//			Entry<Long, Tick> nextOther = getNext(otherIterator, otherEndingTime);
//
//			while (currentToMerge != null) {
//
//				if (entry1.getKey() < entry2.getKey() + offset) {
//					merged.setLoad(commodity, entry1.getKey(),
//							entry1.getValue().value + activeValue2);
//
//					activeValue1 = entry1.getValue().value;
//
//					entry1 = getNext(iSet1, this.endingTimeOfProfile);
//				} else if (entry1.getKey() > entry2.getKey() + offset) {
//					merged.setLoad(commodity, entry2.getKey() + offset,
//							activeValue1 + entry2.getValue().value);
//
//					activeValue2 = entry2.getValue().value;
//
//					entry2 = getNext(iSet2, other.endingTimeOfProfile);
//				} else /* (entry1.getKey() == entry2.getKey() + offset) */{
//					merged.setLoad(commodity, entry2.getKey() + offset,
//							entry1.getValue().value + entry2.getValue().value);
//
//					activeValue1 = entry1.getValue().value;
//					activeValue2 = entry2.getValue().value;
//
//					entry1 = getNext(iSet1, this.endingTimeOfProfile);
//					entry2 = getNext(iSet2, other.endingTimeOfProfile);
//				}
//			}
//
//			while (entry1 != null) { // 1st profile still has data points
//				if (entry1.getKey() < other.endingTimeOfProfile + offset) {
//					merged.setLoad(commodity, entry1.getKey(),
//							entry1.getValue().value + activeValue2);
//					activeValue1 = entry1.getValue().value;
//				} else { // 2nd profile has ended
//					if (activeValue2 != 0) {
//						merged.setLoad(commodity, other.endingTimeOfProfile + offset,
//								activeValue1);
//						activeValue2 = 0;
//					}
//					merged.setLoad(commodity, entry1.getKey(),
//							entry1.getValue().value + activeValue2);
//				}
//
//				entry1 = getNext(iSet1, this.endingTimeOfProfile);
//			}
//			while (entry2 != null) {
//				if (entry2.getKey() + offset < this.endingTimeOfProfile) {
//					merged.setLoad(commodity, entry2.getKey() + offset,
//							entry2.getValue().value + activeValue1);
//					activeValue2 = entry2.getValue().value;
//				} else {
//					if (activeValue1 != 0) {
//						merged.setLoad(commodity, endingTimeOfProfile, activeValue2);
//						activeValue1 = 0;
//					}
//					merged.setLoad(commodity, entry2.getKey() + offset,
//							entry2.getValue().value + activeValue1);
//				}
//
//				entry2 = getNext(iSet2, other.endingTimeOfProfile);
//			}
//
//			// handling the end of profiles
//			if (activeValue1 != 0 && activeValue2 != 0) {
//				if (this.endingTimeOfProfile > other.endingTimeOfProfile + offset) {
//					merged.setLoad(commodity, other.endingTimeOfProfile + offset,
//							activeValue1);
//				} else if (this.endingTimeOfProfile < other.endingTimeOfProfile + offset) {
//					merged.setLoad(commodity, this.endingTimeOfProfile, activeValue2);
//				} else { /* == */
//					assert (this.endingTimeOfProfile == merged.endingTimeOfProfile);
//				}
//			} else if (activeValue2 != 0) {
//				merged.setLoad(commodity, other.endingTimeOfProfile + offset, activeValue1);
//			} else if (activeValue1 != 0) {
//				merged.setLoad(commodity, endingTimeOfProfile, activeValue2);
//			}
//		}
//	}

	/**
	 * EndingTimeOfProfile is defined as the point in time where
	 * the profile stops having a value other than 0, NOT the length of the profile.
	 * See {@link ILoadProfile#getEndingTimeOfProfile()}
	 */
	@Override
	public long getEndingTimeOfProfile() {
		return endingTimeOfProfile;
	}

	@Override
	public int getLoadAt(C commodity, long t) {
		TreeMap<Long, Tick> loadProfile = getLoadProfile(commodity);
		
		if (t >= endingTimeOfProfile) {
//			throw new IndexOutOfBoundsException();
			return 0;
		}

		Entry<Long, Tick> entry = loadProfile.floorEntry(t);
		return (entry == null) ? 0 : entry.getValue().value;
	}
	
//	public int getAverageLoadFromTill(C commodity, long start, long end) {
//		TreeMap<Long, Tick> loadProfile = getLoadProfile(commodity);
//		
//		if (start >= endingTimeOfProfile) {
//			return 0;
//		}
//		double avg = 0.0;
//		long currentTime = start;
//		
//		//checking if profile has values
//		Entry<Long, Tick> entry = loadProfile.floorEntry(start);
//		if (entry == null) 
//			return 0;
//		
//		while (currentTime < end && currentTime < endingTimeOfProfile) {
//			int powerVal = loadProfile.floorEntry(currentTime).getValue().value;
//			Long nextChange = loadProfile.higherKey(currentTime);
//			if (nextChange == null)
//				nextChange = end;
//			else if (nextChange > end)
//				nextChange = end;
//			if (nextChange > endingTimeOfProfile)
//				nextChange = endingTimeOfProfile;
//			
//			avg += (double) powerVal * ((double) (nextChange - currentTime) / (double) (end - start));
//			currentTime = nextChange;
//		}
//		
////		if (avg == 0)
////			System.out.println("No GOOD");
//		
//		return (int) Math.round(avg);
//	}
	
	public int getAverageLoadFromTill(C commodity, long start, long end) {
		TreeMap<Long, Tick> loadProfile = getLoadProfile(commodity);
		
		if (start >= endingTimeOfProfile) {
			return 0;
		}
		double avg = 0.0;
		long currentTime = start;
		long maxTime = Math.min(end, endingTimeOfProfile);
		
		//checking if profile has values
		Entry<Long, Tick> currentEntry = loadProfile.floorEntry(start);
		if (currentEntry == null) 
			return 0;
		
		Long higherKey = loadProfile.higherKey(currentTime);
		
		//no other values for the requested time period
		if (higherKey == null || higherKey >= maxTime) {
			avg = ((double) currentEntry.getValue().value) * ((double) (maxTime - currentTime) / (double) (end - start));
			return (int) Math.round(avg);
//			return currentEntry.getValue().value;
		}
		
		Iterator<Entry<Long, Tick>> entryIterator = loadProfile.subMap(start, false, maxTime, false).entrySet().iterator();
		Entry<Long, Tick> nextEntry = entryIterator.next();
		
		while (nextEntry != null) {
			long nextChange = nextEntry.getKey();
			
			avg += ((double) currentEntry.getValue().value) * ((double) (nextChange - currentTime) / (double) (end - start));
			currentTime = nextChange;
			currentEntry = nextEntry;
			
			if (entryIterator.hasNext()) {
				nextEntry = entryIterator.next();
			} else {
				nextEntry = null;
			}
		}
		
		if (currentTime < maxTime) {
			avg += ((double) currentEntry.getValue().value) * ((double) (maxTime - currentTime) / (double) (end - start));
		}
		
		return (int) Math.round(avg);
	}
	
	private EnumMap<C, Entry<Long, Tick>> currentEntry;
	private EnumMap<C, Entry<Long, Tick>> nextEntry; 
	private EnumMap<C, Iterator<Entry<Long, Tick>>> iterators; 
	
	public void removeSequentialPriming() {
		currentEntry = null;
		nextEntry = null;
		iterators = null;
	}
	
	public void initSequentialAverageLoad(long from, long till) {
		
		currentEntry = new EnumMap<C, Entry<Long, Tick>>(enumType);
		nextEntry = new EnumMap<C, Entry<Long, Tick>>(enumType);
		iterators = new EnumMap<C, Iterator<Entry<Long, Tick>>>(enumType);
		
		for (C c : getEnumValues()) {
			
			TreeMap<Long, Tick> loadProfile = getLoadProfile(c);
			
			if (from < endingTimeOfProfile) {
				currentEntry.put(c, loadProfile.floorEntry(from));
				
				Iterator<Entry<Long, Tick>> it = loadProfile.subMap(from, false, till, true).entrySet().iterator();
				iterators.put(c, it);
				
				nextEntry.put(c, (it.hasNext() ? it.next() : null));
			} else {
				currentEntry.put(c, null);
				iterators.put(c, null);
				nextEntry.put(c, null);
			}
		}
	}
	
	public void initSequentialAverageLoad(long from) {
		
		initSequentialAverageLoad(from, Long.MAX_VALUE);
	}
	
	public int getAverageLoadFromTillSequential(C commodity, long start, long end) {
		
		if (start >= endingTimeOfProfile) {
			return 0;
		}
		Entry<Long, Tick> current = currentEntry.get(commodity);
		
		//checking if profile has values
		if (current == null) 
			return 0;
		
		double avg = 0.0;
		long currentTime = start;
		double span = end - start;
		long maxTime = Math.min(end, endingTimeOfProfile);
		
//		
		Entry<Long, Tick> next = nextEntry.get(commodity);

		//no other values for the requested time period
		if (next == null || next.getKey() >= maxTime) {
//			avg = ((double) current.getValue().value) * (((double) (maxTime - currentTime)) / span);
			return current.getValue().value;
		}
		
		Iterator<Entry<Long, Tick>> entryIterator = iterators.get(commodity);
		
		while (next != null && next.getKey() < maxTime) {
			long nextChange = next.getKey();
			
			avg += current.getValue().value * (nextChange - currentTime);
			currentTime = nextChange;
			current = next;
			
			if (entryIterator.hasNext()) {
				next = entryIterator.next();
			} else {
				next = null;
			}
		}
		
		if (currentTime < maxTime) {
			avg += current.getValue().value * (maxTime - currentTime);
		}
		
		currentEntry.put(commodity, current);
		nextEntry.put(commodity, next);
//		iterators.put(commodity, entryIterator);
		
		return (int) Math.round(avg / span);
	}
	
	public double getAverageLoadFromTillSequentialNotRounded(C commodity, long start, long end) {
		
		if (start >= endingTimeOfProfile) {
			return 0;
		}
		Entry<Long, Tick> current = currentEntry.get(commodity);
		
		//checking if profile has values
		if (current == null) 
			return 0;
		
		double avg = 0.0;
		long currentTime = start;
		double span = end - start;
		long maxTime = Math.min(end, endingTimeOfProfile);
		
//		
		Entry<Long, Tick> next = nextEntry.get(commodity);

		//no other values for the requested time period
		if (next == null || next.getKey() >= maxTime) {
//			avg = ((double) current.getValue().value) * (((double) (maxTime - currentTime)) / span);
//			return avg;
			return current.getValue().value;
		}
		
		Iterator<Entry<Long, Tick>> entryIterator = iterators.get(commodity);
		
		while (next != null && next.getKey() < maxTime) {
			long nextChange = next.getKey();
			
			avg +=current.getValue().value * (nextChange - currentTime);
			currentTime = nextChange;
			current = next;
			
			if (entryIterator.hasNext()) {
				next = entryIterator.next();
			} else {
				next = null;
			}
		}
		
		if (currentTime < maxTime) {
			avg += current.getValue().value * (maxTime - currentTime);
		}
		
		currentEntry.put(commodity, current);
		nextEntry.put(commodity, next);
//		iterators.put(commodity, entryIterator);
		
		return (avg / span);
	}
	
	@Override
	public Long getNextLoadChange(C commodity, long t) {
		return getLoadProfile(commodity).higherKey(t);
	}
	
	/**
	 * cuts off all ticks with a negative time and inserts a tick at time 0
	 * if necessary. This function changes the current object.
	 * @author tisu
	 */
	@Override
	public void cutOffNegativeTimeValues() {
		boolean modified = false;
		
		for (TreeMap<Long, Tick> ticks : commodities.values()) {
			Entry<Long, Tick> lastProcessed = null;
			while (ticks.size() > 0 && ticks.firstKey() < 0) {
				lastProcessed = ticks.firstEntry();
				ticks.remove(lastProcessed.getKey());
				modified = true;
			}
			if (lastProcessed != null && ticks.size() > 0 && ticks.firstKey() > 0) {
				ticks.put(0L, lastProcessed.getValue());
			}
		}
		
		if (modified) recalculateIntegrals();
	}
	
	
	private void recalculateIntegrals() {
		for (Entry<C, TreeMap<Long, Tick> > ticks : commodities.entrySet()) {
			//we could really recalculate all integrals in here, but I prefer
			//to do all the calculations in one place to minimize errors
			//this method is a bit slower, but won't take too much more
			//processing time.
			
			TreeMap<Long, Tick> newticks = new TreeMap<>();
			for (Entry<Long, Tick> t : ticks.getValue().entrySet()) {
				newticks.put(t.getKey(), t.getValue());
			}
			commodities.put(ticks.getKey(), newticks);
		}
	}
	
	public void multiplyLoadsWithFactor(double factor) {
		for( Entry<C, TreeMap<Long, Tick>> es : commodities.entrySet() ) {
			TreeMap<Long, Tick> originalLoadProfile = es.getValue();
			
			for (Entry<Long, Tick> entry : originalLoadProfile.entrySet()) {
				Tick newTick = new Tick();
				newTick.value =  (int) Math.round(entry.getValue().value * factor);
				originalLoadProfile.put(entry.getKey(), newTick);
			}
		}
	}

	
	// ###########
	// COMPRESSION
	// ###########
	
	public abstract LoadProfile<C> getProfileWithoutDuplicateValues();
	
	// general
	protected void getProfileWithoutDuplicateValues(LoadProfile<C> compress) {
		for (C c : getEnumValues()) {
			TreeMap<Long, Tick> map = commodities.get(c);
			TreeMap<Long, Tick> otherMap = compress.commodities.get(c);
			Iterator<Map.Entry<Long, Tick>> it = map.entrySet().iterator();
			
			if (!it.hasNext())
				continue;
			
			Entry<Long, Tick> lastValue = it.next();
			otherMap.put(lastValue.getKey(), lastValue.getValue());
			
			while (it.hasNext()) {
				Entry<Long,Tick> e = it.next();
				
				if (!lastValue.getValue().equals(e.getValue())) {
					lastValue = e;
					otherMap.put(lastValue.getKey(), lastValue.getValue());
				}				
			}
		}
		compress.setEndingTimeOfProfile(this.getEndingTimeOfProfile());
	}
	
	public abstract LoadProfile<C> getCompressedProfile(LoadProfileCompressionTypes ct, final int powerEps, final int time);
	
	// general
	protected void getCompressedProfile(
			LoadProfileCompressionTypes ct, final int powerEps, final int time, LoadProfile<C> compress) {
		if (ct == null || time <= 0 || powerEps <= 0) {
			compress = this.clone();
			System.out.println("[ERROR][AbstractLoadProfile]: Compressiontype or -value is invalid (null or <= 0) returning clone of original profile");
		} else if (ct == LoadProfileCompressionTypes.DISCONTINUITIES) {
			getCompressedProfileByDiscontinuities(powerEps, compress);
		}
		else if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
			getCompressedProfileByTimeSlot(time, compress);
		}
	}
	
	public abstract LoadProfile<C> getCompressedProfileByDiscontinuities(final double powerEps);
	
	// ByDiscontinuities

	protected void getCompressedProfileByDiscontinuities(
			final double powerEps, LoadProfile<C> compressed) {

		for (C c : getEnumValues()) {
			TreeMap<Long, Tick> map = commodities.get(c);
			
			double lastValueSaved = Double.MAX_VALUE;
			long lastValueSavedKey = Long.MIN_VALUE;
			
			double momentaryAvg = Double.MAX_VALUE;
			double momentaryAvgMax = Double.MIN_VALUE;
			double momentaryAvgMin = Double.MAX_VALUE;
			
			Tick lastLookedAtTick = null;
			long lastLookedAtKey = Long.MIN_VALUE;
			
			long counter = 0;
			
			for (Iterator<Map.Entry<Long, Tick>> it = map.entrySet().iterator(); it.hasNext();) {
				Entry<Long,Tick> e = it.next();
				// if last -> set value
				if (it.hasNext() == false) {
					compressed.setLoad(c, e.getKey(), e.getValue().value);
					
					if (lastLookedAtTick != null) {
						// write previous average value...
						compressed.setLoad(c, lastValueSavedKey, (int) Math.round(momentaryAvg));
					}
				}
				// first value
				else if (lastLookedAtTick == null) {
					compressed.setLoad(c, e.getKey(), e.getValue().value);

					lastValueSavedKey = e.getKey();
					lastValueSaved = e.getValue().value;
					
					lastLookedAtTick = e.getValue();					
					lastLookedAtKey = e.getKey();
					
					momentaryAvg = lastValueSaved;
					momentaryAvgMax = lastValueSaved;
					momentaryAvgMin = lastValueSaved;
					
					counter = 0;
				}
				//if difference of avg to min/max/lastValue/nowValue > powerEps --> save
				else if (Math.abs(momentaryAvg - momentaryAvgMax) > powerEps
						|| Math.abs(momentaryAvg - momentaryAvgMin) > powerEps
						|| Math.abs(momentaryAvg - lastValueSaved) > powerEps
						|| Math.abs(momentaryAvg - e.getValue().value) > powerEps) {
					
					long diffToLastKey = e.getKey() - lastLookedAtKey;
					momentaryAvg = (lastLookedAtTick.value * diffToLastKey + momentaryAvg * counter) / (diffToLastKey + counter);
					
					compressed.setLoad(c, lastValueSavedKey, (int) Math.round(momentaryAvg));
					
					lastValueSavedKey = e.getKey();
					lastValueSaved = e.getValue().value;
					
					lastLookedAtTick = e.getValue();					
					lastLookedAtKey = e.getKey();
					
					momentaryAvg = lastValueSaved;
					momentaryAvgMax = lastValueSaved;
					momentaryAvgMin = lastValueSaved;
					
					counter = 0;
				} 
				//diff to small, update avg/min/max etc.
				else {
					long diffToLastKey = e.getKey() - lastLookedAtKey;
					momentaryAvg = (lastLookedAtTick.value * diffToLastKey + momentaryAvg * counter) / (diffToLastKey + counter);

					lastLookedAtKey = e.getKey();
					lastLookedAtTick = e.getValue();
					
					if (e.getValue().value > momentaryAvgMax) {
						momentaryAvgMax = e.getValue().value;
					} else if (e.getValue().value < momentaryAvgMin) {
						momentaryAvgMin = e.getValue().value;
					}
					
					counter = counter + diffToLastKey;
				}
			}			
			
			
			
//			double lastAvgToSave = Double.MAX_VALUE;
//			long lastKeyToSave = Long.MIN_VALUE;
//			Tick lastValueTickToSave = null;
//			int lastValueTickValueToSave = 0;
//			
//			long lastKey = Long.MIN_VALUE;
//			int lastTickValue = 0;
//			
//			long counter = 0;
//			
//			for (Iterator<Map.Entry<Long, Tick>> it = map.entrySet().iterator(); it.hasNext();) {
//				Entry<Long,Tick> e = it.next();
//				// if last -> set value
//				if (it.hasNext() == false) {
//					compressed.setLoad(c, e.getKey(), e.getValue().value);
//					
//					if (lastValueTickToSave != null) {
//						// write previous average value...
//						compressed.setLoad(c, lastKeyToSave, (int) Math.round(lastAvgToSave));
//					}
//				}
//				// first value
//				else if (lastValueTickToSave == null) {
//					compressed.setLoad(c, e.getKey(), e.getValue().value);
//
//					lastKeyToSave = e.getKey();
//					lastValueTickToSave = e.getValue();
//					lastValueTickValueToSave = e.getValue().value;
//					lastAvgToSave = lastValueTickValueToSave;
//					
//					lastKey = lastKeyToSave;
//					lastTickValue = lastValueTickValueToSave;
//					counter = 0;
//				}
//				// difference to previous value is too small -> update avg
////				else if (Math.abs(e.getValue().value - lastValueTickValueToSave) < powerEps) {
////					long diffToLastKey = e.getKey() - lastKeyToSave;
////					int currentValue = e.getValue().value;
////					lastAvg = (lastAvg * diffToLastKey + currentValue) / (diffToLastKey + 1);
//					
//				else if (Math.abs(e.getValue().value - lastAvgToSave) < powerEps) {
//					long diffToLastKey = e.getKey() - lastKey;
//					lastAvgToSave = (lastTickValue * diffToLastKey + lastAvgToSave * counter) / (diffToLastKey + counter);
//
//					lastKey = e.getKey();
//					lastTickValue = e.getValue().value;
//					
//					counter = counter + diffToLastKey;
//				}
//				// difference to previous is big enough -> set avg and add new datapoint
//				else {
//					long diffToLastKey = e.getKey() - lastKey;
//					lastAvgToSave = (lastTickValue * diffToLastKey + lastAvgToSave * counter) / (diffToLastKey + counter);
//					
//					compressed.setLoad(c, lastKeyToSave, (int) Math.round(lastAvgToSave));
//					
//					lastKeyToSave = e.getKey();
//					lastValueTickToSave = e.getValue();
//					lastValueTickValueToSave = e.getValue().value;
//					lastAvgToSave = lastValueTickValueToSave;
//					
//					lastKey = e.getKey();
//					lastTickValue = e.getValue().value;
//					counter = 0;
//				}
//			}
		}
		
		compressed.setEndingTimeOfProfile(this.getEndingTimeOfProfile());	
	}
	
	public abstract LoadProfile<C> getCompressedProfileByTimeSlot(final int time);
	
	// by TimeSlot
	protected void getCompressedProfileByTimeSlot(final int time, LoadProfile<C> compressed) {

		for (C c : getEnumValues()) {
			TreeMap<Long, Tick> map = commodities.get(c);
			
			Tick lastTick = null;
			double lastAvg = Double.MAX_VALUE;
			long lastKey = Long.MIN_VALUE;
			
			for (Iterator<Map.Entry<Long, Tick>> it = map.entrySet().iterator(); it.hasNext();) {
				Entry<Long,Tick> e = it.next();
				// if last -> set value
				if (it.hasNext() == false) {
					compressed.setLoad(c, e.getKey(), e.getValue().value);
					
					if (lastTick != null) {
						// write last value...
						compressed.setLoad(c, lastKey, (int) Math.round(lastAvg));
					}
				}
				// first value
				else if (lastTick == null) {
					compressed.setLoad(c, e.getKey(), e.getValue().value);

					lastKey = e.getKey();
					lastTick = e.getValue();
					lastAvg = e.getValue().value;
				}
				// difference to previous value is too small -> update avg
				else if (Math.abs(e.getKey() - lastKey) < time) {
					long diffToLastKey = e.getKey() - lastKey;
					int currentValue = e.getValue().value;
					lastAvg = (lastAvg * diffToLastKey + currentValue) / (diffToLastKey + 1);
				}
				// difference to previous is big enough -> set avg and add new datapoint
				else {
					compressed.setLoad(c, lastKey, (int) Math.round(lastAvg));
					
					lastKey = e.getKey();
					lastTick = e.getValue();
					lastAvg = e.getValue().value;
				}
			}
		}
		compressed.setEndingTimeOfProfile(this.getEndingTimeOfProfile());
		//if slot-value too low remove duplicate entries to speed up optimization
		if (time < 300) {
			compressed = compressed.getProfileWithoutDuplicateValues();
		}	
	}	
	
	
	/***********************************************************
	 * utility functions (cloning, toString, ...)
	 **********************************************************/	
	
	public abstract LoadProfile<C> clone();
	
	protected void clone(LoadProfile<C> newLoadProfile) {

		for( Entry<C, TreeMap<Long, Tick>> es : commodities.entrySet() ) {
			TreeMap<Long, Tick> originalLoadProfile = es.getValue();
			TreeMap<Long, Tick> newProfile = new TreeMap<>();
			
			for (Entry<Long, Tick> entry : originalLoadProfile.entrySet()) {
				newProfile.put(entry.getKey(), entry.getValue().clone());
			}
			
			newLoadProfile.commodities.put(es.getKey(), newProfile);
		}

		newLoadProfile.endingTimeOfProfile = this.endingTimeOfProfile;
	}
	
	/**
	 * clones this load profile after the given time and returns the result
	 * @param timestamp
	 * @return a clone of this profile after the timestamp
	 */
	public abstract LoadProfile<C> cloneAfter(long timestamp);	
	
	protected void cloneAfter(long timestamp, LoadProfile<C> newLoadProfile) {

		for( Entry<C, TreeMap<Long, Tick>> es : commodities.entrySet() ) {
			TreeMap<Long, Tick> originalLoadProfile = es.getValue();
			TreeMap<Long, Tick> newProfile = new TreeMap<>();
			
			Entry<Long, Tick> startCorrection = originalLoadProfile.floorEntry(timestamp);
			
			if (startCorrection != null)
				newProfile.put(timestamp, startCorrection.getValue());
			for (Entry<Long, Tick> entry : originalLoadProfile.tailMap(timestamp).entrySet()) {
				newProfile.put(entry.getKey(), entry.getValue().clone());
			}
			
			newLoadProfile.commodities.put(es.getKey(), newProfile);
		}

		newLoadProfile.endingTimeOfProfile = this.endingTimeOfProfile;
	}
	
	/**
	 * clones this load profile before the given time and returns the result
	 * @param timestamp
	 * @return a clone of this profile before the timestamp
	 */
	public abstract LoadProfile<C> cloneBefore(long timestamp);
	
	protected void cloneBefore(long timestamp, LoadProfile<C> newLoadProfile) {
		
		long maxEntry = 0;

		for( Entry<C, TreeMap<Long, Tick>> es : commodities.entrySet() ) {
			TreeMap<Long, Tick> originalLoadProfile = es.getValue();
			TreeMap<Long, Tick> newProfile = new TreeMap<>();
			
			
			for (Entry<Long, Tick> entry : originalLoadProfile.headMap(timestamp).entrySet()) {
				newProfile.put(entry.getKey(), entry.getValue().clone());
			}
			
			Long highestKey = originalLoadProfile.lowerKey(timestamp);
			
			if (highestKey != null && highestKey > maxEntry) {
				maxEntry = highestKey;
			}
			
			newLoadProfile.commodities.put(es.getKey(), newProfile);
		}

		newLoadProfile.endingTimeOfProfile = maxEntry + 1;
	}
	
	public abstract LoadProfile<C> cloneWithOffset(long offset);
	
	protected void cloneWithOffset(long offset, LoadProfile<C> newLoadProfile) {	

		for( Entry<C, TreeMap<Long, Tick>> es : commodities.entrySet() ) {
			TreeMap<Long, Tick> originalLoadProfile = es.getValue();
			TreeMap<Long, Tick> newProfile = new TreeMap<>();
			
			for (Entry<Long, Tick> entry : originalLoadProfile.entrySet()) {
				newProfile.put(entry.getKey() + offset, entry.getValue().clone());
			}
			
			newLoadProfile.commodities.put(es.getKey(), newProfile);
		}

		newLoadProfile.endingTimeOfProfile = this.endingTimeOfProfile + offset;
	}
	
	public abstract LoadProfile<C> cloneOnlyDuration();	
	
	// toString
	
	@Override
	public String toString() {
		String returnValue = "";
		
		for ( Entry<C, TreeMap<Long, Tick>> es : commodities.entrySet() ) {
			returnValue += "Profile for " + enumType.getSimpleName() + " " + es.getKey() + ": " + es.getValue().toString();
		}
		
		return returnValue;
	}

	@Override
	public String toStringShort() {
		String returnValue = "[ ";
		
		for ( Entry<C, TreeMap<Long, Tick>> es : commodities.entrySet() ) {
			TreeMap<Long, Tick> map = es.getValue();
			if( map != null && map.lastEntry() != null ) {
				returnValue += es.getKey() + ", ";
			}
		}
		
		return returnValue + "]";
	}
	
	protected C[] getEnumValues() {
		return this.enumType.getEnumConstants();
	}
}
