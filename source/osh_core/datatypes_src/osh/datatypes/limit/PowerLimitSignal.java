package osh.datatypes.limit;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.power.PowerInterval;

/**
 * Representation of the Complex Power Limitation Signal for Shot Term Optimization
 *  
 * @author Ingo Mauser
 *
 */
public class PowerLimitSignal {
	
	private boolean isCompressed = true;
	
	private long limitUnknownBefore = 0;
	private long limitUnknownAtAndAfter = 0;

	//private TreeMap<Long, ComplexPower> powerLimits;
	private TreeMap<Long, PowerInterval> powerLimits;
	private TreeMap<Long, Double> remsPowerLimits;

	// means the limit of a 64A three phase supply in complex power with cosPhi = 0.95 (inductive)
	//private final ComplexPower UNKNOWN_LIMIT = new ComplexPower(430000, 2, 0.95, true); 
	private final PowerInterval UNKNOWN_LIMIT = new PowerInterval();
	
	
	/**
	 * CONSTRUCTOR
	 */
	public PowerLimitSignal() {
		powerLimits = new TreeMap<Long, PowerInterval>();
		this.remsPowerLimits = new TreeMap<Long, Double>();
	}
	
	public long getLimitUnknownAtAndAfter() {
		return limitUnknownAtAndAfter;
	}

	public void setLimitUnknownAtAndAfter(long limitUnknownAtAndAfter) {
		this.limitUnknownAtAndAfter = limitUnknownAtAndAfter;
	}
	
	public void setPowerLimits(TreeMap<Long, PowerInterval> powerLimits) {
		this.powerLimits = powerLimits;
	}
	
	public TreeMap<Long, PowerInterval> getPowerLimits() {
		return  powerLimits;
	}
	
	/**
	 * Sets the interval during which the power limit is known
	 * 
	 */
	public void setKnownPowerLimitInterval( long start, long end ) {
		this.limitUnknownBefore = start;
		this.limitUnknownAtAndAfter = end;
	}
	
	/**
	 * Removes redundant entries
	 */
	public void compress() {
		if( isCompressed )
			return;
		
		Iterator<Entry<Long, PowerInterval>> i = powerLimits.entrySet().iterator();
		PowerInterval last = null;
		
		while( i.hasNext() ) {
			Entry<Long, PowerInterval> e = i.next();
			if( e.getValue().equals(last)) {
				i.remove();
			} else {
				last = e.getValue();
			}
		}
		
		isCompressed = true;
	}
	
//	public boolean getIsCompressed() {
//		return isCompressed;
//	}
	

	public void setPowerLimit(long time, PowerInterval limit) {
		if (limit == null) throw new NullPointerException("limit is null");
		powerLimits.put(time, limit);
		isCompressed = false;
	}
	
	public void setPowerLimit(
			long time, 
			double activePowerUpperLimit,
			double activePowerLowerLimit) {
		PowerInterval limit = new PowerInterval(
				activePowerUpperLimit,
				activePowerLowerLimit);
		setPowerLimit(time, limit);
	} 
	
	public void setPowerLimit(long time, double powerUpperLimit) {
		PowerInterval limit = new PowerInterval(powerUpperLimit);
		setPowerLimit(time, limit);
	}
	
	
	public PowerInterval getPowerLimitClone(long time) {
		
		Entry<Long, PowerInterval> entry = powerLimits.floorEntry(time);
		
		if (entry != null) {
			return entry.getValue().clone();
		}
		else {
			return UNKNOWN_LIMIT.clone();
		}
	}
	
	public PowerInterval getPowerLimitInterval(Long time) {
		return powerLimits.floorEntry(time).getValue();
	}
	
	public double getPowerUpperLimit(long time) {
		
		Entry<Long, PowerInterval> entry = powerLimits.floorEntry(time);
		
		if (entry != null) {
			return entry.getValue().getPowerUpperLimit();
		}
		else {
			return UNKNOWN_LIMIT.getPowerUpperLimit();
		}
	}
	
