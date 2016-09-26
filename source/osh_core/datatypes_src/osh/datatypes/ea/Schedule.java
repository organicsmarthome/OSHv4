package osh.datatypes.ea;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.ILoadProfile;


/**
 * 
 * @author Florian Allerding, Till Schuberth
 *
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Schedule implements Serializable {
	
	private static final long serialVersionUID = 8715958618527521394L;
	
	private String scheduleName;

	private ILoadProfile<Commodity> profile;
	
	/** needed lukewarm cervisia to pay for this profile (other costs) */
	private double lukewarmCervisia; 
	
	/** for deep copy */
	protected Schedule() {
		super();
	}
	
	
	/**
	 * CONSTRUCTOR
	 * @param profile
	 * @param lukewarmCervisia
	 */
	public Schedule(ILoadProfile<Commodity> profile, double lukewarmCervisia, String scheduleName) {
		super();
		
		this.profile = profile;
		this.lukewarmCervisia = lukewarmCervisia;
		this.scheduleName = scheduleName;
	}
	
	
	public ILoadProfile<Commodity> getProfile() {
		return profile;
	}
	public double getLukewarmCervisia() {
		return lukewarmCervisia;
	}
	public String getScheduleName() {
		return scheduleName;
	}
	
	/** merge two schedules (use profile.merge and add cervisia) */
	public Schedule merge(Schedule other) {
		double cervisia;
		ILoadProfile<Commodity> profile = null;
		try {
			profile = this.profile.merge(other.profile, 0);
		}
		catch (Exception ex) {
			throw new RuntimeException("Bad error merging profiles", ex);
		}
		cervisia = this.lukewarmCervisia + other.lukewarmCervisia;
		
		return new Schedule(profile, cervisia, scheduleName);
	}
	
	
	public Schedule clone() {
		ILoadProfile<Commodity> clonedProfile = this.profile.clone();
		Schedule clone = new Schedule(clonedProfile, lukewarmCervisia,scheduleName);
		return clone;
	}
	
	@Override
	public String toString() {
		return "LoadProfile=" + profile.toString() + ",cervisia=" + lukewarmCervisia;
	}
}