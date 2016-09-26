package osh.driver.meter;

import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class BcontrolHeaterDataSwitches {
	
	@XmlElement(name="power")
	private int power;
	
	@XmlElement(name="operating_seconds")
	private int operating_seconds;

	@XmlElement(name="switching_cycles")
	private int switching_cycles;
	
	@XmlElement(name="min_on_time")
	private int min_on_time;
	
	@XmlElement(name="min_off_time")
	private int min_off_time;
	
	@XmlElement(name="state")
	private String state;

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

	public int getOperating_seconds() {
		return operating_seconds;
	}

	public void setOperating_seconds(int operating_seconds) {
		this.operating_seconds = operating_seconds;
	}

	public int getSwitching_cycles() {
		return switching_cycles;
	}

	public void setSwitching_cycles(int switching_cycles) {
		this.switching_cycles = switching_cycles;
	}

	public int getMin_on_time() {
		return min_on_time;
	}

	public void setMin_on_time(int min_on_time) {
		this.min_on_time = min_on_time;
	}

	public int getMin_off_time() {
		return min_off_time;
	}

	public void setMin_off_time(int min_off_time) {
		this.min_off_time = min_off_time;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
}
