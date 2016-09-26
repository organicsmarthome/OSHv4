package osh.busdriver.mielegateway.data;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import osh.en50523.EN50523DeviceState;


/**
 * A Miele device on the XML homebus
 * 
 * @author Kaibin Bao
 *
 */
public class MieleDeviceHomeBusDataJSON {
	@JsonProperty("class")
	private int deviceClass;
	
	@JsonProperty("uid")
	private int uid;
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("state")
	private EN50523DeviceState state;
//	private int state;
	
	@JsonProperty("additionalName")
	private String additionalName;
	
	@JsonProperty("room")
	private String room;
	
	@JsonProperty("roomId")
	private String roomId;
	
	@JsonProperty("roomLevel")
	private String roomLevel;
	
	@JsonProperty("stateName")
	private String stateName;
	
	@JsonProperty("phaseName")
	private String phaseName;
	
	@JsonIgnore
	private MieleDuration duration;

	@JsonIgnore
	private MieleDuration startTime;
	
	@JsonIgnore
	private MieleDuration remainingTime;
	
	@JsonProperty("actions")
	private ArrayList<String> actions;
	
	@JsonProperty("detailsUrl")
	private String detailsUrl;

	@JsonProperty("deviceDetails")
	private MieleApplianceRawDataJSON deviceDetails;
	
	/* GETTERS */
	
	public int getDeviceClass() {
		return deviceClass;
	}

	public int getUid() {
		return uid;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public EN50523DeviceState getState() {
		return state;
	}

//	public int getState() {
//		return state;
//	}
	
	public String getAdditionalName() {
		return additionalName;
	}
	
	public String getRoomName() {
		return room;
	}

	public String getRoomId() {
		return roomId;
	}

	public String getRoomLevel() {
		return roomLevel;
	}

	public String getStateName() {
		return stateName;
	}

	public String getPhaseName() {
		return phaseName;
	}

	public MieleDuration getDuration() {
		return duration;
	}
	
	public MieleDuration getStartTime() {
		return startTime;
	}

	public MieleDuration getRemainingTime() {
		return remainingTime;
	}

	public String getDetailsUrl() {
		return detailsUrl;
	}
	
	public MieleApplianceRawDataJSON getDeviceDetails() {
		return deviceDetails;
	}
	
	/* SETTERS */
	
	public void setDeviceDetails(MieleApplianceRawDataJSON deviceDetails) {
		this.deviceDetails = deviceDetails;
	}
	
	@JsonProperty("duration")
	public void setDuration(Integer duration) {
    	if( duration != null ) {
    		this.duration = new MieleDuration(duration);
    	} else {
    		this.duration = null;
    	}
	}
	
	@JsonProperty("startTime")
	public void setStartTime(Integer startTime) {
    	if( startTime != null ) {
    		this.startTime = new MieleDuration(startTime);
    	} else {
    		this.startTime = null;
    	}
	}
	
	@JsonProperty("remainingTime")
    public void setRemainingTime(Integer remainingTime) {
    	if( remainingTime != null ) {
    		this.remainingTime = new MieleDuration(remainingTime);
    	} else {
    		this.remainingTime = null;
    	}
	}
	
	public ArrayList<String> getActions() {
		return actions;
	}

	public void setActions(ArrayList<String> actions) {
		this.actions = actions;
	}

	@Override
	public String toString() {
		return String.format("miele device %x, class %x, state %s", uid, deviceClass, state.toString());
	}
}
