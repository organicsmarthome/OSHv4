package osh.datatypes.registry.oc.state;

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public class MieleDofStateExchange extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1795073842080845700L;
	/** the duration, NOT an absolute point in time! */
	private long lastDof;
	private long earliestStartTime;
	private long latestStartTime;
	private long expectedStartTime;
	
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 * @param lastDof
	 * @param earliestStartTime
	 * @param latestStartTime
	 * @param expectedStartTime
	 */
	public MieleDofStateExchange(
			UUID sender, 
			long timestamp, 
			long lastDof, 
			long earliestStartTime, 
			long latestStartTime, 
			long expectedStartTime) {
		super(sender, timestamp);
		
		this.lastDof = lastDof;
		this.earliestStartTime = earliestStartTime;
		this.latestStartTime = latestStartTime;
		this.expectedStartTime = expectedStartTime;
	}

	/**
	 * returns the last set degree of freedom in seconds as duration
	 */
	@XmlElement
	public long getLastDof() {
		return lastDof;
	}
	@XmlElement
	public long getEarliestStartTime() {
		return earliestStartTime;
	}
	@XmlElement
	public long getLatestStartTime() {
		return latestStartTime;
	}
	@XmlElement
	public long getExpectedStartTime() {
		return expectedStartTime;
	}

	
}
