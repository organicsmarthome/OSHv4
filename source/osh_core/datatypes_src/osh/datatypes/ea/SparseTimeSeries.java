package osh.datatypes.ea;

import java.util.Iterator;
import java.util.Map.Entry;

import osh.datatypes.ea.interfaces.ITimeSeries;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Sparse time series - use it for price signal / max load signal
 * 
 */
public class SparseTimeSeries implements ITimeSeries {
	private class Tick {
		public double value;
		public double positiveInt;
		public double negativeInt;
		
		@Override
		public String toString() {
			return value + "W  S+" + positiveInt + "Ws  S-" + negativeInt + "Ws";
		}
	}
	
	protected TreeMap<Long, Tick> profile;
	protected long duration;
	protected Long lastEntry = null;
	
	public SparseTimeSeries() {
		profile = new TreeMap<Long, Tick>();
		duration = 0;
	}
	
	@Override
	public double get(long time) {
		if( time >= duration )
			throw new IndexOutOfBoundsException();
		Entry<Long,Tick> entry = profile.floorEntry(time);
		return (entry==null)?0.0:entry.getValue().value;
	}

	@Override
	public long getNextChange(long time) {
		Long next = profile.higherKey(time);
		
		if( next == null )
			return -1;
		else
			return next;
	}

	@Override
	public void set(long t, double value) {
		Tick v = new Tick();
		v.value = value;
		
		Entry<Long, Tick> floor = profile.floorEntry(t);
		
		// sum up from -infty to t
		if( floor != null ) {
			Tick f = floor.getValue();
			v.positiveInt = f.positiveInt;
			v.negativeInt = f.negativeInt;
			if( f.value > 0 ) {
				v.positiveInt += (t - floor.getKey()) * (f.value);
			} else {
				v.negativeInt += (t - floor.getKey()) * (f.value);
			}
		} else {
			v.negativeInt = 0.0;
			v.positiveInt = 0.0;
		}
		profile.put(t, v);

		// update from t to +infty
		SortedMap<Long, Tick> ceil;
		if( lastEntry == null || lastEntry < t ) {
			ceil = null;
		} else {
			ceil = profile.tailMap(t, false);
		}
		lastEntry = t;
		
		if( ceil != null && !ceil.isEmpty() ) {
			long firstKey = ceil.firstKey();
			assert( firstKey > t );
			double partialsum = (firstKey - t) * v.value;
			for( Entry<Long, Tick> entry : ceil.entrySet() ) {
				Tick tr = entry.getValue();
				if( v.value > 0 )
					tr.positiveInt += partialsum;
				else
					tr.negativeInt += partialsum;
			}
		}
	}

	private Entry<Long, Tick> getNext( Iterator<Entry<Long, Tick>> it, long duration ) {
		if(it.hasNext()) {
			Entry<Long, Tick> e = it.next(); 
			if( e.getKey() < duration )
				return e;
			else
				return null;
		} else
			return null;
	}
	
