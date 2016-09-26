package osh.busdriver.mielegateway.data;

import java.net.URL;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * The XML homebus device detail root node
 * 
 * @author Kaibin Bao
 *
 */
@XmlRootElement(name="device")
public class MieleApplianceRawDataREST {
	@XmlPath("information/key[@name='Appliance Type']/@value")
	private String applianceTypeName;
	
	@XmlPath("information/key[@name='State']/@value")
	private String stateName;

	@XmlPath("information/key[@name='Program']/@value")
	private String programName;
	
	@XmlPath("information/key[@name='Phase']/@value")
	private String phaseName;
	
	@XmlPath("information/key[@name='Start Time']/@value")
	private MieleDuration startTime;
	
	@XmlPath("information/key[@name='Smart Start']/@value")
	private MieleDuration smartStartTime;
	
	@XmlPath("information/key[@name='Remaining Time']/@value")
	private MieleDuration remainingTime;
	
	@XmlPath("information/key[@name='Duration']/@value")
	private MieleDuration duration;
	
	@XmlPath("information/key[@name='End Time']/@value")
	private MieleDuration endTime;
	
	/* SPECIFIC INFORMATION */
	
	
	
	/* COMMAND URLS */
	
	@XmlPath("actions/action[@name='Stop']/@URL")
	private URL stopCommandUrl;

	@XmlPath("actions/action[@name='Start']/@URL")
	private URL startCommandUrl;
	
	@XmlPath("actions/action[@name='Light On']/@URL")
	private URL lightOnCommandUrl;

	@XmlPath("actions/action[@name='Light Off']/@URL")
	private URL lightOffCommandUrl;
	
	/* GETTERS */
	
	public String getApplianceTypeName() {
		return applianceTypeName;
	}

	public String getStateName() {
		return stateName;
	}

	public String getProgramName() {
		return programName;
	}

	public String getPhaseName() {
		return phaseName;
	}

	public MieleDuration getStartTime() {
		return startTime;
	}

	public MieleDuration getSmartStartTime() {
		return smartStartTime;
	}

	public MieleDuration getRemainingTime() {
		return remainingTime;
	}

	public MieleDuration getDuration() {
		return duration;
	}

	public MieleDuration getEndTime() {
		return endTime;
	}

	public URL getStopCommandUrl() {
		return stopCommandUrl;
	}

	public URL getStartCommandUrl() {
		return startCommandUrl;
	}

	public URL getLightOnCommandUrl() {
		return lightOnCommandUrl;
	}

	public URL getLightOffCommandUrl() {
		return lightOffCommandUrl;
	}
}
