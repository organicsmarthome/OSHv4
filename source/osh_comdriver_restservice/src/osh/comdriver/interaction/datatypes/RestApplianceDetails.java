package osh.comdriver.interaction.datatypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import osh.en50523.EN50523DeviceState;

/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="applianceDetails")
public class RestApplianceDetails extends RestStateDetail {
	
	protected long startTime;
	
	protected long endTime;
	
	protected int remainingTime;
	
	protected int expectedProgramDuration;
	
	protected String programName;
	
	protected String phaseName;
	
	@Enumerated(value=EnumType.STRING)
	protected EN50523DeviceState state;
	
	protected String stateTextDE;
	
	protected Map<String, String> programExtras;
	
	protected Map<String, String> actions;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestApplianceDetails() {
		this(null, 0);
	};

	public RestApplianceDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
		programExtras = new HashMap<String, String>();
	}
	
	

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getExpectedProgramDuration() {
		return expectedProgramDuration;
	}

	public void setExpectedProgramDuration(int expectedProgramDuration) {
		this.expectedProgramDuration = expectedProgramDuration;
	}
	
	public String getStateTextDE() {
		return stateTextDE;
	}

	public void setStateTextDE(String stateTextDE) {
		this.stateTextDE = stateTextDE;
	}

	public EN50523DeviceState getState() {
		return state;
	}

	public void setState(EN50523DeviceState state) {
		this.state = state;
		setStateTextDE(state.getDescriptionDE());
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public String getPhaseName() {
		return phaseName;
	}

	public void setPhaseName(String phaseName) {
		this.phaseName = phaseName;
	}

	public Map<String, String> getProgramExtras() {
		return programExtras;
	}

	public void setProgramExtras(Map<String, String> programExtras) {
		this.programExtras = programExtras;
	}
	
	public String getProgramExtra( String key ) {
		return programExtras.get(key);
	}
	
	public void setProgramExtra( String key, String value ) {
		programExtras.put(key, value);
	}
	
	public void setActions(Map<String, String> actions) {
		this.actions = actions;
	}
	
	public Map<String, String> getActions() {
		return actions;
	}

	public int getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(int remainingTime) {
		this.remainingTime = remainingTime;
	}
	
	
}