	private SparseTimeSeries merge( SparseTimeSeries other, long offset ) {
		SparseTimeSeries merged = new SparseTimeSeries();

		merged.setLength(Math.max(this.duration, other.duration + offset));
		
		Iterator<Entry<Long, Tick>> iSet1 = this.profile.entrySet().iterator();
		Iterator<Entry<Long, Tick>> iSet2 = other.profile.entrySet().iterator();
		
		Entry<Long, Tick> entry1 = null;
		Entry<Long, Tick> entry2 = null;
		
		double value1 = 0;
		double value2 = 0;
		
		entry1 = getNext( iSet1, this.duration );
		entry2 = getNext( iSet2, other.duration );
		
		while (entry1 != null && entry2 != null)
		{
			if (entry1.getKey() < entry2.getKey() + offset)
			{
				merged.set(entry1.getKey(), entry1.getValue().value + value2);
				
				value1 = entry1.getValue().value;
				
				entry1 = getNext( iSet1, this.duration );
			}
			else if (entry1.getKey() > entry2.getKey() + offset)
			{
				merged.set(entry2.getKey() + offset, value1 + entry2.getValue().value);
				
				value2 = entry2.getValue().value;
				
				entry2 = getNext( iSet2, other.duration );
			}
			else /* (entry1.getKey() == entry2.getKey() + offset) */
			{
				merged.set(entry2.getKey() + offset, entry1.getValue().value + entry2.getValue().value);
				
				value1 = entry1.getValue().value;
				value2 = entry2.getValue().value;

				entry1 = getNext( iSet1, this.duration );
				entry2 = getNext( iSet2, other.duration );
			}
		}
		
		while( entry1 != null ) { // 1st profile still has data points
			if( entry1.getKey() < other.duration + offset ) {
				merged.set(entry1.getKey(), entry1.getValue().value + value2);
				value1 = entry1.getValue().value;
			} else { // 2nd profile has ended
				if( value2 != 0.0 ) {
					merged.set(other.duration + offset, value1);
					value2 = 0.0;
				}
				merged.set(entry1.getKey(), entry1.getValue().value + value2);
			}
			
			entry1 = getNext( iSet1, this.duration );
		}
		while( entry2 != null ) {
			if( entry2.getKey() + offset < this.duration ) {
				merged.set(entry2.getKey() + offset, entry2.getValue().value + value1);
				value2 = entry2.getValue().value;
			} else {
				if( value1 != 0.0 ) {
					merged.set(duration, value2);
					value1 = 0.0;
				}
				merged.set(entry2.getKey() + offset, entry2.getValue().value + value1);
			}
			
			entry2 = getNext( iSet2, other.duration );
		}
		
		// handling the end of profiles
		if( value1 != 0.0 && value2 != 0.0 ) {
			if( this.duration > other.duration + offset ) {
				merged.set(other.duration+offset, value1);
			} else
			if( this.duration < other.duration + offset ) {
				merged.set(this.duration, value2);
			} else { /* == */
				assert( this.duration == merged.duration );
			}
		}
		else
		if( value2 != 0.0 ) {
			merged.set(other.duration + offset, value1);
		} else
		if( value1 != 0.0 ) {
			merged.set(duration, value2);
		}

		return merged;
	}
	
	@Override
	public long length() {
		return duration;
	}

	@Override
	public void setLength(long newLength) {
		this.duration = newLength;
	}

	@Override
	public void add(ITimeSeries operand, long offset) {
		if( operand instanceof SparseTimeSeries ) {
			SparseTimeSeries res = merge((SparseTimeSeries) operand, offset);
			this.duration = res.duration;
			this.lastEntry = res.lastEntry;
			this.profile = res.profile;
		}
		else
			throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void multiply(ITimeSeries operand, long offset) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Calculates discrete integral from time 0 to t
	 * 
	 * @param t
	 * @return
	 */
	private double integral(long t) {
		if( t > duration )
			throw new IndexOutOfBoundsException();

		Entry<Long,Tick> e = profile.floorEntry(t);

		if( e != null ) {
			double power = e.getValue().value;
			
			return e.getValue().positiveInt + e.getValue().negativeInt
					+ (t - e.getKey()) * power;
		} else {
			return 0.0;
		}
	}
	
	/**
	 * Calculates discrete integral from time 0 to t - only for positive values
	 * 
	 * @param t
	 * @return
	 */
	private double integralPositive(long t) {
		if( t > duration )
			throw new IndexOutOfBoundsException();

		Entry<Long,Tick> e = profile.floorEntry(t);
	
		if( e != null ) {
			double power = e.getValue().value;
			
			if (power >= 0.0)
			{
				assert(t >= e.getKey());
				return e.getValue().positiveInt	+ (t - e.getKey()) * power;
			}
			else return e.getValue().positiveInt;
		} else {
			return 0.0;
		}
	}
	
	/**
	 * Calculates discrete integral from time 0 to t - only for negative values
	 * 
	 * @param t
	 * @return
	 */
	private double integralNegative(long t) {
		if( t > duration )
			throw new IndexOutOfBoundsException();

		Entry<Long,Tick> e = profile.floorEntry(t);
	
		if( e != null ) {
			double power = e.getValue().value;
			
			if (power <= 0.0)
			{
				return e.getValue().negativeInt	+ (t - e.getKey()) * power;
			}
			else return e.getValue().negativeInt;
		} else {
			return 0.0;
		}
	}
	
	@Override
	public double sum(long from, long to) {
		return integral(to)-integral(from);
	}

	@Override
	public double sumPositive(long from, long to) {
		return integralPositive(to)-integralPositive(from);
	}

	@Override
	public double sumNegative(long from, long to) {
		return integralNegative(to)-integralNegative(from);
	}

	@Override
	public void sub(ITimeSeries operand, long offset) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public ITimeSeries cloneMe() {
		throw new UnsupportedOperationException("not yet implemented");
	}
}
