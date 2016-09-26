package osh.datatypes.registry.driver.details.appliance.miele;

import java.util.UUID;



import osh.datatypes.registry.StateExchange;


/**
 * Communication program duration, starting time, and remaining time
 * @author Kaibin Bao
 *
 */
public class MieleApplianceDriverDetails extends StateExchange {

	/**  */
	private static final long serialVersionUID = 5818061135587946337L;
	protected long expectedProgramDuration;
	protected long startTime;
	protected long programRemainingTime;

	
	/**
	 * CONSTRUCTOR
	 */
	public MieleApplianceDriverDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	/**
	 * gets the program duration in seconds 
	 * 
	 * @param expectedProgramDuration
	 */
	public long getExpectedProgramDuration() {
		return expectedProgramDuration;
	}

	/**
	 * sets the program duration in seconds 
	 * 
	 * @param expectedProgramDuration
	 */
	public void setExpectedProgramDuration(long expectedProgramDuration) {
		this.expectedProgramDuration = expectedProgramDuration;
	}

	/**
	 * gets the start time from the timer set by the user
	 * 
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * setter for startTime
	 * 
	 * @param startTime
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * gets the remaining program time in seconds 
	 * 
	 * @param expectedProgramDuration
	 */
	public long getProgramRemainingTime() {
		return programRemainingTime;
	}

	/**
	 * sets the remaining program time in seconds 
	 * 
	 * @param expectedProgramDuration
	 */
	public void setProgramRemainingTime(long programTimeLeft) {
		this.programRemainingTime = programTimeLeft;
	}
	
}