	public double getPowerLowerLimit(long time) {
		
		Entry<Long, PowerInterval> entry = powerLimits.floorEntry(time);
		
		if (entry != null) {
			return entry.getValue().getPowerLowerLimit();
		}
		else {
			return UNKNOWN_LIMIT.getPowerLowerLimit();
		}
	}
	
	
	
	/**
	 * 
	 * @param ps old power limit signal
	 * @param base offset time for new power limit signal 
	 * @param resolution New resolution in seconds
	 * @return
	 */
	public PowerLimitSignal scalePowerLimitSignal(long base, long resolution) 
	{
		PowerLimitSignal newPS = new PowerLimitSignal();

		for( Entry<Long, PowerInterval> e : powerLimits.entrySet() ) {
			newPS.setPowerLimit((e.getKey() - base) / resolution, e.getValue());
		}
		
		newPS.setKnownPowerLimitInterval(
				(limitUnknownBefore - base) / resolution, 
				(limitUnknownAtAndAfter - base) / resolution);
		
		return newPS;
	}
	
	/**
	 * Returns the time the power limit changes after t
	 * 
	 * @param t time after power limit will change
	 * @return null if there is no next power limit change
	 */
	public Long getNextPowerLimitChange( long t ) {
		if( t >= limitUnknownAtAndAfter ) {
			return null;
		}
			
		compress();

		Long key = powerLimits.higherKey(t);
		
		if( key == null /* && t < priceUnknownAfter */ )
			return limitUnknownAtAndAfter;
		else
			return key;
	}
	
	public Iterator<Entry<Long, PowerInterval>> getIteratorForSubMap(long from, long to) {
		return powerLimits.subMap(from, false, to, false).entrySet().iterator();
	}
	
	public Entry<Long, PowerInterval> getFloorEntry(long t) {
		return powerLimits.floorEntry(t);
	}
	
	@Deprecated
	public Long getNextActivePowerLimitChange(long t) {
		if( t >= limitUnknownAtAndAfter ) {
			return null;
		}
			
		compress();

		Long nextChange = powerLimits.higherKey(t);
		Long prevChange = powerLimits.higherKey(t);
		
		boolean flag = true;
		Long returnValue = 0L;
		
		do {
			if( nextChange == null /* && t < priceUnknownAfter */ )
				returnValue = limitUnknownAtAndAfter;
			else
				returnValue = nextChange;
			
			if (nextChange == null || prevChange == null) {
				flag = false;
			}
			else if (powerLimits.get(prevChange).getPowerUpperLimit() 
					== powerLimits.get(nextChange).getPowerUpperLimit()) {
				flag = false;
			}
			else {
				flag = true;
			}
			
		} while (flag);
		
		return returnValue;
	}
	
	@Override
	public PowerLimitSignal clone() {
		PowerLimitSignal clone = new PowerLimitSignal();
		
		clone.isCompressed = this.isCompressed;
		clone.limitUnknownBefore = this.limitUnknownBefore;
		clone.limitUnknownAtAndAfter = this.limitUnknownAtAndAfter;
		
		//deep clone tree map
		for (Entry<Long, PowerInterval> e : powerLimits.entrySet()) {
			clone.powerLimits.put(e.getKey(), e.getValue().clone());
		}
		
		return clone;
	}
	
	/** returned value is the first time tick which has no limit.*/
	public long getPowerLimitUnknownAtAndAfter() {
		return limitUnknownAtAndAfter;
	}
	
	/** returned value is the first time tick which has a limit.*/
	public long getPowerLimitUnknownBefore() {
		return limitUnknownBefore;
	}
	
	public TreeMap<Long, PowerInterval> getLimits() {
		return powerLimits;
	}
	
