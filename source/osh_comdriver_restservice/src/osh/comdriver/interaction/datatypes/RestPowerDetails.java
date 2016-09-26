package osh.comdriver.interaction.datatypes;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="powerDetails")
public class RestPowerDetails extends RestStateDetail {
	
	protected double activePower;
	
	protected double reactivePower;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestPowerDetails() {
		this(null, 0);
	}
	
	public RestPowerDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	public double getActivePower() {
		return activePower;
	}

	public void setActivePower(double activePower) {
		this.activePower = activePower;
	}

	public double getReactivePower() {
		return reactivePower;
	}

	public void setReactivePower(double reactivePower) {
		this.reactivePower = reactivePower;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj == null )
			return false;
		if( !(obj instanceof RestPowerDetails) )
			return false;
		RestPowerDetails other = (RestPowerDetails) obj;
		
		return  (this.activePower == other.activePower) &&
			    (this.reactivePower == other.reactivePower);
	}
	
	@Override
	public String toString() {
		return "Power: { " +
				"P=" + getActivePower() + "W " +
				"Q=" + getReactivePower() + "var " +
				"}" ;
	}
}
