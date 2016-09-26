package osh.datatypes.power;

import osh.datatypes.commodity.Commodity;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
public class PowerProfileTick {
	
	public Commodity commodity;
	
	public long timeTick;
	
	public int load;
	
	@Override
	public String toString() {
		return "@" + timeTick + " load=" + load + " " + commodity.getUnit();
	}
}