	private <T> Entry<Long, T> getNext(
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

	public void extendAndOverride(PowerLimitSignal toExtend) {

		Iterator<Entry<Long, PowerInterval>> iSet1 = this.powerLimits.entrySet()
				.iterator();
		Iterator<Entry<Long, PowerInterval>> iSet2 = toExtend.powerLimits.entrySet()
				.iterator();

		Entry<Long, PowerInterval> entry1 = null;
		Entry<Long, PowerInterval> entry2 = null;
		TreeMap<Long, PowerInterval> newLimits = new TreeMap<Long, PowerInterval>();
		long oldUnknownAfter = this.limitUnknownAtAndAfter;
		long oldUnknownBefore = this.limitUnknownBefore;

		this.limitUnknownBefore = Math.min(this.limitUnknownBefore, toExtend.limitUnknownBefore);
		this.limitUnknownAtAndAfter = Math.max(this.limitUnknownAtAndAfter, toExtend.limitUnknownAtAndAfter);
		this.isCompressed = false;

		entry1 = getNext(iSet1, oldUnknownAfter);
		entry2 = getNext(iSet2, toExtend.limitUnknownAtAndAfter);

		while (entry1 != null && entry2 != null) {

			if (entry1.getKey() < toExtend.limitUnknownBefore) {
				newLimits.put(entry1.getKey(), entry1.getValue());
				entry1 = getNext(iSet1, oldUnknownAfter);
			} else {
				newLimits.put(entry2.getKey(), entry2.getValue());
				entry2 = getNext(iSet2, toExtend.limitUnknownAtAndAfter);
			}
		}
		
		while (entry1 != null) { // 1st profile still has data points
			if (entry1.getKey() > toExtend.limitUnknownAtAndAfter) {
				newLimits.put(entry1.getKey(), entry1.getValue());
			}
			entry1 = getNext(iSet1, oldUnknownAfter);
		}
		
		while (entry2 != null) { // 2nd profile still has data points
			if (entry2.getKey() > toExtend.limitUnknownAtAndAfter) {
				newLimits.put(entry2.getKey(), entry2.getValue());
			}
			entry2 = getNext(iSet2, toExtend.limitUnknownAtAndAfter);
		}

		//price signals dont overlap (|----2----|     |----1----|), so we have an uncertain period
		if (toExtend.limitUnknownAtAndAfter < oldUnknownBefore) {
			newLimits.put(toExtend.limitUnknownAtAndAfter, UNKNOWN_LIMIT);
		}	

		//price signals dont overlap (|----1----|     |----2----|), so we have an uncertain period
		if (oldUnknownAfter < toExtend.limitUnknownBefore ) {
			newLimits.put(oldUnknownAfter, UNKNOWN_LIMIT);
		}

		this.powerLimits = newLimits;

		this.compress();
	}
	
	/**
	 * clones this power limit signal after the given time and returns the result
	 * @param timestamp
	 * @return a clone of this power limit signal after the timestamp
	 */
	public PowerLimitSignal cloneAfter(long timestamp) {		
		
		PowerLimitSignal newLimitSignal = new PowerLimitSignal();

		PowerInterval startCorrection = getPowerLimitInterval(timestamp);
		
		if (startCorrection != null)		
			newLimitSignal.powerLimits.put(timestamp, getPowerLimitInterval(timestamp).clone());
		
		for( Entry<Long, PowerInterval> en : powerLimits.tailMap(timestamp).entrySet() ) {
			newLimitSignal.powerLimits.put(en.getKey(), en.getValue().clone());
		}
		newLimitSignal.limitUnknownAtAndAfter = this.limitUnknownAtAndAfter;
		newLimitSignal.limitUnknownBefore = timestamp;

		return newLimitSignal;
	}
	
	/**
	 * clones this power limit signal before the given time and returns the result
	 * @param timestamp
	 * @return a clone of this power limit signal before the timestamp
	 */
	public PowerLimitSignal cloneBefore(long timestamp) {
		
		PowerLimitSignal newLimitSignal = new PowerLimitSignal();
		
		for( Entry<Long, PowerInterval> en : powerLimits.headMap(timestamp).entrySet() ) {
			newLimitSignal.powerLimits.put(en.getKey(), en.getValue().clone());
		}

		newLimitSignal.limitUnknownAtAndAfter = this.limitUnknownAtAndAfter >= timestamp ? timestamp : this.limitUnknownAtAndAfter;
		newLimitSignal.limitUnknownBefore = this.limitUnknownBefore;

		return newLimitSignal;
	}

	public AncillaryCommodity getAc() {
		return null;
	}
	
}
