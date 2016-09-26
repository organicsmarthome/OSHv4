package osh.datatypes.ea;

import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.interfaces.ITimeSeries;
import osh.datatypes.power.PowerProfileTick;


/**
 * Representation of the power consumption (predicted or recorded)
 * 
 */
public class LoadProfileEA {

	protected EnumMap<Commodity, ITimeSeries> profiles;
	
	protected ITimeSeries voltageProfile;
	protected ITimeSeries currentProfile;

	/**
	 * default constructor
	 * 
	 */
	public LoadProfileEA() {
		this.profiles = new EnumMap<>(Commodity.class);
	}
	
	/**
	 * convenience constructor
	 * 
	 * @param powerProfile
	 */
	public LoadProfileEA(EnumMap<Commodity,List<PowerProfileTick>> powerProfiles) {
		this();
		
		
		for (Entry<Commodity,List<PowerProfileTick>> e : powerProfiles.entrySet()) {
			List<PowerProfileTick> list = e.getValue();
			Commodity commodity = e.getKey();
			
			ITimeSeries currentSeries = new ArrayTimeSeries();
			currentSeries.setLength(list.size());
			
			long t = 0;
			for( PowerProfileTick _p : list ) {
				currentSeries.set(t, _p.load);
				t++;
			}
			
			this.profiles.put(commodity, currentSeries);
		}
		
	}
	
	/* OPERATIONS */
	
	/**
	 * Function addition of two load profiles.
	 * Result stored in this object.
	 * Operand is shifted by offset first before operation. 
	 * 
	 * @param operand
	 * @param offset
	 */
	public void add(LoadProfileEA operand, long offset) {
		
		if( this.profiles.get(Commodity.ACTIVEPOWER) != null && operand.profiles.get(Commodity.ACTIVEPOWER) != null )
			this.profiles.get(Commodity.ACTIVEPOWER).add(operand.profiles.get(Commodity.ACTIVEPOWER), offset);
		
		if( this.profiles.get(Commodity.REACTIVEPOWER) != null && operand.profiles.get(Commodity.REACTIVEPOWER) != null )
			this.profiles.get(Commodity.REACTIVEPOWER).add(operand.profiles.get(Commodity.REACTIVEPOWER), offset);
		
		if( this.voltageProfile != null && operand.voltageProfile != null )
			this.voltageProfile.add(operand.voltageProfile, offset);
		if( this.currentProfile != null && operand.currentProfile != null )
			this.currentProfile.add(operand.currentProfile, offset);
	}
	
	/**
	 * Function multiplication of two load profiles.
	 * Result stored in this object 
	 * Operand is shifted by offset first before operation. 
	 * 
	 * @param operand
	 * @param offset
	 */
	public void multiply(LoadProfileEA operand, long offset) {
		
		if( this.profiles.get(Commodity.ACTIVEPOWER) != null && operand.profiles.get(Commodity.ACTIVEPOWER) != null )
			this.profiles.get(Commodity.ACTIVEPOWER).multiply(operand.profiles.get(Commodity.ACTIVEPOWER), offset);
		
		if( this.profiles.get(Commodity.REACTIVEPOWER) != null && operand.profiles.get(Commodity.REACTIVEPOWER) != null )
			this.profiles.get(Commodity.REACTIVEPOWER).multiply(operand.profiles.get(Commodity.REACTIVEPOWER), offset);
		
		if( this.voltageProfile != null && operand.voltageProfile != null )
			this.voltageProfile.multiply(operand.voltageProfile, offset);
		if( this.currentProfile != null && operand.currentProfile != null )
			this.currentProfile.multiply(operand.currentProfile, offset);
	}
	
	/* GETTERS / SETTERS */
	
	public ITimeSeries getActivePowerProfile() {
		return this.profiles.get(Commodity.ACTIVEPOWER);
	}
	public void setActivePowerProfile(ITimeSeries activePowerProfile) {
		this.profiles.put(Commodity.ACTIVEPOWER, activePowerProfile);
	}
	public ITimeSeries getReactivePowerProfile() {
		return this.profiles.get(Commodity.REACTIVEPOWER);
	}
	public void setReactivePowerProfile(ITimeSeries reactivePowerProfile) {
		this.profiles.put(Commodity.REACTIVEPOWER, reactivePowerProfile);
	}
	public ITimeSeries getVoltageProfile() {
		return voltageProfile;
	}
	public void setVoltageProfile(ITimeSeries voltageProfile) {
		this.voltageProfile = voltageProfile;
	}
	public ITimeSeries getCurrentProfile() {
		return currentProfile;
	}
	public void setCurrentProfile(ITimeSeries currentProfile) {
		this.currentProfile = currentProfile;
	}
}
