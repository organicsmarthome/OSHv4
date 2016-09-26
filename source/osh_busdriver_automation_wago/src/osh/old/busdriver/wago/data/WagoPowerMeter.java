package osh.old.busdriver.wago.data;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * A meter device of the wago xml interface
 * 
 * @author Kaibin Bao
 *
 */
@XmlType
public class WagoPowerMeter {
	@XmlTransient
	private int groupId;
	
	@XmlPath("@port")
	private int meterId;

	@XmlPath("voltage/@value")
	private double voltage;
	
	@XmlPath("current/@value")
	private double current;
	
	@XmlPath("power/@value")
	private double power;
	
	@XmlPath("energy/@value")
	private double energy;
	
	@XmlPath("transformerdivisor/@value")
	private double transformerdivisor;
	
	@XmlPath("dcfilter/@value")
	private boolean dcfilter;

	@XmlPath("power/@time")
	private long timestamp;

	public long getTimestamp() {
		return timestamp;
	}
	
	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getMeterId() {
		return meterId;
	}

	public double getVoltage() {
		return voltage;
	}

	public double getCurrent() {
		return current;
	}

	public double getPower() {
		return power;
	}

	public double getEnergy() {
		return energy;
	}

	public double getTransformerdivisor() {
		return transformerdivisor;
	}

	public boolean isDcfilter() {
		return dcfilter;
	}
}
