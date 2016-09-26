package osh.datatypes.power;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
public interface ILoadProfile<T extends Enum<T>> {
	
	/**
	 * Returns the EndingTimeOfProfile. EndingTimeOfProfile is defined as the point in time where
	 * the profile stops having a value other than 0, NOT the length of the profile.
	 * E. g.: if the profile starts at 5 with a power of 1 and has a duration of 10, the
	 * power value at 10 will be 0. If the profile starts at -10 with a value of 1 and the
	 * duration is 10, the power at 9 will be 1, and the energy will be 20*10 units*s. 
	 * @return duration
	 */
	public long getEndingTimeOfProfile();
	
	public int getLoadAt(T commodity, long t);
	
	public Long getNextLoadChange(T commodity, long t);
	
	public ILoadProfile<T> merge( ILoadProfile<T> other, long offset );
	
	/**
	 * cuts off all ticks with a negative time and inserts a tick at time 0
	 * if necessary. This function changes the current object.
	 */
	public void cutOffNegativeTimeValues();
	
	public ILoadProfile<T> clone();
	
	public String toStringShort();
}
