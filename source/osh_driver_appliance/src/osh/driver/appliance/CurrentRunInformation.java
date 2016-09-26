package osh.driver.appliance;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class CurrentRunInformation {
	
	private int currentProfileID = -1;
	private int currentSegment = -1;
	private boolean currentlyRunningPhase = false; // false: PAUSE, true: PHASE
	private int currentTickCounter = 0;
	
	public CurrentRunInformation(
			int currentProfileID, 
			int currentSegment,
			boolean currentlyRunningPhase, 
			int currentTickCounter) {
		super();
		
		this.currentProfileID = currentProfileID;
		this.currentSegment = currentSegment;
		this.currentlyRunningPhase = currentlyRunningPhase;
		this.currentTickCounter = currentTickCounter;
	}

	public int getCurrentProfileID() {
		return currentProfileID;
	}

	public void setCurrentProfileID(int currentProfileID) {
		this.currentProfileID = currentProfileID;
	}

	public int getCurrentSegment() {
		return currentSegment;
	}

	public void setCurrentSegment(int currentSegment) {
		this.currentSegment = currentSegment;
	}

	public boolean isCurrentlyRunningPhase() {
		return currentlyRunningPhase;
	}

	public void setCurrentlyRunningPhase(boolean currentlyRunningPhase) {
		this.currentlyRunningPhase = currentlyRunningPhase;
	}

	public int getCurrentTickCounter() {
		return currentTickCounter;
	}

	public void setCurrentTickCounter(int currentTickCounter) {
		this.currentTickCounter = currentTickCounter;
	}
	
}
