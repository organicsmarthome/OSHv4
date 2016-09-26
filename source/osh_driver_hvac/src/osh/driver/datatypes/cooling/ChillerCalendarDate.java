package osh.driver.datatypes.cooling;

import java.io.Serializable;

/**
 * 
 * @author Julian Feder, Ingo Mauser
 *
 */
public class ChillerCalendarDate implements Serializable {

	private static final long serialVersionUID = 8538462160812879806L;
	
	private long startTimestamp;
	private long length;
	private int amountOfPerson;
	private double setTemperature;
	private int knownPower;
	
	
	/**
	 * CONSTRUCTOR 
	 */
	public ChillerCalendarDate(long startTimestamp, long length, int amountOfPerson, double setTemperature, int knownPower) {
		this.startTimestamp = startTimestamp;
		this.length = length;
		this.amountOfPerson = amountOfPerson;
		this.setTemperature = setTemperature;
		this.knownPower = knownPower;
	}
	
	
	//GETTER METHODS
	public long getStartTimestamp() {
        return this.startTimestamp;
    }
	
	public long getlength() {
        return this.length;
    }

	public int getAmountOfPerson() {
		return this.amountOfPerson;
	}
	
	public double getSetTemperature() {
		return setTemperature;
	}
	
	public int getKnownPower() {
		return knownPower;
	}
}