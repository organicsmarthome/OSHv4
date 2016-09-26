package osh.mgmt.localobserver.miele;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import osh.datatypes.registry.oc.state.IAction;
import osh.mgmt.localobserver.ipp.MieleApplianceIPP;


/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement
public class MieleAction implements IAction {

	private UUID deviceID;
	private long programmedAt;
	private MieleApplianceIPP ipp;

	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private MieleAction() {}

	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param programmedAt
	 * @param eapart
	 */
	public MieleAction(
			UUID deviceID, 
			long programmedAt, 
			MieleApplianceIPP ipp) {
		this.deviceID = deviceID;
		this.programmedAt = programmedAt;
		this.ipp = ipp;
	}

	@Override
	public UUID getDeviceId() {
		return deviceID;
	}

	@Override
	public long getTimestamp() {
		return programmedAt;
	}

	public MieleApplianceIPP getIPP() {
		return ipp;
	}
	
	@Override
	public boolean equals(IAction other) {
		if( !(other instanceof MieleAction) )
			return false;
		
		MieleAction otherMieleAction = (MieleAction) other;
		
		if( !(deviceID.equals(otherMieleAction.getDeviceId()) ) )
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return deviceID.hashCode();
	}

	// TODO: keep for prediction
//	@Override
//	public IAction createAction(long newTimestamp) {
//		MieleEAPart newEAPart = new MieleEAPart(deviceID,newTimestamp,newTimestamp,newTimestamp+eapart.getOriginalDof(),eapart.getProfile(), true);
//
//		return new MieleAction(deviceID, newTimestamp, newEAPart);
//	}

}
