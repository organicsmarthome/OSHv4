package osh.mgmt.mox;

import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.en50523.EN50523DeviceState;

/**
 * 
 * @author Sebastian Kramer
 *
 */
public class MieleApplianceMOX implements IModelOfObservationExchange {

	/**
	 * SparseLoadProfile containing different profile with different commodities<br>
	 * IMPORATANT: RELATIVE TIMES!
	 */
	private SparseLoadProfile currentProfile;
	private EN50523DeviceState currentState;
	
	private long profileStarted = -1;
	private long programmedAt = -1;
	
	private LoadProfileCompressionTypes compressionType;
	private int compressionValue;
	
	public MieleApplianceMOX(SparseLoadProfile currentProfile, EN50523DeviceState currentState, 
			long profileStarted, long programmedAt, LoadProfileCompressionTypes compressionType,
			int compressionValue) {
		super();
		this.currentProfile = currentProfile;
		this.currentState = currentState;
		this.profileStarted = profileStarted;
		this.programmedAt = programmedAt;
		this.compressionType = compressionType;
		this.compressionValue = compressionValue;
	}

	public SparseLoadProfile getCurrentProfile() {
		return currentProfile;
	}

	public EN50523DeviceState getCurrentState() {
		return currentState;
	}

	public long getProfileStarted() {
		return profileStarted;
	}

	public long getProgrammedAt() {
		return programmedAt;
	}

	public LoadProfileCompressionTypes getCompressionType() {
		return compressionType;
	}

	public int getCompressionValue() {
		return compressionValue;
	}
	
	


}
